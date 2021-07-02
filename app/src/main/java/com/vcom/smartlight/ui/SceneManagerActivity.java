package com.vcom.smartlight.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;
import com.telink.bluetooth.event.NotificationEvent;
import com.telink.bluetooth.light.ConnectionStatus;
import com.telink.util.Event;
import com.telink.util.EventListener;
import com.vcom.smartlight.R;
import com.vcom.smartlight.VcomApp;
import com.vcom.smartlight.base.BaseBindingAdapter;
import com.vcom.smartlight.base.BaseMvvmActivity;
import com.vcom.smartlight.databinding.ActivitySceneManagerBinding;
import com.vcom.smartlight.databinding.DialogDeviceListNullBinding;
import com.vcom.smartlight.databinding.DialogSceneAddEquipBinding;
import com.vcom.smartlight.databinding.DialogSelectIconBinding;
import com.vcom.smartlight.databinding.ItemScanDeviceBinding;
import com.vcom.smartlight.databinding.ItemSceneManagerEquipBinding;
import com.vcom.smartlight.model.Equip;
import com.vcom.smartlight.model.MessageEvent;
import com.vcom.smartlight.model.Product;
import com.vcom.smartlight.model.Region;
import com.vcom.smartlight.model.Scene;
import com.vcom.smartlight.model.Timing;
import com.vcom.smartlight.server.VcomService;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.uivm.SceneManagerVM;
import com.vcom.smartlight.utils.GsonUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SceneManagerActivity extends BaseMvvmActivity<SceneManagerVM, ActivitySceneManagerBinding>
        implements SceneManagerVM.SceneManagerVmCallBack, EventListener<String> {

    private Scene mScene;
    private String sceneId;
    private String mRegionId;

    private AlertDialog alertDialog;
    private SceneManagerAdapter adapter;
    private final List<Equip> sceneEquips = new ArrayList<>();
    private final List<Equip> equipList = VcomSingleton.getInstance().getUserEquips();
    private List<Timing> timingList = new ArrayList<>();
    private Equip equipTemp = new Equip();

    private int tagMeshAddress = -1;
    private byte[] tagParam = null;

    //默认场景图标
    private Integer[] mIcons = {R.drawable.ic_scene_0, R.drawable.ic_scene_1, R.drawable.ic_scene_2, R.drawable.ic_scene_3};
    private Integer[] mIconSelected = {R.drawable.ic_scene_selected_0, R.drawable.ic_scene_selected_1, R.drawable.ic_scene_selected_2, R.drawable.ic_scene_selected_3};

    //场景图标unicode
    private Integer[] mIconsUnicode = {R.string.icon_scene_0, R.string.icon_scene_1, R.string.icon_scene_2, R.string.icon_scene_3};

    public static int mSelectIndex = 0;    //选择场景图标位置, 默认为 0

    private String mHour="";
    private String mMin="";

    private boolean isSunday=false;
    private boolean isMonday=false;
    private boolean isTuesday=false;
    private boolean isWednesday=false;
    private boolean isThursday=false;
    private boolean isFriday=false;
    private boolean isSaturday=false;

    private int mTimingCount=0;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_scene_manager;
    }

    @Override
    protected void initViews() {
        getViewDataBind().setVm(getViewModel());
        getViewModel().setCallBack(this);
        EventBus.getDefault().register(this);

        //banlap: 判断mScene不为null时 传入相应值
        if(mScene!=null) {
            getViewDataBind().sceneManagerName.setText(mScene.getSceneName());
            getViewDataBind().sceneManagerName.setSelection(mScene.getSceneName().length());
            //banlap: 传递场景图标
            int iconCount = Integer.parseInt(mScene.getSceneImg().trim());
            getViewDataBind().ivSelectSceneIcon.setBackgroundResource(mIcons[iconCount]);
            mSelectIndex = iconCount;
        }

        adapter = new SceneManagerAdapter(this);
        adapter.setItems(sceneEquips);
        getViewDataBind().sceneManagerRecycler.setLayoutManager(new LinearLayoutManager(this));
        getViewDataBind().sceneManagerRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        getCurrentTiming();
    }

    @Override
    protected void onStart() {
        super.onStart();
        VcomApp.getApp().doInit();

        VcomApp.getApp().addEventListener(NotificationEvent.GET_ALARM, this);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch(event.msgCode) {
            //banlap: 刷新场景信息
            case MessageEvent.regionReady:
                for (Region region : VcomSingleton.getInstance().getUserRegion()) {
                    for (Scene scene : region.getSceneList()) {
                        if (scene.getSceneId().equals(sceneId)) {
                            mScene = scene;
                            break;
                        }
                    }
                }
                if (mScene == null) {
                    finish();
                }
                sceneEquips.clear();
                sceneEquips.addAll(mScene.getUserEquipList());
                adapter.notifyDataSetChanged();
                break;
            //banlap: 当前账号没有绑定设备时，跳转到添加设备
            case MessageEvent.switchFragment:
                Intent goScanIntent = new Intent(this, ScanByBleActivity.class);
                startActivity(goScanIntent);
                break;
        }
    }

    @Override
    protected void initDatum() {
        if (getIntent().getExtras() == null) {
            finish();
        }

        sceneId = getIntent().getStringExtra("sceneId");
        mRegionId = getIntent().getStringExtra("regionId");
        for (Region region : VcomSingleton.getInstance().getUserRegion()) {
            for (Scene scene : region.getSceneList()) {
                if (scene.getSceneId().equals(sceneId)) {
                    mScene = scene;
                    break;
                }
            }
        }

        if (mScene == null) {
            finish();
        } else {
            sceneEquips.addAll(mScene.getUserEquipList());
            VcomSingleton.getInstance().setCurrentSceneEquips(sceneEquips);
        }

    }

    /*
     * banlap: 获取闹钟/定时 发送事件
     * */
    public void getCurrentTiming() {
        byte opcode = (byte) 0xE6;     //获取灯的闹钟 操作码
        byte timingCode = (byte) 0x00; //0x00 表示读取所有的闹钟的详细信息
        byte[] param = new byte[] {(byte) 0x10, timingCode};

        for(int i=0; i < sceneEquips.size(); i++) {
            if (Integer.parseInt(sceneEquips.get(i).getMeshAddress()) != -1) {
                VcomService.getInstance().sendCommandNoResponse(opcode, Integer.parseInt(sceneEquips.get(i).getMeshAddress()), param);
            }
        }
    }

    /*
     * banlap: 获取闹钟/定时 接收通知事件
     * */
    @Override
    public void performed(Event<String> event) {
        switch (event.getType()) {
            case NotificationEvent.GET_ALARM:
                onGetAlarm((NotificationEvent) event);
                break;
        }
    }
    private synchronized void onGetAlarm(NotificationEvent event) {
        if(event.getArgs().opcode!=0) {
            byte[] data = event.getArgs().params;
            if (data == null) {
                return;
            }
            if(data[0] == 0) {
                return;
            }
            Timing timing = new Timing();
            timing.setTimingId(String.valueOf(data[1]));
            timing.setWeek(String.valueOf(data[4]));
            timing.setHour(String.valueOf(data[5]));
            timing.setMinute(String.valueOf(data[6]));
            timing.setSceneMeshId(String.valueOf(data[8]));
            mTimingCount = data[9];
            timing.setEnabled(false);


            if(timingList.size()>0) {
                boolean isAdd=true;
                for(int i=0; i<timingList.size(); i++) {
                    if(timingList.get(i).getTimingId().equals(timing.getTimingId())) {
                        isAdd = !isAdd;
                    }
                }
                if(isAdd) {
                    timingList.add(timing);
                }
            } else {
                timingList.add(timing);
            }

            //VcomSingleton.getInstance().setTimingList(timingList);
        }
    }

    @Override
    public void viewBack() {
        finish();
    }

    @Override
    public void viewGoAddEquip() {
        alertDialog = null;
        if (equipList.size() > 0) {

            List<Equip> newEquipList = new ArrayList<>();

            for (Equip equip : equipList) {
                //banlap: 场景中添加设备列表时 除去21508 智能场景开关
                if (!equip.getProductUuid().equals("21508")) {
                    equip.setCheck(false);
                    newEquipList.add(equip);
                }
            }

            for (Equip sceneEquip : sceneEquips) {
                for (Equip equip : newEquipList) {
                    if (sceneEquip.getMac().equals(equip.getMac())) {
                        newEquipList.remove(equip);
                        break;
                    }
                }
            }

            DialogSceneAddEquipBinding addEquipBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_scene_add_equip,
                    null, false);
            alertDialog = new AlertDialog.Builder(this)
                    .setView(addEquipBinding.getRoot())
                    .create();
            ScanAddEquipAdapter deviceAdapter = new ScanAddEquipAdapter(this);
            addEquipBinding.dialogSceneAddEquipRecycler.setLayoutManager(new LinearLayoutManager(this));
            addEquipBinding.dialogSceneAddEquipRecycler.setAdapter(deviceAdapter);
            deviceAdapter.setItems(newEquipList);
            deviceAdapter.notifyDataSetChanged();

            addEquipBinding.tvCancel.setOnClickListener(v -> alertDialog.dismiss());
            addEquipBinding.dialogSceneAddEquipCancel.setOnClickListener(v -> alertDialog.dismiss());
            addEquipBinding.dialogSceneAddEquipCancel.setVisibility(View.GONE);
            addEquipBinding.dialogSceneAddEquipNext.setOnClickListener(v -> {
                for (Equip equip : newEquipList) {
                    if (equip.isCheck()) {
                        getParamsForActivity(equip);
                        alertDialog.dismiss();
                        break;
                    }
                }
            });

        } else {
            DialogDeviceListNullBinding deviceListNullBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_device_list_null,
                    null, false);
            alertDialog = new AlertDialog.Builder(this)
                    .setView(deviceListNullBinding.getRoot())
                    .create();

            deviceListNullBinding.ivDialogCancel.setOnClickListener(v-> alertDialog.dismiss());
            deviceListNullBinding.dialogMessageCancel.setOnClickListener(v-> alertDialog.dismiss());
            deviceListNullBinding.dialogMessageConform.setOnClickListener(v-> {

                getViewModel().goAddEquip();
                alertDialog.dismiss();
                finish();
            });
                    /*.setTitle(getString(R.string.dialog_title))
                    .setMessage(getString(R.string.dialog_message_add_device_into_empty_list))
                    .setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(getString(R.string.dialog_confirm), (dialog, which) -> {
                        getViewModel().goAddEquip();
                        dialog.dismiss();
                        finish();
                    })
                    .create();*/
        }
        alertDialog.show();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_switch_bg_gray_f9);
    }

    /*
     * banlap: 选择场景图标
     * */
    @Override
    public void viewSelectSceneIcon() {
        DialogSelectIconBinding selectIconBinding = DataBindingUtil.inflate(LayoutInflater.from(this),
                R.layout.dialog_select_icon, null, false);
        alertDialog = new AlertDialog.Builder(this)
                .setView(selectIconBinding.getRoot())
                .create();


        SceneManagerActivity.SceneIconAdapter sceneIconAdapter = new SceneManagerActivity.SceneIconAdapter(this, mIcons, mIconSelected, mIconsUnicode);
        selectIconBinding.rvSelectIconList.setLayoutManager(new GridLayoutManager(this, 2));
        selectIconBinding.rvSelectIconList.setAdapter(sceneIconAdapter);
        selectIconBinding.btSelectIconCancel.setOnClickListener(v->{alertDialog.dismiss();});
        sceneIconAdapter.notifyDataSetChanged();

        //banlap: 点击确认选择该场景图标
        selectIconBinding.btSelectIconCommit.setOnClickListener(v->{
            alertDialog.dismiss();
            getViewDataBind().ivSelectSceneIcon.setBackgroundResource(mIcons[mSelectIndex]);
        });

        alertDialog.show();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_switch_bg_gray_f9);



    }
    /*
     * banlap: 选择场景定时
     * */
    @Override
    public void viewSelectSceneTiming() {
        Intent intent = new Intent(this, TimingListActivity.class);
        intent.putExtra("SceneId", sceneId);
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == 0x1A) {
                if(result.getData() != null) {
                    mHour = result.getData().getStringExtra("NewHour");
                    mMin = result.getData().getStringExtra("NewMin");

                    isSunday = result.getData().getBooleanExtra("NewSunday", false);
                    isMonday = result.getData().getBooleanExtra("NewMonday", false);
                    isTuesday = result.getData().getBooleanExtra("NewTuesday", false);
                    isWednesday = result.getData().getBooleanExtra("NewWednesday", false);
                    isThursday = result.getData().getBooleanExtra("NewThursday", false);
                    isFriday = result.getData().getBooleanExtra("NewFriday", false);
                    isSaturday = result.getData().getBooleanExtra("NewSaturday", false);

                    String time = mHour + ":" + mMin;
                    if(mHour!=null && mMin!=null) {
                        getViewDataBind().tvShowTiming.setText(time);
                        getViewDataBind().tvShowTiming.setTextColor(getResources().getColor(R.color.green));

                        StringBuilder week= new StringBuilder();
                        week.append(isSunday? getString(R.string.sunday)+" ":"")
                            .append(isMonday? getString(R.string.monday)+" ":"")
                            .append(isTuesday? getString(R.string.tuesday)+" ":"")
                            .append(isWednesday? getString(R.string.wednesday)+" ":"")
                            .append(isThursday? getString(R.string.thursday)+" ":"")
                            .append(isFriday? getString(R.string.friday)+" ":"")
                            .append(isSaturday? getString(R.string.saturday)+" ":"")
                        ;
                        getViewDataBind().tvShowWeek.setText(week);

                        if(isSunday && isMonday && isTuesday && isWednesday && isThursday && isFriday && isSaturday) {
                            getViewDataBind().tvShowWeek.setText(getString(R.string.everyday));
                        } else if (!isSunday && isMonday && isTuesday && isWednesday && isThursday && isFriday && !isSaturday) {
                            getViewDataBind().tvShowWeek.setText(getString(R.string.weekdays));
                        } else if (!isSunday && !isMonday && !isTuesday && !isWednesday && !isThursday && !isFriday && !isSaturday) {
                            getViewDataBind().tvShowWeek.setText(getString(R.string.everyday));
                        }
                    }

                }
            }
        }).launch(intent);
    }

    @Override
    public void viewGoDeleteEquip() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_title))
                .setMessage(getString(R.string.dialog_message_delete_scene))
                .setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dialog.dismiss())
                .setPositiveButton(getString(R.string.dialog_confirm), (dialog, which) -> {
                    //getViewModel().deleteScene(mScene.getSceneId());
                    deleteSceneEquip(mScene);
                    finish();
                })
                .create();
        alertDialog.show();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_switch_bg_gray_f9);

    }
    //banlap: 先对设备删除场景id 后删除场景
    public void deleteSceneEquip(Scene scene) {
        //banlap: 根据场景里面的设备上逐步删除场景id
        if(scene.getUserEquipList().size()>0) {
            for (int i=0; i<scene.getUserEquipList().size(); i++) {
                if(Integer.parseInt(scene.getUserEquipList().get(i).getMeshAddress()) != -1) {
                    int meshId = Integer.parseInt(scene.getUserEquipList().get(i).getMeshAddress());
                    byte opcode = (byte) 0xEE;
                    byte[] param = {0x00, (byte) Integer.parseInt(scene.getSceneMeshId())};
                    VcomService.getInstance().sendCommandNoResponse(opcode, meshId, param);
                }
            }
        }
        getViewModel().deleteScene(scene.getSceneId());
    }
    /*
     * banlap: 修改场景页面 - 删除场景
     * */
    @Override
    public void deleteSceneSuccess() {
        Toast.makeText(this, getString(R.string.toast_delete_success),Toast.LENGTH_SHORT).show();
       /* byte opcode = (byte) 0xEE;
        byte[] param = {0x00, (byte) Integer.parseInt(mScene.getSceneMeshId())};
        VcomService.getInstance().sendCommandNoResponse(opcode, 0XFF, param);*/
    }

    @Override
    public void deleteSceneFailure() {
        Toast.makeText(this, getString(R.string.toast_delete_error),Toast.LENGTH_SHORT).show();
    }

    /*
    * banlap: 更新场景信息
    * */
    @Override
    public void viewGoUpdateScene() {
        String newSceneName = getViewDataBind().sceneManagerName.getText().toString();
        if (TextUtils.isEmpty(newSceneName)) {
            return;
        }
        mScene.setSceneName(newSceneName);
        //banlap: 更新场景图标
        mScene.setSceneImg(String.valueOf(mSelectIndex));
        getViewModel().updateScene(mScene);
    }

    /*
     * banlap: 更新场景 返回成功
     * */
    @Override
    public void updateSceneSuccess() {
        Toast.makeText(this, getString(R.string.toast_update_success),Toast.LENGTH_SHORT).show();

        if(mHour !=null && mMin !=null) {
            if(mTimingCount<15) {
                //setCurrentTime(mHour, mMin);
            } else {
                Toast.makeText(this, "定时已超过15个",Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
    /*
     * banlap: 更新场景 返回失败
     * */
    @Override
    public void updateSceneFailure() {
        Toast.makeText(this, getString(R.string.toast_update_error),Toast.LENGTH_SHORT).show();
    }

    /*
    * banlap: 设置当前时间 到设备中
    * */
    private void setCurrentTime(String cHour, String cMin) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy,M,dd,HH,mm,ss");
        String currentTime = simpleDateFormat.format(new Date());
        String[] str =currentTime.split(",");
        int year=0, month=0, day=0, hour=0, min=0, sec=0;
        for(int i=0; i<str.length; i++) {
            switch (i) {
                case 0: year = Integer.parseInt(str[i]); break;
                case 1: month = Integer.parseInt(str[i]); break;
                case 2: day = Integer.parseInt(str[i]); break;
                case 3: hour = Integer.parseInt(str[i]); break;
                case 4: min = Integer.parseInt(str[i]); break;
                case 5: sec = Integer.parseInt(str[i]); break;
            }
        }
        int yearMsb = (year&0xff00)>>8;
        int yearLsb = year&0xff;

        byte opcode = (byte) 0xE4;     //设置灯的时间 操作码
        byte[] param = new byte[] {(byte) yearLsb, (byte) yearMsb,  (byte) month, (byte) day,  (byte) hour, (byte) min, (byte) sec};

        for(int i=0; i < sceneEquips.size(); i++) {
            if (Integer.parseInt(sceneEquips.get(i).getMeshAddress()) != -1) {
                VcomService.getInstance().sendCommandNoResponse(opcode, Integer.parseInt(sceneEquips.get(i).getMeshAddress()), param);
            }
        }

        setTiming(cHour, cMin);
    }
    /*
     * banlap: 设置定时 到设备中
     * */
    private void setTiming(String cHour, String cMin) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M,dd");
        String currentTime = simpleDateFormat.format(new Date());
        String[] str =currentTime.split(",");
        int month=0, day=0;
        for(int i=0; i<str.length; i++) {
            switch (i) {
                case 0:
                    month = Integer.parseInt(str[i]);
                    break;
                case 1:
                    day = Integer.parseInt(str[i]);
                    break;
            }
        }

        byte opcode = (byte) 0xE5;     //闹钟操作码
        byte alarmType = (byte) 0x02;  //更新闹钟
        int index = mTimingCount+1; //闹钟索引
        byte timingType = (byte) 0x92; //0x92: 表示按week、场景id  0x82: 表示按day、场景id
        byte[] param = new byte[] {alarmType, (byte) index, timingType, (byte) 0x00, (byte) (
                    (isMonday? 0x01<<1:0) | (isTuesday? 0x01<<2:0)| (isWednesday? 0x01<<3:0)|
                    (isThursday? 0x01<<4:0) | (isFriday? 0x01<<5:0) | (isSaturday? 0x01<<6:0)|
                    (isSunday? 0x01:0) & (0x7f) ),
                (byte) Integer.parseInt(cHour), (byte) Integer.parseInt(cMin),
                (byte) 0x00, (byte) Integer.parseInt(mScene.getSceneMeshId())};

       /* byte[] param = new byte[] {alarmType, (byte) index, (byte) 0x82, (byte) month, (byte) day,
                (byte) Integer.parseInt(cHour), (byte) Integer.parseInt(cMin),
                (byte) 0x00, (byte) Integer.parseInt(mScene.getSceneMeshId())};*/

        for(int i=0; i < sceneEquips.size(); i++) {
            if(Integer.parseInt(sceneEquips.get(i).getMeshAddress()) != -1) {
                VcomService.getInstance().sendCommandNoResponse(opcode, Integer.parseInt(sceneEquips.get(i).getMeshAddress()), param);
            }
        }
    }

    /*
     * banlap: 修改场景页面 - 删除设备
     * */
    @Override
    public void deleteSceneEquipSuccess() {
        Toast.makeText(this, getString(R.string.toast_delete_success),Toast.LENGTH_SHORT).show();
        if (tagMeshAddress == -1) {
            return;
        }
        byte opcode = (byte) 0xEE;
        byte[] param = {0x00, (byte) Integer.parseInt(mScene.getSceneMeshId())};
        VcomService.getInstance().sendCommandNoResponse(opcode, tagMeshAddress, param);
    }

    @Override
    public void deleteSceneEquipFailure() {
        Toast.makeText(this, getString(R.string.toast_delete_error),Toast.LENGTH_SHORT).show();
    }


    private void deleteSceneEquip(Equip equip) {
        tagMeshAddress = -1;
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_title))
                .setMessage(getString(R.string.dialog_message_delete_scene_device))
                .setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dialog.dismiss())
                .setPositiveButton(getString(R.string.dialog_confirm), (dialog, which) -> {
                    tagMeshAddress = Integer.parseInt(equip.getMeshAddress());
                    getViewModel().deleteSceneEquip(VcomSingleton.getInstance().getLoginUser().getUserId(), mRegionId, mScene.getSceneId(), equip.getUserEquipId());
                    dialog.dismiss();
                })
                .create();
        alertDialog.show();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_switch_bg_gray_f9);

    }

    private void showParams(Equip equip) {
        getViewModel().selectRecord(equip, mScene.getSceneId(), equip.getUserEquipId());
    }

    @Override
    public void selectRecordSuccess(Equip equip, String data) {
        equipTemp = GsonUtil.getInstance().json2List(data, new TypeToken<Equip>() {}.getType());
        getParamsForActivity(equip);
    }

    @Override
    public void selectRecordFailure(Equip equip) {
        getParamsForActivity(equip);
    }

    private void getParamsForActivity(Equip equip) {
        tagParam = null;
        tagMeshAddress = -1;

        Intent intent = new Intent(this, DeviceSettingActivity.class);
        intent.putExtra("sceneId", mScene.getSceneId());
        intent.putExtra("equipId", equip.getUserEquipId());

        intent.putExtra("mode", equipTemp.getMode());
        intent.putExtra("brightness", Integer.parseInt(String.valueOf(equipTemp.getBrightness())));
        intent.putExtra("temp", Integer.parseInt(String.valueOf(equipTemp.getTemperature())));
        intent.putExtra("red", equipTemp.getRed());
        intent.putExtra("green", equipTemp.getGreen());
        intent.putExtra("blue", equipTemp.getBlue());

        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == 0x1A) {
                if (result.getData() != null) {
                    String jsonData = result.getData().getStringExtra("save_param");

                    byte cmd = result.getData().getByteExtra("cmd", (byte) 0);
                    tagParam = result.getData().getByteArrayExtra("equip_param");
                    tagMeshAddress = Integer.parseInt(equip.getMeshAddress());

                    Map<String, Object> data = new HashMap<>();
                    if (!TextUtils.isEmpty(jsonData)) {
                        data = GsonUtil.getInstance().json2Map(jsonData);
                    }

                    if (cmd == 0x02) {
                        data.put("equipType", 1);//三位开关
                    } else {
                        data.put("equipType", 0);
                    }

                    data.put("userEquipId", equip.getUserEquipId());
                    data.put("userId", VcomSingleton.getInstance().getLoginUser().getUserId());
                    data.put("sceneId", mScene.getSceneId());
                    data.put("spaceId", mScene.getSpaceId());

                    getViewModel().addSceneEquip(GsonUtil.getInstance().toJson(data));
                }
            }
        }).launch(intent);
    }
    /*
     * banlap: 添加设备到场景中 - 返回成功
     * */
    @Override
    public void addSceneEquipSuccess() {
        Toast.makeText(this, getString(R.string.toast_add_success),Toast.LENGTH_SHORT).show();
        if (tagMeshAddress == -1) {
            return;
        }
        if (tagParam == null) {
            return;
        }
        byte opcode = (byte) 0xEE;
        VcomService.getInstance().sendCommandNoResponse(opcode, tagMeshAddress,
                tagParam);
    }
    /*
     * banlap: 添加设备到场景中 - 返回失败
     * */
    @Override
    public void addSceneEquipFailure() {
        Toast.makeText(this, getString(R.string.toast_add_error),Toast.LENGTH_SHORT).show();
    }



    private class SceneManagerAdapter extends BaseBindingAdapter<Equip, ItemSceneManagerEquipBinding> {

        public SceneManagerAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        protected int getLayoutId(int layoutId) {
            return R.layout.item_scene_manager_equip;
        }

        @Override
        protected void onBindItem(ItemSceneManagerEquipBinding binding, Equip item, int i) {
            Product mProduct = null;
            for (Product product : VcomSingleton.getInstance().getUserProduct()) {
                for (Product.EquipInfo equipInfo : product.getEquipInfoList()) {
                    if (item.getProductUuid().equals(equipInfo.getEquipInfoPid())) {
                        mProduct = product;
                        break;
                    }
                }
            }

            //banlap: 获取当前系统语言;
            String localeLanguage = Locale.getDefault().getLanguage();

            int resId = 0;
            if (mProduct != null) {
                //banlap: 判断当前手机系统是否为en英语
                    if(localeLanguage.equals("en")){
                    binding.itemSceneManagerName.setText(mProduct.getEquiNickName());
                } else {
                    binding.itemSceneManagerName.setText(mProduct.getEquiName());
                }
                resId = mContext.getResources().getIdentifier("ic_" + item.getProductUuid() + "_on", "drawable", mContext.getPackageName());
            }
            if (resId > 0) {
                binding.itemSceneManagerIcon.setBackgroundResource(resId);
            } else {
                binding.itemSceneManagerIcon.setBackgroundResource(R.drawable.ic_lamp_default_on);
            }

            binding.itemDeviceSetting.setOnClickListener(v -> showParams(item));
            //binding.itemDeviceRemove.setOnClickListener(v -> deleteSceneEquip(item));
            binding.flDelete.setOnClickListener(v -> deleteSceneEquip(item));
        }
    }

    private static class ScanAddEquipAdapter extends BaseBindingAdapter<Equip, ItemScanDeviceBinding> {

        public ScanAddEquipAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        protected int getLayoutId(int layoutId) {
            return R.layout.item_scan_device;
        }

        @Override
        protected void onBindItem(ItemScanDeviceBinding itemScanDeviceBinding, Equip item, int i) {

            Product mProduct = null;
            for (Product product : VcomSingleton.getInstance().getUserProduct()) {
                for (Product.EquipInfo equipInfo : product.getEquipInfoList()) {
                    if (item.getProductUuid().equals(equipInfo.getEquipInfoPid())) {
                        mProduct = product;
                        break;
                    }
                }
            }

            //banlap: 获取当前系统语言;
            String localeLanguage = Locale.getDefault().getLanguage();

            int resId = 0;
            if (mProduct != null) {
                //banlap: 判断当前手机系统是否为en英语
                if(localeLanguage.equals("en")){
                    itemScanDeviceBinding.itemScanDeviceName.setText(mProduct.getEquiNickName());
                } else {
                    itemScanDeviceBinding.itemScanDeviceName.setText(mProduct.getEquiName());
                }

                String drawableName = "ic_" + item.getProductUuid() + (item.getConnectionStatus()==null?  "_off" : "_on");
                if(item.getConnectionStatus()!=null) {
                    drawableName = "ic_" + item.getProductUuid() + (item.getConnectionStatus().equals(ConnectionStatus.OFFLINE)?  "_off" : "_on");
                }

                resId = mContext.getResources().getIdentifier(drawableName, "drawable", mContext.getPackageName());

            }
            if (resId > 0) {
                itemScanDeviceBinding.itemScanDeviceIcon.setBackgroundResource(resId);
            } else {
                itemScanDeviceBinding.itemScanDeviceIcon.setBackgroundResource(R.drawable.ic_lamp_default_on);
            }

            if (item.isCheck()) {
                itemScanDeviceBinding.itemScanDeviceSelection.setVisibility(View.VISIBLE);
                itemScanDeviceBinding.itemScanDeviceSelection.setBackgroundResource(R.drawable.ic_select_yes);
            } else {
                itemScanDeviceBinding.itemScanDeviceSelection.setVisibility(View.VISIBLE);
                itemScanDeviceBinding.itemScanDeviceSelection.setBackgroundResource(R.drawable.ic_select_no_gray);

                itemScanDeviceBinding.itemScanDeviceSelection.setVisibility(item.getConnectionStatus() !=null? View.VISIBLE:View.GONE);
                if(item.getConnectionStatus()!=null) {
                    itemScanDeviceBinding.itemScanDeviceSelection.setVisibility(item.getConnectionStatus().equals(ConnectionStatus.OFFLINE)? View.GONE : View.VISIBLE);
                }
            }

            itemScanDeviceBinding.getRoot().setOnClickListener(v -> {
                //banlap：判断当前设备不在线时 不能在场景中设置调控参数
                if(item.getConnectionStatus() != null) {
                    if(!item.getConnectionStatus().equals(ConnectionStatus.OFFLINE)) {
                        allNotCheck();
                        item.setCheck(!item.isCheck());
                        notifyDataSetChanged();
                    }
                }
            });

        }

        private void allNotCheck() {
            for (Equip item : items) {
                item.setCheck(false);
            }
            notifyDataSetChanged();
        }

    }

    //banlap: 场景选择图标 icon
    private static class SceneIconViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView iconUni;
        public SceneIconViewHolder(@NonNull View itemView) {
            super(itemView);
            //icon = itemView.findViewById(R.id.iv_scene_icon);
            iconUni = itemView.findViewById(R.id.tv_scene_icon);
        }
    }

    private static class SceneIconAdapter extends RecyclerView.Adapter<SceneManagerActivity.SceneIconViewHolder> {

        private Context mContext;
        private final Integer[] data;
        private final Integer[] dataSelected;
        private final Integer[] dataUni;

        private int selectIndex = 0;

        public SceneIconAdapter(Context context, Integer[] arg0, Integer[] arg1, Integer[] arg2) {
            this.mContext = context;
            this.data = arg0;
            this.dataSelected = arg1;
            this.dataUni = arg2;
        }

        public void setSelectIndex(int selectIndex) {
            this.selectIndex = selectIndex;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SceneManagerActivity.SceneIconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_scene_new_icon, parent, false);
            return new SceneManagerActivity.SceneIconViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SceneManagerActivity.SceneIconViewHolder holder, int position) {
            Typeface iconView = Typeface.createFromAsset(mContext.getAssets(), "fonts/iconfont.ttf");
            holder.iconUni.setTypeface(iconView);
            //banlap: 传递当前场景的icon值到selectIndex
            selectIndex = mSelectIndex;
            holder.iconUni.setText(dataUni[position]);
            if(selectIndex == position) {
                //holder.icon.setBackgroundResource(dataSelected[position]);
                holder.iconUni.setTextColor(mContext.getResources().getColor(R.color.green));
            } else {
                //holder.icon.setBackgroundResource(data[position]);
                holder.iconUni.setTextColor(mContext.getResources().getColor(R.color.gray_99));
            }
            holder.itemView.setOnClickListener(v->{
                setSelectIndex(position);
                mSelectIndex = position;
            });
        }

        @Override
        public int getItemCount() {
            return data.length;
        }
    }

}