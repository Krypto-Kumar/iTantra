package com.iTantra.app.ml.stt

import android.content.Context
import com.iTantra.app.domain.AudioData
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OfflineDolphinModelConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig

class SherpaSttEngine(
    context: Context
) : SpeechToText {

    private val recognizer: OfflineRecognizer

    init {
        val modelConfig = OfflineModelConfig(
            dolphin = OfflineDolphinModelConfig(
                model = "models/dolphin/model.int8.onnx"
            ),
            tokens = "models/dolphin/tokens.txt",
            numThreads = 2,
            provider = "cpu"
        )

        val config = OfflineRecognizerConfig(
            featConfig = FeatureConfig(
                sampleRate = 16000,
                featureDim = 80
            ),
            modelConfig = modelConfig
        )

        recognizer = OfflineRecognizer(
            assetManager = context.assets,
            config = config
        )
    }

    fun transcribeTestWav(): String {
        // We'll connect this to a bundled WAV in the next step.
        return "TEST"
    }

    override fun transcribe(audio: AudioData): String {
        if (audio.samples.isEmpty()) return ""

        val stream = recognizer.createStream()

        val samples = FloatArray(audio.samples.size) { i ->
            audio.samples[i] / 32768.0f
        }

        stream.acceptWaveform(
            samples = samples,
            sampleRate = audio.sampleRate
        )

        recognizer.decode(stream)

        val result = recognizer.getResult(stream)

        stream.release()

        return result.text.trim()
    }

    fun release() {
        recognizer.release()
    }
}