package com.aengussong.seamcarver.algorithm.pixelProvider

/**
 * Cached pixels provider, caches part of the image. It stores pixels in 1D array, which is a trick to higher performance.
 * In order to work with it we need to know how many pixels is in the row and the index of first stored row. Based on this
 * data we can cache part of the bitmap in-memory, which increases performance.
 * */
class CachedSlice(private var initialSlice: IntArray, private var startRow: Int, private val rowWidth: Int) :
    PixelProvider {

    override fun get(x: Int, y: Int): Int {
        if (y < startRow || y >= startRow + initialSlice.size / rowWidth || x > rowWidth - 1 || x < 0) throw IndexOutOfBoundsException(
            "x:$x, y:$y, width: $rowWidth"
        )

        val newIndex = (y - startRow) * rowWidth + x
        return initialSlice[newIndex]
    }

    /**
     * Removes first cached row of pixels and appends new one to the end of the cache. This action will increment
     * [startRow] value as the cache moved one row further.
     * */
    fun moveSpotlight(newLine: IntArray) {
        // remove first cached row and append new row
        val removedFirstLine = initialSlice.sliceArray(rowWidth..initialSlice.lastIndex)
        initialSlice = removedFirstLine + newLine
        startRow++
    }
}