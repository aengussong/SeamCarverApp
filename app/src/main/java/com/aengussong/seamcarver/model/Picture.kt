package com.aengussong.seamcarver.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.annotation.ColorInt
import java.io.File
import java.io.IOException


class Picture {
    private var image: Bitmap

    //todo do I need filename a global variable
    private var filename: String? = null

    //todo do I need this? the variable indicated whether top left is the start point (0,0) of the coordinates
    private var isOriginUpperLeft: Boolean
    private val width: Int
    private val height: Int

    constructor(width: Int, height: Int) {
        isOriginUpperLeft = true
        require(width > 0) { "width must be positive" }
        require(height > 0) { "height must be positive" }
        this.width = width
        this.height = height
        image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    constructor(picture: Picture?) {
        isOriginUpperLeft = true
        requireNotNull(picture) { "constructor argument is null" }
        width = picture.width()
        height = picture.height()
        image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        filename = picture.filename
        isOriginUpperLeft = picture.isOriginUpperLeft
        for (col in 0 until width()) {
            for (row in 0 until height()) {
                image.setPixel(col, row, picture.image.getPixel(col, row))
            }
        }
    }

    constructor(bitmap: Bitmap){
        isOriginUpperLeft = true
        width = bitmap.width
        height = bitmap.height
        image = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    }

    constructor(name: String) {
        isOriginUpperLeft = true
        filename = name
        try {
            image = BitmapFactory.decodeFile(filename)
            width = image.width
            height = image.height
        } catch (ioe: IOException) {
            throw IllegalArgumentException("could not open image: $name", ioe)
        }
    }

    constructor(file: File) {
        isOriginUpperLeft = true
        try {
            image = BitmapFactory.decodeFile(file.path)
            width = image.width
            height = image.height
        } catch (ioe: IOException) {
            throw IllegalArgumentException("could not open file: $file", ioe)
        }
    }

    fun setOriginUpperLeft() {
        isOriginUpperLeft = true
    }

    fun setOriginLowerLeft() {
        isOriginUpperLeft = false
    }

    fun height(): Int {
        return height
    }

    fun width(): Int {
        return width
    }

    private fun validateRowIndex(row: Int) {
        require(!(row < 0 || row >= height())) { "row index must be between 0 and " + (height() - 1) + ": " + row }
    }

    private fun validateColumnIndex(col: Int) {
        require(!(col < 0 || col >= width())) { "column index must be between 0 and " + (width() - 1) + ": " + col }
    }

    operator fun get(col: Int, row: Int): Color {
        validateColumnIndex(col)
        validateRowIndex(row)
        val rgb = getRGB(col, row)
        return Color.valueOf(rgb)
    }

    @ColorInt
    fun getRGB(col: Int, row: Int): Int {
        validateColumnIndex(col)
        validateRowIndex(row)
        return if (isOriginUpperLeft) image.getPixel(col, row) else image.getPixel(col, height - row - 1)
    }

    operator fun set(col: Int, row: Int, color: Color) {
        validateColumnIndex(col)
        validateRowIndex(row)
        val rgb: Int = color.toArgb()
        setRGB(col, row, rgb)
    }

    fun setRGB(col: Int, row: Int, rgb: Int) {
        validateColumnIndex(col)
        validateRowIndex(row)
        if (!isOriginUpperLeft) {
            image.setPixel(col, height - row - 1, rgb)
        } else {
            image.setPixel(col, row, rgb)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other == null || other.javaClass != javaClass) {
            return false
        }
        val that = other as Picture
        if (width() != that.width() || height() != that.height()) {
            return false
        }
        for (col in 0 until width()) {
            for (row in 0 until height()) {
                if (getRGB(col, row) != that.getRGB(col, row)) {
                    return false
                }
            }
        }
        return true
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(
            """${width}-by-${height} picture (RGB values given in hex)"""
        )
        for (row in 0 until height) {
            for (col in 0 until width) {
                val rgb: Int =
                    if (isOriginUpperLeft) image.getPixel(col, row) else image.getPixel(col, height - row - 1)
                sb.append(String.format("#%06X ", Integer.valueOf(rgb and 16777215)))
            }
            sb.append("\n")
        }
        return sb.toString().trim { it <= ' ' }
    }

    override fun hashCode(): Int {
        throw UnsupportedOperationException("hashCode() is not supported because pictures are mutable")
    }

    fun getImage(): Bitmap = image
}