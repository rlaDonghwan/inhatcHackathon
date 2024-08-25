package com.example.hackathonproject.Login;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER_ID = "UserID";
    private static final String KEY_USER_NAME = "UserName";
    private static final String KEY_IS_LOGGED_IN = "IsLoggedIn"; // 로그인 상태 저장용

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // 세션 생성 메서드
    public void createSession(String userName, int userId) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putBoolean(KEY_IS_LOGGED_IN, true); // 로그인 상태를 true로 설정
        editor.commit();
    }

    // SessionManager.java
    public int getUserId() {
        int userId = pref.getInt(KEY_USER_ID, -1);
        Log.d("SessionManager", "Retrieved UserId: " + userId);  // 로그 추가
        return userId;
    }


    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "");
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false); // 로그인 상태를 반환
    }

    public void logout() {
        editor.clear();
        editor.commit();
    }

    public void clearSession() {
        editor.clear();
        editor.commit();
    }
}
