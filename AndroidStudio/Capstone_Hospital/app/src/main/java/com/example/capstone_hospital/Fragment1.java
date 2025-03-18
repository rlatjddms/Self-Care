package com.example.capstone_hospital;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Fragment1 extends Fragment {

    Button btnStart, btnBot, btnSkin;
    TextView tvDate;
    String getDate = "";
    RecyclerView recyclerView;
    RecyclerAdapter adapter;
    String health_url = "https://kormedi.com/todayhealth/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_1, container, false);

        btnStart = (Button) view.findViewById(R.id.btnStart);
        btnBot = (Button) view.findViewById(R.id.btnBot);
        btnSkin = (Button) view.findViewById(R.id.btnSkin);
        tvDate = (TextView) view.findViewById(R.id.tvDate);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SymptomActivity.class);
                startActivity(intent);
            }
        });

        btnBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ChatBotActivity.class);
                startActivity(intent);
            }
        });

        btnSkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SkinActivity.class);
                startActivity(intent);
            }
        });

        long now = System.currentTimeMillis();
        Date date = new Date(now);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        getDate = sdf.format(date);
        tvDate.setText(getDate);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new RecyclerAdapter();
        recyclerView.setAdapter(adapter);
        getData();

        return view;
    }

    private void getData(){
        HealthJsoup jsoupAsyncTask = new HealthJsoup();
        jsoupAsyncTask.execute();
    }

    private class HealthJsoup extends AsyncTask<Void, Void, Void> {
        ArrayList<String> listTitle = new ArrayList<>();
        ArrayList<String> listDate = new ArrayList<>();
        ArrayList<String> listImage = new ArrayList<>();
        ArrayList<String> listUrl = new ArrayList<>();

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                org.jsoup.nodes.Document doc = Jsoup.connect(health_url).get();

                final Elements title = doc.select("h2.title a.post-title");
                final Elements image = doc.select("div.featured a.img-holder");
                final Elements url = doc.select("h2.title a.post-title");

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 제목
                        for(org.jsoup.nodes.Element element: title) {
                            listTitle.add(element.text());
                        }
                        // 날짜
                        for (int i = 0; i < title.size(); i++) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.DATE, -i);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            String formattedDate = sdf.format(calendar.getTime());
                            listDate.add(formattedDate);
                        }
                        // 이미지
                        for (org.jsoup.nodes.Element element : image){
                            listImage.add(element.attr("data-bg"));
                        }
                        // url
                        for (org.jsoup.nodes.Element element : url){
                            listUrl.add(element.attr("href"));
                        }

                        for (int i = 0; i < 10 ; i++) {
                            HealthList data = new HealthList();
                            data.setTitle(listTitle.get(i));
                            data.setDate(listDate.get(i));
                            data.setImage(listImage.get(i));
                            data.setUrl(listUrl.get(i));

                            adapter.addItem(data);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}

class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {

    private ArrayList<HealthList> listData = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.health_item, viewGroup, false);
        return new
                ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.ItemViewHolder itemViewHolder, int i) {
        itemViewHolder.onBind(listData.get(i));

        itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = itemViewHolder.getAdapterPosition();

                Context context = v.getContext();
                Intent intent = new Intent(context, TodayHealthActivity.class);
                intent.putExtra("url", listData.get(position).getUrl());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
    void addItem(HealthList data) {
        listData.add(data);
    }
    class ItemViewHolder extends RecyclerView.ViewHolder{

        private TextView txt_date, txt_title;
        private ImageView img;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_title = itemView.findViewById(R.id.txt_title);
            txt_date = itemView.findViewById(R.id.txt_date);
            img = itemView.findViewById(R.id.img);

        }

        void onBind(HealthList data){
            txt_date.setText(data.getDate());
            txt_title.setText(data.getTitle());

            Glide.with(itemView.getContext()).load(data.getImage()).into(img);

        }
    }
}

class HealthList {

    private String title;
    private String date;
    private String image;
    private String url;

    public HealthList() {
        this.title = title;
        this.date = date;
        this.image = image;
        this.url = url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getImage() {
        return image;
    }

    public String getUrl() {
        return url;
    }
}