package com.vcom.smartlight.uivm;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.vcom.smartlight.model.Equip;
import com.vcom.smartlight.model.MessageEvent;
import com.vcom.smartlight.model.Scene;
import com.vcom.smartlight.request.ApiLoader;
import com.vcom.smartlight.request.ApiObserver;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.utils.GsonUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;

/**
 * @author Banlap on 2021/1/28
 */
public class AddSceneVM extends AndroidViewModel {

    private AddSceneVMCallBack callBack;

    public AddSceneVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(AddSceneVMCallBack callBack) {
        this.callBack = callBack;
    }


    public void viewBack() {
        callBack.viewBack();
    }

    public void goAddEquip() {
        EventBus.getDefault().post(new MessageEvent(MessageEvent.switchFragment, "1"));
    }

    public void goAddEquipToNewScene() {
        callBack.viewGoAddNewEquip();
    }

    public void addSceneEquip(String data) {
        ApiLoader.getApi().addSceneEquip(data, new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {
                //Toast.makeText(getApplication(),message,Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onSuccess(String data) {
                EventBus.getDefault().post(new MessageEvent(MessageEvent.refreshRegion));
                callBack.viewAddSceneEquipSuccess();
            }

            @Override
            protected void onFailure() {
                callBack.viewAddSceneEquipFailure();
            }

            @Override
            protected void onError() {
                callBack.viewAddSceneEquipFailure();
            }
        });

    }

    public void goUpdateScene() {
        callBack.viewGoUpdateScene();
    }

    public void goSelectSceneIcon() {
        callBack.viewSelectSceneIcon();
    }

    public void goSelectSceneTiming() {
        callBack.viewSelectSceneTiming();
    }

    public void selectRecord(Equip equip, String sceneId, String equipId) {
        String userId = VcomSingleton.getInstance().getLoginUser().getUserId();
        ApiLoader.getApi().selectRecord(userId, sceneId, equipId, new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {

            }

            @Override
            protected void onSuccess(String data) {
                callBack.selectRecordSuccess(equip, data);
            }

            @Override
            protected void onFailure() {
                callBack.selectRecordFailure(equip);
            }

            @Override
            protected void onError() {
                callBack.selectRecordFailure(equip);
            }
        });
    }

    public void updateScene(Scene scene) {
        Map<String, Object> param = new HashMap<>();
        param.put("sceneId", scene.getSceneId());
        param.put("sceneName", scene.getSceneName());
        param.put("sceneImg", scene.getSceneImg());
        param.put("userId", VcomSingleton.getInstance().getLoginUser().getUserId());

        ApiLoader.getApi().updateScene(GsonUtil.getInstance().toJson(param), new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {
                //Toast.makeText(getApplication(),message,Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onSuccess(String data) {
                callBack.viewUpdateSceneSuccess();
                EventBus.getDefault().post(new MessageEvent(MessageEvent.refreshRegion));
            }

            @Override
            protected void onFailure() {
                callBack.viewUpdateSceneFailure();
            }

            @Override
            protected void onError() {
                callBack.viewUpdateSceneFailure();
            }
        });
    }

    public void deleteScene(Scene scene1) {

        ApiLoader.getApi().deleteScene(scene1.getSceneId(), new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {
                Toast.makeText(getApplication(),message,Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onSuccess(String data) {
                EventBus.getDefault().post(new MessageEvent(MessageEvent.refreshRegion));
                callBack.viewDeleteSceneSuccess(scene1);
            }

            @Override
            protected void onFailure() {

            }

            @Override
            protected void onError() {

            }
        });
    }

    public void deleteSceneEquip(Scene scene, String userId, String spaceId, String sceneId, String equipId) {
        ApiLoader.getApi().deleteSceneEquip(userId, spaceId, sceneId, equipId, new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {
                //Toast.makeText(getApplication(),message,Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onSuccess(String data) {
                EventBus.getDefault().post(new MessageEvent(MessageEvent.refreshRegion));
                callBack.viewDeleteSceneSuccess(scene);
            }

            @Override
            protected void onFailure() {
                callBack.viewDeleteSceneFailure();

            }

            @Override
            protected void onError() {
                callBack.viewDeleteSceneFailure();

            }
        });
    }

    public interface AddSceneVMCallBack {
        void viewBack();
        void viewGoAddNewEquip();

        void viewAddSceneEquipSuccess();
        void viewAddSceneEquipFailure();

        void selectRecordSuccess(Equip equip, String data);
        void selectRecordFailure(Equip equip);

        void viewGoUpdateScene();

        void viewUpdateSceneSuccess();
        void viewUpdateSceneFailure();

        void viewDeleteSceneSuccess(Scene scene);
        void viewDeleteSceneFailure();

        void viewSelectSceneIcon();
        void viewSelectSceneTiming();

    }
}
