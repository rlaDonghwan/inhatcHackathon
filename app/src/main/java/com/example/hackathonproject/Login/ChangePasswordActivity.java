package com.example.hackathonproject.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.hackathonproject.R;
import com.example.hackathonproject.db.AuthManager;

import java.sql.SQLException;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText passwordInput;
    private Button confirmButton;
    private String phoneNumber;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_change_password);

        // Toolbar 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 뒤로가기 버튼을 활성화
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 제목을 표시하지 않음
        }

        // AuthManager 초기화
        authManager = new AuthManager();

        passwordInput = findViewById(R.id.password_input);
        confirmButton = findViewById(R.id.confirm_button);

        // SharedPreferences에서 폰트 크기 불러오기
        SharedPreferences preferences = getSharedPreferences("fontSizePrefs", MODE_PRIVATE);
        int savedFontSize = preferences.getInt("fontSize", 25);  // 기본값 25

        // 불러온 폰트 크기를 UI 요소에 적용
        passwordInput.setTextSize(savedFontSize);
        confirmButton.setTextSize(savedFontSize);
        //---------------------------------------------------------------------------------------------------

        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");

        confirmButton.setOnClickListener(v -> {
            String newPassword = passwordInput.getText().toString();
            if (!newPassword.isEmpty()) {
                new ChangePasswordTask().execute(phoneNumber, newPassword);
            } else {
                Toast.makeText(ChangePasswordActivity.this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 뒤로가기 버튼의 동작을 정의
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private class ChangePasswordTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String phoneNum = params[0];
            String newPassword = params[1];

            try {
                return authManager.changePassword(phoneNum, newPassword);  // AuthManager 사용
            } catch (SQLException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(ChangePasswordActivity.this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ChangePasswordActivity.this, SignInPhoneNumActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(ChangePasswordActivity.this, "비밀번호 변경 실패: 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
