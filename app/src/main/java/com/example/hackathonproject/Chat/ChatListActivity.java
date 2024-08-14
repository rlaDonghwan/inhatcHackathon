package com.example.hackathonproject.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathonproject.Education.EducationActivity;
import com.example.hackathonproject.R;
import com.example.hackathonproject.Seminar.SeminarActivity;
import com.example.hackathonproject.Setting.SettingsActivity;

public class ChatListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        // 필터 버튼 초기 선택 상태 설정
        Button filterAll = findViewById(R.id.filter_all);
        Button filterEducation = findViewById(R.id.filter_education);
        Button filterLecture = findViewById(R.id.filter_lecture);

        // 기본 선택 상태 설정
        filterAll.setSelected(true);

        // 필터 버튼 클릭 이벤트 설정
        filterAll.setOnClickListener(v -> {
            filterAll.setSelected(true);
            filterEducation.setSelected(false);
            filterLecture.setSelected(false);
            // 여기에 필터가 적용된 데이터 로딩 로직을 추가
        });

        filterEducation.setOnClickListener(v -> {
            filterAll.setSelected(false);
            filterEducation.setSelected(true);
            filterLecture.setSelected(false);
            // 여기에 필터가 적용된 데이터 로딩 로직을 추가
        });

        filterLecture.setOnClickListener(v -> {
            filterAll.setSelected(false);
            filterEducation.setSelected(false);
            filterLecture.setSelected(true);
            // 여기에 필터가 적용된 데이터 로딩 로직을 추가
        });

        // 교육 받기 탭 클릭 시 EducationActivity로 이동
        LinearLayout firstMenuItem = findViewById(R.id.first_menu_item);
        firstMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(ChatListActivity.this, EducationActivity.class);
            startActivity(intent);
        });

        // 강연자 신청 탭 클릭 시 SeminarActivity로 이동
        LinearLayout secondMenuItem = findViewById(R.id.second_menu_item);
        secondMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(ChatListActivity.this, SeminarActivity.class);
            startActivity(intent);
        });

        // 채팅 탭 클릭 시 ChatListActivity로 이동 (현재 액티비티와 동일)
        LinearLayout thirdMenuItem = findViewById(R.id.third_menu_item);
        thirdMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(ChatListActivity.this, ChatListActivity.class);
            startActivity(intent);
        });

        // 설정 탭 클릭 시 SettingsActivity로 이동
        LinearLayout fourthMenuItem = findViewById(R.id.fourth_menu_item);
        fourthMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(ChatListActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}
