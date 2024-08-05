package com.example.hackathonproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.sql.SQLException;

public class ChangePasswordActivity extends AppCompatActivity {
    //동환---------------------------------------------------------------------------------------------------------
    private EditText passwordInput; // 비밀번호 입력 필드
    private Button confirmButton; // 확인 버튼
    private String phoneNumber; // 전화번호 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password); // 레이아웃 설정

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // 툴바 설정

        // 기본 타이틀 숨기기
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 툴바 타이틀 숨기기
        }

        ImageButton backButton = findViewById(R.id.back_button); // 뒤로가기 버튼 찾기
        backButton.setOnClickListener(v -> onBackPressed()); // 뒤로가기 버튼 클릭 시 이전 화면으로 이동

        passwordInput = findViewById(R.id.password_input); // 비밀번호 입력 필드 찾기
        confirmButton = findViewById(R.id.confirm_button); // 확인 버튼 찾기

        // 이전 화면에서 전달된 전화번호 받기
        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber"); // 전달된 전화번호 가져오기

        confirmButton.setOnClickListener(v -> {
            String password = passwordInput.getText().toString(); // 입력된 비밀번호 가져오기
            if (!password.isEmpty()) { // 비밀번호가 비어있지 않은 경우
                new ChangePasswordTask().execute(phoneNumber, password); // 비밀번호 변경 비동기 작업 실행
            } else {
                Toast.makeText(ChangePasswordActivity.this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show(); // 비밀번호 미입력 시 메시지 출력
            }
        });

    }

    // 비동기 작업으로 비밀번호 변경 처리
    private class ChangePasswordTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String phoneNumber = params[0]; // 전화번호 가져오기
            String password = params[1]; // 비밀번호 가져오기

            try {
                DatabaseHelper dbHelper = new DatabaseHelper(); // DatabaseHelper 객체 생성
                return dbHelper.changePassword(phoneNumber, password); // 비밀번호 변경 시도
            } catch (SQLException e) {
                return false; // 예외 발생 시 false 반환
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) { // 비밀번호 변경 성공 시
                Toast.makeText(ChangePasswordActivity.this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ChangePasswordActivity.this, SignInPhoneNumActivity.class); // MainActivity로 인텐트 생성
                startActivity(intent); // MainActivity 시작
                finish(); // 현재 액티비티 종료
            } else { // 비밀번호 변경 실패 시
                Toast.makeText(ChangePasswordActivity.this, "비밀번호 변경 실패: 다시 시도해주세요.", Toast.LENGTH_SHORT).show(); // 비밀번호 변경 실패 메시지 출력
            }
        }
    }
    //동환---------------------------------------------------------------------------------------------------------
}
