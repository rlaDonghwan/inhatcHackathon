package com.example.hackathonproject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.appcompat.widget.Toolbar;

public class AboutB1A3Activity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b1a3_info); // 레이아웃 설정

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // 툴바 설정
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 활성화
            getSupportActionBar().setTitle("B1A3 소개"); // 툴바 제목 설정
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // 뒤로 가기 버튼 클릭 시 이전 화면으로 이동
        return true;
    }
}
