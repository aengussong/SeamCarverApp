package com.aengussong.seamcarver.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import com.aengussong.seamcarver.model.Picture

private const val MAX_IMAGE_SIZE_PX = 1000

/**
 * Rotate picture to position it's longest side to be vertical.
 * */
fun Picture.adjustAngle(): Picture {
    val rotation = if (this.width > this.height) 90f else return this
    val matrix = Matrix()
    matrix.postRotate(rotation)
    val bitmap = Bitmap.createBitmap(this.image, 0, 0, this.width, this.height, matrix, true)
    return Picture(bitmap)
}

/**
 * Scale picture so it's longer side won't be larger then [MAX_IMAGE_SIZE_PX]
 * */
fun Picture.adjustSize(): Picture {
    val prevBitmap = this.image
    if (prevBitmap.height < MAX_IMAGE_SIZE_PX && prevBitmap.width < MAX_IMAGE_SIZE_PX) return this
    val scale =
        (MAX_IMAGE_SIZE_PX.toFloat() / prevBitmap.width).coerceAtMost(MAX_IMAGE_SIZE_PX.toFloat() / prevBitmap.height)
    val newWidth = Math.round(prevBitmap.width * scale)
    val newHeight = Math.round(prevBitmap.height * scale)
    val scaledBitmap = Bitmap.createScaledBitmap(this.image, newWidth, newHeight, true)
    return Picture(scaledBitmap)
}