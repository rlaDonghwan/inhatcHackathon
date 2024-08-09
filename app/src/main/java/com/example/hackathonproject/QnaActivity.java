package com.example.hackathonproject;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class QnaActivity extends AppCompatActivity {

    private TextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qna);

        // Toolbar 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Toolbar 제목 설정을 위한 TextView 찾기
        titleTextView = findViewById(R.id.toolbar_title);

        // Toolbar 제목 설정
        if (titleTextView != null) {
            titleTextView.setText("Page 1 Title");
        }
    }
}
