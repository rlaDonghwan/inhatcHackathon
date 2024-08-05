package com.example.hackathonproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextPaint;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    //동환---------------------------------------------------------------------------------------------------------
    private Button startButton; // 시작하기 버튼
    private TextView logoutTextView; // 로그아웃 텍스트뷰
    private TextView welcomeTextView; // 환영 메시지 텍스트뷰

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 레이아웃 설정

        // SharedPreferences에서 사용자 이름과 자동 로그인 상태를 가져옴
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "사용자"); // 사용자 이름 가져오기

        // 사용자 이름을 환영 메시지에 설정
        welcomeTextView = findViewById(R.id.welcome_text); // 환영 메시지 텍스트뷰 찾기
        welcomeTextView.setText(userName + "님"); // 환영 메시지 설정

        // 텍스트에 밑줄 추가
        logoutTextView = findViewById(R.id.logout_text); // 로그아웃 텍스트뷰 찾기
        logoutTextView.setPaintFlags(logoutTextView.getPaintFlags() | TextPaint.UNDERLINE_TEXT_FLAG); // 밑줄 추가
        logoutTextView.setOnClickListener(v -> {
            // 로그아웃 처리
            sharedPreferences.edit().clear().apply(); // SharedPreferences 초기화
            Toast.makeText(MainActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show(); // 로그아웃 메시지 출력
            Intent intent = new Intent(MainActivity.this, StartActivity.class); // StartActivity로 인텐트 생성
            startActivity(intent); // StartActivity 시작
            finish(); // MainActivity 종료
        });

        // 시작하기 버튼 클릭 이벤트 처리
        startButton = findViewById(R.id.button_start); // 시작하기 버튼 찾기
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EducationActivity.class); // EducationActivity로 인텐트 생성
            startActivity(intent); // EducationActivity 시작
        });
    }
    //동환---------------------------------------------------------------------------------------------------------
}
