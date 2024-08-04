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

    private CheckBox autoLoginCheckbox;
    private Button startButton;
    private TextView logoutTextView;
    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SharedPreferences에서 사용자 이름과 자동 로그인 상태를 가져옴
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "사용자");
        boolean isAutoLogin = sharedPreferences.getBoolean("autoLogin", false);

        // 사용자 이름을 환영 메시지에 설정
        welcomeTextView = findViewById(R.id.welcome_text);
        welcomeTextView.setText(userName + "님");

        // 자동 로그인 체크박스 설정
        autoLoginCheckbox = findViewById(R.id.checkbox_auto_login);
        autoLoginCheckbox.setChecked(isAutoLogin);
        autoLoginCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("autoLogin", isChecked).apply();
        });

        // 텍스트에 밑줄 추가
        logoutTextView = findViewById(R.id.logout_text);
        logoutTextView.setPaintFlags(logoutTextView.getPaintFlags() | TextPaint.UNDERLINE_TEXT_FLAG);
        logoutTextView.setOnClickListener(v -> {
            // 로그아웃 처리
            sharedPreferences.edit().clear().apply();
            Toast.makeText(MainActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(intent);
            finish();
        });

        // 시작하기 버튼 클릭 이벤트 처리
        startButton = findViewById(R.id.button_start);
        startButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "시작하기 버튼 클릭됨", Toast.LENGTH_SHORT).show();
            // 여기에 시작하기 버튼 클릭 시 실행할 코드를 추가
        });
    }
}
