package com.zrs.yuh.rs;

/**
 * Created by nolov on 2018/2/21.
 */

import java.util.HashMap;
import java.util.Map;

import android.app.Application;

public class Data extends Application{
    private String data_s;
    private String data_s_2;

    public void onCreate() {
        super.onCreate();
        data_s = "初始化";
        data_s_2 = "初始化_2";
    }

    public String getData_s() {
        return data_s;
    }

    public void setData_s(String data_s) {
        this.data_s = data_s;
    }
    public String getData_s_2() {
        return data_s_2;
    }

    public void setData_s_2(String data_s_2) {
        this.data_s_2 = data_s_2;
    }




}
