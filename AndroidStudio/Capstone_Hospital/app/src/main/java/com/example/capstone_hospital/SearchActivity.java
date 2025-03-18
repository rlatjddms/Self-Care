package com.example.capstone_hospital;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    Button btnBack, btnSearch;
    RadioButton rbDisease, rbSymptom;
    RadioGroup radioGroup;
    TextView tvDelete, delete;
    EditText etSearch;
    ListView listView, searchList;
    LinearLayout searchLayout, listLayout;
    SQLiteDatabase sqlDB;
    MainActivity.MyDBHelper myHelper;
    ArrayList<String> recentSearch = new ArrayList<>();
    ArrayAdapter<String> adapter; // 최근 검색어
    DiseaseListView listAdapter; // 질병 리스트
    ArrayList<DiseaseListData> filteredData = new ArrayList<>();
    ArrayList<DiseaseListData> listViewData = new ArrayList<>();
    ArrayList<String> diseaseSymptomList = new ArrayList<>();
    String userId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        btnBack = (Button) findViewById(R.id.btnBack);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        delete = (TextView) findViewById(R.id.delete);
        rbDisease = (RadioButton) findViewById(R.id.rbDisease);
        rbSymptom = (RadioButton) findViewById(R.id.rbSymptom);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        tvDelete = (TextView) findViewById(R.id.tvDelete);
        etSearch = (EditText) findViewById(R.id.etSearch);
        listView = (ListView) findViewById(R.id.listView);
        searchList = (ListView) findViewById(R.id.searchList);
        searchLayout = (LinearLayout) findViewById(R.id.searchLayout);
        listLayout = (LinearLayout) findViewById(R.id.listLayout);

        SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
        userId = sharedPref.getString("userId", "");

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listView.setVisibility(View.INVISIBLE);

        myHelper = new MainActivity.MyDBHelper(getApplicationContext(), "capstone", null, 1);
        sqlDB = myHelper.getReadableDatabase();

        String sql = "select * from disease";
        Log.d("SQL", sql);
        Cursor cursor = sqlDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            DiseaseListData listData = new DiseaseListData();

            String diseaseName = cursor.getString(0);
            String symptomName = cursor.getString(1);

            diseaseSymptomList.add(diseaseName + ":" + symptomName);

            listData.diseaseName = diseaseName;
            listViewData.add(listData);
            filteredData.add(listData);
        }
        cursor.close();

        listAdapter = new DiseaseListView(filteredData);
        listView.setAdapter(listAdapter);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(rbDisease.isChecked()) {
                    etSearch.setHint("  질병을 검색해주세요.");
                }
                else if(rbSymptom.isChecked()) {
                    etSearch.setHint("  증상을 검색해주세요.");
                }
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchText = editable.toString();

                if (rbDisease.isChecked()) {
                    // 질병 검색 로직
                    filterDiseaseData(searchText);
                } else if (rbSymptom.isChecked()) {
                    // 증상 검색 로직
                    filterSymptomData(searchText);
                }
            }
        });

        tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                builder.setMessage("전체 기록을 삭제하시겠습니까?")
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteSearchUpdate();
                                onResume();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                // AlertDialog 생성 및 보여주기
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setText(null);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DiseaseListData selectedDisease = (DiseaseListData) parent.getAdapter().getItem(position);
                String diseaseName = selectedDisease.diseaseName;
                recentSearchUpdate(diseaseName);

                Intent intent = new Intent(getApplicationContext(), DiseaseActivity.class);
                intent.putExtra("diseaseName", diseaseName);
                startActivity(intent);
            }
        });

        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String recentSearch = searchList.getItemAtPosition(position).toString();

                Intent intent = new Intent(getApplicationContext(), DiseaseActivity.class);
                intent.putExtra("diseaseName", recentSearch);
                startActivity(intent);
            }
        });
    }

    private void filterDiseaseData(String searchText) {
        filteredData.clear();

        if (searchText.isEmpty()) {
            filteredData.addAll(listViewData);
            listView.setVisibility(View.INVISIBLE);
            searchLayout.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            searchLayout.setVisibility(View.INVISIBLE);
            for (DiseaseListData item : listViewData) {
                if (item.getDiseaseName().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredData.add(item);
                }
            }
        }
        listAdapter.notifyDataSetChanged();
    }

    // 증상 검색 필터링 로직
    private void filterSymptomData(String searchText) {
        filteredData.clear();

        if (searchText.isEmpty()) {
            listView.setVisibility(View.INVISIBLE);
            searchLayout.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            searchLayout.setVisibility(View.INVISIBLE);

            // 입력된 증상을 ','로 분리하여 리스트로 변환
            String[] symptoms = searchText.split(",\\s*");

            // 증상과 일치하는 질병을 찾아서 필터링
            for (String diseaseSymptom : diseaseSymptomList) {
                String[] parts = diseaseSymptom.split(":");
                if (parts.length < 2) {
                    // parts 배열이 2개 미만인 경우 건너뛰기
                    continue;
                }
                String diseaseName = parts[0];
                String symptomInfo = parts[1];

                int matchCount = 0;
                for (String symptom : symptoms) {
                    if (symptomInfo.toLowerCase().contains(symptom.toLowerCase())) {
                        matchCount++;
                    }
                }

                // 증상 일치도가 높을수록 리스트에 추가
                if (matchCount > 0) {
                    DiseaseListData data = new DiseaseListData();
                    data.diseaseName = diseaseName;
                    data.matchCount = "(" + matchCount + "개 일치)"; // 일치 개수를 저장

                    filteredData.add(data);  // DiseaseListData 객체를 추가
                }
            }

            // 일치 개수 순으로 정렬
            filteredData.sort((a, b) -> {
                // 문자열에서 숫자 부분만 추출해서 정수로 변환
                int matchCountA = Integer.parseInt(a.matchCount.replaceAll("[^0-9]", ""));
                int matchCountB = Integer.parseInt(b.matchCount.replaceAll("[^0-9]", ""));

                // 내림차순 정렬
                return Integer.compare(matchCountB, matchCountA);
            });
        }
        listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sql = "SELECT recentSearch FROM member WHERE id='" + userId + "'";
        Log.d("SQL", sql);
        Cursor cursor = sqlDB.rawQuery(sql, null);

        String search = "";
        if (cursor.moveToFirst()) {
            search = cursor.getString(0);
        }
        cursor.close();

        String[] searchSplit = search.split("/");
        recentSearch = new ArrayList<>(Arrays.asList(searchSplit));
        recentSearch.removeIf(String::isEmpty);

        if(recentSearch.isEmpty()) {
            tvDelete.setVisibility(View.INVISIBLE);
        }
        else {
            tvDelete.setVisibility(View.VISIBLE);
        }

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, recentSearch);
        searchList.setAdapter(adapter);
    }

    // 최근 검색어
    private void recentSearchUpdate(String diseaseName) {
        SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
        String userId = sharedPref.getString("userId", "");
        Log.d("userId", userId);

        Cursor cursor = sqlDB.rawQuery("SELECT recentSearch FROM member WHERE id = ?", new String[]{userId});
        String currentSearch = "";
        if (cursor.moveToFirst()) {
            currentSearch = cursor.getString(0);
        }
        cursor.close();

        ArrayList<String> searchList = new ArrayList<>(Arrays.asList(currentSearch.split("/")));
        searchList.removeIf(String::isEmpty);
        searchList.remove(diseaseName);

        searchList.add(0, diseaseName);
        String updatedSearch = String.join("/", searchList);

        sqlDB.execSQL("UPDATE member SET recentSearch = ? WHERE id = ?", new Object[]{updatedSearch, userId});

        // 서버에 POST 요청 보내기
        new Thread(() -> {
            try {
                URL url = new URL("https://kse.calab.myds.me/update_member.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String columnName = "recentSearch";
                String postData = "columnName=" + URLEncoder.encode(columnName, "UTF-8") +
                        "&userId=" + URLEncoder.encode(userId, "UTF-8") +
                        "&item=" + URLEncoder.encode(updatedSearch, "UTF-8");

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


    private void deleteSearchUpdate() {
        SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
        String userId = sharedPref.getString("userId", "");
        Log.d("userId", userId);

        sqlDB.execSQL("UPDATE member SET recentSearch = ? WHERE id = ?", new Object[]{"", userId});

        // 서버에 POST 요청 보내기
        new Thread(() -> {
            try {
                URL url = new URL("https://kse.calab.myds.me/update_member.php"); // PHP 파일의 URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String columnName = "recentSearch";
                String postData = "columnName=" + URLEncoder.encode(columnName, "UTF-8") +
                        "&userId=" + URLEncoder.encode(userId, "UTF-8") +
                        "&item=" + URLEncoder.encode("", "UTF-8");

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                // 서버 응답 처리
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
                    Log.d("SERVER_RESPONSE", response.toString()); // 서버 응답 로그
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