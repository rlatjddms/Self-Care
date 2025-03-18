package com.example.capstone_hospital;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Fragment1 fragment1;
    Fragment2 fragment2;
    Fragment3 fragment3;
    Fragment4 fragment4;
    SQLiteDatabase sqlDB;
    MyDBHelper myHelper;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
        boolean autoLogin = sharedPref.getBoolean("autoLogin", false);
        boolean isLoggedIn = getIntent().getBooleanExtra("isLoggedIn", false);

        if (!autoLogin && !isLoggedIn) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        fragment1 = new Fragment1();
        fragment2 = new Fragment2();
        fragment3 = new Fragment3();
        fragment4 = new Fragment4();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment1).commit();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();
                if(itemId == R.id.home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment1).commit();
                }
                if(itemId == R.id.disease) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment2).commit();
                }
                if(itemId == R.id.hospital) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment3).commit();
                }
                if(itemId == R.id.myPage) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment4).commit();
                }
                return true;
            }
        });

        myHelper = new MainActivity.MyDBHelper(this, "capstone", null, 1);
        sqlDB = myHelper.getWritableDatabase();
        myHelper.onUpgrade(sqlDB,1,2);
        sqlDB.close();

        final String url_disease = "https://kse.calab.myds.me/select_disease.php";
        final String url_member = "https://kse.calab.myds.me/select_member.php";
        final String url_hospital = "https://kse.calab.myds.me/select_hospital.php";
        final String url_question = "https://kse.calab.myds.me/select_question.php";
        new Thread(new Runnable() {
            @Override
            public void run() {
                requestDisease(url_disease);
                requestMember(url_member);
                requestHospital(url_hospital);
                requestQuestion(url_question);
            }
        }).start();
    }

    public static class MyDBHelper extends SQLiteOpenHelper {

        public MyDBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE disease (diseaseName varchar(200) primary key, diagnosis text, definition text, " +
                    "symptom text, cause text, management text, medicalSubject varchar(50), category text);");
            Log.d("MyDBHelper", "disease table created");

            db.execSQL("CREATE TABLE member (id varchar(20) primary key, pw varchar(20), name varchar(20), " +
                    "gender varchar(20), birth varchar(20), phone varchar(20), email varchar(50), address varchar(50), " +
                    "recentSearch text, medicalHistory text, bookmark text);");
            Log.d("MyDBHelper", "member table created");

            db.execSQL("CREATE TABLE hospital (no int(255) primary key, hospitalName varchar(50), " +
                    "medicalSubject text, address varchar(50), phone varchar(20), url text);");
            Log.d("MyDBHelper", "hospital table created");

            db.execSQL("CREATE TABLE question (no int(50) primary key, question text, answer text, result text, next varchar(100));");
            Log.d("MyDBHelper", "question table created");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists disease");
            db.execSQL("drop table if exists member");
            db.execSQL("drop table if exists hospital");
            db.execSQL("drop table if exists question");
            onCreate(db);
        }
    }

    public void requestDisease(String url_disease) {
        StringBuilder output = new StringBuilder();

        try {
            URL url = new URL(url_disease);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection != null) {
                connection.setConnectTimeout(10000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                int resCode = connection.getResponseCode();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));

                String line = null;
                while (true) {
                    line = reader.readLine();
                    if (line == null) break;
                    output.append(line + "\n");
                }
                reader.close();
                connection.disconnect();
            }
        } catch (Exception e) {
            Log.d("에러 발생 : ", e.toString());
        }
        println(output.toString());
    }

    public void requestMember(String url_member) {
        StringBuilder output = new StringBuilder();

        try {
            URL url = new URL(url_member);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection != null) {
                connection.setConnectTimeout(10000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                int resCode = connection.getResponseCode();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));

                String line = null;
                while (true) {
                    line = reader.readLine();
                    if (line == null) break;
                    output.append(line + "\n");
                }
                reader.close();
                connection.disconnect();
            }
        } catch (Exception e) {
            println("에러 발생 : " + e.toString());
        }
        println(output.toString());
    }

    public void requestHospital(String url_hospital) {
        StringBuilder output = new StringBuilder();

        try {
            URL url = new URL(url_hospital);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection != null) {
                connection.setConnectTimeout(10000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                int resCode = connection.getResponseCode();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));

                String line = null;
                while (true) {
                    line = reader.readLine();
                    if (line == null) break;
                    output.append(line + "\n");
                }
                reader.close();
                connection.disconnect();
            }
        } catch (Exception e) {
            println("에러 발생 : " + e.toString());
        }
        println(output.toString());
    }

    public void requestQuestion(String url_question) {
        StringBuilder output = new StringBuilder();

        try {
            URL url = new URL(url_question);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection != null) {
                connection.setConnectTimeout(10000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                int resCode = connection.getResponseCode();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));

                String line = null;
                while (true) {
                    line = reader.readLine();
                    if (line == null) break;
                    output.append(line + "\n");
                }
                reader.close();
                connection.disconnect();
            }
        } catch (Exception e) {
            println("에러 발생 : " + e.toString());
        }
        println(output.toString());
    }

    public void println(final String data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                jsonParsing_disease(data);
                jsonParsing_member(data);
                jsonParsing_hospital(data);
                jsonParsing_question(data);
            }
        });
    }

    private void jsonParsing_disease(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("disease");
            sqlDB = myHelper.getWritableDatabase();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject diseaseObject = jsonArray.getJSONObject(i);

                String diseaseName = diseaseObject.getString("diseaseName");
                String diagnosis = diseaseObject.getString("diagnosis");
                String definition = diseaseObject.getString("definition");
                String symptom = diseaseObject.getString("symptom");
                String cause = diseaseObject.getString("cause");
                String management = diseaseObject.getString("management");
                String medicalSubject = diseaseObject.getString("medicalSubject");
                String category = diseaseObject.getString("category");

                ContentValues values = new ContentValues();
                values.put("diseaseName", diseaseName);
                values.put("diagnosis", diagnosis);
                values.put("definition", definition);
                values.put("symptom", symptom);
                values.put("cause", cause);
                values.put("management", management);
                values.put("medicalSubject", medicalSubject);
                values.put("category", category);

                sqlDB.insert("disease", null, values);
            }
            sqlDB.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void jsonParsing_member(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("member");
            sqlDB = myHelper.getWritableDatabase();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject diseaseObject = jsonArray.getJSONObject(i);

                String id = diseaseObject.getString("id");
                String pw = diseaseObject.getString("pw");
                String name = diseaseObject.getString("name");
                String gender = diseaseObject.getString("gender");
                String birth = diseaseObject.getString("birth");
                String phone = diseaseObject.getString("phone");
                String email = diseaseObject.getString("email");
                String address = diseaseObject.getString("address");
                String recentSearch = diseaseObject.getString("recentSearch");
                String medicalHistory = diseaseObject.getString("medicalHistory");
                String bookmark = diseaseObject.getString("bookmark");

                ContentValues values = new ContentValues();
                values.put("id", id);
                values.put("pw", pw);
                values.put("name", name);
                values.put("gender", gender);
                values.put("birth", birth);
                values.put("phone", phone);
                values.put("email", email);
                values.put("address", address);
                values.put("recentSearch", recentSearch);
                values.put("medicalHistory", medicalHistory);
                values.put("bookmark", bookmark);

                sqlDB.insert("member", null, values);
            }
            sqlDB.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void jsonParsing_hospital(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("hospital");
            sqlDB = myHelper.getWritableDatabase();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject diseaseObject = jsonArray.getJSONObject(i);

                int no = diseaseObject.getInt("no");
                String hospitalName = diseaseObject.getString("hospitalName");
                String medicalSubject = diseaseObject.getString("medicalSubject");
                String address = diseaseObject.getString("address");
                String phone = diseaseObject.getString("phone");
                String url = diseaseObject.getString("url");

                ContentValues values = new ContentValues();
                values.put("no", no);
                values.put("hospitalName", hospitalName);
                values.put("medicalSubject", medicalSubject);
                values.put("address", address);
                values.put("phone", phone);
                values.put("url", url);

                sqlDB.insert("hospital", null, values);
            }
            sqlDB.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void jsonParsing_question(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("question");
            sqlDB = myHelper.getWritableDatabase();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject diseaseObject = jsonArray.getJSONObject(i);

                int no = diseaseObject.getInt("no");
                String question = diseaseObject.getString("question");
                String answer = diseaseObject.getString("answer");
                String result = diseaseObject.getString("result");
                String next = diseaseObject.getString("next");

                ContentValues values = new ContentValues();
                values.put("no", no);
                values.put("question", question);
                values.put("answer", answer);
                values.put("result", result);
                values.put("next", next);

                sqlDB.insert("question", null, values);
            }
            sqlDB.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}