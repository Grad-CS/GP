package com.example.minder.features.blocking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minder.ui.theme.OutfitFont
import com.example.minder.ui.theme.JakartaFont

private val Purple = Color(0xFF9061D8)
private val Turquoise = Color(0xFF38C9C5)
private val Ink = Color(0xFF302342)

@Composable
fun ChallengeLockedScreen(onExitApp: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F8FE))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Minder", fontFamily = OutfitFont, fontSize = 29.sp,
            fontWeight = FontWeight.Bold, color = Purple)
        Spacer(Modifier.height(40.dp))
        Box(
            modifier = Modifier
                .size(124.dp)
                .background(Color(0xFFE5F8F6), RoundedCornerShape(62.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🔒", fontSize = 62.sp)
        }
        Spacer(Modifier.height(26.dp))
        Text("Challenge Locked", fontFamily = OutfitFont, fontSize = 30.sp,
            fontWeight = FontWeight.Bold, color = Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text("You've used all three attempts. Access to your restricted apps is paused until tomorrow.",
            fontFamily = JakartaFont, fontSize = 15.sp, lineHeight = 24.sp,
            color = Color(0xFF655B76), textAlign = TextAlign.Center)
        Spacer(Modifier.height(26.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5EEFC), RoundedCornerShape(26.dp))
                .border(1.dp, Color(0xFFE9DDF9), RoundedCornerShape(26.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Try Again Tomorrow", fontFamily = OutfitFont,
                fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Purple)
            Spacer(Modifier.height(8.dp))
            Text("Take this time to rest and return with a fresh start.",
                fontFamily = JakartaFont, fontSize = 14.sp, lineHeight = 22.sp,
                textAlign = TextAlign.Center, color = Color(0xFF655B76))
        }
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onExitApp,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
        ) {
            Text("Exit App", fontFamily = OutfitFont, fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp, color = Ink)
        }
    }
}
