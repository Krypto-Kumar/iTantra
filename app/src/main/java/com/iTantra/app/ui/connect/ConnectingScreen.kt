package com.iTantra.app.ui.connect

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iTantra.app.ui.theme.*

@Composable
fun ConnectingScreen(
    deviceName: String
) {

    val transition = rememberInfiniteTransition(
        label = "connecting"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ITantraBackground)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(
            modifier = Modifier.height(80.dp)
        )

        Text(
            text = "Connecting...",
            color = ITantraText,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Text(
            text = "Please wait while we\nestablish a secure link.",
            color = ITantraSecondaryText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(
            modifier = Modifier.height(60.dp)
        )

        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {

            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {

                val center = Offset(
                    size.width / 2,
                    size.height / 2
                )

                drawCircle(
                    color = ITantraGreen.copy(alpha = pulse * 0.15f),
                    radius = size.minDimension * 0.48f,
                    center = center,
                    style = Stroke(3f)
                )

                drawCircle(
                    color = ITantraGreen.copy(alpha = pulse * 0.3f),
                    radius = size.minDimension * 0.36f,
                    center = center,
                    style = Stroke(3f)
                )

                drawCircle(
                    color = ITantraGreen.copy(alpha = pulse * 0.55f),
                    radius = size.minDimension * 0.25f,
                    center = center,
                    style = Stroke(3f)
                )
            }

            Box(
                modifier = Modifier
                    .size(82.dp)
                    .background(
                        ITantraLightGreen,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = ITantraGreen,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    ITantraSurface,
                    RoundedCornerShape(18.dp)
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Connecting to",
                    color = ITantraSecondaryText,
                    fontSize = 12.sp
                )

                Text(
                    text = deviceName,
                    color = ITantraText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = ITantraGreen,
                strokeWidth = 2.dp
            )
        }

        Spacer(
            modifier = Modifier.height(36.dp)
        )
    }
}