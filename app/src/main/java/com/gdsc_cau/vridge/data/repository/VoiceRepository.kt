package com.gdsc_cau.vridge.data.repository

import com.gdsc_cau.vridge.data.models.Voice
import kotlinx.coroutines.flow.Flow

interface VoiceRepository {
    fun getScript(index: Int): String
    fun getScriptSize(): Int
    suspend fun setFileName(path: String): Boolean
    suspend fun uploadRecord(index: Int): Boolean
    suspend fun finishRecord(name: String, pitch: Int): Boolean
    suspend fun synthesize(vid: List<String>, name: String, pitch: Int): Voice
    suspend fun getVoiceList(): List<Voice>
    suspend fun removeRecordingVoice(): Boolean
}
