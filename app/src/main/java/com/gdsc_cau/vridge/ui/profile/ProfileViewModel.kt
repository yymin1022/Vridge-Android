package com.gdsc_cau.vridge.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdsc_cau.vridge.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _errorFlow = MutableSharedFlow<Throwable>()
    val errorFlow: SharedFlow<Throwable> get() = _errorFlow

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        viewModelScope.launch {
            flow {
                emit(repository.getUserInfo())
            }.map { user ->
                ProfileUiState.Success(
                    user = user,
                    isLoggedOut = false
                )
            }.catch { throwable ->
                Log.e("VRIDGE-ProfileVM", throwable.message.toString())
                _errorFlow.emit(throwable)
            }.collect { _uiState.value = it }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state !is ProfileUiState.Success) return@launch
            repository.signOut()

            _uiState.value = state.copy(
                user = state.user.copy(),
                isLoggedOut = true
            )
        }
    }

    fun unregister() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state !is ProfileUiState.Success) return@launch

            if (repository.unregister().not()) return@launch

            _uiState.value = state.copy(
                user = state.user.copy(),
                isLoggedOut = true
            )
        }
    }
}
