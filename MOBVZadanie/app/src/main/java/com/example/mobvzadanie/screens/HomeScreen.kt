package com.example.mobvzadanie.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToQuizSettingsScreen: () -> Unit,
    onNavigateToHistoryScreen: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onNavigateToQuizSettingsScreen,
            modifier = Modifier.height(64.dp).width(256.dp)
        ) {
            Text(text = "Začať kvíz", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onNavigateToHistoryScreen,
            modifier = Modifier.height(64.dp).width(256.dp)
        ) {
            Text(text = "História kvízov", fontSize = 20.sp)
        }
    }
}