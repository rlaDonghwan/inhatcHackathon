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
import android.widget.TextView;
import android.widget.Toast;

import com.example.hackathonproject.Setting.EditProfileActivity;
import com.example.hackathonproject.db.AuthManager;
import com.example.hackathonproject.R;

import java.sql.SQLException;

public class ForgotPasswordActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_SEND_SMS = 2323;
    private static final String TAG = "ForgotPasswordActivity";

    private EditText emailAddressTextController;
    private EditText textController;
    private Button sendCodeButton;
    private Button verifyCodeButton;
    private AuthManager authManager;
    private String generatedCode;
    private String phoneNumberToSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            // Intent로 전달된 from_edit_profile 값을 확인
            boolean fromEditProfile = getIntent().getBooleanExtra("from_edit_profile", false);

            if (fromEditProfile) {
                // EditProfileActivity에서 넘어온 경우 EditProfileActivity로 돌아가기
                Intent intent = new Intent(ForgotPasswordActivity.this, EditProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else {
                // 그렇지 않은 경우 SignInPasswordActivity로 돌아가기
                Intent intent = new Intent(ForgotPasswordActivity.this, SignInPasswordActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
            finish(); // 현재 Activity 종료
        });

        boolean fromEditProfile = getIntent().getBooleanExtra("from_edit_profile", false);

        TextView titleTextView = findViewById(R.id.titleTextView);
        if (fromEditProfile) {
            titleTextView.setText("비밀번호 재설정");
        } else {
            titleTextView.setText("비밀번호를 잊으셨나요?");
        }

        emailAddressTextController = findViewById(R.id.emailAddressTextController);
        textController = findViewById(R.id.textController);
        sendCodeButton = findViewById(R.id.sendCodeButton);
        verifyCodeButton = findViewById(R.id.verifyCodeButton);
        authManager = new AuthManager();  // AuthManager 초기화

        // TextWatcher를 사용하여 전화번호 입력 시 자동으로 '-' 추가
        emailAddressTextController.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private int prevLength;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!isFormatting) {
                    prevLength = s.length();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 이 메서드는 구현할 필요가 없습니다.
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;

                int length = s.length();
                if (prevLength < length) {
                    if (length == 4 || length == 9) {
                        s.insert(length - 1, "-");
                    } else if (length > 13) {
                        s.delete(length - 1, length);
                    }
                } else if (prevLength > length) {
                    if (length == 4 || length == 9) {
                        s.delete(length - 1, length);
                    }
                }

                isFormatting = false;
            }
        });

        sendCodeButton.setOnClickListener(v -> {
            String phoneNumber = emailAddressTextController.getText().toString();
            Log.d(TAG, "Phone number input: " + phoneNumber);
            if (phoneNumber.isEmpty() || phoneNumber.length() < 12) {
                Toast.makeText(ForgotPasswordActivity.this, "전화번호를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
            } else {
                phoneNumberToSend = phoneNumber;
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSIONS_REQUEST_SEND_SMS);
                } else {
                    new CheckUserExistenceTask().execute(phoneNumber);
                }
            }
        });

        verifyCodeButton.setOnClickListener(v -> {
            String code = textController.getText().toString();
            if (code.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "인증번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            } else {
                if (code.equals(generatedCode)) {
                    Toast.makeText(ForgotPasswordActivity.this, "인증되었습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ForgotPasswordActivity.this, ChangePasswordActivity.class);
                    intent.putExtra("phoneNumber", phoneNumberToSend);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "인증번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent;
        String previousActivity = getIntent().getStringExtra("previousActivity");

        if ("EditProfileActivity".equals(previousActivity)) {
            intent = new Intent(ForgotPasswordActivity.this, EditProfileActivity.class);
        } else {
            intent = new Intent(ForgotPasswordActivity.this, SignInPasswordActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();

        super.onBackPressed(); // 기본 동작을 호출하여 경고를 해결합니다.
    }



    private void sendSMSVerificationCode(String phoneNumber) {
        generatedCode = generateVerificationCode();
        SmsSend(phoneNumber, "인증번호: " + generatedCode);
        Toast.makeText(ForgotPasswordActivity.this, "인증번호가 발송되었습니다.", Toast.LENGTH_SHORT).show();
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

    private String generateVerificationCode() {
        int code = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }

    private class CheckUserExistenceTask extends AsyncTask<String, Void, Boolean> {
        private String phoneNumber;

        @Override
        protected Boolean doInBackground(String... params) {
            phoneNumber = params[0];
            Log.d(TAG, "Checking user existence for phone number: " + phoneNumber);
            try {
                return authManager.isUserExist(phoneNumber);  // AuthManager를 사용하여 사용자 존재 여부 확인
            } catch (SQLException e) {
                Log.e(TAG, "Failed to check user existence", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean userExists) {
            Log.d(TAG, "User existence result for phone number " + phoneNumber + ": " + userExists);
            if (userExists) {
                sendSMSVerificationCode(phoneNumber);
            } else {
                Toast.makeText(ForgotPasswordActivity.this, "가입되어 있지 않은 전화번호입니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
