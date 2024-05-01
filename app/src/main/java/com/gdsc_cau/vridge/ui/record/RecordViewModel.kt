package com.gdsc_cau.vridge.ui.record

import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdsc_cau.vridge.data.repository.VoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val repository: VoiceRepository
) : ViewModel() {
    private val _recordContent = MutableStateFlow(Pair(1, repository.getScript(1)))
    private val recordContent: StateFlow<Pair<Int, String>> = _recordContent

    private val index: Int get() = recordContent.value.first

    private val _recordState = MutableStateFlow(RecordState.IDLE)
    private val recordState: StateFlow<RecordState> = _recordState

    private val _errorFlow = MutableSharedFlow<Throwable>()
    val errorFlow: SharedFlow<Throwable> get() = _errorFlow

    private val _uiState = MutableStateFlow<RecordUiState>(RecordUiState.Loading)
    val uiState: StateFlow<RecordUiState> = _uiState

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    private lateinit var fileDir: String
    private lateinit var fileName: String

    private val size = repository.getScriptSize()

    init {
        viewModelScope.launch {
            combine(recordContent, recordState) { content, state ->
                RecordUiState.Success(content.first, content.second, size, state)
            }.catch {
                _errorFlow.emit(it)
            }.collect {
                _uiState.emit(it)
            }
        }
    }

    fun setFileName(name: String) {
        viewModelScope.launch {
            fileDir = name
            fileName = "$fileDir/$index.m4a"
            repository.setFileName(fileDir)
        }
    }

    fun startRecord(recorder: MediaRecorder) {
        viewModelScope.launch {
            try {
                _recordState.emit(RecordState.RECORDING)
                this@RecordViewModel.recorder = recorder.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setOutputFile(fileName)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                }
                recorder.prepare()
                recorder.start()
            } catch (e: Exception) {
                _errorFlow.emit(e)
                _recordState.emit(RecordState.IDLE)
            }
        }
    }

    fun stopRecord() {
        viewModelScope.launch {
            try {
                recorder?.let {
                    it.stop()
                    it.release()
                    recorder = null
                    _recordState.emit(RecordState.RECORDED)
                } ?: throw IllegalStateException("Recorder is not initialized")
            } catch (e: Exception) {
                _recordState.emit(RecordState.IDLE)
                _errorFlow.emit(e)
            }
        }
    }

    fun startPlay() {
        viewModelScope.launch {
            try {
                player = MediaPlayer().apply {
                    _recordState.emit(RecordState.PLAYING)
                    setDataSource(fileName)
                    this.prepare()
                    this.start()
                }
            } catch (e: Exception) {
                _recordState.emit(RecordState.RECORDED)
                _errorFlow.emit(e)
            }
        }
    }

    fun stopPlay() {
        viewModelScope.launch {
            try {
                player?.stop()
                player?.release()
                player = null
                _recordState.emit(RecordState.RECORDED)
            } catch (e: Exception) {
                _recordState.emit(RecordState.RECORDED)
                _errorFlow.emit(e)
            }
        }
    }

    fun getNext() {
        viewModelScope.launch {
            try {
                _recordState.emit(RecordState.LOADING)
                if (uiState.value is RecordUiState.Success) {
                    uiState.value.let {
                        if (index == size) {
                            _recordState.emit(RecordState.FINISHING)
                            return@launch
                        }
                    }
                }
                if (repository.uploadRecord(index)) {
                    fileName = "$fileDir/${index + 1}}.m4a"
                    _recordContent.emit(Pair(index + 1, repository.getScript(index + 1)))
                    _recordState.emit(RecordState.IDLE)
                }
            } catch (e: Exception) {
                _recordState.emit(RecordState.RECORDED)
                _errorFlow.emit(e)
            }
        }
    }

    fun finishRecord(name: String, pitch: Float) {
        viewModelScope.launch {
            try {
                _recordState.emit(RecordState.LOADING)
                if (repository.finishRecord(name, pitch.roundToInt())) {
                    _recordState.emit(RecordState.FINISHED)
                } else {
                    _recordState.emit(RecordState.RECORDED)
                }
            } catch (e: Exception) {
                _recordState.emit(RecordState.RECORDED)
                _errorFlow.emit(e)
            }
        }
    }
}
