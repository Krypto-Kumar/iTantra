package com.iTantra.app.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iTantra.app.ui.theme.*

@Composable
fun HowItWorksScreen(
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ITantraBackground)
            .padding(horizontal = 22.dp)
    ) {

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack
            ) {

                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = ITantraText
                )
            }

            Text(
                text = "How iTANTRA Works",
                color = ITantraText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        HowStep(
            icon = Icons.Default.Mic,
            title = "Speech",
            description = "You speak in your language"
        )

        HowStep(
            icon = Icons.Default.GraphicEq,
            title = "VAD",
            description = "Detects voice automatically"
        )

        HowStep(
            icon = Icons.Default.TextFields,
            title = "STT",
            description = "Converts speech to text"
        )

        HowStep(
            icon = Icons.Default.Description,
            title = "Text",
            description = "Only lightweight data remains"
        )

        HowStep(
            icon = Icons.Default.Bluetooth,
            title = "Bluetooth",
            description = "Transmits the text securely"
        )

        HowStep(
            icon = Icons.Default.RecordVoiceOver,
            title = "TTS",
            description = "Converts received text into speech"
        )

        HowStep(
            icon = Icons.Default.VolumeUp,
            title = "Speech",
            description = "The listener hears audio in the target language",
            showLine = false
        )
    }
}


@Composable
private fun HowStep(
    icon: ImageVector,
    title: String,
    description: String,
    showLine: Boolean = true
) {

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        ITantraLightGreen,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ITantraGreen
                )
            }

            if (showLine) {

                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                        .background(ITantraBorder)
                )
            }
        }

        Spacer(
            modifier = Modifier.width(16.dp)
        )

        Column(
            modifier = Modifier.padding(top = 4.dp)
        ) {

            Text(
                text = title,
                color = ITantraText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = description,
                color = ITantraSecondaryText,
                fontSize = 13.sp
            )
        }
    }
}