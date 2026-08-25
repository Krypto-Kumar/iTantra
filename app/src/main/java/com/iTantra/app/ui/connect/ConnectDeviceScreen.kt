package com.iTantra.app.ui.connect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iTantra.app.ui.theme.*

@Composable
fun ConnectDeviceScreen(
    devices: List<String>,
    onDeviceClick: (String) -> Unit,
    onScanAgain: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ITantraBackground)
            .padding(horizontal = 24.dp)
    ) {

        Spacer(
            modifier = Modifier.height(48.dp)
        )

        Text(
            text = "Connect Device",
            color = ITantraText,
            fontSize = 27.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Turn on Bluetooth to connect with nearby devices.",
            color = ITantraSecondaryText,
            fontSize = 14.sp
        )

        Spacer(
            modifier = Modifier.height(26.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    ITantraLightGreen,
                    RoundedCornerShape(14.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = null,
                tint = ITantraGreen
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Text(
                text = "Bluetooth",
                color = ITantraText,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "●  On",
                color = ITantraSuccess,
                fontSize = 13.sp
            )
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Text(
            text = "Nearby Devices",
            color = ITantraSecondaryText,
            fontSize = 13.sp
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        devices.forEach { device ->

            DeviceCard(
                deviceName = device,
                onClick = {
                    onDeviceClick(device)
                }
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        OutlinedButton(
            onClick = onScanAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ITantraGreen
            )
        ) {

            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = "Scan Again"
            )
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )
    }
}