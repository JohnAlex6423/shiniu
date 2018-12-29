package com.olcow.shiniu.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.olcow.shiniu.R;
import com.olcow.shiniu.until.TimeType;

import java.sql.Time;

public class SendPostActivity extends AppCompatActivity {

    private ImageView closeImg;
    private ConstraintLayout con;
    private ConstraintLayout iconsCon;
    private ConstraintLayout headerCon;
    private TextView dayOfMonthText;
    private TextView dayOfWeekText;
    private TextView yearText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_post);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Window window = this.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(ContextCompat.getColor(this,R.color.white));
                //底部导航栏
                //window.setNavigationBarColor(activity.getResources().getColor(colorResId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        con = findViewById(R.id.send_post_con_con);
        closeImg = findViewById(R.id.send_post_close);
        iconsCon = findViewById(R.id.send_post_icons_con);
        dayOfMonthText = findViewById(R.id.send_post_month_text);
        dayOfWeekText = findViewById(R.id.send_post_week_text);
        yearText = findViewById(R.id.send_post_year_text);
        headerCon = findViewById(R.id.send_post_header_con);
        con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        dayOfMonthText.setText(TimeType.getNowDayOfMonth());
        dayOfWeekText.setText(TimeType.getNowDayOfWeek());
        yearText.setText(TimeType.getNowYear());
        ObjectAnimator.ofFloat(closeImg,"rotation",-45,0).setDuration(300).start();
        ObjectAnimator iconsConAnim1 = ObjectAnimator.ofFloat(iconsCon,"translationY",350.0f,-50.0f).setDuration(200);
        ObjectAnimator iconsConAnim2 = ObjectAnimator.ofFloat(iconsCon,"translationY",-50.0f,0.0f).setDuration(100);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(iconsConAnim2).after(iconsConAnim1);
        animatorSet.start();
        ObjectAnimator.ofFloat(headerCon,"alpha",0,1).setDuration(300).start();
        ObjectAnimator.ofFloat(con,"alpha",0,1).setDuration(300).start();
    }

    @Override
    public void finish() {
        super.finish();
        ObjectAnimator.ofFloat(iconsCon,"translationY",0.0f,350.0f).setDuration(50).start();
        ObjectAnimator.ofFloat(headerCon,"alpha",1,0).setDuration(50).start();
        ObjectAnimator.ofFloat(con,"alpha",1,0).setDuration(50).start();
        overridePendingTransition(0,0);
    }
}
