package com.aengussong.seamcarver.ui.screen

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun MainScreen(fileSelector: suspend ()-> File?, onFileSelected: (File) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    Button(onClick = {
        coroutineScope.launch {
            fileSelector()?.let { file ->
                onFileSelected(file)
            }
        }
    }) {
        Text("Select file")
    }
}