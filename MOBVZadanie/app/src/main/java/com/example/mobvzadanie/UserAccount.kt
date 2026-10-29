package com.example.mobvzadanie

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "account")

class UserAccount(private val context: Context) {

    companion object {
        private val NAME_KEY = stringPreferencesKey("name")
        private val AVATAR_KEY = stringPreferencesKey("avatar")
    }

    val nameFlow: Flow<String> =
        context.dataStore.data.map { it[NAME_KEY] ?: "" }

    val avatarFlow: Flow<String> =
        context.dataStore.data.map { it[AVATAR_KEY] ?: "" }

    fun getName(): Flow<String> = nameFlow

    fun getAvatar(): Flow<String> = avatarFlow

    suspend fun setName(name: String) {
        context.dataStore.edit { it[NAME_KEY] = name }
    }

    suspend fun setAvatar(avatar: String) {
        context.dataStore.edit { it[AVATAR_KEY] = avatar }
    }
}
