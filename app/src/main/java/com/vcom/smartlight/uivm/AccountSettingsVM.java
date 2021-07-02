package com.vcom.smartlight.uivm;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.vcom.smartlight.model.Equip;
import com.vcom.smartlight.request.ApiLoader;
import com.vcom.smartlight.request.ApiObserver;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.utils.ActivityUtil;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;

/**
 * @author Banlap on 2021/3/3
 */
public class AccountSettingsVM extends AndroidViewModel {


    private AccountSettingsCallBack callBack;

    public AccountSettingsVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(AccountSettingsCallBack callBack) { this.callBack = callBack;}


    public void viewBack(){
        callBack.viewBack();
    }

    public void setNewPassword() {
        callBack.goSetNewPassword();
    }

    public void changePasswordAuthentication(String oldPassword, String newPassword, String newPasswordConform) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", VcomSingleton.getInstance().getLoginUser().getUserId());
        map.put("userName", VcomSingleton.getInstance().getLoginUser().getUserName());
        map.put("userPassword", oldPassword);

        ApiLoader.getApi().userLogin(map, new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {
                //Toast.makeText(getApplication(),message,Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onSuccess(String data) {
                callBack.AuthenticationSuccess(newPassword, newPasswordConform);
            }

            @Override
            protected void onFailure() {
                callBack.AuthenticationError();
            }

            @Override
            protected void onError() {

            }
        });

    }

    public void changePassword(String newPassword) {

        Map<String, Object> map = new HashMap<>();
        map.put("userId", VcomSingleton.getInstance().getLoginUser().getUserId());
        map.put("userName", VcomSingleton.getInstance().getLoginUser().getUserName());
        map.put("userPassword", newPassword);

        ApiLoader.getApi().updateUser(map, new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {

            }

            @Override
            protected void onSuccess(String data) {
                callBack.updateSuccess();
            }

            @Override
            protected void onFailure() {
                callBack.updateError();
            }

            @Override
            protected void onError() {

            }
        });
    }



    public interface AccountSettingsCallBack {
        void viewBack();
        void goSetNewPassword();
        void AuthenticationSuccess(String newPassword, String newPasswordConform);
        void AuthenticationError();
        void updateSuccess();
        void updateError();

    }
}
