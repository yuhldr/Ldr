package com.zrs.yuh.rs;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zrs.yuh.rs.Function.Utils;
import com.zrs.yuh.rs.timetable.Course;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    protected static final int SUCCESS_CHANGE = 0;
    protected static final int ERROR_CHANGE= 1;
    private ProgressDialog progressDialog;
    TextView tv_change_password;

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case ERROR_CHANGE:
                    String error_change_password = (String) msg.obj;
                    tv_change_password = findViewById(R.id.tv_change_password);

                    tv_change_password.setText(error_change_password);
                    dismissProgressDialog();

                    break;

                case SUCCESS_CHANGE:
                    tv_change_password = findViewById(R.id.tv_change_password);
                    tv_change_password.setText("修改成功");
                    dismissProgressDialog();

                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        List<Course> list_change = Utils.getCourseInfo(ChangePasswordActivity.this,"cookies");
        if (list_change!=null) {

            final String session = list_change.get(0).getClassRoomName();

            Button bt_change_password = findViewById(R.id.bt_change_password);

            bt_change_password.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText et_old_password = findViewById(R.id.et_old_password);
                    EditText et_new_password_1 = findViewById(R.id.et_new_password_1);
                    EditText et_new_password_2 = findViewById(R.id.et_new_password_2);
                    String old_password = et_old_password.getText().toString().trim();
                    String new_password_1 = et_new_password_1.getText().toString().trim();
                    String new_password_2 = et_new_password_2.getText().toString().trim();

                    PostChangePassword(old_password, new_password_1, new_password_2, session);
                }
            });
        }
    }

    public void PostChangePassword(final String oldpasswd, String newpasswd, String confirmedpasswd, final String JSESSIONID) {

        showProgressDialog(this,"修改中……","连接超时！，检查网络后重新登陆");

        String url = "http://jwk.lzu.edu.cn/academic/sysmgr/modifypasswd_user.jsdo";
        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("confirmedpasswd", confirmedpasswd)
                .add("ff", "%D0%DE+%B8%C4")
                .add("gotoUrl", "null")
                .add("newpasswd", newpasswd)
                .add("oldpasswd", oldpasswd)
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
                    assert response.body() != null;
                    String results =response.body().string();
                    Log.i("info_call2success", results);

                    String result;
                    Document doc = Jsoup.parse(results);
                    Elements elements = doc.getElementsByClass("error");
                    result = elements.text();
                    Message msg =new Message();
                    if(!Objects.equals(result, "")){
                        Log.d("----------------",result);
                        msg.what = ERROR_CHANGE;
                        msg.obj = result;
                    }else {
                        Log.d("================","成功");
                        Document document = Jsoup.parse(results);
                        if (document.select("script").size()==1) {
                            msg.what = SUCCESS_CHANGE;
                            msg.obj = results;
                        }
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
//        progressDialog.setCancelable(false);//点击屏幕和按返回键都不能取消加载框
        progressDialog.show();

        //设置超时自动消失
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //取消加载框
                //超时处理
                if(dismissProgressDialog()) {
                    Toast.makeText(ChangePasswordActivity.this, dismiss_text, Toast.LENGTH_SHORT).show();
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
