package com.vcom.smartlight.utils;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.vcom.smartlight.R;

import java.lang.ref.WeakReference;

/**
 * @author Banlap on 2021/6/22
 */
public class TimeUtil {
    private CountDownTimer timer;
    private static int time = 60*1000 +300;  //倒计时时间60s, 这里300为了防止时间不准确

    WeakReference<TextView> tvTimer;  //控件软引用，防止内存泄漏
    private String color;             //这里可以修改文字颜色

    public TimeUtil(TextView tvTimer, String color) {
        super();
        this.tvTimer = new WeakReference<>(tvTimer);
        this.color = color;
    }

    public void RunTimer() {
        timer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(tvTimer.get()!=null) {
                    tvTimer.get().setClickable(false);
                    tvTimer.get().setEnabled(false);

                    String textSecond = millisUntilFinished /1000 + "s";
                    tvTimer.get().setText(textSecond);
                    tvTimer.get().setTextColor(Color.parseColor("#FFFFFF"));
                    tvTimer.get().setBackgroundResource(R.drawable.shape_switch_bg_gray_f1);
                }
            }

            @Override
            public void onFinish() {
                tvTimer.get().setClickable(true);
                tvTimer.get().setEnabled(true);

                tvTimer.get().setText("重新获取");
                tvTimer.get().setTextColor(Color.parseColor(color));
                tvTimer.get().setBackgroundResource(R.drawable.shape_switch_bg_green);

            }
        }.start();
    }

    /**
     * 强制取消倒计时 activity或fragment onDestroy()时调用， 防止内存泄漏
     * */
    public void cancelTimer() {
        if(timer!=null){
            timer.cancel();
            timer = null;
        }
    }
}
