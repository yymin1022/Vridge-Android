package com.gdsc_cau.vridge.ui.talk

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdsc_cau.vridge.data.models.Tts
import com.gdsc_cau.vridge.data.repository.TalkRepository
import com.gdsc_cau.vridge.ui.record.LOG_TAG
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel(assistedFactory = TalkViewModel.TalkViewModelFactory::class)
class TalkViewModel @AssistedInject constructor(
    @Assisted private val vid: String,
    private val repository: TalkRepository
) : ViewModel() {
    @AssistedFactory
    interface TalkViewModelFactory {
        fun create(vid: String): TalkViewModel
    }

    private val _uiState = MutableStateFlow<TalkUiState>(TalkUiState.Loading)
    val uiState: StateFlow<TalkUiState> = _uiState

    private val _errorFlow = MutableSharedFlow<Throwable>()
    val errorFlow: SharedFlow<Throwable> get() = _errorFlow

    private var player: MediaPlayer? = null

    init {
        viewModelScope.launch {
            flow {
                emit(repository.getTalks(vid))
            }.map { talks ->
                TalkUiState.Success(talks)
            }.catch { throwable ->
                _errorFlow.emit(throwable)
            }.collect { _uiState.value = it }
        }
    }

    fun onPlay(start: Boolean, ttsId: String = "") = if (start) {
        startPlaying(ttsId)
    } else {
        stopPlaying()
    }

    private fun startPlaying(ttsId: String) {
        viewModelScope.launch {
            val url = repository.getTtsUrl(vid, ttsId)

            player = MediaPlayer().apply {
                try {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(url)
                    prepare()
                    start()
                } catch (e: IOException) {
                    Log.e(LOG_TAG, "prepare() failed")
                }
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    fun createTts(text: String) {
        viewModelScope.launch {
            repository.createTts(text, vid)
        }
    }
}
