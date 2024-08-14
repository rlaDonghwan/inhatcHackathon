package com.example.hackathonproject.Login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextPaint;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathonproject.Education.EducationActivity;
import com.example.hackathonproject.R;

public class MainActivity extends AppCompatActivity {
    private Button startButton;  // 교육 활동을 시작하는 버튼
    private TextView logoutTextView;  // 로그아웃 버튼 역할을 하는 텍스트뷰
    private TextView welcomeTextView;  // 환영 메시지를 표시하는 텍스트뷰
    private SessionManager sessionManager;  // 세션 관리 객체

    //-----------------------------------------------------------------------------------------------------------------------------------------------
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);  // 세션 관리 객체 초기화

        // 앱 시작 시 로그인 상태를 확인
        if (!sessionManager.isLoggedIn()) {
            // 로그인 상태가 아닐 경우 로그인 화면으로 이동
            Intent intent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(intent);
            finish(); // MainActivity 종료
            return;
        }

        // 사용자의 이름을 가져와 환영 메시지를 설정
        String userName = sessionManager.getUserName();
        welcomeTextView = findViewById(R.id.welcome_text);
        welcomeTextView.setText(userName + "님");

        // 로그아웃 텍스트뷰 초기화 및 밑줄 설정
        logoutTextView = findViewById(R.id.logout_text);
        logoutTextView.setPaintFlags(logoutTextView.getPaintFlags() | TextPaint.UNDERLINE_TEXT_FLAG);
        logoutTextView.setOnClickListener(v -> {
            sessionManager.logout();  // 세션 초기화 (로그아웃)
            Toast.makeText(MainActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();  // 로그아웃 메시지 표시
            Intent intent = new Intent(MainActivity.this, StartActivity.class);  // 로그인 화면으로 이동
            startActivity(intent);
            finish();  // MainActivity 종료
        });

        // 교육 활동을 시작하는 버튼 초기화 및 클릭 리스너 설정
        startButton = findViewById(R.id.button_start);
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EducationActivity.class);  // EducationActivity로 이동
            startActivity(intent);
        });
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------
}
