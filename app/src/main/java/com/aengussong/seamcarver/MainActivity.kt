package com.aengussong.seamcarver

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
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
import com.aengussong.seamcarver.algorithm.SeamCarver
import com.aengussong.seamcarver.model.Picture
import com.aengussong.seamcarver.ui.screen.MainScreen
import com.aengussong.seamcarver.ui.screen.SaveScreen
import com.aengussong.seamcarver.ui.screen.SeamCarverScreen
import com.aengussong.seamcarver.ui.theme.SeamCarverTheme
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : ComponentActivity() {
    private var imageUriDeferred = CompletableDeferred<Uri?>()
    private val mediaPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        imageUriDeferred.complete(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeamCarverTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var selectedFile: File? by remember {
                        mutableStateOf(null)
                    }
                    var resultPicture: Picture? by remember {
                        mutableStateOf(null)
                    }

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
                        val initPic = Picture(selectedFile!!).adjustAngle()
                        doShit(initPic)
//                        SeamCarverScreen() { finalPicture ->
//                        }
                    } else if (resultPicture != null) {
                        SaveScreen(resultPicture!!)
                    }
                }
            }
        }
    }

    var thread: Thread? = null

    fun doShit(pic: Picture) {
        thread = Thread {
            println("start shit")
            println("start computing shit energy")
            val seamCarver = SeamCarver(pic)
            println("shit energy computed")
            println("starting removing shit")
            for (i in 0..300) {
                println("remove shit number $i")
                val seam = seamCarver.findHorizontalSeam()
                seamCarver.removeHorizontalSeam(seam)
                println("removed $i shit")
            }
            seamCarver.generateNewPicture()
            println("finished removing shit")
        }
        thread?.start()
    }

    //todo move to util functions
    fun getFileFromUri(file: Uri, contentResolver: ContentResolver): File? {
        val filePath: String?
        try {
            val inputStream = contentResolver.openInputStream(file)
            filePath = cacheDir.path + "/" + getFileName(file)
            File(filePath).createNewFile()
            val outputStream = FileOutputStream(filePath)
            outputStream.write(inputStream?.readBytes())
            // close stream
            try {
                inputStream?.close()
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        }
        return File(filePath)
    }

    // todo move to util functions
    fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }
                        ?: throw IllegalArgumentException()
                    result = cursor.getString(columnIndex)
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    //todo move to utils
    fun Picture.adjustAngle(): Picture {
        val rotation = if (this.width() > this.height()) 90f else return this
        val matrix = Matrix()
        matrix.postRotate(rotation)
        val bitmap = Bitmap.createBitmap(this.getImage(), 0, 0, this.width(), this.height(), matrix, true)
        return Picture(bitmap)
    }

    private suspend fun getImageFile(): File? {
        mediaPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        val uri = imageUriDeferred.await()

        return uri?.let {
            getFileFromUri(it, contentResolver)
        }
    }
}