package com.gdsc_cau.vridge.ui.profile

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.gdsc_cau.vridge.data.models.User

@Stable
sealed interface ProfileUiState {

    @Immutable
    data object Loading : ProfileUiState

    @Immutable
    data class Success(
        val user: User = User(),
        val isLoggedOut: Boolean = false
    ) : ProfileUiState
}
