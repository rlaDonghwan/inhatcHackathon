package com.example.hackathonproject;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        autoLoginCheckbox = findViewById(R.id.checkbox_auto_login);
        startButton = findViewById(R.id.button_start);
        logoutTextView = findViewById(R.id.logout_text);

        // 텍스트에 밑줄 추가
        logoutTextView.setPaintFlags(logoutTextView.getPaintFlags() | TextPaint.UNDERLINE_TEXT_FLAG);

        startButton.setOnClickListener(v -> {
            // 시작하기 버튼 클릭 이벤트 처리
            Toast.makeText(MainActivity.this, "시작하기 버튼 클릭됨", Toast.LENGTH_SHORT).show();
        });

        logoutTextView.setOnClickListener(v -> {
            // 로그아웃 텍스트 클릭 이벤트 처리
            Toast.makeText(MainActivity.this, "로그아웃 텍스트 클릭됨", Toast.LENGTH_SHORT).show();
        });
    }
}
