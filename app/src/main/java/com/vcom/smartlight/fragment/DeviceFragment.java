 package com.vcom.smartlight.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;
import com.telink.bluetooth.light.ConnectionStatus;
import com.vcom.smartlight.R;
import com.vcom.smartlight.adapter.ItemTouchHelperAdapter;
import com.vcom.smartlight.base.BaseBindingAdapter;
import com.vcom.smartlight.base.BaseMvvmFragment;
import com.vcom.smartlight.databinding.FragmentDeviceBinding;
import com.vcom.smartlight.databinding.ItemDeviceFragmentEquipBinding;
import com.vcom.smartlight.databinding.ItemDeviceFragmentSafeBoxBinding;
import com.vcom.smartlight.fvm.DeviceFVM;
import com.vcom.smartlight.model.Equip;
import com.vcom.smartlight.model.MessageEvent;
import com.vcom.smartlight.model.Product;
import com.vcom.smartlight.model.SafeBox;
import com.vcom.smartlight.server.VcomService;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.ui.DeviceSafeBoxActivity;
import com.vcom.smartlight.ui.DeviceSettingActivity;
import com.vcom.smartlight.ui.ElectricityStatisticsActivity;
import com.vcom.smartlight.ui.ScanByBleActivity;
import com.vcom.smartlight.ui.SecurityWarningActivity;
import com.vcom.smartlight.ui.SmartTerminalActivity;
import com.vcom.smartlight.ui.TimingControlActivity;
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
 * @Author Lzz
 * @Date 2020/10/27 18:44
 */
public class DeviceFragment extends BaseMvvmFragment<DeviceFVM, FragmentDeviceBinding> implements DeviceFVM.DeviceFvmCallBack {

    private DeviceAdapter adapter;
    private ItemTouchHelper.Callback callback;
    private final List<Equip> equipList = new ArrayList<>();

    private SafeBoxAdapter safeBoxAdapter;                         //安全电箱adapter
    private final List<SafeBox> safeBoxList = new ArrayList<>();   //安全电箱list
    private Boolean mIsShowSafeBoxFunction = false;
    private Boolean mIsShowSmartLightList = false;

    private final static int REQUEST_CODE_SAFE_BOX_ACTIVITY = 100;

    private int tagMesh = -1;

    private Equip mEquipTemp = new Equip();   //查询当前设备的保存参数

     @Override
    protected int getLayoutId() {
        return R.layout.fragment_device;
    }

    @Override
    protected void afterCreate() {

    }

    @Override
    protected void initViews() {
        getViewDataBind().setVm(getViewModel());
        getViewModel().setCallBack(this);
        EventBus.getDefault().register(this);

        adapter = new DeviceAdapter(getActivity());
        getViewDataBind().deviceFragmentRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        getViewDataBind().deviceFragmentRecycler.setAdapter(adapter);

        //banlap: list侧滑
        /*callback = new DefaultItemTouchHelperCallback(adapter, getActivity());
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(getViewDataBind().deviceFragmentRecycler);*/

        //banlap: 添加安全电箱adapter，显示列表
        safeBoxAdapter = new SafeBoxAdapter(getActivity());
        getViewDataBind().rvSafeBoxList.setLayoutManager(new LinearLayoutManager(getActivity()));
        getViewDataBind().rvSafeBoxList.setAdapter(safeBoxAdapter);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        getViewDataBind().rvSafeBoxList.setItemAnimator(itemAnimator);

        //banlap: 测试安全电箱 数据
        for(int i=0; i<5; i++){
            SafeBox safeBox = new SafeBox();
            safeBox.setDeviceId("1001"+i);
            safeBox.setDeviceName("智能电箱"+i);
            safeBox.setCity("GZ");
            safeBox.setShowDetail(0);
            safeBoxList.add(safeBox);
        }
        safeBoxAdapter.setItems(safeBoxList);
    }

    @Override
    protected void initDatum() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.msgCode) {
            case MessageEvent.equipReady:
                getViewModel().dataChanged();
                break;
            case MessageEvent.onlineStatusNotify:
                getViewModel().onOnlineStatusNotify(event.data);
                break;
            case MessageEvent.deviceStatusChanged:
                getViewModel().onDeviceStatusChanged(event.event);
                break;
        }
    }

    /*
    * banlap：点击智能照明后回调
    * */
    @Override
    public void selectSmartLight() {
        getViewDataBind().ivDeviceLightArrow.setVisibility(View.VISIBLE);
        getViewDataBind().ivSafeElectricBoxArrow.setVisibility(View.INVISIBLE);

        /*if(mIsShowSmartLightList){
            getViewDataBind().deviceFragmentRecycler.setVisibility(View.VISIBLE);
            getViewDataBind().clSmartLightControl.setVisibility(View.GONE);
        } else {
            getViewDataBind().deviceFragmentRecycler.setVisibility(View.GONE);
            getViewDataBind().clSmartLightControl.setVisibility(View.VISIBLE);
        }*/

        getViewDataBind().deviceFragmentRecycler.setVisibility(View.VISIBLE);
        getViewDataBind().clSmartLightControl.setVisibility(View.GONE);

        getViewDataBind().clSafeBoxControl.setVisibility(View.GONE);
        getViewDataBind().rvSafeBoxList.setVisibility(View.GONE);
    }

    /*
     * banlap：点击安全电箱后回调
     * */
    @Override
    public void selectSafeElectricBox() {
        getViewDataBind().ivDeviceLightArrow.setVisibility(View.INVISIBLE);
        getViewDataBind().ivSafeElectricBoxArrow.setVisibility(View.VISIBLE);

        getViewDataBind().deviceFragmentRecycler.setVisibility(View.GONE);
        getViewDataBind().clSmartLightControl.setVisibility(View.GONE);
        getViewDataBind().clSafeBoxControl.setVisibility(View.VISIBLE);
    }

    /*
     * banlap: 列表无设备时，点击添加设备
     * */
    @Override
    public void clickSmartLight(){
        Intent intent = new Intent(getActivity(), ScanByBleActivity.class);
        startActivity(intent);
    }

    /*
    * banlap: 点击添加安全电箱
    * */
    @Override
    public void clickSafeElectricBoxSetting() {
        Intent intent = new Intent(getActivity(), DeviceSafeBoxActivity.class);
        startActivity(intent);

        getViewDataBind().rvSafeBoxList.setVisibility(View.VISIBLE);
        getViewDataBind().clSafeBoxControl.setVisibility(View.GONE);
    }

    /*
    * banlap: 点击右上角添加设备
    * */
    @Override
    public void goScanDevice() {
        Intent intent = new Intent(getActivity(), ScanByBleActivity.class);
        startActivity(intent);
    }

    @Override
    public void notifyChanged(List<Equip> equips) {
        equipList.clear();
        equipList.addAll(equips);
        adapter.setItems(equipList);
        adapter.notifyDataSetChanged();

        //banlap: 判断显示列表还是显示添加设备
        if(equips.size()>0){
            getViewDataBind().clSmartLightControl.setVisibility(View.GONE);
            getViewDataBind().deviceFragmentRecycler.setVisibility(View.VISIBLE);
        } else {
            getViewDataBind().clSmartLightControl.setVisibility(View.VISIBLE);
            getViewDataBind().deviceFragmentRecycler.setVisibility(View.GONE);
        }
    }

     /*
      * banlap: 解绑设备 返回成功
      * */
    @Override
    public void removeEquipSuccess() {
        Toast.makeText(getActivity(),getString(R.string.toast_unbinding_success),Toast.LENGTH_SHORT).show();
        if (tagMesh == -1) {
            return;
        }
        byte kickCode = (byte) 0xE3;
        byte[] params = new byte[]{0x01};
        VcomService.getInstance().sendCommandNoResponse(kickCode, tagMesh, params);
    }

     /*
      * banlap: 解绑设备 返回失败
      * */
    @Override
    public void removeEquipFailure() {
        Toast.makeText(getActivity(),getString(R.string.toast_unbinding_error),Toast.LENGTH_SHORT).show();
    }

    public void showRemoveDialog(Equip equip) {
        tagMesh = -1;
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.dialog_title_2))
                .setMessage(getString(R.string.dialog_message_initialization_action))
                .setPositiveButton(getString(R.string.dialog_confirm), (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    getViewModel().removeEquip(equip.getUserEquipId());
                    tagMesh = Integer.parseInt(equip.getMeshAddress());
                })
                .setNegativeButton(getString(R.string.dialog_cancel), (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_switch_bg_gray_f9);
    }

    /*
    * banlap: 显示设备上一次的调控参数
    * */
    private void showParams(Equip equip) {
         getViewModel().selectRecord(equip, "0", equip.getUserEquipId());
    }

    @Override
    public void selectRecordSuccess(Equip equip, String data) {
        mEquipTemp = GsonUtil.getInstance().json2List(data, new TypeToken<Equip>() {}.getType());
        goDeviceSetting(equip);
    }

    @Override
    public void selectRecordFailure(Equip equip) {
        goDeviceSetting(equip);
    }

    public void goDeviceSetting(Equip equip) {
        Intent intent = new Intent(getActivity(), DeviceSettingActivity.class);
        intent.putExtra("equipId", equip.getUserEquipId());
        intent.putExtra("isSmartView", true);

        if(mEquipTemp!=null) {
            intent.putExtra("mode", mEquipTemp.getMode());
            intent.putExtra("brightness", Integer.parseInt(String.valueOf(mEquipTemp.getBrightness())));
            intent.putExtra("temp", Integer.parseInt(String.valueOf(mEquipTemp.getTemperature())));
            intent.putExtra("red", mEquipTemp.getRed());
            intent.putExtra("green", mEquipTemp.getGreen());
            intent.putExtra("blue", mEquipTemp.getBlue());
        }
        requireActivity().startActivity(intent);
    }

    private class DeviceAdapter extends BaseBindingAdapter<Equip, ItemDeviceFragmentEquipBinding>
            implements ItemTouchHelperAdapter {

        public DeviceAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        protected int getLayoutId(int layoutId) {
            return R.layout.item_device_fragment_equip;
        }

        @Override
        protected void onBindItem(ItemDeviceFragmentEquipBinding binding, Equip item, int i) {


            Product mProduct = null;
            for (Product product : VcomSingleton.getInstance().getUserProduct()) {
                for (Product.EquipInfo equipInfo : product.getEquipInfoList()) {
                    if (item.getProductUuid().equals(equipInfo.getEquipInfoPid())) {
                        mProduct = product;
                        break;
                    }
                }
            }

            if (mProduct == null) {
                return;
            }

            //banlap: 获取当前系统语言;
            String localeLanguage = Locale.getDefault().getLanguage();
            //banlap: 判断当前手机系统是否为en英语
            if(localeLanguage.equals("en")){
                binding.itemDeviceName.setText(mProduct.getEquiNickName());
            } else {
                binding.itemDeviceName.setText(mProduct.getEquiName());
            }

            int offLineResId = mContext.getResources().getIdentifier("ic_" + item.getProductUuid() + "_offline", "drawable", mContext.getPackageName());
            int offResId = mContext.getResources().getIdentifier("ic_" + item.getProductUuid() + "_off", "drawable", mContext.getPackageName());
            int onResId = mContext.getResources().getIdentifier("ic_" + item.getProductUuid() + "_on", "drawable", mContext.getPackageName());

            if (item.getConnectionStatus() == null) {
                binding.itemDeviceIcon.setBackgroundResource(offResId);
                binding.itemDeviceSetting.setVisibility(View.GONE);
                binding.itemDeviceStatus.setVisibility(View.VISIBLE);
                binding.itemDeviceStatus.setBackgroundResource(R.drawable.ic_hourglass_loading);
                binding.ivItemDeviceStatus.setBackgroundResource(R.drawable.shape_round_purple);
                binding.tvItemDeviceStatus.setText(getString(R.string.connecting));

            } else {
                binding.itemDeviceIcon.setBackgroundResource(onResId);
                switch (item.getConnectionStatus()) {
                    case ON:
                        //banlap: 更新 设置图标不显示
                        binding.itemDeviceSetting.setVisibility(View.GONE);
                        binding.itemDeviceStatus.setVisibility(View.VISIBLE);
                        binding.itemDeviceStatus.setBackgroundResource(R.drawable.ic_switch_weight_on);
                        binding.ivItemDeviceStatus.setBackgroundResource(R.drawable.shape_round_green);
                        binding.tvItemDeviceStatus.setText(getString(R.string.open));
                        break;
                    case OFF:
                        switch (item.getProductUuid()) {
                            case "24580"://2.8寸 智能面板
                            case "25092"://3.5寸 智能面板
                            case "25604"://4寸智能面板
                            case "20484"://电机窗帘
                            case "22532"://三位开关
                            case "21508"://触摸场景开关
                                //banlap: 更新 设置图标不显示
                                binding.itemDeviceIcon.setBackgroundResource(onResId);
                                binding.itemDeviceSetting.setVisibility(View.GONE);
                                binding.itemDeviceStatus.setVisibility(View.GONE);
                                binding.ivItemDeviceStatus.setBackgroundResource(R.drawable.shape_round_orange);
                                binding.tvItemDeviceStatus.setText(getString(R.string.connected));
                                break;
                            default:
                                //banlap: 更新 设置图标不显示
                                binding.itemDeviceIcon.setBackgroundResource(offResId);
                                binding.itemDeviceSetting.setVisibility(View.GONE);
                                binding.itemDeviceStatus.setVisibility(View.VISIBLE);
                                binding.itemDeviceStatus.setBackgroundResource(R.drawable.ic_switch_weight_off);
                                binding.ivItemDeviceStatus.setBackgroundResource(R.drawable.shape_round_orange);
                                binding.tvItemDeviceStatus.setText(getString(R.string.connected));
                                break;
                        }
                        break;
                    case OFFLINE:
                        binding.itemDeviceIcon.setBackgroundResource(offLineResId);
                        binding.itemDeviceSetting.setVisibility(View.GONE);
                        binding.itemDeviceStatus.setVisibility(View.VISIBLE);
                        binding.itemDeviceStatus.setBackgroundResource(R.drawable.ic_link_off_line);
                        binding.ivItemDeviceStatus.setBackgroundResource(R.drawable.shape_round_gray);
                        binding.tvItemDeviceStatus.setText(getString(R.string.disconnected));
                        break;
                }
            }

            int dstAddr = Integer.parseInt(item.getMeshAddress());

            //banlap: 更新 点击进入设备详情设置
            binding.getRoot().setOnClickListener(v -> {

                if (item.getConnectionStatus() == null || item.getConnectionStatus().equals(ConnectionStatus.OFFLINE)) {
                    return;
                }
                showParams(item);
            });

            //banlap: 更新 点击开启或关闭设备
            binding.rlDeviceStatus.setOnClickListener(v->{
                if (item.getConnectionStatus() == null) {
                    return;
                }
                //banlap: 开关灯需要上一次的保存参数
                String json = null;

                Map<String, Object> map = new HashMap<>();
                map.put("userId", VcomSingleton.getInstance().getLoginUser().getUserId());
                map.put("userEquipId", item.getUserEquipId());

                map.put("sceneId", 0);
                map.put("spaceId", 0);
                map.put("equipType", 0);
                map.put("mode", item.getMode());
                map.put("red", 0);
                map.put("blue", 0);
                map.put("green", 0);


                switch (item.getConnectionStatus()) {
                    case ON:
                        byte offCode = (byte) 0xD0;
                        VcomService.getInstance().sendCommandNoResponse(offCode, dstAddr,
                                new byte[]{0x00, 0x00, 0x00});
                        map.put("brightness", 0);
                        map.put("temperature", 2);
                        break;
                    case OFF:
                        byte onCode = (byte) 0xD0;
                        VcomService.getInstance().sendCommandNoResponse(onCode, dstAddr,
                                new byte[]{0x01, 0x00, 0x00});
                        map.put("brightness", 50);
                        map.put("temperature", 2);
                        break;
                }

                json = GsonUtil.getInstance().toJson(map);
                getViewModel().saveParams(json);
            });
            /*//banlap: 点击进入设备详情设置
            binding.itemDeviceSetting.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, DeviceSettingActivity.class);
                intent.putExtra("equipId", item.getUserEquipId());
                mContext.startActivity(intent);
            });*/

            //banlap: 点击图标解绑设备
            binding.itemDeviceRemove.setOnClickListener(v -> showRemoveDialog(item));

            //banlap: 侧滑解绑设备
            binding.flDelete.setOnClickListener(v->{
                //Toast.makeText(getActivity(), "点击删除", Toast.LENGTH_LONG).show();
                showRemoveDialog(item);
            });
        }


        @Override
        public void onItemDismiss(int position) {
           //notifyItemChanged(position);
        }

    }

    @Override
    public void saveParamsSuccess() {

    }

     @Override
     public void saveParamsFailure() {


     }
    /*
    * banlap: 安全电箱列表 Adapter
    * */
    private class SafeBoxAdapter extends BaseBindingAdapter<SafeBox, ItemDeviceFragmentSafeBoxBinding>{

        public SafeBoxAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        protected int getLayoutId(int layoutId) {
            return R.layout.item_device_fragment_safe_box;
        }

        @Override
        protected void onBindItem(ItemDeviceFragmentSafeBoxBinding itemSafeBoxBinding, SafeBox item, int i) {

            itemSafeBoxBinding.tvSafeElectricBoxName.setText(item.getDeviceName());

            itemSafeBoxBinding.tvSafeElectricBoxManage.setOnClickListener(v->{
                Intent intent = new Intent(getActivity(), DeviceSafeBoxActivity.class);
                startActivity(intent);
            });

            //banlap: 点击安全电箱item的展开或收起按钮
            itemSafeBoxBinding.tvShowOrHideFunction.setOnClickListener(v -> {
                if(itemSafeBoxBinding.tvShowOrHideFunction.getText().equals("收起")){
                    itemSafeBoxBinding.tvShowOrHideFunction.setText("展开");
                    itemSafeBoxBinding.clSafeBoxFunction.setVisibility(View.GONE);
                } else {
                    itemSafeBoxBinding.tvShowOrHideFunction.setText("收起");
                    itemSafeBoxBinding.clSafeBoxFunction.setVisibility(View.VISIBLE);
                }
                notifyItemChanged(i, getItemCount());
            });

            //banlap: 点击安全电箱item的智能终端
            itemSafeBoxBinding.clSafeBoxFunctionSmartTerminal.setOnClickListener(v->{
                Intent intent = new Intent(getActivity(), SmartTerminalActivity.class);
                startActivity(intent);
            });

            //banlap: 点击安全电箱item的定时控制
            itemSafeBoxBinding.clSafeBoxFunctionTimingControl.setOnClickListener(v->{
                Intent intent = new Intent(getActivity(), TimingControlActivity.class);
                startActivity(intent);
            });

            //banlap: 点击安全电箱item的电量统计
            itemSafeBoxBinding.clSafeBoxFunctionElectricityStatistics.setOnClickListener(v->{
                Intent intent = new Intent(getActivity(), ElectricityStatisticsActivity.class);
                startActivity(intent);
            });

            //banlap: 点击安全电箱item的安全自预警
            itemSafeBoxBinding.clSafeBoxFunctionSecurityWarning.setOnClickListener(v->{
                Intent intent = new Intent(getActivity(), SecurityWarningActivity.class);
                startActivity(intent);
            });
        }
    }


}
