package com.example.myfirstkmpapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
@Composable
fun App() {
    MaterialTheme {
        val platformName = remember { getPlatform().name }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Halo, Ray Regan Sitepu!",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "123140047",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Platform: $platformName",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}