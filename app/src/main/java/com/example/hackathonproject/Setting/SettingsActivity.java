package com.example.hackathonproject.Setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathonproject.Chat.ChatListActivity;
import com.example.hackathonproject.Education.EducationActivity;
import com.example.hackathonproject.Login.SessionManager;
import com.example.hackathonproject.Login.StartActivity;
import com.example.hackathonproject.R;
import com.example.hackathonproject.Lecture.LectureActivity;

public class SettingsActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // 세션 매니저 초기화
        sessionManager = new SessionManager(this);

        // 세션에서 사용자 이름 가져오기
        String userName = sessionManager.getUserName();
        int volunteerHours = sessionManager.getVolunteerHours();

        // 상단 프로필 이름을 세션 이름으로 설정
        TextView profileNameTextView = findViewById(R.id.question);
        profileNameTextView.setText(userName);

        TextView volunteerHoursTextView = findViewById(R.id.title_time);
        volunteerHoursTextView.setText(String.valueOf(volunteerHours));

        // 각 옵션 클릭 시 해당 액티비티로 이동
        LinearLayout editProfileOption = findViewById(R.id.option_edit_profile);
        LinearLayout aboutB1A3Option = findViewById(R.id.option_about_b1a3);
        LinearLayout qnaOption = findViewById(R.id.option_qna);
        LinearLayout logoutOption = findViewById(R.id.logout_option);

        // TextView들 초기화
        TextView editMyProfileTextView = findViewById(R.id.edit_my_profile);
        TextView faqTextView = findViewById(R.id.faq);
        TextView fontSizeTextView = findViewById(R.id.font_size);
        TextView logoutTextView = findViewById(R.id.logout);
        TextView welcomeMessageTextView = findViewById(R.id.welcome_message);

        // SharedPreferences에서 폰트 크기 불러오기
        SharedPreferences preferences = getSharedPreferences("fontSizePrefs", MODE_PRIVATE);
        int savedFontSize = preferences.getInt("fontSize", 25);  // 기본값 25

        // 불러온 폰트 크기를 UI 요소에 적용
        editMyProfileTextView.setTextSize(savedFontSize);
        faqTextView.setTextSize(savedFontSize);
        fontSizeTextView.setTextSize(savedFontSize);
        logoutTextView.setTextSize(savedFontSize);
        welcomeMessageTextView.setTextSize(savedFontSize);


        editProfileOption.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        aboutB1A3Option.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, SettingFontSizeActivity.class);
            startActivity(intent);
        });

        qnaOption.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, QnaActivity.class);
            startActivity(intent);
        });

        // 로그아웃 클릭 시 StartActivity로 이동하고 세션 정보 제거
        logoutOption.setOnClickListener(v -> {
            sessionManager.clearSession();  // 세션 정보 제거

            Intent intent = new Intent(SettingsActivity.this, StartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();  // 현재 액티비티 종료
        });

        // 교육 받기 탭 클릭 시 EducationActivity로 이동
        LinearLayout firstMenuItem = findViewById(R.id.first_menu_item);
        firstMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, EducationActivity.class);
            startActivity(intent);
        });

        // 강연자 신청 탭 클릭 시 SeminarActivity로 이동
        LinearLayout secondMenuItem = findViewById(R.id.second_menu_item);
        secondMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, LectureActivity.class);
            startActivity(intent);
        });

        // 채팅 탭 클릭 시 ChatActivity로 이동
        LinearLayout thirdMenuItem = findViewById(R.id.third_menu_item);
        thirdMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ChatListActivity.class);
            startActivity(intent);
        });

        // 설정 탭 클릭 시 SettingsActivity로 이동
        LinearLayout fourthMenuItem = findViewById(R.id.fourth_menu_item);
        fourthMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}
