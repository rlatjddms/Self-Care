package com.example.capstone_hospital;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.chip.Chip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Fragment2 extends Fragment {

    Chip search;
    ListView listView;
    SQLiteDatabase sqlDB;
    MainActivity.MyDBHelper myHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_2, container, false);

        search = (Chip) view.findViewById(R.id.search);
        listView = (ListView) view.findViewById(R.id.listLayout);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        ArrayList<DiseaseListData> listViewData = new ArrayList<>();

        myHelper = new MainActivity.MyDBHelper(getActivity(), "capstone", null, 1);
        sqlDB = myHelper.getReadableDatabase();

        String sql = "select * from disease";
        Log.d("SQL", sql);
        Cursor cursor = sqlDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            DiseaseListData listData = new DiseaseListData();

            String diseaseName = cursor.getString(0);

            listData.diseaseName = diseaseName;
            listViewData.add(listData);
        }
        cursor.close();
        sqlDB.close();

        ListAdapter listAdapter = new DiseaseListView(listViewData);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                DiseaseListData selectedDisease = (DiseaseListData) adapterView.getAdapter().getItem(position);
                String diseaseName = selectedDisease.diseaseName;

                Intent intent = new Intent(getActivity(), DiseaseActivity.class);
                intent.putExtra("diseaseName", diseaseName);
                startActivity(intent);
            }
        });

        return view;
    }
}

class DiseaseListView extends BaseAdapter {
    LayoutInflater layoutInflater = null;
    private ArrayList<DiseaseListData> listViewData = null;

    public DiseaseListView(ArrayList<DiseaseListData> listData)
    {
        listViewData = listData;
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
        if (convertView == null) {
            final Context context = parent.getContext();
            if (layoutInflater == null) {
                layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = layoutInflater.inflate(R.layout.disease_list, parent, false);
        }

        TextView diseaseName = convertView.findViewById(R.id.diseaseName);
        TextView matchCount = convertView.findViewById(R.id.count);

        diseaseName.setText(listViewData.get(position).diseaseName);
        matchCount.setText(listViewData.get(position).matchCount);

        return convertView;
    }
}

class DiseaseListData {
    public String diseaseName = "";
    public String matchCount = "";

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public String getCount() {
        return matchCount;
    }

    public void setCount(String matchCount) {
        this.matchCount = matchCount;
    }
}