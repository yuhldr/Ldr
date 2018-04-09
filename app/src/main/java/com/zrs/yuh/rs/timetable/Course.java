package com.zrs.yuh.rs.timetable;


import java.io.Serializable;
import java.util.Map;

public class Course implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -9121734039844677432L;
    private int jieci;

    private int day;
    private int spanNum ;//节数

    private String ClassName;
    private String ClassTeacherName;
    private String ClassRoomName;
    private String ClassTypeName;
    private String ClassAll;
    private String ClassWeek;
    private int CourseColor;
    private int StartWeek;

    public Course(String ClassWeek,int StartWeek, int spanNum, int jieci, int day, String ClassName, String ClassTeacherName, int CourseColor, String ClassRoomName,String ClassAll) {
        this.jieci = jieci;
        this.day = day;
        this.spanNum = spanNum;
        this.ClassName = ClassName;
        this.ClassTeacherName = ClassTeacherName;
        this.CourseColor = CourseColor;
        this.ClassRoomName = ClassRoomName;
        this.ClassAll = ClassAll;
        this.StartWeek = StartWeek;
        this.ClassWeek = ClassWeek;
    }

    public Course() {
    }

    public int getJieci() {
        return jieci;
    }

    public void setJieci(int jieci) {
        this.jieci = jieci;
    }

    public int getDay() {
        return day;
    }

    public void setDay(String day) {

        switch (day){
            case "星期一":
                this.day = 1;
                break;
            case "星期二":
                this.day = 2;
                break;
            case "星期三":
                this.day = 3;
                break;
            case "星期四":
                this.day = 4;
                break;
            case "星期五":
                this.day = 5;
                break;
            case "星期六":
                this.day = 6;
                break;
            case "星期日":
                this.day = 7;
                break;
            default:
                break;

        }
    }

    public String getClassName() {
        return ClassName;
    }

    public void setClassName(String ClassName) {
        this.ClassName = ClassName;
    }

    public int getSpanNum() {
        return spanNum;
    }

    public void setSpanNum(int spanNum) {
        this.spanNum = spanNum;
    }

    @Override
    public String toString() {
        return "Course [jieci=" + jieci + ", day=" + day + ", ClassName=" + ClassName
                + ", spanNun=" + spanNum + "]";
    }

    public String getClassRoomName() {
        return ClassRoomName;
    }

    public void setClassRoomName(String classRoomName) {
        ClassRoomName = classRoomName;
    }

    public String getClassTeacherName() {
        return ClassTeacherName;
    }

    public void setClassTeacherName(String classTeacherName) {
        ClassTeacherName = classTeacherName;
    }
    public int getCourseColor() {
        return CourseColor;
    }

    public void setCourseColor(int coursecolor) {
        CourseColor = coursecolor;
    }

    public String getClassTypeName() {
        return ClassTypeName;
    }

    public void setClassTypeName(String classTypeName) {
        ClassTypeName = classTypeName;
    }

    public String getClassAll() {
        return ClassAll;
    }

    public void setClassAll(String classAll) {
        ClassAll = classAll;
    }

    public int getStartWeek() {
        return StartWeek;
    }

    public void setStartWeek(int startweek) {
        StartWeek = startweek;
    }

    public String getClassWeek() {
        return ClassWeek;
    }

    public void setClassWeek(String classWeek) {
        ClassWeek = classWeek;
    }



}
