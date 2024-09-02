package com.example.hackathonproject.Lecture;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.hackathonproject.Login.SessionManager;
import com.example.hackathonproject.R;
import com.example.hackathonproject.db.LectureDAO;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

public class LectureWriteActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationProviderClient fusedLocationClient;
    private String currentLocation = "인천"; // 기본 위치는 서울로 설정

    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText priceEditText;
    private Button submitButton;
    private CheckBox checkBoxWant;
    private LectureDAO lectureDAO;
    private SessionManager sessionManager;
    private int lectureId = -1; // 수정 시 사용할 강연 ID
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView selectedImageView;
    private byte[] imageBytes;

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
        selectedImageView = findViewById(R.id.selected_image_view);

        // 이미지 프리뷰 클릭 시 이미지 선택 창 열기
        selectedImageView.setOnClickListener(v -> openImageChooser());

        // 위치 권한 요청 및 위치 정보 가져오기
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastKnownLocation();
        }

        // Intent로 전달된 데이터 처리 (수정 모드인지 확인)
        Intent intent = getIntent();
        if (intent.hasExtra("lectureId")) {
            lectureId = intent.getIntExtra("lectureId", -1);
            String title = intent.getStringExtra("title");
            String content = intent.getStringExtra("content");
            String location = intent.getStringExtra("location");
            int fee = intent.getIntExtra("fee", 0);
            boolean isYouthAudienceAllowed = intent.getBooleanExtra("isYouthAudienceAllowed", false);
            byte[] imageData = intent.getByteArrayExtra("imageData");

            titleEditText.setText(title);
            descriptionEditText.setText(content);
            priceEditText.setText(String.valueOf(fee));
            checkBoxWant.setChecked(isYouthAudienceAllowed);

            if (imageData != null) {
                Glide.with(this)
                        .load(imageData)
                        .into(selectedImageView);  // 이미지 미리보기 설정
                selectedImageView.setVisibility(View.VISIBLE);
                imageBytes = imageData;  // 이미지 데이터를 변수에 저장하여 업데이트에 사용
            }

            toolbarTitle.setText("강연 수정");
            submitButton.setOnClickListener(v -> updateLecture());
        } else {
            toolbarTitle.setText("강연자 구직");
            submitButton.setOnClickListener(v -> submitLecture());
        }
    }


    private void getLastKnownLocation() {
        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            currentLocation = getAddressFromLocation(location); // 좌표를 주소로 변환하여 저장
                        } else {
                            // 위치를 가져올 수 없을 경우 IP 주소 기반 위치로 대체
                            new GetLocationFromIPTask().execute();
                        }
                    }
                });
    }

    // 좌표를 주소로 변환하는 메서드
    private String getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                // 주소에서 구 단위만 추출
                String subLocality = address.getSubLocality(); // '학익동' 같은 동 단위
                String locality = address.getLocality(); // '미추홀구' 같은 구 단위

                if (locality != null) {
                    return locality;
                } else if (subLocality != null) {
                    return subLocality;
                } else {
                    return "인천"; // locality와 subLocality 모두 null일 경우 기본값
                }
            } else {
                return "인천"; // 주소를 찾을 수 없을 경우 기본값
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "인천"; // 예외가 발생했을 경우 기본값
        }
    }

    // IP 주소를 통해 위치를 가져오는 비동기 작업
    private class GetLocationFromIPTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String location = "서울";
            try {
                URL url = new URL("https://ipinfo.io/json");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                InputStream inputStream = urlConnection.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                int read;
                byte[] buffer = new byte[1024];
                while ((read = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, read);
                }
                String jsonResponse = byteArrayOutputStream.toString();

                JSONObject jsonObject = new JSONObject(jsonResponse);
                String loc = jsonObject.getString("loc");
                String[] latLng = loc.split(",");
                double latitude = Double.parseDouble(latLng[0]);
                double longitude = Double.parseDouble(latLng[1]);

                return getAddressFromLocation(new Location("IP") {{
                    setLatitude(latitude);
                    setLongitude(longitude);
                }});
            } catch (Exception e) {
                Log.e("IP Location Error", "Error getting location from IP", e);
            }
            return location;
        }

        @Override
        protected void onPostExecute(String location) {
            currentLocation = location;
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            selectedImageView.setVisibility(View.VISIBLE);
            selectedImageView.setImageURI(imageUri);

            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                imageBytes = getBytes(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    // 새 강연을 등록하는 메서드
    private void submitLecture() {
        String title = titleEditText.getText().toString().trim();
        String content = descriptionEditText.getText().toString().trim();
        String feeText = priceEditText.getText().toString().trim();
        double fee = feeText.isEmpty() ? 0 : Double.parseDouble(feeText);
        boolean isYouthAudienceAllowed = checkBoxWant.isChecked();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 모두 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        new SubmitLectureTask().execute(title, content, currentLocation, fee, isYouthAudienceAllowed, imageBytes);
    }

    // 기존 강연을 수정하는 메서드
    private void updateLecture() {
        String title = titleEditText.getText().toString().trim();
        String content = descriptionEditText.getText().toString().trim();
        String feeText = priceEditText.getText().toString().trim();
        double fee = feeText.isEmpty() ? 0 : Double.parseDouble(feeText);
        boolean isYouthAudienceAllowed = checkBoxWant.isChecked();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 모두 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        new UpdateLectureTask().execute(lectureId, title, content, currentLocation, fee, isYouthAudienceAllowed, imageBytes);
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
            byte[] imageData = (byte[]) params[5];
            int userId = sessionManager.getUserId();

            if (userId == -1) {
                Log.e("SubmitLectureTask", "Invalid user ID: " + userId);
                return false;
            }

            try {
                ZonedDateTime kstTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
                return lectureDAO.submitLectureWithImage(title, content, location, fee, userId, kstTime, isYouthAudienceAllowed, imageData);
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
            byte[] imageData = (byte[]) params[6];
            int userId = sessionManager.getUserId();

            // 데이터베이스 업데이트 호출
            return lectureDAO.updateLectureWithImage(lectureId, title, content, location, fee, userId, isYouthAudienceAllowed, imageData);
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