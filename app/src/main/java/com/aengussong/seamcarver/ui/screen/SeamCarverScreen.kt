package com.aengussong.seamcarver.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun SeamCarverScreen(initialPicture: Picture, onSaveFile: (Picture) -> Unit) {
    val backgroundScope = rememberCoroutineScope { Dispatchers.Default }
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
            seamCarver = SeamCarver(initialPicture)
        }
    })
}

@Composable
fun ShowImage(seamCarver: SeamCarver) {
    val scope = rememberCoroutineScope { Dispatchers.Default }

    val pictureProcessor by remember {
        mutableStateOf(PictureProcessor(seamCarver.getPicture()))
    }

    val picture by pictureProcessor.pictureState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            bitmap = picture.image.asImageBitmap(),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            contentDescription = "Image to scale",
        )
    }

    // todo: test setup, remove after adding user controls
    LaunchedEffect(key1 = Unit, block = {
        scope.launch {
            delay(200)
            for (i in 0..900) {
                removeSeam(seamCarver, pictureProcessor)
            }
        }
    })
}

suspend fun removeSeam(seamCarver: SeamCarver, pictureProcessor: PictureProcessor) {
    coroutineScope {
        async(Dispatchers.IO) {
            val seam = seamCarver.findHorizontalSeam()
            seamCarver.removeHorizontalSeam(seam)
            pictureProcessor.sendPicture(seamCarver.getPicture())
        }
    }
}

class PictureProcessor(initialPicture: Picture) {

    // StateFlow here is used mainly for it's BufferOverflow.DROP_OLDEST behaviour
    val pictureState = MutableStateFlow(initialPicture)

    fun sendPicture(picture: Picture) {
        pictureState.value = picture
    }
}