package com.hanclip.android.core.project

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hanclip.android.core.model.ClipItem
import com.hanclip.android.core.model.MoviePreset
import com.hanclip.android.core.model.OutputQualityPreset
import com.hanclip.android.core.model.VideoSegmentMode
import com.hanclip.android.core.model.WatermarkSettings
import com.hanclip.android.core.media.MediaImportReader
import com.hanclip.android.core.media.CaptionTypefaceLoader
import com.hanclip.android.R
import org.junit.After
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ProjectPersistenceInstrumentedTest {
    private val baseContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var context: Context
    private lateinit var filesRoot: File
    private lateinit var preferencePrefix: String

    @Before
    fun prepareIsolatedStorage() {
        preferencePrefix = "persistence-test-${UUID.randomUUID()}"
        filesRoot = File(baseContext.cacheDir, preferencePrefix)
        check(filesRoot.mkdirs() || filesRoot.isDirectory)
        context = object : ContextWrapper(baseContext) {
            override fun getFilesDir(): File = filesRoot
            override fun getSharedPreferences(name: String, mode: Int) =
                baseContext.getSharedPreferences("$preferencePrefix-$name", mode)
        }
    }

    @After
    fun cleanTestStorage() {
        if (::context.isInitialized) DraftProjectStore.clear(context)
        if (::filesRoot.isInitialized) filesRoot.deleteRecursively()
        if (::preferencePrefix.isInitialized) {
            listOf("hanclip_draft_project", "hanclip_editable_projects").forEach { name ->
                baseContext.getSharedPreferences("$preferencePrefix-$name", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .commit()
            }
        }
    }

    @Test
    fun legacyFixtureMigratesDefaultsWithoutLosingClip() {
        val fixture = InstrumentationRegistry.getInstrumentation().context.assets
            .open("fixtures/project-v0.json")
            .bufferedReader()
            .use { it.readText() }
        context.getSharedPreferences("hanclip_draft_project", Context.MODE_PRIVATE)
            .edit()
            .putString("draft", fixture)
            .commit()

        val migrated = DraftProjectStore.load(context)

        assertNotNull(migrated)
        assertEquals(1, migrated?.schemaVersion)
        assertEquals(VideoSegmentMode.Multiple, migrated?.defaultVideoSegmentMode)
        assertEquals(listOf("legacy-photo"), migrated?.clips?.map(ClipItem::id))
    }

    @Test
    fun mixedProjectFixtureKeepsKindsOrderSelectionAndGroupingAcrossSaveReload() {
        val fixture = InstrumentationRegistry.getInstrumentation().context.assets
            .open("fixtures/project-v1-mixed.json")
            .bufferedReader()
            .use { it.readText() }
        context.getSharedPreferences("hanclip_draft_project", Context.MODE_PRIVATE)
            .edit()
            .putString("draft", fixture)
            .commit()

        val loaded = requireNotNull(DraftProjectStore.load(context))
        DraftProjectStore.save(context, loaded)
        val reloaded = requireNotNull(DraftProjectStore.load(context))

        assertEquals(
            listOf("photo-1", "video-parent", "video-child-excluded", "motion-1", "similar-parent", "similar-child"),
            reloaded.clips.map(ClipItem::id)
        )
        assertEquals(false, reloaded.clips.first { it.id == "video-child-excluded" }.isVideoSegmentSelected)
        assertEquals(true, reloaded.clips.first { it.id == "motion-1" }.isLivePhoto)
        assertEquals("group-1", reloaded.clips.first { it.id == "similar-child" }.similarPhotoGroupId)
        assertEquals(false, reloaded.clips.first { it.id == "similar-child" }.isSimilarPhotoGroupRepresentative)
    }

    @Test
    fun userAssetFixtureKeepsProjectIconAndFallsBackWhenImportedFontDisappears() {
        val fontSource = File(context.filesDir, "fixture-font.ttf")
        baseContext.assets.open("fonts/poppins_regular.ttf").use { input ->
            FileOutputStream(fontSource).use { output ->
                input.copyTo(output)
                output.fd.sync()
            }
        }
        val importedFont = ImportedFontStore.import(context, Uri.fromFile(fontSource))
        fun writeIcon(file: File, color: Int) {
            FileOutputStream(file).use { output ->
                val bitmap = Bitmap.createBitmap(6, 6, Bitmap.Config.ARGB_8888)
                try {
                    bitmap.eraseColor(color)
                    assertEquals(true, bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
                } finally {
                    bitmap.recycle()
                }
                output.fd.sync()
            }
        }
        val firstIconSource = File(context.filesDir, "first-icon.png")
        writeIcon(firstIconSource, android.graphics.Color.MAGENTA)
        val globalIconPath = CopyrightIconStore.persist(context, Uri.fromFile(firstIconSource))
        val fixture = InstrumentationRegistry.getInstrumentation().context.assets
            .open("fixtures/project-v4-user-assets.json")
            .bufferedReader()
            .use { it.readText() }
            .replace("__IMPORTED_FONT_ID__", importedFont.id)
            .replace("__CUSTOM_ICON_PATH__", globalIconPath)
        context.getSharedPreferences("hanclip_draft_project", Context.MODE_PRIVATE)
            .edit()
            .putString("draft", fixture)
            .commit()

        val stored = EditableProjectStore.upsert(context, requireNotNull(DraftProjectStore.load(context)))
        val projectIcon = File(stored.watermarkSettings.customCopyrightIconPath)
        val projectIconBytes = projectIcon.readBytes().toList()
        val secondIconSource = File(context.filesDir, "second-icon.png")
        writeIcon(secondIconSource, android.graphics.Color.CYAN)
        CopyrightIconStore.persist(context, Uri.fromFile(secondIconSource))
        File(context.filesDir, "imported-fonts/${importedFont.id.substringAfter(':')}").delete()

        EditableProjectStore.updateMemo(context, stored.projectId, "자산 누락 후 재저장")
        val reloaded = requireNotNull(EditableProjectStore.load(context, stored.projectId))

        assertEquals(true, projectIcon.isFile)
        assertEquals(projectIconBytes, projectIcon.readBytes().toList())
        assertEquals(importedFont.id, reloaded.watermarkSettings.fontName)
        assertEquals(projectIcon.absolutePath, reloaded.watermarkSettings.customCopyrightIconPath)
        assertNotNull(CaptionTypefaceLoader.load(context, reloaded.watermarkSettings.fontName))
    }

    @Test
    fun corruptPrimaryMetadataFallsBackToPreviousVerifiedBackup() {
        val projectId = "test-${UUID.randomUUID()}"
        val first = sampleProject(projectId, defaultDurationSeconds = 3.0)
        EditableProjectStore.upsert(context, first)
        EditableProjectStore.upsert(context, first.copy(defaultDurationSeconds = 4.0))
        val directory = File(context.filesDir, "editable-projects/$projectId")
        directory.resolve("project.json").writeText("corrupt")

        val recovered = EditableProjectStore.load(context, projectId)

        assertNotNull(recovered)
        assertEquals(3.0, recovered?.defaultDurationSeconds ?: 0.0, 0.0)
        assertEquals(listOf("clip-1"), recovered?.clips?.map(ClipItem::id))
    }

    @Test
    fun representativeIntervalAndCreationTimeSurviveDraftRestart() {
        val createdAt = 1_700_000_000_000L
        val project = sampleProject("restart-project", 3.0).copy(
            similarPhotoRepresentativeInterval = 8,
            createdAtMillis = createdAt
        )
        DraftProjectStore.save(context, project)

        val restored = DraftProjectStore.load(context)

        assertEquals(8, restored?.similarPhotoRepresentativeInterval)
        assertEquals(createdAt, restored?.createdAtMillis)
        assertEquals(project.clips.map(ClipItem::id), restored?.clips?.map(ClipItem::id))
    }

    @Test
    fun importRollbackDeletesOnlyFilesInsideWorkingMediaBoundary() {
        val workingDirectory = File(context.filesDir, "working-media").apply { mkdirs() }
        val staged = workingDirectory.resolve("hanclip-test.jpg").apply { writeText("staged") }
        val outside = File(context.filesDir, "saved-project-source.jpg").apply { writeText("saved") }
        val clip = ClipItem(
            sourceUri = Uri.fromFile(staged),
            thumbnailUri = Uri.fromFile(outside)
        )

        MediaImportReader.discardUncommittedClipFiles(context, clip)

        assertEquals(false, staged.exists())
        assertEquals(true, outside.exists())
    }

    @Test
    fun largeImportRollbackRemovesEveryStagedFileAndKeepsSavedSources() {
        val workingDirectory = File(context.filesDir, "working-media").apply { mkdirs() }
        val savedDirectory = File(context.filesDir, "editable-projects/saved").apply { mkdirs() }
        val savedSources = (0 until 8).map { index ->
            savedDirectory.resolve("saved-$index.jpg").apply { writeText("saved-$index") }
        }
        val stagedClips = (0 until 240).map { index ->
            val staged = workingDirectory.resolve("batch-$index.jpg").apply { writeText("staged-$index") }
            ClipItem(
                id = "batch-$index",
                sourceUri = Uri.fromFile(staged),
                thumbnailUri = Uri.fromFile(savedSources[index % savedSources.size])
            )
        }

        stagedClips.forEach { clip ->
            MediaImportReader.discardUncommittedClipFiles(context, clip)
        }

        assertEquals(emptyList<String>(), workingDirectory.listFiles().orEmpty().map(File::getName))
        assertEquals(8, savedSources.count(File::isFile))
    }

    @Test
    fun fontLimitPreservesAllExistingFilesAndRejectsNextImport() {
        val fontDirectory = File(context.filesDir, "imported-fonts").apply { mkdirs() }
        val originalNames = (0 until ImportedFontStore.MaximumFontCount).map { index ->
            "font-$index--fixed.ttf"
        }
        originalNames.forEach { name -> fontDirectory.resolve(name).writeText("existing-$name") }
        val nextSource = File(context.filesDir, "next-font.ttf").apply { writeText("invalid-new-font") }

        val error = runCatching { ImportedFontStore.import(context, Uri.fromFile(nextSource)) }.exceptionOrNull()

        assertEquals(true, error?.message?.contains("최대 30개") == true)
        assertEquals(originalNames.sorted(), fontDirectory.listFiles().orEmpty().map(File::getName).sorted())
    }

    @Test
    fun invalidFontImportLeavesNoPartialOrStagingFile() {
        val fontDirectory = File(context.filesDir, "imported-fonts").apply { mkdirs() }
        val invalidSource = File(context.filesDir, "invalid-font.ttf").apply { writeText("not-a-font") }

        val error = runCatching { ImportedFontStore.import(context, Uri.fromFile(invalidSource)) }.exceptionOrNull()

        assertNotNull(error)
        assertEquals(emptyList<String>(), fontDirectory.listFiles().orEmpty().map(File::getName))
    }

    @Test
    fun validFontIsVerifiedAndKeepsStableImportedId() {
        val source = File(context.filesDir, "poppins-test.ttf")
        baseContext.assets.open("fonts/poppins_regular.ttf")
            .use { input ->
                FileOutputStream(source).use { output ->
                    input.copyTo(output)
                    output.fd.sync()
                }
            }

        val imported = ImportedFontStore.import(context, Uri.fromFile(source))

        assertEquals(true, ImportedFontStore.isImportedFont(imported.id))
        assertNotNull(ImportedFontStore.typeface(context, imported.id))
        assertEquals(listOf(imported.id), ImportedFontStore.list(context).map(ImportedFontSummary::id))
    }

    @Test
    fun invalidCopyrightIconPreservesPreviousVerifiedImage() {
        val source = File(context.filesDir, "valid-source.png")
        FileOutputStream(source).use { output ->
            val bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
            try {
                bitmap.eraseColor(android.graphics.Color.MAGENTA)
                assertEquals(true, bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
            } finally {
                bitmap.recycle()
            }
            output.fd.sync()
        }
        val storedPath = CopyrightIconStore.persist(context, Uri.fromFile(source))
        val stored = File(storedPath)
        val verifiedBytes = stored.readBytes()
        val invalid = File(context.filesDir, "invalid-icon.bin").apply { writeText("not-an-image") }

        val error = runCatching { CopyrightIconStore.persist(context, Uri.fromFile(invalid)) }.exceptionOrNull()

        assertNotNull(error)
        assertEquals(verifiedBytes.toList(), stored.readBytes().toList())
        assertEquals(
            emptyList<String>(),
            stored.parentFile?.listFiles().orEmpty()
                .filter { it.name.startsWith(".icon-staging-") }
                .map(File::getName)
        )
    }

    @Test
    fun invalidBackgroundMusicLeavesNoPartialWorkingFile() {
        val invalid = File(context.filesDir, "invalid-music.m4a").apply { writeText("not-audio") }

        val error = runCatching { BackgroundMusicStore.persist(context, Uri.fromFile(invalid)) }.exceptionOrNull()
        val musicDirectory = File(context.filesDir, "background-music")

        assertNotNull(error)
        assertEquals(emptyList<String>(), musicDirectory.listFiles().orEmpty().map(File::getName))
    }

    @Test
    fun validBackgroundMusicIsVerifiedAndPersisted() {
        val source = File(context.filesDir, "valid-music.wav")
        baseContext.resources.openRawResource(R.raw.daily_loop).use { input ->
            FileOutputStream(source).use { output ->
                input.copyTo(output)
                output.fd.sync()
            }
        }

        val stored = BackgroundMusicStore.persist(context, Uri.fromFile(source))
        val storedFile = File(stored.path.orEmpty())

        assertEquals(true, storedFile.isFile && storedFile.length() > 0L)
        assertEquals("background-music", storedFile.parentFile?.name)
        assertEquals(emptyList<String>(), storedFile.parentFile?.listFiles().orEmpty()
            .filter { it.name.startsWith(".music-staging-") }
            .map(File::getName))
    }

    @Test
    fun replacingProjectMusicWithSameExtensionStoresTheNewVerifiedAsset() {
        fun copyRawMusic(resourceId: Int, filename: String): File {
            return File(context.filesDir, filename).also { target ->
                baseContext.resources.openRawResource(resourceId).use { input ->
                    FileOutputStream(target).use { output ->
                        input.copyTo(output)
                        output.fd.sync()
                    }
                }
            }
        }
        val firstSource = copyRawMusic(R.raw.daily_loop, "first-project-music.wav")
        val secondSource = copyRawMusic(R.raw.travel_joy, "second-project-music.wav")
        val first = EditableProjectStore.upsert(
            context,
            sampleProject("music-replacement", 2.0).copy(
                backgroundMusicUri = Uri.fromFile(firstSource)
            )
        )
        val firstBytes = File(requireNotNull(first.backgroundMusicUri).path.orEmpty()).readBytes().toList()

        val second = EditableProjectStore.upsert(
            context,
            first.copy(backgroundMusicUri = Uri.fromFile(secondSource))
        )
        val secondBytes = File(requireNotNull(second.backgroundMusicUri).path.orEmpty()).readBytes().toList()

        assertEquals(false, firstBytes == secondBytes)
        assertEquals(secondSource.readBytes().toList(), secondBytes)
    }

    @Test
    fun collectionFallsBackToBackupAndKeepsLastValidIndexOnNextSave() {
        val directory = File(context.filesDir, "movie-collection").apply { mkdirs() }
        directory.resolve("collection-test.mp4").writeText("video")
        val fixture = InstrumentationRegistry.getInstrumentation().context.assets
            .open("fixtures/collection-v1.json")
            .bufferedReader()
            .use { it.readText() }
        directory.resolve("collection.json.bak").writeText(fixture)
        directory.resolve("collection.json").writeText("corrupt")

        val recovered = MovieCollectionStore.list(context)
        MovieCollectionStore.updateTitle(context, "legacy-movie", "새 제목")

        assertEquals(listOf("legacy-movie"), recovered.map(CollectedMovie::id))
        assertEquals("새 제목", MovieCollectionStore.list(context).single().title)
        assertEquals(true, directory.resolve("collection.json.bak").isFile)
        assertEquals("기존 제목", org.json.JSONObject(directory.resolve("collection.json.bak").readText())
            .getJSONArray("movies").getJSONObject(0).getString("title"))
    }

    @Test
    fun interruptedCompressionFixtureDeletesOnlyStagingAndKeepsOriginalAcrossRewrite() {
        val directory = File(context.filesDir, "movie-collection").apply { mkdirs() }
        val original = directory.resolve("interrupted-original.mp4")
            .apply { writeBytes("verified-original-video".toByteArray()) }
        val originalBytes = original.readBytes().toList()
        val staging = directory.resolve(".compression-interrupted-movie-deadbeef.tmp.mp4")
            .apply { writeBytes("partial-compression".toByteArray()) }
        val fixture = InstrumentationRegistry.getInstrumentation().context.assets
            .open("fixtures/collection-v4-interrupted-compression.json")
            .bufferedReader()
            .use { it.readText() }
        directory.resolve("collection.json").writeText(fixture)

        val loaded = MovieCollectionStore.list(context)
        MovieCollectionStore.updateTitle(context, "interrupted-movie", "복구 후 사용자 제목")
        val reloaded = MovieCollectionStore.list(context).single()

        assertEquals("interrupted-original.mp4", loaded.single().videoFilename)
        assertEquals(false, staging.exists())
        assertEquals(true, original.isFile)
        assertEquals(originalBytes, original.readBytes().toList())
        assertEquals("복구 후 사용자 제목", reloaded.title)
        assertEquals(true, reloaded.isPinned)
    }

    @Test
    fun thirtyMovieCollectionKeepsTitlesPinnedOrderAndMaximumAcrossRewrite() {
        val directory = File(context.filesDir, "movie-collection").apply { mkdirs() }
        (1..MovieCollectionStore.MaximumMovieCount).forEach { index ->
            directory.resolve("movie-${index.toString().padStart(2, '0')}.mp4").writeText("video-$index")
        }
        val fixture = InstrumentationRegistry.getInstrumentation().context.assets
            .open("fixtures/collection-v4-30.json")
            .bufferedReader()
            .use { it.readText() }
        directory.resolve("collection.json").writeText(fixture)

        val loaded = MovieCollectionStore.list(context)
        MovieCollectionStore.updateTitle(context, "movie-30", "마지막 사용자 제목")
        val reloaded = MovieCollectionStore.list(context)

        assertEquals(MovieCollectionStore.MaximumMovieCount, loaded.size)
        assertEquals(listOf("movie-05", "movie-04", "movie-03", "movie-02", "movie-01"), loaded.take(5).map(CollectedMovie::id))
        assertEquals("마지막 사용자 제목", reloaded.first { it.id == "movie-30" }.title)
        assertEquals(5, reloaded.count(CollectedMovie::isPinned))
        assertEquals(MovieCollectionStore.MaximumMovieCount, reloaded.size)
    }

    private fun sampleProject(projectId: String, defaultDurationSeconds: Double) = DraftProject(
        projectId = projectId,
        clips = listOf(ClipItem(id = "clip-1", sourceUri = Uri.parse("sample://photo.jpg"))),
        preset = MoviePreset.NewMovie,
        defaultDurationSeconds = defaultDurationSeconds,
        defaultVideoSegmentMode = VideoSegmentMode.Multiple,
        outputAspectRatio = null,
        outputQualityPreset = OutputQualityPreset.Standard,
        watermarkSettings = WatermarkSettings(),
        backgroundMusicUri = null
    )
}
