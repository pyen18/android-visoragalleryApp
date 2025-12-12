package com.example.learnapp01.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumnExample()
        LazyRowWithCard()
    }
}

@Composable
fun LazyColumnExample() {
    val itemsList = listOf("Apple", "Banana", "Cherry", "Date", "Elderberry")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(25.dp)
    ) {
        items(itemsList) { item ->
            Text(
                text = item,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}
@Composable
fun LazyRowExample() {
    val itemsList = listOf("Dog", "Cat", "Rabbit", "Parrot", "Hamster")

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        items(itemsList) { item ->
            Text(
                text = item,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun LazyRowWithCard() {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow)

    LazyRow(modifier = Modifier.padding(16.dp)) {
        items(colors) { color ->
            Card(
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp)
                    .background(color)
            ) {}
        }
    }
}
