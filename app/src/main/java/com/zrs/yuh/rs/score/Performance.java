package com.zrs.yuh.rs.score;

import java.io.Serializable;


public class Performance implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -9121734039844677432L;
//    private String Year; //学年

//    private String Term; // 学期
//    private String CourseNum1 ;//课程号

    private String CourseName; //课程名
//    private String CourseNum2; //课序号
    private String DailyPerformance;//平时
    private String Midterm; //期中
    private String Terminal; //期末
    private String GeneralComment; //总评
    private String Credit; //学分
//    private String Period; //学时
//    private String ExamMethods;// 考核方式
    private String CourseProperty; //选课属性
//    private String Others; //备注
//    private String ExamProperty; //考试性质
//    private String CorrosionTest; //是否缓考
//    private String Minor; //二学位/辅修
    private String Pass ; //及格标志



    public Performance(String CourseName, String DailyPerformance, String Midterm,
                       String Terminal, String GeneralComment, String Credit,
                       String CourseProperty, String Pass) {
        this.CourseName = CourseName;
        this.DailyPerformance = DailyPerformance;
        this.Midterm = Midterm;
        this.Terminal = Terminal;
        this.GeneralComment = GeneralComment;
        this.Credit = Credit;
        this.CourseProperty = CourseProperty;
        this.Pass = Pass;
    }

    public Performance() {
    }

    public String getCourseName() {
        return CourseName;
    }
    public void setCourseName(String courseName) {
        this.CourseName = courseName;
    }

    String getDailyPerformance() {
        return DailyPerformance;
    }

    public void setDailyPerformance(String dailyPerformance) {
        this.DailyPerformance = dailyPerformance;
    }

    public String getMidterm() {
        return Midterm;
    }

    public void setMidterm(String midterm) {
        this.Midterm = midterm;
    }


    public String getTerminal() {
        return Terminal;
    }

    public void setTerminal(String terminal) {
        this.Terminal = terminal;
    }

    public String getGeneralComment() {
        return GeneralComment;
    }

    public void setGeneralComment(String generalComment) {
        this.GeneralComment = generalComment;
    }

    public String getCredit() {
        return Credit;
    }

    public void setCredit(String credit) {
        Credit = credit;
    }

    public String getCourseProperty() {
        return CourseProperty;
    }

    public void setCourseProperty(String courseProperty) {
        CourseProperty = courseProperty;
    }
    public String getPass() {
        return Pass;
    }

    public void setPass(String pass) {
        Pass = pass;
    }



}
