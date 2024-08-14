package com.example.hackathonproject.Login;
import android.content.Context;
import android.content.SharedPreferences;


public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER_ID = "UserID";
    private static final String KEY_USER_NAME = "UserName"; // 사용자 이름 키 추가
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // 세션 생성 메서드
    public void createSession(String userName, int userId) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName); // 사용자 이름 저장
        editor.commit();
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1); // 로그인하지 않은 경우 -1 반환
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, ""); // 사용자 이름 반환
    }

    public void logout() {
        editor.clear();
        editor.commit();
    }

    public boolean isLoggedIn() {
        return getUserId() != -1; // -1이 아니면 로그인된 상태
    }

    public void clearSession() {
        editor.clear();
        editor.commit();
    }
}
