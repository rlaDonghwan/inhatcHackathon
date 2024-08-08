package com.example.hackathonproject.Login;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.hackathonproject.DatabaseHelper;
import com.example.hackathonproject.R;

import java.sql.SQLException;

public class ForgotPasswordActivity extends AppCompatActivity {
    //동환---------------------------------------------------------------------------------------------------------
    private static final int PERMISSIONS_REQUEST_SEND_SMS = 2323; // SMS 권한 요청 코드
    private static final String TAG = "ForgotPasswordActivity"; // 디버깅을 위한 태그

    private EditText emailAddressTextController; // 전화번호 입력 필드
    private EditText textController; // 인증번호 입력 필드
    private Button sendCodeButton; // 인증번호 발송 버튼
    private Button verifyCodeButton; // 인증번호 확인 버튼 찾기
    private DatabaseHelper dbHelper; // DatabaseHelper 인스턴스
    private String generatedCode; // 생성된 인증번호
    private String phoneNumberToSend; // 인증번호를 보낼 전화번호

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
        dbHelper = new DatabaseHelper(); // DatabaseHelper 인스턴스 생성

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
            String phoneNumber = emailAddressTextController.getText().toString();
            Log.d(TAG, "Phone number input: " + phoneNumber);
            if (phoneNumber.isEmpty() || phoneNumber.length() < 12) { // 전화번호 길이 수정
                Toast.makeText(ForgotPasswordActivity.this, "전화번호를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show(); // 전화번호 미입력 시 메시지 출력
            } else {
                phoneNumberToSend = phoneNumber; // 전송할 전화번호 저장
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) { // 권한 확인
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSIONS_REQUEST_SEND_SMS); // 권한 요청
                } else {
                    new CheckUserExistenceTask().execute(phoneNumber); // 사용자 존재 여부 확인
                }
            }
        });

        // 인증번호 확인 로직
        verifyCodeButton.setOnClickListener(v -> {
            String code = textController.getText().toString(); // 입력된 인증번호 가져오기
            if (code.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "인증번호를 입력하세요.", Toast.LENGTH_SHORT).show(); // 인증번호 미입력 시 메시지 출력
            } else {
                // 인증번호 확인 로직
                if (code.equals(generatedCode)) {
                    Toast.makeText(ForgotPasswordActivity.this, "인증되었습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ForgotPasswordActivity.this, ChangePasswordActivity.class);
                    intent.putExtra("phoneNumber", phoneNumberToSend); // 전화번호 전달
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "인증번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void sendSMSVerificationCode(String phoneNumber) {
        // SMS 인증번호 발송 로직
        generatedCode = generateVerificationCode();
        SmsSend(phoneNumber, "인증번호: " + generatedCode);
        Toast.makeText(ForgotPasswordActivity.this, "인증번호가 발송되었습니다.", Toast.LENGTH_SHORT).show(); // 인증번호 발송 메시지 출력
    }

    public void SmsSend(String strPhoneNumber, String strMsg) {
        PendingIntent sendIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), PendingIntent.FLAG_IMMUTABLE);

        SmsManager smsManager = SmsManager.getDefault();
        try {
            Log.d(TAG, "Sending SMS to " + strPhoneNumber + ": " + strMsg);
            smsManager.sendTextMessage(strPhoneNumber, null, strMsg, sendIntent, deliveredIntent);
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to send SMS: " + ex.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 승인된 경우 사용자 존재 여부 확인
                new CheckUserExistenceTask().execute(phoneNumberToSend);
            } else {
                // 권한이 거부된 경우
                Toast.makeText(this, "SMS 전송 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String generateVerificationCode() {
        // 간단한 6자리 인증번호 생성
        int code = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }

    // 사용자 존재 여부 확인을 위한 AsyncTask
    private class CheckUserExistenceTask extends AsyncTask<String, Void, Boolean> {
        private String phoneNumber;

        @Override
        protected Boolean doInBackground(String... params) {
            phoneNumber = params[0];
            Log.d(TAG, "Checking user existence for phone number: " + phoneNumber);
            try {
                return dbHelper.isUserExist(phoneNumber); // 사용자가 존재하는지 확인
            } catch (SQLException e) {
                Log.e(TAG, "Failed to check user existence", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean userExists) {
            Log.d(TAG, "User existence result for phone number " + phoneNumber + ": " + userExists);
            if (userExists) {
                sendSMSVerificationCode(phoneNumber); // 사용자가 존재하면 인증번호 발송
            } else {
                Toast.makeText(ForgotPasswordActivity.this, "가입되어 있지 않은 전화번호입니다.", Toast.LENGTH_SHORT).show(); // 사용자 미존재 메시지 출력
            }
        }
    }
    //동환---------------------------------------------------------------------------------------------------------
}
