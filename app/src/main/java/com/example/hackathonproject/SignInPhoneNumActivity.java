package com.example.hackathonproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SignInPhoneNumActivity extends AppCompatActivity {

    private EditText phoneInput;
    private Button confirmButton;
    private Button createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_phone_number);

        // Toolbar 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 기본 타이틀 숨기기
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 뒤로가기 버튼 설정
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        // 전화번호 입력과 확인 버튼 설정
        phoneInput = findViewById(R.id.phone_input);
        confirmButton = findViewById(R.id.confirm_button);
        createAccountButton = findViewById(R.id.create_account_button);
        phoneInput.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        confirmButton.setOnClickListener(v -> {
            String phoneNumber = phoneInput.getText().toString();
            if (phoneNumber.length() == 13) {
                Intent intent = new Intent(SignInPhoneNumActivity.this, SignInPWActivity.class);
                intent.putExtra("phoneNumber", phoneNumber); // 전화번호 전달
                startActivity(intent);
            } else {
                Toast.makeText(SignInPhoneNumActivity.this, "전화번호를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        createAccountButton.setOnClickListener(view -> {
            Intent intent = new Intent(SignInPhoneNumActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private class PhoneNumberFormattingTextWatcher implements TextWatcher {

        private boolean isFormatting;
        private int start, before, count;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            this.start = start;
            this.before = count;
            this.count = after;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // 구현할 필요 없음
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (isFormatting) {
                return;
            }
            isFormatting = true;

            if (before == 0 && count == 1) { // 입력 중
                if (s.length() == 3 || s.length() == 8) {
                    s.append('-');
                } else if (s.length() > 13) {
                    s.delete(13, s.length());
                }
            } else if (before == 1 && count == 0) { // 삭제 중
                if (start == 3 || start == 8) {
                    s.delete(start - 1, start);
                }
            }

            isFormatting = false;

            // 전화번호 길이가 13자리가 되면 자동으로 다음 화면으로 이동
            if (s.length() == 13) {
                Intent intent = new Intent(SignInPhoneNumActivity.this, SignInPWActivity.class);
                intent.putExtra("phoneNumber", s.toString()); // 전화번호 전달
                startActivity(intent);
            }
        }
    }
}
