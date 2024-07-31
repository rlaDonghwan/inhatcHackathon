package com.example.hackathonproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.SQLException;

public class LoginActivity extends AppCompatActivity {

    private EditText etPhoneNum, etPassword;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPhoneNum = findViewById(R.id.etPhoneNum);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> {
            String phoneNum = etPhoneNum.getText().toString();
            String password = etPassword.getText().toString();
            new LoginTask().execute(phoneNum, password);
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private class LoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String phoneNum = params[0];
            String password = params[1];
            DatabaseHelper dbHelper = new DatabaseHelper();
            try {
                return dbHelper.loginUser(phoneNum, password);
            } catch (SQLException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String userName) {
            if (userName != null) {
                // 로그인 성공, 토큰 생성 및 저장
                String token = "dummy_token_from_server"; // 실제 토큰을 사용해야 합니다.
                // 로그인 성공 시
                SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("authToken", token);
                editor.putString("userName", userName); // 사용자 이름 저장
                editor.apply();


                // 메인 화면으로 이동
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("userName", userName);
                startActivity(intent);
                finish();
            } else {
                // 로그인 실패 메시지 표시
                Toast.makeText(LoginActivity.this, "로그인 실패. 다시 시도하세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
