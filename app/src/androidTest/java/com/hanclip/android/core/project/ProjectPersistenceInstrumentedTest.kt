package com.hanclip.android.core.project

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hanclip.android.core.model.ClipItem
import com.hanclip.android.core.model.MoviePreset
import com.hanclip.android.core.model.OutputQualityPreset
import com.hanclip.android.core.model.VideoSegmentMode
import com.hanclip.android.core.model.WatermarkSettings
import com.hanclip.android.core.media.MediaImportReader
import org.junit.After
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
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
