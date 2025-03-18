package com.example.capstone_hospital;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class HistoryInfoActivity extends AppCompatActivity {

    Button btnBack;
    TextView symptomName, tvResult;
    ListView listView;
    SQLiteDatabase sqlDB;
    MainActivity.MyDBHelper myHelper;
    ArrayList<String> splitList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_info);

        btnBack = (Button) findViewById(R.id.btnBack);
        symptomName = (TextView) findViewById(R.id.symptomName);
        tvResult = (TextView) findViewById(R.id.tvResult);
        listView = (ListView) findViewById(R.id.listView);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String selectedHistory = getIntent().getStringExtra("selectedHistory");
        Log.d("selectedHistory", selectedHistory);

        myHelper = new MainActivity.MyDBHelper(getApplicationContext(), "capstone", null, 1);
        sqlDB = myHelper.getReadableDatabase();

        String[] itemSplit = selectedHistory.split("\\|\\|");
        symptomName.setText(itemSplit[1]);

        String[] resultSplit = itemSplit[2].split("/");
        splitList = new ArrayList<>(Arrays.asList(resultSplit));
        splitList.removeIf(String::isEmpty);

        if(splitList.get(0).equals("3")) {
            String[] split = splitList.get(Integer.parseInt(itemSplit[3])).split("\\|");
            splitList = new ArrayList<>(Arrays.asList(split));
            splitList.removeIf(String::isEmpty);

            handleResultList(splitList);
        }
        else {
            handleResultList(splitList);
        }
    }

    private void handleResultList(ArrayList<String> resultList) {
        String index = resultList.get(0);

        switch (index) {
            case "1":
                tvResult.setText(splitList.get(1));
                listView.setVisibility(View.INVISIBLE);
                break;
            case "2":
                tvResult.setText(splitList.get(1));
                listView.setVisibility(View.VISIBLE);

                ArrayList<String> resultItems = new ArrayList<>(splitList.subList(2, splitList.size()));
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
                break;
            default:
                tvResult.setText("Unknown Result");
                break;
        }
    }
}