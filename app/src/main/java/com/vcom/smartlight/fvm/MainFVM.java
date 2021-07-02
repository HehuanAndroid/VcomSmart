package com.vcom.smartlight.fvm;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.google.gson.reflect.TypeToken;
import com.telink.bluetooth.event.DeviceEvent;
import com.telink.bluetooth.light.ConnectionStatus;
import com.telink.util.Arrays;
import com.vcom.smartlight.R;
import com.vcom.smartlight.model.Equip;
import com.vcom.smartlight.model.MessageEvent;
import com.vcom.smartlight.model.Region;
import com.vcom.smartlight.model.Scene;
import com.vcom.smartlight.model.User;
import com.vcom.smartlight.model.Weather;
import com.vcom.smartlight.request.ApiLoader;
import com.vcom.smartlight.request.ApiObserver;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.utils.GsonUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;

/**
 * @Author Lzz
 * @Date 2020/10/27 18:45
 */
public class MainFVM extends AndroidViewModel {

    private MainFvmCallBack callBack;


    public MainFVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(MainFvmCallBack callBack) {
        this.callBack = callBack;
    }

    public void goControlMenu() {
        callBack.goControlMenu();
    }

    public void goSelectRegion() { callBack.goSelectRegion(); }

    public void goEditRegion() {
        callBack.goEditRegion();
    }

    public void goGuideRegion() {
        callBack.goGuideRegion();
    }

    public void goGuideScene() {
        callBack.goGuideScene();
    }

    public void goAddScene() {
        callBack.goAddScene();
    }

    public void goAddNewScene() {
        callBack.goAddNewScene();
    }

    public void goRegionManger() {
        callBack.goRegionManger();
    }

    public void goSceneListManager() {
        callBack.goSceneListManager();
    }

    public void addSingleScene(String spaceId, Scene scene) {
        User user = VcomSingleton.getInstance().getLoginUser();

        ApiLoader.getApi().addScene(user.getUserId(), spaceId, scene.getSceneName(), scene.getSceneImg(), new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {
                Toast.makeText(getApplication(),message,Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onSuccess(String data) {
                callBack.refreshData();
            }

            @Override
            protected void onFailure() {

            }

            @Override
            protected void onError() {

            }
        });
    }

    public void addRegionList(List<Region> regions) {
        User user = VcomSingleton.getInstance().getLoginUser();
        String json = GsonUtil.getInstance().toJson(regions);

        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getUserId());
        map.put("spaceName", json);
        map.put("spaceImg", regions.get(0).getSpaceImg());

        ApiLoader.getApi().addRegionList(map, new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {
                //Toast.makeText(getApplication(),message,Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onSuccess(String data) {
                Toast.makeText(getApplication(),getApplication().getString(R.string.toast_add_success),Toast.LENGTH_SHORT).show();
                callBack.refreshData();
            }

            @Override
            protected void onFailure() {
                Toast.makeText(getApplication(),getApplication().getString(R.string.toast_add_error),Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onError() {
                Toast.makeText(getApplication(),getApplication().getString(R.string.toast_add_error),Toast.LENGTH_SHORT).show();
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
            @Override
            protected void showMessage(String message) {
                //Toast.makeText(getApplication(),message,Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onSuccess(String data) {
                callBack.addSceneSuccess(scenes);
                //callBack.refreshData();
            }

            @Override
            protected void onFailure() {
                callBack.addSceneFailure();
            }

            @Override
            protected void onError() {
                callBack.addSceneFailure();
            }
        });


    }

    public void updateRegion(Region region){
        User user = VcomSingleton.getInstance().getLoginUser();
        ApiLoader.getApi().modifyArea(user.getUserId(), region.getSpaceId(), region.getSpaceName(), region.getSpaceImg(), new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {
                //Toast.makeText(getApplication(),message,Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onSuccess(String data) {
                callBack.updateSuccess();
            }

            @Override
            protected void onFailure() {
                callBack.updateFailure();
            }

            @Override
            protected void onError() {
                callBack.updateFailure();
            }
        });
    }

    public void deleteRegion(Region region){
        User user = VcomSingleton.getInstance().getLoginUser();
        ApiLoader.getApi().deleteArea(user.getUserId(), region.getSpaceId(), new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {
                //Toast.makeText(getApplication(),message,Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onSuccess(String data) {
                callBack.deleteSuccess();
            }

            @Override
            protected void onFailure() {
                callBack.deleteFailure();
            }

            @Override
            protected void onError() {
                callBack.deleteFailure();
            }
        });
    }

    public void deleteScene(Scene scene){
        ApiLoader.getApi().deleteScene(scene.getSceneId(), new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {
                Toast.makeText(getApplication(),message,Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onSuccess(String data) {
                EventBus.getDefault().post(new MessageEvent(MessageEvent.refreshRegion));
                callBack.goDeleteSceneSuccess(scene);
            }

            @Override
            protected void onFailure() {

            }

            @Override
            protected void onError() {

            }
        });
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

    public void selectRecord(Equip equip,String sceneId, String equipId) {
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


    /*
    * banlap: 主页 点击区域图片 查询该区域下的所有场景
    * */
    public void getCurrentRegion(String spaceId) {
        ApiLoader.getApi().selectListScene(spaceId, new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {

            }

            @Override
            protected void onSuccess(String data) {
                VcomSingleton.getInstance().setUserScene(GsonUtil.getInstance().json2List(data, new TypeToken<List<Scene>>() {}.getType()));
                callBack.getCurrentRegionSuccess();
            }

            @Override
            protected void onFailure() {

            }

            @Override
            protected void onError() {

            }
        });

    }

    public void refreshRegion() {
        callBack.refreshData();
    }

    public void getWeather() {
        /*
        * 天气api  https://tianqiapi.com/
        * 详情参考官接口网文档
        * */
        String version = "v6";
        String appId = "77631512";
        String appSecret = "N21YkpFT";

        ApiLoader.getApi().getWeather(version, appId, appSecret, new ApiObserver<ResponseBody>() {

            @Override
            protected void showMessage(String message) {
            }

            @Override
            protected void onSuccess(String data) {
                System.out.println("banlap:" + "weather: " + data);
                VcomSingleton.getInstance().setWeather(GsonUtil.getInstance().json2Bean(data, Weather.class));
                EventBus.getDefault().post(new MessageEvent(MessageEvent.weatherReady));
            }

            @Override
            protected void onFailure() {
                System.out.println("banlap:" + "weather:failure ");
            }

            @Override
            protected void onError() {
                System.out.println("banlap:" + "weather: error");
            }
        });
    }

    public void removeUserEquip(String spaceId, String userEquipId) {
        ApiLoader.getApi().deleteByUserEquip(spaceId, userEquipId, new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {

            }

            @Override
            protected void onSuccess(String data) {
                EventBus.getDefault().post(new MessageEvent(MessageEvent.refreshEquip));
                callBack.removeUserEquipSuccess();
            }

            @Override
            protected void onFailure() {
                callBack.removeUserEquipFailure();
            }

            @Override
            protected void onError() {
                callBack.removeUserEquipFailure();
            }
        });
    }

    public void removeEquip(String equipId) {
        ApiLoader.getApi().deleteEquip(VcomSingleton.getInstance().getLoginUser().getUserId(), equipId, new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {
                //Toast.makeText(getApplication(),message,Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onSuccess(String data) {
                EventBus.getDefault().post(new MessageEvent(MessageEvent.refreshEquip));
                callBack.removeEquipSuccess();
            }

            @Override
            protected void onFailure() {

            }

            @Override
            protected void onError() {

            }
        });
    }

    public void saveParams(String json) {
        ApiLoader.getApi().addSceneEquip(json, new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {

            }

            @Override
            protected void onSuccess(String data) {
            }

            @Override
            protected void onFailure() {
            }

            @Override
            protected void onError() {
            }
        });
    }

    public void getUserAllEquip() {
        ApiLoader.getApi().getAllEquips(VcomSingleton.getInstance().getLoginUser().getUserId(), new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {
            }

            @Override
            protected void onSuccess(String data) {
                VcomSingleton.getInstance().setUserEquips(GsonUtil.getInstance().json2List(data, new TypeToken<List<Equip>>() {
                }.getType()));
                EventBus.getDefault().post(new MessageEvent(MessageEvent.equipReady));
            }

            @Override
            protected void onFailure() {

            }

            @Override
            protected void onError() {

            }
        });
    }

    public synchronized void onOnlineStatusNotify(byte[] data) {
        if (data == null) {
            for (Equip userEquip : VcomSingleton.getInstance().getUserEquips()) {
                userEquip.setConnectionStatus(ConnectionStatus.OFFLINE);
            }
            callBack.notifyChanged(VcomSingleton.getInstance().getUserEquips());
            return;
        }
        int position = 0;
        int packetSize = 4;
        int length = data.length;

        while ((position + packetSize) < length) {
            int meshAddress = data[position++] & 0xFF;

            Equip equip = null;
            for (Equip userEquip : VcomSingleton.getInstance().getUserEquips()) {
                if (meshAddress == Integer.parseInt(userEquip.getMeshAddress())) {
                    equip = userEquip;
                    break;
                }
            }
            if (equip == null) {
                return;
            }

            int status = data[position++];

            switch (equip.getProductUuid()) {
                case "22532"://三位
                    Log.e("curtain_test", status + " Switch --> " + Arrays.bytesToHexString(data, ","));
                    position++;
                    byte bytes = data[position++];
                    equip.setSwitchStatus((bytes & 0x01) + "" + ((bytes & 0x02) >> 1) + "" + ((bytes & 0x04) >> 2));
                    break;
                case "21508":
                    Log.e("curtain_test", status + " Touch --> " + Arrays.bytesToHexString(data, ","));
                    break;
                default:
                    Log.e("curtain_test", "Lamp --> " + Arrays.bytesToHexString(data, ","));
                    equip.setBrightness(data[position++]);
                    equip.setMode(data[position++]);

                    Log.e("curtain_test", "mode: " + equip.getMode() + " brightness: " + equip.getBrightness());
                    switch (equip.getMode()) {
                        case 1: //暖光
                            equip.setTemperature(data[position++]);
                            break;
                        case 2: //RGB
                            equip.setRed(data[position++]);
                            equip.setGreen(data[position++]);
                            equip.setBlue(data[position++]);
                            break;
                        case 3: //闪烁
                            break;
                        case 4: //呼吸
                            break;
                        case 5: //流光
                            break;
                    }
                    break;
            }

            if (status == 0) {
                equip.setConnectionStatus(ConnectionStatus.OFFLINE);
            } else if (equip.getBrightness() != 0) {
                equip.setConnectionStatus(ConnectionStatus.ON);
            } else {
                equip.setConnectionStatus(ConnectionStatus.OFF);
            }

            callBack.notifyChanged(VcomSingleton.getInstance().getUserEquips());
        }
    }

    public void onDeviceStatusChanged(DeviceEvent event) {

    }



    public interface MainFvmCallBack {

        void goControlMenu();

        void goSelectRegion();

        void goEditRegion();

        void goGuideRegion();

        void goGuideScene();

        void goAddScene();

        void goAddNewScene();

        void goRegionManger();

        void goSceneListManager();

        void refreshData();

        void goDeleteSceneSuccess(Scene scene);

        void selectRecordSuccess(Equip equip, String data);
        void selectRecordFailure(Equip equip);

        void addSceneSuccess(List<Scene> scenes);
        void addSceneFailure();

        void deleteSuccess();
        void deleteFailure();

        void removeEquipSuccess();

        void removeUserEquipSuccess();
        void removeUserEquipFailure();

        void notifyChanged(List<Equip> equips);

        void updateSuccess();
        void updateFailure();
        void getCurrentRegionSuccess();
    }

}
