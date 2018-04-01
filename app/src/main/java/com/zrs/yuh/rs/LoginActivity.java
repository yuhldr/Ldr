package com.zrs.yuh.rs;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zrs.yuh.rs.timetable.Course;
import com.zrs.yuh.rs.timetable.Lesson2LoadActivity;
import com.zrs.yuh.rs.timetable.LessonLoadActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    protected static final int CHANGE_UI = 1;
    protected static final int ERROR = 2;
    protected static final int SUCCESS_SIGN_IN = 3;
    protected static final int ERROR_SIGN_IN = 4;

    private ProgressDialog progressDialog;

    private EditText et_user;
    private EditText et_password;
    private EditText et_Captcha;
    private ImageView iv_Captcha;

    public String JSESSIONID;

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case CHANGE_UI:

                    byte[] Picture = (byte[]) msg.obj; //使用BitmapFactory工厂，把字节数组转化为bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(Picture, 0, Picture.length);

                    dismissProgressDialog();
                    iv_Captcha.setImageBitmap(bitmap);

                    break;

                case ERROR:

                    String error = (String) msg.obj;
                    Toast.makeText(LoginActivity.this, "抱歉，显示错误："+error, Toast.LENGTH_SHORT).show();
                    break;

                case ERROR_SIGN_IN:

                    get_captcha();
                    String error_sign_in = (String) msg.obj;
                    Toast.makeText(LoginActivity.this, error_sign_in + "\n" + "刷新中……" + "\n" + JSESSIONID, Toast.LENGTH_SHORT).show();
                    break;

                case SUCCESS_SIGN_IN:

                    dismissProgressDialog();
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                    break;

                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        et_user = findViewById(R.id.et_user);
        et_password = findViewById(R.id.et_password);
        et_Captcha = findViewById(R.id.et_Captcha);
        iv_Captcha = findViewById(R.id.iv_Captcha);
        Button bt_sign_in = findViewById(R.id.bt_sign_in);
        Button bt_time_table = findViewById(R.id.bt_time_table);
        Button bt_calculate = findViewById(R.id.bt_calculate);

        Map<String,String> userInfo = Utils.getUserInfo(this,"user_password");
        if(userInfo!=null){
            et_user.setText(userInfo.get("number"));
            et_password.setText(userInfo.get("pwd"));
        }

        get_captcha();

        iv_Captcha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                get_captcha();
                Toast.makeText(LoginActivity.this,"刷新中……",Toast.LENGTH_SHORT).show();

            }
        });

        bt_time_table.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Course> list1 = Utils.getCourseInfo(LoginActivity.this,"week_1");
                if(list1!=null){
                    Intent intent = new Intent(LoginActivity.this,LessonLoadActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(LoginActivity.this,"未检测到课程表数据，\n先登录,刷新一下课程表页面吧",Toast.LENGTH_LONG).show();
                }

            }
        });

        bt_calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this,Calculator.class);
                startActivity(intent);


            }
        });

        bt_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String user = et_user.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                String captcha = et_Captcha.getText().toString().trim();

                post_sign_in(user, password, captcha);

                boolean isSaveSuccess = Utils.saveUserInfo(LoginActivity.this, "user_password",user, password);

            }
        });

    }

        public void get_captcha(){

            showProgressDialog(this,"验证码加载中……","验证码加载超时！，检查网络后重新登陆");

            OkHttpClient client = new OkHttpClient
                    .Builder()
                    .connectTimeout(100, TimeUnit.SECONDS) //设置连接超时
                    .readTimeout(100, TimeUnit.SECONDS) //设置读超时
                    .writeTimeout(100,TimeUnit.SECONDS) //设置写超时
                    .retryOnConnectionFailure(true) //是否自动重连
                    .build(); //构建OkHttpClient对象

            String url1 = "http://jwk.lzu.edu.cn/academic/getCaptcha.do";

            Request captcha_response = new Request.Builder()
                    .url(url1)
                    .header("Accept", "*/*")
                    .addHeader("Accept-Encoding", "gzip, deflate")
                    .addHeader("Accept-Language","zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                    .addHeader("Cache-Control","max-age=0")
                    .addHeader("Connection", "keep-alive")
                    .addHeader("Host","jwk.lzu.edu.cn")
                    .addHeader("Referer","http://jwk.lzu.edu.cn/academic/common/security/login.jsp")
                    .addHeader("Upgrade-Insecure-Requests","1")
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0")
                    .build();

            Call captcha_pic = client.newCall(captcha_response);
            captcha_pic.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.i("info_callFailure",e.toString());
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    byte[] byte_image = response.body().bytes();
                    Message msg = new Message();
                    msg.what = CHANGE_UI;
                    msg.obj = byte_image;
                    handler.sendMessage(msg);

                    Headers captcha_headers = response.headers();
                    int responseHeadersLength = captcha_headers.size();
                    for (int i = 0; i < responseHeadersLength; i++) {
                        String headerName = captcha_headers.name(i);
                        String headerValue = captcha_headers.get(headerName);
                        Log.d("header", headerName + "======>" + headerValue);
                    }
                    @SuppressLint("HandlerLeak")

                    String Set_Cookies = captcha_headers.get("Set-Cookie");
                    Log.d("set_cookies", Set_Cookies);
                    JSESSIONID = Set_Cookies;

                    final Data session_app = (Data) getApplication();
                    session_app.setData_s(JSESSIONID);



                }
            });


        }

        public void post_sign_in(final String name, String pwd, String captcha) {

            showProgressDialog(this,"登陆中……","登陆超时！，检查网络后重新登陆");

            String url = "http://jwk.lzu.edu.cn/academic/j_acegi_security_check";
            OkHttpClient okHttpClient = new OkHttpClient();

            RequestBody body = new FormBody.Builder()
                    .add("groupId", "")
                    .add("j_username", name)
                    .add("j_password", pwd)
                    .add("j_captcha", captcha)
                    .add("button1", "%B5%C7%C2%BC")
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .addHeader("Accept-Encoding", "gzip, deflate")
                    .addHeader("Accept-Language","zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                    .addHeader("Content-Length","90")
                    .addHeader("Content-Type","application/x-www-form-urlencoded")
                    .addHeader("Connection", "keep-alive")
                    .addHeader("Cookie",JSESSIONID)
                    .addHeader("Host","jwk.lzu.edu.cn")
                    .addHeader("Referer","http://jwk.lzu.edu.cn/academic/common/security/login.jsp")
                    .addHeader("Upgrade-Insecure-Requests","1")
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0")
                    .post(body)
                    .build();

            Call call = okHttpClient.newCall(request);


            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.i("info_call2fail", e.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String results =response.body().string();
                        Log.i("info_call2success", results);

                        String result = "";
                        Document doc = Jsoup.parse(results);
                        Elements elements = doc.getElementsByClass("error");
                        //错误提示： 您输入的验证码不正确(密码可能也不正确！!
                        // 错误提示： 密码不匹配!
                        result = elements.text();

//                        Elements elements = doc.select("body");
//                        String result = elements.get(0).text();

                        Message msg =new Message();
                        if(!Objects.equals(result, "")){
                            Log.d("----------------",result);
                            msg.what = ERROR_SIGN_IN;
                            msg.obj = result;
                        }else {
                            Log.d("================","成功");
                            msg.what = SUCCESS_SIGN_IN;
                            msg.obj = result;
                        }

                        handler.sendMessage(msg);

                    }

                }

            });


        }

    /**
     * 加载框
     */
    public void showProgressDialog(Context mContext, String text, final String dismiss_text) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        progressDialog.setMessage(text);    //设置内容
        progressDialog.setCancelable(false);//点击屏幕和按返回键都不能取消加载框
        progressDialog.show();

        //设置超时自动消失
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //取消加载框
                //超时处理
                if(dismissProgressDialog()) {
                    Toast.makeText(LoginActivity.this, dismiss_text, Toast.LENGTH_SHORT).show();
                }
            }
        }, 10000);//超时时间10秒
    }
    public Boolean dismissProgressDialog() {
        if (progressDialog != null){
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                return true;//取消成功
            }
        }
        return false;//已经取消过了，不需要取消
    }






}
