package com.example.hackathonproject;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailAddressTextController; // 전화번호 입력 필드
    private EditText textController; // 인증번호 입력 필드
    private Button sendCodeButton; // 인증번호 발송 버튼
    private Button verifyCodeButton; // 인증번호 확인 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password); // 레이아웃 설정

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // 툴바 설정

        // 기본 타이틀 숨기기
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 툴바 타이틀 숨기기
        }

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            // 이전 화면으로 돌아가기
            Intent intent = new Intent(ForgotPasswordActivity.this, SignInPWActivity.class); // 인텐트 생성
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // 인텐트 플래그 설정
            startActivity(intent); // 액티비티 시작
            finish(); // 현재 액티비티 종료
        });

        emailAddressTextController = findViewById(R.id.emailAddressTextController); // 전화번호 입력 필드 찾기
        textController = findViewById(R.id.textController); // 인증번호 입력 필드 찾기
        sendCodeButton = findViewById(R.id.sendCodeButton); // 인증번호 발송 버튼 찾기
        verifyCodeButton = findViewById(R.id.verifyCodeButton); // 인증번호 확인 버튼 찾기

        // 전화번호 입력 시 자동으로 '-' 기호 추가 및 최대 13자리 제한
        emailAddressTextController.addTextChangedListener(new TextWatcher() {
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
            }
        });

        sendCodeButton.setOnClickListener(v -> {
            String phoneNumber = emailAddressTextController.getText().toString(); // 입력된 전화번호 가져오기
            if (phoneNumber.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "전화번호를 입력하세요.", Toast.LENGTH_SHORT).show(); // 전화번호 미입력 시 메시지 출력
            } else {
                // 인증번호 발송 로직
                checkIfUserExists(phoneNumber); // 사용자 존재 여부 확인
            }
        });

        verifyCodeButton.setOnClickListener(v -> {
            String code = textController.getText().toString(); // 입력된 인증번호 가져오기
            if (code.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "인증번호를 입력하세요.", Toast.LENGTH_SHORT).show(); // 인증번호 미입력 시 메시지 출력
            } else {
                // 인증번호 확인 로직
                Toast.makeText(ForgotPasswordActivity.this, "인증되었습니다.", Toast.LENGTH_SHORT).show(); // 인증 완료 메시지 출력
            }
        });
    }

    private void checkIfUserExists(String phoneNumber) {
        // 여기에 DB에서 사용자 존재 여부를 확인하는 로직을 구현하세요.
        // 예시로 사용자 존재 여부를 확인하고 SMS 인증번호를 보내는 로직을 추가합니다.
        boolean userExists = true; // 예시로 사용자 존재 여부를 true로 설정

        if (userExists) {
            sendSMSVerificationCode(phoneNumber); // 사용자 존재 시 인증번호 발송
        } else {
            Toast.makeText(ForgotPasswordActivity.this, "가입되어 있지 않은 전화번호입니다.", Toast.LENGTH_SHORT).show(); // 사용자 미존재 메시지 출력
        }
    }

    private void sendSMSVerificationCode(String phoneNumber) {
        // 여기에 SMS 인증번호를 보내는 로직을 구현하세요.
        Toast.makeText(ForgotPasswordActivity.this, "인증번호가 발송되었습니다.", Toast.LENGTH_SHORT).show(); // 인증번호 발송 메시지 출력
    }
}
