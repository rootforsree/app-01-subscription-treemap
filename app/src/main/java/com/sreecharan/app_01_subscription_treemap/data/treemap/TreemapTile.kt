package com.sreecharan.app_01_subscription_treemap.data.treemap

import androidx.compose.runtime.Immutable

private const val MIN_POSITIVE_WEIGHT = 0f
private const val ORIGIN = 0f
private const val FULL_EXTENT = 1f

/**
 * One rectangle in the treemap. Coordinates are fractions of the container
 * ([x], [y] in 0..1), [width] and [height] are fractions too.
 */
@Immutable
data class TreemapTile(
    val subscriptionId: Long,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
)

/**
 * Squarified treemap layout (Bruls, Huizing, van Wijk).
 *
 * Each weight becomes a rectangle whose area is proportional to the weight.
 * Rows are built along the shorter side of the remaining free rectangle so
 * that every rectangle stays as square as possible.
 *
 * Rules:
 * - empty weights map -> empty list
 * - weights <= 0 are dropped; when nothing remains -> empty list
 * - a single entry fills the whole container
 * - deterministic: same input, same output
 *
 * [weights] maps subscription id to its weight. [width] and [height] are the
 * container size; the returned tiles hold fractional coordinates.
 */
fun squarifiedTreemap(
    weights: Map<Long, Float>,
    width: Float,
    height: Float,
): List<TreemapTile> {
    val entries = weights.entries
        .filter { it.value > MIN_POSITIVE_WEIGHT }
        .sortedByDescending { it.value }
    return when {
        entries.isEmpty() || width <= ORIGIN || height <= ORIGIN -> emptyList()
        entries.size == 1 -> {
            val single = entries.first()
            listOf(
                TreemapTile(
                    subscriptionId = single.key,
                    x = ORIGIN,
                    y = ORIGIN,
                    width = FULL_EXTENT,
                    height = FULL_EXTENT,
                ),
            )
        }
        else -> layoutRows(entries)
    }
}

private data class Scaled(val id: Long, val area: Float)

private data class Rect(val x: Float, val y: Float, val width: Float, val height: Float) {
    fun shorterSide(): Float = minOf(width, height)
}

private fun layoutRows(entries: List<Map.Entry<Long, Float>>): List<TreemapTile> {
    val total = entries.sumOf { it.value.toDouble() }.toFloat()
    // Scale weights so that their sum equals the container's fractional area (1).
    val scaled = entries.map { (id, weight) -> Scaled(id, weight / total) }

    val tiles = mutableListOf<TreemapTile>()
    val row = mutableListOf<Scaled>()
    var remaining = Rect(ORIGIN, ORIGIN, FULL_EXTENT, FULL_EXTENT)

    for (item in scaled) {
        row.add(item)
        val worstNow = worstAspectRatio(row, remaining.shorterSide())
        val rowWithoutLast = row.dropLast(1)
        if (rowWithoutLast.isNotEmpty() && worstAspectRatio(rowWithoutLast, remaining.shorterSide()) < worstNow) {
            row.removeAt(row.lastIndex)
            remaining = layoutRow(row, remaining, tiles)
            row.add(item)
        }
    }
    if (row.isNotEmpty()) {
        layoutRow(row, remaining, tiles)
    }
    return tiles
}

/**
 * Worst aspect ratio the row would produce if laid along a
 * strip of length [stripWidth] (the shorter side of the remaining rectangle),
 * with thickness area-sum / [stripWidth] perpendicular to it.
 */
private fun worstAspectRatio(row: List<Scaled>, stripWidth: Float): Float {
    val areaSum = row.sumOf { it.area.toDouble() }.toFloat()
    if (areaSum <= ORIGIN || stripWidth <= ORIGIN) return Float.MAX_VALUE
    val max = row.maxOf { it.area }
    val min = row.minOf { it.area }
    val widthSquared = stripWidth * stripWidth
    val areaSquared = areaSum * areaSum
    return maxOf(
        widthSquared * max / areaSquared,
        areaSquared / (widthSquared * min),
    )
}

/**
 * Lays [row] out as a strip along the shorter side of [rect], emitting tiles
 * into [out]. Returns the leftover free rectangle.
 */
private fun layoutRow(
    row: List<Scaled>,
    rect: Rect,
    out: MutableList<TreemapTile>,
): Rect {
    val areaSum = row.sumOf { it.area.toDouble() }.toFloat()
    val stripWidth = rect.shorterSide()
    val stripThickness = areaSum / stripWidth
    var offset = ORIGIN
    if (rect.width <= rect.height) {
        // Shorter side is the width: horizontal strip, segments laid along x.
        for (scaled in row) {
            val segmentLength = scaled.area / stripThickness
            out.add(
                TreemapTile(
                    subscriptionId = scaled.id,
                    x = rect.x + offset,
                    y = rect.y,
                    width = segmentLength,
                    height = stripThickness,
                ),
            )
            offset += segmentLength
        }
        return Rect(rect.x, rect.y + stripThickness, rect.width, rect.height - stripThickness)
    }
    // Shorter side is the height: vertical strip, segments stacked along y.
    for (scaled in row) {
        val segmentLength = scaled.area / stripThickness
        out.add(
            TreemapTile(
                subscriptionId = scaled.id,
                x = rect.x,
                y = rect.y + offset,
                width = stripThickness,
                height = segmentLength,
            ),
        )
        offset += segmentLength
    }
    return Rect(rect.x + stripThickness, rect.y, rect.width - stripThickness, rect.height)
}
