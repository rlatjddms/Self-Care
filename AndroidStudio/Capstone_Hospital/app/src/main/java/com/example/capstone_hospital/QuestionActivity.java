package com.example.capstone_hospital;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;

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

public class QuestionActivity extends AppCompatActivity {

    Button btnBack, btnFirst, btnPrevious, btnGo, btnHome, btnSave;
    TextView symptomName, tvQuestion, tvResult;
    LinearLayout questionLayout, resultLayout;
    ListView listView;
    TextView[] answers = new TextView[7];
    SQLiteDatabase sqlDB;
    MainActivity.MyDBHelper myHelper;
    ArrayList<String> answerList = new ArrayList<>();
    ArrayList<String> nextNum = new ArrayList<>();
    ArrayList<String> resultList = new ArrayList<>();
    ArrayList<Integer> questionNumList = new ArrayList<>();
    String question = "";
    String answer = "";
    String result = "";
    String next = "";
    String selectedAnswer = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        btnBack = (Button) findViewById(R.id.btnBack);
        btnFirst = (Button) findViewById(R.id.btnFirst);
        btnPrevious = (Button) findViewById(R.id.btnPrevious);
        btnGo = (Button) findViewById(R.id.btnGo);
        btnHome = (Button) findViewById(R.id.btnHome);
        btnSave = (Button) findViewById(R.id.btnSave);
        symptomName = (TextView) findViewById(R.id.symptomName);
        tvQuestion = (TextView) findViewById(R.id.tvQuestion);
        tvResult = (TextView) findViewById(R.id.tvResult);
        questionLayout = (LinearLayout) findViewById(R.id.questionLayout);
        resultLayout = (LinearLayout) findViewById(R.id.resultLayout);
        listView = (ListView) findViewById(R.id.listView);

        answers[0] = (TextView) findViewById(R.id.chip1);
        answers[1] = (TextView) findViewById(R.id.chip2);
        answers[2] = (TextView) findViewById(R.id.chip3);
        answers[3] = (TextView) findViewById(R.id.chip4);
        answers[4] = (TextView) findViewById(R.id.chip5);
        answers[5] = (TextView) findViewById(R.id.chip6);
        answers[6] = (TextView) findViewById(R.id.chip7);

        myHelper = new MainActivity.MyDBHelper(getApplicationContext(), "capstone", null, 1);
        sqlDB = myHelper.getReadableDatabase();

        int questionNum = getIntent().getIntExtra("questionNum", 0);
        String name = getIntent().getStringExtra("symptomName");
        symptomName.setText(name);

        resultLayout.setVisibility(View.INVISIBLE);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        questionNumList.add(questionNum);
        loadQuestionFromDB(questionNum);

        // TextView 클릭 이벤트 설정
        for (int i = 0; i < answers.length; i++) {
            final int index = i;
            answers[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleAnswerClick(index);
                }
            });
        }

        btnFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionNumList.clear();
                questionNumList.add(questionNum);
                loadQuestionFromDB(questionNum);
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (questionNumList.size() > 1) {
                    questionNumList.remove(questionNumList.size() - 1);
                    loadQuestionFromDB(questionNumList.get(questionNumList.size() - 1));
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                medicalHistoryUpdate();
                Toast.makeText(getApplicationContext(), "진료내역에 저장되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // DB에서 질문 및 답변 데이터를 불러오는 함수
    private void loadQuestionFromDB(int questionNum) {
        questionLayout.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.INVISIBLE);

        String sql = "SELECT * FROM question WHERE no=" + questionNum;
        Log.d("SQL", sql);
        Cursor cursor = sqlDB.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            question = cursor.getString(1);
            answer = cursor.getString(2);
            result = cursor.getString(3);
            next = cursor.getString(4);
        }
        cursor.close();

        tvQuestion.setText(question);

        String[] answerSplit = answer.split("/");
        answerList = new ArrayList<>(Arrays.asList(answerSplit));
        answerList.removeIf(String::isEmpty);

        String[] nextSplit = next.split("/");
        nextNum = new ArrayList<>(Arrays.asList(nextSplit));
        nextNum.removeIf(String::isEmpty);

        for (int i = 0; i < answers.length; i++) {
            if (i < answerList.size()) {
                answers[i].setText(answerList.get(i));
                answers[i].setVisibility(View.VISIBLE);
            } else {
                answers[i].setVisibility(View.INVISIBLE);
            }
        }

        if (questionNumList.size() == 1) {
            btnFirst.setVisibility(View.INVISIBLE);
            btnPrevious.setVisibility(View.INVISIBLE);
        } else {
            btnFirst.setVisibility(View.VISIBLE);
            btnPrevious.setVisibility(View.VISIBLE);
        }
        btnHome.setVisibility(View.INVISIBLE);
    }

    // chip 클릭 핸들러
    private void handleAnswerClick(int index) {
        if (index < answerList.size()) {
            selectedAnswer = nextNum.get(index);
            Log.d("SelectedAnswer", selectedAnswer);
            int nextQuestionNum = Integer.parseInt(nextNum.get(index));
            questionNumList.add(nextQuestionNum);

            String[] resultSplit = result.split("/");
            resultList = new ArrayList<>(Arrays.asList(resultSplit));
            resultList.removeIf(String::isEmpty);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (resultList.isEmpty()) {
                        loadQuestionFromDB(nextQuestionNum);
                        questionLayout.setVisibility(View.VISIBLE);
                        resultLayout.setVisibility(View.INVISIBLE);
                    } else {
                        if (resultList.get(0).equals("3")) {
                            String[] split = resultList.get(Integer.parseInt(selectedAnswer)).split("\\|");
                            resultList = new ArrayList<>(Arrays.asList(split));
                            resultList.removeIf(String::isEmpty);

                            questionLayout.setVisibility(View.INVISIBLE);
                            resultLayout.setVisibility(View.VISIBLE);
                            btnFirst.setVisibility(View.VISIBLE);
                            btnPrevious.setVisibility(View.VISIBLE);
                            btnHome.setVisibility(View.VISIBLE);
                            btnSave.setVisibility(View.INVISIBLE);

                            handleResultList(resultList);
                        } else {
                            if (selectedAnswer.equals("0")) {
                                // 답변이 0인 경우 결과를 보여줌
                                questionLayout.setVisibility(View.INVISIBLE);
                                resultLayout.setVisibility(View.VISIBLE);
                                btnFirst.setVisibility(View.VISIBLE);
                                btnPrevious.setVisibility(View.VISIBLE);
                                btnHome.setVisibility(View.VISIBLE);

                                handleResultList(resultList);
                            } else {
                                // 답변이 0이 아닌 경우 다음 질문을 불러옴
                                loadQuestionFromDB(nextQuestionNum);
                                questionLayout.setVisibility(View.VISIBLE);
                                resultLayout.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                }
            }, 400);
        }
    }

    // 결과 띄우기
    private void handleResultList(ArrayList<String> resultList) {
        String index = resultList.get(0);

        switch (index) {
            case "0":
                tvResult.setText(resultList.get(1));
                btnGo.setText(resultList.get(2) + " 설문으로 바로가기");
                btnGo.setVisibility(View.VISIBLE);
                listView.setVisibility(View.INVISIBLE);
                btnSave.setVisibility(View.INVISIBLE);

                btnGo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadQuestionFromDB(Integer.parseInt(resultList.get(3)));
                        symptomName.setText(resultList.get(2));
                    }
                });
                break;
            case "1":
                tvResult.setText(resultList.get(1));
                btnGo.setVisibility(View.INVISIBLE);
                listView.setVisibility(View.INVISIBLE);
                btnSave.setVisibility(View.VISIBLE);
                break;
            case "2":
                tvResult.setText(resultList.get(1));
                btnGo.setVisibility(View.INVISIBLE);
                listView.setVisibility(View.VISIBLE);
                btnSave.setVisibility(View.VISIBLE);

                ArrayList<String> resultItems = new ArrayList<>(resultList.subList(2, resultList.size()));
                ArrayList<String> fetchedResults = new ArrayList<>();

                for (String item : resultItems) {
                    String sql = "SELECT * FROM disease WHERE diseaseName like '%" + item + "%'";
                    Log.d("SQL", sql);
                    Cursor cursor = sqlDB.rawQuery(sql, null);

                    while (cursor.moveToNext()) {
                        fetchedResults.add(cursor.getString(0));
                    }
                    cursor.close();
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, fetchedResults);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener((parent, view, position, id) -> {
                    String diseaseName = (String) parent.getAdapter().getItem(position);

                    Intent intent = new Intent(getApplicationContext(), DiseaseActivity.class);
                    intent.putExtra("diseaseName", diseaseName);
                    startActivity(intent);
                });

                btnHome.setOnClickListener(v -> {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("isLoggedIn", true);
                    startActivity(intent);
                    finish();
                });
                break;
            case "3":
                questionNumList.remove(questionNumList.size() - 1);
                questionNumList.add(Integer.parseInt(resultList.get(1)));
                loadQuestionFromDB(Integer.parseInt(resultList.get(1)));
                break;
            default:
                tvResult.setText("Unknown Result");
                break;
        }
    }

    private void medicalHistoryUpdate() {
        SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
        String userId = sharedPref.getString("userId", "");
        Log.d("userId", userId);

        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String history = questionNumList.get(questionNumList.size() - 1) + "||" +
                symptomName.getText().toString().trim() + "||" + result + "||" + selectedAnswer + "||" + time;

        Cursor cursor = sqlDB.rawQuery("SELECT medicalHistory FROM member WHERE id = ?", new String[]{userId});
        String currentHistory = "";
        if (cursor.moveToFirst()) {
            currentHistory = cursor.getString(0);
            if (currentHistory == null) {
                currentHistory = "";
            }
        }
        cursor.close();

        String updatedHistory = currentHistory.isEmpty() ? history : history + "//" + currentHistory;

        sqlDB.execSQL("UPDATE member SET medicalHistory = ? WHERE id = ?", new Object[]{updatedHistory, userId});

        // 서버에 POST 요청 보내기
        new Thread(() -> {
            try {
                URL url = new URL("https://kse.calab.myds.me/update_member.php"); // PHP 파일의 URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String columnName = "medicalHistory";
                String postData = "columnName=" + URLEncoder.encode(columnName, "UTF-8") +
                        "&userId=" + URLEncoder.encode(userId, "UTF-8") +
                        "&item=" + URLEncoder.encode(updatedHistory, "UTF-8");

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