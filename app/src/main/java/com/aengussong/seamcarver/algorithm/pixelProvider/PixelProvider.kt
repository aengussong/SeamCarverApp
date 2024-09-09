package com.aengussong.seamcarver.algorithm.pixelProvider

interface PixelProvider {
    fun get(x: Int, y: Int): Int
}