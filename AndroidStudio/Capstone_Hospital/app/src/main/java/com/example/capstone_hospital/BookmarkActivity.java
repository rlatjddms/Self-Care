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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class BookmarkActivity extends AppCompatActivity {

    Button btnBack;
    RadioButton rbAll, rbDisease, rbHospital;
    RadioGroup radioGroup;
    ListView listView;
    SQLiteDatabase sqlDB;
    MainActivity.MyDBHelper myHelper;
    ArrayList<BookmarkListData> listViewData = new ArrayList<>();
    String userId = "";
    String bookmarkSplit[] = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        btnBack = (Button) findViewById(R.id.btnBack);
        rbAll = (RadioButton) findViewById(R.id.rbAll);
        rbDisease = (RadioButton) findViewById(R.id.rbDisease);
        rbHospital = (RadioButton) findViewById(R.id.rbHospital);
        listView = (ListView) findViewById(R.id.listView);
        radioGroup = findViewById(R.id.radioGroup);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
        userId = sharedPref.getString("userId", "");

        myHelper = new MainActivity.MyDBHelper(getApplicationContext(), "capstone", null, 1);
        sqlDB = myHelper.getReadableDatabase();

        loadBookmarks();

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                loadBookmarks();
            }
        });

        ListAdapter listAdapter = new BookmarkListView(listViewData);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedBookmark = listViewData.get(position).item;
                Log.d("ItemSplit[0]", selectedBookmark);

                for(int i=0; i<bookmarkSplit.length; i++) {
                    if(bookmarkSplit[i].contains(selectedBookmark)) {
                        selectedBookmark = bookmarkSplit[i];
                    }
                }

                String[] itemSplit = selectedBookmark.split("\\|");
                Log.d("ItemSplit[0]", itemSplit[0]);

                if(itemSplit[0].equals("0")) {
                    Intent intent = new Intent(getApplicationContext(), DiseaseActivity.class);
                    intent.putExtra("selectedBookmark", selectedBookmark);
                    startActivity(intent);
                }
                else if(itemSplit[0].equals("1")) {
                    Intent intent = new Intent(getApplicationContext(), HospitalInfoActivity.class);
                    intent.putExtra("selectedBookmark", selectedBookmark);
                    startActivity(intent);
                }
            }
        });
    }

    private void loadBookmarks() {
        listViewData.clear();
        String sql = "SELECT bookmark FROM member WHERE id='" + userId + "'";
        Cursor cursor = sqlDB.rawQuery(sql, null);

        String bookmark = "";
        if (cursor.moveToFirst()) {
            bookmark = cursor.getString(0);
        }
        cursor.close();

        if (bookmark == null || bookmark.isEmpty()) {
            listView.setVisibility(View.INVISIBLE);
            return;
        }

        bookmarkSplit = bookmark.split("~");

        for (int i = 0; i < bookmarkSplit.length; i++) {
            String item = bookmarkSplit[i];
            String[] itemSplit = item.split("\\|");
            if (itemSplit.length > 1) {
                if (rbAll.isChecked() || (rbDisease.isChecked() && itemSplit[0].equals("0"))
                        || (rbHospital.isChecked() && itemSplit[0].equals("1"))) {
                    BookmarkListData listData = new BookmarkListData();
                    listData.item = itemSplit[1];
                    listViewData.add(listData);
                }
            }
        }

        ListAdapter listAdapter = new BookmarkListView(listViewData);
        listView.setAdapter(listAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookmarks();
    }
}

class BookmarkListView extends BaseAdapter {
    LayoutInflater layoutInflater = null;
    private ArrayList<BookmarkListData> listViewData = null;
    private int count = 0;

    public BookmarkListView(ArrayList<BookmarkListData> listData)
    {
        listViewData = listData;
        count = listViewData.size();
    }

    @Override
    public int getCount()
    {
        return listViewData.size();
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
            convertView = layoutInflater.inflate(R.layout.disease_list, parent, false);
        }

        TextView item = convertView.findViewById(R.id.diseaseName);

        item.setText(listViewData.get(position).item);

        return convertView;
    }
}

class BookmarkListData {
    public String item = "";
}