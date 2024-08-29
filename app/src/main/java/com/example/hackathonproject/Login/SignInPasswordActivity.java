package com.example.hackathonproject.Login;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.hackathonproject.Setting.ForgotPasswordActivity;
import com.example.hackathonproject.db.AuthManager;

import java.sql.SQLException;

public class SignInPasswordActivity extends AppCompatActivity {
    private EditText passwordInput;
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
        Button confirmButton = findViewById(R.id.confirm_button);
        TextView forgotPasswordText = findViewById(R.id.forgot_password_text);

        //---------------------------------------------------------------------------------------------
        // SharedPreferences에서 폰트 크기 불러오기
        SharedPreferences preferences = getSharedPreferences("fontSizePrefs", MODE_PRIVATE);
        int savedFontSize = preferences.getInt("fontSize", 25);  // 기본값 25

        // 불러온 폰트 크기를 UI 요소에 적용
        passwordInput.setTextSize(savedFontSize);
        confirmButton.setTextSize(savedFontSize);
        forgotPasswordText.setTextSize(savedFontSize);
        //---------------------------------------------------------------------------------------------

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
        private int userId;
        private int volunteerHours;
        private boolean isOrganization;  // 기관 여부 추가

        @Override
        protected Boolean doInBackground(String... params) {
            String phoneNumber = params[0];
            String password = params[1];

            try {
                userId = authManager.loginUserAndGetId(phoneNumber, password);
                if (userId != -1) {
                    userName = authManager.getUserNameById(userId);
                    volunteerHours = authManager.getVolunteerHoursById(userId);
                    isOrganization = authManager.isUserOrganization(userId);  // 기관 여부 가져오기
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
                // 로그인 성공 시 사용자 이름, ID, 누적 봉사 시간, 기관 여부를 SessionManager에 저장
                sessionManager.createSession(userName, userId, volunteerHours, isOrganization);

                Intent intent = new Intent(SignInPasswordActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(SignInPasswordActivity.this, "로그인 실패: 전화번호나 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
