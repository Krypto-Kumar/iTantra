package com.iTantra.app.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import com.iTantra.app.domain.AudioData

class AudioRecorder(
    private val context: Context
) {

    companion object {
        const val SAMPLE_RATE = 16_000
        const val CHANNEL_COUNT = 1

        private const val TAG = "AudioRecorder"
    }

    private val channelConfig =
        AudioFormat.CHANNEL_IN_MONO

    private val audioFormat =
        AudioFormat.ENCODING_PCM_16BIT

    @Volatile
    private var audioRecord: AudioRecord? = null

    @Volatile
    private var isRecording = false

    fun record(durationMs: Long): AudioData {

        // -----------------------------------------
        // Check microphone permission
        // -----------------------------------------

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException(
                "RECORD_AUDIO permission has not been granted"
            )
        }

        // -----------------------------------------
        // Prevent overlapping recordings
        // -----------------------------------------

        if (isRecording) {
            throw IllegalStateException(
                "Audio recording is already in progress"
            )
        }

        // -----------------------------------------
        // Get minimum buffer size
        // -----------------------------------------

        val minBufferSize =
            AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                channelConfig,
                audioFormat
            )

        require(minBufferSize > 0) {
            "Unable to determine audio buffer size"
        }

        // -----------------------------------------
        // Number of samples
        // -----------------------------------------

        val sampleCount =
            (SAMPLE_RATE * durationMs / 1000).toInt()

        val samples =
            ShortArray(sampleCount)

        // -----------------------------------------
        // Create AudioRecord
        // -----------------------------------------

        val audio = AudioRecord.Builder()
            .setAudioSource(
                MediaRecorder.AudioSource.MIC
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(channelConfig)
                    .setEncoding(audioFormat)
                    .build()
            )
            .setBufferSizeInBytes(
                minBufferSize
            )
            .build()

        // -----------------------------------------
        // Verify initialization
        // -----------------------------------------

        if (
            audio.state !=
            AudioRecord.STATE_INITIALIZED
        ) {

            Log.e(
                TAG,
                "AudioRecord failed to initialize. " +
                        "state=${audio.state}"
            )

            audio.release()

            throw IllegalStateException(
                "AudioRecord failed to initialize"
            )
        }

        audioRecord = audio
        isRecording = true

        try {

            // -------------------------------------
            // Start recording
            // -------------------------------------

            Log.d(
                TAG,
                "Starting recording for ${durationMs}ms"
            )

            audio.startRecording()

            if (
                audio.recordingState !=
                AudioRecord.RECORDSTATE_RECORDING
            ) {

                throw IllegalStateException(
                    "AudioRecord failed to start recording"
                )
            }

            // -------------------------------------
            // Read microphone samples
            // -------------------------------------

            var offset = 0

            while (
                offset < samples.size &&
                isRecording
            ) {

                val count =
                    audio.read(
                        samples,
                        offset,
                        samples.size - offset
                    )

                when {

                    count > 0 -> {
                        offset += count
                    }

                    count < 0 -> {
                        throw IllegalStateException(
                            "AudioRecord.read() failed: $count"
                        )
                    }
                }
            }

            Log.d(
                TAG,
                "Recording complete: " +
                        "$offset samples"
            )

            // -------------------------------------
            // Return audio
            // -------------------------------------

            return AudioData(
                samples = if (offset == samples.size) samples else samples.copyOf(offset),
                sampleRate = SAMPLE_RATE,
                channelCount = CHANNEL_COUNT
            )

        } finally {

            isRecording = false

            // -------------------------------------
            // Stop
            // -------------------------------------

            try {

                if (
                    audio.recordingState ==
                    AudioRecord.RECORDSTATE_RECORDING
                ) {
                    audio.stop()
                }

            } catch (e: Exception) {

                Log.w(
                    TAG,
                    "Error stopping AudioRecord",
                    e
                )
            }

            // -------------------------------------
            // Release
            // -------------------------------------

            try {

                audio.release()

            } catch (e: Exception) {

                Log.w(
                    TAG,
                    "Error releasing AudioRecord",
                    e
                )
            }

            audioRecord = null

            Log.d(
                TAG,
                "AudioRecord released"
            )
        }
    }

    fun stop() {

        isRecording = false

        audioRecord?.let { audio ->

            try {

                if (
                    audio.recordingState ==
                    AudioRecord.RECORDSTATE_RECORDING
                ) {
                    audio.stop()
                }

            } catch (e: Exception) {

                Log.w(
                    TAG,
                    "Error stopping recorder",
                    e
                )
            }
        }
    }
}