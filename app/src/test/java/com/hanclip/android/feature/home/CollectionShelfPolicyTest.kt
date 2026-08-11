package com.hanclip.android.feature.home

import org.junit.Assert.assertEquals
import org.junit.Test

class CollectionShelfPolicyTest {
    @Test
    fun `zero and one movie keep one add card`() {
        assertEquals(
            listOf(CollectionShelfCellKind.Add, CollectionShelfCellKind.Spacer),
            collectionShelfCellPlan(movieCount = 0, columnCount = 2, maximumMovieCount = 30)
        )
        assertEquals(
            listOf(CollectionShelfCellKind.Movie, CollectionShelfCellKind.Add),
            collectionShelfCellPlan(movieCount = 1, columnCount = 2, maximumMovieCount = 30)
        )
    }

    @Test
    fun `twenty nine shows add and thirty hides it`() {
        val twentyNine = collectionShelfCellPlan(29, 3, 30)
        val thirty = collectionShelfCellPlan(30, 3, 30)

        assertEquals(29, twentyNine.count { it == CollectionShelfCellKind.Movie })
        assertEquals(1, twentyNine.count { it == CollectionShelfCellKind.Add })
        assertEquals(0, twentyNine.count { it == CollectionShelfCellKind.Spacer })
        assertEquals(30, thirty.count { it == CollectionShelfCellKind.Movie })
        assertEquals(0, thirty.count { it == CollectionShelfCellKind.Add })
        assertEquals(0, thirty.count { it == CollectionShelfCellKind.Spacer })
    }

    @Test
    fun `plan pads incomplete rows without dropping existing movies`() {
        val plan = collectionShelfCellPlan(30, 4, 30)

        assertEquals(30, plan.count { it == CollectionShelfCellKind.Movie })
        assertEquals(2, plan.count { it == CollectionShelfCellKind.Spacer })
    }
}
