package com.gdsc_cau.vridge.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UidDTO(
    @SerialName("uid") val uid: String,
)
