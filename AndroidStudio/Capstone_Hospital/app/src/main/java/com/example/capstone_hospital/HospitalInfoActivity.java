package com.example.capstone_hospital;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HospitalInfoActivity extends AppCompatActivity {

    Button btnBack;
    TextView tvDiseaseName;
    CheckBox bookmark;
    WebView webView;
    SQLiteDatabase sqlDB;
    MainActivity.MyDBHelper myHelper;
    String userId = "";
    String hospitalName = "";
    String web_url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_info);

        btnBack = (Button) findViewById(R.id.btnBack);
        tvDiseaseName = (TextView) findViewById(R.id.tvDiseaseName);
        bookmark = (CheckBox) findViewById(R.id.bookmark);
        webView = (WebView) findViewById(R.id.webView);

        if(getIntent().getStringExtra("selectedBookmark") != null) {
            String[] itemSplit = getIntent().getStringExtra("selectedBookmark").split("\\|");
            hospitalName = itemSplit[1];
            web_url = itemSplit[2];
        }
        else {
            hospitalName = getIntent().getStringExtra("hospitalName");
            web_url = getIntent().getStringExtra("url");
        }

        tvDiseaseName.setText(hospitalName);

        myHelper = new MainActivity.MyDBHelper(getApplicationContext(), "capstone", null, 1);
        sqlDB = myHelper.getReadableDatabase();

        SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
        userId = sharedPref.getString("userId", "");

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String sql = "select bookmark from member where id ='" + userId + "'";
        Log.d("SQL", sql);
        Cursor cursor = sqlDB.rawQuery(sql, null);

        String memberBookmark = "";
        if (cursor.moveToFirst()) {
            memberBookmark = cursor.getString(0);
        }
        cursor.close();

        if(memberBookmark.contains("1|" + hospitalName + "|" + web_url)) {
            bookmark.setChecked(true);
        }

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        String[] split = web_url.split("/place/");
        String[] split2 = split[1].split("\\?c=");
        String mobile_url = "https://m.place.naver.com/hospital/" + split2[0] + "/home";
        Log.d("mobile_url", mobile_url);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(mobile_url);
                return true;
            }

        });
        webView.loadUrl(mobile_url);

        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bookmark.isChecked()) {
                    Toast.makeText(getApplicationContext(), "즐겨찾기에 추가되었습니다.", Toast.LENGTH_SHORT).show();
                    updateBookmark();
                }
                else {
                    Toast.makeText(getApplicationContext(), "즐겨찾기에서 제거되었습니다.", Toast.LENGTH_SHORT).show();
                    removeBookmark();
                }
            }
        });
    }

    private void updateBookmark() {
        String bookmark = 1 + "|" + hospitalName + "|" + web_url;

        Cursor cursor = sqlDB.rawQuery("SELECT bookmark FROM member WHERE id = ?", new String[]{userId});
        String currentBookmark = "";
        if (cursor.moveToFirst()) {
            currentBookmark = cursor.getString(0);
            if (currentBookmark == null) {
                currentBookmark = "";
            }
        }
        cursor.close();

        String updatedBookmark = currentBookmark.isEmpty() ? bookmark : bookmark + "~" + currentBookmark;

        sqlDB.execSQL("UPDATE member SET bookmark = ? WHERE id = ?", new Object[]{updatedBookmark, userId});

        // 서버에 POST 요청 보내기
        new Thread(() -> {
            try {
                URL url = new URL("https://kse.calab.myds.me/update_member.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String columnName = "bookmark";
                String postData = "columnName=" + URLEncoder.encode(columnName, "UTF-8") +
                        "&userId=" + URLEncoder.encode(userId, "UTF-8") +
                        "&item=" + URLEncoder.encode(updatedBookmark, "UTF-8");

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    Log.d("SERVER_RESPONSE", response.toString());
                } else {
                    Log.e("SERVER_ERROR", "Response code: " + responseCode);
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e("HTTP_ERROR", e.getMessage());
            }
        }).start();
    }

    private void removeBookmark() {
        String bookmarkToRemove = 1 + "|" + hospitalName + "|" + web_url;

        Cursor cursor = sqlDB.rawQuery("SELECT bookmark FROM member WHERE id = ?", new String[]{userId});
        String currentBookmark = "";
        if (cursor.moveToFirst()) {
            currentBookmark = cursor.getString(0);
        }
        cursor.close();

        String removedBookmark = currentBookmark.replace(bookmarkToRemove, "").replaceAll("~~", "~").trim();

        if (removedBookmark.startsWith("~")) {
            removedBookmark = removedBookmark.substring(1);
        }
        if (removedBookmark.endsWith("~")) {
            removedBookmark = removedBookmark.substring(0, removedBookmark.length() - 1);
        }

        if (removedBookmark == null || removedBookmark.isEmpty()) {
            removedBookmark = "";
        }

        sqlDB.execSQL("UPDATE member SET bookmark = ? WHERE id = ?", new Object[]{removedBookmark, userId});

        // 서버에 POST 요청 보내기
        String finalRemovedBookmark = removedBookmark;
        new Thread(() -> {
            try {
                URL url = new URL("https://kse.calab.myds.me/update_member.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String columnName = "bookmark";
                String postData = "columnName=" + URLEncoder.encode(columnName, "UTF-8") +
                        "&userId=" + URLEncoder.encode(userId, "UTF-8") +
                        "&item=" + URLEncoder.encode(finalRemovedBookmark, "UTF-8");

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    Log.d("SERVER_RESPONSE", response.toString());
                } else {
                    Log.e("SERVER_ERROR", "Response code: " + responseCode);
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e("HTTP_ERROR", e.getMessage());
            }
        }).start();
    }
}