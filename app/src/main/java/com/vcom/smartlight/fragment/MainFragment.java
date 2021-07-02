package com.vcom.smartlight.fragment;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.reflect.TypeToken;
import com.telink.bluetooth.light.ConnectionStatus;
import com.vcom.smartlight.R;
import com.vcom.smartlight.base.BaseBindingAdapter;
import com.vcom.smartlight.base.BaseMvvmFragment;
import com.vcom.smartlight.databinding.DialogAddRegionBinding;
import com.vcom.smartlight.databinding.DialogGuideAddSceneBinding;
import com.vcom.smartlight.databinding.DialogGuideAddSceneSingleBinding;
import com.vcom.smartlight.databinding.DialogSelectRegionBinding;
import com.vcom.smartlight.databinding.FragmentMainBinding;
import com.vcom.smartlight.databinding.ItemDeviceFragmentEquipBinding;
import com.vcom.smartlight.databinding.ItemMainSceneBinding;
import com.vcom.smartlight.databinding.ItemRegionPictureBinding;
import com.vcom.smartlight.databinding.ItemSelectRegionBinding;
import com.vcom.smartlight.fvm.MainFVM;
import com.vcom.smartlight.model.Equip;
import com.vcom.smartlight.model.MessageEvent;
import com.vcom.smartlight.model.Product;
import com.vcom.smartlight.model.Region;
import com.vcom.smartlight.model.Scene;
import com.vcom.smartlight.model.Weather;
import com.vcom.smartlight.server.VcomService;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.ui.AddSceneActivity;
import com.vcom.smartlight.ui.AddSceneListActivity;
import com.vcom.smartlight.ui.DeviceSettingActivity;
import com.vcom.smartlight.ui.EditRegionActivity;
import com.vcom.smartlight.ui.RegionActivity;
import com.vcom.smartlight.ui.RegionDemoActivity;
import com.vcom.smartlight.ui.ScanByBleActivity;
import com.vcom.smartlight.ui.SceneManagerActivity;
import com.vcom.smartlight.utils.GsonUtil;
import com.vcom.smartlight.utils.SHA1Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Lzz
 * @Date 2020/10/27 18:44
 */
public class MainFragment extends BaseMvvmFragment<MainFVM, FragmentMainBinding> implements MainFVM.MainFvmCallBack {

    private final List<Region> mRegions = new ArrayList<>();
    private final List<Scene> mScenes = new ArrayList<>();
    private final List<Equip> mEquip = new ArrayList<>();

    private List<Region> defaultList = new ArrayList<>();  //选择区域
    private Boolean isAddDefaultRegion = true;             //选择区域中显示添加区域按钮
    private Boolean isClickRegionEdit = false;             //判断是否点击编辑区域

    private AlertDialog alertDialog;
    private MainFragmentSceneAdapter sceneAdapter;         //场景适配器
    private MainFragmentDeviceAdapter deviceAdapter;       //设备适配器

    private final List<Region> guideRegion = new ArrayList<>();
    private final List<Scene> guideScene = new ArrayList<>();

    private final List<Equip> mEquipList = new ArrayList<>();
    private final List<Equip> mEquipListTemp = new ArrayList<>();

    private final AtomicInteger regionIndex = new AtomicInteger(0);

    private String currentRegion ="";   //当前区域ID
    private int mSceneCount = 0;

    private int tagMesh = -1;

    private String localeLanguage="";  //获取当前系统语言
    private Equip mEquipTemp = new Equip();   //查询当前设备的保存参数


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    protected void afterCreate() {

    }

    @Override
    protected void initViews() {
        getViewDataBind().setVm(getViewModel());
        getViewModel().setCallBack(this);

        int screenWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        //banlap: 设置显示底层绿色圆形大小
        ViewGroup.LayoutParams layoutParams = getViewDataBind().ivBottomGreen.getLayoutParams();
        layoutParams.width = screenWidth + 120;
        getViewDataBind().ivBottomGreen.setLayoutParams(layoutParams);

        sceneAdapter = new MainFragmentSceneAdapter(getActivity());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(),RecyclerView.HORIZONTAL,false);
        getViewDataBind().mainFragmentRecycler.setLayoutManager(layoutManager);
        getViewDataBind().mainFragmentRecycler.setHasFixedSize(true);
        getViewDataBind().mainFragmentRecycler.setAdapter(sceneAdapter);
        sceneAdapter.setItems(mScenes);

        //banlap: 获取当前系统语言;
        localeLanguage = Locale.getDefault().getLanguage();

      /*  DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        defaultItemAnimator.setAddDuration(1000);
        defaultItemAnimator.setRemoveDuration(1000);
        getViewDataBind().mainFragmentRecycler.setItemAnimator(defaultItemAnimator);*/


        deviceAdapter = new MainFragmentDeviceAdapter(getActivity());
        getViewDataBind().rvRegionDeviceList.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL,false));
        getViewDataBind().rvRegionDeviceList.setAdapter(deviceAdapter);
        deviceAdapter.setItems(mEquipList);

        Typeface icon = Typeface.createFromAsset(getActivity().getAssets(), "fonts/iconfont.ttf");
        //getViewDataBind().tvDefaultTest.setTypeface(icon);

        //banlap: 获取本项目SHA1值
        String sha1 = SHA1Util.sHA1(getActivity());
        if(sha1!=null) {
            Log.e("SHA1", sha1);
        }



    }


    @Override
    protected void initDatum() {
        //banlap: 获取天气参数
        //getViewModel().getWeather();
        //banlap: 选择区域的list 载入 添加区域选项
        if(isAddDefaultRegion) {
            Region region = new Region(getString(R.string.dialog_add_region));
            region.setSpaceId("0");
            defaultList.add(region);
            isAddDefaultRegion = false;
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!VcomSingleton.getInstance().getLoginUser().isEmpty()) {
            EventBus.getDefault().post(new MessageEvent(MessageEvent.refreshRegion));
            EventBus.getDefault().post(new MessageEvent(MessageEvent.weatherReady));
        }
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onMessageEvent(MessageEvent event) {
        switch (event.msgCode) {
            case MessageEvent.regionReady:
                mRegions.clear();
                mRegions.addAll(VcomSingleton.getInstance().getUserRegion());
                getViewDataBind().mainFragmentProgress.setVisibility(View.GONE);
                loadTabs();
                break;
            case MessageEvent.equipReady:
                mEquip.clear();
                mEquip.addAll(VcomSingleton.getInstance().getUserEquips());
                loadEquipsCount();
                break;
            case MessageEvent.refreshRegion:
            case MessageEvent.refreshEquip:
                getViewModel().getNewRegion();
                break;
            case MessageEvent.weatherReady:
                //banlap: 获取当前位置和天气信息 高德地图sdk
                getAMapLocationAndWeather();
                //Weather weather = VcomSingleton.getInstance().getWeather();
                //loadWeatherData(weather);
                break;
            case MessageEvent.onlineStatusNotify:
                getViewModel().onOnlineStatusNotify(event.data);
                break;
            case MessageEvent.deviceStatusChanged:
                getViewModel().onDeviceStatusChanged(event.event);
                break;
        }
    }



    @SuppressLint("SetTextI18n")
    private void loadEquipsCount() {
        mEquipList.clear();
        //banlap: 如果当前区域中任意一个场景存在设备，则显示
        if(mEquipListTemp.size()>0) {
            //优化for循环
            int userEquipsSize = VcomSingleton.getInstance().getUserEquips().size();
            int mEquipListTempSize = mEquipListTemp.size();

            for(int x=0; x<userEquipsSize; x++) {
                for(int y=0; y<mEquipListTempSize; y++) {
                    if(VcomSingleton.getInstance().getUserEquips().get(x).getProductUuid().equals(mEquipListTemp.get(y).getProductUuid())) {
                        if(mEquipList.size()>0){
                            boolean isAdd = true;
                            int mEquipListSize = mEquipList.size();
                            for(int z=0; z<mEquipListSize; z++) {
                                if(mEquipList.get(z).getProductUuid().equals(VcomSingleton.getInstance().getUserEquips().get(x).getProductUuid())) {
                                    isAdd = false;
                                }
                            }
                            if(isAdd){
                                mEquipList.add(VcomSingleton.getInstance().getUserEquips().get(x));
                            }
                        } else {
                            mEquipList.add(VcomSingleton.getInstance().getUserEquips().get(x));
                        }
                        break;
                    }
                }
            }
        }


        //banlap: 切换区域时 更新区域数量
        if (localeLanguage.equals("en")) {
            getViewDataBind().tvDefaultDeviceCount.setText(mEquipList.size() + " device");
        } else {
            getViewDataBind().tvDefaultDeviceCount.setText(mEquipList.size() + "个设备");
        }
        deviceAdapter.notifyDataSetChanged();


    }

    /**
     * banlap: 获取当前位置和天气
     * */
    @SuppressLint("SetTextI18n")
    private void getAMapLocationAndWeather() {
        getViewDataBind().tvDefaultLocation.setText(VcomSingleton.getInstance().getAMapCity());
        getViewDataBind().tvDefaultWeather.setText(VcomSingleton.getInstance().getAMapWeather());
        getViewDataBind().tvDefaultTemp.setText(VcomSingleton.getInstance().getAMapTemp()+"℃");
        getViewDataBind().tvDefaultAirStatus.setText(VcomSingleton.getInstance().getAMapWind());
    }


    @SuppressLint("SetTextI18n")
    private void loadWeatherData(Weather weather) {
        //banlap: 显示当前天气状况
        if (localeLanguage.equals("en")) {
            getViewDataBind().tvDefaultLocation.setText(weather.getCityEn());
        } else {
            getViewDataBind().tvDefaultLocation.setText(weather.getCity());
        }
        getViewDataBind().tvDefaultWeather.setText(weather.getWea());
        getViewDataBind().tvDefaultTemp.setText(weather.getTem()+"°C");
        getViewDataBind().tvDefaultAirStatus.setText(weather.getAir_level());
    }

    @SuppressLint("SetTextI18n")
    private void loadTabs() {
        if (mRegions.size() == 0) {
            //banlap: 添加显示无场景无区域时图标
            getViewDataBind().ivMainNoSmartScene.setVisibility(View.VISIBLE);
            getViewDataBind().tvMainNoSmartScene.setVisibility(View.VISIBLE);
            getViewDataBind().tvMainNoSmartScene.setText(getString(R.string.no_region));
            getViewDataBind().mainFragmentCreateRegion.setVisibility(View.VISIBLE);
            getViewDataBind().mainFragmentCreateScene.setVisibility(View.GONE);
            getViewDataBind().mainFragmentTab.setVisibility(View.GONE);
            getViewDataBind().mainFragmentAddScene.setVisibility(View.GONE);
            getViewDataBind().mainFragmentRecycler.setVisibility(View.GONE);
            getViewDataBind().rlDeviceTitleBar.setVisibility(View.GONE);
            getViewDataBind().tvRegionSelect.setVisibility(View.VISIBLE);

            getViewDataBind().tvRegionSelect.setText(R.string.region);
            getViewDataBind().tvDefaultRegion.setText(R.string.no_add_region);
            getViewDataBind().tvDefaultDeviceCount.setText(R.string.default_device_count);
            getViewDataBind().tvDefaultSceneCount.setText(R.string.default_scene_count);
            getViewDataBind().cvRegionImg.setImageResource(R.mipmap.ic_region_img_horizontal_0);
            return;
        }

        getViewDataBind().mainFragmentTab.removeAllTabs();
        getViewDataBind().mainFragmentTab.setVisibility(View.GONE);
        //banlap: 变更主页交互功能 隐藏添加场景 (抛弃)
        //getViewDataBind().mainFragmentAddScene.setVisibility(View.INVISIBLE);
        getViewDataBind().mainFragmentAddScene.setVisibility(View.GONE);
        getViewDataBind().mainFragmentCreateRegion.setVisibility(View.GONE);
        //banlap: 添加显示无场景无区域时图标
        getViewDataBind().ivMainNoSmartScene.setVisibility(View.GONE);
        getViewDataBind().tvMainNoSmartScene.setVisibility(View.GONE);

        //banlap: 选择区域的list 重新载入数据
        defaultList.clear();
        isAddDefaultRegion = true;

        for (int i = 0; i < mRegions.size(); i++) {
            Region region = mRegions.get(i);
            //banlap: 添加所有区域到default
            defaultList.add(region);

            TabLayout.Tab tab = getViewDataBind().mainFragmentTab.newTab();
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_main_fragment_region, null);
            tab.setCustomView(view);
            TextView tvTitle = view.findViewById(R.id.item_main_fragment_region_name);
            tvTitle.setText(region.getSpaceName());
            getViewDataBind().mainFragmentTab.addTab(tab, false);
        }

        //banlap: 选择区域的list 载入 添加区域选项
        if(isAddDefaultRegion) {
            Region region = new Region(getString(R.string.dialog_add_region));
            region.setSpaceId("0");
            defaultList.add(region);
            isAddDefaultRegion = false;
        }

        getViewDataBind().mainFragmentTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                regionIndex.set(tab.getPosition());
                Region region = mRegions.get(tab.getPosition());
                refreshSceneData(region);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        if (regionIndex.get() >= mRegions.size()) {
            regionIndex.set(0);
        }
        //banlap: 获取当前区域的场景数量
        mSceneCount = mRegions.get(regionIndex.get()).getSceneList().size();

        //refreshSceneData(mRegions.get(regionIndex.get()));
        refreshSceneData(mRegions.get(regionIndex.get()));
        getViewDataBind().mainFragmentTab.selectTab(getViewDataBind().mainFragmentTab.getTabAt(regionIndex.get()));

        //banlap: 点击主页图片进入对应的区域管理界面
        getViewDataBind().rlThisRegion.setOnClickListener(v-> {
            //editRegion(mRegions.get(regionIndex.get()));
            //getViewModel().getCurrentRegion(mRegions.get(regionIndex.get()).getSpaceId());

            //banlap: 检测当前区域下所有设备的状态
            /*List<Equip> equipList =  VcomSingleton.getInstance().getUserEquips();
            if(equipList.size()>0) {
                Toast.makeText(getActivity(), "size: " + equipList.size(), Toast.LENGTH_SHORT).show();
            }*/

            VcomSingleton.getInstance().setUserCurrentRegion(mRegions.get(regionIndex.get()));

            Intent goRegion = new Intent(getActivity(), RegionActivity.class);
            goRegion.putExtra("SpaceId", mRegions.get(regionIndex.get()).getSpaceId());
            goRegion.putExtra("SpaceImg", mRegions.get(regionIndex.get()).getSpaceImg());
            goRegion.putExtra("SpaceName", mRegions.get(regionIndex.get()).getSpaceName());
            startActivity(goRegion);
        });
    }

    @Override
    public void getCurrentRegionSuccess() {

        Intent goRegion = new Intent(getActivity(), RegionActivity.class);
        goRegion.putExtra("SpaceId", mRegions.get(regionIndex.get()).getSpaceId());
        goRegion.putExtra("SpaceImg", mRegions.get(regionIndex.get()).getSpaceImg());
        startActivity(goRegion);
    }

    @SuppressLint("SetTextI18n")
    public void refreshSceneData(Region region) {

        //banlap: 切换区域后显示区域名称
        if(region.getSpaceName().length()>10){
            getViewDataBind().tvDefaultRegion.setText(region.getSpaceName().substring(0,10) + "...");
        } else {
            getViewDataBind().tvDefaultRegion.setText(region.getSpaceName());
        }
        getViewDataBind().tvRegionSelect.setVisibility(View.VISIBLE);
        if(region.getSpaceImg()!=null){
            getViewDataBind().cvRegionImg.setImageResource(getResources()
                    .getIdentifier("ic_region_img_horizontal_" + region.getSpaceImg(), "mipmap", getActivity().getPackageName()));
        } else {
            getViewDataBind().cvRegionImg.setImageResource(R.mipmap.ic_region_img_cut_0);
        }

        getViewDataBind().cvRegionImg.setAlpha((float)(1));

        //banlap: 如果区域名称字段过长则省略显示
        if(region.getSpaceName().length()>7) {
            getViewDataBind().tvRegionSelect.setText(region.getSpaceName().substring(0, 7)+"...");
        } else {
            getViewDataBind().tvRegionSelect.setText(region.getSpaceName());
        }

        List<Scene> scenes = region.getSceneList();
        currentRegion = region.getSpaceId();
        if (scenes == null || scenes.size() == 0) {
            getViewDataBind().mainFragmentCreateScene.setVisibility(View.VISIBLE);
            //banlap: 添加显示无场景无空间时图标
            getViewDataBind().ivMainNoSmartScene.setVisibility(View.VISIBLE);
            getViewDataBind().tvMainNoSmartScene.setVisibility(View.VISIBLE);
            getViewDataBind().tvMainNoSmartScene.setText(getString(R.string.no_scene));
            getViewDataBind().mainFragmentRecycler.setVisibility(View.GONE);
            getViewDataBind().rlDeviceTitleBar.setVisibility(View.GONE);
            getViewDataBind().rvRegionDeviceList.setVisibility(View.GONE);

            getViewDataBind().tvDefaultDeviceCount.setText(R.string.default_device_count);
            getViewDataBind().tvDefaultSceneCount.setText(R.string.default_scene_count);
            return;
        }

        getViewDataBind().mainFragmentCreateScene.setVisibility(View.GONE);
        //banlap: 添加显示无场景无空间时图标
        getViewDataBind().ivMainNoSmartScene.setVisibility(View.GONE);
        getViewDataBind().tvMainNoSmartScene.setVisibility(View.GONE);
        getViewDataBind().mainFragmentRecycler.setVisibility(View.VISIBLE);
        getViewDataBind().rlDeviceTitleBar.setVisibility(View.VISIBLE);
        getViewDataBind().rvRegionDeviceList.setVisibility(View.VISIBLE);

        mScenes.clear();
        mScenes.addAll(scenes);
        sceneAdapter.notifyDataSetChanged();

        //banlap: 区域下显示所有场景的所有设备
        mEquipList.clear();
        mEquipListTemp.clear();

        for(int i=0; i<scenes.size(); i++) {
            if(scenes.get(i).getUserEquipList().size()>0) {
                List<Equip> regionDevice = scenes.get(i).getUserEquipList();
                if(VcomSingleton.getInstance().getUserEquips().size()>0){
                    //优化for循环
                    int userEquipsSize = VcomSingleton.getInstance().getUserEquips().size();
                    int regionDeviceSize = regionDevice.size();
                    for(int x=0; x<userEquipsSize; x++) {
                        for(int y=0; y<regionDeviceSize; y++) {
                            if(VcomSingleton.getInstance().getUserEquips().get(x).getProductUuid().equals(regionDevice.get(y).getProductUuid())) {
                                if(mEquipList.size()>0){
                                    boolean isAdd = true;
                                    int mEquipListSize = mEquipList.size();
                                    for(int z=0; z<mEquipListSize; z++) {
                                        if(mEquipList.get(z).getProductUuid().equals(VcomSingleton.getInstance().getUserEquips().get(x).getProductUuid())) {
                                            isAdd = false;
                                        }
                                    }
                                    if(isAdd) {
                                        mEquipListTemp.add(VcomSingleton.getInstance().getUserEquips().get(x));
                                        mEquipList.add(VcomSingleton.getInstance().getUserEquips().get(x));
                                    }
                                } else {
                                    mEquipListTemp.add(VcomSingleton.getInstance().getUserEquips().get(x));
                                    mEquipList.add(VcomSingleton.getInstance().getUserEquips().get(x));
                                }
                                break;
                            }
                        }
                    }
                } else {
                    mEquipListTemp.addAll(regionDevice);
                    mEquipList.addAll(regionDevice);
                }
            } else {
                //mEquipList.clear();
                //mEquipListTemp.clear();
            }
        }

        //banlap: 优化代码 - 区域下显示所有场景的所有设备 (还没完成)
       /* for(int i=0; i<scenes.size(); i++) {
            if(scenes.get(i).getUserEquipList().size()>0) {
                List<Equip> regionDevice = scenes.get(i).getUserEquipList();
                if(VcomSingleton.getInstance().getUserEquips().size()>0){

                } else {
                    mEquipList.addAll(regionDevice);
                }
                if(mEquipList.size()>0){
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        Map<String, Equip> map = regionDevice.stream()
                                .collect(Collectors.toMap(Equip::getProductUuid, Equip -> Equip));
                        regionDevice.forEach(n -> {
                            if(!map.containsKey(n.getProductUuid())) {
                                mEquipList.add(n);
                            }
                        });
                    }
                }
            }
        }*/


        //banlap: 切换区域时 更新区域数量
        if (localeLanguage.equals("en")) {
            getViewDataBind().tvDefaultDeviceCount.setText(mEquipList.size() + " device");
            getViewDataBind().tvDefaultSceneCount.setText(scenes.size() + " scene");
        } else {
            getViewDataBind().tvDefaultDeviceCount.setText(mEquipList.size() + " 个设备");
            getViewDataBind().tvDefaultSceneCount.setText(scenes.size() +" 个场景");
        }
        deviceAdapter.notifyDataSetChanged();

    }



    @Override
    public void notifyChanged(List<Equip> equips) {
        mEquipList.clear();
        //banlap: 刷新设备状态，如果当前区域中任意一个场景存在设备，则显示
        if(mEquipListTemp.size()>0) {
            //优化for循环 单独拿出size值
            int equipsSize = equips.size();
            int mEquipListTempSize = mEquipListTemp.size();

            for(int x=0; x<equipsSize; x++) {
                for(int y=0; y<mEquipListTempSize; y++) {
                    if(equips.get(x).getProductUuid().equals(mEquipListTemp.get(y).getProductUuid())) {
                        if(mEquipList.size()>0){
                            boolean isAdd = true;
                            for(int z=0; z<mEquipList.size(); z++) {
                                if(mEquipList.get(z).getProductUuid().equals(equips.get(x).getProductUuid())) {
                                    isAdd = false;
                                }
                            }
                            if(isAdd){
                                mEquipList.add(equips.get(x));
                            }
                        } else {
                            mEquipList.add(equips.get(x));
                        }
                        break;
                    }
                }
            }
        }



        //banlap: 优化代码 - 区域下显示所有场景的所有设备 (还没完成)
       /* if(mEquipList.size()>0){
            List<Equip> list = new ArrayList<>(mEquipList);
            mEquipList.clear();
            mEquipListTemp.clear();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Map<String, Equip> map = list.stream()
                        .collect(Collectors.toMap(Equip::getProductUuid, Equip -> Equip));
                equips.forEach(n -> {
                    if(map.containsKey(n.getProductUuid())) {
                        mEquipList.add(n);
                        mEquipListTemp.add(n);
                    }
                });
            }
        }*/

        deviceAdapter.setItems(mEquipList);
        deviceAdapter.notifyDataSetChanged();
    }

    //banlap: 点击场景管理
    public void goSceneManager(Scene scene) {
        Intent intent = new Intent(getActivity(), SceneManagerActivity.class);
        intent.putExtra("sceneId", scene.getSceneId());
        startActivity(intent);
    }

    //banlap: 侧滑删除场景 提示框
    public void goDeleteScene(Scene scene) {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.dialog_title))
                .setMessage(getString(R.string.dialog_message_delete_scene))
                .setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dialog.dismiss())
                .setPositiveButton(getString(R.string.dialog_confirm), (dialog, which) -> {
                    dialog.dismiss();
                    getViewModel().deleteScene(scene);
                })
                .create()
                .show();
    }

    /**
     *  banlap: 侧滑删除场景
     * */
    @Override
    public void goDeleteSceneSuccess(Scene scene) {
        byte opcode = (byte) 0xEE;
        byte[] param = {0x00, (byte) Integer.parseInt(scene.getSceneMeshId())};
        VcomService.getInstance().sendCommandNoResponse(opcode, 0XFF, param);
    }

    /*
     * banlap: 点击控制台显示菜单
     * */
    @Override
    public void goControlMenu() {
        Intent intent = new Intent(getActivity(), RegionDemoActivity.class);
        startActivity(intent);
    }

    /**
     * banlap: 点击选择区域
     * */
    @Override
    public void goSelectRegion() {
        isClickRegionEdit = false;
        //banlap: 点击显示区域下拉框
        DialogSelectRegionBinding mSelectRegionBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()),
                R.layout.dialog_select_region, null, false);
        alertDialog = new AlertDialog.Builder(getActivity())
                .setView(mSelectRegionBinding.getRoot())
                .create();

        Window window = alertDialog.getWindow();
        //banlap: 设置AlertDialog占满全屏
        window.getDecorView().setPadding(0, 0, 0, 0);

        //banlap: 获取设备宽高
        WindowManager windowManager = getActivity().getWindowManager();
        Display display = windowManager.getDefaultDisplay();

      /*  DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);*/

        //banlap: 设置AlertDialog长度
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.y = -(int)(display.getHeight());

        //banlap: 设置AlertDialog下方圆角
        window.setBackgroundDrawableResource(R.drawable.shape_half_round_white);
        //banlap: 设置AlertDialog出现位置
        //window.setGravity(Gravity.TOP);
        window.setAttributes(params);
        window.setWindowAnimations(R.style.dialogStyle);


        final SelectRegionAdapter adapter = new SelectRegionAdapter(getActivity(), defaultList);
        mSelectRegionBinding.rvSelectIconList.setLayoutManager(new GridLayoutManager(getActivity(),2));
        mSelectRegionBinding.rvSelectIconList.setAdapter(adapter);
        mSelectRegionBinding.tvDialogSelectTitle.setText(getViewDataBind().tvRegionSelect.getText());
        mSelectRegionBinding.tvDialogSelectTitle.setTextColor(getResources().getColor(R.color.black));
        mSelectRegionBinding.llDialogSelectSettings.setOnClickListener(v->{
            mSelectRegionBinding.ivDialogSelectSettings.setVisibility(View.GONE);
            mSelectRegionBinding.tvDialogSelectFinish.setVisibility(View.VISIBLE);

            isClickRegionEdit = !isClickRegionEdit;
            //banlap: 选择区域 点击改变交互
            if(isClickRegionEdit) {
                mSelectRegionBinding.ivDialogSelectSettings.setVisibility(View.GONE);
                mSelectRegionBinding.tvDialogSelectFinish.setVisibility(View.VISIBLE);
            } else {
                mSelectRegionBinding.ivDialogSelectSettings.setVisibility(View.VISIBLE);
                mSelectRegionBinding.tvDialogSelectFinish.setVisibility(View.GONE);
            }
            adapter.notifyDataSetChanged();
        });

        adapter.notifyDataSetChanged();

        alertDialog.show();

    }



    /**
     * banlap: 变更主页添加按钮功能 - 显示菜单
     * */
    @Override
    public void goEditRegion() {

        Intent goScan = new Intent(getActivity(), ScanByBleActivity.class);
        startActivity(goScan);

        /*FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        DeviceFragment deviceFragment = new DeviceFragment();
        transaction.replace(R.id.main_fragment, deviceFragment);
        transaction.commit();*/

        //banlap: 变更主页添加按钮功能 - 显示菜单
        /*DialogAddMenuBinding addMenuBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()),
                R.layout.dialog_add_menu, null, false);
        alertDialog = new AlertDialog.Builder(getActivity())
                .setView(addMenuBinding.getRoot())
                .create();
        alertDialog.show();
        //banlap: 获取设备宽高
        WindowManager windowManager = getActivity().getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        //banlap: 设置菜单大小长宽
        WindowManager.LayoutParams params = alertDialog.getWindow().getAttributes();
        params.width = (int)(display.getWidth()*0.4);
        params.height = (int)(display.getHeight()*0.165);
        params.x = (int)(display.getWidth());
        params.y = -(int)(display.getHeight()*0.34);

        alertDialog.getWindow().setAttributes(params);

        //banlap: 点击添加区域
        addMenuBinding.llAddMenuAddRoom.setOnClickListener(v->{
            alertDialog.dismiss();
            goGuideRegion();
        });
        //banlap: 点击添加场景
        addMenuBinding.llAddMenuAddScene.setOnClickListener(v->{
            alertDialog.dismiss();
            if(mRegions.size()>0){
                //goAddScene();
                goAddNewScene();
            } else {
                Toast.makeText(getActivity(), getString(R.string.toast_add_region),Toast.LENGTH_LONG).show();
            }
        });*/

       /* Intent goManager = new Intent(getActivity(), EditRegionActivity.class);
        startActivity(goManager);*/

    }

    @Override
    public void refreshData() {
        EventBus.getDefault().post(new MessageEvent(MessageEvent.refreshRegion));
    }


    /**
     * banlap: 修改区域
     * */
    public void editRegion(Region region) {
        if (getActivity() == null) {
            return;
        }

        //banlap: 变更添加区域向导方式
        DialogAddRegionBinding addRegionBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()),
                R.layout.dialog_add_region, null, false);
        alertDialog = new AlertDialog.Builder(getActivity())
                .setView(addRegionBinding.getRoot())
                .create();
        alertDialog.show();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_switch_bg_gray_f9);
        addRegionBinding.tvDialogAlterRegion.setText(getString(R.string.dialog_alter_region));
        addRegionBinding.dialogAddRegionName.setText(region.getSpaceName());

        //banlap: 默认区域图片
        /*Integer[] picture = {R.mipmap.ic_region_img_demo_0,
                R.mipmap.ic_region_img_demo_1,
                R.mipmap.ic_region_img_demo_2,
                R.mipmap.ic_region_img_demo_3,
                R.mipmap.ic_region_img_demo_4,
                R.mipmap.ic_region_img_demo_5
        };*/

       /* Integer[] picture = {R.mipmap.ic_region_img_cut_0,
                R.mipmap.ic_region_img_cut_1,
                R.mipmap.ic_region_img_cut_2,
        };*/

        Integer[] picture = {R.mipmap.ic_region_img_horizontal_0,
                R.mipmap.ic_region_img_horizontal_1,
                R.mipmap.ic_region_img_horizontal_2,
                R.mipmap.ic_region_img_horizontal_3,
                R.mipmap.ic_region_img_horizontal_4,
        };

        int spaceImg = Integer.parseInt(region.getSpaceImg());

        SelectRegionPictureAdapter selectRegionPictureAdapter = new SelectRegionPictureAdapter(getActivity(), picture, spaceImg);
        addRegionBinding.rvSelectRegionPictureList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        addRegionBinding.rvSelectRegionPictureList.setAdapter(selectRegionPictureAdapter);

        addRegionBinding.dialogAddRegionImageCancel.setVisibility(View.INVISIBLE);
        addRegionBinding.dialogAddRegionCancel.setOnClickListener(v->{ alertDialog.dismiss();});
        addRegionBinding.dialogAddRegionCommit.setOnClickListener(v -> {
            String regionName =  addRegionBinding.dialogAddRegionName.getText().toString();
            if (TextUtils.isEmpty(regionName)) {
                return;
            }
            region.setSpaceName(regionName);
            region.setSpaceImg(String.valueOf(selectRegionPictureAdapter.selectIndex));
            getViewModel().updateRegion(region);
            alertDialog.dismiss();
        });
    }

    /**
     * banlap: 更新区域 返回成功
     * */
    @Override
    public void updateSuccess() {
        Toast.makeText(getActivity(),getString(R.string.toast_update_success),Toast.LENGTH_SHORT).show();
        refreshData();
    }
    /**
     * banlap: 更新区域 返回失败
     * */
    @Override
    public void updateFailure() {
        Toast.makeText(getActivity(),getString(R.string.toast_update_error),Toast.LENGTH_SHORT).show();
    }

    /**
     * banlap: 新增区域
     * */
    @Override
    public void goGuideRegion() {

        if (getActivity() == null) {
            return;
        }

        guideRegion.clear();

        //banlap: 变更添加区域向导方式
        DialogAddRegionBinding addRegionBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()),
                R.layout.dialog_add_region, null, false);
        alertDialog = new AlertDialog.Builder(getActivity())
                .setView(addRegionBinding.getRoot())
                .create();
        alertDialog.show();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_switch_bg_gray_f9);

        //banlap: 默认填入区域名称
        int regionCount = 1;
        String regionNewName = getString(R.string.region);

        if(mRegions.size()>0){
            regionCount = mRegions.size();
            regionCount++;
        }
        regionNewName = regionNewName + regionCount;
        addRegionBinding.dialogAddRegionName.setText(regionNewName);

        //banlap: 默认区域图片
       /* Integer[] picture = {R.mipmap.ic_region_img_demo_0,
                R.mipmap.ic_region_img_demo_1,
                R.mipmap.ic_region_img_demo_2,
                R.mipmap.ic_region_img_demo_3,
                R.mipmap.ic_region_img_demo_4,
                R.mipmap.ic_region_img_demo_5
        };*/

       /* Integer[] picture = {R.mipmap.ic_region_img_cut_0,
                R.mipmap.ic_region_img_cut_1,
                R.mipmap.ic_region_img_cut_2,
        };*/

        Integer[] picture = {R.mipmap.ic_region_img_horizontal_0,
                R.mipmap.ic_region_img_horizontal_1,
                R.mipmap.ic_region_img_horizontal_2,
                R.mipmap.ic_region_img_horizontal_3,
                R.mipmap.ic_region_img_horizontal_4,
        };

        SelectRegionPictureAdapter selectRegionPictureAdapter = new SelectRegionPictureAdapter(getActivity(), picture, 0);
        addRegionBinding.rvSelectRegionPictureList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        addRegionBinding.rvSelectRegionPictureList.setAdapter(selectRegionPictureAdapter);

        addRegionBinding.dialogAddRegionImageCancel.setVisibility(View.INVISIBLE);
        addRegionBinding.dialogAddRegionCancel.setOnClickListener(v->{ alertDialog.dismiss();});
        addRegionBinding.dialogAddRegionCommit.setOnClickListener(v -> {
            String regionName = addRegionBinding.dialogAddRegionName.getText().toString();
            if(!TextUtils.isEmpty(regionName)){
                Region newRegion = new Region(regionName);
                newRegion.setSpaceImg(String.valueOf(selectRegionPictureAdapter.selectIndex));
                guideRegion.add(newRegion);
                getViewModel().addRegionList(guideRegion);
                alertDialog.dismiss();
            }
        });


        //banlap: 原来的添加区域向导方式
       /* Region officeRegion = new Region("办公室");
        Region parlourRegion = new Region("会客厅");
        Region corridorRegion = new Region("走廊");

        DialogGuideAddRegionBinding guideAddRegionBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()),
                R.layout.dialog_guide_add_region, null, false);
        alertDialog = new AlertDialog.Builder(getActivity())
                .setView(guideAddRegionBinding.getRoot())
                .create();
        alertDialog.show();

        guideAddRegionBinding.dialogGuideAddRegionCancel.setOnClickListener(v -> alertDialog.dismiss());

        guideAddRegionBinding.dialogGuideAddRegionOffice.setOnClickListener(view -> {
            if (guideAddRegionBinding.dialogGuideAddRegionOfficeIcon.getVisibility() == View.GONE) {
                guideRegion.add(officeRegion);
                guideAddRegionBinding.dialogGuideAddRegionOfficeIcon.setVisibility(View.VISIBLE);
            } else {
                guideRegion.remove(officeRegion);
                guideAddRegionBinding.dialogGuideAddRegionOfficeIcon.setVisibility(View.GONE);
            }
        });

        guideAddRegionBinding.dialogGuideAddRegionParlour.setOnClickListener(view -> {
            if (guideAddRegionBinding.dialogGuideAddRegionParlourIcon.getVisibility() == View.GONE) {
                guideRegion.add(parlourRegion);
                guideAddRegionBinding.dialogGuideAddRegionParlourIcon.setVisibility(View.VISIBLE);
            } else {
                guideRegion.remove(parlourRegion);
                guideAddRegionBinding.dialogGuideAddRegionParlourIcon.setVisibility(View.GONE);
            }
        });

        guideAddRegionBinding.dialogGuideAddRegionCorridor.setOnClickListener(view -> {
            if (guideAddRegionBinding.dialogGuideAddRegionCorridorIcon.getVisibility() == View.GONE) {
                guideRegion.add(corridorRegion);
                guideAddRegionBinding.dialogGuideAddRegionCorridorIcon.setVisibility(View.VISIBLE);
            } else {
                guideRegion.remove(corridorRegion);
                guideAddRegionBinding.dialogGuideAddRegionCorridorIcon.setVisibility(View.GONE);
            }
        });

        guideAddRegionBinding.dialogGuideAddRegionCommit.setOnClickListener(v -> {
            getViewModel().addRegionList(guideRegion);
            alertDialog.dismiss();
        });*/
    }

    @Override
    public void goGuideScene() {
        if (getActivity() == null) {
            return;
        }

        guideScene.clear();

        Scene scene1 = new Scene("场景1", "0");
        Scene scene2 = new Scene("场景2", "0");
        Scene scene3 = new Scene("场景3", "0");

        DialogGuideAddSceneBinding addSceneBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()),
                R.layout.dialog_guide_add_scene, null, false);
        alertDialog = new AlertDialog.Builder(getActivity())
                .setView(addSceneBinding.getRoot())
                .create();
        alertDialog.show();

        addSceneBinding.dialogGuideAddSceneRegion.setText(mRegions.get(regionIndex.get()).getSpaceName());

        addSceneBinding.dialogGuideAddSceneCancel.setOnClickListener(v -> alertDialog.dismiss());

        addSceneBinding.dialogGuideAddSceneOne.setOnClickListener(view -> {
            if (addSceneBinding.dialogGuideAddSceneOneIcon.getVisibility() == View.GONE) {
                guideScene.add(scene1);
                addSceneBinding.dialogGuideAddSceneOneIcon.setVisibility(View.VISIBLE);
            } else {
                guideScene.remove(scene1);
                addSceneBinding.dialogGuideAddSceneOneIcon.setVisibility(View.GONE);
            }
        });

        addSceneBinding.dialogGuideAddSceneTwo.setOnClickListener(view -> {
            if (addSceneBinding.dialogGuideAddSceneTwoIcon.getVisibility() == View.GONE) {
                guideScene.add(scene2);
                addSceneBinding.dialogGuideAddSceneTwoIcon.setVisibility(View.VISIBLE);
            } else {
                guideScene.remove(scene2);
                addSceneBinding.dialogGuideAddSceneTwoIcon.setVisibility(View.GONE);
            }
        });

        addSceneBinding.dialogGuideAddSceneThree.setOnClickListener(view -> {
            if (addSceneBinding.dialogGuideAddSceneThreeIcon.getVisibility() == View.GONE) {
                guideScene.add(scene3);
                addSceneBinding.dialogGuideAddSceneThreeIcon.setVisibility(View.VISIBLE);
            } else {
                guideScene.remove(scene3);
                addSceneBinding.dialogGuideAddSceneThreeIcon.setVisibility(View.GONE);
            }
        });

        addSceneBinding.dialogGuideAddSceneCommit.setOnClickListener(v -> {
            getViewModel().addSceneList(mRegions.get(regionIndex.get()).getSpaceId(), guideScene);
            alertDialog.dismiss();
        });
    }

    @Override
    public void goAddScene() {
        if (getActivity() == null) {
            return;
        }
        DialogGuideAddSceneSingleBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()),
                R.layout.dialog_guide_add_scene_single, null, false);
        alertDialog = new AlertDialog.Builder(getActivity())
                .setView(binding.getRoot())
                .create();

        Integer[] icons = {R.drawable.ic_scene_0, R.drawable.ic_scene_1, R.drawable.ic_scene_2, R.drawable.ic_scene_3};
        Integer[] iconSelected = {R.drawable.ic_scene_selected_0, R.drawable.ic_scene_selected_1, R.drawable.ic_scene_selected_2, R.drawable.ic_scene_selected_3};

        Integer[] iconsUnicode = {R.string.icon_scene_0, R.string.icon_scene_1, R.string.icon_scene_2, R.string.icon_scene_3};

        //banlap: 场景选择适配器
        SceneIconAdapter adapter = new SceneIconAdapter(getActivity(), icons, iconSelected, iconsUnicode);
        binding.dialogGuideAddSceneSingleRegion.setText(mRegions.get(regionIndex.get()).getSpaceName());
        binding.dialogGuideAddSceneSingleCancel.setOnClickListener(v -> alertDialog.dismiss());
        binding.dialogGuideAddSceneSingleRecycler.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        binding.dialogGuideAddSceneSingleRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        binding.dialogGuideAddSceneSingleCommit.setOnClickListener(v -> {
            String sceneName = binding.dialogGuideAddSceneSingleEdit.getText().toString();
            if (!TextUtils.isEmpty(sceneName)) {
                getViewModel().addSingleScene(mRegions.get(regionIndex.get()).getSpaceId(), new Scene(sceneName, String.valueOf(adapter.selectIndex)));
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    /**
     * banlap: 点击添加新场景 （重新设计界面）
     * */
    @Override
    public void goAddNewScene() {

        if(!currentRegion.equals("")){
            mSceneCount++;
            List<Scene> addNewScene = new ArrayList<>();
            String sceneName = getString(R.string.add_new_scene) + mSceneCount;
            Scene scene = new Scene(sceneName, "0");
            addNewScene.add(scene);
            //getViewModel().addSceneList(mRegions.get(regionIndex.get()).getSpaceId(), addNewScene);
            getViewModel().addSceneList(currentRegion, addNewScene);


        }
    }

    /**
     * banlap: 点击进入区域管理界面
     * */
    @Override
    public void goRegionManger() {
        Intent goManger = new Intent(getActivity(), EditRegionActivity.class);
        startActivity(goManger);
    }

    /*
     * banlap: 点击进入场景列表管理界面
     * */
    @Override
    public void goSceneListManager() {

        if(!currentRegion.equals("")) {
            Intent goSceneMangerIntent = new Intent(getActivity(), AddSceneListActivity.class);
            goSceneMangerIntent.putExtra("CurrentRegionId", currentRegion);
            startActivity(goSceneMangerIntent);
        }
    }


    /**
     * banlap: 添加场景 返回成功
     * */
    @Override
    public void addSceneSuccess(List<Scene> scenes) {
        Toast.makeText(getActivity(),getString(R.string.toast_add_success),Toast.LENGTH_SHORT).show();
        //添加新场景
        Intent addNewSceneIntent = new Intent(getActivity(), AddSceneActivity.class);
        addNewSceneIntent.putExtra("CurrentRegionId", currentRegion);
        addNewSceneIntent.putExtra("NewSceneDefaultName", scenes.get(0).getSceneName());
        startActivity(addNewSceneIntent);
        refreshData();
    }

    /**
     * banlap: 添加场景 返回失败
     * */
    @Override
    public void addSceneFailure() {
        Toast.makeText(getActivity(),getString(R.string.toast_add_error),Toast.LENGTH_SHORT).show();
    }


    public void deleteSceneEquip(Region region) {
        //banlap: 删除区域下所有场景的设备删除
        for(int i=0; i<region.getSceneList().size(); i++) {
            if(region.getSceneList().get(i).getUserEquipList().size()>0) {
                List<Equip> userEquipList = region.getSceneList().get(i).getUserEquipList();
                for(int j=0; j<userEquipList.size(); j++) {
                    if(Integer.parseInt(userEquipList.get(j).getMeshAddress()) != -1) {
                        int meshId = Integer.parseInt(userEquipList.get(j).getMeshAddress());
                        byte opcode = (byte) 0xEE;
                        byte[] param = {0x00, (byte) Integer.parseInt(region.getSceneList().get(i).getSceneMeshId())};
                        VcomService.getInstance().sendCommandNoResponse(opcode, meshId, param);
                    }
                }
            }
        }
        getViewModel().deleteRegion(region);
    }


    /**
     * banlap: 删除区域 返回成功
     * */
    @Override
    public void deleteSuccess() {
        Toast.makeText(getActivity(),getString(R.string.toast_delete_success),Toast.LENGTH_SHORT).show();
        //banlap: 区域剩余一个时删除后 需要清空deviceList表数据
        if(mRegions.size()==1) {
            List<Equip> nullList = new ArrayList<>();
            deviceAdapter.setItems(nullList);
            deviceAdapter.notifyDataSetChanged();
        }
        getViewModel().refreshRegion();
    }
    /**
     * banlap: 删除区域 返回失败
     * */
    @Override
    public void deleteFailure() {
        Toast.makeText(getActivity(),getString(R.string.toast_delete_error),Toast.LENGTH_SHORT).show();
    }

    /**
     * banlap: 主页控制台 弹框显示是否解绑设备
     * */
    public void showRemoveDialog(Equip equip) {
        tagMesh = -1;
        new android.app.AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.dialog_title_2))
                .setMessage(getString(R.string.dialog_message_initialization_action))
                .setPositiveButton(getString(R.string.dialog_confirm), (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    getViewModel().removeEquip(equip.getUserEquipId());
                    tagMesh = Integer.parseInt(equip.getMeshAddress());
                })
                .setNegativeButton(getString(R.string.dialog_cancel), (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }
    @Override
    public void removeEquipSuccess() {

        Toast.makeText(getActivity(), getString(R.string.toast_unbinding_success),Toast.LENGTH_SHORT).show();
        if (tagMesh == -1) {
            return;
        }
        byte kickCode = (byte) 0xE3;
        byte[] params = new byte[]{0x01};
        VcomService.getInstance().sendCommandNoResponse(kickCode, tagMesh, params);
    }

    /**
     * banlap: 主页控制台 弹框显示是否删除设备
     * */
    public void showRemoveEquipDialog(Equip equip) {
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.dialog_title_2))
                .setMessage(getString(R.string.dialog_message_delete_scene_device))
                .setPositiveButton(getString(R.string.dialog_confirm), (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    removeAllSceneUserEquip(currentRegion, equip.getUserEquipId());
                })
                .setNegativeButton(getString(R.string.dialog_cancel), (dialogInterface, i) -> dialogInterface.dismiss())
                .create();

        alertDialog.show();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_switch_bg_gray_f9);
    }

    public void removeAllSceneUserEquip(String spaceId, String userEquipId) {
        //banlap: 根据当前区域场景里面的设备逐步删除场景id
        if(mScenes.size()>0) {
            Region cRegion = new Region();
            for(int i=0; i<mRegions.size(); i++) {
                if(mRegions.get(i).getSpaceId().equals(spaceId)) {
                    cRegion = mRegions.get(i);
                }
            }

            if(cRegion.getSceneList().size()>0) {
                int regionSceneListSize = cRegion.getSceneList().size();
                for(int j=0; j<regionSceneListSize; j++) {
                    if(cRegion.getSceneList().get(j).getUserEquipList().size()>0) {
                        List<Equip> equipList = cRegion.getSceneList().get(j).getUserEquipList();
                        int equipListSize = equipList.size();
                        for(int k=0; k<equipListSize; k++) {
                            if(equipList.get(k).getUserEquipId().equals(userEquipId)) {
                                int meshId = Integer.parseInt(equipList.get(k).getMeshAddress());
                                byte opcode = (byte) 0xEE;
                                byte[] param = {0x00, (byte) Integer.parseInt(cRegion.getSceneList().get(j).getSceneMeshId())};
                                VcomService.getInstance().sendCommandNoResponse(opcode, meshId, param);
                            }
                        }
                    }
                }
            }

        }
        getViewModel().removeUserEquip(spaceId, userEquipId);
    }

    @Override
    public void removeUserEquipSuccess() {
        EventBus.getDefault().post(new MessageEvent(MessageEvent.refreshEquip));
        Toast.makeText(getActivity(), getString(R.string.toast_delete_success),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void removeUserEquipFailure() {
        Toast.makeText(getActivity(), getString(R.string.toast_delete_error),Toast.LENGTH_SHORT).show();
    }

    /**
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

    /**
     * banlap: 跳转到设备调控界面
     * */
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


    /**
     * banlap: 主页控制台 区域中场景列表
     * */
    private class MainFragmentSceneAdapter extends BaseBindingAdapter<Scene, ItemMainSceneBinding> {

        public MainFragmentSceneAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        protected int getLayoutId(int layoutId) {
            return R.layout.item_main_scene;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onBindItem(ItemMainSceneBinding binding, Scene item, int i) {
            //banlap: 当场景名称字符大于7时省略显示
            if(item.getSceneName().length()>5) {
                binding.itemSceneNormalName.setText(item.getSceneName().substring(0, 4) + getString(R.string.more));
            } else {
                binding.itemSceneNormalName.setText(item.getSceneName());
            }
            //banlap: 场景中有设备存在则显示特定的图标
            if(item.getUserEquipList().size()>0){
                binding.itemSceneNormalIcon.setBackgroundResource(mContext.getResources()
                        .getIdentifier("ic_scene_selected_" + item.getSceneImg(), "drawable", mContext.getPackageName()));
            } else {
                binding.itemSceneNormalIcon.setBackgroundResource(mContext.getResources()
                        .getIdentifier("ic_scene_" + item.getSceneImg(), "drawable", mContext.getPackageName()));
            }


            binding.getRoot().setOnClickListener(v -> {
                //banlap: 获取该场景的设备列表数量
                int userEquipListSize = mScenes.get(i).getUserEquipList().size();
                //banlap: 该场景有设备时执行场景
                if(userEquipListSize>0){
                    Toast.makeText(mContext,getString(R.string.toast_running) + item.getSceneName() + getString(R.string.toast_scene) + "!",Toast.LENGTH_SHORT).show();
                    int addr = 0xFFFF;
                    byte opcode = (byte) 0xEF;
                    byte[] params = {(byte) Integer.parseInt(item.getSceneMeshId())};
                    VcomService.getInstance().sendCommandNoResponse(opcode, addr, params);

                    //banlap: 设置点击场景后显示动画效果
                    int cx=binding.getRoot().getWidth();
                    int cy=binding.getRoot().getHeight()/2;
                    float radius=binding.getRoot().getWidth();

                    Animator anim = ViewAnimationUtils.createCircularReveal(binding.clMainSceneItem,cx,cy, radius,0);
                    anim.setDuration(500);
                    anim.start();

                } else {
                    Toast.makeText(getActivity(), getString(R.string.toast_no_device_in_scene), Toast.LENGTH_LONG).show();
                }

            });
            //banlap: 点击进入场景设置
            binding.itemSceneNormalSetting.setOnClickListener(v -> goSceneManager(item));
            //binding.flDelete.setOnClickListener(v -> goDeleteScene(item));
        }
    }


    /**
     * banlap: 主页控制台 区域中场景中所有设备列表
     * */
    private class MainFragmentDeviceAdapter extends BaseBindingAdapter<Equip, ItemDeviceFragmentEquipBinding> {

        public MainFragmentDeviceAdapter(Context mContext) {
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

            //banlap: 主页控制器 点击进入设备详情设置
            binding.getRoot().setOnClickListener(v -> {
                if (item.getConnectionStatus() == null || item.getConnectionStatus().equals(ConnectionStatus.OFFLINE)) {
                    return;
                }
                showParams(item);
            });

            int dstAddr = Integer.parseInt(item.getMeshAddress());

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

            //banlap: 侧滑解绑设备
            binding.flDelete.setOnClickListener(v->{
                //Toast.makeText(getActivity(), "点击删除", Toast.LENGTH_LONG).show();
                //showRemoveDialog(item);
                showRemoveEquipDialog(item);
            });
        }
    }


    /**
     * banlap: 场景选择图标
     * */
    private static class SceneIconViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView iconUni;

        public SceneIconViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.item_scene_icon_image);
            iconUni = itemView.findViewById(R.id.tv_scene_icon);
        }
    }

    /**
     * banlap: 场景选择图标 适配器
     * */
    private static class SceneIconAdapter extends RecyclerView.Adapter<SceneIconViewHolder> {

        private final Integer[] data;
        private final Integer[] dataSelected;
        private final Integer[] dataUni;
        private final Context mContext;

        private int selectIndex = 0;

        public SceneIconAdapter(Context context, Integer[] arg0, Integer[] arg1, Integer[] arg2) {
            mContext = context;
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
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_scene_icon, parent, false);
            return new SceneIconViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SceneIconViewHolder holder, int position) {
            Typeface iconView = Typeface.createFromAsset(mContext.getAssets(), "fonts/iconfont.ttf");
            holder.iconUni.setTypeface(iconView);
            holder.iconUni.setText(dataUni[position]);
            if (selectIndex == position) {
                //holder.icon.setBackgroundResource(dataSelected[position]);
                holder.iconUni.setTextColor(mContext.getResources().getColor(R.color.green));
            } else {
                //holder.icon.setBackgroundResource(data[position]);
                holder.iconUni.setTextColor(mContext.getResources().getColor(R.color.gray_99));
            }

            holder.itemView.setOnClickListener(v -> setSelectIndex(position));
        }

        @Override
        public int getItemCount() {
            return data.length;
        }

    }

    /**
     * banlap: 选择区域 适配器 （重新设计交互）
     * */
    private class SelectRegionAdapter extends RecyclerView.Adapter {

        private Context mContext;
        private List<Region> mData;

        private class SelectRegionViewHolder extends RecyclerView.ViewHolder{
            public SelectRegionViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        public SelectRegionAdapter(Context context, List<Region> data){
            this.mContext = context;
            this.mData = data;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemSelectRegionBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext),R.layout.item_select_region, parent,false);
            return new SelectRegionViewHolder(binding.getRoot());
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ItemSelectRegionBinding binding = DataBindingUtil.getBinding(holder.itemView);
            //banlap: 区域名称显示字段过长则省略显示
            if(mData.get(position).getSpaceName().length()>10){
                binding.tvRegionItem.setText(mData.get(position).getSpaceName().substring(0,10) + "...");
            } else {
                binding.tvRegionItem.setText(mData.get(position).getSpaceName());
            }
            //banlap: 是否处于删除状态
            binding.ivRegionItemDelete2.setVisibility(isClickRegionEdit? View.VISIBLE : View.INVISIBLE);

            if(!currentRegion.equals("")){
                if(mData.get(position).getSpaceId().equals(currentRegion)){
                    binding.tvRegionItem.setTextColor(getResources().getColor(R.color.white));
                    binding.clRegionItem.setBackgroundResource(R.drawable.selector_click_bt_green);
                    binding.ivRegionItemDelete2.setBackgroundResource(R.drawable.ic_delete_white);
                } else {
                    binding.tvRegionItem.setTextColor(getResources().getColor(R.color.text_color_default));
                    binding.clRegionItem.setBackgroundResource(R.drawable.selector_click_bt_white_gray);
                    binding.ivRegionItemDelete2.setBackgroundResource(R.drawable.ic_delete_green);
                }
            }

            //banlap: 添加区域 字体颜色
            if(mData.get(position).getSpaceId().equals("0")) {
                binding.tvRegionItem.setTextColor(getResources().getColor(R.color.green));
                binding.clRegionItem.setBackgroundResource(R.drawable.shape_switch_bg_gray_f9);
                binding.ivRegionItemDelete2.setVisibility(View.INVISIBLE);
            }
            //banlap: 点击删除区域
            binding.llRegionItemDelete2.setOnClickListener(v->{
                if(isClickRegionEdit){
                    if(!mData.get(position).getSpaceId().equals("0")) {
                        AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                                .setTitle(R.string.dialog_title_2)
                                .setMessage(R.string.dialog_message_delete_region)
                                .setNegativeButton(R.string.dialog_cancel, ((dialog, which) -> dialog.cancel()))
                                .setPositiveButton(R.string.dialog_confirm, ((dialog, which) -> {
                                    deleteSceneEquip(mData.get(position));
                                    //getViewModel().deleteRegion(mData.get(position));
                                    mData.remove(position);
                                    notifyDataSetChanged();
                                }))
                                .create();
                        alertDialog.show();

                    }
                }
            });

            //banlap: 点击选择区域 显示或修改
            binding.getRoot().setOnClickListener(v->{
                if(!isClickRegionEdit){
                    alertDialog.dismiss();
                    if(mData.get(position).getSpaceId().equals("0")) {
                        goGuideRegion();
                    } else {
                        binding.tvRegionItem.setTextColor(getResources().getColor(R.color.text_color_default));
                        binding.clRegionItem.setBackgroundResource(R.drawable.shape_switch_bg_gray_f9);
                        Region region = mData.get(position);
                        regionIndex.set(position);
                        refreshSceneData(region);
                    }
                }
            });

            //banlap: 长按进入修改区域弹窗
            binding.getRoot().setOnLongClickListener(v -> {
                if(!isClickRegionEdit) {
                    if(!mData.get(position).getSpaceId().equals("0")) {
                        alertDialog.dismiss();
                        editRegion(mData.get(position));
                    }
                }
                return false;
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    /**
     * banlap: 选择区域图片 适配器
     * */
    private class SelectRegionPictureAdapter extends RecyclerView.Adapter {

        private Context mContext;
        private final Integer[] pictureSelected;
        private int selectIndex = 0;


        public SelectRegionPictureAdapter(Context context, Integer[] arg0, int spaceImg){
            this.mContext = context;
            this.pictureSelected = arg0;
            this.selectIndex = spaceImg;
        }

        public void setSelectIndex(int selectIndex){
            this.selectIndex = selectIndex;
            notifyDataSetChanged();
        }

        private class SelectRegionViewHolder extends RecyclerView.ViewHolder{
            public SelectRegionViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemRegionPictureBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_region_picture, parent,false);
            return new SelectRegionViewHolder(binding.getRoot());
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ItemRegionPictureBinding binding = DataBindingUtil.getBinding(holder.itemView);
            //binding.ivRegionPicture.setImageResource(pictureSelected[position]);
            Glide.with(mContext).load(pictureSelected[position]).into(binding.ivRegionPicture);

            if (selectIndex == position) {
                binding.ivRegionPictureSelect.setBackgroundResource(R.mipmap.icon_selected);
            } else {
                binding.ivRegionPictureSelect.setBackgroundResource(R.mipmap.icon_default_selected);
            }
            binding.getRoot().setOnClickListener(v-> setSelectIndex(position));
        }

        @Override
        public int getItemCount() {
            return pictureSelected.length;
        }
    }


}
