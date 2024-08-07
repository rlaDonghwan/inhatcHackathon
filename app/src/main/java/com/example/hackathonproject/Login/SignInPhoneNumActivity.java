package com.example.hackathonproject.Login;

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

import com.example.hackathonproject.R;

public class SignInPhoneNumActivity extends AppCompatActivity {
    //동환---------------------------------------------------------------------------------------------------------
    private EditText phoneInput; // 전화번호 입력 필드
    private Button confirmButton; // 확인 버튼
    private Button createAccountButton; // 계정 생성 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_phone_number); // 레이아웃 설정

        // Toolbar 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // 툴바 설정

        // 기본 타이틀 숨기기
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 툴바 타이틀 숨기기
        }

        // 뒤로가기 버튼 설정
        ImageButton backButton = findViewById(R.id.back_button); // 뒤로가기 버튼 찾기
        backButton.setOnClickListener(v -> onBackPressed()); // 뒤로가기 버튼 클릭 시 이전 화면으로 이동

        // 전화번호 입력과 확인 버튼 설정
        phoneInput = findViewById(R.id.phone_input); // 전화번호 입력 필드 찾기
        confirmButton = findViewById(R.id.confirm_button); // 확인 버튼 찾기
        createAccountButton = findViewById(R.id.create_account_button); // 계정 생성 버튼 찾기
        phoneInput.addTextChangedListener(new PhoneNumberFormattingTextWatcher()); // 전화번호 포맷팅 TextWatcher 추가

        confirmButton.setOnClickListener(v -> {
            String phoneNumber = phoneInput.getText().toString(); // 입력된 전화번호 가져오기
            if (phoneNumber.length() == 13) { // 전화번호가 13자리인 경우
                Intent intent = new Intent(SignInPhoneNumActivity.this, SignInPWActivity.class); // SignInPWActivity로 인텐트 생성
                intent.putExtra("phoneNumber", phoneNumber); // 전화번호 전달
                startActivity(intent); // SignInPWActivity 시작
            } else {
                Toast.makeText(SignInPhoneNumActivity.this, "전화번호를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show(); // 전화번호 오류 메시지 출력
            }
        });

        createAccountButton.setOnClickListener(view -> {
            Intent intent = new Intent(SignInPhoneNumActivity.this, SignUpActivity.class); // SignUpActivity로 인텐트 생성
            startActivity(intent); // SignUpActivity 시작
        });
    }

    private class PhoneNumberFormattingTextWatcher implements TextWatcher {

        private boolean isFormatting; // 포맷팅 여부
        private int start, before, count; // 텍스트 변경 정보

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            this.start = start; // 시작 위치 저장
            this.before = count; // 변경 전 문자 수 저장
            this.count = after; // 변경 후 문자 수 저장
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // 구현할 필요 없음
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (isFormatting) {
                return; // 포맷팅 중이면 리턴
            }
            isFormatting = true; // 포맷팅 시작

            if (before == 0 && count == 1) { // 입력 중
                if (s.length() == 3 || s.length() == 8) {
                    s.append('-'); // 특정 위치에 '-' 추가
                } else if (s.length() > 13) {
                    s.delete(13, s.length()); // 13자리 초과 시 삭제
                }
            } else if (before == 1 && count == 0) { // 삭제 중
                if (start == 3 || start == 8) {
                    s.delete(start - 1, start); // 특정 위치의 '-' 삭제
                }
            }

            isFormatting = false; // 포맷팅 종료

            // 전화번호 길이가 13자리가 되면 자동으로 다음 화면으로 이동
            if (s.length() == 13) {
                Intent intent = new Intent(SignInPhoneNumActivity.this, SignInPWActivity.class); // SignInPWActivity로 인텐트 생성
                intent.putExtra("phoneNumber", s.toString()); // 전화번호 전달
                startActivity(intent); // SignInPWActivity 시작
            }
        }
    }
    //동환---------------------------------------------------------------------------------------------------------
}
