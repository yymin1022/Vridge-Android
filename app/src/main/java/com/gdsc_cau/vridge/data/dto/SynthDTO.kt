package com.gdsc_cau.vridge.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SynthDTO(
    @SerialName("uid") val uid: String,
    @SerialName("vid") val vid: List<String>,
)
