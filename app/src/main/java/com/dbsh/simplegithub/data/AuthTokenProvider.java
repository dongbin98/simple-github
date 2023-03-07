package com.dbsh.simplegithub.data;

import android.content.Context;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class AuthTokenProvider {
    private static final String KEY_AUTH_TOKEN = "auth_token";

    private final Context context;

    public AuthTokenProvider(@NonNull Context context) {
        this.context = context;
    }

    // SharedPreferences 에 액세스 토큰 저장
    public void updateToken(@NonNull String token) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(KEY_AUTH_TOKEN, token)
                .apply();
    }

    // 저장된 액세스 토큰 반환 메소드 (없으면 null 반환)
    @Nullable
    public String getToken() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_AUTH_TOKEN, null);
    }
}
