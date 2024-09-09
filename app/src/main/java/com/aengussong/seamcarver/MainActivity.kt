package com.aengussong.seamcarver

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.aengussong.seamcarver.model.Picture
import com.aengussong.seamcarver.ui.screen.MainScreen
import com.aengussong.seamcarver.ui.screen.SeamCarverScreen
import com.aengussong.seamcarver.ui.theme.SeamCarverTheme
import com.aengussong.seamcarver.utils.adjustSize
import com.aengussong.seamcarver.utils.getFileFromUri
import kotlinx.coroutines.CompletableDeferred
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class MainActivity : ComponentActivity() {
    private var imageUriDeferred = CompletableDeferred<Uri?>()
    private val mediaPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        imageUriDeferred.complete(uri)
    }

    private var pictureToSave: Bitmap? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue with the operation.
            pictureToSave?.let(::saveImageToGallery)
        }
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
                        val initPic = Picture(selectedFile!!).adjustSize()
                        SeamCarverScreen(initPic, onSaveFile = { picture ->
                            pictureToSave = picture.image
                            checkAndRequestWritePermissions(pictureToSave!!)
                        }, onShareFile = { picture ->
                            shareImage(picture.image, "SeamCarving_${System.currentTimeMillis()}.jpg")
                        }, onBackPressed = {
                            selectedFile = null
                        })
                    }
                }
            }
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val title = "SeamCarver_${System.currentTimeMillis()}"
        val timestamp = System.currentTimeMillis()
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$title.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.TITLE, title)
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SeamCarver") // For Android 10+

            put(MediaStore.Images.Media.DATE_ADDED, timestamp / 1000) // Timestamp in seconds
            put(MediaStore.Images.Media.DATE_MODIFIED, timestamp / 1000) // Timestamp in seconds
            put(MediaStore.Images.Media.DATE_TAKEN, timestamp) // Timestamp in milliseconds
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            var outputStream: OutputStream? = null
            try {
                outputStream = contentResolver.openOutputStream(it)!!
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                Toast.makeText(this, getString(R.string.image_saved_to_gallery), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, getString(R.string.error_saving_image), Toast.LENGTH_SHORT).show()
            } finally {
                outputStream?.close()
            }
        } ?: run {
            Toast.makeText(this, getString(R.string.error_creating_media_store_entry), Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndRequestWritePermissions(bitmap: Bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // No need to request WRITE_EXTERNAL_STORAGE for Android 10+
            saveImageToGallery(bitmap)
        } else {
            // Android 9 and below: Request WRITE_EXTERNAL_STORAGE permission
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted. Save the image.
                    saveImageToGallery(bitmap)
                }

                else -> {
                    // Request the permission
                    requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun shareImage(bitmap: Bitmap, fileName: String) {
        try {
            // Save bitmap to a file in internal cache directory
            val file = File(cacheDir, fileName)
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.close()

            // Get the URI using FileProvider
            val uri: Uri = FileProvider.getUriForFile(this, "com.aengussong.seamcarver.fileprovider", file)

            // Create a share intent
            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "image/jpeg"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Launch the share intent
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_image)))
        } catch (e: IOException) {
            e.printStackTrace()
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