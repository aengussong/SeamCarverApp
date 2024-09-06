package com.aengussong.seamcarver.algorithm

import androidx.compose.runtime.currentRecomposeScope
import com.aengussong.seamcarver.model.Picture
import java.util.Arrays

// todo improve array traversal - in 2D array first go trough each rows (for y in height { for x in width })

const val MOVE_BITMAP_COLORS_IN_MEMORY = true

class SeamCarver(picture: Picture) {
    private var energy: Array<DoubleArray> = arrayOf()
    private var picture: Picture? = null

    private lateinit var precalculatedImage: Array<Array<Int>>
    private var m: Int = 0
    private var n: Int = 0

    init {
        if (MOVE_BITMAP_COLORS_IN_MEMORY) {
            this.picture = picture
            this.m = this.picture!!.height()
            this.n = this.picture!!.width()
            moveColorsToMemory(picture)
            this.picture = null
            computeEnergy()
        }
        //initPicture(picture)
    }

    fun picture(): Picture {
        return Picture(picture)
    }
/*
    fun getColumns(): Int {
        return picture.width()
    }

    fun getRows(): Int {
        return picture.height()
    }*/

   /* fun energy(x: Int, y: Int): Double {
        checkPixel(x, y)
        return energy[x][y]
    }*/

    fun findHorizontalSeam(): IntArray {
        //val path = Array(m) { IntArray(n) }
        val values = Array(m) { DoubleArray(n) }
        for (i in 0 until m) {
            Arrays.fill(values[i], Double.MAX_VALUE)
        }
        for (i in 0 until m) {
            values[i][0] = energy[i][0]
        }

        for (j in 1 until n) {
            for (i in 1 until m - 1) {
                values[i][j] = energy[i][j] + minOf(values[i - 1][j - 1], values[i][j - 1], values[i + 1][j - 1])
            }
        }
/*
        for (x in 0 until getColumns() - 1) {
            for (y in 1 until getRows() - 1) {
                relax(path, values, x, y, x + 1, y - 1, HORIZONTAL)
                relax(path, values, x, y, x + 1, y, HORIZONTAL)
                relax(path, values, x, y, x + 1, y + 1, HORIZONTAL)
            }
        }
        */

        var shortest = Double.MAX_VALUE
        var shorthestIndex: Int = 0

        for (i in 0 until m) {
            if (values[i][n - 1] < shortest) {
                shortest = values[i][n - 1]
                shorthestIndex = i
            }
        }

        var currentIndex = shorthestIndex
        val horizontalSeam = IntArray(n)

        for (j in n - 1 downTo 1) {
            horizontalSeam[j] = currentIndex
            val previousMin = minOf(values[currentIndex - 1][j - 1], values[currentIndex][j - 1], values[currentIndex + 1][j - 1])
            if (previousMin == values[currentIndex - 1][j - 1]) {
                currentIndex -= 1
            } else if (previousMin == values[currentIndex + 1][j - 1]) {
                currentIndex += 1
            }
        }

        horizontalSeam[0] = currentIndex
/*

        for (x in getColumns() - 1 downTo 0) {
            horizontalSeam[x] = nextIndex
            nextIndex = path[x][nextIndex]
        }*/
        return horizontalSeam
    }
/*
    fun findVerticalSeam(): IntArray {
        val path = Array(getColumns()) { IntArray(getRows()) }
        val values = Array(getColumns()) { DoubleArray(getRows()) }
        for (i in 0 until getColumns()) {
            Arrays.fill(values[i], Double.MAX_VALUE)
        }
        for (i in 0 until getColumns()) {
            values[i][0] = energy[i][0]
        }
        for (y in 0 until getRows() - 1) {
            for (x in 1 until getColumns() - 1) {
                relax(path, values, x, y, x - 1, y + 1, VERTICAl)
                relax(path, values, x, y, x, y + 1, VERTICAl)
                relax(path, values, x, y, x + 1, y + 1, VERTICAl)
            }
        }
        var shorthest = Double.MAX_VALUE
        var shorthestIndex = 0
        for (x in 0 until getColumns()) {
            if (values[x][getRows() - 1] < shorthest) {
                shorthest = values[x][getRows() - 1]
                shorthestIndex = x
            }
        }
        var nextIndex = shorthestIndex
        val verticalSeam = IntArray(getRows())
        for (y in getRows() - 1 downTo 0) {
            verticalSeam[y] = nextIndex
            nextIndex = path[nextIndex][y]
        }
        return verticalSeam
    }
*/
    private fun relax(
        path: Array<IntArray>,
        values: Array<DoubleArray>,
        x: Int,
        y: Int,
        tx: Int,
        ty: Int,
        orientation: Int
    ) {
        if (values[tx][ty] < values[x][y] + energy[tx][ty]) return
        values[tx][ty] = values[x][y] + energy[tx][ty]
        if (orientation == VERTICAl) {
            path[tx][ty] = x
        } else {
            path[tx][ty] = y
        }
    }

    fun generateNewPicture() {
        val newPicture = Picture(n, m)

        for (i in 0 until m) {
            for (j in 0 until n) {
                newPicture.setRGB(j, i, getRGB(i, j))
            }
        }

        this.picture = newPicture
    }

    fun removeHorizontalSeam(seam: IntArray) {
        //checkSeam(seam, HORIZONTAL)
        //val newPicture = Picture(n, m - 1)
        for (i in 0 until m) {
            for (j in 0 until n) {
                if (i == seam[j]) continue
                if (i > seam[j]) {
                    precalculatedImage[i][j] = precalculatedImage[i-1][j]
                    //newPicture.setRGB(j, i - 1, getRGB(i, j))
                } /*else {
                    //newPicture.setRGB(j, i, getRGB(i, j))
                }*/
            }
        }
        m -= 1

        //initPicture(newPicture)
    }
/*
    fun removeVerticalSeam(seam: IntArray) {
        checkSeam(seam, VERTICAl)
        val newPicture = Picture(getColumns() - 1, getRows())
        for (y in 0 until getRows()) {
            for (x in 0 until getColumns()) {
                if (x == seam[y]) continue
                if (x > seam[y]) {
                    newPicture.setRGB(x - 1, y, getRGB(x, y))
                } else {
                    newPicture.setRGB(x, y, getRGB(x, y))
                }
            }
        }
        initPicture(newPicture)
    }
*/
    private fun moveColorsToMemory(picture: Picture) {
        precalculatedImage = Array(m) {Array(n) {0}}

        for (i in 0 until m) {
            for (j in 0 until n) {
                precalculatedImage[i][j] = picture.getRGB(j, i)
            }
        }
    }

    // todo move precalculation logic inside the Picture class
    private fun getRGB(i: Int, j: Int): Int {
        return if (MOVE_BITMAP_COLORS_IN_MEMORY) {
            precalculatedImage[i][j]
        } else {
            picture!!.getRGB(j, i)
        }
    }

    private fun initPicture(picture: Picture) {
        this.picture = picture
        computeEnergy()
    }
/*
    private fun checkPixel(x: Int, y: Int) {
        require(!(x < 0 || x > getColumns() - 1 || y < 0 || y > getRows() - 1))
    }*/

    /*private fun checkSeam(seam: IntArray?, orientation: Int) {
        requireNotNull(seam)
        val size: Int
        val maxSeamIndex: Int
        if (orientation == VERTICAl) {
            size = getRows()
            maxSeamIndex = getColumns() - 1
        } else {
            size = getColumns()
            maxSeamIndex = getRows() - 1
        }
        require(maxSeamIndex > 0)
        require(seam.size == size)
        var prevP = -1
        for (p in seam) {
            require(!(p < 0 || p > maxSeamIndex))
            if (prevP == -1) {
                prevP = p
                continue
            }
            require(Math.abs(prevP - p) <= 1)
            prevP = p
        }
    }
*/
    private fun computeEnergy() {
        energy = Array(m) { DoubleArray(n) }
        for (i in 0 until m) {
            for (j in 0 until n) {
                energy[i][j] = pixelEnergy(i, j)
            }
        }
    }

    private fun pixelEnergy(i: Int, j: Int): Double {
        if (i == 0 || j == 0 || i == m - 1 || j == n - 1) return 1000.0
        val colorRight: Int = getRGB(i, j + 1)
        val colorLeft: Int = getRGB(i, j - 1)
        val colorBottom: Int = getRGB(i + 1, j)
        val colorTop: Int = getRGB(i - 1, j)
        val deltaX = calculateDelta(colorRight, colorLeft)
        val deltaY = calculateDelta(colorBottom, colorTop)
        return Math.sqrt(deltaX + deltaY)
    }

    private fun calculateDelta(firstColor: Int, secondColor: Int): Double {
        val rFirst = getRed(firstColor)
        val gFirst = getGreen(firstColor)
        val bFirst = getBlue(firstColor)
        val rSecond = getRed(secondColor)
        val gSecond = getGreen(secondColor)
        val bSecond = getBlue(secondColor)
        return Math.pow((rFirst - rSecond).toDouble(), 2.0) + Math.pow(
            (gFirst - gSecond).toDouble(),
            2.0
        ) + Math.pow((bFirst - bSecond).toDouble(), 2.0)
    }

    private fun getRed(color: Int): Int {
        return color shr 16 and 0xFF
    }

    private fun getGreen(color: Int): Int {
        return color shr 8 and 0xFF
    }

    private fun getBlue(color: Int): Int {
        return color shr 0 and 0xFF
    }

    companion object {
        private const val VERTICAl = 0
        private const val HORIZONTAL = 1
    }
}