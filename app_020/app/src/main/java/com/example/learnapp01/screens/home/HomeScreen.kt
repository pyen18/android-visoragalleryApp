package com.example.learnapp01.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterStart,
    ) {
        RadioButton(
            selected = true,
            onClick = { /* Do nothing */ }
        )
        RadioButton(
            enabled = true,
            selected = false,
            onClick = { /* Do nothing */ }
        )
        Spacer(modifier = Modifier.height(25.dp))
        // TextField
        DemoTextField()
    }
}
@Composable
fun DemoTextField() {
    var firstName by remember {
        mutableStateOf("")
    }
    TextField(
        value = firstName,
        onValueChange = { newValue ->
            firstName = newValue
        },
        textStyle = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        ),
    )
}


