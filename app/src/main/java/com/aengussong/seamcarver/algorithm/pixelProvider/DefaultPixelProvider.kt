package com.aengussong.seamcarver.algorithm.pixelProvider

import com.aengussong.seamcarver.model.Picture

class DefaultPixelProvider(private val picture: Picture) : PixelProvider {
    override fun get(x: Int, y: Int): Int {
        return picture.getRGB(x, y)
    }
}