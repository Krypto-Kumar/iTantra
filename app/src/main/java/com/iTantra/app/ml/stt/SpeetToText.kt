package com.iTantra.app.ml.stt

import com.iTantra.app.domain.AudioData

interface SpeechToText {

    fun transcribe(audio: AudioData): String
}