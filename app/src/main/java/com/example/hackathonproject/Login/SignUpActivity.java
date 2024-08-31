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
    private CheckBox cbIsOrganization, cbIsSchool;
    private AuthManager authManager;
    private Button btnRegister;
    private TextView companyCheckboxText;
    private TextView schoolCheckboxText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // 뒤로 가기 버튼 설정
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        // AuthManager 초기화
        authManager = new AuthManager();

        // UI 요소 초기화
        etName = findViewById(R.id.full_name_input);
        etPassword = findViewById(R.id.password_input);
        etPhoneNum = findViewById(R.id.phone_number_input);
        etBirthYear = findViewById(R.id.birthYear_input);
        etMonth = findViewById(R.id.month_input);
        etDay = findViewById(R.id.day_input);
        cbIsOrganization = findViewById(R.id.checkbox);
        cbIsSchool = findViewById(R.id.checkbox_school);
        companyCheckboxText = findViewById(R.id.checkbox_text);
        schoolCheckboxText = findViewById(R.id.checkbox_school_text);
        btnRegister = findViewById(R.id.sign_up_button);

        // 폰트 크기 설정 (SharedPreferences 사용)
        SharedPreferences preferences = getSharedPreferences("fontSizePrefs", MODE_PRIVATE);
        int savedFontSize = preferences.getInt("fontSize", 25);  // 기본값 25

        // 폰트 크기 적용
        etName.setTextSize(savedFontSize);
        etPassword.setTextSize(savedFontSize);
        etPhoneNum.setTextSize(savedFontSize);
        etBirthYear.setTextSize(savedFontSize);
        etMonth.setTextSize(savedFontSize);
        etDay.setTextSize(savedFontSize);
        companyCheckboxText.setTextSize(savedFontSize);
        schoolCheckboxText.setTextSize(savedFontSize);
        btnRegister.setTextSize(savedFontSize);

        // 전화번호 입력 형식 설정 및 최대 길이 제한
        etPhoneNum.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        etPhoneNum.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        // 생년월일 입력 필드 자동 이동 설정
        setupFieldAutoMove();

        // 체크박스 상태 변경 리스너 설정 (한쪽만 선택 가능하도록)
        cbIsOrganization.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbIsSchool.setChecked(false);  // 학교 체크박스 비활성화
            }
        });

        cbIsSchool.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbIsOrganization.setChecked(false);  // 기업 체크박스 비활성화
            }
        });

        // 회원가입 버튼 클릭 이벤트
        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String password = etPassword.getText().toString();
            String phoneNum = etPhoneNum.getText().toString();
            String birthYear = etBirthYear.getText().toString();
            String month = etMonth.getText().toString();
            String day = etDay.getText().toString();
            boolean isOrganization = cbIsOrganization.isChecked();
            boolean isSchool = cbIsSchool.isChecked();

            // 모든 필드가 입력되었는지 확인
            if (name.isEmpty() || password.isEmpty() || phoneNum.isEmpty() || birthYear.isEmpty() || month.isEmpty() || day.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "모든 필드를 입력하세요", Toast.LENGTH_SHORT).show();
            } else {
                // 한 자리 입력의 경우 앞에 0 추가
                month = month.length() == 1 ? "0" + month : month;
                day = day.length() == 1 ? "0" + day : day;

                // birthYear의 길이가 4자리인지 확인
                if (birthYear.length() != 4) {
                    Toast.makeText(SignUpActivity.this, "올바른 생년을 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                String birthDate = birthYear + month + day;

                // 인증이 필요한 경우
                if (isOrganization || isSchool) {
                    // 인증 화면으로 이동하면서 데이터를 전달
                    Intent intent = new Intent(SignUpActivity.this, CertificationActivity.class);
                    intent.putExtra("name", name);
                    intent.putExtra("password", password);
                    intent.putExtra("phoneNum", phoneNum);
                    intent.putExtra("birthDate", birthDate);
                    intent.putExtra("isOrganization", isOrganization);
                    intent.putExtra("isSchool", isSchool);
                    startActivity(intent);
                } else {
                    // 인증이 필요 없는 경우 바로 회원가입 처리
                    new RegisterUserTask().execute(name, password, phoneNum, birthDate, isOrganization, null, null);
                }
            }
        });
    }

    // 생년월일 필드 자동 이동 설정 메서드
    private void setupFieldAutoMove() {
        etBirthYear.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        etMonth.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        etDay.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});

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

    // 회원가입 작업을 백그라운드에서 처리하는 AsyncTask 클래스
    private class RegisterUserTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            String name = (String) params[0];
            String password = (String) params[1];
            String phoneNum = (String) params[2];
            String birthDate = (String) params[3];
            boolean isOrganization = (boolean) params[4];
            String companyName = (String) params[5];  // companyName is null if not organization
            String schoolName = (String) params[6];   // schoolName is null if not school

            try {
                return authManager.registerUser(name, password, phoneNum, birthDate, isOrganization, companyName, schoolName);
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