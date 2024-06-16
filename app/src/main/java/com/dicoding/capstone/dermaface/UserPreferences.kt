package com.dicoding.capstone.dermaface

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_TOKEN = "user_token"
    }

    fun saveUserToken(token: String) {
        prefs.edit().putString(KEY_USER_TOKEN, token).apply()
    }

    fun getUserToken(): String? {
        return prefs.getString(KEY_USER_TOKEN, null)
    }

    fun clearUserData() {
        prefs.edit().clear().apply()
    }
}
