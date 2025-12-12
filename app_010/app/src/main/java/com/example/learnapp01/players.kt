package com.example.learnapp01

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.learnapp01.R
import com.example.learnapp01.data.model.Player
import com.example.learnapp01.ui.theme.T1Gold
import com.example.learnapp01.ui.theme.T1Gray
import com.example.learnapp01.ui.theme.T1Navy
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersScreen(
    onPlayerClick: (String) -> Unit,
    viewModel: PlayersViewModel = viewModel()
) {
    val players by viewModel.players.collectAsState()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Content - Team Photo
        TeamPhotoContent(
            onShowPlayers = {
                showBottomSheet = true
            }
        )

        // Modal Bottom Sheet
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                tonalElevation = 0.dp,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Drag Handle
                        Box(
                            modifier = Modifier
                                .padding(top = 12.dp, bottom = 8.dp)
                                .width(40.dp)
                                .height(4.dp)
                                .background(
                                    T1Gray,
                                    RoundedCornerShape(2.dp)
                                )
                        )

                        // Title
                        Text(
                            text = "T1 Roster",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = T1Navy,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        Divider(color = T1Gray, thickness = 1.dp)
                    }
                }
            ) {
                // Players List in Bottom Sheet
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(players) { player ->
                        PlayerModalItem(
                            player = player,
                            onClick = {
                                scope.launch {
                                    sheetState.hide()
                                    showBottomSheet = false
                                }
                                onPlayerClick(player.name)
                            }
                        )
                        if (player != players.last()) {
                            Divider(color = T1Gray, thickness = 1.dp)
                        }
                    }

                    // Bottom Spacing
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamPhotoContent(
    onShowPlayers: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Full Screen Team Photo
        Image(
            painter = painterResource(id = R.drawable.team_photo),
            contentDescription = "Team Photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
        ) {
            // Header with Logo and Stars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Five Stars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(5) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_star),
                            contentDescription = null,
                            tint = T1Gold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // T1 Logo
                Image(
                    painter = painterResource(id = R.drawable.t1_logo_small),
                    contentDescription = "T1 Logo",
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Button to show players
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "World Champions",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "5x Worlds • 10x LCK",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = T1Gold,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Button(
                    onClick = onShowPlayers,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "VIEW ROSTER",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = T1Navy
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerModalItem(
    player: Player,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player Image
        Card(
            modifier = Modifier.size(72.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Image(
                painter = painterResource(id = player.imageRes),
                contentDescription = player.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Player Info
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = player.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = T1Navy
            )

            Text(
                text = player.realName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Arrow Icon
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_more),
            contentDescription = "View Details",
            tint = T1Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}