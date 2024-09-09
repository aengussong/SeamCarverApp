package com.aengussong.seamcarver.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.ColorInt
import java.io.File
import java.io.IOException


class Picture {
    val image: Bitmap
    val width: Int
    val height: Int

    constructor(width: Int, height: Int) {
        require(width > 0) { "width must be positive" }
        require(height > 0) { "height must be positive" }
        this.width = width
        this.height = height
        image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    constructor(picture: Picture) {
        width = picture.width
        height = picture.height
        image = picture.image
    }

    constructor(bitmap: Bitmap) {
        width = bitmap.width
        height = bitmap.height
        image = bitmap
    }

    constructor(name: String) {
        try {
            image = BitmapFactory.decodeFile(name)
            width = image.width
            height = image.height
        } catch (ioe: IOException) {
            throw IllegalArgumentException("could not open image: $name", ioe)
        }
    }

    constructor(file: File) {
        try {
            image = BitmapFactory.decodeFile(file.path)
            width = image.width
            height = image.height
        } catch (ioe: IOException) {
            throw IllegalArgumentException("could not open file: $file", ioe)
        }
    }

    fun getHorizontalRgbLine(startX: Int, startY: Int, linesCount: Int = 1): IntArray {
        val pixels = IntArray(width * linesCount)
        image.getPixels(pixels, 0, width, startX, startY, width, linesCount)
        return pixels
    }

    fun getVerticalRgbLine(startX: Int, startY: Int): IntArray {
        val pixels = IntArray(height)
        image.getPixels(pixels, 0, 1, startX, startY, 1, height)
        return pixels
    }

    fun setHorizontalRgbLine(rgbToSet: IntArray, startX: Int, startY: Int) {
        image.setPixels(rgbToSet, 0, width, startX, startY, width, 1)
    }

    fun setVerticalRgbLine(rgbToSet: IntArray, startX: Int, startY: Int) {
        image.setPixels(rgbToSet, 0, 1, startX, startY, 1, height)
    }

    @ColorInt
    fun getRGB(col: Int, row: Int): Int {
        return image.getPixel(col, row)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(
            """${width}-by-${height} picture (RGB values given in hex)"""
        )
        for (row in 0 until height) {
            for (col in 0 until width) {
                val rgb: Int = image.getPixel(col, row)
                sb.append(String.format("#%06X ", Integer.valueOf(rgb and 16777215)))
            }
            sb.append("\n")
        }
        return sb.toString().trim { it <= ' ' }
    }
}