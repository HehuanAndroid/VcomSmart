package com.vcom.smartlight.ui;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.telink.bluetooth.event.DeviceEvent;
import com.telink.bluetooth.event.LeScanEvent;
import com.telink.bluetooth.event.NotificationEvent;
import com.telink.bluetooth.light.DeviceInfo;
import com.telink.util.Event;
import com.telink.util.EventListener;
import com.vcom.smartlight.R;
import com.vcom.smartlight.VcomApp;
import com.vcom.smartlight.base.BaseBindingAdapter;
import com.vcom.smartlight.base.BaseMvvmActivity;
import com.vcom.smartlight.databinding.ActivityScanByBleBinding;
import com.vcom.smartlight.databinding.ItemScanDeviceBinding;
import com.vcom.smartlight.model.MessageEvent;
import com.vcom.smartlight.model.Product;
import com.vcom.smartlight.server.VcomService;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.uivm.ScanByBleVM;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScanByBleActivity extends BaseMvvmActivity<ScanByBleVM, ActivityScanByBleBinding>
        implements EventListener<String>, ScanByBleVM.ScanByBleCallBack {

    private ScanListAdapter adapter;
    private final List<DeviceInfo> deviceInfos = new ArrayList<>();
    private final List<DeviceInfo> selectDevices = new ArrayList<>();

    private boolean isClick = false;        //绑定设备列表: 点击全选按钮 和 单独选择设备 区分
    private boolean clickAll = false;       //全选设备绑定 标记

    @Override
    protected int getLayoutId() {
        return R.layout.activity_scan_by_ble;
    }

    @Override
    protected void initViews() {
        getViewDataBind().setVm(getViewModel());
        getViewModel().setCallBack(this);

        adapter = new ScanListAdapter(this);
        adapter.setItems(deviceInfos);
        getViewDataBind().scanViewRecycler.setAdapter(adapter);
        getViewDataBind().scanViewRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void initDatum() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        VcomApp.getApp().addEventListener(LeScanEvent.LE_SCAN, this);
        VcomApp.getApp().addEventListener(LeScanEvent.LE_SCAN_TIMEOUT, this);
        VcomApp.getApp().addEventListener(LeScanEvent.LE_SCAN_COMPLETED, this);

        VcomApp.getApp().addEventListener(DeviceEvent.STATUS_CHANGED, this);
        VcomApp.getApp().addEventListener(NotificationEvent.UPDATE_MESH_COMPLETE, this);
        VcomService.getInstance().idleMode(false);
        getViewModel().startScan();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getViewModel().stopScan();
        EventBus.getDefault().post(new MessageEvent(MessageEvent.refreshEquip));
        VcomApp.getApp().removeEventListener(this);
    }

    @Override
    public void performed(Event<String> event) {
        getViewModel().performed(event);
    }

    @Override
    public void viewBack() {
        finish();
    }

    @Override
    public void viewRefreshList(boolean hideLoading, List<DeviceInfo> scanList) {
        if (hideLoading) {
            getViewDataBind().scanViewLoading.setVisibility(View.GONE);
        }

        deviceInfos.clear();
        deviceInfos.addAll(scanList);
        adapter.notifyDataSetChanged();
        if(scanList.size()>0){
            getViewDataBind().clScanBleNoData.setVisibility(View.GONE);
            getViewDataBind().scanViewRecycler.setVisibility(View.VISIBLE);
        } else {
            getViewDataBind().clScanBleNoData.setVisibility(View.VISIBLE);
            getViewDataBind().scanViewRecycler.setVisibility(View.GONE);
        }
    }

    @Override
    public void viewIsScanning(boolean isScan) {
        if (isScan) {
            getViewDataBind().scanViewProgress.setVisibility(View.VISIBLE);
            getViewDataBind().scanViewScan.setVisibility(View.GONE);
        } else {
            getViewDataBind().scanViewProgress.setVisibility(View.GONE);
            getViewDataBind().scanViewScan.setVisibility(View.VISIBLE);
        }
        getViewDataBind().llCheckBox.setOnClickListener(v->{
            if(!isScan) {
                isClick = true;
                clickAll = !clickAll;
                if(clickAll){
                    getViewDataBind().ivCheckBox.setBackgroundResource(R.drawable.ic_check_box_click);
                    selectDevices.clear();
                    selectDevices.addAll(deviceInfos);
                } else {
                    getViewDataBind().ivCheckBox.setBackgroundResource(R.drawable.ic_check_box_no_click);
                    selectDevices.clear();
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void viewClickAll() {

    }

    @Override
    public void viewBindEquip() {
        if (selectDevices.size() == 0) {
            return;
        }
        getViewDataBind().scanViewLoading.setVisibility(View.VISIBLE);
        getViewModel().bindEquips(selectDevices);

    }

    private class ScanListAdapter extends BaseBindingAdapter<DeviceInfo, ItemScanDeviceBinding> {

        public ScanListAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        protected int getLayoutId(int layoutId) {
            return R.layout.item_scan_device;
        }

        @Override
        protected void onBindItem(ItemScanDeviceBinding itemScanDeviceBinding, DeviceInfo item, int i) {

            Product mProduct = null;
            for (Product product : VcomSingleton.getInstance().getUserProduct()) {
                for (Product.EquipInfo equipInfo : product.getEquipInfoList()) {
                    if (item.productUUID == Integer.parseInt(equipInfo.getEquipInfoPid())) {
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
                resId = mContext.getResources().getIdentifier("ic_" + item.productUUID + "_on", "drawable", mContext.getPackageName());
            }
            if (resId > 0) {
                itemScanDeviceBinding.itemScanDeviceIcon.setBackgroundResource(resId);
            } else {
                itemScanDeviceBinding.itemScanDeviceIcon.setBackgroundResource(R.drawable.ic_lamp_default_on);
            }
            //banlap: 判断是否点击全选按钮，是则使用clickAll判断，否则使用点击设备选项判断
            if(isClick){
                item.isCheck = clickAll;
            }
            itemScanDeviceBinding.itemScanDeviceSelection.setBackgroundResource(item.isCheck ? R.drawable.ic_select_yes : R.drawable.ic_select_no);

            itemScanDeviceBinding.getRoot().setOnClickListener(v -> {
                if (item.isCheck) {
                    item.isCheck = false;
                    selectDevices.remove(item);
                } else {
                    item.isCheck = true;
                    selectDevices.add(item);
                }
                //banlap: 使用点击设备选项判断是否勾选设备
                isClick = false;
                notifyDataSetChanged();
            });

        }
    }
}