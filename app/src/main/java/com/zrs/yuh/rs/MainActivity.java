package com.zrs.yuh.rs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.zrs.yuh.rs.Function.Utils;
import com.zrs.yuh.rs.score.Performance;
import com.zrs.yuh.rs.score.PersonalPerformance;
import com.zrs.yuh.rs.timetable.Course;
import com.zrs.yuh.rs.timetable.LessonLoadActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
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

import static com.zrs.yuh.rs.R.color.colorPrimary;
import static com.zrs.yuh.rs.R.color.color_white;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{


    protected static final int NAME = 0;
    protected static final int NOTICE = 1;

    protected static final int CHANGE_UI = 6;
    protected static final int ERROR_SIGN_IN = 4;

    protected static final int SUCCESS_SIGN_IN = 3;

    private TextView tv_name,tv_notice,tv_today_table0;


    private AlertDialog dialog;
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

            switch (msg.what ){

                case CHANGE_UI:

                    //必须先实例化，再进行对话框UI更新
                    @SuppressLint("InflateParams") View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_login,null); //找到并对自定义布局中的控件进行操作的示例

                    byte[] Picture = (byte[]) msg.obj; //使用BitmapFactory工厂，把字节数组转化为bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(Picture, 0, Picture.length);
                    dismissProgressDialog();

                    et_user = view.findViewById(R.id.et_user_2);
                    et_password = view.findViewById(R.id.et_password_2);
                    et_Captcha = view.findViewById(R.id.et_Captcha_2);

                    ImageView iv_Captcha_2 = view.findViewById(R.id.iv_Captcha_2);
                    iv_Captcha_2.setImageBitmap(bitmap);
                    iv_Captcha_2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            SaveView();  // 刷新前，保存密码
                            get_captcha();
                            dialog.dismiss();

                        }
                    });

                    // 获取输入框密码，存储。

                    Map<String, String> userInfo = Utils.getUserInfo(MainActivity.this,"user_password");
                    if (userInfo != null) {
                        et_user.setText(userInfo.get("number"));
                        et_password.setText(userInfo.get("pwd"));
                    }
                    //创建对话框
                    dialog = new AlertDialog.Builder(MainActivity.this).create();
                    dialog.setTitle("教务系统登陆(主用户)");//设置标题
                    dialog.setView(view);//添加布局
                    //设置按键
                    dialog.setButton(AlertDialog.BUTTON_POSITIVE, "登陆",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    String user = et_user.getText().toString().trim();
                                    String password = et_password.getText().toString().trim();
                                    String captcha = et_Captcha.getText().toString().trim();

                                    boolean isSaveSuccess = Utils.saveUserInfo(MainActivity.this,"user_password", user, password);


                                    //TODO,登陆，激活验证码
                                    post_sign_in(user, password, captcha);


                                }
                            });
                    dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    dialog.show();

                    break;


                case ERROR_SIGN_IN:

                    get_captcha();
                    String error_sign_in = (String) msg.obj;
                    Toast.makeText(MainActivity.this, error_sign_in + "\n" + "刷新中……" + "\n" + JSESSIONID, Toast.LENGTH_SHORT).show();
                    break;

                case SUCCESS_SIGN_IN:

                    dismissProgressDialog();
                    String url_name = "http://jwk.lzu.edu.cn/academic/showHeader.do";
                    String Referer = "http://jwk.lzu.edu.cn/academic/index_new.jsp";
                    Get_String(url_name,JSESSIONID,Referer);


                    break;

                case NAME:

                    String results = (String) msg.obj;

                    // 取出姓名学号字段
                    Document doc = Jsoup.parse(results);
                    Element elements0 = doc.getElementById("greeting");
                    if (elements0 == null) {
                        Toast.makeText(MainActivity.this, "验证码已失效，请退出重新登陆后刷新", Toast.LENGTH_LONG).show();
                        get_captcha();

                    }else {
                        String name = elements0.select("span").get(0).text();
                        Log.d("姓名",name);
                        Toast.makeText(MainActivity.this,"欢迎" + name + "登录",Toast.LENGTH_SHORT).show();
                        tv_name.setText(name);

                        List<Course> list_name = Utils.getCourseInfo(MainActivity.this,"cookies");
                        if (list_name!=null) {

                            String session_notice = list_name.get(0).getClassRoomName();
                            String Referer_notice = "http://jwk.lzu.edu.cn/academic/index_new.jsp";
                            String url_notice = "http://jwk.lzu.edu.cn/academic/calendarinfo/viewCalendarInfo.do";
                            Get_Notice(url_notice, session_notice, Referer_notice);
                        }
                    }

                    break;

                case NOTICE:

                    String notice_code = (String) msg.obj;
                    // 取出教务系统运行公告
                    Document notice_doc = Jsoup.parse(notice_code);
                    Elements notice_elements = notice_doc.getElementsByClass("content");
                    if (notice_elements.isEmpty()) {
                        Toast.makeText(MainActivity.this, "验证码已失效，请退出重新登陆后刷新", Toast.LENGTH_LONG).show();
                        get_captcha();

                    }else {
                        String notice = notice_elements.toString().replaceAll("<br>", "\n").replaceAll("<div class=\"content\">", "").replaceAll("</div>", "");
//                        Utils.saveCourseInfo(MainActivity.this,notice,"")
                        Log.d("公告", notice);
                        tv_notice.setText(notice);
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
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

//        navigationView.setNavigationItemSelectedListener(this);

//        View headerView = navigationView.getHeaderView(0);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);//点击跳转侧栏
        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main);
        tv_name = headerLayout.findViewById(R.id.tv_name1);
        tv_notice = findViewById(R.id.tv_notice);
        tv_today_table0 = findViewById(R.id.tv_today_table0);

        TableMain();


        List<Course> list1 = Utils.getCourseInfo(this,"cookies");
        if(list1==null){

            get_captcha();

        }else {
            String session = list1.get(0).getClassRoomName();
            String url_name = "http://jwk.lzu.edu.cn/academic/showHeader.do";
            String Referer = "http://jwk.lzu.edu.cn/academic/index_new.jsp";
            Get_String(url_name,session,Referer);
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
//                Toast.makeText(MainActivity.this,"开发中……",Toast.LENGTH_LONG).show();
                Intent intent_exam = new Intent(MainActivity.this, PersonalPerformance.class);
                startActivity(intent_exam);
                break;
            case R.id.nav_secret:
                //
                Intent intent_password = new Intent(MainActivity.this, ChangePasswordActivity.class);
                startActivity(intent_password);
                break;
            case R.id.nav_calculate:
                //
                Intent intent_nav_calculate = new Intent(MainActivity.this,Calculator.class);
                startActivity(intent_nav_calculate);

                break;

            case R.id.nav_share:
                //


                break;

            case R.id.nav_send:
                //
                Toast.makeText(MainActivity.this,"开发中……",Toast.LENGTH_LONG).show();
                break;

        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void write_file(String file, String week_name){
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(week_name + ".txt", Context.MODE_PRIVATE);
            outputStream.write(file.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void get_captcha(){

        showProgressDialog(this,"验证码加载中……","验证码加载超时！，检查网络后重新登陆");

        OkHttpClient client = new OkHttpClient
                .Builder()
                .connectTimeout(100, TimeUnit.SECONDS) //设置连接超时
                .readTimeout(100, TimeUnit.SECONDS) //设置读超时
                .writeTimeout(100,TimeUnit.SECONDS) //设置写超时
                .retryOnConnectionFailure(false) //是否自动重连
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
                assert response.body() != null;
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

                ArrayList session_list = new ArrayList();
                Course course_list = new Course();

                course_list.setClassRoomName(JSESSIONID);
                session_list.add(course_list);
                Utils.saveCourseInfo(MainActivity.this,"cookies",session_list);



            }
        });


    }

    public void post_sign_in(final String name, String pwd, String captcha) {

        showProgressDialog(this, "登陆中……", "登陆超时！，检查网络后重新登陆");

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
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                .addHeader("Content-Length", "90")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Connection", "keep-alive")
                .addHeader("Cookie", JSESSIONID)
                .addHeader("Host", "jwk.lzu.edu.cn")
                .addHeader("Referer", "http://jwk.lzu.edu.cn/academic/common/security/login.jsp")
                .addHeader("Upgrade-Insecure-Requests", "1")
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
                    assert response.body() != null;
                    String results = response.body().string();
                    Log.i("info_call2success", results);

                    String result;
                    Document doc = Jsoup.parse(results);
                    Elements elements = doc.getElementsByClass("error");
                    //错误提示： 您输入的验证码不正确(密码可能也不正确！!
                    // 错误提示： 密码不匹配!
                    result = elements.text();

//                        Elements elements = doc.select("body");
//                        String result = elements.get(0).text();

                    Message msg = new Message();
                    if (!Objects.equals(result, "")) {
                        Log.d("----------------", result);
                        msg.what = ERROR_SIGN_IN;
                        msg.obj = result;
                    } else {
                        Log.d("================", "成功");
                        msg.what = SUCCESS_SIGN_IN;
                        msg.obj = result;
                    }
                    handler.sendMessage(msg);
                }
            }
        });
    }

        public void showCouseDetails (String about, Activity activity){
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);

            String About = "    此软件为个人开发，将兰州大学教务系统的一些网页操作移动到了手机端。、\n" +
                    "    第一次发布软件，感谢同学们的帮忙测试，物理院（2017、2016级）、资环院（2017级）、生科院（2017级）、外语院（2016级）、药学院（2017级）、已经测试过。" +
                    "但是，由于教务系统中存在太多稀奇古怪的课程和一些任选课，因此，可能会在导入课程表时出现闪退，等问题";
            builder.setTitle("关于");
            builder.setMessage(About);
            android.app.AlertDialog dialog = builder.create();
            dialog.show();
        }

        public void Get_String (String url, String session, String Referer){
            OkHttpClient client = new OkHttpClient
                    .Builder()
                    .connectTimeout(100, TimeUnit.SECONDS) //设置连接超时
                    .readTimeout(100, TimeUnit.SECONDS) //设置读超时
                    .writeTimeout(100, TimeUnit.SECONDS) //设置写超时
                    .retryOnConnectionFailure(true) //是否自动重连
                    .build(); //构建OkHttpClient对象

            Request captcha_response = new Request.Builder()
                    .url(url)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .addHeader("Accept-Encoding", "gzip, deflate")
                    .addHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                    .addHeader("Cookie", session)
                    .addHeader("Connection", "keep-alive")
                    .addHeader("Host", "jwk.lzu.edu.cn")
                    .addHeader("Referer", Referer)
                    .addHeader("Upgrade-Insecure-Requests", "1")
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
                            msg.what = NAME;
                            msg.obj = results;
                            handler.sendMessage(msg);
                        }
                    }

                }
            });
        }
    public void Get_Notice (String url, String session, String Referer){
        OkHttpClient client = new OkHttpClient
                .Builder()
                .connectTimeout(100, TimeUnit.SECONDS) //设置连接超时
                .readTimeout(100, TimeUnit.SECONDS) //设置读超时
                .writeTimeout(100, TimeUnit.SECONDS) //设置写超时
                .retryOnConnectionFailure(false) //是否自动重连
                .build(); //构建OkHttpClient对象

        Request captcha_response = new Request.Builder()
                .url(url)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                .addHeader("Cookie", session)
                .addHeader("Connection", "keep-alive")
                .addHeader("Host", "jwk.lzu.edu.cn")
                .addHeader("Referer", Referer)
                .addHeader("Upgrade-Insecure-Requests", "1")
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
                        msg.what = NOTICE;
                        msg.obj = results;
                        handler.sendMessage(msg);
                    }
                }

            }
        });
    }


        public void showProgressDialog (Context mContext, String text,final String dismiss_text){
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
                    if (dismissProgressDialog()) {
                        Toast.makeText(MainActivity.this, dismiss_text, Toast.LENGTH_SHORT).show();
                    }
                }
            }, 10000);//超时时间10秒
        }
        public Boolean dismissProgressDialog () {
            if (progressDialog != null) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    return true;//取消成功
                }
            }
            return false;//已经取消过了，不需要取消
        }

    @SuppressLint("SetTextI18n")
    public void TableMain() {
        int today_week = now_week();
        ArrayList list = new ArrayList();
        List<Course> list1 = Utils.getCourseInfo(this, "week_" + today_week);
        List<Course> list2 = Utils.getCourseInfo(this, "2_week_" + today_week);
        list.add(list1);
        list.add(list2);
        Calendar c_now = Calendar.getInstance();

        int USA = c_now.get(Calendar.DAY_OF_WEEK);
        int todayzhou;
        if (USA == 1) {
            todayzhou = 7;
        } else {
            todayzhou = USA - 1;
        }

        tv_today_table0.setText("今日课程" + "（第" + String.valueOf(today_week) + "周,周" + String.valueOf(todayzhou) + "）");

        for (int i =0; i < 2; i ++){
            if (list.get(i) != null) {


                ArrayList today_lesson = new ArrayList();

                Performance performance = new Performance();
                performance.setCourseName("课程名");
                performance.setCourseProperty("地点");
                performance.setPass("上课时间");
                today_lesson.add(performance);

                for (final Course c1 : (List<Course>) list.get(i)) {
                    Log.d("week", ":========>>" + c1.getDay() + todayzhou);

                    if (c1.getDay() == todayzhou) {

                        int time_start = c1.getJieci();
                        int time_end = c1.getJieci() + c1.getSpanNum() - 1;

                        Performance performance1 = new Performance();

                        performance1.setCourseName(c1.getClassName());
                        performance1.setCourseProperty(c1.getClassRoomName());
                        performance1.setPass(String.valueOf(time_start) + "-" + String.valueOf(time_end));
                        today_lesson.add(performance1);

                    }


                }


                TableLayout layout = findViewById(R.id.today_table);

                TableLayout.LayoutParams layoutParam = new TableLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT); // 定义布局管理器的参数

                layoutParam.topMargin = 20;
                layoutParam.leftMargin = 20;
                layoutParam.rightMargin = 20;

                layoutParam.gravity = 20;
                int flag = 0;

                for (final Performance performance0 : (List<? extends Performance>) today_lesson) { // 循环设置表格行

                    if (flag == 0) {
                        TableRow row0 = new TableRow(this); // 定义表格行


                        int blue = getResources().getColor(colorPrimary);
                        row0.setBackgroundColor(blue);
                        int white = getResources().getColor(color_white);

                        row0.addView(draw_socre(performance0.getCourseName(), white), 0); // 加入一个编号
                        row0.addView(draw_socre(performance0.getCourseProperty(), white), 1); // 加入一个编号
                        row0.addView(draw_socre(performance0.getPass(), white), 2); // 加入一个编号

                        layout.addView(row0); // 向表格之中增加若干个表格行


                        flag++;

                    } else {

                        TableRow row = new TableRow(this); // 定义表格行
                        int blue = getResources().getColor(colorPrimary);

                        row.addView(draw_socre(performance0.getCourseName(), blue), 0); // 加入一个编号
                        row.addView(draw_socre(performance0.getCourseProperty(), blue), 1); // 加入一个编号
                        row.addView(draw_socre(performance0.getPass(), blue), 2); // 加入一个编号

                        layout.addView(row); // 向表格之中增加若干个表格行
                    }
                }
            }
        }


    }




        TextView draw_socre(final String course,int color){

            TextView text = new TextView(this);
            text.setPadding(15,15,15,15);
            text.setText(course); // 设置文本内容
            text.setTextColor(color);
            text.setGravity(Gravity.CENTER);

            return text;
        }


    public int now_week(){

        int weeks = 1;
        List<Course> startWeekDate = Utils.getCourseInfo(this,"startWeekDate");
        if(startWeekDate != null){
            int startDay = startWeekDate.get(0).getJieci();
            int startWeek = startWeekDate.get(0).getStartWeek();
            int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
            weeks = (today - startDay)/7 + startWeek;
        }else {
            Toast.makeText(MainActivity.this,"请先导入课程表数据",Toast.LENGTH_SHORT).show();
        }
        return weeks;

    }


}


