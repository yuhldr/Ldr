package com.zrs.yuh.rs;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zrs.yuh.rs.Function.Utils;
import com.zrs.yuh.rs.timetable.Course;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator extends AppCompatActivity {

    protected final int RESULT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        List<Course> calculate_list = Utils.getCourseInfo(this,"calculate");
        if (calculate_list == null){
            showCalculateMsg();
            ArrayList calculate_list0 = new ArrayList();
            Course c = new Course();
            c.setJieci(1);
            calculate_list0.add(c);
            Utils.saveCourseInfo(Calculator.this, "calculate", calculate_list0);
            init();
        }else {
            int time = calculate_list.get(0).getJieci();
            if (time == 1){
                showCalculateMsg();
                ArrayList calculate_list0 = new ArrayList();
                Course c = new Course();
                c.setJieci(2);
                calculate_list0.add(c);
                Utils.saveCourseInfo(Calculator.this, "calculate", calculate_list0);

            }
            init();
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.calculate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.CalculateAbout:
                showCalculateMsg();
                break;
            default:
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    public void init(){

        Button Bt_calculate = findViewById(R.id.Calculate);
        Bt_calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText et_ExperimentalData = findViewById(R.id.ExperimentalData);
                EditText et_MinimumScaleValue = findViewById(R.id.MinimumScaleValue);
                String ExperimentalData = et_ExperimentalData.getText().toString().trim();
                String MinimumScaleValue = et_MinimumScaleValue.getText().toString().trim();

                if(ExperimentalData.isEmpty()|MinimumScaleValue.isEmpty()){
                    TextView tv_uncertainty_results = findViewById(R.id.uncertainty_results);
                    tv_uncertainty_results.setText("    请将实验数据和最小刻度值两项，都输入计算内容后，再计算吧");
                }else {
                    CalculatorUncertainty(ExperimentalData,MinimumScaleValue,"此组实验数据");
                }
            }
        });
    }

    @SuppressLint("HandlerLeak")
    public final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case RESULT:
                    TextView tv_uncertainty_results = findViewById(R.id.uncertainty_results);
                    String uncertainty_results = (String)msg.obj;
                    tv_uncertainty_results.setText(uncertainty_results);

                    break;
                default:
                    break;
            }
        }
    };

    public void CalculatorUncertainty(String ExperimentalData,String MinimumScaleValue,String n ){

        String reg = "([1-9]\\d*\\.?\\d*)|(0\\.\\d*[1-9])";
        Pattern pattern = Pattern.compile(reg);

        String x_String[] = ExperimentalData.split("\\+");
        if (x_String.length<3){
            String error = "    额……，你是实验数据连接号（+）输入错了呢？，还是实验组数太少了呢？";
            Message message = new Message();
            message.what = RESULT;
            message.obj = error;
            handler.sendMessage(message);
        }else {
            int error_info = 0;
            double x[] = new double[x_String.length];

            for (int i = 0; i < x_String.length; i++) {
                Matcher matcher = pattern.matcher(x_String[i]);
                if (matcher.matches()) {
                    x[i] = Double.valueOf(x_String[i]);
                } else {
                    error_info = 1;
                }
            }
            if (error_info == 0){
                Matcher matcher = pattern.matcher(MinimumScaleValue);
                if (matcher.matches()){
                    zhjs(x, Double.valueOf(MinimumScaleValue), n);
                }else {
                    String error = "    ……你的最小刻度输入的是数字吗？？";
                    Message message = new Message();
                    message.what = RESULT;
                    message.obj = error;
                    handler.sendMessage(message);
                }

            }else {
                String error = "    ……这么不听话的吗？说好的实验数据之间，按照加号连接的呢？";
                Message message = new Message();
                message.what = RESULT;
                message.obj = error;
                handler.sendMessage(message);
            }
        }

    }

    public void  zhjs(double a[] ,double jdz,String s)
    {
        double A =(Average(a));
        double k =PN(a.length);

        double ua2 = cif(ua(k,a,A),2);
        double ub2 = cif(jdz/cif(3,0.5),2);
        double U = cif((ua2+ub2),0.5);

        String U1 = s+"的平均数是："+A;
        String UA = s+"的A类不确定度是："+ua(k,a,A);
        String UB = s+"的B类不确定度是："+jdz/cif(3,0.5);
        String UC = s+"的不确定度是："+ U;
        String UD = s+"的最终结果是:" + "\n" +A+" ? "+U+"\n";

        String results =
                U1 + "\n"  +
                UA + "\n"  +
                UB + "\n"  +
                UC + "\n"  +  "\n" +
                UD + "\n"  ;

        Message message = new Message();
        message.what = RESULT;
        message.obj = results;
        handler.sendMessage(message);


    }
    public double Average (double a[]){
        double sum = 0 ;
        for (double anA : a) {
            sum += anA;
        }
        return (sum / a.length) ;
    }
    public static double PN(int a )
    {
        double t = 0;
        switch(a){
            case 3:
                t =1.32;
                break;
            case 4:
                t =1.20;
                break;
            case 5:
                t =1.14;
                break;
            case 6:
                t =1.11;
                break;
            case 7:
                t =1.09;
                break;
            case 8:
                t =1.08;
                break;
            case 9:
                t =1.07;
                break;
            case 10:
                t =1.06;
                break;
            default:
                System.out.println("超出范围");
                break;
        }
        return t;
    }
    public double ua (double k ,double []a,double p)
    {
        double sum = 0 ;
        int n = a.length;

        for (double anA : a) {
            sum = cif((anA - p), 2) + sum;
        }
        double h = sum/(n*(n-1));
        return k * cif(h,0.5);
    }
    public  double cif(double a ,double n )
    {
        return Math.pow(a,n);
    }

    public void showCalculateMsg(){
        String showCalculateMsgTitle
                = "不确定度的计算：";
        String showCalculateMsg
                =
                "1. 此计算方式可能仅适合兰州大学物理实验！\n"+
                        "2. 只能计算直接测量量的不确定度。\n"+
                        "3. 系数PN采用0.683对应的数值，内置实验组数应在3—10组。\n" +
                        "4. UB的计算方式为（最小刻度值）/（根号3）\n" +
                        "5. 请将实验测量若干组数字，输入时，以加号（+）隔开";


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(showCalculateMsgTitle)
                .setMessage(showCalculateMsg)
                .setPositiveButton("明白啦", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


}
