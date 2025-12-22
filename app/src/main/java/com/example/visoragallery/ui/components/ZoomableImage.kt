package com.example.visoragallery.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import java.io.File

@Composable
fun ZoomableImage(
    file: File,
    contentDescription: String?,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val zoomState = rememberZoomableState()

    AsyncImage(
        model = file,
        contentDescription = contentDescription,
        modifier = modifier
            .zoomable(zoomState)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() }
                )
            },
        contentScale = ContentScale.Fit
    )
}