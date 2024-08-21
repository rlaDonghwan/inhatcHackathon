package com.example.hackathonproject.Login;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
        setContentView(R.layout.activity_change_password);

        authManager = new AuthManager();  // AuthManager 초기화

        passwordInput = findViewById(R.id.password_input);
        confirmButton = findViewById(R.id.confirm_button);

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
                //startActivity(intent);
                finish();
            } else {
                Toast.makeText(ChangePasswordActivity.this, "비밀번호 변경 실패: 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
