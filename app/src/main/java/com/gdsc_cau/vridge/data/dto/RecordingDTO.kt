package com.gdsc_cau.vridge.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecordingDTO(
    val uid: String,
    val vid: String,
    val index: Int,
)
