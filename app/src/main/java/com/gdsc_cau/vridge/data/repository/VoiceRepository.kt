package com.gdsc_cau.vridge.data.repository

import com.gdsc_cau.vridge.data.models.Voice
import kotlinx.coroutines.flow.Flow

interface VoiceRepository {
    fun getScript(index: Int): String
    fun getScriptSize(): Int
    suspend fun setFileName(path: String): Boolean
    suspend fun setFile(index: Int): Boolean
    suspend fun finishRecord(name: String, pitch: Float): Boolean
    suspend fun synthesize(vid: List<String>): String
    suspend fun getVoiceList(): List<Voice>
}
