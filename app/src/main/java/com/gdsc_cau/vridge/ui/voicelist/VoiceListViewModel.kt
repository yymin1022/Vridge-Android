package com.gdsc_cau.vridge.ui.voicelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdsc_cau.vridge.data.models.Voice
import com.gdsc_cau.vridge.data.repository.VoiceRepository
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
class VoiceListViewModel @Inject constructor(
    private val repository: VoiceRepository
) : ViewModel() {

    private val _errorFlow = MutableSharedFlow<Throwable>()
    val errorFlow: SharedFlow<Throwable> get() = _errorFlow

    private val _uiState = MutableStateFlow<VoiceListUiState>(VoiceListUiState.Loading)
    val uiState: StateFlow<VoiceListUiState> = _uiState

    init {
        viewModelScope.launch {
            flow {
                emit(repository.getVoiceList())
            }.map { voiceList ->
                VoiceListUiState.Success(
                    voiceList = voiceList
                )
            }.catch { throwable ->
                _errorFlow.emit(throwable)
            }.collect { _uiState.value = it }
        }
    }

    fun synthesize(voiceList: List<String>, name: String) {
        viewModelScope.launch {
            val state = _uiState.value
            val synthVoiceId = repository.synthesize(voiceList)

            if (state !is VoiceListUiState.Success) return@launch

            _uiState.value = VoiceListUiState.Success(
                voiceList = state.voiceList + Voice(
                    id = synthVoiceId,
                    name = name
                )
            )
        }
    }
}
