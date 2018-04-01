package com.zrs.yuh.rs;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Calculator extends AppCompatActivity {

    protected final int RESULT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        showCalculateMsg();

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
                    CalculatorUncertainty(ExperimentalData,MinimumScaleValue,"第一组");
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


        String x_String[] = ExperimentalData.split("\\+");
        double x[] = new double[x_String.length];

        for (int i = 0 ; i < x_String.length; i++) {
            x[i] = Double.valueOf(x_String[i]);
        }

        zhjs(x,Double.valueOf(MinimumScaleValue),n);

    }

    public void  zhjs(double a[] ,double jdz,String s)
    {
        double A =(Average(a));
        double k =PN(a.length);

        double ua2 = cif(ua(k,a,A),2);
        double ub2 = cif(jdz/cif(3,0.5),2);
        double U = cif((ua2+ub2),0.5);

        String U1 = s+"原始数据的平均数是："+A;
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
