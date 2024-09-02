package com.example.hackathonproject.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.hackathonproject.R;

public class SignInPhoneNumActivity extends AppCompatActivity {
    private EditText phoneInput;
    private Button confirmButton;
    private Button createAccountButton;
    private TextView firstTimeText;
    //-----------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_phone_number);

        // UI 요소 초기화
        phoneInput = findViewById(R.id.phone_input);
        confirmButton = findViewById(R.id.confirm_button);
        createAccountButton = findViewById(R.id.create_account_button);
        firstTimeText = findViewById(R.id.first_time_text);

        //---------------------------------------------------------------------------------------------
        // SharedPreferences에서 폰트 크기 불러오기
        SharedPreferences preferences = getSharedPreferences("fontSizePrefs", MODE_PRIVATE);
        int savedFontSize = preferences.getInt("fontSize", 25);  // 기본값 25

        // 불러온 폰트 크기를 UI 요소에 적용
        phoneInput.setTextSize(savedFontSize);
        confirmButton.setTextSize(savedFontSize);
        firstTimeText.setTextSize(savedFontSize);
        createAccountButton.setTextSize(savedFontSize);
        //---------------------------------------------------------------------------------------------


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        phoneInput.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        confirmButton.setOnClickListener(v -> {
            String phoneNumber = phoneInput.getText().toString();
            if (phoneNumber.length() == 13) {
                Intent intent = new Intent(SignInPhoneNumActivity.this, SignInPasswordActivity.class);
                intent.putExtra("phoneNumber", phoneNumber);
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
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onBackPressed() {
        // 백 버튼을 눌렀을 때 FontSizeActivity로 돌아가도록 처리
        Intent intent = new Intent(SignInPhoneNumActivity.this, FontSizeActivity.class);
        startActivity(intent);
        finish();  // 현재 액티비티 종료
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

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
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (isFormatting) {
                return;
            }
            isFormatting = true;

            if (before == 0 && count == 1) {
                if (s.length() == 3 || s.length() == 8) {
                    s.append('-');
                } else if (s.length() > 13) {
                    s.delete(13, s.length());
                }
            } else if (before == 1 && count == 0) {
                if (start == 3 || start == 8) {
                    s.delete(start - 1, start);
                }
            }

            isFormatting = false;

            if (s.length() == 13) {
                Intent intent = new Intent(SignInPhoneNumActivity.this, SignInPasswordActivity.class);
                intent.putExtra("phoneNumber", s.toString());
                startActivity(intent);
            }
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------
}
