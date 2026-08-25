package com.iTantra.app.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

class AudioPlayer {

    fun play(samples: FloatArray, sampleRate: Int) {
        if (samples.isEmpty() || sampleRate <= 0) {
            return
        }

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_FLOAT
        )

        if (minBufferSize <= 0) {
            android.util.Log.e("AudioPlayer", "Unable to determine AudioTrack buffer size")
            return
        }

        val bufferSize = maxOf(
            minBufferSize,
            samples.size * Float.SIZE_BYTES
        )

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        try {
            audioTrack.play()

            val written = audioTrack.write(
                samples,
                0,
                samples.size,
                AudioTrack.WRITE_BLOCKING
            )

            android.util.Log.d(
                "AudioPlayer",
                "Played $written / ${samples.size} samples"
            )

            // Give AudioTrack time to drain the buffer.
            val durationMs =
                (samples.size * 1000L) / sampleRate

            if (durationMs > 0) {
                Thread.sleep(durationMs + 100)
            }

            audioTrack.stop()

        } catch (e: Exception) {
            android.util.Log.e("AudioPlayer", "Error during audio playback", e)
        } finally {
            try {
                audioTrack.release()
            } catch (_: Exception) {
            }
        }
    }
}