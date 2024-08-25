package com.example.hackathonproject.Login;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.hackathonproject.R;
import com.example.hackathonproject.db.AuthManager;

import java.sql.SQLException;

public class SignInPasswordActivity extends AppCompatActivity {
    private EditText passwordInput;
    private Button confirmButton;
    private TextView forgotPasswordText;
    private String phoneNumber;

    private AuthManager authManager;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_password);

        authManager = new AuthManager();
        sessionManager = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        passwordInput = findViewById(R.id.password_input);
        confirmButton = findViewById(R.id.confirm_button);
        forgotPasswordText = findViewById(R.id.forgot_password_text);

        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");

        confirmButton.setOnClickListener(v -> {
            String password = passwordInput.getText().toString();
            if (!password.isEmpty()) {
                new LoginUserTask().execute(phoneNumber, password);
            } else {
                Toast.makeText(SignInPasswordActivity.this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        forgotPasswordText.setOnClickListener(v -> {
            Intent forgotPasswordIntent = new Intent(SignInPasswordActivity.this, ForgotPasswordActivity.class);
            forgotPasswordIntent.putExtra("previousActivity", "SignInPasswordActivity");
            startActivity(forgotPasswordIntent);
        });
    }

    private class LoginUserTask extends AsyncTask<String, Void, Boolean> {
        private String userName;
        private int userId;  // 사용자 ID 추가

        @Override
        protected Boolean doInBackground(String... params) {
            String phoneNumber = params[0];
            String password = params[1];

            try {
                userId = authManager.loginUserAndGetId(phoneNumber, password);  // 사용자 ID 가져오기
                if (userId != -1) {
                    userName = authManager.getUserNameById(userId);  // 사용자 이름 가져오기
                    return true;
                } else {
                    return false;
                }
            } catch (SQLException e) {
                Log.e("SignInPasswordActivity", "로그인 중 오류 발생", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                // 로그인 성공 시 사용자 이름과 ID를 SessionManager에 저장
                sessionManager.createSession(userName, userId);

                Intent intent = new Intent(SignInPasswordActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(SignInPasswordActivity.this, "로그인 실패: 전화번호나 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}