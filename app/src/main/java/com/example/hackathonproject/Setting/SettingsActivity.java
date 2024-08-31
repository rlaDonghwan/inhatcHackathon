package com.example.hackathonproject.Setting;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathonproject.Chat.ChatListActivity;
import com.example.hackathonproject.Education.EducationActivity;
import com.example.hackathonproject.Login.SessionManager;
import com.example.hackathonproject.Login.StartActivity;
import com.example.hackathonproject.R;
import com.example.hackathonproject.Lecture.LectureActivity;
import com.example.hackathonproject.db.AuthManager;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;

public class SettingsActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private AuthManager authManager;
    private static final int PICK_IMAGE = 1;
    private ImageView profileImageView;
    private TextView businessOrSchoolTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // 세션 매니저 초기화
        sessionManager = new SessionManager(this);
        authManager = new AuthManager();

        // UI 요소 초기화
        profileImageView = findViewById(R.id.profile_image);
        TextView profileNameTextView = findViewById(R.id.question);
        TextView balanceTextView = findViewById(R.id.title_time); // balance 값을 표시할 텍스트뷰
        businessOrSchoolTextView = findViewById(R.id.business_or_school); // TextView for business/school name
        ImageView authorizationIcon = findViewById(R.id.authorization_icon);

        // 세션에서 사용자 정보 가져오기
        String userName = sessionManager.getUserName();
        int balance = sessionManager.getBalance(); // Balance 값을 가져옴
        int userId = sessionManager.getUserId();
        String role = sessionManager.getUserRole(); // 역할 가져오기

        // 프로필 이름과 봉사 시간, Balance를 설정
        profileNameTextView.setText(userName);
        balanceTextView.setText(String.valueOf(balance)); // Balance 값을 설정

        // 사용자 기관 여부에 따라 인증 마크 가시성 설정
        if (sessionManager.isUserOrganization()) {
            authorizationIcon.setVisibility(View.VISIBLE);
        } else {
            authorizationIcon.setVisibility(View.GONE);
        }

        // Load and display the business or school name
        new LoadBusinessOrSchoolNameTask(userId, role).execute();

        // 프로필 이미지를 비동기로 로드
        new LoadProfileImageTask(userId).execute();

        // 옵션 클릭 리스너 설정
        LinearLayout editProfileOption = findViewById(R.id.option_edit_profile);
        LinearLayout aboutB1A3Option = findViewById(R.id.option_about_b1a3);
        LinearLayout logoutOption = findViewById(R.id.logout_option);

        editProfileOption.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        aboutB1A3Option.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, SettingFontSizeActivity.class);
            startActivity(intent);
        });

        logoutOption.setOnClickListener(v -> {
            sessionManager.clearSession();  // 세션 정보 제거
            Intent intent = new Intent(SettingsActivity.this, StartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();  // 현재 액티비티 종료
        });

        // 메뉴 클릭 리스너 설정
        LinearLayout firstMenuItem = findViewById(R.id.first_menu_item);
        firstMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, EducationActivity.class);
            startActivity(intent);
        });

        LinearLayout secondMenuItem = findViewById(R.id.second_menu_item);
        secondMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, LectureActivity.class);
            startActivity(intent);
        });

        LinearLayout thirdMenuItem = findViewById(R.id.third_menu_item);
        thirdMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ChatListActivity.class);
            startActivity(intent);
        });

        LinearLayout fourthMenuItem = findViewById(R.id.fourth_menu_item);
        fourthMenuItem.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // 프로필 이미지 클릭 리스너 설정
        profileImageView.setOnClickListener(v -> {
            // 갤러리에서 이미지 선택
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData(); // 선택한 이미지의 URI 가져오기

            // 이미지를 내부 저장소에 저장
            String imagePath = saveImageToInternalStorage(imageUri);

            // DB에 이미지 경로를 저장 (비동기 처리)
            int userId = sessionManager.getUserId();  // 사용자 ID 가져오기
            new UpdateProfileImageTask(userId, imagePath).execute();  // AsyncTask 실행

            // 이미지 뷰에 이미지를 설정
            profileImageView.setImageURI(imageUri);
        }
    }

    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            // 이미지 URI에서 비트맵 가져오기
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            // 내부 저장소 경로 가져오기
            String filename = "profile_image_" + System.currentTimeMillis() + ".jpg";
            File directory = getFilesDir(); // 내부 저장소 디렉토리
            File imageFile = new File(directory, filename);

            // 파일 출력 스트림 생성
            FileOutputStream fos = new FileOutputStream(imageFile);

            // 비트맵을 JPEG 형식으로 압축하여 파일에 저장
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            // 스트림 닫기
            fos.close();

            // 이미지 경로 반환
            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 비동기 작업을 수행하는 내부 클래스
    private class UpdateProfileImageTask extends AsyncTask<Void, Void, Boolean> {
        private int userId;
        private String imagePath;

        public UpdateProfileImageTask(int userId, String imagePath) {
            this.userId = userId;
            this.imagePath = imagePath;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                return authManager.updateProfileImage(userId, imagePath);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Log.d("SettingsActivity", "Profile image path updated successfully in DB");
            } else {
                Log.e("SettingsActivity", "Failed to update profile image path in DB");
            }
        }
    }

    private class LoadProfileImageTask extends AsyncTask<Void, Void, String> {
        private int userId;

        public LoadProfileImageTask(int userId) {
            this.userId = userId;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                String imagePath = authManager.getProfileImagePath(userId);
                return imagePath;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String imagePath) {
            if (imagePath != null && !imagePath.isEmpty()) {
                if (imagePath.equals("/path/to/default/image.jpg")) {
                    // 기본 이미지 경로일 경우 리소스에서 이미지 로드
                    profileImageView.setImageResource(R.drawable.default_profile_image);
                } else {
                    File imgFile = new File(imagePath);
                    if (imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        profileImageView.setImageBitmap(myBitmap);
                    } else {
                        Log.e("SettingsActivity", "Failed to load image from " + imagePath);
                    }
                }
            } else {
                // 프로필 이미지가 설정되지 않았을 경우 기본 이미지 로드
                profileImageView.setImageResource(R.drawable.default_profile_image);
            }
        }
    }

    // Task to load and display business or school name
    private class LoadBusinessOrSchoolNameTask extends AsyncTask<Void, Void, String> {
        private int userId;
        private String role;

        public LoadBusinessOrSchoolNameTask(int userId, String role) {
            this.userId = userId;
            this.role = role;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                if (role.equals("기관")) {
                    return authManager.getBusinessNameByUserId(userId);
                } else if (role.equals("학교")) {
                    return authManager.getSchoolNameByUserId(userId);
                }
                return null;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String name) {
            if (name != null && !name.isEmpty()) {
                businessOrSchoolTextView.setText(name);
                businessOrSchoolTextView.setVisibility(View.VISIBLE);
            } else {
                businessOrSchoolTextView.setVisibility(View.GONE);
            }
        }
    }

}