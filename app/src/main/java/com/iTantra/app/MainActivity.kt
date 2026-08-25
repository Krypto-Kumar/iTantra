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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat

import com.iTantra.app.audio.AudioPlayer
import com.iTantra.app.audio.AudioRecorder
import com.iTantra.app.domain.Language

import com.iTantra.app.ml.stt.SherpaSttEngine
import com.iTantra.app.ml.tts.PiperTtsEngine

import com.iTantra.app.room.RoomManager

import com.iTantra.app.security.EncryptionManager

import com.iTantra.app.transport.bluetooth.BluetoothPermissionHelper
import com.iTantra.app.transport.connection.CommunicationService
import com.iTantra.app.transport.connection.ConnectionState
import com.iTantra.app.transport.protocol.Message

import com.iTantra.app.ui.about.AboutScreen
import com.iTantra.app.ui.connect.ConnectDeviceScreen
import com.iTantra.app.ui.connect.ConnectingScreen
import com.iTantra.app.ui.onboarding.PermissionScreen
import com.iTantra.app.ui.radio.MainRadioScreen
import com.iTantra.app.ui.radio.RadioState
import com.iTantra.app.ui.room.CreateRoomScreen
import com.iTantra.app.ui.room.JoinRoomScreen
import com.iTantra.app.ui.room.RoomScreen
import com.iTantra.app.ui.room.RoomSessionScreen
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


// =============================================================
// APP SCREENS
// =============================================================

private enum class AppScreen {

    SPLASH,

    PERMISSIONS,

    CONNECT,

    CONNECTING,

    RADIO,

    SETTINGS,

    ABOUT,

    ROOM,

    CREATE_ROOM,

    JOIN_ROOM,

    ROOM_SESSION
}


// =============================================================
// MAIN ACTIVITY
// =============================================================

class MainActivity : ComponentActivity() {


    // =========================================================
    // AUDIO / ML
    // =========================================================

    private val audioRecorder by lazy {

        AudioRecorder(
            applicationContext
        )
    }


    private val audioPlayer by lazy {

        AudioPlayer()
    }


    private val sttEngine by lazy {

        SherpaSttEngine(
            applicationContext
        )
    }


    private val ttsEngine by lazy {

        PiperTtsEngine(
            applicationContext
        )
    }


    private var recordingJob: Job? =
        null


    // =========================================================
    // COMMUNICATION
    // =========================================================

    private val communicationScope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.Main
        )


    private val communicationService =
        CommunicationService(
            scope =
                communicationScope
        )


    // =========================================================
    // ENCRYPTION
    // =========================================================

    /*
     * One EncryptionManager for this application instance.
     *
     * It owns:
     * - P2P key exchange
     * - Room key management
     * - message encryption/decryption
     */
    private val encryptionManager by lazy {

        EncryptionManager()
    }


    // =========================================================
    // ROOM MANAGER
    // =========================================================

    /*
     * RoomManager uses the same EncryptionManager.
     *
     * Therefore creating a Room automatically creates
     * its Room encryption key.
     */
    private val roomManager by lazy {

        RoomManager(
            encryptionManager
        )
    }


    // =========================================================
    // PERMISSIONS
    // =========================================================

    private val microphonePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            Log.d(
                "PERMISSION",
                "Microphone permission: $granted"
            )
        }


    private val bluetoothPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            Log.d(
                "PERMISSION",
                "Bluetooth permissions: $permissions"
            )
        }


    // =========================================================
    // MICROPHONE PERMISSION
    // =========================================================

    private fun requestMicrophonePermission() {

        val permission =
            Manifest.permission.RECORD_AUDIO


        val granted =
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) ==
                    PackageManager.PERMISSION_GRANTED


        if (!granted) {

            microphonePermissionLauncher.launch(
                permission
            )
        }
    }


    // =========================================================
    // BLUETOOTH PERMISSION
    // =========================================================

    private fun requestBluetoothPermissions() {

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.S
        ) {

            val permissions =
                arrayOf(

                    Manifest.permission.BLUETOOTH_SCAN,

                    Manifest.permission.BLUETOOTH_CONNECT,

                    Manifest.permission.BLUETOOTH_ADVERTISE
                )


            val missing =
                permissions.filter {

                    ContextCompat.checkSelfPermission(
                        this,
                        it
                    ) !=
                            PackageManager.PERMISSION_GRANTED
                }


            if (missing.isNotEmpty()) {

                bluetoothPermissionLauncher.launch(
                    missing.toTypedArray()
                )
            }
        }
    }


    // =========================================================
    // ALL PERMISSIONS
    // =========================================================

    private fun requestAllPermissions() {

        requestMicrophonePermission()

        requestBluetoothPermissions()
    }


    // =========================================================
    // BLUETOOTH SERVER
    // =========================================================

    private fun startBluetoothServer() {

        try {

            communicationService
                .startServer()

        } catch (
            e: SecurityException
        ) {

            Log.e(
                "BLUETOOTH",
                "Bluetooth permission denied",
                e
            )

        } catch (
            e: Exception
        ) {

            Log.e(
                "BLUETOOTH",
                "Failed to start Bluetooth server",
                e
            )
        }
    }


    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )


        setContent {

            ITantraTheme {


                // =================================================
                // NAVIGATION STATE
                // =================================================

                var screen by remember {

                    mutableStateOf(
                        AppScreen.SPLASH
                    )
                }


                // =================================================
                // DEVICE
                // =================================================

                var selectedDevice by remember {

                    mutableStateOf("")
                }


                var selectedBluetoothDevice by remember {

                    mutableStateOf<BluetoothDevice?>(
                        null
                    )
                }


                // =================================================
                // RADIO STATE
                // =================================================

                var radioState by remember {

                    mutableStateOf(
                        RadioState.READY
                    )
                }


                // =================================================
                // CURRENT LANGUAGE
                //
                // Hindi-only for current demo.
                // Language selection can be connected later.
                // =================================================

                val speakingLanguage =
                    Language.HINDI


                val listeningLanguage =
                    Language.HINDI


                // =================================================
                // ROOM STATE
                // =================================================

                var activeRoomId by remember {

                    mutableStateOf("")
                }


                var activeRoomName by remember {

                    mutableStateOf("")
                }


                var roomParticipantCount by remember {

                    mutableIntStateOf(0)
                }


                // =================================================
                // ROOM CREATION ERROR
                // =================================================

                var roomCreationError by remember {

                    mutableStateOf<String?>(null)
                }


                // =================================================
                // BLUETOOTH STATE
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

                                    radioState =
                                        RadioState.READY

                                    screen =
                                        AppScreen.RADIO
                                }


                                ConnectionState.ERROR -> {

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

                                        radioState =
                                            RadioState.ERROR

                                        screen =
                                            AppScreen.CONNECT
                                    }
                                }


                                else -> {
                                    // CONNECTING / WAITING /
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


                            if (
                                message.text.isBlank()
                            ) {

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


                                        val audio =
                                            ttsEngine
                                                .synthesize(
                                                    message.text
                                                )


                                        if (
                                            audio.samples.isNotEmpty()
                                        ) {

                                            audioPlayer.play(

                                                samples =
                                                    audio.samples,

                                                sampleRate =
                                                    audio.sampleRate
                                            )
                                        }


                                        Handler(
                                            Looper.getMainLooper()
                                        ).post {

                                            radioState =
                                                RadioState.READY
                                        }

                                    } else {

                                        Handler(
                                            Looper.getMainLooper()
                                        ).post {

                                            radioState =
                                                RadioState.ERROR
                                        }
                                    }

                                } catch (
                                    e: Exception
                                ) {

                                    Log.e(
                                        "TTS",
                                        "TTS failed",
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
                // SCREEN ROUTER
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

                        LaunchedEffect(Unit) {

                            startBluetoothServer()
                        }


                        ConnectDeviceScreen(

                            devices =
                                getPairedDeviceNames(),

                            onDeviceClick = {

                                    deviceName ->


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
                                    "Scan requested"
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


                            if (
                                device == null
                            ) {

                                screen =
                                    AppScreen.CONNECT

                                return@LaunchedEffect
                            }


                            try {

                                communicationService
                                    .connectToDevice(
                                        device
                                    )

                            } catch (
                                e: Exception
                            ) {

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

                            connectedDeviceCount =
                                if (
                                    selectedDevice.isBlank()
                                ) {
                                    0
                                } else {
                                    1
                                },

                            speakingLanguage =
                                speakingLanguage.displayName,

                            listeningLanguage =
                                listeningLanguage.displayName,


                            onSpeakingLanguageChange = {

                                /*
                                 * Language model switching will be
                                 * implemented later.
                                 */
                            },


                            onListeningLanguageChange = {

                                /*
                                 * Language model switching will be
                                 * implemented later.
                                 */
                            },


                            onRoomClick = {

                                screen =
                                    AppScreen.ROOM
                            },


                            onHistoryClick = {

                                /*
                                 * History integration is our next task.
                                 */
                            },


                            onSettingsClick = {

                                screen =
                                    AppScreen.SETTINGS
                            },


                            onAboutClick = {

                                screen =
                                    AppScreen.ABOUT
                            },


                            onStartTalking = {

                                startRecording(
                                    type = "NORMAL",
                                    targetRoomId = null,

                                    onFinished = {
                                        radioState =
                                            RadioState.READY
                                    },

                                    onError = {
                                        radioState =
                                            RadioState.ERROR
                                    }
                                )
                            },


                            onStopTalking = {

                                stopRecording()
                            }
                        )
                    }


                    // =================================================
                    // ROOM HOME
                    // =================================================

                    AppScreen.ROOM -> {

                        RoomScreen(

                            onBackClick = {

                                screen =
                                    AppScreen.RADIO
                            },


                            onCreateRoomClick = {

                                roomCreationError =
                                    null

                                screen =
                                    AppScreen.CREATE_ROOM
                            },


                            onJoinRoomClick = {

                                screen =
                                    AppScreen.JOIN_ROOM
                            }
                        )
                    }


                    // =================================================
                    // CREATE ROOM
                    // =================================================

                    AppScreen.CREATE_ROOM -> {

                        CreateRoomScreen(

                            onBackClick = {

                                roomCreationError =
                                    null

                                screen =
                                    AppScreen.ROOM
                            },


                            generatedRoomId =
                                if (
                                    activeRoomId.isBlank()
                                ) {
                                    null
                                } else {
                                    activeRoomId
                                },


                            errorMessage =
                                roomCreationError,


                            // -----------------------------------------
                            // BACKEND CREATION
                            // -----------------------------------------

                            onCreateRoom = {

                                    roomName ->


                                roomCreationError =
                                    null


                                try {

                                    /*
                                     * THIS is where the backend creates
                                     * the Room ID.
                                     *
                                     * The UI does not generate it.
                                     */

                                    val result =
                                        roomManager.createRoom(

                                            roomName =
                                                roomName,

                                            hostId =
                                                encryptionManager
                                                    .localNodeId
                                        )


                                    /*
                                     * Backend-generated Room ID.
                                     */

                                    activeRoomId =
                                        result.room.roomId


                                    activeRoomName =
                                        result.room.roomName


                                    roomParticipantCount =
                                        result.room.members.size


                                    Log.d(
                                        "ROOM",
                                        "Room created: " +
                                                activeRoomId
                                    )


                                } catch (
                                    e: Exception
                                ) {

                                    Log.e(
                                        "ROOM",
                                        "Failed to create room",
                                        e
                                    )


                                    roomCreationError =
                                        e.message
                                            ?: "Failed to create room"
                                }
                            },


                            // -----------------------------------------
                            // CONTINUE
                            // -----------------------------------------

                            onContinueToRoom = {

                                if (
                                    activeRoomId.isNotBlank()
                                ) {

                                    screen =
                                        AppScreen.ROOM_SESSION
                                }
                            }
                        )
                    }


                    // =================================================
                    // JOIN ROOM
                    // =================================================

                    AppScreen.JOIN_ROOM -> {

                        JoinRoomScreen(

                            onBackClick = {

                                screen =
                                    AppScreen.ROOM
                            },


                            onJoinRoom = {

                                    roomId ->

                                /*
                                 * The actual remote Room join/key
                                 * exchange will be connected through
                                 * RoomProtocol next.
                                 *
                                 * For now preserve the entered ID.
                                 */

                                activeRoomId =
                                    roomId

                                activeRoomName =
                                    ""

                                roomParticipantCount =
                                    1

                                screen =
                                    AppScreen.ROOM_SESSION
                            }
                        )
                    }


                    // =================================================
                    // ROOM SESSION
                    // =================================================

                    AppScreen.ROOM_SESSION -> {

                        RoomSessionScreen(

                            roomId =
                                activeRoomId,

                            roomName =
                                activeRoomName,

                            participantCount =
                                roomParticipantCount,

                            state =
                                radioState,

                            speakingLanguage =
                                speakingLanguage.displayName,

                            listeningLanguage =
                                listeningLanguage.displayName,


                            onSpeakingLanguageChange = {

                                /*
                                 * WIP.
                                 */
                            },


                            onListeningLanguageChange = {

                                /*
                                 * WIP.
                                 */
                            },


                            onStartTalking = {

                                if (
                                    activeRoomId.isNotBlank()
                                ) {

                                    startRecording(

                                        type = "ROOM",

                                        targetRoomId =
                                            activeRoomId,

                                        onFinished = {

                                            radioState =
                                                RadioState.READY
                                        },

                                        onError = {

                                            radioState =
                                                RadioState.ERROR
                                        }
                                    )
                                }
                            },


                            onStopTalking = {

                                stopRecording()
                            },


                            onHistoryClick = {

                                /*
                                 * History integration is next.
                                 */
                            },


                            onSettingsClick = {

                                screen =
                                    AppScreen.SETTINGS
                            },


                            onAboutClick = {

                                screen =
                                    AppScreen.ABOUT
                            },


                            onLeaveRoom = {

                                try {

                                    roomManager.leaveRoom(

                                        roomId =
                                            activeRoomId,

                                        memberId =
                                            encryptionManager
                                                .localNodeId
                                    )

                                } catch (
                                    e: Exception
                                ) {

                                    Log.e(
                                        "ROOM",
                                        "Failed to leave room",
                                        e
                                    )
                                }


                                activeRoomId =
                                    ""

                                activeRoomName =
                                    ""

                                roomParticipantCount =
                                    0


                                radioState =
                                    RadioState.READY


                                screen =
                                    AppScreen.ROOM
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


                            onConnectedDeviceClick = {

                                screen =
                                    AppScreen.CONNECT
                            },


                            onAboutClick = {

                                screen =
                                    AppScreen.ABOUT
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
                            }
                        )
                    }
                }


                // =================================================
                // BACK BUTTON
                // =================================================

                BackHandler(

                    enabled =
                        screen !=
                                AppScreen.SPLASH

                ) {

                    screen =

                        when (screen) {

                            AppScreen.CREATE_ROOM -> {

                                roomCreationError =
                                    null

                                AppScreen.ROOM
                            }


                            AppScreen.JOIN_ROOM -> {

                                AppScreen.ROOM
                            }


                            AppScreen.ROOM_SESSION -> {

                                activeRoomId =
                                    ""

                                activeRoomName =
                                    ""

                                roomParticipantCount =
                                    0

                                AppScreen.ROOM
                            }


                            AppScreen.ROOM -> {

                                AppScreen.RADIO
                            }


                            AppScreen.SETTINGS -> {

                                AppScreen.RADIO
                            }


                            AppScreen.ABOUT -> {

                                AppScreen.SETTINGS
                            }


                            AppScreen.CONNECTING -> {

                                AppScreen.CONNECT
                            }


                            AppScreen.RADIO -> {

                                communicationService
                                    .disconnect()

                                AppScreen.CONNECT
                            }


                            AppScreen.CONNECT -> {

                                AppScreen.PERMISSIONS
                            }


                            AppScreen.PERMISSIONS -> {

                                AppScreen.PERMISSIONS
                            }


                            AppScreen.SPLASH -> {

                                AppScreen.SPLASH
                            }
                        }
                }
            }
        }
    }


    // =========================================================
    // RECORDING PIPELINE
    // =========================================================

    private fun startRecording(

        type: String,

        targetRoomId: String?,

        onFinished: () -> Unit,

        onError: () -> Unit
    ) {

        if (
            recordingJob?.isActive == true
        ) {

            return
        }


        recordingJob =
            communicationScope.launch(
                Dispatchers.Default
            ) {

                try {

                    Handler(
                        Looper.getMainLooper()
                    ).post {

                        // UI observes this.
                        // Actual state transitions occur below.
                    }


                    // =================================================
                    // RECORD
                    // =================================================

                    val audio =
                        audioRecorder.record(
                            3000
                        )


                    Handler(
                        Looper.getMainLooper()
                    ).post {

                        // Processing state.
                        // This is visible to Compose.
                    }


                    // =================================================
                    // STT
                    // =================================================

                    val text =
                        sttEngine.transcribe(
                            audio
                        )


                    if (
                        text.isBlank()
                    ) {

                        Handler(
                            Looper.getMainLooper()
                        ).post {

                            onFinished()
                        }

                        return@launch
                    }


                    // =================================================
                    // MESSAGE
                    // =================================================

                    val message =
                        Message(

                            id =
                                "msg-" +
                                        UUID
                                            .randomUUID()
                                            .toString(),

                            type =
                                type,

                            language =
                                Language.HINDI.code,

                            timestamp =
                                System.currentTimeMillis(),

                            text =
                                text
                        )


                    // =================================================
                    // SEND
                    // =================================================

                    if (
                        type == "NORMAL"
                    ) {

                        communicationService
                            .sendMessage(
                                message
                            )

                    } else {

                        /*
                         * Room broadcast is intentionally not
                         * connected to CommunicationService yet.
                         *
                         * The next backend step is:
                         *
                         * RoomProtocol
                         *     ↓
                         * RoomManager
                         *     ↓
                         * EncryptionManager
                         *     ↓
                         * CommunicationService
                         */

                        Log.d(
                            "ROOM",
                            "Prepared Room message: ${message.text}"
                        )
                    }


                    Handler(
                        Looper.getMainLooper()
                    ).post {

                        onFinished()
                    }


                } catch (
                    e: Exception
                ) {

                    Log.e(
                        "RADIO",
                        "Recording pipeline failed",
                        e
                    )


                    Handler(
                        Looper.getMainLooper()
                    ).post {

                        onError()
                    }
                }
            }
    }


    // =========================================================
    // STOP RECORDING
    // =========================================================

    private fun stopRecording() {

        try {

            audioRecorder.stop()

        } catch (
            e: Exception
        ) {

            Log.w(
                "AUDIO",
                "Failed to stop recording",
                e
            )
        }
    }


    // =========================================================
    // PAIRED DEVICE NAMES
    // =========================================================

    private fun getPairedDeviceNames():
            List<String> {

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

        } catch (
            e: Exception
        ) {

            Log.e(
                "BLUETOOTH",
                "Failed to get paired devices",
                e
            )

            emptyList()
        }
    }


    // =========================================================
    // FIND DEVICE
    // =========================================================

    private fun findPairedDevice(
        deviceName: String
    ): BluetoothDevice? {

        return try {

            BluetoothPermissionHelper
                .getPairedDevices()
                .firstOrNull { device ->

                    try {

                        device.name ==
                                deviceName

                    } catch (
                        _: SecurityException
                    ) {

                        false
                    }
                }

        } catch (
            e: Exception
        ) {

            Log.e(
                "BLUETOOTH",
                "Failed to find device",
                e
            )

            null
        }
    }


    // =========================================================
    // CLEANUP
    // =========================================================

    override fun onDestroy() {

        recordingJob?.cancel()


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

            communicationService
                .disconnect()

        } catch (_: Exception) {
        }


        /*
         * Clear in-memory encryption keys when the
         * application process is being destroyed.
         */
        try {

            encryptionManager
                .clearAllKeys()

        } catch (_: Exception) {
        }


        communicationScope.cancel()


        super.onDestroy()
    }
}