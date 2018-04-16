package com.zrs.yuh.rs.Function;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zrs.yuh.rs.score.Performance;
import com.zrs.yuh.rs.timetable.Course;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by yuh on 18-2-17.
 */


public class Utils {
    //TODO:保存登录名和密码

    public static boolean saveUserInfo(Context context, String file_name, String number, String password){
        SharedPreferences sp = context.getSharedPreferences(file_name, MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("username",number);
        edit.putString("pwd",password);

        edit.apply();
        return true;
    }
    //TODO：xml中获取登录名和密码
    public static Map<String,String> getUserInfo(Context context,String file_name){
        SharedPreferences sp = context.getSharedPreferences(file_name, MODE_PRIVATE);

        String number = sp.getString("username",null);
        String password = sp.getString("pwd",null);
        Map<String,String> uerMap = new HashMap<>();
        uerMap.put("number",number);
        uerMap.put("pwd",password);


        return uerMap;
    }

    // 课程信息
    public static boolean saveCourseInfo(Context context, String name, List<Course> course ){

        SharedPreferences.Editor editor = context.getSharedPreferences(name, MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(course);
        Log.d(name, "saved json is " + name + json);
        editor.putString(name, json);
        editor.apply();

        return true;
    }

    public static List<Course> getCourseInfo(Context context, String name){
        SharedPreferences preferences = context.getSharedPreferences(name, MODE_PRIVATE);
        String json = preferences.getString(name, null);
        List<Course> course = null;
        if (json != null)
        {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Course>>(){}.getType();
            course = gson.fromJson(json, type);
        }
        return course;
    }


    // 课程信息
    public static boolean savePerformanceInfo(Context context, String name, List<Performance> performance ){

        SharedPreferences.Editor editor = context.getSharedPreferences(name, MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(performance);
        Log.d(name, "saved json is " + name + json);
        editor.putString(name, json);
        editor.apply();

        return true;
    }

    public static List<Performance> getPerformanceInfo(Context context, String name){
        SharedPreferences preferences = context.getSharedPreferences(name, MODE_PRIVATE);
        String json = preferences.getString(name, null);
        List<Performance> performance = null;
        if (json != null)
        {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Performance>>(){}.getType();
            performance = gson.fromJson(json, type);
        }
        return performance;
    }






}
