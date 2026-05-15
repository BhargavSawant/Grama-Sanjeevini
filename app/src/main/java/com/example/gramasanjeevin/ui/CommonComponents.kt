package com.example.gramasanjeevin.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun ZoomableMedicineImage(imageResName: String, modifier: Modifier = Modifier, size: Int = 80) {
    val context = LocalContext.current
    val imageResId = remember(imageResName) {
        if (imageResName.isNotEmpty()) {
            context.resources.getIdentifier(imageResName, "drawable", context.packageName)
        } else 0
    }
    var showDialog by remember { mutableStateOf(false) }

    if (imageResId != 0) {
        Box(
            modifier = modifier
                .size(size.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                .clickable { showDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                modifier = Modifier.padding(8.dp).fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    tonalElevation = 8.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        var scale by remember { mutableStateOf(1f) }
                        var offset by remember { mutableStateOf(Offset.Zero) }
                        val state = rememberTransformableState { zoomChange, offsetChange, _ ->
                            scale *= zoomChange
                            offset += offsetChange
                        }

                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .graphicsLayer(
                                    scaleX = scale.coerceIn(1f, 5f),
                                    scaleY = scale.coerceIn(1f, 5f),
                                    translationX = offset.x,
                                    translationY = offset.y
                                )
                                .transformable(state = state),
                            contentScale = ContentScale.Fit
                        )
                        
                        IconButton(
                            onClick = { showDialog = false },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .size(size.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE0F2F1)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color(0xFF00695C), modifier = Modifier.size((size * 0.4).dp))
        }
    }
}
