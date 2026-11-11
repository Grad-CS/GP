
package com.example.minder.features.blocking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChallengeLockedScreen(
    onExitApp: () -> Unit
) {
    // Lama - Display the challenge lock without extensions.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF071B33))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔒",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Apps Locked",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "You have used all 3 challenge attempts.",
            color = Color.White,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Restricted apps will be available tomorrow.",
            color = Color(0xFFB8C7D9),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(onClick = onExitApp) {
            Text("Exit App")
        }
    }
}
