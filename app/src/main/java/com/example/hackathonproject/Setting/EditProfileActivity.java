package com.example.hackathonproject.Setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.hackathonproject.Login.ChangePasswordActivity;
import com.example.hackathonproject.Login.ForgotPasswordActivity;
import com.example.hackathonproject.Login.SessionManager;
import com.example.hackathonproject.Login.StartActivity;
import com.example.hackathonproject.R;
import com.example.hackathonproject.db.AuthManager;

import java.sql.SQLException;

public class EditProfileActivity extends AppCompatActivity {
    private EditText nameEditText;
    private AuthManager authManager;
    private SharedPreferences sharedPreferences;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // AuthManager와 SharedPreferences 초기화
        authManager = new AuthManager();
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        sessionManager = new SessionManager(this);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 기본 제목 숨기기
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 기본 뒤로 가기 버튼을 제거하고, XML에서 만든 버튼 사용
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // 뒤로 가기 버튼 기능 설정
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        // 툴바 제목 설정
        TextView titleTextView = findViewById(R.id.toolbar_title);
        titleTextView.setText(R.string.editProfileTitle);

        // EditText 초기화
        nameEditText = findViewById(R.id.name_edit_text);

        // 현재 사용자의 이름 불러오기
        loadUserName();

        // 저장 버튼 설정 및 사용자의 이름 업데이트
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> saveUserName());

        // 비밀번호 변경 버튼 설정
        Button changePasswordButton = findViewById(R.id.change_password_button);
        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, ForgotPasswordActivity.class);
            intent.putExtra("from_edit_profile", true);  // 데이터를 추가하여 전달
            startActivity(intent);
        });

        Button deleteAccountButton = findViewById(R.id.delete_account_button);
        deleteAccountButton.setOnClickListener(v -> deleteUserAccount());
    }

    private void deleteUserAccount() {
        new DeleteAccountTask().execute();
    }

    // SharedPreferences 또는 데이터베이스에서 현재 사용자의 이름을 불러옴
    private void loadUserName() {
        String currentName = sharedPreferences.getString("user_name", ""); // 기본값은 빈 문자열
        nameEditText.setText(currentName);
    }

    // 변경된 사용자 이름 저장
    private void saveUserName() {
        String newName = nameEditText.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 비동기 작업으로 이름 변경 처리
        new UpdateNameTask().execute(newName);
    }

    private class DeleteAccountTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            int userId = sessionManager.getUserId();
            if (userId == -1) {
                return false; // userId가 유효하지 않으면 false 반환
            }

            try {
                return authManager.deleteUserAccount(userId); // userId를 사용하여 계정 삭제
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(EditProfileActivity.this, "계정이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                sessionManager.logout(); // 로그아웃 처리
                Intent intent = new Intent(EditProfileActivity.this, StartActivity.class); // 로그인 화면으로 이동
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(EditProfileActivity.this, "계정 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 비동기 작업으로 이름 변경을 처리하는 클래스
    private class UpdateNameTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String newName = params[0];

            // SharedPreferences나 다른 방법을 통해 userId를 얻어와야 합니다.
            int userId = sessionManager.getUserId();
            Log.d("EditProfileActivity", "Retrieved userId: " + userId);
            if (userId == -1) {
                // userId가 유효하지 않으면 false 반환
                return false;
            }

            try {
                return authManager.changeUserName(userId, newName); // userId와 newName을 함께 전달
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(EditProfileActivity.this, "이름이 성공적으로 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("user_name", nameEditText.getText().toString().trim());
                editor.apply();
            } else {
                Toast.makeText(EditProfileActivity.this, "이름 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
