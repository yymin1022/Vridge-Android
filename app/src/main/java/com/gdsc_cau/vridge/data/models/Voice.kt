package com.gdsc_cau.vridge.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Voice(
    val vid: String,
    val name: String,
    val pitch: Int,
    val language: String,
    val status: Boolean
)
