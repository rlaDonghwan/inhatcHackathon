package com.example.hackathonproject.Setting;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.hackathonproject.R;

public class QnaActivity extends AppCompatActivity {

    private TextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_qna);

        // Toolbar 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 기본 제목 숨기기
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 기본 백 버튼을 제거하고, XML에서 만든 버튼을 사용
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // back_button 클릭 시 뒤로 가기
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        // Toolbar 제목 설정을 위한 TextView 찾기 및 설정
        titleTextView = findViewById(R.id.toolbar_title);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
