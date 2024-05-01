package com.gdsc_cau.vridge.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Tts(
    val id: String,
    val text: String,
    val timestamp: Long,
    val status: Boolean = false
)
