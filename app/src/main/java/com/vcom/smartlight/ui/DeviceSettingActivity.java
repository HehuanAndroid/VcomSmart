package com.vcom.smartlight.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.vcom.smartlight.R;
import com.vcom.smartlight.base.BaseBindingAdapter;
import com.vcom.smartlight.base.BaseMvvmActivity;
import com.vcom.smartlight.databinding.ActivityDeviceSettingBinding;
import com.vcom.smartlight.databinding.DialogSelectScenesBinding;
import com.vcom.smartlight.databinding.ItemSelectSceneBinding;
import com.vcom.smartlight.databinding.ItemTouchSwitchBinding;
import com.vcom.smartlight.model.Equip;
import com.vcom.smartlight.model.Product;
import com.vcom.smartlight.model.Region;
import com.vcom.smartlight.model.Scene;
import com.vcom.smartlight.model.TouchSwitch;
import com.vcom.smartlight.server.VcomService;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.uivm.DeviceSettingVM;
import com.vcom.smartlight.utils.GsonUtil;
import com.vcom.smartlight.utils.Util;
import com.vcom.smartlight.widget.SlideColorPickerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeviceSettingActivity extends BaseMvvmActivity<DeviceSettingVM, ActivityDeviceSettingBinding>
        implements DeviceSettingVM.DeviceSettingVmCallback {

    private Scene mScene;
    private Equip mEquip;
    private Product mProduct;

    private List<TouchSwitch> touchSceneList;
    private TouchSwitchAdapter adapter;
    private AlertDialog selectSceneDialog;

    private final AtomicBoolean isGetParams = new AtomicBoolean(false);

    private boolean isOff = false;
    private boolean isSmartView = false;   //标记：在主页或智能界面进入设备调控界面

    private int mode = 0;
    private byte red = 0;
    private byte green = 0;
    private byte blue = 0;
    private byte brightness = 50;
    private byte temperature = 0;

    private byte operate = 0;

    private char[] status = null;

    private byte cmd = 0;
    private int devType = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_device_setting;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initViews() {
        getViewDataBind().setVm(getViewModel());
        getViewModel().setCallback(this);

        if (!isGetParams.get()) {
            getViewDataBind().deviceSettingSave.setVisibility(View.GONE);
            //getViewDataBind().deviceSettingIncludeLamp.deviceIncludeTitle.setVisibility(View.GONE);
        }
        getViewDataBind().deviceSettingIncludeLamp.deviceIncludeTitle.setVisibility(View.GONE);



        //banlap: 获取设备MeshAddress值
        int addr = Integer.parseInt(mEquip.getMeshAddress());

        //banlap: 电机窗帘设置
        if(mEquip.getProductUuid().equals("20484")){
            devType = 2;  //电机窗帘
            cmd = 0x01;
            getViewDataBind().deviceSettingIncludeCurtainSwitchView.setVisibility(View.VISIBLE);

            //banlap: 电机窗帘打开
            getViewDataBind().deviceSettingIncludeCurtainSwitch.clCurtainOpen.setOnClickListener(v->{
                getViewDataBind().deviceSettingIncludeCurtainSwitch.ivCurtainStatus.setImageResource(R.mipmap.ic_curtain_open_1);
                operate = (byte) 0x00;
                //banlap: 场景中设置选择功能，当前不执行该功能
                if (isGetParams.get()) {
                    getViewDataBind().deviceSettingIncludeCurtainSwitch.clCurtainOpen.setBackground(getDrawable(R.drawable.shape_button_selected_green));
                    getViewDataBind().deviceSettingIncludeCurtainSwitch.clCurtainStop.setBackground(getDrawable(R.drawable.selector_button_green));
                    getViewDataBind().deviceSettingIncludeCurtainSwitch.clCurtainClose.setBackground(getDrawable(R.drawable.selector_button_green));
                    return;
                }
                byte opcode = (byte) 0xF3;
                byte[] params = new byte[]{0x00, operate};
                VcomService.getInstance().sendCommandNoResponse(opcode, addr, params);
            });
            //banlap: 电机窗帘停止
            getViewDataBind().deviceSettingIncludeCurtainSwitch.clCurtainStop.setOnClickListener(v->{
                operate = (byte) 0x02;
                //banlap: 场景中设置选择功能，当前不执行该功能
                if (isGetParams.get()) {
                    getViewDataBind().deviceSettingIncludeCurtainSwitch.clCurtainOpen.setBackground(getDrawable(R.drawable.selector_button_green));
                    getViewDataBind().deviceSettingIncludeCurtainSwitch.clCurtainStop.setBackground(getDrawable(R.drawable.shape_button_selected_green));
                    getViewDataBind().deviceSettingIncludeCurtainSwitch.clCurtainClose.setBackground(getDrawable(R.drawable.selector_button_green));
                    return;
                }
                byte opcode = (byte) 0xF3;
                byte[] params = new byte[]{0x00, operate};
                VcomService.getInstance().sendCommandNoResponse(opcode, addr, params);
            });
            //banlap: 电机窗帘关闭
            getViewDataBind().deviceSettingIncludeCurtainSwitch.clCurtainClose.setOnClickListener(v->{
                getViewDataBind().deviceSettingIncludeCurtainSwitch.ivCurtainStatus.setImageResource(R.mipmap.ic_curtain_close_1);
                operate = (byte) 0x01;
                //banlap: 场景中设置选择功能，当前不执行该功能
                if (isGetParams.get()) {
                    getViewDataBind().deviceSettingIncludeCurtainSwitch.clCurtainOpen.setBackground(getDrawable(R.drawable.selector_button_green));
                    getViewDataBind().deviceSettingIncludeCurtainSwitch.clCurtainStop.setBackground(getDrawable(R.drawable.selector_button_green));
                    getViewDataBind().deviceSettingIncludeCurtainSwitch.clCurtainClose.setBackground(getDrawable(R.drawable.shape_button_selected_green));
                    return;
                }
                byte opcode = (byte) 0xF3;
                byte[] params = new byte[]{0x00, operate};
                VcomService.getInstance().sendCommandNoResponse(opcode, addr, params);
            });
            return;
        }
        //banlap: 三位开关设置
        if (mEquip.getProductUuid().equals("22532")) {
            devType = 1;  //三位开关设置
            cmd = 0x02;
            getViewDataBind().deviceSettingIncludeThreeSwitchView.setVisibility(View.VISIBLE);
            //getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchOneIcon.setBackgroundResource(R.drawable.ic_power_on);
            //getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchTwoIcon.setBackgroundResource(R.drawable.ic_power_on);
            //getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchThreeIcon.setBackgroundResource(R.drawable.ic_power_on);
            if (!isGetParams.get()) {
                if (mEquip.getSwitchStatus().charAt(0) == '1') {
                    //getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchOneIcon.setBackgroundResource(R.drawable.ic_power_on_w);
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchOne.setTag("1");
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchOne.setBackgroundResource(R.drawable.ic_three_switch_on);
                } else {
                    //getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchOneIcon.setBackgroundResource(R.drawable.ic_power_on);
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchOne.setTag("0");
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchOne.setBackgroundResource(R.drawable.ic_three_switch_off);
                }

                if (mEquip.getSwitchStatus().charAt(1) == '1') {
                    //getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchTwoIcon.setBackgroundResource(R.drawable.ic_power_on_w);
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchTwo.setTag("1");
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchTwo.setBackgroundResource(R.drawable.ic_three_switch_on);
                } else {
                    //getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchTwoIcon.setBackgroundResource(R.drawable.ic_power_on);
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchTwo.setTag("0");
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchTwo.setBackgroundResource(R.drawable.ic_three_switch_off);
                }

                if (mEquip.getSwitchStatus().charAt(2) == '1') {
                    //getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchThreeIcon.setBackgroundResource(R.drawable.ic_power_on_w);
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchThree.setTag("1");
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchThree.setBackgroundResource(R.drawable.ic_three_switch_on);
                } else {
                    //getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchThreeIcon.setBackgroundResource(R.drawable.ic_power_on);
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchThree.setTag("0");
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchThree.setBackgroundResource(R.drawable.ic_three_switch_off);
                }

                status = mEquip.getSwitchStatus().toCharArray();
            } else {
                status = new char[]{'0', '0', '0'};
            }
            getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchOne.setOnClickListener(v -> {
                //banlap: 当保存到场景时，三位开关控制位置做调整，否则保存参数会出现问题
                if (isGetParams.get()) {
                    switchOption(mEquip, 2);
                } else {
                    switchOption(mEquip, 0);
                }

                if ("0".equals(getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchOne.getTag().toString())) {
                    //getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchOneIcon.setBackgroundResource(R.drawable.ic_power_on_w);
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchOne.setTag("1");
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchOne.setBackgroundResource(R.drawable.ic_three_switch_on);
                } else {
                    //getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchOneIcon.setBackgroundResource(R.drawable.ic_power_on);
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchOne.setTag("0");
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchOne.setBackgroundResource(R.drawable.ic_three_switch_off);
                }
            });

            getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchTwo.setOnClickListener(v -> {
                switchOption(mEquip, 1);

                if ("0".equals(getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchTwo.getTag().toString())) {
                    //getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchTwoIcon.setBackgroundResource(R.drawable.ic_power_on_w);
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchTwo.setTag("1");
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchTwo.setBackgroundResource(R.drawable.ic_three_switch_on);
                } else {
                    //getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchTwoIcon.setBackgroundResource(R.drawable.ic_power_on);
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchTwo.setTag("0");
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchTwo.setBackgroundResource(R.drawable.ic_three_switch_off);
                }
            });

            getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchThree.setOnClickListener(v -> {
                //banlap: 当保存到场景时，三位开关控制位置做调整，否则保存参数会出现问题
                if (isGetParams.get()) {
                    switchOption(mEquip, 0);
                } else {
                    switchOption(mEquip, 2);
                }

                if ("0".equals(getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchThree.getTag().toString())) {
                    //getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchThreeIcon.setBackgroundResource(R.drawable.ic_power_on_w);
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchThree.setTag("1");
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchThree.setBackgroundResource(R.drawable.ic_three_switch_on);
                } else {
                    //getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchThreeIcon.setBackgroundResource(R.drawable.ic_power_on);
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchThree.setTag("0");
                    getViewDataBind().deviceSettingIncludeThreeSwitch.deviceSettingIncludeThreeSwitchThree.setBackgroundResource(R.drawable.ic_three_switch_off);
                }
            });
            return;
        }
        //banlap: 触摸场景开关
        if (mEquip.getProductUuid().equals("21508")) {
            getViewDataBind().deviceSettingIncludeTouchSwitchView.setVisibility(View.VISIBLE);
            touchSceneList = new ArrayList<>();
            adapter = new TouchSwitchAdapter(this);
            adapter.setItems(touchSceneList);
            getViewDataBind().deviceSettingIncludeTouchSwitch.deviceSettingTouchSwitchRecycler.setLayoutManager(new GridLayoutManager(this, 2));
            getViewDataBind().deviceSettingIncludeTouchSwitch.deviceSettingTouchSwitchRecycler.setAdapter(adapter);
            getViewModel().getTouchSwitchSceneList(mEquip.getUserEquipId());
            return;
        }

        if (mEquip.getProductUuid().equals("273")) {
            cmd = 0x03;
        }

        if (mEquip.getProductUuid().equals("258")) {
            getViewDataBind().deviceSettingIncludeLamp.deviceIncludeTitle.setVisibility(View.GONE);
        }

        //banlap: 当为 0x901（2305）杀菌教室灯时，隐藏色温调控
        if(mEquip.getProductUuid().equals("2305")) {
            getViewDataBind().deviceSettingIncludeLamp.deviceIncludeViewTemperature.setVisibility(View.GONE);
        }

        getViewDataBind().deviceSettingIncludeLampView.setVisibility(View.VISIBLE);
        getViewDataBind().deviceSettingIncludeLamp.deviceSettingIncludeLampSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    isOff = isChecked;
                    getViewDataBind().deviceSettingIncludeLamp.deviceSettingIncludeLampViewSwitch.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                }
        );


        //banlap：获取场景下保存的参数
        int brightTemp = getIntent().getIntExtra("brightness",55);
        brightness = (byte) brightTemp;
        getViewDataBind().deviceSettingIncludeLamp.deviceIncludeSbBrightness.setProgress(brightTemp-10);

        //banlap: 设备调节亮度监听
        getViewDataBind().deviceSettingIncludeLamp.deviceIncludeTxtBrightnessLength.setText(brightTemp+"%");
        getViewDataBind().deviceSettingIncludeLamp.deviceIncludeSbBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightness = (byte) (progress + 10);
                isOff = false;
                getViewDataBind().deviceSettingIncludeLamp.clDeviceSwitchBt.setBackgroundResource(R.drawable.selector_button_red_radius);

                getViewDataBind().deviceSettingIncludeLamp.deviceIncludeTxtBrightnessLength.setText(brightness + "%");
                if (isGetParams.get()) {
                    red=0;
                    blue=0;
                    green=0;
                    return;
                }

                byte opcode = (byte) 0xD2;
                byte[] params = new byte[]{brightness};
                VcomService.getInstance().sendCommandNoResponse(opcode, addr, params);

                red=0;
                blue=0;
                green=0;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //banlap：获取场景下保存的参数
        int temperatureTemp = getIntent().getIntExtra("temp",50);
        temperature = (byte) temperatureTemp;
        getViewDataBind().deviceSettingIncludeLamp.deviceIncludeSbTemperature.setProgress(temperatureTemp);

        //banlap: 设备调节色温监听
        getViewDataBind().deviceSettingIncludeLamp.deviceIncludeTxtTemperatureLength.setText(((65 - 27) * temperatureTemp) + 2700 + "K");
        getViewDataBind().deviceSettingIncludeLamp.deviceIncludeSbTemperature.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                temperature = (byte) progress;
                isOff = false;
                getViewDataBind().deviceSettingIncludeLamp.clDeviceSwitchBt.setBackgroundResource(R.drawable.selector_button_red_radius);

                getViewDataBind().deviceSettingIncludeLamp.deviceIncludeTxtTemperatureLength.setText(((65 - 27) * progress) + 2700 + "K");
                if (isGetParams.get()) {
                    red=0;
                    blue=0;
                    green=0;
                    return;
                }
                byte opcode = (byte) 0xE2;
                byte[] params = new byte[]{0x05, temperature};
                VcomService.getInstance().sendCommandNoResponse(opcode, addr, params);

                red=0;
                blue=0;
                green=0;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        getViewDataBind().deviceSettingIncludeLamp.deviceIncludeColorPicker.setListener(color -> {
            red = (byte) (color >> 16 & 0xFF);
            green = (byte) (color >> 8 & 0xFF);
            blue = (byte) (color & 0xFF);

            if (isGetParams.get()) {
                return;
            }

            byte opcode = (byte) 0xE2;
            byte[] params = new byte[]{0x04, red, green, blue};

            VcomService.getInstance().sendCommandNoResponse(opcode, addr, params);

        });

        //banlap：获取场景下保存的参数 色彩
        int redValue = Integer.parseInt(String.valueOf(getIntent().getByteExtra("red", (byte)0)&0xFF));
        int greenValue = Integer.parseInt(String.valueOf(getIntent().getByteExtra("green", (byte)0)&0xFF));
        int blueValue = Integer.parseInt(String.valueOf(getIntent().getByteExtra("blue", (byte)0)&0xFF));

        red = (byte) redValue;
        green = (byte) greenValue;
        blue = (byte) blueValue;

        if(redValue ==0 && greenValue ==0 && blueValue ==0) {
            getViewDataBind().deviceSettingIncludeLamp.clColorValue.setVisibility(View.GONE);
            getViewDataBind().deviceSettingIncludeLamp.tvRgbValue.setVisibility(View.GONE);
        } else {
            Drawable drawable = getResources().getDrawable(R.drawable.shape_color_picker_bg_green);
            drawable.setTint(Color.rgb(redValue, greenValue, blueValue));
            getViewDataBind().deviceSettingIncludeLamp.clColorValue.setBackgroundDrawable(drawable);

            //banlap：显示16进制的rgb颜色值
            getViewDataBind().deviceSettingIncludeLamp.tvRgbValue.setText(toHexEncoding(redValue, greenValue, blueValue));
        }


        //banlap: 设备调节色彩监听 - 颜色选取器
        getViewDataBind().deviceSettingIncludeLamp.scvColorPicker.setOnColorPickerChangeListener(new SlideColorPickerView.OnColorPickerChangeListener() {
            @Override
            public void onColorChanged(SlideColorPickerView picker, int color) {
                Drawable drawable = getResources().getDrawable(R.drawable.shape_color_picker_bg_green);
                drawable.setTint(color);

                Log.e("Banlap: change",String.format("%08x", color)
                    + "redOld:" +  (byte)(color >> 16 & 0xFF)  + "greenOld:" + (byte)(color >> 8 & 0xFF) + "blueOld:" + (byte)(color & 0xFF)
                    + "redNew:" + ((color & 16711680) >> 16) + "greenNew:" + ((color & 65280) >> 8) + "blueNew:" + (color & 255));
                getViewDataBind().deviceSettingIncludeLamp.clColorValue.setBackgroundDrawable(drawable);
                getViewDataBind().deviceSettingIncludeLamp.clColorValue.setVisibility(View.VISIBLE);

                isOff = false;
                getViewDataBind().deviceSettingIncludeLamp.clDeviceSwitchBt.setBackgroundResource(R.drawable.selector_button_red_radius);

                red = (byte) (color >> 16 & 0xFF);
                green = (byte) (color >> 8 & 0xFF);
                blue = (byte) (color & 0xFF);

                //banlap：显示16进制的rgb颜色值
                getViewDataBind().deviceSettingIncludeLamp.tvRgbValue.setText(toHexEncoding((color >> 16 & 0xFF), (color >> 8 & 0xFF), (color & 0xFF)));
                getViewDataBind().deviceSettingIncludeLamp.tvRgbValue.setVisibility(View.VISIBLE);

                if (isGetParams.get()) {
                    return;
                }

                byte opcode = (byte) 0xE2;
                byte[] params = new byte[]{0x04, red, green, blue};

                VcomService.getInstance().sendCommandNoResponse(opcode, addr, params);
            }

            @Override
            public void onStartTrackingTouch(SlideColorPickerView picker) {

            }

            @Override
            public void onStopTrackingTouch(SlideColorPickerView picker) {

            }
        });

        int lampMode = 0;
        for (Product.EquipInfo equipInfo : mProduct.getEquipInfoList()) {
            if (equipInfo.getEquipInfoPid().equals(mEquip.getEquipInfoPid())) {
                lampMode = Integer.parseInt(equipInfo.getEquipInfoType());
            }
        }
        if (lampMode == 0) {
            finish();
        }

        if (lampMode >= 1) {
            getViewDataBind().deviceSettingIncludeLamp.deviceIncludeViewBrightness.setVisibility(View.VISIBLE);
            //banlap: 特殊情况 杀菌教室灯 为 2305, lampMode为 1 只有在mode为2时才能调光 （可能是默认调节色温当作调光，可以与zdd api对接问题）
            if(mEquip.getProductUuid().equals("2305")) {
                mode = 2;
            }
        }

        if (lampMode >= 2) {
            mode = 2;
            getViewDataBind().deviceSettingIncludeLamp.tvControlTemperature.setVisibility(View.VISIBLE);
        }

        if (lampMode == 3) {
            mode = 1;
            getViewDataBind().deviceSettingIncludeLamp.tvControlColor.setVisibility(View.VISIBLE);
        }

        //banlap: 调控 选择面板
        getViewDataBind().deviceSettingIncludeLamp.tvControlBrightness.setOnClickListener(v->{
            getViewDataBind().deviceSettingIncludeLamp.tvControlBrightness.setTextColor(getResources().getColor(R.color.black));
            getViewDataBind().deviceSettingIncludeLamp.tvControlTemperature.setTextColor(getResources().getColor(R.color.gray_c9));
            getViewDataBind().deviceSettingIncludeLamp.tvControlColor.setTextColor(getResources().getColor(R.color.gray_c9));
            getViewDataBind().deviceSettingIncludeLamp.deviceIncludeViewBrightness.setVisibility(View.VISIBLE);
            getViewDataBind().deviceSettingIncludeLamp.deviceIncludeViewTemperature.setVisibility(View.GONE);
            getViewDataBind().deviceSettingIncludeLamp.deviceIncludeColorPicker.setVisibility(View.GONE);
            getViewDataBind().deviceSettingIncludeLamp.deviceIncludeViewColorPicker.setVisibility(View.GONE);
            getViewDataBind().deviceSettingIncludeLamp.clDeviceSwitchBt.setVisibility(View.VISIBLE);
        });
        getViewDataBind().deviceSettingIncludeLamp.tvControlTemperature.setOnClickListener(v->{
            getViewDataBind().deviceSettingIncludeLamp.tvControlBrightness.setTextColor(getResources().getColor(R.color.gray_c9));
            getViewDataBind().deviceSettingIncludeLamp.tvControlTemperature.setTextColor(getResources().getColor(R.color.black));
            getViewDataBind().deviceSettingIncludeLamp.tvControlColor.setTextColor(getResources().getColor(R.color.gray_c9));
            getViewDataBind().deviceSettingIncludeLamp.deviceIncludeViewBrightness.setVisibility(View.GONE);
            getViewDataBind().deviceSettingIncludeLamp.deviceIncludeViewTemperature.setVisibility(View.VISIBLE);
            getViewDataBind().deviceSettingIncludeLamp.deviceIncludeColorPicker.setVisibility(View.GONE);
            getViewDataBind().deviceSettingIncludeLamp.deviceIncludeViewColorPicker.setVisibility(View.GONE);
            getViewDataBind().deviceSettingIncludeLamp.clDeviceSwitchBt.setVisibility(View.VISIBLE);

        });
        getViewDataBind().deviceSettingIncludeLamp.tvControlColor.setOnClickListener(v->{
            getViewDataBind().deviceSettingIncludeLamp.tvControlBrightness.setTextColor(getResources().getColor(R.color.gray_c9));
            getViewDataBind().deviceSettingIncludeLamp.tvControlTemperature.setTextColor(getResources().getColor(R.color.gray_c9));
            getViewDataBind().deviceSettingIncludeLamp.tvControlColor.setTextColor(getResources().getColor(R.color.black));
            getViewDataBind().deviceSettingIncludeLamp.deviceIncludeViewBrightness.setVisibility(View.GONE);
            getViewDataBind().deviceSettingIncludeLamp.deviceIncludeViewTemperature.setVisibility(View.GONE);
            getViewDataBind().deviceSettingIncludeLamp.deviceIncludeColorPicker.setVisibility(View.GONE);
            getViewDataBind().deviceSettingIncludeLamp.deviceIncludeViewColorPicker.setVisibility(View.VISIBLE);
            getViewDataBind().deviceSettingIncludeLamp.clDeviceSwitchBt.setVisibility(View.GONE);
        });

        if(brightTemp==0) {
            getViewDataBind().deviceSettingIncludeLamp.clDeviceSwitchBt.setBackgroundResource(R.drawable.selector_button_gray_radius);
            isOff = !isOff;
        }
        //banlap: 控制设备开关
        getViewDataBind().deviceSettingIncludeLamp.clDeviceSwitchBt.setOnClickListener(v->{
            //banlap: 判断是否在场景中设置该设备
            if (isGetParams.get()) {
                if(isOff){
                    getViewDataBind().deviceSettingIncludeLamp.clDeviceSwitchBt.setBackgroundResource(R.drawable.selector_button_red_radius);
                } else {
                    getViewDataBind().deviceSettingIncludeLamp.clDeviceSwitchBt.setBackgroundResource(R.drawable.selector_button_gray_radius);
                }
                isOff = !isOff;
                return;
            }
            switch(mEquip.getConnectionStatus()) {
                case ON:
                    byte offCode = (byte) 0xD0;
                    VcomService.getInstance().sendCommandNoResponse(offCode, addr,
                            new byte[]{0x00, 0x00, 0x00});
                    getViewDataBind().deviceSettingIncludeLamp.clDeviceSwitchBt.setBackgroundResource(R.drawable.selector_button_gray_radius);
                    isOff = !isOff;
                    break;
                case OFF:
                    byte onCode = (byte) 0xD0;
                    VcomService.getInstance().sendCommandNoResponse(onCode, addr,
                            new byte[]{0x01, 0x00, 0x00});
                    getViewDataBind().deviceSettingIncludeLamp.clDeviceSwitchBt.setBackgroundResource(R.drawable.selector_button_red_radius);
                    isOff = !isOff;
                    break;
            }
        });

    }

    /**
     * banlap: 10进制转换16进制的rgb颜色值
     * */
    private String toHexEncoding(int r, int g, int b) {
        String R, G, B;
        StringBuffer sb = new StringBuffer();
        R = Integer.toHexString(r);
        G = Integer.toHexString(g);
        B = Integer.toHexString(b);
        //判断获取到的R,G,B值的长度 如果长度等于1 给R,G,B值的前边添0
        R = R.length() == 1 ? "0" + R : R;
        G = G.length() == 1 ? "0" + G : G;
        B = B.length() == 1 ? "0" + B : B;
        sb.append("0x");
        sb.append(R);
        sb.append(G);
        sb.append(B);
        return sb.toString();
    }

    @Override
    protected void initDatum() {
        if (getIntent().getExtras() == null) {
            finish();
        }
        String equipId = getIntent().getStringExtra("equipId");
        if (TextUtils.isEmpty(equipId)) {
            finish();
        }
        String sceneId = getIntent().getStringExtra("sceneId");
        if (!TextUtils.isEmpty(sceneId)) {
            isGetParams.set(true);
        }

        if (isGetParams.get()) {
            for (Region region : VcomSingleton.getInstance().getUserRegion()) {
                for (Scene scene : region.getSceneList()) {
                    if (scene.getSceneId().equals(sceneId)) {
                        mScene = scene;
                    }
                }
            }
            if (mScene == null) {
                finish();
            }
        }

        for (Equip userEquip : VcomSingleton.getInstance().getUserEquips()) {
            if (userEquip.getUserEquipId().equals(equipId)) {
                mEquip = userEquip;
            }
        }
        if (mEquip == null) {
            finish();
        }

        for (Product product : VcomSingleton.getInstance().getUserProduct()) {
            for (Product.EquipInfo equipInfo : product.getEquipInfoList()) {
                if (mEquip.getProductUuid().equals(equipInfo.getEquipInfoPid())) {
                    mProduct = product;
                    break;
                }
            }
        }

        if (mProduct == null) {
            finish();
        }
    }

    @Override
    public void viewBack() {
        if (isGetParams.get()) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_title))
                    .setMessage(getString(R.string.dialog_message_save_settings))
                    .setPositiveButton(getString(R.string.dialog_save), (dialog, which) -> {
                        String json = null;
                        Intent intent = new Intent();
                        intent.putExtra("cmd", cmd);

                        Map<String, Object> map = new HashMap<>();

                        if (devType == 0) {
                            if (isOff) {
                                brightness = 0;
                                temperature = 0;
                                mode = 2;
                            }

                            //banlap: 灯存在可以调rgb和冷暖时，mode需要切换， mode=1为rgb，mode=2为冷暖
                            if (red==0 && blue==0 && green ==0 ) {
                                mode = 2;
                                if(temperature==0){
                                    temperature = 2;
                                }
                            } else {
                                mode = 1;
                            }

                            byte[] params = new byte[]{0x01, cmd, (byte) Integer.parseInt(mScene.getSceneMeshId()), (byte) mode, brightness};
                            byte[] sceneByte = new byte[]{red, green, blue, temperature, 0};

                            intent.putExtra("equip_param", Util.byteMergerAll(params, sceneByte));

                            map.put("mode", mode);
                            map.put("red", red);
                            map.put("blue", blue);
                            map.put("green", green);
                            map.put("brightness", brightness);
                            map.put("temperature", temperature);
                        } else if (devType == 2) {

                            map.put("operate", operate);
                            byte[] params = new byte[]{0x01, cmd, (byte) Integer.parseInt(mScene.getSceneMeshId()), operate};
                            intent.putExtra("equip_param", params);

                        } else {

                            int on = 0;
                            int off = 0;
                            if (status != null) {
                                char[] offStr = new char[status.length];
                                for (int i = 0; i < status.length; i++) {
                                    if (status[i] == '0') {
                                        offStr[i] = '1';
                                    } else {
                                        offStr[i] = '0';
                                    }
                                }
                                on = Integer.parseInt(String.valueOf(status), 2);
                                off = Integer.parseInt(String.valueOf(offStr), 2);
                            }
                            byte[] params = new byte[]{0x01, cmd, (byte) Integer.parseInt(mScene.getSceneMeshId()), (byte) on, (byte) off};
                            intent.putExtra("equip_param", params);

                            map.put("threeSwitch", on);
                        }

                        json = GsonUtil.getInstance().toJson(map);
                        intent.putExtra("save_param", json);
                        setResult(0x1A, intent);
                        finish();
                    })
                    .setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> finish())
                    .create();
            alertDialog.show();
            Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_switch_bg_gray_f9);
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(17);
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(17);
        } else {
            finish();
        }
    }

    @Override
    public void viewBack2() {
        isSmartView = getIntent().getBooleanExtra("isSmartView", false);
        if(isSmartView && devType==0) {
            String json = null;

            Map<String, Object> map = new HashMap<>();
            map.put("userId", VcomSingleton.getInstance().getLoginUser().getUserId());
            map.put("userEquipId", getIntent().getStringExtra("equipId"));

            map.put("sceneId", 0);
            map.put("spaceId", 0);
            map.put("equipType", 0);

            if (isOff) {
                brightness = 0;
                temperature = 0;
                mode = 2;
            }

            //banlap: 灯存在可以调rgb和冷暖时，mode需要切换， mode=1为rgb，mode=2为冷暖
            if (red==0 && blue==0 && green ==0 ) {
                mode = 2;
                if(temperature==0){
                    temperature = 2;
                }
            } else {
                mode = 1;
            }

            map.put("mode", mode);
            map.put("red", red);
            map.put("blue", blue);
            map.put("green", green);
            map.put("brightness", brightness);
            map.put("temperature", temperature);

            json = GsonUtil.getInstance().toJson(map);

            getViewModel().saveParams(json);
            return;
        }
        finish();
    }

    //banlap: 主页或智能界面下点击设备调控 保存参数
    @Override
    public void saveParamsSuccess() {
        finish();
    }
    @Override
    public void saveParamsFailure() {
        finish();
    }

    //banlap: 三位开关选项
    private void switchOption(Equip equip, int index) {


        byte opcode = (byte) 0xF4;

        if (status[index] == '1') {
            status[index] = '0';
        } else {
            status[index] = '1';
        }

        if (isGetParams.get()) {
            return;
        }


        mEquip.setSwitchStatus(String.valueOf(status));

        byte arg = (byte) (1 << index);
        byte[] cmd = new byte[]{0x00, 0, 0};
        if (status[index] == '0') {
            cmd[1] = 1;
        }
        cmd[2] = arg;
        VcomService.getInstance().sendCommandNoResponse(opcode, Integer.parseInt(equip.getMeshAddress()), cmd);
    }

    /*
    * banlap: 查询触摸场景 返回成功
    * */
    @Override
    public void refreshTouchSceneList(List<TouchSwitch> list) {
        Toast.makeText(this, getString(R.string.toast_query_success),Toast.LENGTH_SHORT).show();
        touchSceneList.clear();
        touchSceneList.addAll(list);
        adapter.notifyDataSetChanged();
    }

    /*
     * banlap: 查询触摸场景 返回失败
     * */
    @Override
    public void queryTouchSceneListFailure() {
        Toast.makeText(this, getString(R.string.toast_query_error),Toast.LENGTH_SHORT).show();
    }

    private void showSceneListDialog(TouchSwitch touchSwitch) {
        if (selectSceneDialog != null && selectSceneDialog.isShowing()) {
            return;
        }

        List<Scene> newSceneList = new ArrayList<>();
        for (Region region : VcomSingleton.getInstance().getUserRegion()) {
            for (Scene scene : region.getSceneList()) {
                Scene newScene = new Scene();
                newScene.setSceneName(region.getSpaceName() + " -> " + scene.getSceneName());
                newScene.setSceneId(scene.getSceneId());
                newScene.setSceneImg(scene.getSceneImg());
                newScene.setSceneMeshId(scene.getSceneMeshId());
                newScene.setUserEquipList(scene.getUserEquipList());
                newSceneList.add(newScene);
            }
        }

        DialogSelectScenesBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_select_scenes,
                null, false);
        selectSceneDialog = new AlertDialog.Builder(this)
                .setView(binding.getRoot())
                .create();
        selectSceneDialog.show();
        Objects.requireNonNull(selectSceneDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_switch_bg_gray_f9);
        binding.dialogSelectSceneCancel.setOnClickListener(v -> selectSceneDialog.dismiss());
        binding.dialogSelectSceneCommit.setOnClickListener(v -> {
            for (Scene scene : newSceneList) {
                if (scene.isCheck()) {
                    byte opcode = (byte) 0xF6;
                    int address = Integer.parseInt(mEquip.getMeshAddress());
                    byte[] params = new byte[]{0x00, (byte) touchSwitch.getSequence(), (byte) Integer.parseInt(scene.getSceneMeshId())};
                    VcomService.getInstance().sendCommandNoResponse(opcode, address, params);
                    getViewModel().updateTouchSwitchScene(touchSwitch.getUserEquipId(), touchSwitch.getSequence(), scene.getSceneId());
                    selectSceneDialog.dismiss();
                    break;
                }
            }
        });

        SelectSceneAdapter selectSceneAdapter = new SelectSceneAdapter(this);
        selectSceneAdapter.setItems(newSceneList);
        binding.dialogSelectSceneRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.dialogSelectSceneRecycler.setAdapter(selectSceneAdapter);
        selectSceneAdapter.notifyDataSetChanged();

    }

    private class TouchSwitchAdapter extends BaseBindingAdapter<TouchSwitch, ItemTouchSwitchBinding> {

        public TouchSwitchAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        protected int getLayoutId(int layoutId) {
            return R.layout.item_touch_switch;
        }

        @Override
        protected void onBindItem(ItemTouchSwitchBinding binding, TouchSwitch item, int i) {

            Integer[] imgDefaultSmall = {
                    R.drawable.ic_touch_scene_0, R.drawable.ic_touch_scene_1,
                    R.drawable.ic_touch_scene_2, R.drawable.ic_touch_scene_3,
                    R.drawable.ic_touch_scene_4, R.drawable.ic_touch_scene_5
            };

            Integer[] imgDefaultSBackground = {
                    R.mipmap.icon_touch_scene_0, R.mipmap.icon_touch_scene_1,
                    R.mipmap.icon_touch_scene_2, R.mipmap.icon_touch_scene_3,
                    R.mipmap.icon_touch_scene_4, R.mipmap.icon_touch_scene_5
            };


            if(i>=6) {
                binding.ivShowSceneImg.setBackgroundResource(R.drawable.ic_touch_scene_0);
                binding.itemTouchSceneView.setBackgroundResource(R.mipmap.icon_touch_scene_0);
            } else {
                binding.ivShowSceneImg.setBackgroundResource(imgDefaultSmall[i]);
                binding.itemTouchSceneView.setBackgroundResource(imgDefaultSBackground[i]);
            }

            if (TextUtils.isEmpty(item.getSceneName())) {
                binding.itemTouchAddIcon.setVisibility(View.VISIBLE);
                binding.itemTouchLine.setVisibility(View.GONE);
                binding.itemTouchSpaceName.setVisibility(View.GONE);
                binding.itemTouchSceneName.setText(getString(R.string.bar_scene));
                binding.itemTouchSceneName.setTextColor(getResources().getColor(R.color.white));
                //binding.itemTouchSceneView.setBackgroundResource(R.drawable.shape_radius_touch_bg_green_hollow);
            } else {
                binding.itemTouchAddIcon.setVisibility(View.GONE);
                //binding.itemTouchLine.setVisibility(View.VISIBLE);
                //binding.itemTouchSpaceName.setVisibility(View.VISIBLE);
                binding.itemTouchSpaceName.setText(item.getSpaceName());
                binding.itemTouchSceneName.setText(item.getSceneName());
                binding.itemTouchSceneName.setTextColor(getResources().getColor(R.color.white));
                //binding.itemTouchSceneView.setBackgroundResource(R.drawable.shape_radius_touch_bg_green);
            }

            binding.itemTouchSceneView.setOnClickListener(v -> showSceneListDialog(item));
        }
    }

    private static class SelectSceneAdapter extends BaseBindingAdapter<Scene, ItemSelectSceneBinding> {

        public SelectSceneAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        protected int getLayoutId(int layoutId) {
            return R.layout.item_select_scene;
        }

        @Override
        protected void onBindItem(ItemSelectSceneBinding binding, Scene item, int i) {
            binding.itemSelectSceneName.setText(item.getSceneName());
            if(item.getUserEquipList().size()>0) {
                binding.itemSelectSceneIcon.setBackgroundResource(mContext.getResources()
                        .getIdentifier("ic_scene_selected_" + item.getSceneImg(), "drawable", mContext.getPackageName()));
            } else {
                binding.itemSelectSceneIcon.setBackgroundResource(mContext.getResources()
                        .getIdentifier("ic_scene_" + item.getSceneImg(), "drawable", mContext.getPackageName()));
            }


            if (item.isCheck()) {
                binding.itemSelectSceneSelection.setVisibility(View.VISIBLE);
                binding.itemSelectSceneSelection.setBackgroundResource(R.drawable.ic_select_yes);
            } else {
                binding.itemSelectSceneSelection.setVisibility(View.GONE);
            }

            binding.getRoot().setOnClickListener(v -> {
                allNotCheck();
                item.setCheck(!item.isCheck());
                notifyDataSetChanged();
            });
        }

        private void allNotCheck() {
            for (Scene item : items) {
                item.setCheck(false);
            }
            notifyDataSetChanged();
        }
    }

    /**
     * banlap: 监听手机上实体返回键
     * */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //banlap: 保存当前设置的参数
            viewBack2();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}