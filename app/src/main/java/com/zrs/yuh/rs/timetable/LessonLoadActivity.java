package com.zrs.yuh.rs.timetable;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.zrs.yuh.rs.Data;
import com.zrs.yuh.rs.R;
import com.zrs.yuh.rs.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by nolov on 2018/2/23.
 */

public class LessonLoadActivity extends AppCompatActivity {

    protected static final int LESSON_0 = 0;
    protected static final int LESSON_URL = 1;
    protected static final int LESSON = 2;

    private ProgressDialog progressDialog;

    List<Course> list1 = new ArrayList<>();
    List<Course> list2 = new ArrayList<>();
    List<Course> list3 = new ArrayList<>();
    List<Course> list4 = new ArrayList<>();
    List<Course> list5 = new ArrayList<>();
    List<Course> list6 = new ArrayList<>();
    List<Course> list7 = new ArrayList<>();
    List<Course> list8 = new ArrayList<>();
    List<Course> list9 = new ArrayList<>();
    List<Course> list10 = new ArrayList<>();
    List<Course> list11 = new ArrayList<>();
    List<Course> list12 = new ArrayList<>();
    List<Course> list13 = new ArrayList<>();
    List<Course> list14 = new ArrayList<>();
    List<Course> list15 = new ArrayList<>();
    List<Course> list16 = new ArrayList<>();
    List<Course> list17 = new ArrayList<>();
    List<Course> list18 = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);


        int NowWeek = now_week();
        List<Course> list_test = Utils.getCourseInfo(this,"week_1");
        if(list_test!=null){
            Toast.makeText(LessonLoadActivity.this, "当前周为第" + NowWeek + "周", Toast.LENGTH_SHORT).show();
            List<Course> list = Utils.getCourseInfo(this,"week_"+NowWeek);
            table(list);
        }else {
            Refresh();
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.lesson_refush, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.refresh:
                Refresh();
                break;

            case R.id.get_week_two:
                Intent intent = new Intent(LessonLoadActivity.this,Lesson2LoadActivity.class);
                startActivity(intent);
                break;

            case R.id.week_1:
                ChangeWeek("week_1");
                break;

            case R.id.week_2:
                ChangeWeek("week_2");
                break;

            case R.id.week_3:
                ChangeWeek("week_3");
                break;

            case R.id.week_4:
                ChangeWeek("week_4");
                break;

            case R.id.week_5:
                ChangeWeek("week_5");
                break;

            case R.id.week_6:
                ChangeWeek("week_6");
                break;

            case R.id.week_7:
                ChangeWeek("week_7");
                break;

            case R.id.week_8:
                ChangeWeek("week_8");
                break;

            case R.id.week_9:
                ChangeWeek("week_9");
                break;

            case R.id.week_10:
                ChangeWeek("week_10");
                break;

            case R.id.week_11:
                ChangeWeek("week_11");
                break;

            case R.id.week_12:
                ChangeWeek("week_12");
                break;

            case R.id.week_13:
                ChangeWeek("week_13");
                break;

            case R.id.week_14:
                ChangeWeek("week_14");
                break;

            case R.id.week_15:
                ChangeWeek("week_15");
                break;

            case R.id.week_16:
                ChangeWeek("week_16");
                break;

            case R.id.week_17:
                ChangeWeek("week_17");
                break;

            case R.id.week_18:
                ChangeWeek("week_18");
                break;

            default:
                    break;

        }



        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {

        @Override

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String lesson_url = "error";
            switch (msg.what) {
                case LESSON_0:
                    String code = (String) msg.obj;
                    Log.d("lesson", code);
                    get_lesson_url(code);
                    break;

                case LESSON_URL:
                    lesson_url = (String) msg.obj;

                    Log.d("lesson", lesson_url);

                    String Referer = "http://jwk.lzu.edu.cn/academic/listLeft.do";
                    final Data session_Application = (Data) getApplication();
                    String session = session_Application.getData_s();

                    //解析出来带有课程表的网页源码
                    Get_lesson(lesson_url, session, Referer);

                    break;

                case LESSON:
                    String lesson_code = (String )msg.obj;
                    Log.d("lesson_main ========>", lesson_code);
                    write_file(lesson_code,"网页源码");
                    write_file_sdcard("兰州大学课程表错误信息","兰大课程表错误信息请将此文件发送至QQ1946991005.html",lesson_code);
                    Get_Course(lesson_code);
                    break;

                default:
                    break;
            }
        }
    };

    public void Refresh(){

        showProgressDialog(this,"课程表数据导入中……","课程表数据导入超时！，检查网络后重新登陆");

        final Data session_app = (Data) getApplication();
        String session = session_app.getData_s();

        String url = "http://jwk.lzu.edu.cn/academic/listLeft.do";
        String Referer = "http://jwk.lzu.edu.cn/academic/showHeader.do";
        Get_lesson0(url, session, Referer);

    }
    public void ChangeWeek(String week_name){
        Toast.makeText(LessonLoadActivity.this,"第" + week_name.substring(5) + "周",Toast.LENGTH_LONG).show();
        List<Course> list = Utils.getCourseInfo(this,week_name);
        table(list);
    }


    public void Get_lesson0(String url, String session, String Referer) {

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
                    String results = response.body().string();

                    if (!Objects.equals(results, "")) {
                        startWeek(results);
                        Message msg = new Message();
                        msg.what = LESSON_0;
                        msg.obj = results;
                        handler.sendMessage(msg);
                    }
                }
            }
        });
    }

    public void get_lesson_url(String listLeft_code) {

        Document doc = Jsoup.parse(listLeft_code);
        if(doc.getElementById("li13")==null){ //验证是否session失效，防止请求不到数据闪退
            Toast.makeText(LessonLoadActivity.this,"验证码已失效，请退出重新登陆后刷新",Toast.LENGTH_LONG).show();
        }else{
            String lesson_url0 = doc.getElementById("li13").select("a").first().attr("href");
            Log.d("课程表链接（部分）======》", lesson_url0);
            String lesson_url1 = "http://jwk.lzu.edu.cn/academic/accessModule.do?moduleId=2000&groupId=&";

            String lesson_url2 = lesson_url0.substring(18);
            String lesson_url = lesson_url1 + lesson_url2;
            Log.d("课程链接（完整）========》", lesson_url);

            //显示出来带有课程表的网页源码
            Message msg = new Message();
            msg.what = LESSON_URL;
            msg.obj = lesson_url;
            handler.sendMessage(msg);
        }

    }

    public void Get_lesson(String url, String session, String Referer) {
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
            public void onFailure(okhttp3.Call call, @NonNull IOException e) {
                Log.i("lesson_callFailure", e.toString());
            }

            @Override
            public void onResponse(okhttp3.Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
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

    public void Get_Course(String lesson_code) {

        Document doc = Jsoup.parse(lesson_code);
        Elements lessons = doc.select("table").get(3).getElementsByClass("infolist_common");

        int course_n = lessons.size();

        System.out.println(course_n + "");
        String course_name[] = new String[course_n];
        String course_teacher[] = new String[course_n];
        String course_credit[] = new String[course_n];
        int course_color[] = new int[course_n];
        ArrayList course_week_s;//有哪些周


        StringBuilder lesson_s = new StringBuilder();

        for (int i = 0; i < course_n; i++) {

            course_name[i] = lessons.get(i).select("td").get(2).text();
//            Log.d("课程名：", course_name[i]);

            Elements course_teachers = lessons.get(i).select("td").get(3).select("a");
            for (Element teacher :course_teachers ) {
                course_teacher[i] = course_teacher[i] + teacher.text() + "\n";
            }
            if (course_teacher[i]==null) {
                course_teacher[i] = "暂无授课教师";
            }else {
                course_teacher[i] = course_teacher[i].substring(4);
            }
//            Log.d("授课教师:", course_teacher[i]);

            course_credit[i] = lessons.get(i).select("td").get(4).text();
            if (course_credit[i].isEmpty()) {
                course_credit[i] = "暂无";
            }
//            Log.d("学分：", course_credit[i]);

            Elements net = lessons.get(i).select("td").get(9).select("table");
            if (net.isEmpty()){
                System.out.println("网络共享课：“"+course_name[i]+"”已忽略");
            }else {
                Elements week_num = net.select("tr");
                int week_n = week_num.size();

                String course_weeks[][] = new String[course_n][week_n];//course_n这门课一周中第week_n次课，有哪些周上课，原始文件字符串
                String course_week[][] = new String[course_n][week_n];//周几上课
                String course_time[][] = new String[course_n][week_n];
                String course_room[][] = new String[course_n][week_n];

                String course = "每周" + week_n + "节课:\n";
                String lesson_teacher = course_teacher[i];
                String lesson_name = course_name[i];
                String lesson_credit = course_credit[i];


                String ClassAll0 =
                        course_name[i] + "\n" +
                                course_teacher[i] + "\n" +
                                "学分：" + course_credit[i] + "\n";
                for (int k = 0; k < week_n; k++) {
                    course_weeks[i][k] = week_num.get(k).select("td").get(0).text();
                    course_week_s = odd_even_week(course_weeks[i][k]);
                    String lesson_week = course_weeks[i][k];

//                Log.d("上课周数：");

                    course_week[i][k] = week_num.get(k).select("td").get(1).text();
                    if (course_week[i][k].isEmpty()) {
                        course_week[i][k] = "暂无";
                    }
//                Log.d("上课周几：", course_week[i][k]);

                    course_time[i][k] = week_num.get(k).select("td").get(2).text();
                    if (course_time[i][k].isEmpty()) {
                        course_time[i][k] = "暂无";
                    }
//                  Log.d("上课时间：", course_time[i][k]);

                    int span_num = 0;
                    int first_jie = 0;
                    if (course_time[i][k].isEmpty()) {
                        course_time[i][k] = "暂无节数";
                    }else {

                        Pattern p = Pattern.compile("[^0-9]");
                        Matcher m = p.matcher(course_time[i][k]);
                        String result = m.replaceAll("");

                        if (Integer.parseInt(result.substring(1)) > 12) {
                            span_num = Integer.parseInt(result.substring(2)) - Integer.parseInt(result.substring(0, 2));
                            first_jie = Integer.parseInt(result.substring(0, 2));
                        } else {
                            span_num = Integer.parseInt(result.substring(1)) - Integer.parseInt(result.substring(0, 1))+1;
                            first_jie = Integer.parseInt(result.substring(0, 1));
                        }
                    }

                    course_room[i][k] = week_num.get(k).select("td").get(3).text();
                    if (course_room[i][k].isEmpty()) {
                        course_room[i][k] = "暂无地点";
                    }
//                  Log.d("上课地点：", course_room[i][k]);

                    if(span_num!=1){

                        String lesson_room = course_room[i][k];

                        String ClassAll =ClassAll0 + course_room[i][k];
                        Course c = new Course();

                        c.setClassTeacherName(lesson_teacher);
                        c.setClassName(lesson_name);
                        c.setClassTypeName(lesson_credit);
                        c.setClassRoomName(lesson_room);
                        c.setClassWeek(lesson_week);

                        c.setDay(course_week[i][k]);
                        c.setSpanNum(span_num);
                        c.setJieci(first_jie);
                        c.setCourseColor(i);
                        c.setClassAll(ClassAll);

                        if (i ==course_n-1 && k == week_n - 1){
                            add_course(course_week_s,c,true);
                        }else {
                            add_course(course_week_s,c,false);
                        }

                        course = ClassAll0 +
                                "周数：" + course_weeks[i][k] + "\n" +
                                "周几：" + course_week[i][k] + "\n" +
                                "时间：" + course_time[i][k] + "\n" +
                                "第"+ first_jie +"节"  + "" + "\n"+
                                "节数:" + span_num + "\n" +
                                "地点：" + course_room[i][k] + "\n" ;

                        lesson_s.insert(0, course);
                    }

                }


            }
        }
        Log.d("lesson===========>", lesson_s.toString());
    }

    public void table(List<Course> list){
        CourseTableView courseTableView = findViewById(R.id.ctv);
        courseTableView.updateCourseViews(list,this);
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
                Toast.makeText(LessonLoadActivity.this,"保存成功",Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(LessonLoadActivity.this,"SDCard不存在或不可写\n请前往系统设置中授予该软件，读写手机储存权限",Toast.LENGTH_SHORT).show();

        }
    }
    public ArrayList odd_even_week(String odd_even_week_code){
        ArrayList course_weeks=new ArrayList();
        ArrayList odd_week = new ArrayList();
        ArrayList even_week = new ArrayList();

        Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]");//中文字符
        Matcher matcher = pattern.matcher(odd_even_week_code);
        String course_week1 = matcher.replaceAll("");
        String course_week_d[] = course_week1.split(",");

        String course_week_dd[] = odd_even_week_code.split(",");
        for (int dd =0; dd < course_week_dd.length ; dd++){
            for (int s =0; s < course_week_dd[dd].length() ;s++){
                char  item =  course_week_dd[dd].charAt(s);
                if (String.valueOf(item).equals("单")){
                    odd_week.add(dd);
                }else if (String.valueOf(item).equals("双")){
                    even_week.add(dd);
                }
            }
        }
        for (int d =0; d < course_week_d.length ; d++) {
            String course_week_g[] = course_week_d[d].split("-");
            if (course_week_g.length == 1) {
                course_weeks.add(Integer.parseInt(course_week_g[0]));
            } else {
                int week_min = Integer.parseInt(course_week_g[0]);
                int week_max = Integer.parseInt(course_week_g[1]) + 1;
                int odd_min = 0;

                if (week_min%2==0){
                    odd_min = week_min + 1;
                }else {
                    odd_min = week_min;
                }

                if (odd_week.contains(d)){  //是单周就只放单数
                    for (int w = odd_min; w < week_max; w+=2) {
                        course_weeks.add(w);
                    }
                }else if (even_week.contains(d)){  //是双周就只放双数
                    for (int w = odd_min+1; w < week_max; w+=2) {
                        course_weeks.add(w);
                    }
                }else {
                    for (int w = week_min; w < week_max; w++) {
                        course_weeks.add(w);
                    }
                }
            }
        }
        return course_weeks;
    }

    public void add_course(ArrayList even_odd_week, Course c, boolean add_finish){

        List [] a = {list1,list2,list3,list4,list5,list6,list7,list8,list9,list10,list11,list12,list13,list14,list15,list16,list17,list18};
        if (add_finish) {
            for (int i =1; i < 19;i++){
                if (even_odd_week.contains(i)){
                    a[i-1].add(c);
                }
            }
            for (int k = 0; k < 18 ; k++){
                Utils.saveCourseInfo(LessonLoadActivity.this,"week_" + (k+1),a[k] );
            }
            int NowWeek = now_week();

            List<Course> list = Utils.getCourseInfo(this,"week_" + NowWeek);

            if(list!=null){
                if (dismissProgressDialog()){
                    Toast.makeText(this,"课程表数据导入成功啦",Toast.LENGTH_LONG).show();
                }
                table(list);
            }else {
                Refresh();
            }

        }else {
            for (int i =1; i < 19;i++){
                if (even_odd_week.contains(i)){
                    a[i-1].add(c);
                }
            }
            Log.d("name",c.getClassAll());
        }

    }
    public int now_week(){

        int weeks = 1;
        List<Course> startWeekDate = Utils.getCourseInfo(this,"startWeekDate");
        if(startWeekDate != null){
            int startWeek = startWeekDate.get(0).getStartWeek();
            int todayWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
            weeks = todayWeek - startWeek;
        }
        return weeks;

    }

    public void startWeek(String listLeft){
        Document doc = Jsoup.parse(listLeft);
        Element lessons = doc.select("div").first().select("p").first();

        ArrayList startWeekDate = new ArrayList();
        //刷新时，官网上的年月日
        int year = Integer.parseInt(lessons.text().substring(0,4));
        int month = Integer.parseInt(lessons.text().substring(5,7));
        int dayOfMonth = Integer.parseInt(lessons.text().substring(8,10));

        //刷新时，官网上的第几周
        String num = Pattern.compile("[^0-9]").matcher(lessons.select("span").text()).replaceAll("").trim();
        int day_week = Integer.parseInt(num.substring(4));

        Calendar c_now= Calendar.getInstance();
        c_now.set(year, month-1,dayOfMonth);
        int startWeek = c_now.get(Calendar.WEEK_OF_YEAR) - day_week;
        Course c = new Course();
        c.setStartWeek(startWeek);
        startWeekDate.add(c);
        Utils.saveCourseInfo(LessonLoadActivity.this,"startWeekDate",startWeekDate);
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
                    Toast.makeText(LessonLoadActivity.this, dismiss_text, Toast.LENGTH_SHORT).show();
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


