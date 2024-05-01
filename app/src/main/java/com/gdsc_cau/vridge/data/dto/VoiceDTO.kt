package com.gdsc_cau.vridge.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class VoiceDTO(
    val uid: String,
    val vid: String,
    val name: String,
    val pitch: Int,
    val language: String
)
