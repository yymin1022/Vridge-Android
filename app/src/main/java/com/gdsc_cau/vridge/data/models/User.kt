package com.gdsc_cau.vridge.data.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uid: String = "",
    val cntVoice: Int = 0,
    val email: String = "",
    val name: String = "",
)
