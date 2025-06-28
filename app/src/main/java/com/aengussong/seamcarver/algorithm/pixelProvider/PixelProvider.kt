package com.aengussong.seamcarver.algorithm.pixelProvider

interface PixelProvider {
    fun getPixel(x: Int, y: Int): Int
}