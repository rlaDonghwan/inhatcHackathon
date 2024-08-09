package com.example.hackathonproject.Login;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathonproject.R;
import com.example.hackathonproject.db.AuthManager;

import java.sql.SQLException;

public class SignUpActivity extends AppCompatActivity {
    private EditText etName, etPassword, etPhoneNum, etBirthYear, etMonth, etDay;
    private CheckBox cbIsOrganization;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        authManager = new AuthManager();  // AuthManager 초기화

        etName = findViewById(R.id.full_name_input);
        etPassword = findViewById(R.id.password_input);
        etPhoneNum = findViewById(R.id.phone_number_input);
        etBirthYear = findViewById(R.id.birthYear_input);
        etMonth = findViewById(R.id.month_input);
        etDay = findViewById(R.id.day_input);
        cbIsOrganization = findViewById(R.id.checkbox);
        Button btnRegister = findViewById(R.id.sign_up_button);

        etPhoneNum.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        etPhoneNum.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String password = etPassword.getText().toString();
            String phoneNum = etPhoneNum.getText().toString();
            String birthYear = etBirthYear.getText().toString();
            String month = etMonth.getText().toString();
            String day = etDay.getText().toString();
            boolean isOrganization = cbIsOrganization.isChecked();

            if (name.isEmpty() || password.isEmpty() || phoneNum.isEmpty() || birthYear.isEmpty() || month.isEmpty() || day.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "모든 필드를 입력하세요", Toast.LENGTH_SHORT).show();
            } else {
                String birthDate = birthYear + month + day;
                new RegisterUserTask().execute(name, password, phoneNum, birthDate, isOrganization);
            }
        });
    }

    private class RegisterUserTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            String name = (String) params[0];
            String password = (String) params[1];
            String phoneNum = (String) params[2];
            String birthDate = (String) params[3];
            boolean isOrganization = (boolean) params[4];

            try {
                return authManager.registerUser(name, password, phoneNum, birthDate, isOrganization);  // AuthManager 사용
            } catch (SQLException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(SignUpActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUpActivity.this, SignInPhoneNumActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(SignUpActivity.this, "회원가입 실패: 이미 존재하는 사용자", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
