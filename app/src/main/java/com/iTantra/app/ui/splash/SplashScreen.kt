package com.iTantra.app.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iTantra.app.ui.theme.*

@Composable
fun SplashScreen() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ITantraBackground)
    ) {

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Simple temporary logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {

                listOf(
                    22.dp,
                    42.dp,
                    64.dp,
                    42.dp,
                    22.dp
                ).forEach { height ->

                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(height)
                            .clip(CircleShape)
                            .background(ITantraGreen)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Text(
                text = "iTANTRA",
                color = ITantraText,
                fontSize = 36.sp,
                fontWeight = FontWeight.Light
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Offline Multilingual\nCommunication",
                color = ITantraSecondaryText,
                fontSize = 16.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = "Voice beyond bandwidth",
            color = ITantraGreen,
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 42.dp)
        )
    }
}