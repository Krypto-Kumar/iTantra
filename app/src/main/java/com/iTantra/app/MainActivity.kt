package com.iTantra.app

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.iTantra.app.audio.AudioPlayer
import com.iTantra.app.audio.AudioRecorder
import com.iTantra.app.ml.stt.SherpaSttEngine
import com.iTantra.app.ml.tts.PiperTtsEngine
import com.iTantra.app.transport.bluetooth.BluetoothPermissionHelper
import com.iTantra.app.transport.connection.CommunicationService
import com.iTantra.app.transport.connection.ConnectionState
import com.iTantra.app.transport.protocol.Message
import com.iTantra.app.domain.Language
import com.iTantra.app.ui.about.AboutScreen
import com.iTantra.app.ui.about.HowItWorksScreen
import com.iTantra.app.ui.connect.ConnectDeviceScreen
import com.iTantra.app.ui.connect.ConnectingScreen
import com.iTantra.app.ui.onboarding.PermissionScreen
import com.iTantra.app.ui.radio.MainRadioScreen
import com.iTantra.app.ui.radio.RadioState
import com.iTantra.app.ui.settings.SettingsScreen
import com.iTantra.app.ui.splash.SplashScreen
import com.iTantra.app.ui.theme.ITantraTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID


private enum class AppScreen {
    SPLASH,
    PERMISSIONS,
    CONNECT,
    CONNECTING,
    RADIO,
    SETTINGS,
    ABOUT,
    HOW_IT_WORKS
}


class MainActivity : ComponentActivity() {

    // =====================================================
    // AUDIO / ML
    // =====================================================

    private val audioRecorder by lazy {
        AudioRecorder(applicationContext)
    }

    private val audioPlayer by lazy {
        AudioPlayer()
    }

    private val sttEngine by lazy {
        SherpaSttEngine(applicationContext)
    }

    private val ttsEngine by lazy {
        PiperTtsEngine(applicationContext)
    }

    private var recordingJob: Job? = null


    // =====================================================
    // BLUETOOTH
    // =====================================================

    private val communicationScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.Main
        )

    private val communicationService =
        CommunicationService(
            scope = communicationScope
        )


    // =====================================================
    // PERMISSION LAUNCHERS
    // =====================================================

    private val microphonePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            Log.d(
                "PERMISSION",
                "Microphone permission result: $granted"
            )

            if (!granted) {
                Log.w(
                    "PERMISSION",
                    "Microphone permission was denied"
                )
            }
        }


    private val bluetoothPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            Log.d(
                "PERMISSION",
                "Bluetooth permission result: $permissions"
            )
        }


    // =====================================================
    // REQUEST MICROPHONE PERMISSION
    // =====================================================

    private fun requestMicrophonePermission() {

        val permission =
            Manifest.permission.RECORD_AUDIO

        val granted =
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {

            Log.d(
                "PERMISSION",
                "Requesting microphone permission"
            )

            microphonePermissionLauncher.launch(
                permission
            )

        } else {

            Log.d(
                "PERMISSION",
                "Microphone permission already granted"
            )
        }
    }


    // =====================================================
    // REQUEST BLUETOOTH PERMISSIONS
    // =====================================================

    private fun requestBluetoothPermissions() {

        /*
         * Android 12+ introduced runtime Bluetooth
         * permissions.
         */

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.S
        ) {

            val requiredPermissions =
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                )

            val missingPermissions =
                requiredPermissions.filter { permission ->

                    ContextCompat.checkSelfPermission(
                        this,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                }

            if (missingPermissions.isNotEmpty()) {

                Log.d(
                    "PERMISSION",
                    "Requesting Bluetooth permissions: " +
                            missingPermissions
                )

                bluetoothPermissionLauncher.launch(
                    missingPermissions.toTypedArray()
                )

            } else {

                Log.d(
                    "PERMISSION",
                    "Bluetooth permissions already granted"
                )
            }

        } else {

            /*
             * On Android versions below 12, Bluetooth
             * permissions are normally handled as install-time
             * permissions through the manifest.
             */

            Log.d(
                "PERMISSION",
                "Android < 12: runtime Bluetooth permission not required"
            )
        }
    }


    // =====================================================
    // REQUEST ALL RUNTIME PERMISSIONS
    // =====================================================

    private fun requestAllPermissions() {

        requestMicrophonePermission()

        requestBluetoothPermissions()
    }


    // =====================================================
    // START BLUETOOTH SERVER
    // =====================================================

    private fun startBluetoothServer() {

        try {

            Log.d(
                "BLUETOOTH",
                "Starting RFCOMM server"
            )

            communicationService.startServer()

        } catch (e: SecurityException) {

            Log.e(
                "BLUETOOTH",
                "Bluetooth permission denied",
                e
            )

        } catch (e: Exception) {

            Log.e(
                "BLUETOOTH",
                "Failed to start Bluetooth server",
                e
            )
        }
    }


    // =====================================================
    // ON CREATE
    // =====================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        /*
         * IMPORTANT:
         *
         * We intentionally do NOT request permissions here.
         *
         * The user reaches PermissionScreen first and presses
         * Continue. That action starts the permission requests.
         */

        setContent {

            ITantraTheme {

                var screen by remember {
                    mutableStateOf(
                        AppScreen.SPLASH
                    )
                }

                var selectedDevice by remember {
                    mutableStateOf("")
                }

                var selectedBluetoothDevice by remember {
                    mutableStateOf<BluetoothDevice?>(null)
                }

                var radioState by remember {
                    mutableStateOf(
                        RadioState.READY
                    )
                }

                /*
                 * Current presentation build is Hindi-only.
                 */
                val speakingLanguage =
                    Language.HINDI

                val listeningLanguage =
                    Language.HINDI


                // =================================================
                // BLUETOOTH STATE OBSERVER
                // =================================================

                LaunchedEffect(Unit) {

                    communicationService
                        .connectionState
                        .collect { state ->

                            Log.d(
                                "BLUETOOTH",
                                "Connection state: $state"
                            )

                            when (state) {

                                ConnectionState.CONNECTED -> {

                                    Log.d(
                                        "BLUETOOTH",
                                        "CONNECTED"
                                    )

                                    radioState =
                                        RadioState.READY

                                    screen =
                                        AppScreen.RADIO
                                }

                                ConnectionState.ERROR -> {

                                    Log.e(
                                        "BLUETOOTH",
                                        "Connection ERROR"
                                    )

                                    radioState =
                                        RadioState.ERROR

                                    if (
                                        screen ==
                                        AppScreen.CONNECTING
                                    ) {

                                        screen =
                                            AppScreen.CONNECT
                                    }
                                }

                                ConnectionState.DISCONNECTED -> {

                                    if (
                                        screen ==
                                        AppScreen.CONNECTING
                                    ) {

                                        Log.d(
                                            "BLUETOOTH",
                                            "Connection failed"
                                        )

                                        radioState =
                                            RadioState.ERROR

                                        screen =
                                            AppScreen.CONNECT
                                    }
                                }

                                else -> {
                                    // WAITING / CONNECTING /
                                    // UNAVAILABLE
                                }
                            }
                        }
                }


                // =================================================
                // INCOMING MESSAGE → TTS
                // =================================================

                LaunchedEffect(Unit) {

                    communicationService
                        .incomingMessages
                        .collect { message ->

                            Log.d(
                                "BLUETOOTH",
                                "Received message: " +
                                        message.text
                            )

                            if (message.text.isBlank()) {
                                Log.w(
                                    "BLUETOOTH",
                                    "Received empty message, skipping TTS"
                                )
                                return@collect
                            }

                            communicationScope.launch(
                                Dispatchers.Default
                            ) {

                                try {

                                    Handler(
                                        Looper.getMainLooper()
                                    ).post {

                                        radioState =
                                            RadioState.RECEIVING
                                    }


                                    /*
                                     * Current presentation build:
                                     * Hindi TTS.
                                     */

                                    if (
                                        message.language ==
                                        Language.HINDI.code
                                    ) {

                                        Handler(
                                            Looper.getMainLooper()
                                        ).post {

                                            radioState =
                                                RadioState.PLAYING
                                        }

                                        Log.d(
                                            "REMOTE_TTS",
                                            "Generating Hindi TTS: " +
                                                    message.text
                                        )

                                        val ttsAudio =
                                            ttsEngine.synthesize(
                                                message.text
                                            )

                                        if (ttsAudio.samples.isNotEmpty()) {

                                            audioPlayer.play(
                                                samples =
                                                    ttsAudio.samples,

                                                sampleRate =
                                                    ttsAudio.sampleRate
                                            )

                                            Log.d(
                                                "REMOTE_TTS",
                                                "Playback completed"
                                            )
                                        } else {

                                            Log.w(
                                                "REMOTE_TTS",
                                                "TTS generated 0 samples"
                                            )
                                        }

                                        Handler(
                                            Looper.getMainLooper()
                                        ).post {

                                            radioState =
                                                RadioState.READY
                                        }

                                    } else {

                                        Log.w(
                                            "REMOTE_TTS",
                                            "Unsupported language: " +
                                                    message.language
                                        )

                                        Handler(
                                            Looper.getMainLooper()
                                        ).post {

                                            radioState =
                                                RadioState.ERROR
                                        }
                                    }

                                } catch (e: Exception) {

                                    Log.e(
                                        "REMOTE_TTS",
                                        "TTS processing failed",
                                        e
                                    )

                                    Handler(
                                        Looper.getMainLooper()
                                    ).post {

                                        radioState =
                                            RadioState.ERROR
                                    }
                                }
                            }
                        }
                }


                // =================================================
                // SCREEN NAVIGATION
                // =================================================

                when (screen) {

                    // =================================================
                    // SPLASH
                    // =================================================

                    AppScreen.SPLASH -> {

                        SplashScreen()

                        LaunchedEffect(Unit) {

                            delay(1400)

                            screen =
                                AppScreen.PERMISSIONS
                        }
                    }


                    // =================================================
                    // PERMISSIONS
                    // =================================================

                    AppScreen.PERMISSIONS -> {

                        PermissionScreen(

                            onContinue = {

                                Log.d(
                                    "PERMISSION",
                                    "Permission screen Continue pressed"
                                )

                                requestAllPermissions()

                                screen =
                                    AppScreen.CONNECT
                            }
                        )
                    }


                    // =================================================
                    // CONNECT
                    // =================================================

                    AppScreen.CONNECT -> {

                        /*
                         * Start the server when we reach the
                         * Connect screen.
                         */

                        LaunchedEffect(Unit) {

                            startBluetoothServer()
                        }


                        ConnectDeviceScreen(

                            devices =
                                getPairedDeviceNames(),

                            onDeviceClick = { deviceName ->

                                Log.d(
                                    "BLUETOOTH",
                                    "Selected device: $deviceName"
                                )

                                selectedDevice =
                                    deviceName

                                selectedBluetoothDevice =
                                    findPairedDevice(
                                        deviceName
                                    )

                                if (
                                    selectedBluetoothDevice ==
                                    null
                                ) {

                                    Log.e(
                                        "BLUETOOTH",
                                        "Could not find selected device"
                                    )

                                    radioState =
                                        RadioState.ERROR

                                    return@ConnectDeviceScreen
                                }

                                screen =
                                    AppScreen.CONNECTING
                            },

                            onScanAgain = {

                                Log.d(
                                    "BLUETOOTH",
                                    "Scan again requested"
                                )
                            }
                        )
                    }


                    // =================================================
                    // CONNECTING
                    // =================================================

                    AppScreen.CONNECTING -> {

                        ConnectingScreen(
                            deviceName =
                                selectedDevice
                        )


                        LaunchedEffect(
                            selectedBluetoothDevice
                        ) {

                            val device =
                                selectedBluetoothDevice

                            if (device == null) {

                                Log.e(
                                    "BLUETOOTH",
                                    "No BluetoothDevice selected"
                                )

                                radioState =
                                    RadioState.ERROR

                                screen =
                                    AppScreen.CONNECT

                                return@LaunchedEffect
                            }

                            try {

                                Log.d(
                                    "BLUETOOTH",
                                    "Connecting to: " +
                                            selectedDevice
                                )

                                communicationService
                                    .connectToDevice(
                                        device
                                    )

                            } catch (e: SecurityException) {

                                Log.e(
                                    "BLUETOOTH",
                                    "Bluetooth permission error",
                                    e
                                )

                                radioState =
                                    RadioState.ERROR

                                screen =
                                    AppScreen.CONNECT

                            } catch (e: Exception) {

                                Log.e(
                                    "BLUETOOTH",
                                    "Connection failed",
                                    e
                                )

                                radioState =
                                    RadioState.ERROR

                                screen =
                                    AppScreen.CONNECT
                            }
                        }
                    }


                    // =================================================
                    // RADIO
                    // =================================================

                    AppScreen.RADIO -> {

                        MainRadioScreen(

                            state =
                                radioState,

                            connectedDevice =
                                selectedDevice,

                            speakingLanguage =
                                speakingLanguage.displayName,

                            listeningLanguage =
                                listeningLanguage.displayName,

                            onSettingsClick = {

                                screen =
                                    AppScreen.SETTINGS
                            },

                            // -----------------------------------------
                            // START TALKING
                            // -----------------------------------------

                            onStartTalking = {

                                if (
                                    radioState ==
                                    RadioState.READY
                                ) {

                                    Log.d(
                                        "RADIO",
                                        "Talk button pressed - starting recording"
                                    )

                                    radioState =
                                        RadioState.LISTENING

                                    recordingJob =
                                        communicationScope.launch(
                                            Dispatchers.Default
                                        ) {

                                            try {

                                                // =============================
                                                // RECORD (Captures immediately)
                                                // =============================

                                                Log.d(
                                                    "AUDIO",
                                                    "Recording audio for up to 3 seconds"
                                                )

                                                val audio =
                                                    audioRecorder.record(
                                                        3000
                                                    )

                                                Log.d(
                                                    "AUDIO",
                                                    "Recorded " +
                                                            "${audio.samples.size} samples"
                                                )


                                                // =============================
                                                // PROCESSING
                                                // =============================

                                                Handler(
                                                    Looper.getMainLooper()
                                                ).post {

                                                    radioState =
                                                        RadioState.PROCESSING
                                                }


                                                // =============================
                                                // STT
                                                // =============================

                                                Log.d(
                                                    "STT",
                                                    "Starting Dolphin STT"
                                                )

                                                val text =
                                                    sttEngine.transcribe(
                                                        audio
                                                    )

                                                Log.d(
                                                    "STT_RESULT",
                                                    "Language=" +
                                                            speakingLanguage.code +
                                                            ", text=[$text]"
                                                )


                                                if (
                                                    text.isBlank()
                                                ) {

                                                    Log.d(
                                                        "STT",
                                                        "Recognized text is blank, resetting to READY"
                                                    )

                                                    Handler(
                                                        Looper.getMainLooper()
                                                    ).post {

                                                        radioState =
                                                            RadioState.READY
                                                    }

                                                    return@launch
                                                }


                                                // =============================
                                                // SENDING
                                                // =============================

                                                Handler(
                                                    Looper.getMainLooper()
                                                ).post {

                                                    radioState =
                                                        RadioState.SENDING
                                                }


                                                val message =
                                                    Message(

                                                        id =
                                                            "msg-" +
                                                                    UUID
                                                                        .randomUUID()
                                                                        .toString(),

                                                        type =
                                                            "NORMAL",

                                                        language =
                                                            speakingLanguage.code,

                                                        timestamp =
                                                            System
                                                                .currentTimeMillis(),

                                                        text =
                                                            text
                                                    )


                                                Log.d(
                                                    "BLUETOOTH",
                                                    "Sending message: $text"
                                                )


                                                val success =
                                                    communicationService
                                                        .sendMessage(
                                                            message
                                                        )


                                                if (success) {

                                                    Log.d(
                                                        "BLUETOOTH",
                                                        "Message acknowledged"
                                                    )

                                                    Handler(
                                                        Looper.getMainLooper()
                                                    ).post {

                                                        radioState =
                                                            RadioState.READY
                                                    }

                                                } else {

                                                    Log.e(
                                                        "BLUETOOTH",
                                                        "Message failed"
                                                    )

                                                    Handler(
                                                        Looper.getMainLooper()
                                                    ).post {

                                                        radioState =
                                                            RadioState.ERROR
                                                    }
                                                }

                                            } catch (
                                                e: SecurityException
                                            ) {

                                                Log.e(
                                                    "RADIO",
                                                    "Microphone permission error",
                                                    e
                                                )

                                                Handler(
                                                    Looper.getMainLooper()
                                                ).post {

                                                    radioState =
                                                        RadioState.ERROR
                                                }

                                            } catch (
                                                e: Exception
                                            ) {

                                                Log.e(
                                                    "RADIO",
                                                    "Speech pipeline failed",
                                                    e
                                                )

                                                Handler(
                                                    Looper.getMainLooper()
                                                ).post {

                                                    radioState =
                                                        RadioState.ERROR
                                                }
                                            }
                                        }
                                }
                            },

                            // -----------------------------------------
                            // STOP TALKING
                            // -----------------------------------------

                            onStopTalking = {

                                if (
                                    radioState ==
                                    RadioState.LISTENING
                                ) {

                                    Log.d(
                                        "RADIO",
                                        "Talk button released early - stopping recording"
                                    )

                                    try {
                                        audioRecorder.stop()
                                    } catch (e: Exception) {
                                        Log.w(
                                            "RADIO",
                                            "Error stopping audioRecorder",
                                            e
                                        )
                                    }
                                }
                            }
                        )
                    }


                    // =================================================
                    // SETTINGS
                    // =================================================

                    AppScreen.SETTINGS -> {

                        SettingsScreen(

                            connectedDeviceName =
                                selectedDevice,

                            onBackClick = {

                                screen =
                                    AppScreen.RADIO
                            },

                            onAboutClick = {

                                screen =
                                    AppScreen.ABOUT
                            },

                            onHowItWorksClick = {

                                screen =
                                    AppScreen.HOW_IT_WORKS
                            }
                        )
                    }


                    // =================================================
                    // ABOUT
                    // =================================================

                    AppScreen.ABOUT -> {

                        AboutScreen(

                            onBack = {

                                screen =
                                    AppScreen.SETTINGS
                            },

                            onHowItWorks = {

                                screen =
                                    AppScreen.HOW_IT_WORKS
                            }
                        )
                    }


                    // =================================================
                    // HOW IT WORKS
                    // =================================================

                    AppScreen.HOW_IT_WORKS -> {

                        HowItWorksScreen(

                            onBack = {

                                screen =
                                    AppScreen.SETTINGS
                            }
                        )
                    }
                }


                // =================================================
                // BACK HANDLER
                // =================================================

                BackHandler(
                    enabled =
                        screen !=
                                AppScreen.SPLASH
                ) {

                    screen =
                        when (screen) {

                            AppScreen.SETTINGS ->
                                AppScreen.RADIO

                            AppScreen.ABOUT ->
                                AppScreen.SETTINGS

                            AppScreen.HOW_IT_WORKS ->
                                AppScreen.SETTINGS

                            AppScreen.CONNECTING ->
                                AppScreen.CONNECT

                            AppScreen.RADIO -> {

                                communicationService
                                    .disconnect()

                                AppScreen.CONNECT
                            }

                            else ->
                                AppScreen.CONNECT
                        }
                }
            }
        }
    }


    // =====================================================
    // PAIRED DEVICE NAMES
    // =====================================================

    private fun getPairedDeviceNames(): List<String> {

        return try {

            BluetoothPermissionHelper
                .getPairedDevices()
                .mapNotNull { device ->

                    try {

                        device.name

                    } catch (
                        _: SecurityException
                    ) {

                        null
                    }
                }

        } catch (e: Exception) {

            Log.e(
                "BLUETOOTH",
                "Failed to get paired devices",
                e
            )

            emptyList()
        }
    }


    // =====================================================
    // FIND PAIRED DEVICE
    // =====================================================

    private fun findPairedDevice(
        deviceName: String
    ): BluetoothDevice? {

        return try {

            BluetoothPermissionHelper
                .getPairedDevices()
                .firstOrNull { device ->

                    try {

                        device.name == deviceName

                    } catch (
                        _: SecurityException
                    ) {

                        false
                    }
                }

        } catch (e: Exception) {

            Log.e(
                "BLUETOOTH",
                "Failed to find Bluetooth device",
                e
            )

            null
        }
    }


    // =====================================================
    // CLEANUP
    // =====================================================

    override fun onDestroy() {

        Log.d(
            "APP_LIFECYCLE",
            "MainActivity destroyed"
        )

        try {
            recordingJob?.cancel()
        } catch (_: Exception) {
        }

        try {
            audioRecorder.stop()
        } catch (_: Exception) {
        }

        try {
            sttEngine.release()
        } catch (_: Exception) {
        }

        try {
            ttsEngine.release()
        } catch (_: Exception) {
        }

        try {
            communicationService.disconnect()
        } catch (_: Exception) {
        }

        communicationScope.cancel()

        super.onDestroy()
    }
}