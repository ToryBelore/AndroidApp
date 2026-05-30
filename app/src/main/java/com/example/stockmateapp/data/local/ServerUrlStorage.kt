package com.example.stockmateapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("settings")

@Singleton
class ServerUrlStorage @Inject constructor(@ApplicationContext private val context: Context) {

    private val key = stringPreferencesKey("server_url")

    val serverUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[key] ?: DEFAULT_URL
    }

    suspend fun save(url: String) {
        context.dataStore.edit { it[key] = url }
    }

    companion object {
        const val DEFAULT_URL = "https://stockmate.springfamilychat.space"
    }
}
