package com.vcom.smartlight.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.LocaleList;
import android.se.omapi.Session;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.vcom.smartlight.R;
import com.vcom.smartlight.base.BaseMvvmActivity;
import com.vcom.smartlight.databinding.ActivityLoginBinding;
import com.vcom.smartlight.model.User;
import com.vcom.smartlight.uivm.LoginVM;
import com.vcom.smartlight.utils.GsonUtil;
import com.vcom.smartlight.utils.SPUtil;

import java.io.InputStream;
import java.util.Locale;

public class LoginActivity extends BaseMvvmActivity<LoginVM, ActivityLoginBinding>
        implements LoginVM.LoginVmCallBack, TextWatcher {

    private boolean mIsClickBack = false;  //标记：是否点击返回图标
    private boolean mIsClickAgree = false; //标记：是否点击同意按钮

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initViews() {
        getViewDataBind().setVm(getViewModel());
        getViewModel().setCallBack(this);
        if (!TextUtils.isEmpty(SPUtil.getValue(this, "user_param"))) {
            User user = GsonUtil.getInstance().json2Bean(SPUtil.getValue(this, "user_param"), User.class);
            getViewModel().setUserName(user.getUserName());
        }

        getViewDataBind().tvUserDocument1.setText(getString(R.string.login_agreement_title_all));

        //banlap: 底部文本部分颜色调整
        ForegroundColorSpan greenSpan = new ForegroundColorSpan(getResources().getColor(R.color.green));
        ForegroundColorSpan greenSpan2 = new ForegroundColorSpan(getResources().getColor(R.color.green));

        SpannableStringBuilder builder = new SpannableStringBuilder(getViewDataBind().tvUserDocument1.getText().toString());

        int indexUserAgreement = getString(R.string.login_agreement_title_all).indexOf(getString(R.string.login_agreement_title_2));
        int indexPrivacyPolicy = getString(R.string.login_agreement_title_all).indexOf(getString(R.string.login_agreement_title_4));


        ClickableSpan userAgreementClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showTreaty("user");
            }
        };

        ClickableSpan privacyPolicyClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showTreaty("private");
            }
        };

        //banlap: 设置span到指定文本中
        builder.setSpan(greenSpan, indexUserAgreement,indexUserAgreement + getString(R.string.login_agreement_title_2).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        builder.setSpan(greenSpan2,indexPrivacyPolicy,indexPrivacyPolicy + getString(R.string.login_agreement_title_4).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        builder.setSpan(userAgreementClickableSpan, indexUserAgreement,indexUserAgreement + getString(R.string.login_agreement_title_2).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        builder.setSpan(privacyPolicyClickableSpan, indexPrivacyPolicy,indexPrivacyPolicy + getString(R.string.login_agreement_title_4).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        getViewDataBind().tvUserDocument1.setText(builder);
        //banlap: 配置到tvUserDocument1中 (这个在setSpan()之后设置)
        getViewDataBind().tvUserDocument1.setMovementMethod(LinkMovementMethod.getInstance());

        getViewDataBind().etPassword.addTextChangedListener(this);
    }

    @Override
    protected void initDatum() {
        changeLanguage();
        //banlap: 按钮下一步
        getViewDataBind().loginCommit.setText(getString(R.string.next));
    }


    /**
     * banlap: 添加动画效果 向左滑动
     * */
    public AnimationSet animationLeft(boolean leftOrRight){
        WindowManager windowManager = this.getWindowManager();
        Display display = windowManager.getDefaultDisplay();

        AnimationSet animatorSet = new AnimationSet(true);

        int displayAxis = -display.getWidth() * 2-100;

        if(leftOrRight){
            displayAxis = display.getWidth() * 2-100;
        }

        TranslateAnimation animation = new TranslateAnimation(0, displayAxis,
                0, 0);
        animation.setDuration(500);    //设置动画速度
        animation.setRepeatCount(0);   //设置重复动画次数
        animatorSet.addAnimation(animation);
        animatorSet.setFillAfter(true); //完成动画后不再恢复原来状态

        return animatorSet;
    }

    /**
     * banlap: 添加动画效果 向右滑动
     * */
    public AnimationSet animationRight(boolean leftOrRight){
        WindowManager windowManager = this.getWindowManager();
        Display display = windowManager.getDefaultDisplay();

        AnimationSet animatorSet = new AnimationSet(true);

        int displayAxis = display.getWidth() * 2-100;

        if(leftOrRight){
            displayAxis = -display.getWidth() * 2-100;
        }

        TranslateAnimation animation = new TranslateAnimation(displayAxis, 0,
                0, 0);
        animation.setDuration(500);    //设置动画速度
        animation.setRepeatCount(0);   //设置重复动画次数
        animatorSet.addAnimation(animation);
        animatorSet.setFillAfter(true); //完成动画后不再恢复原来状态

        return animatorSet;
    }



    @Override
    public void inputAccount() {
        if(!mIsClickBack) {
            getViewDataBind().etAccount.startAnimation(animationLeft(false));
            getViewDataBind().tvAccount.startAnimation(animationLeft(false));

            showEditTextPassword();
            getViewDataBind().loginCommit.setText(getString(R.string.login_login));
            getViewDataBind().loginCommit.setBackgroundResource(R.drawable.shape_radius_green_alpha);

            mIsClickBack = true;

            InputMethodManager manager = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
            if(manager != null) {
                manager.hideSoftInputFromWindow(getViewDataBind().loginCommit.getWindowToken(), 0);
            }
        }
    }

    /**
     * banlap: 动画效果 显示密码输入框
     * */
    private void showEditTextPassword() {
        getViewDataBind().etPassword.startAnimation(animationRight(false));
        getViewDataBind().tvPassword.startAnimation(animationRight(false));
        getViewDataBind().loginIvBack.startAnimation(animationRight(false));
        getViewDataBind().etPassword.setVisibility(View.VISIBLE);
        getViewDataBind().tvPassword.setVisibility(View.VISIBLE);
        getViewDataBind().loginIvBack.setVisibility(View.VISIBLE);
    }

    /**
     * banlap: 点击返回按钮返回输入账号
     * */
    @Override
    public void backInputAccount() {

        mIsClickBack = false;
        getViewDataBind().etPassword.setText("");
        getViewDataBind().etAccount.startAnimation(animationRight(true));
        getViewDataBind().tvAccount.startAnimation(animationRight(true));

        getViewDataBind().etPassword.startAnimation(animationLeft(true));
        getViewDataBind().tvPassword.startAnimation(animationLeft(true));
        getViewDataBind().loginIvBack.startAnimation(animationLeft(true));

        getViewDataBind().etPassword.setVisibility(View.GONE);
        getViewDataBind().tvPassword.setVisibility(View.GONE);
        getViewDataBind().loginIvBack.setVisibility(View.GONE);
        getViewDataBind().etPassword.clearAnimation();
        getViewDataBind().tvPassword.clearAnimation();
        getViewDataBind().loginIvBack.clearAnimation();

        getViewDataBind().loginCommit.setText(getString(R.string.next));
        getViewDataBind().loginCommit.setBackgroundResource(R.drawable.shape_radius_green);

    }

    @Override
    public void startLogin() {
        getViewDataBind().loginCommit.setClickable(false);
        getViewDataBind().loginCommit.setBackgroundResource(R.drawable.shape_radius_gray);
        getViewDataBind().loginCommit.setTextColor(Color.GRAY);
    }

    @Override
    public void loginSuccess() {
        Toast.makeText(this, this.getString(R.string.toast_login_success), Toast.LENGTH_SHORT).show();
        Intent goMain = new Intent(this, MainActivity.class);
        startActivity(goMain);
        //banlap: 当在app退出账号重新登录后，点击返回键不再返回到登录界面
        finish();
    }

    @Override
    public void loginFailure() {
        Toast.makeText(this, this.getString(R.string.toast_login_error), Toast.LENGTH_SHORT).show();
        getViewDataBind().loginCommit.setClickable(true);
        getViewDataBind().loginCommit.setBackgroundResource(R.drawable.shape_radius_green);
        getViewDataBind().loginCommit.setTextColor(Color.WHITE);
    }

    //banlap: 没有点击同意按钮时提示信息
    @Override
    public void isAgree() {
        Toast.makeText(this, getString(R.string.toast_agree_privacy_policy), Toast.LENGTH_SHORT).show();
    }

    //banlap: 点击同意按钮
    @Override
    public void clickAgree() {
        mIsClickAgree = !mIsClickAgree;
        if(mIsClickAgree) {
            getViewDataBind().ivAgreement.setBackgroundResource(R.drawable.ic_select_yes);
        } else {
            getViewDataBind().ivAgreement.setBackgroundResource(R.drawable.ic_select_no);
        }
    }

    /**
     * banlap: 语言设置 跟随系统语言
     * */
    public void changeLanguage(){
        //String localeLanguage = Locale.getDefault().getLanguage();      //获取当前系统语言;
        //Toast.makeText(this, localeLanguage, Toast.LENGTH_SHORT).show();

        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //banlap: 系统语言首选项语言
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }

        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        //banlap: 设置成系统语言
        config.setLocale(locale);
    }

    /**
      * banlap: 跳转用户协议和隐私政策 （当前代码不使用）
      * */
    @Override
    public void showTreaty(String tag){
        Intent intent = new Intent(this, DocActivity.class);
        intent.putExtra("webTag", tag);
        startActivity(intent);
    }

    /**
     * banlap: 跳转使用帮助界面
     * */
    @Override
    public void goAppHelp() {
        Intent intent = new Intent(this, AppHelpActivity.class);
        intent.putExtra("Advance", false);
        startActivity(intent);
    }

    /**
     * banlap: 跳转手机注册
     * */
    @Override
    public void goAccountRegister() {
        Intent intent = new Intent(this, AccountRegisterActivity.class);
        startActivity(intent);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        /*if(!getViewDataBind().etPassword.getText().toString().equals("")){
            getViewDataBind().loginCommit.setBackgroundResource(R.drawable.shape_radius_green);
            getViewDataBind().ivAgreement.setVisibility(View.VISIBLE);
        } else {
            getViewDataBind().loginCommit.setBackgroundResource(R.drawable.shape_radius_green_alpha);
            getViewDataBind().ivAgreement.setVisibility(View.GONE);
        }*/
    }

    @Override
    public void afterTextChanged(Editable s) {

    }


    //banlap: 输入框点击空白处收回键盘 处理触摸事件
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 获得当前得到焦点的View，一般情况下就是EditText（特殊情况就是轨迹求或者实体案件会移动焦点）
        View v = getCurrentFocus();
        if (isShouldHideInput(v, ev)) {
            hideSoftInput(v.getWindowToken());
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时没必要隐藏
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = { 0, 0 };
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 多种隐藏软件盘方法的其中一种
     *
     * @param token
     */
    private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


}