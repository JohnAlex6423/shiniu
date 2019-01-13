package com.olcow.shiniu.fragment;


import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.olcow.shiniu.R;
import com.olcow.shiniu.activity.SendPostActivity;
import com.olcow.shiniu.adapter.PostAdapter;
import com.olcow.shiniu.entity.Post;
import com.olcow.shiniu.entity.PostPro;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    private OkHttpClient okHttpClient;
    private List<Post> posts;
    private RecyclerView.Adapter adapter;
    private int imgWidth;
    private ImageView homePlus;
    private RecyclerView recyclerView;
    private int pageNumber;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        recyclerView = view.findViewById(R.id.home_recy);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        homePlus = view.findViewById(R.id.home_plus);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        pageNumber = 1;
        imgWidth = (point.x - (int) (20*Resources.getSystem().getDisplayMetrics().density + 0.5f)) /3;
        getPosts();
        homePlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),SendPostActivity.class));
                getActivity().overridePendingTransition(0,0);
            }
        });
        return view;
    }

    private void getPosts(){
        if (okHttpClient == null){
            okHttpClient = new OkHttpClient();
        }
        okHttpClient.newCall(new Request.Builder()
                .url("http://39.96.40.12:7678/post/getallpost?pageNumber="+pageNumber+"&pageSize=20")
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(),"网络链接失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                List<PostPro> postPros = JSON.parseArray(res,PostPro.class);
                posts = new ArrayList<>();
                for (PostPro postPro:postPros){
                    List<String> imgs = new ArrayList<>();
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(postPro.getImgs());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (jsonArray !=null){
                        for (int i=0;i<jsonArray.length();i++){
                            try {
                                imgs.add("http://123.206.93.200/uploadimg/"+jsonArray.getString(i));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    posts.add(new Post(postPro.getPostid(),postPro.getUid(),postPro.getContent(),imgs,postPro.getDate(),postPro.getName(),postPro.getAvatar()));
                }
                adapter = new PostAdapter(posts,imgWidth);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setAdapter(adapter);
                    }
                });
            }
        });
    }
}