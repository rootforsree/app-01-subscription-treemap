package com.sreecharan.app_01_subscription_treemap

import com.sreecharan.app_01_subscription_treemap.data.treemap.TreemapTile
import com.sreecharan.app_01_subscription_treemap.data.treemap.squarifiedTreemap
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val EPSILON = 1e-4f

class TreemapLayoutTest {

    private val containerWidth = 4f
    private val containerHeight = 3f
    private val sampleWeights = mapOf(1L to 10f, 2L to 5f, 3L to 3f, 4L to 2f, 5L to 1f)
    private val singleWeight = mapOf(1L to 10f)
    private val filteredWeights = mapOf(1L to 10f, 2L to 0f, 3L to -5f)
    private val sixWeights = mapOf(1L to 10f, 2L to 5f, 3L to 3f, 4L to 2f, 5L to 1f, 6L to 0.5f)
    private val areaTolerance = 0.01
    private val maxAspectRatio = 4f
    private val unitArea = 1.0
    private val unitSpan = 1f
    private val expectedSingleTile = listOf(TreemapTile(1L, 0f, 0f, 1f, 1f))

    @Test
    fun `empty weights produce no tiles`() {
        assertTrue(squarifiedTreemap(emptyMap(), containerWidth, containerHeight).isEmpty())
    }

    @Test
    fun `single entry fills the whole container`() {
        val tiles = squarifiedTreemap(singleWeight, containerWidth, containerHeight)
        assertEquals(expectedSingleTile, tiles)
    }

    @Test
    fun `tile areas sum to the container area`() {
        val tiles = squarifiedTreemap(sampleWeights, containerWidth, containerHeight)
        val total = tiles.sumOf { (it.width * it.height).toDouble() }
        assertTrue(abs(total - unitArea) < areaTolerance, "area sum was $total")
    }

    @Test
    fun `all tiles stay within unit bounds`() {
        val tiles = squarifiedTreemap(sampleWeights, containerWidth, containerHeight)
        for (tile in tiles) {
            assertTrue(tile.x >= -EPSILON && tile.y >= -EPSILON, "tile $tile starts out of bounds")
            assertTrue(tile.x + tile.width <= unitSpan + EPSILON, "tile $tile overflows right")
            assertTrue(tile.y + tile.height <= unitSpan + EPSILON, "tile $tile overflows bottom")
        }
    }

    @Test
    fun `no two tiles overlap`() {
        val tiles = squarifiedTreemap(sampleWeights, containerWidth, containerHeight)
        for (i in tiles.indices) {
            for (j in i + 1 until tiles.size) {
                assertTrue(!overlaps(tiles[i], tiles[j]), "tiles $i and $j overlap")
            }
        }
    }

    @Test
    fun `zero and negative weights are filtered`() {
        val tiles = squarifiedTreemap(filteredWeights, containerWidth, containerHeight)
        assertEquals(expectedSingleTile, tiles)
    }

    @Test
    fun `five item layout keeps every aspect ratio under 4`() {
        val tiles = squarifiedTreemap(sampleWeights, containerWidth, containerHeight)
        assertEquals(sampleWeights.size, tiles.size)
        for (tile in tiles) {
            val ratio = maxOf(tile.width, tile.height) / minOf(tile.width, tile.height)
            assertTrue(ratio < maxAspectRatio, "tile $tile has aspect ratio $ratio")
        }
    }

    @Test
    fun `layout is deterministic`() {
        val first = squarifiedTreemap(sixWeights, containerWidth, containerHeight)
        val second = squarifiedTreemap(sixWeights, containerWidth, containerHeight)
        assertEquals(first, second)
    }

    private fun overlaps(a: TreemapTile, b: TreemapTile): Boolean = a.x < b.x + b.width - EPSILON &&
        b.x < a.x + a.width - EPSILON &&
        a.y < b.y + b.height - EPSILON &&
        b.y < a.y + a.height - EPSILON
}
