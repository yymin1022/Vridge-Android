package com.gdsc_cau.vridge.data.repository

import com.gdsc_cau.vridge.data.models.Tts
import com.gdsc_cau.vridge.data.models.Voice

interface TalkRepository {
    suspend fun createTts(text: String, vid: String): Tts
    suspend fun getTtsUrl(vid: String, tid: String): String
    suspend fun getTalks(vid: String): List<Tts>
    suspend fun getVoice(vid: String): Voice
}
