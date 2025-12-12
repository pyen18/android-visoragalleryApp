package com.example.learnapp01.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DrawerContent(onDestinationClicked: (route: String) -> Unit) {
    Column(
        modifier = Modifier
//            .padding(16.dp)
            .background(Color.Red, shape = RoundedCornerShape(8.dp))
            .width(250.dp)
            .fillMaxHeight(),
    ) {
        Text(
            "Menu",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 25.dp, top = 30.dp),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(20.dp))
        DrawerItem("Home", "home", onDestinationClicked)
        DrawerItem("Search", "search", onDestinationClicked)
        DrawerItem("Settings", "settings", onDestinationClicked)
        DrawerItem("Players", "players", onDestinationClicked)
    }
}

@Composable
fun DrawerItem(title: String, route: String, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(route) }
            .padding(12.dp)

    ) {
        Text(text = title, fontSize = 18.sp, color = Color.White)
    }
}