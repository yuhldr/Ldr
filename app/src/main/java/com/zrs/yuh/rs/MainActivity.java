package com.zrs.yuh.rs;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zrs.yuh.rs.timetable.Lesson2LoadActivity;
import com.zrs.yuh.rs.timetable.LessonLoadActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{


    protected static final int NAME = 0;
    protected static final int ERROR = 1;
    private TextView tv_name;


    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what ){
                case NAME:

                    String results = (String) msg.obj;

                    // 取出姓名学号字段
                    Document doc = Jsoup.parse(results);
                    Elements elements = doc.getElementById("greeting").select("span");
                    String name = elements.get(0).text();
                    Log.d("姓名",name);
                    Toast.makeText(MainActivity.this,"欢迎" + name + "登录",Toast.LENGTH_SHORT).show();
                    tv_name.setText(name);
                    break;

                default:
                    break;

//                        Elements elements = doc.select("body");
//                        String result = elements.get(0).text();

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

//        navigationView.setNavigationItemSelectedListener(this);

//        View headerView = navigationView.getHeaderView(0);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);//点击跳转侧栏
        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main);
        tv_name = headerLayout.findViewById(R.id.tv_name1);

        final Data session_app = (Data) getApplication();
        String session = session_app.getData_s();
        Log.d("test_set_session",session_app.getData_s());

        String url_name = "http://jwk.lzu.edu.cn/academic/showHeader.do";
        String Referer = "http://jwk.lzu.edu.cn/academic/index_new.jsp";
        Get_String(url_name,session,Referer);




    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()){

            case R.id.nav_lesson:
                //
                Intent intent = new Intent(MainActivity.this,LessonLoadActivity.class);
                startActivity(intent);

                break;

            case R.id.nav_exam:
                //
                Toast.makeText(MainActivity.this,"开发中……",Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_score:
                //
                Toast.makeText(MainActivity.this,"开发中……",Toast.LENGTH_LONG).show();

                break;
            case R.id.nav_secret:
                //
                Toast.makeText(MainActivity.this,"开发中……",Toast.LENGTH_LONG).show();

                break;
            case R.id.nav_calculate:
                //
                Intent intent_nav_calculate = new Intent(MainActivity.this,Calculator.class);
                startActivity(intent_nav_calculate);

                break;

            case R.id.nav_share:
                //
                Toast.makeText(MainActivity.this,"开发中……",Toast.LENGTH_LONG).show();

                break;

            case R.id.nav_send:
                //
                break;

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void Get_String(String url, String session, String Referer){
        OkHttpClient client = new OkHttpClient
                .Builder()
                .connectTimeout(100, TimeUnit.SECONDS) //设置连接超时
                .readTimeout(100, TimeUnit.SECONDS) //设置读超时
                .writeTimeout(100,TimeUnit.SECONDS) //设置写超时
                .retryOnConnectionFailure(true) //是否自动重连
                .build(); //构建OkHttpClient对象

        Request captcha_response = new Request.Builder()
                .url(url)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("Accept-Language","zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                .addHeader("Cookie",session)
                .addHeader("Connection", "keep-alive")
                .addHeader("Host","jwk.lzu.edu.cn")
                .addHeader("Referer",Referer)
                .addHeader("Upgrade-Insecure-Requests","1")
                .addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:58.0) Gecko/20100101 Firefox/58.0")
                .build();

        Call captcha_pic = client.newCall(captcha_response);
        captcha_pic.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i("info_callFailure",e.toString());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String results =response.body().string();
//                    Log.i("info_name_success", results);

                    if(!Objects.equals(results, "")){
                        Log.d("----------------",results);
                        Message msg = new Message();
                        msg.what = NAME;
                        msg.obj = results;
                        handler.sendMessage(msg);
                    }
                }

            }
        });
    }


}


