package com.example.hackathonproject.Seminar;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathonproject.Login.SessionManager;
import com.example.hackathonproject.R;
import com.example.hackathonproject.db.SeminarDAO;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SeminarWriteActivity extends AppCompatActivity {
    // UI 요소 선언
    private EditText titleEditText;
    private EditText contentEditText;
    private EditText priceEditText;
    private SeminarDAO seminarDAO;
    private SessionManager sessionManager;
    private int lectureId = -1; // 수정 시 사용할 강연 ID (새 강연 작성 시에는 -1로 초기화)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seminor_write);

        // DAO와 SessionManager 초기화
        seminarDAO = new SeminarDAO();
        sessionManager = new SessionManager(this);

        // UI 요소 초기화
        titleEditText = findViewById(R.id.title_edit_text);
        contentEditText = findViewById(R.id.content_edit_text);
        priceEditText = findViewById(R.id.price_edit_text);

        // 뒤로 가기 버튼 설정
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        // Intent로 전달된 데이터를 처리하여 수정 모드인지 확인
        Intent intent = getIntent();
        if (intent.hasExtra("lectureId")) {
            // 강연 수정 모드 설정
            lectureId = intent.getIntExtra("lectureId", -1);  // 강연 ID 가져오기
            String title = intent.getStringExtra("title");  // 제목 가져오기
            String content = intent.getStringExtra("content");  // 내용 가져오기
            String fee = intent.getStringExtra("fee");  // 강연료 가져오기

            // UI 요소에 기존 강연 정보 설정
            titleEditText.setText(title);  // 제목 설정
            contentEditText.setText(content);  // 내용 설정
            priceEditText.setText(fee);  // 강연료 설정

            // 수정 버튼 클릭 시 업데이트 처리
            Button submitButton = findViewById(R.id.btnSummit);
            submitButton.setOnClickListener(v -> updateLecture());  // 수정 버튼 클릭 시 처리
        } else {
            // 새 강연 작성 모드 설정
            Button submitButton = findViewById(R.id.btnSummit);
            submitButton.setOnClickListener(v -> submitLecture());  // 제출 버튼 클릭 시 처리
        }
    }

    // 새 강연을 등록하는 메서드
    private void submitLecture() {
        // UI 요소에서 입력값 가져오기
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();
        String feeText = priceEditText.getText().toString().trim();
        double fee = feeText.isEmpty() ? 0 : Double.parseDouble(feeText);

        // 필수 입력값 확인
        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 모두 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 비동기 작업으로 강연 등록
        new SubmitLectureTask().execute(title, content, "서울", fee);  // 위치는 임의로 "서울"로 설정
    }

    // 기존 강연을 수정하는 메서드
    private void updateLecture() {
        // UI 요소에서 입력값 가져오기
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();
        String price_edit_text = priceEditText.getText().toString().trim();
        double fee = price_edit_text.isEmpty() ? 0 : Double.parseDouble(price_edit_text);

        // 필수 입력값 확인
        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 모두 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 비동기 작업으로 강연 수정
        new UpdateLectureTask().execute(lectureId, title, content, "서울", fee);
    }

    // 비동기 작업으로 새 강연을 데이터베이스에 등록하는 클래스
    private class SubmitLectureTask extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            // 파라미터로부터 데이터 추출
            String title = (String) params[0];
            String content = (String) params[1];
            String location = (String) params[2];
            double fee = (double) params[3];
            int userId = sessionManager.getUserId(); // SessionManager를 통해 사용자 ID 가져오기

            // 사용자 ID가 유효하지 않은 경우 실패 처리
            if (userId == -1) {
                Log.e("SubmitLectureTask", "Invalid user ID: " + userId);
                return false;
            }

            try {
                // 현재 시간을 한국 표준시(KST)로 변환
                ZonedDateTime kstTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
                Log.d("SubmitLectureTask", "KST Time: " + kstTime);

                // 강연 데이터를 데이터베이스에 삽입
                return seminarDAO.insertSeminarPost(userId, title, content, location, fee, kstTime);
            } catch (Exception e) {
                Log.e("SubmitLectureTask", "Error inserting lecture", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            // 강연 등록 성공 여부에 따른 UI 처리
            if (success) {
                Toast.makeText(SeminarWriteActivity.this, "강연이 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();  // 성공 메시지 표시
                setResult(RESULT_OK);  // 결과 설정
                finish();  // 액티비티 종료
            } else {
                Toast.makeText(SeminarWriteActivity.this, "강연 등록에 실패하였습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();  // 실패 메시지 표시
            }
        }
    }

    // 비동기 작업으로 기존 강연을 데이터베이스에서 수정하는 클래스
    private class UpdateLectureTask extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            // 파라미터로부터 데이터 추출
            int lectureId = (int) params[0];
            String title = (String) params[1];
            String content = (String) params[2];
            String location = (String) params[3];
            double fee = (double) params[4];
            int userId = sessionManager.getUserId(); // SessionManager를 통해 사용자 ID 가져오기

            try {
                // 강연 데이터를 데이터베이스에서 업데이트
                return seminarDAO.updateSeminarPost(lectureId, title, content, location, fee, userId);
            } catch (Exception e) {
                Log.e("UpdateLectureTask", "Error updating lecture", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            // 강연 수정 성공 여부에 따른 UI 처리
            if (success) {
                Toast.makeText(SeminarWriteActivity.this, "강연이 성공적으로 수정되었습니다.", Toast.LENGTH_SHORT).show();  // 성공 메시지 표시
                setResult(RESULT_OK);  // 결과 설정
                finish();  // 액티비티 종료
            } else {
                Toast.makeText(SeminarWriteActivity.this, "강연 수정에 실패하였습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();  // 실패 메시지 표시
            }
        }
    }
}
