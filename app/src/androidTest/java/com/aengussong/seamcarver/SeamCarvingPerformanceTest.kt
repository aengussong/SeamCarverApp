package com.aengussong.seamcarver

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aengussong.seamcarver.algorithm.SeamCarver
import com.aengussong.seamcarver.model.Picture
import com.aengussong.seamcarver.utils.adjustAngle
import com.aengussong.seamcarver.utils.adjustSize
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


/**
test image 3024x4032

loading row of pixels instead of each pixel one by one - performance improved 2X
on energy calculation
-
moving to C++ - no impact on seam search
-
downscaling image to max 1000 pixels - great performance impact, but bastardizes an image - this
isn't a desired solution to performance issues
-
changing bitmap config to RGB_565 - worse energy calculation

base case measurements:
energy calculation - 27s
vertical seam search - 3s
seam removal (includes energy recalculation) - 41s
 */
@OptIn(ExperimentalTime::class)
@RunWith(AndroidJUnit4::class)
class SeamCarvingPerformanceTest {

    private val performanceTag = "PERF"

    private lateinit var testPicture: Picture

    @Before
    fun initData() {
        val testBitmap = getTestImage("test_img")
        testPicture = Picture(testBitmap).adjustAngle().adjustSize()
    }

    @Test
    fun testHorizontalSeamRemoval() {
        val seamCarver: SeamCarver
        val energyCalculationDuration = measureTime {
            seamCarver = SeamCarver(testPicture)
        }
        println("$performanceTag: energy calculated in ${energyCalculationDuration.inWholeMilliseconds}ms")

        val seam: IntArray
        val seamSearchDuration = measureTime {
            seam = seamCarver.findHorizontalSeam()
        }
        println("$performanceTag: horizontal seam found in ${seamSearchDuration.inWholeMilliseconds}ms")

        val removeAndRecalculateEnergyDuration = measureTime {
            seamCarver.removeHorizontalSeam(seam)
        }
        println("$performanceTag: horizontal seam removed and energy recalculated in ${removeAndRecalculateEnergyDuration.inWholeMilliseconds}ms")
    }

    @Test
    fun testVerticalSeamRemoval() {
        val seamCarver: SeamCarver
        val energyCalculationDuration = measureTime {
            seamCarver = SeamCarver(testPicture)
        }
        println("$performanceTag: energy calculated in ${energyCalculationDuration.inWholeMilliseconds}ms")


        val seam: IntArray
        val seamSearchDuration = measureTime {
            seam = seamCarver.findVerticalSeam()
        }
        println("$performanceTag: vertical seam found in ${seamSearchDuration.inWholeMilliseconds}ms")

        val removeAndRecalculateEnergyDuration = measureTime {
            seamCarver.removeVerticalSeam(seam)
        }
        println("$performanceTag: vertical seam removed and energy recalculated in ${removeAndRecalculateEnergyDuration.inWholeMilliseconds}ms")

    }

    private fun getTestImage(resourceName: String): Bitmap {
        val context = InstrumentationRegistry.getInstrumentation().context
        val resourceId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
        val drawable = context.getDrawable(resourceId) as BitmapDrawable
        return drawable.bitmap
    }
}