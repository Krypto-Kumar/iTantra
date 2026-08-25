package com.iTantra.app.ml.tts

interface TextToSpeech {
    fun synthesize(text: String): FloatArray
    fun release()
}