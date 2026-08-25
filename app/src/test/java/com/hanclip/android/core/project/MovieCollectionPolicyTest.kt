package com.hanclip.android.core.project

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieCollectionPolicyTest {

    private fun sampleMovie(
        id: String,
        kind: MovieLibraryKind = MovieLibraryKind.Released,
        videoFilename: String = "$id.mp4",
        posterFilename: String = "$id.jpg",
        contentSha256: String? = "hash-$id",
        isPinned: Boolean = false,
        pinnedAtMillis: Long? = null,
        title: String = "샘플 영화 $id",
        durationSeconds: Double = 12.0
    ) = CollectedMovie(
        id = id,
        title = title,
        videoFilename = videoFilename,
        posterFilename = posterFilename,
        createdAtMillis = 100_000L,
        durationSeconds = durationSeconds,
        madeAtMillis = 90_000L,
        shootingStartAtMillis = 80_000L,
        shootingEndAtMillis = 85_000L,
        locationName = "서울",
        contentSha256 = contentSha256,
        isPinned = isPinned,
        pinnedAtMillis = pinnedAtMillis,
        posterSelectionVersion = 2,
        kind = kind
    )

    @Test
    fun `add released movie creates distinct collection entry while preserving files and metadata`() {
        val released = sampleMovie(id = "released-1", kind = MovieLibraryKind.Released)
        val plan = planAddReleasedMovieToCollection(
            existingMovies = listOf(released),
            releasedMovieId = "released-1",
            maximumCount = 30,
            newIdGenerator = { "collection-1" },
            currentTimeMillis = 200_000L
        )

        assertTrue(plan is ReleasedToCollectionPlanResult.Success)
        val success = plan as ReleasedToCollectionPlanResult.Success
        assertEquals(2, success.updatedMovies.size)

        val added = success.addedMovie
        assertEquals("collection-1", added.id)
        assertEquals(MovieLibraryKind.Collection, added.kind)
        assertEquals(released.videoFilename, added.videoFilename)
        assertEquals(released.posterFilename, added.posterFilename)
        assertEquals(released.title, added.title)
        assertEquals(released.durationSeconds, added.durationSeconds, 0.0)
        assertEquals(released.madeAtMillis, added.madeAtMillis)
        assertEquals(released.shootingStartAtMillis, added.shootingStartAtMillis)
        assertEquals(released.shootingEndAtMillis, added.shootingEndAtMillis)
        assertEquals(released.locationName, added.locationName)
        assertEquals(released.contentSha256, added.contentSha256)
        assertEquals(200_000L, added.createdAtMillis)
        assertFalse(added.isPinned)
        assertEquals(null, added.pinnedAtMillis)

        // Original released movie remains untouched in the list
        val original = success.updatedMovies.first { it.id == "released-1" }
        assertEquals(MovieLibraryKind.Released, original.kind)
        assertEquals(100_000L, original.createdAtMillis)
    }

    @Test
    fun `repeated add of released movie is idempotent by video filename or hash`() {
        val released = sampleMovie(id = "released-1", kind = MovieLibraryKind.Released, videoFilename = "common.mp4", contentSha256 = "hash-1")
        val existingCollection = sampleMovie(id = "collection-1", kind = MovieLibraryKind.Collection, videoFilename = "common.mp4", contentSha256 = "hash-1")

        val planByFilename = planAddReleasedMovieToCollection(
            existingMovies = listOf(released, existingCollection),
            releasedMovieId = "released-1"
        )
        assertTrue(planByFilename is ReleasedToCollectionPlanResult.Duplicate)
        assertEquals("collection-1", (planByFilename as ReleasedToCollectionPlanResult.Duplicate).existingMovie.id)

        // Duplicate matched via SHA-256 even if filename differed
        val collectionWithDifferentFilename = sampleMovie(id = "collection-2", kind = MovieLibraryKind.Collection, videoFilename = "other.mp4", contentSha256 = "hash-1")
        val planByHash = planAddReleasedMovieToCollection(
            existingMovies = listOf(released, collectionWithDifferentFilename),
            releasedMovieId = "released-1"
        )
        assertTrue(planByHash is ReleasedToCollectionPlanResult.Duplicate)
        assertEquals("collection-2", (planByHash as ReleasedToCollectionPlanResult.Duplicate).existingMovie.id)
    }

    @Test
    fun `adding when collection is full at maximum 30 returns limit reached`() {
        val released = sampleMovie(id = "released-new", kind = MovieLibraryKind.Released)
        val existingThirty = (1..30).map { index ->
            sampleMovie(
                id = "collection-$index",
                kind = MovieLibraryKind.Collection,
                videoFilename = "video-$index.mp4",
                posterFilename = "poster-$index.jpg",
                contentSha256 = "hash-$index"
            )
        }
        val plan = planAddReleasedMovieToCollection(
            existingMovies = listOf(released) + existingThirty,
            releasedMovieId = "released-new",
            maximumCount = 30
        )

        assertTrue(plan is ReleasedToCollectionPlanResult.LimitReached)
        assertEquals(
            "컬렉션에는 영화를 최대 30개까지 보관할 수 있습니다.",
            (plan as ReleasedToCollectionPlanResult.LimitReached).message
        )
    }

    @Test
    fun `nonexistent released movie returns not found`() {
        val plan = planAddReleasedMovieToCollection(
            existingMovies = emptyList(),
            releasedMovieId = "nonexistent"
        )
        assertTrue(plan is ReleasedToCollectionPlanResult.NotFound)
    }

    @Test
    fun `collection movie cannot be added through released movie action`() {
        val collection = sampleMovie(
            id = "collection-1",
            kind = MovieLibraryKind.Collection
        )
        val plan = planAddReleasedMovieToCollection(
            existingMovies = listOf(collection),
            releasedMovieId = collection.id
        )

        assertTrue(plan is ReleasedToCollectionPlanResult.NotFound)
    }

    @Test
    fun `shared file deletion safety prevents deleting video and poster when other entry references it`() {
        val released = sampleMovie(id = "released-1", kind = MovieLibraryKind.Released, videoFilename = "shared.mp4", posterFilename = "shared.jpg")
        val collection = sampleMovie(id = "collection-1", kind = MovieLibraryKind.Collection, videoFilename = "shared.mp4", posterFilename = "shared.jpg")

        // When released movie is removed, collection movie remains
        val remainingAfterReleasedRemoval = listOf(collection)
        assertFalse(shouldDeleteVideoFile(remainingAfterReleasedRemoval, "shared.mp4"))
        assertFalse(shouldDeletePosterFile(remainingAfterReleasedRemoval, "shared.jpg"))

        // When collection movie is removed, released movie remains
        val remainingAfterCollectionRemoval = listOf(released)
        assertFalse(shouldDeleteVideoFile(remainingAfterCollectionRemoval, "shared.mp4"))
        assertFalse(shouldDeletePosterFile(remainingAfterCollectionRemoval, "shared.jpg"))

        // When both entries are removed (no more references in store)
        val remainingEmpty = emptyList<CollectedMovie>()
        assertTrue(shouldDeleteVideoFile(remainingEmpty, "shared.mp4"))
        assertTrue(shouldDeletePosterFile(remainingEmpty, "shared.jpg"))
    }

    @Test
    fun `compression safety preserves source video when released movie still references it`() {
        val released = sampleMovie(id = "released-1", kind = MovieLibraryKind.Released, videoFilename = "source.mp4")
        val collection = sampleMovie(id = "collection-1", kind = MovieLibraryKind.Collection, videoFilename = "source.mp4")

        // Collection movie compressed to new filename, updated store has both
        val updatedMovies = listOf(
            released,
            collection.copy(videoFilename = "collection-1-compressed.mp4")
        )

        // Compressing collection-1 must NOT delete source.mp4 because released-1 still references it
        assertFalse(
            shouldDeleteSourceVideoOnCompression(
                updatedMovies = updatedMovies,
                compressedMovieId = "collection-1",
                sourceFilename = "source.mp4"
            )
        )

        // If only collection-1 existed and was compressed, source.mp4 can be safely deleted
        val standaloneCollectionUpdated = listOf(
            collection.copy(videoFilename = "collection-1-compressed.mp4")
        )
        assertTrue(
            shouldDeleteSourceVideoOnCompression(
                updatedMovies = standaloneCollectionUpdated,
                compressedMovieId = "collection-1",
                sourceFilename = "source.mp4"
            )
        )
    }
}
