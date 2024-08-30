package com.example.hackathonproject.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathonproject.R;
import com.example.hackathonproject.db.AuthManager;

import java.sql.SQLException;

public class SignUpActivity extends AppCompatActivity {
    private EditText etName, etPassword, etPhoneNum, etBirthYear, etMonth, etDay;
    private CheckBox cbIsOrganization ;
    private AuthManager authManager;
    private Button btnRegister;
    private TextView checkboxText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());


        authManager = new AuthManager();  // AuthManager 초기화

        etName = findViewById(R.id.full_name_input);
        etPassword = findViewById(R.id.password_input);
        etPhoneNum = findViewById(R.id.phone_number_input);
        etBirthYear = findViewById(R.id.birthYear_input);
        etMonth = findViewById(R.id.month_input);
        etDay = findViewById(R.id.day_input);
        cbIsOrganization = findViewById(R.id.checkbox);
        btnRegister = findViewById(R.id.sign_up_button);
        checkboxText = findViewById(R.id.checkbox_text);

        //---------------------------------------------------------------------------------------------
        // SharedPreferences에서 폰트 크기 불러오기
        SharedPreferences preferences = getSharedPreferences("fontSizePrefs", MODE_PRIVATE);
        int savedFontSize = preferences.getInt("fontSize", 25);  // 기본값 25

        // 불러온 폰트 크기를 UI 요소에 적용
        etName.setTextSize(savedFontSize);
        etPassword.setTextSize(savedFontSize);
        etPhoneNum.setTextSize(savedFontSize);
        etBirthYear.setTextSize(savedFontSize);
        etMonth.setTextSize(savedFontSize);
        etDay.setTextSize(savedFontSize);
        cbIsOrganization.setTextSize(savedFontSize);
        btnRegister.setTextSize(savedFontSize);
        checkboxText.setTextSize(savedFontSize);
        //---------------------------------------------------------------------------------------------

        // 전화번호 입력 필드에 최대 13자리 제한 및 형식화 적용
        etPhoneNum.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        etPhoneNum.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        // 생년월일 입력 필드의 자동 이동 설정
        setupFieldAutoMove();

        // 회원가입 버튼 클릭 시 동작 정의
        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String password = etPassword.getText().toString();
            String phoneNum = etPhoneNum.getText().toString();
            String birthYear = etBirthYear.getText().toString();
            String month = etMonth.getText().toString();
            String day = etDay.getText().toString();
            boolean isOrganization = cbIsOrganization.isChecked();

            // 모든 필드가 입력되었는지 확인
            if (name.isEmpty() || password.isEmpty() || phoneNum.isEmpty() || birthYear.isEmpty() || month.isEmpty() || day.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "모든 필드를 입력하세요", Toast.LENGTH_SHORT).show();
            } else {
                // 한 자리 입력의 경우 앞에 0을 추가
                month = month.length() == 1 ? "0" + month : month;
                day = day.length() == 1 ? "0" + day : day;

                String birthDate = birthYear + month + day;
                new RegisterUserTask().execute(name, password, phoneNum, birthDate, isOrganization);
            }
        });
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 입력 필드 자동 이동 설정
    private void setupFieldAutoMove() {
        etBirthYear.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        etMonth.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        etDay.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});

        // 연도 입력 완료 후 월 입력으로 자동 이동
        etBirthYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 4) {
                    etMonth.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 월 입력 완료 후 일 입력으로 자동 이동
        etMonth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2) {
                    etDay.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 일 입력 완료 후 회원가입 버튼으로 자동 이동
        etDay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2) {
                    btnRegister.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 비동기로 회원가입 작업을 수행하는 클래스
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
    //-----------------------------------------------------------------------------------------------------------------------------------------------
}
