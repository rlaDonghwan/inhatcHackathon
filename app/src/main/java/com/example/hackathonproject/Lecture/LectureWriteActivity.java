package com.example.hackathonproject.Lecture;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathonproject.Login.SessionManager;
import com.example.hackathonproject.R;
import com.example.hackathonproject.db.LectureDAO;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class LectureWriteActivity extends AppCompatActivity {
    // UI 요소 선언
    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText priceEditText;
    private Button submitButton;
    private CheckBox checkBoxWant;
    private LectureDAO lectureDAO;
    private SessionManager sessionManager;
    private int lectureId = -1; // 수정 시 사용할 강연 ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_write);

        lectureDAO = new LectureDAO();
        sessionManager = new SessionManager(this);

        // UI 요소 초기화
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        titleEditText = findViewById(R.id.title_edit_text);
        descriptionEditText = findViewById(R.id.content_edit_text);
        priceEditText = findViewById(R.id.price_edit_text);
        submitButton = findViewById(R.id.btnSummit);
        checkBoxWant = findViewById(R.id.checkbox_want);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);

        // Intent로 전달된 데이터 처리 (수정 모드인지 확인)
        Intent intent = getIntent();
        if (intent.hasExtra("lectureId")) {
            lectureId = intent.getIntExtra("lectureId", -1);
            String title = intent.getStringExtra("title");
            String content = intent.getStringExtra("content");
            int fee = intent.getIntExtra("fee", 0);
            boolean isYouthAudienceAllowed = intent.getBooleanExtra("isYouthAudienceAllowed", false);

            titleEditText.setText(title);
            descriptionEditText.setText(content);
            priceEditText.setText(String.valueOf(fee));
            checkBoxWant.setChecked(isYouthAudienceAllowed); // 체크박스 상태 설정

            toolbarTitle.setText("강연 수정");

            submitButton.setOnClickListener(v -> updateLecture());
        } else {
            submitButton.setOnClickListener(v -> submitLecture());
            toolbarTitle.setText("강연자 구직");
        }
    }


    // 새 강연을 등록하는 메서드
    private void submitLecture() {
        String title = titleEditText.getText().toString().trim();
        String content = descriptionEditText.getText().toString().trim();
        String feeText = priceEditText.getText().toString().trim();
        double fee = feeText.isEmpty() ? 0 : Double.parseDouble(feeText);
        boolean isYouthAudienceAllowed = checkBoxWant.isChecked(); // 체크박스의 상태를 가져옴

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 모두 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        new SubmitLectureTask().execute(title, content, "서울", fee, isYouthAudienceAllowed);
    }

    // 기존 강연을 수정하는 메서드
    private void updateLecture() {
        String title = titleEditText.getText().toString().trim();
        String content = descriptionEditText.getText().toString().trim();
        String feeText = priceEditText.getText().toString().trim();
        double fee = feeText.isEmpty() ? 0 : Double.parseDouble(feeText);
        boolean isYouthAudienceAllowed = checkBoxWant.isChecked(); // 체크박스의 상태를 가져옴

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 모두 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        new UpdateLectureTask().execute(lectureId, title, content, "서울", fee, isYouthAudienceAllowed);
    }

    // 비동기 작업으로 새 강연을 데이터베이스에 등록하는 클래스
    private class SubmitLectureTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            String title = (String) params[0];
            String content = (String) params[1];
            String location = (String) params[2];
            double fee = (double) params[3];
            boolean isYouthAudienceAllowed = (boolean) params[4];
            int userId = sessionManager.getUserId();

            if (userId == -1) {
                Log.e("SubmitLectureTask", "Invalid user ID: " + userId);
                return false;
            }

            try {
                ZonedDateTime kstTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
                return lectureDAO.insertLecturePost(userId, title, content, location, fee, kstTime, isYouthAudienceAllowed);
            } catch (Exception e) {
                Log.e("SubmitLectureTask", "Error inserting lecture", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(LectureWriteActivity.this, "강연이 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(LectureWriteActivity.this, "강연 등록에 실패하였습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 비동기 작업으로 기존 강연을 데이터베이스에서 수정하는 클래스
    private class UpdateLectureTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            int lectureId = (int) params[0];
            String title = (String) params[1];
            String content = (String) params[2];
            String location = (String) params[3];
            double fee = (double) params[4];
            boolean isYouthAudienceAllowed = (boolean) params[5];
            int userId = sessionManager.getUserId();

            try {
                return lectureDAO.updateLecturePost(lectureId, title, content, location, fee, userId, isYouthAudienceAllowed);
            } catch (Exception e) {
                Log.e("UpdateLectureTask", "Error updating lecture", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(LectureWriteActivity.this, "강연이 성공적으로 수정되었습니다.", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(LectureWriteActivity.this, "강연 수정에 실패하였습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
