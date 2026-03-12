package com.myaux.app.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        private val IS_LOGGED = booleanPreferencesKey("is_logged")
        private val EMAIL = stringPreferencesKey("email")
        private val TOKEN = stringPreferencesKey("token")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_LOGGED] == true
    }

    val email: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[EMAIL]
    }

    suspend fun guardarSesion(email: String) {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED] = true
            prefs[EMAIL] = email
            prefs[TOKEN] = System.currentTimeMillis().toString()
        }
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun getEmail(): String? {
        return context.dataStore.data.first()[EMAIL]
    }

    suspend fun isSessionActive(): Boolean {
        return context.dataStore.data.first()[IS_LOGGED] == true
    }
}
