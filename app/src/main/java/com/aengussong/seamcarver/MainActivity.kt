package com.aengussong.seamcarver

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.aengussong.seamcarver.model.Picture
import com.aengussong.seamcarver.ui.screen.MainScreen
import com.aengussong.seamcarver.ui.screen.SeamCarverScreen
import com.aengussong.seamcarver.ui.theme.SeamCarverTheme
import com.aengussong.seamcarver.utils.adjustAngle
import com.aengussong.seamcarver.utils.adjustSize
import com.aengussong.seamcarver.utils.getFileFromUri
import kotlinx.coroutines.CompletableDeferred
import java.io.File


class MainActivity : ComponentActivity() {
    private var imageUriDeferred = CompletableDeferred<Uri?>()
    private val mediaPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        imageUriDeferred.complete(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeamCarverTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var selectedFile: File? by remember {
                        mutableStateOf(null)
                    }

                    // navigation here is shit, but it's okay until there is 3 screens and not a lot of states
                    // navigation in compose is shit anyway, but this shit is simpler to setup
                    if (selectedFile == null) {
                        MainScreen(
                            fileSelector = {
                                getImageFile()
                            },
                            onFileSelected = { file ->
                                selectedFile = file
                            }
                        )
                    } else if (selectedFile != null) {
                        val initPic = Picture(selectedFile!!).adjustAngle().adjustSize()
                        SeamCarverScreen(initPic, onSaveFile = { pictureToSave ->

                        }, onShareFile = { pictureToShare ->

                        }, onBackPressed = {
                            selectedFile = null
                        })
                    }
                }
            }
        }
    }

    private suspend fun getImageFile(): File? {
        // reset deferred
        imageUriDeferred = CompletableDeferred()
        mediaPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        val uri = imageUriDeferred.await()

        return uri?.let {
            getFileFromUri(it, contentResolver, cacheDir.path)
        }
    }
}