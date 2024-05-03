package com.gdsc_cau.vridge.data.database

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "preferences")

class LocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context) {
    private val tokenKey = stringPreferencesKey("token")

    suspend fun setMessageToken(token: String): Boolean {
        return try {
            context.dataStore.edit { preferences ->
                preferences[tokenKey] = token
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getMessageToken(): String {
        return context.dataStore.data.first()[tokenKey] ?: ""
    }
}
