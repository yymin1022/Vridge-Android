package com.gdsc_cau.vridge.data.models

import kotlinx.serialization.Serializable

@Serializable
data class VoiceListResponse(
    val voiceList: List<Voice>
)
