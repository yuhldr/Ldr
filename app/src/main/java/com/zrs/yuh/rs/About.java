package com.zrs.yuh.rs;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class About extends AppCompatActivity {

    protected static final int NEW = 0;


    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {

                case NEW:

                    String results = (String) msg.obj;

                    Document document = Jsoup.parse(results);

                    String version = "1.2";
                    String version0 = document.getElementsByClass("list_app_info").text();

                    String update_info = document.getElementsByClass("apk_left_title_info").first().toString();
                    if (!version0.equals(version)) {

                        Toast.makeText(About.this, "发现最新版本！!", Toast.LENGTH_LONG).show();

                        AlertDialog dialog = new AlertDialog.Builder(About.this).create();
                        dialog.setTitle("小小一棵树");
                        dialog.setMessage(version0 + "更新内容:\n" + update_info.substring(31).replaceAll("<br>","\n"));
                        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "前往更新",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Uri uri = Uri.parse("https://www.coolapk.com/apk/com.zrs.yuh.rs");
                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                        startActivity(intent);
                                    }
                                });
                        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                        dialog.show();
                    } else {
                        Toast.makeText(About.this, "已是最新版本！!", Toast.LENGTH_LONG).show();
                    }

                    break;

                default:
                    break;


            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Button bt_update = findViewById(R.id.bt_update);
        TextView tv_about_name = findViewById(R.id.tv_about_name);
        TextView tv_about = findViewById(R.id.tv_about);

        String update_info_late =
                "1.2更新日志: \n\n " +
                        "1. 新增，关于界面，以及进入时检查更新\n "+
                        "2. 去除，每次进去应用，检测单cookie失效后弹出的登录界面，需要刷新数据可以通过右上角登陆进行更新cookies（有效期十分钟）\n " +
                        "3. 新增，主界面今日课程，课程按照时间顺序排序，可以向前向后移动天数、恢复今天，进行查看\n " +
                        "4. 修复，成绩查询，课程信息，点开的详情界面仍出现省略号的bug\n " +
                        "5. 优化，教务系统公告，用户姓名数据本地化\n \n "+
                "1.1更新日志：\n \n " +
                        "1. 优化软件界面，修改配色，\n " +
                        "2. 修改图标（图标来源网络，重新加工））\n " +
                        "3. 修复部分课表导入失败，\n " +
                        "4. 修复修改密码成功提示语，\n " +
                        "5. 新增联系开发者。\n \n "+
                "1.0正式版更新日志：\n\n " +
                        "1. 支持添加两个人的课程信息，目前不支持编辑 \n " +
                        "2. 兰州大学物理实验不确定度的计算 \n " +
                        "3. 增加课程成绩查询，\n " +
                        "4. 增加教学公告界面，\n " +
                        "5. 修复不确定度就计算中输入错误闪退bug \n " +
                        "6. 删除登陆界面，改为弹窗式 \n " +
                        "7. cookies保存至本地，十分钟内可登陆 \n \n" ;


        tv_about.setText(update_info_late);


;

        tv_about_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://github.com/yuhang0825/RS");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        bt_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Toast.makeText(About.this, "检查更新中……", Toast.LENGTH_SHORT).show();

                    OkHttpClient client = new OkHttpClient
                            .Builder()
                            .connectTimeout(100, TimeUnit.SECONDS) //设置连接超时
                            .readTimeout(100, TimeUnit.SECONDS) //设置读超时
                            .writeTimeout(100, TimeUnit.SECONDS) //设置写超时
                            .retryOnConnectionFailure(true) //是否自动重连
                            .build(); //构建OkHttpClient对象

                    Request captcha_response = new Request.Builder()
                            .url("https://www.coolapk.com/apk/com.zrs.yuh.rs")
                            .addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:58.0) Gecko/20100101 Firefox/58.0")
                            .build();

                    Call captcha_pic = client.newCall(captcha_response);
                    captcha_pic.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.i("info_callFailure", e.toString());
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (response.isSuccessful()) {
                                assert response.body() != null;
                                String results = response.body().string();
//                    Log.i("info_name_success", results);

                                if (!Objects.equals(results, "")) {
                                    Log.d("----------------", results);
                                    Message msg = new Message();
                                    msg.what = NEW;
                                    msg.obj = results;
                                    handler.sendMessage(msg);
                                }

                            }

                        }
                    });

            }
        });




    }

}
