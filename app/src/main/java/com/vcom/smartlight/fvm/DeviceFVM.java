package com.vcom.smartlight.fvm;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.telink.bluetooth.event.DeviceEvent;
import com.telink.bluetooth.light.ConnectionStatus;
import com.telink.bluetooth.light.DeviceInfo;
import com.telink.bluetooth.light.LightAdapter;
import com.telink.util.Arrays;
import com.vcom.smartlight.model.Equip;
import com.vcom.smartlight.model.MessageEvent;
import com.vcom.smartlight.request.ApiLoader;
import com.vcom.smartlight.request.ApiObserver;
import com.vcom.smartlight.server.VcomService;
import com.vcom.smartlight.singleton.VcomSingleton;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import okhttp3.ResponseBody;

/**
 * @Author Lzz
 * @Date 2020/10/27 18:45
 */
public class DeviceFVM extends AndroidViewModel {

    private DeviceFvmCallBack callBack;

    private final List<Equip> equipList = VcomSingleton.getInstance().getUserEquips();

    public DeviceFVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(DeviceFvmCallBack callBack) {
        this.callBack = callBack;
    }

    //banlap: 智能页面上，点击选择'智能照明'按钮
    public void selectLight() {
        callBack.selectSmartLight();
    }
    //banlap: 智能页面上，点击选择'安全电箱'按钮
    public void selectSafeBox() {
       callBack.selectSafeElectricBox();
    }
    //banlap: 智能页面上，列表无设备时，点击'搜索设备'按钮，添加设备
    public void clickLight() { callBack.clickSmartLight(); }
    //banlap: 智能页面上，点击选择'安全电箱'按钮后，点击'未添加'按钮，添加电箱
    public void clickSafeBox() { callBack.clickSafeElectricBoxSetting(); }

    public void goScan() {
        callBack.goScanDevice();
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

    public void saveParams(String json) {
        ApiLoader.getApi().addSceneEquip(json, new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {

            }

            @Override
            protected void onSuccess(String data) {
                callBack.saveParamsSuccess();
            }

            @Override
            protected void onFailure() {
                callBack.saveParamsFailure();
            }

            @Override
            protected void onError() {
                callBack.saveParamsFailure();
            }
        });
    }

    public void dataChanged() {
        equipList.clear();
        equipList.addAll(VcomSingleton.getInstance().getUserEquips());
        callBack.notifyChanged(equipList);
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

        Log.e("curtain_test", "onOnlineStatusNotify()");

        DeviceInfo deviceInfo = event.getArgs();
        switch (deviceInfo.status) {
            case LightAdapter.STATUS_LOGIN:
                if (VcomService.getInstance().getMode() == LightAdapter.MODE_AUTO_CONNECT_MESH) {

                    Log.e("curtain_test", "-----------------------------");

                    //发送 cmd为0xe4，为Time_Set的命令码
//                    mHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            NewLightService.getInstance().sendCommandNoResponse((byte) 0xE4, 0xFFFF, new byte[]{});
//                        }
//                    }, 3 * 1000);
                }

//                if (App.getApp().getMesh().isOtaProcessing() && !MeshOTAService.isRunning) {
//                    // 获取本地设备OTA状态信息
//                    MeshCommandUtil.getDeviceOTAState();
//                }

                break;
            case LightAdapter.STATUS_CONNECTING:
                break;
            case LightAdapter.STATUS_LOGOUT:
                onLogout();
                break;
            case LightAdapter.STATUS_ERROR_N:
                onNError();
                break;
        }

    }

    private void onLogout() {
        //mDevicesAdapter.notifyDataSetChanged();
    }

    private void onNError() {
        VcomService.getInstance().idleMode(true);
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
                callBack.removeEquipFailure();
            }

            @Override
            protected void onError() {
                callBack.removeEquipFailure();
            }
        });
    }

    public interface DeviceFvmCallBack {

        void goScanDevice();

        void notifyChanged(List<Equip> equips);

        void selectRecordSuccess(Equip equip, String data);
        void selectRecordFailure(Equip equip);

        void removeEquipSuccess();
        void removeEquipFailure();

        void selectSmartLight();
        void selectSafeElectricBox();

        void clickSmartLight();
        void clickSafeElectricBoxSetting();

        void saveParamsSuccess();
        void saveParamsFailure();

    }

}
