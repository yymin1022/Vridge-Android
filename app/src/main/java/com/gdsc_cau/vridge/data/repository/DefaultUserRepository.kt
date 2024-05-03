package com.gdsc_cau.vridge.data.repository

import com.gdsc_cau.vridge.data.api.VridgeApi
import com.gdsc_cau.vridge.data.database.LocalDataSource
import com.gdsc_cau.vridge.data.dto.LoginDTO
import com.gdsc_cau.vridge.data.dto.UidDTO
import com.gdsc_cau.vridge.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class DefaultUserRepository @Inject constructor(
    private val api: VridgeApi,
    private val auth: FirebaseAuth,
    private val dataSource: LocalDataSource
) : UserRepository {
    override suspend fun login(token: String): Boolean {
        return try {
            val fcmToken = dataSource.getMessageToken()
            val data = LoginDTO(token, fcmToken)
            api.login(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun unregister(): Boolean {
        return try {
            val uid = getUid()
            val data = UidDTO(uid)

            api.unregister(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    override fun signOut() {
        auth.signOut()
    }

    override fun getUid(): String {
        return auth.currentUser?.uid ?: ""
    }

    override suspend fun getUserInfo(): User {
        try {
            val uid = getUid()
            return api.getUserInfo(uid)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun setMessageToken(token: String): Boolean {
        return dataSource.setMessageToken(token)
    }
}
