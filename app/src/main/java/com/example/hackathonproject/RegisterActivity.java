//package com.example.hackathonproject;
//
//import android.content.Intent;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//import androidx.appcompat.app.AppCompatActivity;
//
//import java.sql.SQLException;
//
//public class RegisterActivity extends AppCompatActivity {
//
//    private static final String TAG = "RegisterActivity";
//
//    private EditText etName, etPassword, etPhoneNum, etAge;
//    private Button btnRegister;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_sign_up);
//
//        // 레이아웃에서 UI 요소를 찾고 변수에 할당
//        etName = findViewById(R.id.etName);
//        etPassword = findViewById(R.id.etPassword);
//        etPhoneNum = findViewById(R.id.etPhoneNum);
//        etAge = findViewById(R.id.etAge);
//        btnRegister = findViewById(R.id.btnRegister);
//
//        // 등록 버튼 클릭 리스너 설정
//        btnRegister.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "Register button clicked");
//
//                // EditText에서 사용자 입력 값을 가져옴
//                String name = etName.getText().toString();
//                String password = etPassword.getText().toString();
//                String phoneNum = etPhoneNum.getText().toString();
//                String age = etAge.getText().toString();
//
//                // 입력값이 비어 있는지 확인
//                if (name.isEmpty() || password.isEmpty() || phoneNum.isEmpty() || age.isEmpty()) {
//                    Toast.makeText(RegisterActivity.this, "모든 필드를 입력하세요", Toast.LENGTH_SHORT).show();
//                } else {
//                    // 모든 필드가 입력된 경우 비동기 작업 실행
//                    new RegisterUserTask().execute(name, password, phoneNum, age);
//                }
//            }
//        });
//    }
//
//    // 비동기 작업으로 데이터베이스에 사용자 등록을 처리하는 내부 클래스
//    private class RegisterUserTask extends AsyncTask<String, Void, Boolean> {
//        @Override
//        protected Boolean doInBackground(String... params) {
//            Log.d(TAG, "AsyncTask started");
//
//            String name = params[0];
//            String password = params[1];
//            String phoneNum = params[2];
//            String age = params[3];
//
//            try {
//                DatabaseHelper dbHelper = new DatabaseHelper();
//                dbHelper.registerUser(name, password, phoneNum, age);
//                Log.d(TAG, "User registration successful");
//                return true;
//            } catch (SQLException e) {
//                Log.e(TAG, "User registration failed: " + e.getMessage(), e);
//                return false;
//            } catch (Exception e) {
//                Log.e(TAG, "Unexpected error: " + e.getMessage(), e);
//                return false;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            Log.d(TAG, "onPostExecute started, result: " + result);
//            if (result) {
//                Toast.makeText(RegisterActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
//                // 회원가입 성공 시 로그인 페이지로 이동
//                Intent intent = new Intent(RegisterActivity.this, SignUpActivity.class);
//                Log.d(TAG, "Starting LoginActivity");
//                startActivity(intent);
//                finish(); // 현재 액티비티 종료
//            } else {
//                Toast.makeText(RegisterActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
//            }
//        }
//
//    }
//}
