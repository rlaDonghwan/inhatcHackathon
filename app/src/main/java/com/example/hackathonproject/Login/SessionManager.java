package com.example.hackathonproject.Login;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER_ID = "UserID";
    private static final String KEY_USER_NAME = "UserName";
    private static final String KEY_VOLUNTEER_HOURS = "VolunteerHours";
    private static final String KEY_BALANCE = "Balance"; // Balance 키 추가
    private static final String KEY_IS_LOGGED_IN = "IsLoggedIn";
    private static final String KEY_IS_ORGANIZATION = "IsOrganization";
    private static final String KEY_USER_ROLE = "UserRole"; // Role 키 추가

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 세션 생성 메서드
    public void createSession(String userName, int userId, int balance, boolean isOrganization, String userRole) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putInt(KEY_BALANCE, balance); // Balance 저장
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putBoolean(KEY_IS_ORGANIZATION, isOrganization);
        editor.putString(KEY_USER_ROLE, userRole); // Role 저장
        editor.commit();
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    public int getBalance() {
        return pref.getInt(KEY_BALANCE, 0); // 기본값 0
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    public int getUserId() {
        int userId = pref.getInt(KEY_USER_ID, -1);
        Log.d("SessionManager", "Retrieved UserId: " + userId);  // 로그 추가
        return userId;
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "");
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    public int getVolunteerHours() {
        return pref.getInt(KEY_VOLUNTEER_HOURS, 0); // 기본값 0
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false); // 로그인 상태를 반환
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    public boolean isUserOrganization() {
        return pref.getBoolean(KEY_IS_ORGANIZATION, false); // 기관 여부를 반환
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    public String getUserRole() {
        return pref.getString(KEY_USER_ROLE, ""); // Role 반환
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    public void logout() {
        editor.clear();
        editor.commit();
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    public void clearSession() {
        editor.clear();
        editor.commit();
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------
}