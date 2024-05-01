package com.gdsc_cau.vridge.data.repository

import android.content.Context
import com.gdsc_cau.vridge.data.api.VridgeApi
import com.gdsc_cau.vridge.data.database.FileStorage
import com.gdsc_cau.vridge.data.database.InfoDatabase
import com.gdsc_cau.vridge.data.dto.RecordingDTO
import com.gdsc_cau.vridge.data.dto.SynthDTO
import com.gdsc_cau.vridge.data.dto.VoiceDTO
import com.gdsc_cau.vridge.data.models.Voice
import com.gdsc_cau.vridge.ui.util.InvalidUidException
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.util.UUID
import javax.inject.Inject

class DefaultVoiceRepository
@Inject
constructor(
    private val api: VridgeApi,
    private val storage: FileStorage,
    private val database: InfoDatabase,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : VoiceRepository {
    private val assetManager = context.assets
    private val scripts =
        assetManager.open("script.txt")
            .let {
                val size: Int = it.available()
                val buffer = ByteArray(size)
                it.read(buffer)
                buffer.toString(Charsets.UTF_8).split("\n")
            }.filter { it.isNotBlank() }

    private var vid: String? = null
    private var path: String? = null

    override fun getScript(index: Int): String {
        if (index < 1 || index > scripts.size)
            return ""
        return scripts[index - 1]
    }

    override fun getScriptSize(): Int {
        return scripts.size
    }

    override suspend fun setFileName(path: String): Boolean {
        vid = UUID.randomUUID().toString().replace("-", "")
        this.path = path
        return true
    }

    override suspend fun uploadRecord(index: Int): Boolean {
        val uid = getUid() ?: throw InvalidUidException()
        val vid = this.vid ?: throw IllegalStateException("No file to upload")
        val fileName = "$path/$index.m4a"

        return withContext(Dispatchers.IO) {
            val fileReader = FileInputStream(fileName)
            val data = ByteArray(fileReader.available())
            fileReader.read(data)
            fileReader.close()

            api.uploadRecordingVoice(RecordingDTO(uid, vid, index))
            storage.uploadFile(uid, vid, "$index.m4a", data)
        }
    }

    override suspend fun finishRecord(name: String, pitch: Int): Boolean {
        val uid = getUid() ?: throw InvalidUidException()
        val vid = this.vid ?: throw IllegalStateException("No file to upload")

        api.finishRecordingVoice(VoiceDTO(uid, vid, name, pitch, "KOR"))
        this.vid = null
        this.path = null
        return true
    }

    override suspend fun synthesize(vid: List<String>, name: String, pitch: Int): Voice {
        val uid = getUid() ?: throw InvalidUidException()
        val data = SynthDTO(uid, vid, name, pitch, "KOR")

        return api.synthesizeVoice(data)
    }

    override suspend fun getVoiceList(): List<Voice> {
        val uid = getUid() ?: throw InvalidUidException()

        return api.getVoiceList(uid).voiceList
    }

    private fun getUid(): String? {
        return auth.currentUser?.uid
    }
}
