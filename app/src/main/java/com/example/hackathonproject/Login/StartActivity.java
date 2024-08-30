package com.example.hackathonproject.Login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathonproject.R;

public class StartActivity extends AppCompatActivity {
    private Button btnStart;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);  // SessionManager 초기화

        if (sessionManager.isLoggedIn()) {
            // 이미 로그인된 상태라면 MainActivity로 이동
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();  // StartActivity 종료
        } else {
            // 로그인 상태가 아니라면 시작 화면 표시
            setContentView(R.layout.activity_start);
            btnStart = findViewById(R.id.btnStart);

            Toast.makeText(this, "시작 화면이 표시되었습니다.", Toast.LENGTH_SHORT).show();

            // 시작 버튼 클릭 시 FontSizeActivity로 이동
            btnStart.setOnClickListener(v -> {
                Intent intent = new Intent(StartActivity.this, FontSizeActivity.class);
                startActivity(intent);
                finish();  // StartActivity를 종료하여 뒤로 돌아가지 않도록 함
            });
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------
}