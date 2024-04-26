package com.gdsc_cau.vridge.ui.talk

import com.gdsc_cau.vridge.data.models.Tts

sealed interface TalkUiState {
    data object Loading: TalkUiState
    data class Success(
        val talks: List<Tts>
    ): TalkUiState
}
