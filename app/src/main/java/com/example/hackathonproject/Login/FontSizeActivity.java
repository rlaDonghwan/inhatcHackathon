package com.example.hackathonproject.Login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.hackathonproject.R;

public class FontSizeActivity extends Activity {
    private Button btnContinue;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font_size);

        // '확인' 버튼 초기화
        btnContinue = findViewById(R.id.confirm_button);

        // '확인' 버튼 클릭 시 로그인 화면(SignInPhoneNumActivity)으로 이동
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(FontSizeActivity.this, SignInPhoneNumActivity.class);
            startActivity(intent);
            finish();  // FontSizeActivity를 종료하여 뒤로가기 버튼으로 이 화면으로 돌아오지 않도록 함
        });

        // 뒤로 가기 버튼 초기화
        backButton = findViewById(R.id.back_button);

        // 뒤로 가기 버튼 클릭 시 현재 액티비티 종료 (이전 액티비티로 돌아감)
        backButton.setOnClickListener(v -> {
            finish();  // 현재 액티비티를 종료하여 이전 액티비티로 돌아감
        });
    }
}
