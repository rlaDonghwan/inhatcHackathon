package com.example.hackathonproject.Setting;

import android.content.DialogInterface;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.hackathonproject.Login.SessionManager;
import com.example.hackathonproject.Login.StartActivity;
import com.example.hackathonproject.R;
import com.example.hackathonproject.db.AuthManager;

import java.sql.SQLException;

public class EditProfileActivity extends AppCompatActivity {
    private AuthManager authManager;
    private SharedPreferences sharedPreferences;
    SessionManager sessionManager;
    private EditText nameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_edit_profile);

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

        // ---------------------------------------------------------------------------------------------
        // SharedPreferences에서 폰트 크기 불러오기
        SharedPreferences preferences = getSharedPreferences("fontSizePrefs", MODE_PRIVATE);
        int savedFontSize = preferences.getInt("fontSize", 25);  // 기본값 25

        // 불러온 폰트 크기를 UI 요소에 적용
        nameEditText.setTextSize(savedFontSize);
        titleTextView.setTextSize(savedFontSize);

        // 추가적인 UI 요소에 대한 폰트 크기 적용
        TextView nameLabel = findViewById(R.id.name_label); // 이름 레이블
        TextView infoLabel = findViewById(R.id.info_label); // 정보 레이블
        TextView passwordChangeLabel = findViewById(R.id.password_change_label); // 비밀번호 변경 레이블
        TextView passwordChangeInfo = findViewById(R.id.password_change_info); // 비밀번호 변경 정보
        TextView deleteAccountLabel = findViewById(R.id.delete_account_label); // 계정 삭제 레이블
        TextView deleteAccountInfo = findViewById(R.id.delete_account_info); // 계정 삭제 정보
        Button saveButton = findViewById(R.id.save_button); // 저장 버튼
        Button changePasswordButton = findViewById(R.id.change_password_button); // 비밀번호 변경 버튼
        Button deleteAccountButton = findViewById(R.id.delete_account_button); // 계정 삭제 버튼

        // 폰트 크기 적용
        nameLabel.setTextSize(savedFontSize);
        infoLabel.setTextSize(savedFontSize);
        passwordChangeLabel.setTextSize(savedFontSize);
        passwordChangeInfo.setTextSize(savedFontSize);
        deleteAccountLabel.setTextSize(savedFontSize);
        deleteAccountInfo.setTextSize(savedFontSize);
        saveButton.setTextSize(savedFontSize);
        changePasswordButton.setTextSize(savedFontSize);
        deleteAccountButton.setTextSize(savedFontSize);
        // ---------------------------------------------------------------------------------------------

        // 현재 사용자의 이름 불러오기
        loadUserName();

        // 저장 버튼 설정 및 사용자의 이름 업데이트
        saveButton.setOnClickListener(v -> confirmAndSaveUserName());

        // 비밀번호 변경 버튼 설정
        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, ForgotPasswordActivity.class);
            intent.putExtra("from_edit_profile", true);  // 데이터를 추가하여 전달
            startActivity(intent);
        });

        deleteAccountButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    // 계정 삭제 경고 다이얼로그 표시
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this).setTitle("계정 삭제").setMessage("계정을 삭제하면 모든 내용이 삭제됩니다. 계속하시겠습니까?").setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deleteUserAccount();
            }
        }).setNegativeButton("취소", null).show();
    }

    // 계정 삭제를 비동기적으로 처리
    private void deleteUserAccount() {
        new DeleteAccountTask().execute();
    }

    private void loadUserName() {
        // Try retrieving the name from SessionManager first
        String currentName = sessionManager.getUserName();

        if (currentName == null || currentName.isEmpty()) {
            // If SessionManager doesn't have the name, fallback to SharedPreferences
            currentName = sharedPreferences.getString("user_name", ""); // 기본값은 빈 문자열
        }

        Log.d("EditProfileActivity", "Loaded user name: " + currentName);
        nameEditText.setText(currentName);
    }

    // 변경된 사용자 이름 저장 전에 경고 메시지 표시
    private void confirmAndSaveUserName() {
        String newName = nameEditText.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 경고 다이얼로그 표시
        new AlertDialog.Builder(this).setTitle("이름 변경").setMessage("이름을 변경하시면 자동으로 로그아웃됩니다. 계속하시겠습니까?").setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // 이름 변경 수행
                new UpdateNameTask().execute(newName);
            }
        }).setNegativeButton("취소", null).show();
    }

    // 계정 삭제를 처리하는 비동기 작업 클래스
    private class DeleteAccountTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            int userId = sessionManager.getUserId();
            if (userId == -1) {
                return false; // userId가 유효하지 않으면 false 반환
            }

            try {
                // 교육 게시글 삭제
                Log.d("DeleteAccountTask", "Attempting to delete user education posts");
                boolean educationDeleted = authManager.deleteUserEducationPosts(userId);
                Log.d("DeleteAccountTask", "Education posts deleted: " + educationDeleted);

                // 강연 게시글 삭제
                Log.d("DeleteAccountTask", "Attempting to delete user lecture posts");
                boolean lectureDeleted = authManager.deleteUserLecturePosts(userId);
                Log.d("DeleteAccountTask", "Lecture posts deleted: " + lectureDeleted);

                // 채팅 내역 삭제
                Log.d("DeleteAccountTask", "Attempting to delete user chats");
                boolean chatsDeleted = authManager.deleteUserChats(userId);
                Log.d("DeleteAccountTask", "Chats deleted: " + chatsDeleted);

                // 채팅방 삭제
                Log.d("DeleteAccountTask", "Attempting to delete user chat rooms");
                boolean chatRoomsDeleted = authManager.deleteUserChatRooms(userId);
                Log.d("DeleteAccountTask", "Chat rooms deleted: " + chatRoomsDeleted);

                // Even if there were no posts, chats, or chat rooms, proceed with account deletion
                Log.d("DeleteAccountTask", "Attempting to delete user account");
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
                Toast.makeText(EditProfileActivity.this, "이름이 성공적으로 업데이트되었습니다. 로그아웃됩니다.", Toast.LENGTH_SHORT).show();

                // 세션 파기 및 로그아웃 처리
                sessionManager.logout(); // 로그아웃 처리
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear(); // SharedPreferences 초기화
                editor.apply();

                // 전화번호 입력 화면으로 이동
                Intent intent = new Intent(EditProfileActivity.this, StartActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(EditProfileActivity.this, "이름 업데이트에 실패했습니다.", Toast.LENGTH_SHORT);
            }
        }
    }
}