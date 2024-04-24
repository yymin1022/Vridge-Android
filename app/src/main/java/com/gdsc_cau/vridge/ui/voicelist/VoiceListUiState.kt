package com.gdsc_cau.vridge.ui.voicelist

import com.gdsc_cau.vridge.data.models.Voice

sealed interface VoiceListUiState {
    data object Loading : VoiceListUiState
    data object Empty : VoiceListUiState
    data class Success(
        val voiceList: List<Voice>
    ) : VoiceListUiState
}
