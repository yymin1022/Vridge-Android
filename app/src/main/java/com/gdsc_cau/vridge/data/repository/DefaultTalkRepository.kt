package com.gdsc_cau.vridge.data.repository

import com.gdsc_cau.vridge.data.api.VridgeApi
import com.gdsc_cau.vridge.data.database.FileStorage
import com.gdsc_cau.vridge.data.database.InfoDatabase
import com.gdsc_cau.vridge.data.models.Tts
import com.gdsc_cau.vridge.data.dto.TtsDTO
import com.gdsc_cau.vridge.data.models.Voice
import com.gdsc_cau.vridge.ui.util.InvalidUidException
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID
import javax.inject.Inject

class DefaultTalkRepository
@Inject
constructor(
    private val api: VridgeApi,
    private val auth: FirebaseAuth,
    private val storage: FileStorage,
) : TalkRepository {
    override suspend fun createTts(text: String, vid: String): Tts {
        try {
            val uid = getUid() ?: throw InvalidUidException()
            val tid = UUID.randomUUID().toString().replace("-", "")
            val data = TtsDTO(text, uid, vid, tid, 0)

            return api.createTts(data)
        } catch (e: Exception) {
            throw e
        }
    }



    override suspend fun getTtsUrl(vid: String, tid: String): String {
        val uid = getUid() ?: return ""
        return storage.getDownloadUrl("$uid/$vid/$tid.wav")
    }

    override suspend fun getTalks(vid: String): List<Tts> {
        try {
            val uid = getUid() ?: return emptyList()
            return api.getTtsList(uid, vid)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getVoice(vid: String): Voice {
        try {
            val uid = getUid() ?: throw InvalidUidException()
            return api.getVoice(uid, vid)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun getUid() = auth.currentUser?.uid
}
