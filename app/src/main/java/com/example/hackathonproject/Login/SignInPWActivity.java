package com.example.hackathonproject.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.hackathonproject.DatabaseHelper;
import com.example.hackathonproject.MainActivity;
import com.example.hackathonproject.R;

import java.sql.SQLException;

public class SignInPWActivity extends AppCompatActivity {
    //동환---------------------------------------------------------------------------------------------------------
    private EditText passwordInput; // 비밀번호 입력 필드
    private Button confirmButton; // 확인 버튼
    private TextView forgotPasswordText; // 비밀번호 찾기 텍스트
    private String phoneNumber; // 전화번호 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_password); // 레이아웃 설정

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
        forgotPasswordText = findViewById(R.id.forgot_password_text); // 비밀번호 찾기 텍스트 찾기

        // 이전 화면에서 전달된 전화번호 받기
        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber"); // 전달된 전화번호 가져오기

        confirmButton.setOnClickListener(v -> {
            String password = passwordInput.getText().toString(); // 입력된 비밀번호 가져오기
            if (!password.isEmpty()) { // 비밀번호가 비어있지 않은 경우
                new LoginUserTask().execute(phoneNumber, password); // 로그인 비동기 작업 실행
            } else {
                Toast.makeText(SignInPWActivity.this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show(); // 비밀번호 미입력 시 메시지 출력
            }
        });

        forgotPasswordText.setOnClickListener(v -> {
            Intent forgotPasswordIntent = new Intent(SignInPWActivity.this, ForgotPasswordActivity.class); // ForgotPasswordActivity로 인텐트 생성
            startActivity(forgotPasswordIntent); // ForgotPasswordActivity 시작
        });
    }

    // 비동기 작업으로 사용자 로그인 처리
    private class LoginUserTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String phoneNumber = params[0]; // 전화번호 가져오기
            String password = params[1]; // 비밀번호 가져오기

            try {
                DatabaseHelper dbHelper = new DatabaseHelper(); // DatabaseHelper 객체 생성
                String userName = dbHelper.loginUser(phoneNumber, password); // 로그인 시도
                if (userName != null) { // 로그인 성공 시
                    // 로그인 성공 시 사용자 이름과 로그인 상태를 SharedPreferences에 저장
                    SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userName", userName); // 사용자 이름 저장
                    editor.putString("phoneNumber", phoneNumber); // 전화번호 저장
                    editor.putBoolean("isLoggedIn", true); // 로그인 상태 저장
                    editor.apply(); // 변경사항 적용
                    return true;
                }
                return false; // 로그인 실패 시 false 반환
            } catch (SQLException e) {
                return false; // 예외 발생 시 false 반환
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) { // 로그인 성공 시
                Intent intent = new Intent(SignInPWActivity.this, MainActivity.class); // MainActivity로 인텐트 생성
                startActivity(intent); // MainActivity 시작
                finish(); // 현재 액티비티 종료
            } else { // 로그인 실패 시
                Toast.makeText(SignInPWActivity.this, "로그인 실패: 전화번호나 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show(); // 로그인 실패 메시지 출력
            }
        }
    }
    //동환---------------------------------------------------------------------------------------------------------
}
