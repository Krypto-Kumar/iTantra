package com.iTantra.app.ui.radio

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iTantra.app.ui.theme.*

@Composable
fun MainRadioScreen(
    state: RadioState,
    connectedDevice: String,
    speakingLanguage: String,
    listeningLanguage: String,
    onSettingsClick: () -> Unit,
    onStartTalking: () -> Unit,
    onStopTalking: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ITantraBackground)
            .padding(horizontal = 22.dp)
    ) {

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        RadioHeader(
            device = connectedDevice,
            onSettingsClick = onSettingsClick
        )

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        TransmissionDiagram(
            speakingLanguage = speakingLanguage,
            listeningLanguage = listeningLanguage
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        LanguageRow(
            speakingLanguage = speakingLanguage,
            listeningLanguage = listeningLanguage
        )

        Spacer(
            modifier = Modifier.weight(0.55f)
        )

        StateVisual(
            state = state,
            onStartTalking = onStartTalking,
            onStopTalking = onStopTalking
        )

        Spacer(
            modifier = Modifier.weight(0.45f)
        )

        Text(
            text = "Speech  →  Text  →  Bluetooth  →  Speech",
            color = ITantraSecondaryText,
            fontSize = 11.sp,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "Only lightweight text is transmitted",
            color = ITantraGreen,
            fontSize = 11.sp,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )
    }
}


@Composable
private fun RadioHeader(
    device: String,
    onSettingsClick: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = "iTANTRA",
                color = ITantraText,
                fontSize = 25.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Offline Multilingual Communication",
                color = ITantraSecondaryText,
                fontSize = 11.sp
            )
        }

        Surface(
            color = ITantraLightGreen,
            shape = RoundedCornerShape(100.dp)
        ) {

            Row(
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 7.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(
                            ITantraSuccess,
                            CircleShape
                        )
                )

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Text(
                    text = device,
                    color = ITantraDarkGreen,
                    fontSize = 12.sp
                )
            }
        }

        IconButton(
            onClick = onSettingsClick
        ) {

            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = ITantraGreen
            )
        }
    }
}


@Composable
private fun TransmissionDiagram(
    speakingLanguage: String,
    listeningLanguage: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        DiagramDevice(
            label = "YOU",
            icon = Icons.Default.Mic,
            language = speakingLanguage
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = ITantraGreen
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = null,
                tint = ITantraSecondaryText
            )
        }

        DiagramDevice(
            label = "RECEIVER",
            icon = Icons.Default.VolumeUp,
            language = listeningLanguage
        )
    }
}


@Composable
private fun DiagramDevice(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    language: String
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = label,
            color = ITantraSecondaryText,
            fontSize = 10.sp
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Box(
            modifier = Modifier
                .size(
                    width = 56.dp,
                    height = 74.dp
                )
                .border(
                    1.dp,
                    ITantraBorder,
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ITantraGreen,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = language,
            color = ITantraGreen,
            fontSize = 11.sp
        )
    }
}


@Composable
private fun LanguageRow(
    speakingLanguage: String,
    listeningLanguage: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        LanguageCard(
            heading = "I SPEAK",
            language = speakingLanguage,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .size(38.dp)
                .background(
                    ITantraLightGreen,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = null,
                tint = ITantraGreen
            )
        }

        LanguageCard(
            heading = "YOU HEAR",
            language = listeningLanguage,
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
private fun LanguageCard(
    heading: String,
    language: String,
    modifier: Modifier
) {

    Column(
        modifier = modifier
            .background(
                ITantraSurface,
                RoundedCornerShape(14.dp)
            )
            .border(
                1.dp,
                ITantraBorder,
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {

        Text(
            text = heading,
            color = ITantraGreen,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = language,
            color = ITantraText,
            fontSize = 16.sp
        )
    }
}


@Composable
private fun StateVisual(
    state: RadioState,
    onStartTalking: () -> Unit,
    onStopTalking: () -> Unit
) {

    val transition = rememberInfiniteTransition(
        label = "radio pulse"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radio pulse value"
    )

    val status = when (state) {

        RadioState.READY ->
            Triple(
                "Ready",
                "Hold to talk",
                Icons.Default.Mic
            )

        RadioState.LISTENING ->
            Triple(
                "Listening...",
                "Release to send",
                Icons.Default.Mic
            )

        RadioState.PROCESSING ->
            Triple(
                "Processing...",
                "Converting speech to text",
                Icons.Default.GraphicEq
            )

        RadioState.SENDING ->
            Triple(
                "Sending...",
                "Transmitting lightweight text",
                Icons.Default.Send
            )

        RadioState.RECEIVING ->
            Triple(
                "Receiving...",
                "Preparing incoming speech",
                Icons.Default.Download
            )

        RadioState.PLAYING ->
            Triple(
                "Playing...",
                "Generating speech",
                Icons.Default.VolumeUp
            )

        RadioState.ERROR ->
            Triple(
                "Connection Error",
                "Please reconnect",
                Icons.Default.ErrorOutline
            )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {

        Box(
            modifier = Modifier.size(190.dp),
            contentAlignment = Alignment.Center
        ) {

            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {

                val center = Offset(
                    size.width / 2,
                    size.height / 2
                )

                val alpha =
                    if (
                        state == RadioState.LISTENING ||
                        state == RadioState.PLAYING
                    ) pulse
                    else 0.35f

                drawCircle(
                    color = ITantraGreen.copy(
                        alpha = alpha * 0.12f
                    ),
                    radius = size.minDimension * .48f,
                    center = center,
                    style = Stroke(3f)
                )

                drawCircle(
                    color = ITantraGreen.copy(
                        alpha = alpha * 0.28f
                    ),
                    radius = size.minDimension * .37f,
                    center = center,
                    style = Stroke(3f)
                )

                drawCircle(
                    color = ITantraGreen.copy(
                        alpha = alpha * 0.5f
                    ),
                    radius = size.minDimension * .27f,
                    center = center,
                    style = Stroke(3f)
                )
            }

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        ITantraGreen,
                        CircleShape
                    )
                    .pointerInput(state) {

                        if (
                            state == RadioState.READY ||
                            state == RadioState.LISTENING
                        ) {

                            detectTapGestures(
                                onPress = {

                                    onStartTalking()

                                    tryAwaitRelease()

                                    onStopTalking()
                                }
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {

                if (
                    state == RadioState.PROCESSING ||
                    state == RadioState.SENDING ||
                    state == RadioState.RECEIVING
                ) {

                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )

                } else {

                    Icon(
                        imageVector = status.third,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }

        Text(
            text = status.first,
            color = ITantraGreen,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = status.second,
            color = ITantraSecondaryText,
            fontSize = 13.sp
        )
    }
}