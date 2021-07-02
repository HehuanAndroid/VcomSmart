package com.vcom.smartlight.uivm;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.google.gson.reflect.TypeToken;
import com.telink.bluetooth.event.DeviceEvent;
import com.telink.bluetooth.event.NotificationEvent;
import com.telink.bluetooth.light.ConnectionStatus;
import com.telink.bluetooth.light.LeAutoConnectParameters;
import com.telink.bluetooth.light.LeRefreshNotifyParameters;
import com.telink.bluetooth.light.Parameters;
import com.telink.util.Arrays;
import com.telink.util.Event;
import com.vcom.smartlight.VcomApp;
import com.vcom.smartlight.model.Equip;
import com.vcom.smartlight.model.MessageEvent;
import com.vcom.smartlight.model.Region;
import com.vcom.smartlight.model.Scene;
import com.vcom.smartlight.model.User;
import com.vcom.smartlight.request.ApiLoader;
import com.vcom.smartlight.request.ApiObserver;
import com.vcom.smartlight.server.VcomService;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.utils.GsonUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import okhttp3.ResponseBody;

/**
 * @author Banlap on 2021/3/30
 */
public class RegionVM extends AndroidViewModel {

    private RegionCallBack callBack;

    public RegionVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(RegionCallBack callBack){
        this.callBack = callBack;
    }

    public void viewBack(){
        callBack.viewBack();
    }

    public void showDetail(){
        callBack.showDetail();
    }

    public void selectUserRegion() {
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

    public void goSceneListManager() {
        callBack.goSceneListManager();
    }

    public void goScanDevice() {
        callBack.goScanDevice();
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
                Toast.makeText(getApplication(),message,Toast.LENGTH_SHORT).show();
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

    public void mainVmPerformed(Event<String> event) {
        switch (event.getType()) {
            case NotificationEvent.ONLINE_STATUS:
                onOnlineStatusNotify((NotificationEvent) event);
                break;
            case DeviceEvent.STATUS_CHANGED:
                onDeviceStatusChanged((DeviceEvent) event);
                break;
        }
    }

    private synchronized void onOnlineStatusNotify(NotificationEvent event) {
        byte[] data = (byte[]) event.parse();
        EventBus.getDefault().post(new MessageEvent(MessageEvent.onlineStatusNotify, data));

    }

    private void onDeviceStatusChanged(DeviceEvent event) {
        EventBus.getDefault().post(new MessageEvent(MessageEvent.onlineStatusNotify, event));
    }

    public void startAutoConnect() {

        //自动连接参数
        LeAutoConnectParameters connectParams = LeAutoConnectParameters.createAutoConnectParameters();
        connectParams.setMeshName(VcomApp.NEW_NAME);
        connectParams.setPassword(VcomSingleton.getInstance().getLoginUser().getMeshPassword());
        connectParams.autoEnableNotification(true);
        connectParams.set(Parameters.PARAM_OFFLINE_TIMEOUT_SECONDS, 40);
        VcomService.getInstance().autoConnect(connectParams);


        //刷新Notify参数, 重新回到主页时不刷新
        LeRefreshNotifyParameters refreshNotifyParams = Parameters.createRefreshNotifyParameters();
        refreshNotifyParams.setRefreshRepeatCount(2);
        refreshNotifyParams.setRefreshInterval(2000);
        //开启自动刷新Notify
        VcomService.getInstance().autoRefreshNotify(refreshNotifyParams);

    }

    public interface RegionCallBack {
        void viewBack();
        void showDetail();
        void notifyChanged(List<Equip> equips);
        void goSceneListManager();
        void goScanDevice();

        void selectRecordSuccess(Equip equip, String data);
        void selectRecordFailure(Equip equip);

        void removeEquipSuccess();
        void removeUserEquipSuccess();
        void removeUserEquipFailure();
    }
}
