package com.aengussong.seamcarver.algorithm

import com.aengussong.seamcarver.algorithm.pixelProvider.CachedSlice
import com.aengussong.seamcarver.algorithm.pixelProvider.DefaultPixelProvider
import com.aengussong.seamcarver.algorithm.pixelProvider.PixelProvider
import com.aengussong.seamcarver.model.Picture
import java.util.Arrays
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class SeamCarver(picture: Picture) {
    private var energy: Array<DoubleArray> = arrayOf()
    private lateinit var picture: Picture
    private val width: Int
        get() = picture.width
    private val height: Int
        get() = picture.height

    private external fun findVerticalSeam(energy: Array<DoubleArray>, width: Int, height: Int): IntArray

    init {
        System.loadLibrary("seam_finder")
        initPicture(picture)
    }

    fun getPicture(): Picture {
        return Picture(picture)
    }

    fun energy(x: Int, y: Int): Double {
        checkPixel(x, y)
        return energy[x][y]
    }

    fun findHorizontalSeam(): IntArray {
        val path = Array(width) { IntArray(height) }
        val values = Array(width) { DoubleArray(height) }
        for (i in 0 until width) {
            Arrays.fill(values[i], Double.MAX_VALUE)
        }
        for (i in 0 until height) {
            values[0][i] = energy[0][i]
        }
        for (x in 0 until width - 1) {
            for (y in 1 until height - 1) {
                relax(path, values, x, y, x + 1, y - 1, HORIZONTAL)
                relax(path, values, x, y, x + 1, y, HORIZONTAL)
                relax(path, values, x, y, x + 1, y + 1, HORIZONTAL)
            }
        }
        var shortest = Double.MAX_VALUE
        var shortestIndex = 0
        for (y in 0 until height) {
            if (values[width - 1][y] < shortest) {
                shortest = values[width - 1][y]
                shortestIndex = y
            }
        }
        var nextIndex = shortestIndex
        val horizontalSeam = IntArray(width)
        for (x in width - 1 downTo 0) {
            horizontalSeam[x] = nextIndex
            nextIndex = path[x][nextIndex]
        }
        return horizontalSeam
    }

    fun findVerticalSeam(): IntArray {
        return findVerticalSeam(energy, width, height)
    }

    fun removeHorizontalSeam(seam: IntArray) {
        if (height == 1) return
        val newPicture = Picture(width, height - 1)

        for (x in 0 until width) {
            val pixels = picture.getVerticalRgbLine(x, 0)
            val newPixels = IntArray(height)
            for (y in 0 until height) {
                if (y == seam[x]) continue
                if (y > seam[x]) {
                    newPixels[y - 1] = pixels[y]
                } else {
                    newPixels[y] = pixels[y]
                }
            }
            newPicture.setVerticalRgbLine(newPixels, x, 0)
        }

        picture = newPicture
        recalculateEnergy(seam, HORIZONTAL)
    }

    fun removeVerticalSeam(seam: IntArray) {
        if (width == 1) return
        val newPicture = Picture(width - 1, height)
        for (y in 0 until height) {
            val pixels = picture.getHorizontalRgbLine(0, y)
            val newPixels = IntArray(width)
            for (x in 0 until width) {
                if (x == seam[y]) continue
                if (x > seam[y]) {
                    newPixels[x - 1] = pixels[x]
                } else {
                    newPixels[x] = pixels[x]
                }
            }
            newPicture.setHorizontalRgbLine(newPixels, 0, y)
        }

        picture = newPicture
        recalculateEnergy(seam, VERTICAL)
    }

    private fun initPicture(picture: Picture) {
        this.picture = picture
        computeInitialEnergy()
    }

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
        if (orientation == VERTICAL) {
            path[tx][ty] = x
        } else {
            path[tx][ty] = y
        }
    }

    /**
     * When recalculating energy after seam removal we only need to recalculated energy only for the position of removed
     * seam, and pixel's around it, which might be affected (pixels on top or on left of the removed seam coordinates,
     * based on whether horizontal or vertical seam was removed). This allows us not to spend as much time for the energy
     * calculation as it is needed for initial pass.
     *
     * @param removedSeam - single array of x or y coordinates of the seam, that was just removed, depending on
     * [orientation]. x-s for Horizontal and y-s for Vertical.
     * @param orientation - orientation of the seam that was just removed
     * */
    private fun recalculateEnergy(removedSeam: IntArray, orientation: Int) {
        val pixelProvider = DefaultPixelProvider(picture)
        var traversedSide = 0
        for (i in removedSeam) {
            // coordinates of the removed seam
            val (x, y) = if (orientation == HORIZONTAL) traversedSide to i else i to traversedSide
            // nearest coordinates that also has to be recalculated (top or left row of the removed seam)
            val (nx, ny) = if (orientation == HORIZONTAL) x to (y - 1).coerceAtLeast(0) else (x - 1).coerceAtLeast(0) to y
            energy[x][y] = pixelEnergy(x, y, pixelProvider)
            energy[nx][ny] = pixelEnergy(nx, ny, pixelProvider)
            traversedSide++
        }
    }

    private fun checkPixel(x: Int, y: Int) {
        require(!(x < 0 || x > width - 1 || y < 0 || y > height - 1))
    }

    // Previously was used to check seam, passed into remove seam functions, but disabled it to gain a little bit of
    // performance. Maybe will need it later.
    private fun checkSeam(seam: IntArray?, orientation: Int) {
        requireNotNull(seam)
        val size: Int
        val maxSeamIndex: Int
        if (orientation == VERTICAL) {
            size = height
            maxSeamIndex = width - 1
        } else {
            size = width
            maxSeamIndex = height - 1
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
            require(abs(prevP - p) <= 1)
            prevP = p
        }
    }

    private fun computeInitialEnergy() {
        energy = Array(width) { DoubleArray(height) }

        /**
         * The next lines are for performance, and they lack beauty. We're caching 3 lines and rolling cache as the energy
         * calculation goes. We need 3 lines as the energy is calculated for each pixel based on the pixels around it (on
         * the top, left, right and bottom). So for each pixel in the row we also have to have neighbour rows. The energy
         * calculation goes from left to right, from top to bottom, as this is the most performant way to traverse 2D
         * array. We retrieve from picture whole rows of pixels instead of getting one by one, as this also allows us to
         * improve performance, retrieving pixels in batches and storing them in-memory. We can't store whole bitmap
         * in-memory, as it can be too large, and will cause OOM.
         * */
        val initialLines: IntArray = picture.getHorizontalRgbLine(0, 0, linesCount = 3)
        val cachedSlice = CachedSlice(initialLines, startRow = 0, rowWidth = width)

        for (y in 0 until height) {
            // start rolling cache if we're going to traverse trough the last row of initially cached lines
            if (y >= 2 && y < height - 1) {
                // get next line to cache
                val newline = picture.getHorizontalRgbLine(0, y + 1)
                // roll cache to the new line, deleting the first one in the cache
                cachedSlice.moveSpotlight(newline)
            }
            for (x in 0 until width) {
                energy[x][y] = pixelEnergy(x, y, cachedSlice)
            }
        }
    }

    private fun pixelEnergy(x: Int, y: Int, pixelProvider: PixelProvider): Double {
        if (x == 0 || y == 0 || x == width - 1 || y == height - 1) return 1000.0
        val colorRight = pixelProvider.get(x + 1, y)
        val colorLeft = pixelProvider.get(x - 1, y)
        val colorBottom = pixelProvider.get(x, y + 1)
        val colorTop = pixelProvider.get(x, y - 1)
        val deltaX = calculateDelta(colorRight, colorLeft)
        val deltaY = calculateDelta(colorBottom, colorTop)
        return sqrt(deltaX + deltaY)
    }

    private fun calculateDelta(firstColor: Int, secondColor: Int): Double {
        val rFirst = getRed(firstColor)
        val gFirst = getGreen(firstColor)
        val bFirst = getBlue(firstColor)
        val rSecond = getRed(secondColor)
        val gSecond = getGreen(secondColor)
        val bSecond = getBlue(secondColor)
        return (rFirst - rSecond).toDouble().pow(2.0) +
                (gFirst - gSecond).toDouble().pow(2.0) +
                (bFirst - bSecond).toDouble().pow(2.0)
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
        private const val VERTICAL = 0
        private const val HORIZONTAL = 1
    }
}