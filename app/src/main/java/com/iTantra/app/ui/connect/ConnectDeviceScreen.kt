package com.iTantra.app.ui.connect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
            modifier = Modifier.height(42.dp)
        )


        // HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Connect Device",
                color = ITantraText,
                fontSize = 27.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )


            // SCAN AGAIN - TOP RIGHT
            TextButton(
                onClick = onScanAgain
            ) {

                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Scan Again",
                    tint = ITantraGreen,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(
                    modifier = Modifier.width(5.dp)
                )

                Text(
                    text = "Scan",
                    color = ITantraGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        Text(
            text = "Turn on Bluetooth to connect with nearby devices.",
            color = ITantraSecondaryText,
            fontSize = 14.sp,
            lineHeight = 21.sp
        )


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // BLUETOOTH STATUS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    ITantraLightGreen,
                    RoundedCornerShape(16.dp)
                )
                .padding(
                    horizontal = 18.dp,
                    vertical = 18.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = null,
                tint = ITantraGreen,
                modifier = Modifier.size(28.dp)
            )


            Spacer(
                modifier = Modifier.width(14.dp)
            )


            Text(
                text = "Bluetooth",
                color = ITantraText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )


            Text(
                text = "●  On",
                color = ITantraSuccess,
                fontSize = 14.sp
            )
        }


        Spacer(
            modifier = Modifier.height(28.dp)
        )


        Text(
            text = "Nearby Devices",
            color = ITantraSecondaryText,
            fontSize = 14.sp
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // SCROLLABLE DEVICE LIST
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                bottom = 24.dp
            )
        ) {

            items(
                items = devices,
                key = { device ->
                    device
                }
            ) { device ->

                DeviceCard(
                    deviceName = device,
                    onClick = {
                        onDeviceClick(device)
                    }
                )
            }
        }
    }
}