package com.vcom.smartlight.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.vcom.smartlight.R;

/**
 * @author Banlap on 2021/3/17
 */
public class NewButton extends androidx.appcompat.widget.AppCompatButton {

    private Drawable backGroundDrawable = null;

    private GradientDrawable gradientDrawable = null;

    public NewButton(@NonNull Context context) {
        super(context);
    }

    public NewButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public NewButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.NewButton);
        float radius = ta.getFloat(R.styleable.NewButton_radius,0);
        setRadius(radius);
        ta.recycle();
    }

    public void setBackGroundDrawable(Drawable backGroundDrawable) {
        this.backGroundDrawable = backGroundDrawable;
        setBackgroundDrawable(backGroundDrawable);
    }

    public void setRadius(float radius) {
        getGradientDrawable();
        gradientDrawable.setCornerRadius(radius);
        setBackGroundDrawable(gradientDrawable);
    }

    private void getGradientDrawable() {
        if(gradientDrawable == null){
            gradientDrawable = new GradientDrawable();
        }
    }

}
