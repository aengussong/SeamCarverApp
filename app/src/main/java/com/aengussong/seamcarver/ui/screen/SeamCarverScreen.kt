package com.aengussong.seamcarver.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.aengussong.seamcarver.algorithm.SeamCarver
import com.aengussong.seamcarver.model.Picture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SeamCarverScreen(initialPicture: Picture, onSaveFile: (Picture) -> Unit) {
    val backgroundScope = rememberCoroutineScope {Dispatchers.Default}
    var seamCarver: SeamCarver? by remember {
        mutableStateOf(null)
    }

    if (seamCarver == null) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
    } else {
        ShowImage(seamCarver!!)
    }

    LaunchedEffect(key1 = Unit, block = {
        backgroundScope.launch {
            println("initializing seam carver shit")
            seamCarver = SeamCarver(initialPicture)
            println("done calculating energy shit")
        }
    })
}

@Composable
fun ShowImage(seamCarver: SeamCarver) {
    var picture by remember {
        mutableStateOf(seamCarver.picture())
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            bitmap = picture.getImage().asImageBitmap(),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            contentDescription = "test",
        )
    }

    LaunchedEffect(key1 = Unit, block = {
        delay(200)
        println("starting shit")
        for (i in 0..1000) {
            val seam = seamCarver.findHorizontalSeam()
            seamCarver.removeHorizontalSeam(seam)
            println("remove $i shit")
        }
        seamCarver.generateNewPicture()
        println("finished removing shit")
    })
}