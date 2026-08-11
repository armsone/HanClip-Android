package com.hanclip.android.feature.home

internal enum class CollectionShelfCellKind {
    Movie,
    Add,
    Spacer
}

internal fun collectionShelfCellPlan(
    movieCount: Int,
    columnCount: Int,
    maximumMovieCount: Int
): List<CollectionShelfCellKind> {
    val safeMovieCount = movieCount.coerceAtLeast(0)
    val safeColumnCount = columnCount.coerceAtLeast(1)
    val cells = MutableList(safeMovieCount) { CollectionShelfCellKind.Movie }
    if (safeMovieCount < maximumMovieCount.coerceAtLeast(0)) {
        cells += CollectionShelfCellKind.Add
    }
    repeat((safeColumnCount - cells.size % safeColumnCount) % safeColumnCount) {
        cells += CollectionShelfCellKind.Spacer
    }
    return cells
}
