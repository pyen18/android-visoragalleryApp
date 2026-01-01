package com.example.visoragallery.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.visoragallery.data.AlbumItem
import com.example.visoragallery.data.AlbumType

@Composable
fun AddToAlbumDialog(
    albums: List<AlbumItem>,
    selectedPhotosCount: Int,
    onDismiss: () -> Unit,
    onAlbumSelected: (AlbumItem) -> Unit,
    onCreateNewAlbum: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
        },
        title = {
            Text("Add to Album")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Add $selectedPhotosCount photo${if (selectedPhotosCount > 1) "s" else ""} to:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Create new album button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCreateNewAlbum() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CreateNewFolder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Create New Album",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Album list
                if (albums.isEmpty()) {
                    Text(
                        text = "No albums yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(albums) { album ->
                            AlbumListItem(
                                album = album,
                                onClick = { onAlbumSelected(album) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AlbumListItem(
    album: AlbumItem,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(album.name) },
        supportingContent = {
            Text("${album.photoCount} photo${if (album.photoCount != 1) "s" else ""}")
        },
        leadingContent = {
            Icon(
                imageVector = when (album.type) {
                    AlbumType.FAVORITES -> Icons.Filled.Favorite
                    AlbumType.PRIVATE -> Icons.Filled.Lock
                    AlbumType.TRASH_BIN -> Icons.Filled.Delete
                    AlbumType.USER_DEFINED -> Icons.Filled.Folder
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
    HorizontalDivider()
}

@Composable
fun CreateAlbumDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var albumName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Filled.CreateNewFolder, contentDescription = null)
        },
        title = {
            Text("Create New Album")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = albumName,
                    onValueChange = {
                        albumName = it
                        showError = false
                    },
                    label = { Text("Album name") },
                    placeholder = { Text("Enter album name") },
                    singleLine = true,
                    isError = showError,
                    supportingText = if (showError) {
                        { Text("Please enter album name") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (albumName.isBlank()) {
                        showError = true
                    } else {
                        onConfirm(albumName.trim())
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}