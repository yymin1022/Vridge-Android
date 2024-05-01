package com.gdsc_cau.vridge.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TtsDTO(
    val text: String,
    val uid: String,
    val vid: String,
    val tid: String,
    val pitch: Int,
    val timestamp: Long = System.currentTimeMillis()
)
