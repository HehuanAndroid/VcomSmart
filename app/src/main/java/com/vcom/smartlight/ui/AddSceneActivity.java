package com.vcom.smartlight.ui;

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
import com.telink.bluetooth.light.ConnectionStatus;
import com.vcom.smartlight.R;
import com.vcom.smartlight.base.BaseBindingAdapter;
import com.vcom.smartlight.base.BaseMvvmActivity;
import com.vcom.smartlight.databinding.ActivityAddSceneBinding;
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
import com.vcom.smartlight.server.VcomService;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.uivm.AddSceneVM;
import com.vcom.smartlight.utils.GsonUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * @author Banlap on 2021/1/28
 */
public class AddSceneActivity extends BaseMvvmActivity<AddSceneVM, ActivityAddSceneBinding>
        implements AddSceneVM.AddSceneVMCallBack {

    private String mRegionId;               //新增场景的区域id
    private String mDefaultSceneName;       //新增场景默认名称
    private Scene mScene = null;            //新增场景信息
    private List<Scene> mCurrentSceneList;  //当前区域的场景列表（不包括当前新增场景）
    private Equip equipTemp = new Equip();  //新增设备后临时表 （保存并显示参数）


    private int tagMeshAddress = -1;
    private byte[] tagParam = null;

    private AlertDialog mAlertDialog;
    private AddSceneAdapter mAdapter;
    private final List<Equip> mSceneEquipsList = new ArrayList<>();  //新增场景下添加的设备列表
    private final List<Equip> mEquipList = VcomSingleton.getInstance().getUserEquips();
    private final List<Region> mRegionList = VcomSingleton.getInstance().getUserRegion();

    public static int mSelectIndex = 0;    //选择场景图标位置, 默认为 0

    private boolean isAcceptAdd = false;  //修复在多个新增场景中点击增加设备，会出现sceneId NULL报错

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_scene;
    }

    @Override
    protected void initViews() {
        getViewDataBind().setVm(getViewModel());
        getViewModel().setCallBack(this);
        //banlap: 注册EventBus
        EventBus.getDefault().register(this);

        mAdapter = new AddSceneAdapter(this);
        mAdapter.setItems(mSceneEquipsList);
        getViewDataBind().rvAddSceneDevice.setLayoutManager(new LinearLayoutManager(this));
        getViewDataBind().rvAddSceneDevice.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        //selectIconAdapter = new SelectIconAdapter(this, mapList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initDatum() {
        //banlap: 初始化变量 场景图标选择
        mSelectIndex = 0;
        if (getIntent().getExtras() == null) {
            finish();
        }

        mDefaultSceneName = getIntent().getStringExtra("NewSceneDefaultName");
        getViewDataBind().etAddSceneName.setText(mDefaultSceneName);

        //banlap: 获取当前区域id
        mRegionId = getIntent().getStringExtra("CurrentRegionId");
        if(mRegionList.size()>0){
           for (Region region : mRegionList){
               if(region.getSpaceId().equals(mRegionId)) {
                   //banlap: 获取当前新增场景的list
                   mCurrentSceneList = region.getSceneList();
               }
           }

        }
        //banlap: 添加设备时设置加载gif
        getViewDataBind().prLoading2.setVisibility(View.VISIBLE);
        getViewDataBind().ivAddIcon.setVisibility(View.GONE);
        getViewDataBind().tvAddScene.setTextColor(getResources().getColor(R.color.gray_99));
        isAcceptAdd = false;
        //Toast.makeText(this, "cRegionId: " + mRegionId, Toast.LENGTH_SHORT).show();
    }

    /*
    * banlap: EventBus 事件订阅响应方法
    * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.msgCode) {
            case MessageEvent.regionReady:
                //banlap: 刷新当前新增场景的设备list
                if(mScene!=null){
                    for (Region region : VcomSingleton.getInstance().getUserRegion()) {
                        for (Scene scene : region.getSceneList()) {
                            if (scene.getSceneId().equals(mScene.getSceneId())) {
                                mSceneEquipsList.clear();
                                mSceneEquipsList.addAll(scene.getUserEquipList());
                                mAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                }

                //banlap: 当收到regionReady通知后可在场景中添加设备
                getViewDataBind().prLoading2.setVisibility(View.GONE);
                getViewDataBind().ivAddIcon.setVisibility(View.VISIBLE);
                getViewDataBind().tvAddScene.setTextColor(getResources().getColor(R.color.green));
                isAcceptAdd = true;
                break;

            case MessageEvent.switchFragment:
                Intent goScanIntent = new Intent(this, ScanByBleActivity.class);
                startActivity(goScanIntent);
                break;
        }

    }


    /*
    * banlap: 点击添加设备
    * */
    @Override
    public void viewGoAddNewEquip() {

        if(isAcceptAdd) {
            mAlertDialog = null;
            if(mScene == null){
                findNewScene();
            }

            //banlap: 判断当前账号是否有绑定的设备
            if(mEquipList.size()>0){

                List<Equip> newEquipList = new ArrayList<>();

                for(Equip equip : mEquipList){
                    //banlap: 场景中添加设备列表时 除去21508 智能场景开关
                    if(!equip.getProductUuid().equals("21508")) {
                        equip.setCheck(false);
                        newEquipList.add(equip);
                    }
                }

                //banlap: 弹框 显示当前账号绑定的列表
                DialogSceneAddEquipBinding addEquipBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_scene_add_equip,
                        null, false);
                mAlertDialog = new AlertDialog.Builder(this)
                        .setView(addEquipBinding.getRoot())
                        .create();
                Objects.requireNonNull(mAlertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_switch_bg_gray_f9);

                AddSceneActivity.ScanAddEquipAdapter deviceAdapter = new AddSceneActivity.ScanAddEquipAdapter(this);
                addEquipBinding.dialogSceneAddEquipRecycler.setLayoutManager(new LinearLayoutManager(this));
                addEquipBinding.dialogSceneAddEquipRecycler.setAdapter(deviceAdapter);
                deviceAdapter.setItems(newEquipList);
                deviceAdapter.notifyDataSetChanged();

                addEquipBinding.tvCancel.setOnClickListener(v -> mAlertDialog.dismiss());
                addEquipBinding.dialogSceneAddEquipCancel.setOnClickListener(v -> mAlertDialog.dismiss());
                addEquipBinding.dialogSceneAddEquipCancel.setVisibility(View.GONE);
                //banlap: 选择列表中的设备
                addEquipBinding.dialogSceneAddEquipNext.setOnClickListener(v -> {
                    for(Equip equip : mEquipList) {
                        if(equip.isCheck()) {
                            getParamsForActivity(equip);
                            mAlertDialog.dismiss();
                            break;
                        }
                    }
                });

            } else {
                DialogDeviceListNullBinding deviceListNullBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_device_list_null,
                        null, false);
                mAlertDialog = new AlertDialog.Builder(this)
                        .setView(deviceListNullBinding.getRoot())
                        .create();

                deviceListNullBinding.ivDialogCancel.setOnClickListener(v-> mAlertDialog.dismiss());
                deviceListNullBinding.dialogMessageCancel.setOnClickListener(v-> mAlertDialog.dismiss());
                deviceListNullBinding.dialogMessageConform.setOnClickListener(v-> {
                    getViewModel().goAddEquip();
                    mAlertDialog.dismiss();
                    finish();
                });

                   /* .setTitle(getString(R.string.dialog_title))
                    .setMessage(getString(R.string.dialog_message_add_device_into_empty_list))
                    .setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(getString(R.string.dialog_confirm),((dialog, which) -> {
                        EventBus.getDefault().post(new MessageEvent(MessageEvent.switchFragment, "1"));
                        dialog.dismiss();
                        finish();
                    }))
                    .create();*/
            }
            mAlertDialog.show();
            Objects.requireNonNull(mAlertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_switch_bg_gray_f9);

        }
    }


    /*
    * banlap: 不保存返回列表
    * */
    @Override
    public void viewBack() {
        /*
        * banlap: 返回时 删除当前创建的空场景
        * 需要找到当前创建空场景的sceneId
        * 需要查询当前区域才能查询到这个区域下的所有场景列表
        * */
       /* mScene = null;
        findNewScene();
        //banlap: 执行删除场景
        if(mScene!=null){
            getViewModel().deleteScene(mScene);
        }*/
        finish();
    }

    /*
    * banlap: 新增设备到该场景后
    * */
    @Override
    public void viewAddSceneEquipSuccess() {
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

    @Override
    public void viewAddSceneEquipFailure() {
        Toast.makeText(this, getString(R.string.toast_add_error),Toast.LENGTH_SHORT).show();
    }
    /*
     * banlap: 选择场景图标
     * */
    @Override
    public void viewSelectSceneIcon() {
        DialogSelectIconBinding selectIconBinding = DataBindingUtil.inflate(LayoutInflater.from(this),
                R.layout.dialog_select_icon, null, false);
        mAlertDialog = null;
        mAlertDialog = new AlertDialog.Builder(this)
                .setView(selectIconBinding.getRoot())
                .create();

        Integer[] icons = {R.drawable.ic_scene_0, R.drawable.ic_scene_1, R.drawable.ic_scene_2, R.drawable.ic_scene_3};
        Integer[] iconSelected = {R.drawable.ic_scene_selected_0, R.drawable.ic_scene_selected_1, R.drawable.ic_scene_selected_2, R.drawable.ic_scene_selected_3};

        Integer[] iconsUnicode = {R.string.icon_scene_0, R.string.icon_scene_1, R.string.icon_scene_2, R.string.icon_scene_3};

        SceneIconAdapter sceneIconAdapter = new SceneIconAdapter(this, icons, iconSelected, iconsUnicode);
        selectIconBinding.rvSelectIconList.setLayoutManager(new GridLayoutManager(this, 2));
        selectIconBinding.rvSelectIconList.setAdapter(sceneIconAdapter);
        selectIconBinding.btSelectIconCancel.setOnClickListener(v->{mAlertDialog.dismiss();});
        sceneIconAdapter.notifyDataSetChanged();

        //banlap: 点击确认选择该场景图标
        selectIconBinding.btSelectIconCommit.setOnClickListener(v->{
            mAlertDialog.dismiss();
            getViewDataBind().ivSelectSceneIcon.setBackgroundResource(icons[mSelectIndex]);
        });

        mAlertDialog.show();
        Objects.requireNonNull(mAlertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_switch_bg_gray_f9);

    }
    /*
     * banlap: 选择场景定时
     * */
    @Override
    public void viewSelectSceneTiming() {

    }
    /*
     * banlap: 更新并保存场景
     * */
    @Override
    public void viewGoUpdateScene() {
        if(mScene == null){
            findNewScene();
        }

        String newSceneName = getViewDataBind().etAddSceneName.getText().toString();
        if (TextUtils.isEmpty(newSceneName)) {
            return;
        }

        if(mSceneEquipsList.size() == 0) {
            Toast.makeText(this, getString(R.string.toast_add_device),Toast.LENGTH_SHORT).show();
            return;
        }

        mScene.setSceneName(newSceneName);
        //banlap: 更新场景图标
        mScene.setSceneImg(String.valueOf(mSelectIndex));
        getViewModel().updateScene(mScene);
    }

    /*
     * banlap: 更新场景成功后
     * */
    @Override
    public void viewUpdateSceneSuccess() {
        Toast.makeText(this, getString(R.string.toast_update_success),Toast.LENGTH_SHORT).show();
        finish();
    }
    /*
     * banlap: 更新场景失败后
     * */
    @Override
    public void viewUpdateSceneFailure() {
        Toast.makeText(this, getString(R.string.toast_update_error),Toast.LENGTH_SHORT).show();
    }

    /*
    * banlap: 删除场景成功后执行
    * */
    @Override
    public void viewDeleteSceneSuccess(Scene scene) {
        Toast.makeText(this, getString(R.string.toast_delete_success),Toast.LENGTH_SHORT).show();

        if (tagMeshAddress == -1) {
            return;
        }
        byte opcode = (byte) 0xEE;
        byte[] param = {0x00, (byte) Integer.parseInt(mScene.getSceneMeshId())};
        VcomService.getInstance().sendCommandNoResponse(opcode, tagMeshAddress, param);

       /* byte opcode = (byte) 0xEE;
        byte[] param = {0x00, (byte) Integer.parseInt(scene.getSceneMeshId())};
        VcomService.getInstance().sendCommandNoResponse(opcode, 0XFF, param);*/
    }

    @Override
    public void viewDeleteSceneFailure() {
        Toast.makeText(this, getString(R.string.toast_delete_error),Toast.LENGTH_SHORT).show();
    }

    /*
    * banlap: 查询场景id
    * */
    public void findNewScene(){
        List<Region> newRegionList = VcomSingleton.getInstance().getUserRegion();
        List<Scene> newSceneList = new ArrayList<>();
        if(newRegionList.size() >0){
            for (Region region : newRegionList) {
                if (region.getSpaceId().equals(mRegionId)) {
                    newSceneList = region.getSceneList();
                }
            }
            //banlap: 场景列表数据
            if(newSceneList.size()>0) {
                if(mCurrentSceneList.size()>0){
                    for(Scene newScene : newSceneList) {
                        //banlap: 标记 是否查询到相同场景id
                        boolean isFind = false;
                        for(Scene scene : mCurrentSceneList) {
                            if(newScene.getSceneId().equals(scene.getSceneId())){
                                isFind = true;
                                break;
                            }

                        }
                        //banlap: 没查询到则为新增的场景
                        if(!isFind){
                            mScene = newScene;
                        }
                    }
                } else {
                    mScene = newSceneList.get(0);
                }
            }

        }
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
    /*
    * banlap: 添加设备到场景里面
    * */
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
    * banlap: 删除场景里面的设备
    * */
    private void deleteSceneEquip(Equip equip) {
        tagMeshAddress = -1;
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_title))
                .setMessage(getString(R.string.dialog_message_delete_scene_device))
                .setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dialog.dismiss())
                .setPositiveButton(getString(R.string.dialog_confirm), (dialog, which) -> {
                    tagMeshAddress = Integer.parseInt(equip.getMeshAddress());
                    getViewModel().deleteSceneEquip(mScene, VcomSingleton.getInstance().getLoginUser().getUserId(), mRegionId, mScene.getSceneId(), equip.getUserEquipId());
                    dialog.dismiss();
                })
                .create();
        alertDialog.show();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_switch_bg_gray_f9);
    }



    /*
    * banlap: 新增场景的设备列表
    * */
    private class AddSceneAdapter extends BaseBindingAdapter<Equip, ItemSceneManagerEquipBinding> {

        public AddSceneAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        protected int getLayoutId(int layoutId) {
            return R.layout.item_scene_manager_equip;
        }

        @Override
        protected void onBindItem(ItemSceneManagerEquipBinding itemSceneManagerEquipBinding, Equip item, int i) {
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
                    itemSceneManagerEquipBinding.itemSceneManagerName.setText(mProduct.getEquiNickName());
                } else {
                    itemSceneManagerEquipBinding.itemSceneManagerName.setText(mProduct.getEquiName());
                }
                resId = mContext.getResources().getIdentifier("ic_" + item.getProductUuid() + "_on", "drawable", mContext.getPackageName());
            }
            if (resId > 0) {
                itemSceneManagerEquipBinding.itemSceneManagerIcon.setBackgroundResource(resId);
            } else {
                itemSceneManagerEquipBinding.itemSceneManagerIcon.setBackgroundResource(R.drawable.ic_lamp_default_on);
            }

            itemSceneManagerEquipBinding.itemDeviceSetting.setOnClickListener(v -> showParams(item));
            //itemSceneManagerEquipBinding.itemDeviceSetting.setOnClickListener(v -> getParamsForActivity(item));
            //itemSceneManagerEquipBinding.itemDeviceRemove.setOnClickListener(v -> deleteSceneEquip(item));
            itemSceneManagerEquipBinding.flDelete.setOnClickListener(v -> deleteSceneEquip(item));
        }
    }

    //banlap: 扫描当前用户绑定的设备
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
            for(Product product : VcomSingleton.getInstance().getUserProduct()) {
                for(Product.EquipInfo equipInfo : product.getEquipInfoList()) {
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
                    if (!item.getConnectionStatus().equals(ConnectionStatus.OFFLINE)) {
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

    private static class SceneIconAdapter extends RecyclerView.Adapter<SceneIconViewHolder> {

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
        public SceneIconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_scene_new_icon, parent, false);
            return new SceneIconViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SceneIconViewHolder holder, int position) {
            Typeface iconView = Typeface.createFromAsset(mContext.getAssets(), "fonts/iconfont.ttf");
            holder.iconUni.setTypeface(iconView);
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
