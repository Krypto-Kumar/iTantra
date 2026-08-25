package com.iTantra.app.domain

data class AudioData(
    val samples: ShortArray,
    val sampleRate: Int,
    val channelCount: Int
)