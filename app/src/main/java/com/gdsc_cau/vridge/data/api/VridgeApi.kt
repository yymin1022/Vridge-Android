package com.gdsc_cau.vridge.data.api

import com.gdsc_cau.vridge.data.dto.LoginDTO
import com.gdsc_cau.vridge.data.dto.SynthDTO
import com.gdsc_cau.vridge.data.models.ApiResponse
import com.gdsc_cau.vridge.data.dto.TtsDTO
import com.gdsc_cau.vridge.data.dto.UidDTO
import com.gdsc_cau.vridge.data.dto.RecordingDTO
import com.gdsc_cau.vridge.data.dto.VoiceDTO
import com.gdsc_cau.vridge.data.models.Tts
import com.gdsc_cau.vridge.data.models.User
import com.gdsc_cau.vridge.data.models.Voice
import com.gdsc_cau.vridge.data.models.VoiceListResponse
import kotlinx.serialization.json.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VridgeApi {
    @POST("/api/v1/user/login")
    suspend fun login(
        @Body data: LoginDTO
    ): JsonObject

    @POST("/api/v1/user/unregister")
    suspend fun unregister(
        @Body data: UidDTO
    ): JsonObject

    @GET("/api/v1/user/info")
    suspend fun getUserInfo(
        @Query("uid") uid: String
    ): User

    @GET("api/v1/voice/list")
    suspend fun getVoiceList(
        @Query("uid") uid: String
    ): VoiceListResponse

    @POST("api/v1/voice/upload")
    suspend fun uploadRecordingVoice(
        @Body data: RecordingDTO
    ): JsonObject

    @POST("api/v1/voice/delete")
    suspend fun clearRecordingVoice(
        @Body data: UidDTO
    ): JsonObject

    @POST("api/v1/voice/finish")
    suspend fun finishRecordingVoice(
        @Body data: VoiceDTO
    ): JsonObject

    @POST("api/v1/voice/synthesize")
    suspend fun synthesizeVoice(
        @Body data: SynthDTO
    ): Voice

    @POST("api/v1/tts/create")
    suspend fun createTts(
        @Body data: TtsDTO
    ): Tts

    @GET("api/v1/tts/list")
    suspend fun getTtsList(
        @Query("uid") uid: String,
        @Query("vid") vid: String
    ): List<Tts>
}
