package com.zrs.yuh.rs;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import android.view.Menu;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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
    protected static final int NEW = 5;

    private TextView tv_name,tv_notice,tv_today_table0,tv_today_table_before,tv_today_table_now,tv_today_table_late;

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
                case NEW:

                    String update_results = (String) msg.obj;

                    Document document = Jsoup.parse(update_results);

                    String version = "1.2";
                    String version0 = document.getElementsByClass("list_app_info").text();

                    String update_info = document.getElementsByClass("apk_left_title_info").first().toString();
                    if (!version0.equals(version)) {

                        Toast.makeText(MainActivity.this, "发现最新版本！!", Toast.LENGTH_LONG).show();

                        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
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
                        Log.d("版本=======》","已是最新版本！!");
                    }

                    break;

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

                        ArrayList name_list = new ArrayList();
                        Course course_name = new Course();
                        course_name.setClassName(name);
                        name_list.add(course_name);
                        boolean name_true  = Utils.saveCourseInfo(MainActivity.this,"name",name_list);
                        if (name_true){
                            Log.d("姓名",name);
                        }

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
                        tv_notice.setText(notice);

                        ArrayList notice_list = new ArrayList();
                        Course course_notice = new Course();
                        course_notice.setClassName(notice);
                        notice_list.add(course_notice);
                        boolean notice1 = Utils.saveCourseInfo(MainActivity.this, "notice", notice_list);
                        if (notice1){
                            Log.d("公告",notice);
                        }

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
        update();


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
        tv_today_table_before = findViewById(R.id.tv_today_table_before);
        tv_today_table_now = findViewById(R.id.tv_today_table_now);
        tv_today_table_late = findViewById(R.id.tv_today_table_late);


        final int today_week = now_week();
        Calendar c_now = Calendar.getInstance();
        int USA = c_now.get(Calendar.DAY_OF_WEEK);
        final int todayzhou;
        if (USA == 1) {
            todayzhou = 7;
        } else {
            todayzhou = USA - 1;
        }

        ArrayList list_BNL = new ArrayList();
        Course course_BNL = new Course();

        course_BNL.setSpanNum(todayzhou);
        course_BNL.setJieci(today_week);

        list_BNL.add(course_BNL);
        Utils.saveCourseInfo(MainActivity.this,"now_day",list_BNL);

        TableMain(today_week,todayzhou);

        tv_today_table_before.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<Course> list_B = Utils.getCourseInfo(MainActivity.this,"now_day");
                if (list_B!=null){

                    int now_day = list_B.get(0).getSpanNum() - 1;
                    int now_week = list_B.get(0).getJieci();
                    if (now_day==0){
                        now_day = 7;
                        now_week = now_week - 1;
                        if (now_week==0){
                            now_week = 18;
                        }
                    }

                    TableMain(now_week ,now_day);

                    ArrayList list_B0 = new ArrayList();
                    Course course_B0 = new Course();

                    course_B0.setJieci(now_week);
                    course_B0.setSpanNum(now_day);

                    list_B0.add(course_B0);
                    Utils.saveCourseInfo(MainActivity.this,"now_day",list_B0);


                }
            }
        });
        tv_today_table_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TableMain(today_week,todayzhou);

                ArrayList list_BNL = new ArrayList();
                Course course_BNL = new Course();

                course_BNL.setJieci(today_week);
                course_BNL.setSpanNum(todayzhou);

                list_BNL.add(course_BNL);
                Utils.saveCourseInfo(MainActivity.this,"now_day",list_BNL);

            }
        });
        tv_today_table_late.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<Course> list_L = Utils.getCourseInfo(MainActivity.this,"now_day");
                if (list_L!=null){

                    int now_day = list_L.get(0).getSpanNum() + 1;
                    int now_week = list_L.get(0).getJieci();
                    if (now_day==8){
                        now_day = 1;
                        now_week = now_week + 1;
                        if (now_week==19){
                            now_week = 1;
                        }
                    }
                    TableMain(now_week,now_day);

                    ArrayList list_L0 = new ArrayList();
                    Course course_L0 = new Course();

                    course_L0.setJieci(now_week);
                    course_L0.setSpanNum(now_day);

                    list_L0.add(course_L0);
                    Utils.saveCourseInfo(MainActivity.this,"now_day",list_L0);


                }
            }
        });



        //有名字的话，就不再登陆，直接载入

        List<Course> list_name = Utils.getCourseInfo(this,"name");
        if (list_name==null){
            get_captcha();

        }else {
            tv_name.setText(list_name.get(0).getClassName());

            //有公告的话，直接载入

            List<Course> list_notice = Utils.getCourseInfo(this,"notice");

            if (list_notice!=null){
                tv_notice.setText(list_notice.get(0).getClassName());
            }
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.login_main:
                get_captcha();

                break;

            case R.id.about_main:
                Intent intent = new Intent(MainActivity.this,About.class);
                startActivity(intent);

                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
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
                Toast.makeText(MainActivity.this,"开发中……",Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_send:
                //
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

                builder.setTitle("联系开发者");
                builder.setMessage("    请联系QQ1946991005，可点击直接跳转QQ或TIM，进入我的聊天界面(随风而去）,谢谢");
                android.app.AlertDialog dialog = builder.create();
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "跳转至QQ",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (checkApkExist(MainActivity.this, "com.tencent.mobileqq")){
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin="+1946991005+"&version=1")));
                                }else{
                                    Toast.makeText(MainActivity.this,"本机未安装QQ或TIM应用",Toast.LENGTH_SHORT).show();
                                }

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

        }



        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean checkApkExist(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
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
            progressDialog.setCancelable(true);//点击屏幕和按返回键都不能取消加载框
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
            }, 30000);//超时时间10秒
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
    public void TableMain(int today_week,int todayzhou) {

        TableLayout layout = findViewById(R.id.today_table);
        layout.removeAllViews();

        ArrayList list = new ArrayList();
        List<Course> list1 = Utils.getCourseInfo(this, "week_" + today_week);
        List<Course> list2 = Utils.getCourseInfo(this, "2_week_" + today_week);

        //指定周的课程信息
        list.add(list1);
        list.add(list2);


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

                        //此周中，星期几符合的课程信息添加到Performance对应的对象中

                        int time_start = c1.getJieci();
                        int time_end = c1.getJieci() + c1.getSpanNum() - 1;

                        Performance performance1 = new Performance();

                        performance1.setCourseName(c1.getClassName());
                        performance1.setCourseProperty(c1.getClassRoomName());
                        performance1.setPass(String.valueOf(time_start) + "-" + String.valueOf(time_end));
                        performance1.setCourseNum(time_start);
                        today_lesson.add(performance1);
                        //集中到今天的列表中

                    }


                }

                TableLayout.LayoutParams layoutParam = new TableLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT); // 定义布局管理器的参数

                layoutParam.topMargin = 20;
                layoutParam.leftMargin = 20;
                layoutParam.rightMargin = 20;

                layoutParam.gravity = 20;
                int flag = 0;

                //用于排序
                Collections.sort(today_lesson,new SortByTime());

                Utils.savePerformanceInfo(MainActivity.this, "today_lesson", today_lesson);


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
                        row.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showCouseDetails(performance0.getCourseName(),
                                        "地点：" + performance0.getCourseProperty() +   "\n" +
                                                "时间：" + performance0.getPass() + "节课");
                            }
                        });
                        int blue = getResources().getColor(colorPrimary);

                        String course_name0;
                        if (performance0.getCourseName().length()>8){
                            course_name0 = performance0.getCourseName().substring(0,7) + "…";
                        }else {
                            course_name0 = performance0.getCourseName();
                        }

                        row.addView(draw_socre(course_name0, blue), 0); // 加入一个编号


                        String Course_Property;
                        if (performance0.getCourseProperty().length()>5){
                            Course_Property = performance0.getCourseProperty().substring(0,4) + "…";
                        }else {
                            Course_Property = performance0.getCourseProperty();
                        }

                        row.addView(draw_socre(Course_Property, blue), 1); // 加入一个编号


                        row.addView(draw_socre(performance0.getPass(), blue), 2); // 加入一个编号

                        layout.addView(row); // 向表格之中增加若干个表格行
                    }
                }
            }
        }


    }

    class SortByTime implements Comparator {
        public int compare(Object o1, Object o2) {
            Performance p1 = (Performance) o1;
            Performance p2 = (Performance) o2;
            if (p1.getCourseNum()>=p2.getCourseNum())return 1;return -1;

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

    public void showCouseDetails (String course_name,String other){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

        builder.setTitle(course_name);
        builder.setMessage(other);
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void update(){
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




}


