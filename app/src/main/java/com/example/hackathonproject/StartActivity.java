package com.example.hackathonproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        btnStart = findViewById(R.id.btnStart); // xml에서 아이디 값을 찾아서 변수에 넣고

        Toast.makeText(this, "메인 화면이 표시되었습니다.", Toast.LENGTH_SHORT).show();

        btnStart.setOnClickListener(v -> { // 버튼 변수가 클릭이되는 이벤트가 발생하면 작동
            Intent intent = new Intent(StartActivity.this, SignInPhoneNumActivity.class); // 메인에서 전화번호 입력 페이지로 가는 코드
            startActivity(intent);
        });
    }
}
