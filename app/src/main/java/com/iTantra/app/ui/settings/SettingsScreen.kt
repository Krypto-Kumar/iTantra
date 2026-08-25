package com.iTantra.app.ui.settings

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


private val Background = Color(0xFFFAFBF9)
private val CardBackground = Color(0xFFFFFFFF)

private val PrimaryGreen = Color(0xFF1F7F7B)
private val LightGreen = Color(0xFFE6F3F1)
private val SuccessGreen = Color(0xFF43A66E)

private val PrimaryText = Color(0xFF263333)
private val SecondaryText = Color(0xFF818989)

private val BorderColor = Color(0xFFDDE2E0)
private val DividerColor = Color(0xFFE7EAE9)


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
fun SettingsScreen(
    connectedDeviceName: String,
    onBackClick: () -> Unit = {},
    onConnectedDeviceClick: () -> Unit = {},
    onMicrophonePermissionClick: () -> Unit = {},
    onBluetoothPermissionClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},

) {

    var speakingLanguage by remember {
        mutableStateOf("Hindi")
    }

    var listeningLanguage by remember {
        mutableStateOf("English")
    }

    var speakerVolume by remember {
        mutableFloatStateOf(0.65f)
    }

    var voiceOutputEnabled by remember {
        mutableStateOf(true)
    }

    Scaffold(
        containerColor = Background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {

            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onBackClick
                ) {

                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryText
                    )
                }

                Text(
                    text = "Settings",
                    color = PrimaryText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }


            Spacer(
                modifier = Modifier.height(8.dp)
            )


            // COMMUNICATION
            SectionTitle("COMMUNICATION")

            SettingsCard {

                LanguageDropdownRow(
                    title = "Speaking Language",
                    selectedLanguage = speakingLanguage,
                    onLanguageSelected = {
                        speakingLanguage = it
                    }
                )

                SettingsDivider()

                LanguageDropdownRow(
                    title = "Listening Language",
                    selectedLanguage = listeningLanguage,
                    onLanguageSelected = {
                        listeningLanguage = it
                    }
                )

                SettingsDivider()

                SettingsNavigationRow(
                    title = "Connected Device",
                    value = if (connectedDeviceName.isNotBlank()) {
                        connectedDeviceName
                    } else {
                        "Not connected"
                    },
                    onClick = onConnectedDeviceClick
                )

                SettingsDivider()

                SettingsNavigationRow(
                    title = "Swap Languages",
                    value = "⇄",
                    showArrow = false,
                    onClick = {

                        val oldSpeaking = speakingLanguage

                        speakingLanguage = listeningLanguage
                        listeningLanguage = oldSpeaking
                    }
                )
            }


            Spacer(
                modifier = Modifier.height(24.dp)
            )


            // AUDIO
            SectionTitle("AUDIO")

            SettingsCard {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Speaker Volume",
                        color = PrimaryText,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Slider(
                        value = speakerVolume,
                        onValueChange = {
                            speakerVolume = it
                        },
                        modifier = Modifier.weight(1.2f),
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryGreen,
                            activeTrackColor = PrimaryGreen,
                            inactiveTrackColor = LightGreen
                        )
                    )
                }

                SettingsDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 10.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Voice Output",
                        color = PrimaryText,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = voiceOutputEnabled,
                        onCheckedChange = {
                            voiceOutputEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = BorderColor
                        )
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(24.dp)
            )


            // PERMISSIONS
            SectionTitle("PERMISSIONS")

            SettingsCard {

                PermissionRow(
                    title = "Microphone",
                    status = "Allowed",
                    onClick = onMicrophonePermissionClick
                )

                SettingsDivider()

                PermissionRow(
                    title = "Bluetooth",
                    status = "Allowed",
                    onClick = onBluetoothPermissionClick
                )
            }


            Spacer(
                modifier = Modifier.height(24.dp)
            )


            // ABOUT
            SectionTitle("ABOUT")

            SettingsCard {

                SettingsNavigationRow(
                    title = "About iTANTRA",
                    value = "",
                    onClick = onAboutClick
                )

                SettingsDivider()


            }
            Spacer(
                modifier = Modifier.height(40.dp)
            )
        }
    }
}


@Composable
private fun LanguageDropdownRow(
    title: String,
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded = true
                }
                .padding(
                    horizontal = 16.dp,
                    vertical = 16.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = title,
                color = PrimaryText,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = selectedLanguage,
                color = PrimaryGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(
                modifier = Modifier.width(4.dp)
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select language",
                tint = SecondaryText
            )
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

            supportedLanguages.forEach { language ->

                DropdownMenuItem(
                    text = {

                        Text(
                            text = language,
                            color = if (
                                language == selectedLanguage
                            ) PrimaryGreen
                            else PrimaryText,
                            fontWeight = if (
                                language == selectedLanguage
                            ) FontWeight.SemiBold
                            else FontWeight.Normal
                        )
                    },

                    onClick = {

                        onLanguageSelected(language)

                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
private fun SectionTitle(
    title: String
) {

    Text(
        text = title,
        color = PrimaryGreen,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(
            start = 4.dp,
            bottom = 10.dp
        )
    )
}


@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = CardBackground,
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.dp,
                color = BorderColor,
                shape = RoundedCornerShape(18.dp)
            )
    ) {

        content()
    }
}


@Composable
private fun SettingsNavigationRow(
    title: String,
    value: String,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            color = PrimaryText,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        if (value.isNotEmpty()) {

            Text(
                text = value,
                color = if (
                    value == "⇄"
                ) PrimaryGreen
                else PrimaryText,
                fontSize = if (
                    value == "⇄"
                ) 20.sp
                else 14.sp,
                modifier = Modifier.padding(
                    end = 6.dp
                )
            )
        }

        if (showArrow) {

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = SecondaryText
            )
        }
    }
}


@Composable
private fun PermissionRow(
    title: String,
    status: String,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            color = PrimaryText,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = status,
            color = SuccessGreen,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(
                end = 6.dp
            )
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = SecondaryText
        )
    }
}


@Composable
private fun SettingsDivider() {

    HorizontalDivider(
        modifier = Modifier.padding(
            horizontal = 16.dp
        ),
        color = DividerColor,
        thickness = 1.dp
    )
}