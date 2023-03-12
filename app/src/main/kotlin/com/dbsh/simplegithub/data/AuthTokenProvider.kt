package com.dbsh.simplegithub.data

import android.content.Context
import androidx.preference.PreferenceManager

class AuthTokenProvider(private val context: Context) {
    // SharedPreferences 에 액세스 토큰 저장
    fun updateToken(token: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(KEY_AUTH_TOKEN, token)
            .apply()
    }

    // 저장된 액세스 토큰 반환 메소드 (없으면 null 반환)
    val token: String?
        get() = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_AUTH_TOKEN, null)

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
    }
}