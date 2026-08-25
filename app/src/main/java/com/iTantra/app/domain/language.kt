package com.iTantra.app.domain

enum class Language(
    val displayName: String,
    val code: String
) {
    HINDI("Hindi", "hi"),
    GUJARATI("Gujarati", "gu"),
    MARATHI("Marathi", "mr"),
    KANNADA("Kannada", "kn"),
    MALAYALAM("Malayalam", "ml"),
    TAMIL("Tamil", "ta"),
    TELUGU("Telugu", "te"),
    ODIA("Odia", "or"),
    BENGALI("Bengali", "bn"),
    ENGLISH("English", "en")
}