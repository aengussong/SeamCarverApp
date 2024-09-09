package com.aengussong.seamcarver.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.aengussong.seamcarver.algorithm.SeamCarver
import com.aengussong.seamcarver.model.Picture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SeamCarverScreen(
    initialPicture: Picture,
    onSaveFile: (Picture) -> Unit,
    onShareFile: (Picture) -> Unit,
    onBackPressed: () -> Unit
) {
    BackHandler {
        onBackPressed()
    }

    val backgroundScope = rememberCoroutineScope { Dispatchers.Default }
    var seamCarver: SeamCarver? by remember {
        mutableStateOf(null)
    }
    Box {
        if (seamCarver == null) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.Center)
            )
        } else {
            ShowImage(seamCarver!!, onSaveFile, onShareFile)
        }

        LaunchedEffect(key1 = Unit, block = {
            backgroundScope.launch {
                seamCarver = SeamCarver(initialPicture)
            }
        })
    }
}

@Composable
fun ShowImage(seamCarver: SeamCarver, onSaveFile: (Picture) -> Unit, onShareFile: (Picture) -> Unit) {
    val scope = rememberCoroutineScope { Dispatchers.Default }

    val pictureProcessor by remember {
        mutableStateOf(PictureProcessor(seamCarver.getPicture()))
    }

    val picture by pictureProcessor.pictureState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(100f)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
        ) {
            Row {
                Spacer(modifier = Modifier.weight(1f))
                RoundButton(Icons.Default.Share, "Share image") {
                    onShareFile(seamCarver.getPicture())
                }
                RoundButton(Icons.Default.Done, "Save image") {
                    onSaveFile(seamCarver.getPicture())
                }
            }
        }

        Image(
            bitmap = picture.image.asImageBitmap(),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f),
            contentScale = ContentScale.Fit,
            contentDescription = "Image to scale",
        )

        Column(
            modifier = Modifier
                .zIndex(100f)
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(5.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                text = "${picture.width}x${picture.height}"
            )

            val horizontalSqueezeInteractionSource = remember { MutableInteractionSource() }
            val verticalSqueezeInteractionSource = remember { MutableInteractionSource() }

            Row {
                InteractionButton(
                    "Squeeze horizontal",
                    horizontalSqueezeInteractionSource,
                    verticalSqueezeInteractionSource,
                    seamCarver,
                    pictureProcessor,
                    HORIZONTAL,
                    modifier = Modifier.weight(1f),
                )

                InteractionButton(
                    "Squeeze vertical",
                    verticalSqueezeInteractionSource,
                    horizontalSqueezeInteractionSource,
                    seamCarver,
                    pictureProcessor,
                    VERTICAL,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun InteractionButton(
    text: String,
    mainInteractionSource: MutableInteractionSource,
    otherButtonInteractionSource: MutableInteractionSource,
    seamCarver: SeamCarver,
    pictureProcessor: PictureProcessor,
    orientation: RemovedSeamOrientation,
    modifier: Modifier = Modifier
) {
    val buttonPressed by mainInteractionSource.collectIsPressedAsState()
    val otherButtonPressed by otherButtonInteractionSource.collectIsPressedAsState()

    Button(
        modifier = modifier.padding(10.dp),
        interactionSource = mainInteractionSource,
        enabled = !otherButtonPressed,
        onClick = {}) {
        Text(text)
    }

    LaunchedEffect(buttonPressed) {
        withContext(Dispatchers.IO) {
            while (buttonPressed) {
                removeSeam(seamCarver, pictureProcessor, orientation)
            }
        }
    }
}

@Composable
fun RoundButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .size(70.dp)
            .padding(10.dp),
        shape = CircleShape,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            icon, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.onBackground
        )
    }
}

fun removeSeam(
    seamCarver: SeamCarver,
    pictureProcessor: PictureProcessor,
    orientation: RemovedSeamOrientation
) {
    if (orientation == HORIZONTAL) {
        val seam = seamCarver.findHorizontalSeam()
        seamCarver.removeHorizontalSeam(seam)
    } else {
        val seam = seamCarver.findVerticalSeam()
        seamCarver.removeVerticalSeam(seam)
    }

    pictureProcessor.sendPicture(seamCarver.getPicture())

}

class PictureProcessor(initialPicture: Picture) {

    // StateFlow here is used mainly for it's BufferOverflow.DROP_OLDEST behaviour
    val pictureState = MutableStateFlow(initialPicture)

    fun sendPicture(picture: Picture) {
        pictureState.value = picture
    }
}

sealed interface RemovedSeamOrientation
object VERTICAL : RemovedSeamOrientation
object HORIZONTAL : RemovedSeamOrientation