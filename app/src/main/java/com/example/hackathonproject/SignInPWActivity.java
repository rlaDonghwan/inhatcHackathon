package com.example.hackathonproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.sql.SQLException;

public class SignInPWActivity extends AppCompatActivity {

    private EditText passwordInput;
    private Button confirmButton;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_pw);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 기본 타이틀 숨기기
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        passwordInput = findViewById(R.id.password_input);
        confirmButton = findViewById(R.id.confirm_button);

        // 이전 화면에서 전달된 전화번호 받기
        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");

        confirmButton.setOnClickListener(v -> {
            String password = passwordInput.getText().toString();
            if (!password.isEmpty()) {
                new LoginUserTask().execute(phoneNumber, password);
            } else {
                Toast.makeText(SignInPWActivity.this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });




    }

    // 비동기 작업으로 사용자 로그인 처리
    private class LoginUserTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String phoneNumber = params[0];
            String password = params[1];

            try {
                DatabaseHelper dbHelper = new DatabaseHelper();
                String userName = dbHelper.loginUser(phoneNumber, password);
                if (userName != null) {
                    // 로그인 성공 시 사용자 이름을 SharedPreferences에 저장
                    getSharedPreferences("user_prefs", MODE_PRIVATE)
                            .edit()
                            .putString("userName", userName)
                            .putString("phoneNumber", phoneNumber)
                            .apply();
                    return true;
                }
                return false;
            } catch (SQLException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Intent intent = new Intent(SignInPWActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // 현재 액티비티 종료
            } else {
                Toast.makeText(SignInPWActivity.this, "로그인 실패: 전화번호나 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
