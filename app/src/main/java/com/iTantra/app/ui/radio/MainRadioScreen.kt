package com.iTantra.app.ui.radio

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iTantra.app.ui.theme.ITantraBackground
import com.iTantra.app.ui.theme.ITantraBorder
import com.iTantra.app.ui.theme.ITantraDarkGreen
import com.iTantra.app.ui.theme.ITantraGreen
import com.iTantra.app.ui.theme.ITantraLightGreen
import com.iTantra.app.ui.theme.ITantraSecondaryText
import com.iTantra.app.ui.theme.ITantraSuccess
import com.iTantra.app.ui.theme.ITantraSurface
import com.iTantra.app.ui.theme.ITantraText

private val supportedLanguages = listOf(
    "Hindi",
    "English",
    "Tamil",
    "Telugu",
    "Bengali",
    "Gujarati",
    "Malayalam",
    "Marathi",
    "Odia",
    "Kannada"
)

@Composable
fun MainRadioScreen(
    state: RadioState,
    connectedDevice: String,
    connectedDeviceCount: Int = 1,
    speakingLanguage: String,
    listeningLanguage: String,

    onSpeakingLanguageChange: (String) -> Unit,
    onListeningLanguageChange: (String) -> Unit,

    onRoomClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,

    onStartTalking: () -> Unit,
    onStopTalking: () -> Unit
) {

    var drawerOpen by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ITantraBackground)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
        ) {

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // TOP HEADER
            RadioHeader(
                connectedDeviceCount = connectedDeviceCount,
                onMenuClick = {
                    drawerOpen = true
                }
            )

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            // TOP DEVICE DIAGRAM
            TransmissionDiagram(
                speakingLanguage = speakingLanguage,
                listeningLanguage = listeningLanguage,
                speakerActive = state == RadioState.LISTENING,
                receiverActive =
                    state == RadioState.RECEIVING ||
                            state == RadioState.PLAYING
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // LANGUAGE DROPDOWNS
            LanguageRow(
                speakingLanguage = speakingLanguage,
                listeningLanguage = listeningLanguage,
                onSpeakingLanguageChange = onSpeakingLanguageChange,
                onListeningLanguageChange = onListeningLanguageChange,
                onSwapLanguages = {
                    val oldSpeaking = speakingLanguage

                    onSpeakingLanguageChange(
                        listeningLanguage
                    )

                    onListeningLanguageChange(
                        oldSpeaking
                    )
                }
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
                text = "Only lightweight text is transmitted",
                color = ITantraGreen,
                fontSize = 12.sp,
                modifier = Modifier.align(
                    Alignment.CenterHorizontally
                )
            )

            Spacer(
                modifier = Modifier.height(28.dp)
            )
        }

        // DRAWER
        if (drawerOpen) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.25f)
                    )
                    .clickable {
                        drawerOpen = false
                    }
            )

            NavigationDrawer(
                onClose = {
                    drawerOpen = false
                },
                onRoomClick = {
                    drawerOpen = false
                    onRoomClick()
                },
                onSettingsClick = {
                    drawerOpen = false
                    onSettingsClick()
                },
                onHistoryClick = {
                    drawerOpen = false
                    onHistoryClick()
                },
                onAboutClick = {
                    drawerOpen = false
                    onAboutClick()
                }
            )
        }
    }
}

@Composable
private fun RadioHeader(
    connectedDeviceCount: Int,
    onMenuClick: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // MENU BUTTON
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(44.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = ITantraGreen,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            // CONNECTED DEVICES
            Surface(
                color = ITantraLightGreen,
                shape = RoundedCornerShape(100.dp)
            ) {

                Row(
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 7.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = ITantraSuccess,
                                shape = CircleShape
                            )
                    )

                    Spacer(
                        modifier = Modifier.width(5.dp)
                    )

                    Text(
                        text = "$connectedDeviceCount " +
                                if (connectedDeviceCount == 1) {
                                    "Device"
                                } else {
                                    "Devices"
                                },
                        color = ITantraDarkGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        // CENTERED LOGO / NAME
        Text(
            text = "iTANTRA",
            color = ITantraText,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(
            modifier = Modifier.height(3.dp)
        )

        Text(
            text = "Offline Multilingual Communication",
            color = ITantraSecondaryText,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun NavigationDrawer(
    onClose: () -> Unit,
    onRoomClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onAboutClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(290.dp)
            .background(
                ITantraBackground,
                RoundedCornerShape(
                    topEnd = 24.dp,
                    bottomEnd = 24.dp
                )
            )
            .padding(
                horizontal = 20.dp,
                vertical = 24.dp
            )
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "iTANTRA",
                    color = ITantraGreen,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onClose
                ) {

                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Close menu",
                        tint = ITantraText
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(30.dp)
            )

            DrawerItem(
                icon = Icons.Default.Groups,
                title = "Room",
                onClick = onRoomClick
            )

            DrawerItem(
                icon = Icons.Default.Settings,
                title = "Settings",
                onClick = onSettingsClick
            )

            DrawerItem(
                icon = Icons.Default.History,
                title = "History",
                onClick = onHistoryClick
            )

            DrawerItem(
                icon = Icons.Default.Info,
                title = "About App",
                onClick = onAboutClick
            )
        }
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 10.dp,
                vertical = 15.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    ITantraLightGreen,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = ITantraGreen,
                modifier = Modifier.size(21.dp)
            )
        }

        Spacer(
            modifier = Modifier.width(14.dp)
        )

        Text(
            text = title,
            color = ITantraText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TransmissionDiagram(
    speakingLanguage: String,
    listeningLanguage: String,
    speakerActive: Boolean,
    receiverActive: Boolean
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        DiagramDevice(
            label = "YOU",
            icon = Icons.Default.Mic,
            language = speakingLanguage,
            isActive = speakerActive
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
            language = listeningLanguage,
            isActive = receiverActive
        )
    }
}

@Composable
private fun DiagramDevice(
    label: String,
    icon: ImageVector,
    language: String,
    isActive: Boolean
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = label,
            color = if (isActive) {
                ITantraGreen
            } else {
                ITantraSecondaryText
            },
            fontSize = 11.sp,
            fontWeight = if (isActive) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Box(
            modifier = Modifier
                .size(
                    width = 58.dp,
                    height = 74.dp
                )
                .background(
                    color = if (isActive) {
                        ITantraLightGreen
                    } else {
                        ITantraSurface
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = if (isActive) 2.dp else 1.dp,
                    color = if (isActive) {
                        ITantraGreen
                    } else {
                        ITantraBorder
                    },
                    shape = RoundedCornerShape(12.dp)
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
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = language,
            color = if (isActive) {
                ITantraGreen
            } else {
                ITantraSecondaryText
            },
            fontSize = 12.sp,
            fontWeight = if (isActive) {
                FontWeight.SemiBold
            } else {
                FontWeight.Normal
            }
        )
    }
}

@Composable
private fun LanguageRow(
    speakingLanguage: String,
    listeningLanguage: String,
    onSpeakingLanguageChange: (String) -> Unit,
    onListeningLanguageChange: (String) -> Unit,
    onSwapLanguages: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        LanguageDropdownCard(
            heading = "I SPEAK",
            language = speakingLanguage,
            onLanguageSelected = onSpeakingLanguageChange,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .size(40.dp)
                .background(
                    ITantraLightGreen,
                    CircleShape
                )
                .clickable {
                    onSwapLanguages()
                },
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Swap languages",
                tint = ITantraGreen
            )
        }

        LanguageDropdownCard(
            heading = "YOU HEAR",
            language = listeningLanguage,
            onLanguageSelected = onListeningLanguageChange,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LanguageDropdownCard(
    heading: String,
    language: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    ITantraSurface,
                    RoundedCornerShape(16.dp)
                )
                .border(
                    1.dp,
                    ITantraBorder,
                    RoundedCornerShape(16.dp)
                )
                .clickable {
                    expanded = true
                }
                .padding(16.dp)
        ) {

            Text(
                text = heading,
                color = ITantraGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = language,
                    color = ITantraText,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select language",
                    tint = ITantraSecondaryText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier
                .background(Color.White)
                .width(190.dp)
        ) {

            supportedLanguages.forEach { option ->

                DropdownMenuItem(
                    text = {

                        Text(
                            text = option,
                            color = if (option == language) {
                                ITantraGreen
                            } else {
                                ITantraText
                            },
                            fontWeight = if (option == language) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            }
                        )
                    },
                    onClick = {

                        onLanguageSelected(option)
                        expanded = false
                    }
                )
            }
        }
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

    val title: String
    val subtitle: String
    val icon: ImageVector

    when (state) {

        RadioState.READY -> {
            title = "Ready"
            subtitle = "Hold to talk"
            icon = Icons.Default.Mic
        }

        RadioState.LISTENING -> {
            title = "Listening..."
            subtitle = "Release to send"
            icon = Icons.Default.Mic
        }

        RadioState.PROCESSING -> {
            title = "Processing..."
            subtitle = "Converting speech to text"
            icon = Icons.Default.GraphicEq
        }

        RadioState.SENDING -> {
            title = "Sending..."
            subtitle = "Transmitting lightweight text"
            icon = Icons.Default.Send
        }

        RadioState.RECEIVING -> {
            title = "Receiving..."
            subtitle = "Preparing incoming speech"
            icon = Icons.Default.Download
        }

        RadioState.PLAYING -> {
            title = "Playing..."
            subtitle = "Generating speech"
            icon = Icons.Default.VolumeUp
        }

        RadioState.ERROR -> {
            title = "Connection Error"
            subtitle = "Please reconnect"
            icon = Icons.Default.ErrorOutline
        }
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
                    ) {
                        pulse
                    } else {
                        0.35f
                    }

                drawCircle(
                    color = ITantraGreen.copy(
                        alpha = alpha * 0.12f
                    ),
                    radius = size.minDimension * 0.48f,
                    center = center,
                    style = Stroke(3f)
                )

                drawCircle(
                    color = ITantraGreen.copy(
                        alpha = alpha * 0.28f
                    ),
                    radius = size.minDimension * 0.37f,
                    center = center,
                    style = Stroke(3f)
                )

                drawCircle(
                    color = ITantraGreen.copy(
                        alpha = alpha * 0.5f
                    ),
                    radius = size.minDimension * 0.27f,
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
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }

        Text(
            text = title,
            color = ITantraGreen,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = subtitle,
            color = ITantraSecondaryText,
            fontSize = 13.sp
        )
    }
}