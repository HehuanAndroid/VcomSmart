package com.vcom.smartlight.uivm;

import android.app.Application;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.google.gson.reflect.TypeToken;
import com.vcom.smartlight.model.Equip;
import com.vcom.smartlight.model.MessageEvent;
import com.vcom.smartlight.model.Region;
import com.vcom.smartlight.model.Scene;
import com.vcom.smartlight.model.User;
import com.vcom.smartlight.request.ApiLoader;
import com.vcom.smartlight.request.ApiObserver;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.ui.AddSceneActivity;
import com.vcom.smartlight.utils.GsonUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;

/**
 * @author Banlap on 2021/2/26
 */
public class AddSceneListVM extends AndroidViewModel {

    private AddSceneListCallBack callBack;

    public AddSceneListVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(AddSceneListCallBack callBack) { this.callBack = callBack; }

    public void viewBack(){
        callBack.viewBack();
    }

    public void addNewScene() {
        callBack.addNewScene();
    }

    public void getNewRegion() {
        User user = VcomSingleton.getInstance().getLoginUser();
        ApiLoader.getApi().selectAllData(user.getUserId(), new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {
            }

            @Override
            protected void onSuccess(String data) {
                VcomSingleton.getInstance().setUserRegion(GsonUtil.getInstance().json2List(data, new TypeToken<List<Region>>() {}.getType()));
                EventBus.getDefault().post(new MessageEvent(MessageEvent.regionReady));
            }

            @Override
            protected void onFailure() {

            }

            @Override
            protected void onError() {

            }
        });
    }

    public void addSceneList(String spaceId, List<Scene> scenes) {
        User user = VcomSingleton.getInstance().getLoginUser();
        String json = GsonUtil.getInstance().toJson(scenes);

        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getUserId());
        map.put("spaceId", spaceId);
        map.put("sceneImg", scenes.get(0).getSceneImg());
        map.put("sceneName", json);

        ApiLoader.getApi().addSceneList(map, new ApiObserver<ResponseBody>() {
        //ApiLoader.getApi().addScene(user.getUserId(), spaceId, scenes.get(0).getSceneName(), scenes.get(0).getSceneImg()
        //       , new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {
                //Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onSuccess(String data) {
                //banlap: 如果data不为空则添加新场景
               /* if(!data.equals("")){
                    Intent addNewSceneIntent = new Intent(getApplication(), AddSceneActivity.class);
                    addNewSceneIntent.putExtra("CurrentRegionId", spaceId);
                    addNewSceneIntent.putExtra("NewSceneDefaultName", scenes.get(0).getSceneName());
                    getApplication().startActivity(addNewSceneIntent);
                }*/
                callBack.addNewSceneSuccess(scenes);
                //callBack.refreshData();
            }

            @Override
            protected void onFailure() {
                callBack.addNewSceneFailure();
            }

            @Override
            protected void onError() {
                callBack.addNewSceneFailure();
            }
        });
    }

    public interface AddSceneListCallBack {
        void viewBack();
        void addNewScene();
        void refreshData();
        void addNewSceneSuccess(List<Scene> scenes);
        void addNewSceneFailure();
    }
}
