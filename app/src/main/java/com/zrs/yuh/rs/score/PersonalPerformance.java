package com.zrs.yuh.rs.score;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.zrs.yuh.rs.Function.Utils;
import com.zrs.yuh.rs.R;
import com.zrs.yuh.rs.timetable.Course;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.zrs.yuh.rs.R.color.colorPrimary;
import static com.zrs.yuh.rs.R.color.color_white;

public class PersonalPerformance extends AppCompatActivity {

    private List<? extends Performance> socre_list;


    String PersonalPerformance = "";

    protected static final int LESSON_0 = 0;
    protected static final int LESSON_URL = 1;
    protected static final int LESSON = 2;
    protected static final int COURSE = 3;
    protected static final int COURSE_ERROR = 4;

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {

        @Override

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String lesson_url;
            switch (msg.what) {
                case LESSON_0:
                    String code = (String) msg.obj;
                    Log.d("lesson", code);
                    get_Performance_url(code);
                    break;

                case LESSON_URL:
                    lesson_url = (String) msg.obj;

                    Log.d("lesson", lesson_url);

                    String Referer = "http://jwk.lzu.edu.cn/academic/listLeft.do";
                    List<Course> cookies_p = Utils.getCourseInfo(PersonalPerformance.this,"cookies");
                    String session = cookies_p.get(0).getClassRoomName();

                    //解析出来带有课程表的网页源码
                    Get_Performance_code(lesson_url, session, Referer);

                    break;

                case LESSON:
                    String Performance_code = (String )msg.obj;
                    Log.d("lesson_main ========>", Performance_code);
                    write_file(Performance_code,"个人成绩查询源码");
                    write_file_sdcard("兰州大学个人成绩查询错误信息","兰大个人成绩查询错误信息请将此文件发送至QQ1946991005.html",Performance_code);
                    Get_Performance(Performance_code);
                    break;
                case COURSE:
                    ArrayList score_a = (ArrayList) msg.obj;
                    updateSocreViews(score_a);
                    break;
                case COURSE_ERROR:
                    String code_error = (String )msg.obj;
                    Toast.makeText(PersonalPerformance.this,code_error,Toast.LENGTH_LONG).show();
                    break;



                default:
                    break;
            }
        }
    };


    // 定义要显示的数据

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance);


        List<Course> cookies_p = Utils.getCourseInfo(this,"cookies");
        String session = cookies_p.get(0).getClassRoomName();


        String url = "http://jwk.lzu.edu.cn/academic/listLeft.do";
        String Referer = "http://jwk.lzu.edu.cn/academic/showHeader.do";
        Get_listLeft(url, session, Referer);

    }
    public void updateSocreViews(List<? extends Performance> socre_list) {
        this.socre_list = socre_list;
        init();
    }

    @SuppressLint("ResourceAsColor")
    public void init(){
        TableLayout layout = findViewById(R.id.s_table);
        layout.removeAllViews();


        TableLayout.LayoutParams layoutParam = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT); // 定义布局管理器的参数

        layoutParam.topMargin = 30;
        layoutParam.leftMargin = 30;
        layoutParam.rightMargin = 30;

        layoutParam.gravity = 30;

        int flag = 0;

        for ( final Performance performance: socre_list) { // 循环设置表格行
            if (flag == 0){
                TableRow row0 = new TableRow(this); // 定义表格行

                int blue = getResources().getColor(colorPrimary);
                row0.setBackgroundColor(blue);
                int white = getResources().getColor(color_white);

                row0.addView(draw_socre(performance.getCourseName(),white), 0); // 加入一个编号
                row0.addView(draw_socre(performance.getTerminal(),white), 1); // 加入一个编号
                row0.addView(draw_socre(performance.getGeneralComment(),white), 2); // 加入一个编号
                row0.addView(draw_socre(performance.getCredit(),white), 3); // 加入一个编号
                layout.addView(row0); // 向表格之中增加若干个表格行
                flag++;

            }else {

                TableRow row = new TableRow(this); // 定义表格行
                int blue = getResources().getColor(colorPrimary);

                row.addView(draw_socre(performance.getCourseName(), blue), 0); // 加入一个编号
                row.addView(draw_socre(performance.getTerminal(), blue), 1); // 加入一个编号
                row.addView(draw_socre(performance.getGeneralComment(), blue), 2); // 加入一个编号
                row.addView(draw_socre(performance.getCredit(), blue), 3); // 加入一个编号

                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showCouseDetails(performance);

                    }
                });
                layout.addView(row); // 向表格之中增加若干个表格行
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

    /**
     * 弹出窗口，显示课程详细信息
     *
     */
    @SuppressLint("ResourceType")
    public void showCouseDetails(Performance performance) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String credit_s = "学分：" + performance.getCredit();
        String courseProperty_s = "选课属性：" + performance.getCourseProperty();
        String dailyPerformance_s = "平时成绩：" + performance.getDailyPerformance();
        String midterm_s = "期中成绩：" + performance.getMidterm();
        String terminal_s = "期末成绩：" + performance.getTerminal();
        String generalComment_s = "总评成绩：" + performance.getGeneralComment();
        String All = credit_s + "\n" + courseProperty_s + "\n" +  dailyPerformance_s + "\n" + midterm_s + "\n" + terminal_s + "\n" + generalComment_s;

        builder.setTitle(performance.getCourseName());
        builder.setMessage(All);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.personal_performance, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        List<Course> list1 = Utils.getCourseInfo(PersonalPerformance.this,"cookies");
        String session = list1.get(0).getClassRoomName();

        switch (id) {
            case R.id.Performance_time_change0:

                String url0 = PersonalPerformance + "&year=34&term=2&para=0&sortColumn=&Submit=%E6%9F%A5%E8%AF%A2";
                Get_Performance_code(url0,session,PersonalPerformance);

                break;
            case R.id.Performance_time_change1:

                String url1 = PersonalPerformance + "&year=35&term=1&para=0&sortColumn=&Submit=%E6%9F%A5%E8%AF%A2";
                Get_Performance_code(url1,session,PersonalPerformance);

                break;
            case R.id.Performance_time_change2:

                String url2 = PersonalPerformance + "&year=35&term=2&para=0&sortColumn=&Submit=%E6%9F%A5%E8%AF%A2";
                Get_Performance_code(url2,session,PersonalPerformance);

                break;
            case R.id.Performance_time_change3:

                String url3 = PersonalPerformance + "&year=36&term=1&para=0&sortColumn=&Submit=%E6%9F%A5%E8%AF%A2";
                Get_Performance_code(url3,session,PersonalPerformance);

                break;
            case R.id.Performance_time_change4:

                String url4 = PersonalPerformance + "&year=36&term=2&para=0&sortColumn=&Submit=%E6%9F%A5%E8%AF%A2";
                Get_Performance_code(url4,session,PersonalPerformance);

                break;
            case R.id.Performance_time_change5:

                String url5 = PersonalPerformance + "&year=37&term=1&para=0&sortColumn=&Submit=%E6%9F%A5%E8%AF%A2";
                Get_Performance_code(url5,session,PersonalPerformance);

                break;

            case R.id.Performance_time_change6:

                String url6 = PersonalPerformance + "&year=37&term=2&para=0&sortColumn=&Submit=%E6%9F%A5%E8%AF%A2";
                Get_Performance_code(url6,session,PersonalPerformance);

                break;

            case R.id.Performance_time_change7:

                String url7 = PersonalPerformance + "&year&term&para=0&sortColumn=&Submit=%E6%9F%A5%E8%AF%A2";
                Get_Performance_code(url7,session,PersonalPerformance);

                break;

            default:
                break;

        }

        return super.onOptionsItemSelected(item);
    }


    public void Get_listLeft(String url, String session, String Referer) {

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
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0")
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

                    if (!Objects.equals(results, "")) {
                        Message msg = new Message();
                        msg.what = LESSON_0;
                        msg.obj = results;
                        handler.sendMessage(msg);
                    }
                }
            }
        });
    }

    public void get_Performance_url(String listLeft_code) {

        Document doc = Jsoup.parse(listLeft_code);
        if(doc.getElementById("li13")==null){ //验证是否session失效，防止请求不到数据闪退
            Toast.makeText(PersonalPerformance.this,"验证码已失效，请退出重新登陆后刷新",Toast.LENGTH_LONG).show();
        }else{

            String PersonalPerformance0 = doc.getElementById("li17").select("a").get(1).attr("href");
            PersonalPerformance = "http://jwk.lzu.edu.cn/academic" + PersonalPerformance0.substring(1);

            Log.d("个人成绩查询链接（完整）========》", PersonalPerformance);

            //显示出来带有课程表的网页源码
            Message msg = new Message();
            msg.what = LESSON_URL;
            msg.obj = PersonalPerformance;
            handler.sendMessage(msg);
        }

    }

    public void Get_Performance_code(final String url, String session, String Referer) {

        OkHttpClient okHttpClient = new OkHttpClient
                .Builder()
                .connectTimeout(100, TimeUnit.SECONDS) //设置连接超时
                .readTimeout(100, TimeUnit.SECONDS) //设置读超时
                .writeTimeout(100, TimeUnit.SECONDS) //设置写超时
                .retryOnConnectionFailure(true) //是否自动重连
                .build(); //构建OkHttpClient对象

        Request lesson1_response = new Request.Builder()
                .url(url)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                .addHeader("Cookie", session)
                .addHeader("Connection", "keep-alive")
                .addHeader("Host", "jwk.lzu.edu.cn")
                .addHeader("Referer", Referer)
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0")
                .build();

        okHttpClient.newCall(lesson1_response).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                Log.i("lesson_callFailure", e.toString());
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String results = response.body().string();
//                    Log.i("info_name_success", results);

                    if (!Objects.equals(results, "")) {
                        Message msg = new Message();
                        msg.what = LESSON;
                        msg.obj = results;
                        handler.sendMessage(msg);

                    }
                }
            }
        });

    }

    public void Get_Performance(String lesson_code) {
        ArrayList socre = new ArrayList();

        Performance performance0 = new Performance();
        performance0.setCourseName("课程名");
        performance0.setTerminal("期末");
        performance0.setGeneralComment("总评");
        performance0.setCredit("学分");
        socre.add(performance0);

        Document doc = Jsoup.parse(lesson_code);
        int n = doc.select("table").size();//无课程时，会有一个”table“，但是，请求不到下面的数据会闪退
        if (n != 1) {
            Elements Courses = doc.select("table").get(1).select("tr");
            for (int i = 1; i < Courses.size(); i++) {
                Elements Course = Courses.get(i).select("td");
                Performance performance = new Performance();
                if (Course.size() == 18) {
                    String lesson_name = Course.get(3).text();

                    if (lesson_name.length()>8){
                        lesson_name = lesson_name.substring(0,7) + "…";
                    }
                    performance.setCourseName(lesson_name);
                    performance.setDailyPerformance(Course.get(5).text());
                    performance.setMidterm(Course.get(6).text());
                    performance.setTerminal(Course.get(7).text());
                    performance.setGeneralComment(Course.get(8).text());
                    performance.setCredit(Course.get(9).text());
                    performance.setCourseProperty(Course.get(12).text());
                    performance.setPass(Course.get(17).text());

                    socre.add(performance);

                }
            }
            Message msg = new Message();
            msg.what = COURSE;
            msg.obj = socre;
            handler.sendMessage(msg);
        }else {
            String results = "这学期还没考试吧？";
            Message msg = new Message();
            msg.what = COURSE_ERROR;
            msg.obj = results;
            handler.sendMessage(msg);
        }


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
    public void write_file_sdcard(String dirName, String fileName, String code)  {

        try {
            String path = Environment.getExternalStorageDirectory().getPath()+"/"+fileName;
            File file = new File(path);
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(code.getBytes());
                outputStream.close();
                Toast.makeText(PersonalPerformance.this,"保存成功",Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

}
