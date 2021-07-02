package com.vcom.smartlight.uivm;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.google.gson.reflect.TypeToken;
import com.vcom.smartlight.model.TouchSwitch;
import com.vcom.smartlight.request.ApiLoader;
import com.vcom.smartlight.request.ApiObserver;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.utils.GsonUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;

/**
 * @Author Lzz
 * @Date 2020/11/6 13:33
 */
public class DeviceSettingVM extends AndroidViewModel {

    private DeviceSettingVmCallback callback;

    public DeviceSettingVM(@NonNull Application application) {
        super(application);
    }

    public void setCallback(DeviceSettingVmCallback callback) {
        this.callback = callback;
    }

    public void viewBack() {
        callback.viewBack();
    }

    public void viewBack2() {
        callback.viewBack2();
    }

    public void saveParams(String json) {
        ApiLoader.getApi().addSceneEquip(json, new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {

            }

            @Override
            protected void onSuccess(String data) {
                callback.saveParamsSuccess();
            }

            @Override
            protected void onFailure() {
                callback.saveParamsFailure();
            }

            @Override
            protected void onError() {
                callback.saveParamsFailure();
            }
        });

    }

    public void updateTouchSwitchScene(String equipId, int index, String sceneId) {

        Map<String, Object> map = new HashMap<>();
        map.put("userEquipId", equipId);
        map.put("sceneId", sceneId);
        map.put("sequence", index);
        map.put("userId", VcomSingleton.getInstance().getLoginUser().getUserId());

        ApiLoader.getApi().updateSceneSwitch(GsonUtil.getInstance().toJson(map), new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {
                Toast.makeText(getApplication(),message,Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onSuccess(String data) {
                getTouchSwitchSceneList(equipId);
            }

            @Override
            protected void onFailure() {

            }

            @Override
            protected void onError() {

            }
        });
    }

    public void getTouchSwitchSceneList(String equipId) {
        ApiLoader.getApi().getSceneSwitch(VcomSingleton.getInstance().getLoginUser().getUserId(), equipId,
                new ApiObserver<ResponseBody>() {
                    @Override
                    protected void showMessage(String message) {
                        //Toast.makeText(getApplication(),message,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected void onSuccess(String data) {
                        callback.refreshTouchSceneList(GsonUtil.getInstance().json2List(data, new TypeToken<List<TouchSwitch>>() {}.getType()));
                    }

                    @Override
                    protected void onFailure() {
                        callback.queryTouchSceneListFailure();
                    }

                    @Override
                    protected void onError() {
                        callback.queryTouchSceneListFailure();
                    }
                });
    }

    public interface DeviceSettingVmCallback {
        void viewBack();
        void viewBack2();

        void saveParamsSuccess();
        void saveParamsFailure();

        void refreshTouchSceneList(List<TouchSwitch> list);
        void queryTouchSceneListFailure();
    }

}
