package com.hanclip.android.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import com.hanai.onnx.DinoImageFeatures
import com.hanai.onnx.DinoImagePreprocessor
import com.hanclip.android.core.model.ClipItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

/**
 * Computes transient DINO visual feature vectors for a batch of imported photos/live-photo stills,
 * used only as an additional AND veto on top of the existing fingerprint similarity grouping.
 * Any failure anywhere in the batch yields a null result so callers fall back to the exact
 * pre-existing fingerprint-only policy rather than mixing partial results.
 */
object HanYangVisualFeatures {

    private const val MODEL_ASSET_PATH = "hanai/dinov2-small.onnx"
    private const val MODEL_FILE_NAME = "dinov2-small.onnx"
    private const val EXPECTED_MODEL_SIZE = 88400292L
    private const val EXPECTED_MODEL_SHA256 = "6b3e37fdea1393dda0ea0312f5f19b20829ca96447ab5b8b3c92a36777fc431d"

    private const val MAX_DIMENSION = 16384
    private const val MAX_PIXELS = 16_000_000L
    private const val MIN_DECODE_SHORT_EDGE = 256
    private const val MAX_DECODE_LONG_EDGE = 2048

    private val sessionLock = Any()
    @Volatile private var cachedSession: DinoImageFeatures? = null

    suspend fun computeBatch(context: Context, clips: List<ClipItem>): Map<String, FloatArray>? {
        if (clips.isEmpty()) return emptyMap()
        return try {
            withContext(Dispatchers.Default) {
                val session = obtainSession(context)
                val result = LinkedHashMap<String, FloatArray>(clips.size)
                for (clip in clips) {
                    currentCoroutineContext().ensureActive()
                    val uri = clip.thumbnailUri ?: clip.sourceUri
                    val decoded = decodeRgb8(context, uri) ?: return@withContext null
                    val (rgb, width, height) = decoded
                    val input = DinoImagePreprocessor.normalizedInput(rgb, width, height)
                    currentCoroutineContext().ensureActive()
                    val embedding = synchronized(sessionLock) { session.infer(input) }
                    result[clip.id] = embedding
                }
                result
            }
        } catch (interrupted: InterruptedException) {
            throw interrupted
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Throwable) {
            null
        }
    }

    private fun obtainSession(context: Context): DinoImageFeatures {
        cachedSession?.let { return it }
        synchronized(sessionLock) {
            cachedSession?.let { return it }
            val modelFile = ensureModelFile(context.applicationContext)
            val canonicalBase = context.applicationContext.noBackupFilesDir.canonicalFile
            val canonicalTarget = modelFile.canonicalFile
            check(canonicalTarget.isFile) { "Model file is not a regular file" }
            check(canonicalTarget.toPath().startsWith(canonicalBase.toPath())) {
                "Model file escapes app-private storage"
            }
            val session = DinoImageFeatures.open(canonicalTarget)
            cachedSession = session
            return session
        }
    }

    private fun ensureModelFile(appContext: Context): File {
        val directory = File(appContext.noBackupFilesDir, "hanai").apply { mkdirs() }
        val target = File(directory, MODEL_FILE_NAME)
        if (target.isFile && target.length() == EXPECTED_MODEL_SIZE) {
            return target
        }
        val staging = File(directory, "$MODEL_FILE_NAME.tmp-${UUID.randomUUID()}")
        try {
            appContext.assets.open(MODEL_ASSET_PATH).use { input ->
                FileOutputStream(staging).use { output ->
                    val digest = MessageDigest.getInstance("SHA-256")
                    val buffer = ByteArray(65536)
                    var total = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        digest.update(buffer, 0, read)
                        total += read
                    }
                    output.fd.sync()
                    check(total == EXPECTED_MODEL_SIZE) { "Model asset size mismatch" }
                    val actualHash = digest.digest().joinToString("") { "%02x".format(it) }
                    check(actualHash.equals(EXPECTED_MODEL_SHA256, ignoreCase = true)) {
                        "Model asset SHA-256 mismatch"
                    }
                }
            }
            check(staging.renameTo(target)) { "Failed to publish model asset" }
        } finally {
            staging.delete()
        }
        return target
    }

    private data class DecodedRgb(val rgb: ByteArray, val width: Int, val height: Int)

    private fun decodeRgb8(context: Context, uri: Uri): DecodedRgb? {
        val bitmap = decodeBoundedBitmap(context, uri) ?: return null
        return try {
            val width = bitmap.width
            val height = bitmap.height
            if (width <= 0 || height <= 0) return null
            if (max(width, height) > MAX_DECODE_LONG_EDGE) return null
            if (min(width, height) < MIN_DECODE_SHORT_EDGE) return null
            if (width.toLong() * height.toLong() > MAX_PIXELS) return null
            DecodedRgb(bitmapToRgb8(bitmap), width, height)
        } finally {
            bitmap.recycle()
        }
    }

    private fun decodeBoundedBitmap(context: Context, uri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            runCatching {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = false
                    decoder.setTargetColorSpace(android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.SRGB))
                    val srcWidth = info.size.width.coerceAtLeast(1)
                    val srcHeight = info.size.height.coerceAtLeast(1)
                    if (max(srcWidth, srcHeight) > MAX_DIMENSION) {
                        throw IllegalArgumentException("Source image exceeds bounded decode limits")
                    }
                    val target = targetDecodeSize(srcWidth, srcHeight)
                        ?: throw IllegalArgumentException("Source image cannot satisfy decode bounds")
                    decoder.setTargetSize(target.first, target.second)
                }
            }.getOrNull()
        } else {
            decodeBoundedBitmapLegacy(context, uri)
        }
    }

    private fun decodeBoundedBitmapLegacy(context: Context, uri: Uri): Bitmap? {
        return runCatching {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            val boundsStreamOpened = context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
                Unit
            } != null
            if (!boundsStreamOpened) return null
            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                return null
            }
            val srcWidth = boundsOptions.outWidth
            val srcHeight = boundsOptions.outHeight
            if (max(srcWidth, srcHeight) > MAX_DIMENSION) {
                return null
            }
            val (targetWidth, targetHeight) = targetDecodeSize(srcWidth, srcHeight) ?: return null
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(srcWidth, srcHeight, targetWidth, targetHeight)
            }
            val rawBitmap = context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return null
            val sampledLongEdge = max(rawBitmap.width, rawBitmap.height)
            val resizedBitmap = if (sampledLongEdge > MAX_DECODE_LONG_EDGE) {
                val scale = MAX_DECODE_LONG_EDGE.toDouble() / sampledLongEdge.toDouble()
                val scaledWidth = max(1, (rawBitmap.width * scale).toInt())
                val scaledHeight = max(1, (rawBitmap.height * scale).toInt())
                val scaled = Bitmap.createScaledBitmap(rawBitmap, scaledWidth, scaledHeight, true)
                if (scaled !== rawBitmap) rawBitmap.recycle()
                scaled
            } else {
                rawBitmap
            }
            applyExifOrientation(resizedBitmap, readExifOrientation(context, uri))
        }.getOrNull()
    }

    private fun readExifOrientation(context: Context, uri: Uri): Int {
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated !== bitmap) bitmap.recycle()
        return rotated
    }

    private fun calculateInSampleSize(srcWidth: Int, srcHeight: Int, targetWidth: Int, targetHeight: Int): Int {
        var inSampleSize = 1
        if (srcHeight > targetHeight || srcWidth > targetWidth) {
            val halfHeight = srcHeight / 2
            val halfWidth = srcWidth / 2
            while ((halfHeight / inSampleSize) >= targetHeight && (halfWidth / inSampleSize) >= targetWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun targetDecodeSize(width: Int, height: Int): Pair<Int, Int>? {
        val longEdge = max(width, height)
        val shortEdge = min(width, height)
        val scale = if (longEdge > MAX_DECODE_LONG_EDGE) MAX_DECODE_LONG_EDGE.toDouble() / longEdge.toDouble() else 1.0
        if (shortEdge * scale < MIN_DECODE_SHORT_EDGE) return null
        val targetWidth = max(1, (width * scale).toInt())
        val targetHeight = max(1, (height * scale).toInt())
        return targetWidth to targetHeight
    }

    private fun bitmapToRgb8(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val out = ByteArray(width * height * 3)
        var offset = 0
        for (pixel in pixels) {
            out[offset] = ((pixel shr 16) and 0xFF).toByte()
            out[offset + 1] = ((pixel shr 8) and 0xFF).toByte()
            out[offset + 2] = (pixel and 0xFF).toByte()
            offset += 3
        }
        return out
    }
}
