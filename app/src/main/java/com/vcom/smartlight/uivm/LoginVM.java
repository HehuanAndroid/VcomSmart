package com.vcom.smartlight.uivm;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.vcom.smartlight.R;
import com.vcom.smartlight.model.User;
import com.vcom.smartlight.request.ApiLoader;
import com.vcom.smartlight.request.ApiObserver;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.utils.GsonUtil;
import com.vcom.smartlight.utils.SPUtil;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;

/**
 * @Author Lzz
 * @Date 2020/10/27 14:14
 */

public class LoginVM extends AndroidViewModel {

    private MutableLiveData<String> userName = new MutableLiveData<>();
    private MutableLiveData<String> userPassword = new MutableLiveData<>();
    private boolean mIsClickAgree = false;

    private LoginVmCallBack callBack;

    public LoginVM(Application application) {
        super(application);
    }

    public void setCallBack(LoginVmCallBack callBack) {
        this.callBack = callBack;
    }

    public MutableLiveData<String> getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName.postValue(userName);
    }

    public MutableLiveData<String> getUserPassword() {
        return userPassword;
    }

    public synchronized void userLogin() {
        if (TextUtils.isEmpty(userName.getValue()) && TextUtils.isEmpty(userPassword.getValue())) {
            return;
        }

        if(!TextUtils.isEmpty(userName.getValue()) && TextUtils.isEmpty(userPassword.getValue())){
            callBack.inputAccount();
            return;
        }

        if(TextUtils.isEmpty(userName.getValue()) && !TextUtils.isEmpty(userPassword.getValue())){
            return;
        }

        //banlap: 是否点击同意按钮
        if(!mIsClickAgree) {
            callBack.isAgree();
            return;
        }

        callBack.startLogin();

        Map<String, Object> map = new HashMap<>();
        map.put("userName", userName.getValue());
        map.put("userPassword", userPassword.getValue());

        //ApiLoader.getApi2(getApplication()).userLogin(map, new ApiObserver<ResponseBody>() {
        ApiLoader.getApi().userLogin(map, new ApiObserver<ResponseBody>() {

            @Override
            protected void showMessage(String message) {
                //Toast.makeText(getApplication(),message,Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onSuccess(String data) {
                //banlap: 登录判断data数据是否为空
                if(data.equals("")||data.isEmpty()){
                    //Toast.makeText(getApplication(), getApplication().getString(R.string.toast_login_error), Toast.LENGTH_SHORT).show();
                    callBack.loginFailure();
                } else {
                    SPUtil.setValues(getApplication(), "user_param", data);
                    VcomSingleton.getInstance().setLoginUser(GsonUtil.getInstance().json2Bean(data, User.class));
                    callBack.loginSuccess();
                }

            }

            @Override
            protected void onFailure() {
                callBack.loginFailure();
            }

            @Override
            protected void onError() {
                callBack.loginFailure();
            }
        });
    }

    public void backEditTextAccount() {
        callBack.backInputAccount();
    }

    public void showTreaty(View view) {
        String tag = (String) view.getTag();
        callBack.showTreaty(tag);
    }

    public void isClickAgree() {
        mIsClickAgree = !mIsClickAgree;
        callBack.clickAgree();
    }

    public void goAppHelp() {
        callBack.goAppHelp();
    }

    public void goAccountRegister() {
        callBack.goAccountRegister();
    }

    public interface LoginVmCallBack {

        void inputAccount();

        void startLogin();

        void loginSuccess();

        void loginFailure();

        void backInputAccount();

        void showTreaty(String tag);

        void isAgree();

        void clickAgree();

        void goAppHelp();

        void goAccountRegister();
    }

}
