package com.example.hackathonproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {
    //동환---------------------------------------------------------------------------------------------------------
    private Button btnStart; // 시작 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SharedPreferences에서 로그인 상태를 확인
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false); // 로그인 상태 가져오기

        if (isLoggedIn) {
            // 로그인 상태라면 MainActivity로 이동
            Intent intent = new Intent(StartActivity.this, MainActivity.class); // MainActivity로 인텐트 생성
            startActivity(intent); // MainActivity 시작
            finish(); // 현재 액티비티 종료
        } else {
            // 로그인 상태가 아니라면 로그인 화면 표시
            setContentView(R.layout.activity_start); // 레이아웃 설정
            btnStart = findViewById(R.id.btnStart); // xml에서 아이디 값을 찾아서 변수에 할당

            Toast.makeText(this, "메인 화면이 표시되었습니다.", Toast.LENGTH_SHORT).show(); // 토스트 메시지 출력

            btnStart.setOnClickListener(v -> { // 버튼 클릭 이벤트 처리
                Intent intent = new Intent(StartActivity.this, SignInPhoneNumActivity.class); // SignInPhoneNumActivity로 인텐트 생성
                startActivity(intent); // SignInPhoneNumActivity 시작
            });
        }
    }
    //동환---------------------------------------------------------------------------------------------------------

    //주석 추가할 때 이름 넣어서 밑에 처럼 해주세요
    //동환---------------------------------------------------------------------------------------------------------
    //코드
    //동환---------------------------------------------------------------------------------------------------------
}
