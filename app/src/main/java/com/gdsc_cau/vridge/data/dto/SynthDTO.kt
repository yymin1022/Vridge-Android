package com.gdsc_cau.vridge.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SynthDTO(
    val uid: String,
    val vids: List<String>,
    val name: String,
    val pitch: Int,
    val language: String
)
