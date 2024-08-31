package com.example.hackathonproject.Login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathonproject.R;

public class CertificationActivity extends AppCompatActivity {

    private EditText schoolNameInput;
    private EditText companyNameInput;
    private Button schoolCertifyButton;
    private Button companyCertifyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_certification);

        // View 초기화
        schoolNameInput = findViewById(R.id.school_name_input);
        companyNameInput = findViewById(R.id.company_name_input);
        schoolCertifyButton = findViewById(R.id.school_certify_button);
        companyCertifyButton = findViewById(R.id.company_certify_button);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        // SignUpActivity에서 넘어온 Intent 데이터 받기
        boolean isSchoolChecked = getIntent().getBooleanExtra("isSchool", false);
        boolean isCompanyChecked = getIntent().getBooleanExtra("isOrganization", false);

        // 초기 상태 설정
        schoolNameInput.setEnabled(isSchoolChecked);
        schoolCertifyButton.setEnabled(isSchoolChecked);
        companyNameInput.setEnabled(isCompanyChecked);
        companyCertifyButton.setEnabled(isCompanyChecked);

        // 뒤로 가기 버튼 처리
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(CertificationActivity.this, SignUpActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // 뒤로 가기 버튼을 누르면 SignUpActivity로 이동
        Intent intent = new Intent(CertificationActivity.this, SignUpActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}