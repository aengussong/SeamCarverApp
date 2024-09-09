package com.aengussong.seamcarver.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun MainScreen(fileSelector: suspend () -> File?, onFileSelected: (File) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxSize()) {
        Button(modifier = Modifier
            .align(Alignment.Center)
            .size(250.dp)
            .padding(10.dp), shape = CircleShape, onClick = {
            coroutineScope.launch {
                fileSelector()?.let { file ->
                    onFileSelected(file)
                }
            }
        }) {
            Text("Select image")
        }
    }
}