package com.iTantra.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iTantra.app.ui.theme.*

@Composable
fun PermissionScreen(
    onContinue: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ITantraBackground)
            .padding(horizontal = 24.dp)
    ) {

        Spacer(
            modifier = Modifier.height(42.dp)
        )

        Text(
            text = "iTANTRA",
            fontSize = 30.sp,
            color = ITantraText,
            fontWeight = FontWeight.Light
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "A few permissions are needed",
            fontSize = 25.sp,
            color = ITantraText,
            fontWeight = FontWeight.Normal
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "These allow iTANTRA to discover nearby devices and enable communication without internet.",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = ITantraSecondaryText
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        PermissionCard(
            icon = Icons.Default.Bluetooth,
            title = "Nearby devices",
            description = "Discover and connect to nearby Bluetooth devices."
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        PermissionCard(
            icon = Icons.Default.Mic,
            title = "Microphone",
            description = "Required for voice communication and speech input."
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        PermissionCard(
            icon = Icons.Default.WifiOff,
            title = "Works offline",
            description = "Communication is designed to work without internet or cellular data."
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "You can manage permissions anytime in Settings.",
            color = ITantraSecondaryText,
            fontSize = 12.sp
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ITantraGreen
            )
        ) {

            Text(
                text = "Continue",
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 16.sp
            )
        }

        Spacer(
            modifier = Modifier.height(26.dp)
        )
    }
}


@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                ITantraSurface,
                RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = ITantraBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(62.dp)
                .background(
                    ITantraLightGreen,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ITantraGreen,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(
            modifier = Modifier.width(18.dp)
        )

        Column {

            Text(
                text = title,
                color = ITantraText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = description,
                color = ITantraSecondaryText,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}