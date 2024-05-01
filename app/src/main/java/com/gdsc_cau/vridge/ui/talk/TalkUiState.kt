package com.gdsc_cau.vridge.ui.talk

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.gdsc_cau.vridge.data.models.Tts

@Stable
sealed interface TalkUiState {
    @Immutable
    data object Loading: TalkUiState

    @Immutable
    data class Success(
        val talks: List<Tts>
    ): TalkUiState
}
