package com.example.hackathonproject.Login;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.hackathonproject.DatabaseHelper;
import com.example.hackathonproject.R;

import java.sql.SQLException;

public class SignUpActivity extends AppCompatActivity {
    //동환---------------------------------------------------------------------------------------------------------
    private static final String TAG = "SignUpActivity";
    private EditText etName, etPassword, etPhoneNum, etBirthYear, etMonth, etDay;
    private CheckBox cbIsOrganization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // 뒤로가기 버튼 설정
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 기본 타이틀 숨기기
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 텍스트뷰 설정
        TextView termsText = findViewById(R.id.terms_text);
        String terms = "\"가입하기\" 클릭 시 디지털 손주 이용권한에 동의하는 것으로 간주됩니다.";

        SpannableString spannableString = new SpannableString(terms);

        // "이용권한" 부분을 밑줄과 볼드체로 설정
        int start = terms.indexOf("이용권한");
        int end = start + "이용권한".length();

        spannableString.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        termsText.setText(spannableString);
        termsText.setTypeface(Typeface.DEFAULT); // 전체 텍스트를 기본 폰트로 설정

        // 레이아웃에서 UI 요소를 찾고 변수에 할당
        etName = findViewById(R.id.full_name_input);
        etPassword = findViewById(R.id.password_input);
        etPhoneNum = findViewById(R.id.phone_number_input);
        etBirthYear = findViewById(R.id.birthYear_input);
        etMonth = findViewById(R.id.month_input);
        etDay = findViewById(R.id.day_input);
        cbIsOrganization = findViewById(R.id.checkbox);
        Button btnRegister = findViewById(R.id.sign_up_button);

        // 전화번호 입력 시 자동으로 하이픈 추가 및 최대 길이 13자리로 설정
        etPhoneNum.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        etPhoneNum.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private int lastStart;
            private int lastLength;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (isFormatting) {
                    return;
                }
                lastStart = start;
                lastLength = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isFormatting) {
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) {
                    return;
                }

                isFormatting = true;
                String formatted = formatPhoneNumber(s.toString());
                s.replace(0, s.length(), formatted);
                isFormatting = false;
            }

            private String formatPhoneNumber(String number) {
                number = number.replaceAll("[^\\d]", "");
                int length = number.length();

                if (length > 3 && length <= 7) {
                    number = number.substring(0, 3) + "-" + number.substring(3);
                } else if (length > 7) {
                    number = number.substring(0, 3) + "-" + number.substring(3, 7) + "-" + number.substring(7);
                }

                return number;
            }
        });

        // 출생년도, 월, 일 입력 시 최대 길이 설정 및 자동으로 다음 필드로 이동
        etBirthYear.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        etBirthYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 4) {
                    etMonth.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etMonth.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        etMonth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2) {
                    etDay.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etDay.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        etDay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2) {
                    // 마지막 입력 필드이므로 추가 동작 필요 없음
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // 등록 버튼 클릭 리스너 설정
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Register button clicked");

                // EditText에서 사용자 입력 값을 가져옴
                String name = etName.getText().toString();
                String password = etPassword.getText().toString();
                String phoneNum = etPhoneNum.getText().toString();
                String birthYear = etBirthYear.getText().toString();
                String month = etMonth.getText().toString();
                String day = etDay.getText().toString();
                boolean isOrganization = cbIsOrganization.isChecked();

                // 입력값이 비어 있는지 확인
                if (name.isEmpty() || password.isEmpty() || phoneNum.isEmpty() || birthYear.isEmpty() || month.isEmpty() || day.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "모든 필드를 입력하세요", Toast.LENGTH_SHORT).show();
                } else {
                    // 유효한 월과 일인지 확인
                    if (!isValidDate(month, day)) {
                        Toast.makeText(SignUpActivity.this, "유효한 월과 일을 입력하세요", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // 생년월일 형식으로 변환
                    String birthDate = birthYear + month + day;
                    // 모든 필드가 입력된 경우 비동기 작업 실행
                    new RegisterUserTask().execute(name, password, phoneNum, birthDate, String.valueOf(isOrganization));
                }
            }
        });
    }

    // 유효한 월과 일인지 확인하는 메서드
    private boolean isValidDate(String month, String day) {
        int monthInt, dayInt;
        try {
            monthInt = Integer.parseInt(month);
            dayInt = Integer.parseInt(day);
        } catch (NumberFormatException e) {
            return false;
        }

        if (monthInt < 1 || monthInt > 12) {
            return false;
        }

        if (dayInt < 1 || dayInt > 31) {
            return false;
        }

        // 2월의 경우 29일까지 허용
        if (monthInt == 2 && dayInt > 29) {
            return false;
        }

        // 4월, 6월, 9월, 11월의 경우 30일까지 허용
        if ((monthInt == 4 || monthInt == 6 || monthInt == 9 || monthInt == 11) && dayInt > 30) {
            return false;
        }

        return true;
    }

    // 비동기 작업으로 데이터베이스에 사용자 등록을 처리하는 내부 클래스
    private class RegisterUserTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(TAG, "AsyncTask started");

            String name = params[0];
            String password = params[1];
            String phoneNum = params[2];
            String birthDate = params[3];
            boolean isOrganization = Boolean.parseBoolean(params[4]);

            try {
                DatabaseHelper dbHelper = new DatabaseHelper();

                // 사용자가 이미 존재하는지 확인
                if (dbHelper.isUserExist(phoneNum)) {
                    Log.d(TAG, "User already exists with phone number: " + phoneNum);
                    return false; // 이미 존재하는 사용자
                }

                dbHelper.registerUser(name, password, phoneNum, birthDate, isOrganization);
                Log.d(TAG, "User registration successful");
                return true;
            } catch (SQLException e) {
                Log.e(TAG, "User registration failed: " + e.getMessage(), e);
                return false;
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error: " + e.getMessage(), e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(TAG, "onPostExecute started, result: " + result);
            if (result) {
                Toast.makeText(SignUpActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                // 회원가입 성공 시 로그인 페이지로 이동
                Intent intent = new Intent(SignUpActivity.this, SignInPhoneNumActivity.class);
                Log.d(TAG, "Starting LoginActivity");
                startActivity(intent);
                finish(); // 현재 액티비티 종료
            } else {
                Toast.makeText(SignUpActivity.this, "회원가입 실패: 이미 존재하는 사용자", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //동환---------------------------------------------------------------------------------------------------------
}
