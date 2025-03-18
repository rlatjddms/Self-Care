package com.example.capstone_hospital;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class MedicalHistoryActivity extends AppCompatActivity {

    Button btnBack;
    ListView listView;
    SQLiteDatabase sqlDB;
    MainActivity.MyDBHelper myHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_history);

        btnBack = (Button) findViewById(R.id.btnBack);
        listView = (ListView) findViewById(R.id.listView);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
        String userId = sharedPref.getString("userId", "");

        myHelper = new MainActivity.MyDBHelper(getApplicationContext(), "capstone", null, 1);
        sqlDB = myHelper.getReadableDatabase();

        String sql = "SELECT medicalHistory FROM member WHERE id='" + userId + "'";
        Log.d("SQL", sql);
        Cursor cursor = sqlDB.rawQuery(sql, null);

        String medicalHistory = "";
        if (cursor.moveToFirst()) {
            medicalHistory = cursor.getString(0);
        }
        cursor.close();

        if (medicalHistory == null || medicalHistory.isEmpty()) {
            listView.setVisibility(View.INVISIBLE);
            return;
        }

        String[] historySplit = medicalHistory.split("//");

        ArrayList<HistoryListData> listViewData = new ArrayList<>();

        if (historySplit.length == 1 && historySplit[0].isEmpty()) {
            listView.setVisibility(View.INVISIBLE);
        } else {
            for (int i = 0; i < historySplit.length; i++) {
                String[] itemSplit = historySplit[i].split("\\|\\|");
                if (itemSplit.length > 0) {
                    HistoryListData listData = new HistoryListData();
                    listData.title = itemSplit[1];
                    listData.time = itemSplit[4];
                    listViewData.add(listData);
                }
            }
        }

        ListAdapter listAdapter = new HistoryListView(listViewData);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedHistory = historySplit[position];

                Intent intent = new Intent(getApplicationContext(), HistoryInfoActivity.class);
                intent.putExtra("selectedHistory", selectedHistory);
                startActivity(intent);
            }
        });
    }
}

class HistoryListView extends BaseAdapter {
    LayoutInflater layoutInflater = null;
    private ArrayList<HistoryListData> listViewData = null;
    private int count = 0;

    public HistoryListView(ArrayList<HistoryListData> listData)
    {
        listViewData = listData;
        count = listViewData.size();
    }

    @Override
    public int getCount()
    {
        return count;
    }

    @Override
    public Object getItem(int position)
    {
        return listViewData.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            final Context context = parent.getContext();
            if (layoutInflater == null)
            {
                layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = layoutInflater.inflate(R.layout.history_list, parent, false);
        }

        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        TextView tvTime = convertView.findViewById(R.id.tvTime);

        tvTitle.setText(listViewData.get(position).title);
        tvTime.setText(listViewData.get(position).time);

        return convertView;
    }
}

class HistoryListData {
    public String title = "";
    public String time = "";
}