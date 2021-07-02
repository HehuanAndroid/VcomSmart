package com.vcom.smartlight.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.google.android.material.tabs.TabLayout;
import com.telink.bluetooth.event.DeviceEvent;
import com.telink.bluetooth.event.ErrorReportEvent;
import com.telink.bluetooth.event.MeshEvent;
import com.telink.bluetooth.event.NotificationEvent;
import com.telink.bluetooth.event.ServiceEvent;
import com.telink.util.Event;
import com.telink.util.EventListener;
import com.vcom.smartlight.R;
import com.vcom.smartlight.VcomApp;
import com.vcom.smartlight.base.BaseMvvmActivity;
import com.vcom.smartlight.databinding.ActivityMainBinding;
import com.vcom.smartlight.dialog.RequestPermissionDialog;
import com.vcom.smartlight.fragment.DeviceFragment;
import com.vcom.smartlight.fragment.MainFragment;
import com.vcom.smartlight.fragment.MeFragment;
import com.vcom.smartlight.model.MessageEvent;
import com.vcom.smartlight.server.VcomService;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.uivm.MainVM;
import com.vcom.smartlight.utils.ActivityUtil;
import com.vcom.smartlight.utils.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends BaseMvvmActivity<MainVM, ActivityMainBinding> implements EventListener<String>, MainVM.MainVmCallBack {

    private RequestPermissionDialog request;

    private final int[] tab_icons = {R.drawable.selector_tab_main, R.drawable.selector_tab_device,
            R.drawable.selector_tab_me};

    private final AtomicBoolean isLoginSuccess = new AtomicBoolean(false);

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        goCheckPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        VcomApp.getApp().doInit();

        VcomApp.getApp().addEventListener(DeviceEvent.STATUS_CHANGED, this);
        VcomApp.getApp().addEventListener(NotificationEvent.ONLINE_STATUS, this);
        VcomApp.getApp().addEventListener(NotificationEvent.GET_ALARM, this);
        VcomApp.getApp().addEventListener(NotificationEvent.GET_DEVICE_STATE, this);
        VcomApp.getApp().addEventListener(ServiceEvent.SERVICE_CONNECTED, this);
        VcomApp.getApp().addEventListener(MeshEvent.OFFLINE, this);
        VcomApp.getApp().addEventListener(ErrorReportEvent.ERROR_REPORT, this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        VcomService.getInstance().disableAutoRefreshNotify();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            EventBus.getDefault().unregister(this);
            getViewModel().stopAutoConnect();
            VcomApp.getApp().doDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void performed(Event<String> event) {
        getViewModel().mainVmPerformed(event);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {

        getViewDataBind().setVm(getViewModel());
        getViewModel().setCallBack(this);
        EventBus.getDefault().register(this);

        if (VcomSingleton.getInstance().isUserLogin()) {
            getViewModel().validateUser();
        } else {
            validateSuccess();
            goRefreshRegion();
        }

        initTabs();
    }

    @Override
    protected void initDatum() {

        FragmentTransaction cleanFragmentTransaction = getSupportFragmentManager().beginTransaction();
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            cleanFragmentTransaction.remove(fragment);
        }
        cleanFragmentTransaction.commitAllowingStateLoss();

        Fragment main = new MainFragment();
        Fragment device = new DeviceFragment();
        Fragment me = new MeFragment();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_fragment, device, "1");
        fragmentTransaction.add(R.id.main_fragment, me, "2");
        fragmentTransaction.add(R.id.main_fragment, main, "0");
        fragmentTransaction.commitAllowingStateLoss();

        new Handler().postDelayed(() -> showAndHideFragmentByTag("0"), 300);
        //banlap: 获取天气参数
        //getViewModel().getWeather();
        //banlap: 测试天气 后台
        //getViewModel().getNewWeather();
        //banlap: 获取位置和天气信息 高的地域sdk
        getAMapLocation();

    }

    private void initTabs() {

        //banlap: 显示下方菜单栏 菜单图标
        String icon_main_text = getString(R.string.main_icon_main);
        String icon_smart_text = getString(R.string.main_icon_smart);
        String icon_user_text = getString(R.string.main_icon_user);
        String[] tab_titles = new String[]{icon_main_text, icon_smart_text, icon_user_text};

        for (int i = 0; i < tab_titles.length; i++) {
            TabLayout.Tab tab = getViewDataBind().mainTab.newTab();
            View view = LayoutInflater.from(this).inflate(R.layout.item_main_menu, null);
            tab.setCustomView(view);

            TextView tvTitle = view.findViewById(R.id.item_main_menu_name);
            tvTitle.setText(tab_titles[i]);
            ImageView imgTab = view.findViewById(R.id.item_main_menu_icon);
            imgTab.setImageResource(tab_icons[i]);
            getViewDataBind().mainTab.addTab(tab);
        }

        getViewDataBind().mainTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                    //banlap: 使用图片加载框架Glide后需去除 com.bumptech.glide.manager 字符串
                    if (fragment.getTag() != null && !fragment.getTag().equals("com.bumptech.glide.manager")) {
                        if (Integer.parseInt(fragment.getTag()) == tab.getPosition()) {
                            showAndHideFragmentByTag(fragment.getTag());
                        }

                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    /*
     * banlap: 获取当前位置 用于显示天气信息 高德地图 定位sdk
     * */
    private void getAMapLocation() {
        AMapLocationClient locationClientSingle = new AMapLocationClient(this);
        //初始化定位参数
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();

        mLocationOption.setNeedAddress(true);
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setInterval(2000);
        locationClientSingle.setLocationOption(mLocationOption);

        AMapLocationListener locationSingleListener = aMapLocation -> {
            if(aMapLocation!=null) {
                if(aMapLocation.getErrorCode() == 0) {
                    /*Log.e("AmapSucceed","location Succeed, SucCode:" + aMapLocation.getAddress());*/
                    //getViewDataBind().tvDefaultLocation.setText(aMapLocation.getCity());
                    VcomSingleton.getInstance().setAMapCity(changeCityLanguage(aMapLocation.getCity()));
                    getAMapWeather(aMapLocation.getAdCode());
                    locationClientSingle.stopLocation();
                } else {
                    Log.e("AmapError","location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }
        };

        locationClientSingle.setLocationListener(locationSingleListener);
        locationClientSingle.startLocation();
    }

    /*
     * banlap: 获取当前位置的天气信息 高德地图 地图sdk的获取天气方法
     * */
    private void getAMapWeather(String adCode) {
        //检索参数为城市和天气类型，实况天气为WEATHER_TYPE_LIVE、天气预报为WEATHER_TYPE_FORECAST
        WeatherSearchQuery weatherSearchQuery = new WeatherSearchQuery(adCode, WeatherSearchQuery.WEATHER_TYPE_LIVE);
        WeatherSearch weatherSearch = new WeatherSearch(this);
        weatherSearch.setQuery(weatherSearchQuery);
        //异步搜索
        weatherSearch.searchWeatherAsyn();

        weatherSearch.setOnWeatherSearchListener(new WeatherSearch.OnWeatherSearchListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int i) {
                if(i==1000) {
                    if (localWeatherLiveResult != null && localWeatherLiveResult.getLiveResult() != null) {
                        LocalWeatherLive localWeatherLive = localWeatherLiveResult.getLiveResult();
                        //Log.e("AmapWeatherSucceed","weather succeed" + localWeatherLive.getWeather());
                        //getViewDataBind().tvDefaultWeather.setText(localWeatherLive.getWeather());
                        //getViewDataBind().tvDefaultTemp.setText(localWeatherLive.getTemperature()+"℃");
                        //getViewDataBind().tvDefaultAirStatus.setText(localWeatherLive.getWindDirection());
                        VcomSingleton.getInstance().setAMapWeather(changeWeatherLanguage(localWeatherLive.getWeather()));
                        VcomSingleton.getInstance().setAMapTemp(localWeatherLive.getTemperature());
                        VcomSingleton.getInstance().setAMapWind(changeWindLanguage(localWeatherLive.getWindDirection()));
                        EventBus.getDefault().post(new MessageEvent(MessageEvent.weatherReady));
                    } else {
                        Log.e("AmapWeatherNull","weather null, nullCode:" + i);
                    }
                } else {
                    Log.e("AmapWeatherError","weather Error, ErrCode:" + i);
                }
            }

            @Override
            public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {

            }
        });


    }
    /*
     * banlap: 高德地图 城市字典翻译
     * */
    private String changeCityLanguage(String cityName) {
        String newCityName="";
        switch(cityName) {
            case"北京市":newCityName=getString(R.string.c_beijing);break;
            case"天津市":newCityName=getString(R.string.c_tianjin);break;
            case"石家庄市":newCityName=getString(R.string.c_shijiazhuang);break;
            case"辛集市":newCityName=getString(R.string.c_xinji);break;
            case"晋州市":newCityName=getString(R.string.c_jinzhou);break;
            case"新乐市":newCityName=getString(R.string.c_xinle);break;
            case"唐山市":newCityName=getString(R.string.c_tangshan);break;
            case"滦州市":newCityName=getString(R.string.c_luanzhou);break;
            case"遵化市":newCityName=getString(R.string.c_zunhua);break;
            case"迁安市":newCityName=getString(R.string.c_qianan);break;
            case"秦皇岛市":newCityName=getString(R.string.c_qinhuangdao);break;
            case"邯郸市":newCityName=getString(R.string.c_handan);break;
            case"武安市":newCityName=getString(R.string.c_wuan);break;
            case"邢台市":newCityName=getString(R.string.c_xingtai);break;
            case"南宫市":newCityName=getString(R.string.c_nangong);break;
            case"沙河市":newCityName=getString(R.string.c_shahe);break;
            case"保定市":newCityName=getString(R.string.c_baoding);break;
            case"涿州市":newCityName=getString(R.string.c_zhuozhou);break;
            case"定州市":newCityName=getString(R.string.c_dingzhou);break;
            case"安国市":newCityName=getString(R.string.c_anguo);break;
            case"高碑店市":newCityName=getString(R.string.c_gaobeidian);break;
            case"张家口市":newCityName=getString(R.string.c_zhangjiakou);break;
            case"承德市":newCityName=getString(R.string.c_chengde);break;
            case"平泉市":newCityName=getString(R.string.c_pingquan);break;
            case"沧州市":newCityName=getString(R.string.c_cangzhou);break;
            case"泊头市":newCityName=getString(R.string.c_botou);break;
            case"任丘市":newCityName=getString(R.string.c_renqiu);break;
            case"黄骅市":newCityName=getString(R.string.c_huanghua);break;
            case"河间市":newCityName=getString(R.string.c_hejian);break;
            case"廊坊市":newCityName=getString(R.string.c_langfang);break;
            case"霸州市":newCityName=getString(R.string.c_bazhou);break;
            case"三河市":newCityName=getString(R.string.c_sanhe);break;
            case"衡水市":newCityName=getString(R.string.c_hengshui);break;
            case"深州市":newCityName=getString(R.string.c_shenzhou);break;
            case"太原市":newCityName=getString(R.string.c_taiyuan);break;
            case"古交市":newCityName=getString(R.string.c_gujiao);break;
            case"大同市":newCityName=getString(R.string.c_datong);break;
            case"阳泉市":newCityName=getString(R.string.c_yangquan);break;
            case"长治市":newCityName=getString(R.string.c_changzhi);break;
            case"晋城市":newCityName=getString(R.string.c_jincheng);break;
            case"高平市":newCityName=getString(R.string.c_gaoping);break;
            case"朔州市":newCityName=getString(R.string.c_shuozhou);break;
            case"怀仁市":newCityName=getString(R.string.c_huairen);break;
            case"晋中市":newCityName=getString(R.string.c_jinzhong);break;
            case"介休市":newCityName=getString(R.string.c_jiexiu);break;
            case"运城市":newCityName=getString(R.string.c_yuncheng);break;
            case"永济市":newCityName=getString(R.string.c_yongji);break;
            case"河津市":newCityName=getString(R.string.c_hejin);break;
            case"忻州市":newCityName=getString(R.string.c_xinzhou);break;
            case"原平市":newCityName=getString(R.string.c_yuanping);break;
            case"临汾市":newCityName=getString(R.string.c_linfen);break;
            case"侯马市":newCityName=getString(R.string.c_houma);break;
            case"霍州市":newCityName=getString(R.string.c_huozhou);break;
            case"吕梁市":newCityName=getString(R.string.c_lvliang);break;
            case"孝义市":newCityName=getString(R.string.c_xiaoyi);break;
            case"汾阳市":newCityName=getString(R.string.c_fenyang);break;
            case"呼和浩特市":newCityName=getString(R.string.c_huhehaote);break;
            case"包头市":newCityName=getString(R.string.c_baotou);break;
            case"乌海市":newCityName=getString(R.string.c_wuhai);break;
            case"赤峰市":newCityName=getString(R.string.c_chifeng);break;
            case"通辽市":newCityName=getString(R.string.c_tongliao);break;
            case"霍林郭勒市":newCityName=getString(R.string.c_huolinguole);break;
            case"鄂尔多斯市":newCityName=getString(R.string.c_eerduosi);break;
            case"呼伦贝尔市":newCityName=getString(R.string.c_hulunbeier);break;
            case"满洲里市":newCityName=getString(R.string.c_manzhouli);break;
            case"牙克石市":newCityName=getString(R.string.c_yakeshi);break;
            case"扎兰屯市":newCityName=getString(R.string.c_zhalantun);break;
            case"额尔古纳市":newCityName=getString(R.string.c_eerguna);break;
            case"根河市":newCityName=getString(R.string.c_genhe);break;
            case"巴彦淖尔市":newCityName=getString(R.string.c_bayannaoer);break;
            case"乌兰察布市":newCityName=getString(R.string.c_wulanchabu);break;
            case"丰镇市":newCityName=getString(R.string.c_fengzhen);break;
            case"乌兰浩特市":newCityName=getString(R.string.c_wulanhaote);break;
            case"阿尔山市":newCityName=getString(R.string.c_aershan);break;
            case"二连浩特市":newCityName=getString(R.string.c_erlianhaote);break;
            case"锡林浩特市":newCityName=getString(R.string.c_xilinhaote);break;
            case"沈阳市":newCityName=getString(R.string.c_shenyang);break;
            case"新民市":newCityName=getString(R.string.c_xinmin);break;
            case"大连市":newCityName=getString(R.string.c_dalian);break;
            case"瓦房店市":newCityName=getString(R.string.c_wafangdian);break;
            case"庄河市":newCityName=getString(R.string.c_zhuanghe);break;
            case"鞍山市":newCityName=getString(R.string.c_anshan);break;
            case"海城市":newCityName=getString(R.string.c_haicheng);break;
            case"抚顺市":newCityName=getString(R.string.c_fushun);break;
            case"本溪市":newCityName=getString(R.string.c_benxi);break;
            case"丹东市":newCityName=getString(R.string.c_dandong);break;
            case"东港市":newCityName=getString(R.string.c_donggang);break;
            case"凤城市":newCityName=getString(R.string.c_fengcheng);break;
            case"锦州市":newCityName=getString(R.string.c_jinzhou1);break;
            case"凌海市":newCityName=getString(R.string.c_linghai);break;
            case"北镇市":newCityName=getString(R.string.c_beizhen);break;
            case"营口市":newCityName=getString(R.string.c_yingkou);break;
            case"西市区":newCityName=getString(R.string.c_xiqu);break;
            case"盖州市":newCityName=getString(R.string.c_gaizhou);break;
            case"大石桥市":newCityName=getString(R.string.c_daqiao);break;
            case"阜新市":newCityName=getString(R.string.c_fuxin);break;
            case"辽阳市":newCityName=getString(R.string.c_liaoyang);break;
            case"灯塔市":newCityName=getString(R.string.c_dengta);break;
            case"盘锦市":newCityName=getString(R.string.c_panjin);break;
            case"铁岭市":newCityName=getString(R.string.c_tieling);break;
            case"调兵山市":newCityName=getString(R.string.c_diaobingshan);break;
            case"开原市":newCityName=getString(R.string.c_kaiyuan);break;
            case"朝阳市":newCityName=getString(R.string.c_chaoyang);break;
            case"北票市":newCityName=getString(R.string.c_beipiao);break;
            case"凌源市":newCityName=getString(R.string.c_lingyuan);break;
            case"葫芦岛市":newCityName=getString(R.string.c_huludao);break;
            case"兴城市":newCityName=getString(R.string.c_xingcheng);break;
            case"长春市":newCityName=getString(R.string.c_changchun);break;
            case"榆树市":newCityName=getString(R.string.c_yushu);break;
            case"德惠市":newCityName=getString(R.string.c_dehui);break;
            case"吉林市":newCityName=getString(R.string.c_jilin);break;
            case"蛟河市":newCityName=getString(R.string.c_jiaohe);break;
            case"桦甸市":newCityName=getString(R.string.c_huadian);break;
            case"舒兰市":newCityName=getString(R.string.c_shulan);break;
            case"磐石市":newCityName=getString(R.string.c_panshi);break;
            case"四平市":newCityName=getString(R.string.c_siping);break;
            case"公主岭市":newCityName=getString(R.string.c_gongzhuling);break;
            case"双辽市":newCityName=getString(R.string.c_shuangliao);break;
            case"辽源市":newCityName=getString(R.string.c_liaoyuan);break;
            case"通化市":newCityName=getString(R.string.c_tonghua);break;
            case"梅河口市":newCityName=getString(R.string.c_meihekou);break;
            case"集安市":newCityName=getString(R.string.c_jian);break;
            case"白山市":newCityName=getString(R.string.c_baishan);break;
            case"临江市":newCityName=getString(R.string.c_linjiang);break;
            case"松原市":newCityName=getString(R.string.c_songyuan);break;
            case"扶余市":newCityName=getString(R.string.c_fuyu);break;
            case"白城市":newCityName=getString(R.string.c_baicheng);break;
            case"洮南市":newCityName=getString(R.string.c_tiaonan);break;
            case"大安市":newCityName=getString(R.string.c_daan);break;
            case"延吉市":newCityName=getString(R.string.c_yanji);break;
            case"图们市":newCityName=getString(R.string.c_tumen);break;
            case"敦化市":newCityName=getString(R.string.c_dunhua);break;
            case"珲春市":newCityName=getString(R.string.c_huichun);break;
            case"龙井市":newCityName=getString(R.string.c_longjing);break;
            case"和龙市":newCityName=getString(R.string.c_helong);break;
            case"哈尔滨市":newCityName=getString(R.string.c_haerbin);break;
            case"尚志市":newCityName=getString(R.string.c_shangzhi);break;
            case"五常市":newCityName=getString(R.string.c_wuchang);break;
            case"齐齐哈尔市":newCityName=getString(R.string.c_qiqihaer);break;
            case"讷河市":newCityName=getString(R.string.c_nehe);break;
            case"鸡西市":newCityName=getString(R.string.c_jixi);break;
            case"虎林市":newCityName=getString(R.string.c_hulin);break;
            case"密山市":newCityName=getString(R.string.c_mishan);break;
            case"鹤岗市":newCityName=getString(R.string.c_hegang);break;
            case"双鸭山市":newCityName=getString(R.string.c_shuangyashan);break;
            case"大庆市":newCityName=getString(R.string.c_daqing);break;
            case"伊春市":newCityName=getString(R.string.c_yichun);break;
            case"铁力市":newCityName=getString(R.string.c_tieli);break;
            case"佳木斯市":newCityName=getString(R.string.c_jiamusi);break;
            case"同江市":newCityName=getString(R.string.c_tongjiang);break;
            case"富锦市":newCityName=getString(R.string.c_fujin);break;
            case"抚远市":newCityName=getString(R.string.c_fuyuan);break;
            case"七台河市":newCityName=getString(R.string.c_qitaihe);break;
            case"牡丹江市":newCityName=getString(R.string.c_mudanjiang);break;
            case"绥芬河市":newCityName=getString(R.string.c_suifenhe);break;
            case"海林市":newCityName=getString(R.string.c_hailin);break;
            case"宁安市":newCityName=getString(R.string.c_ningan);break;
            case"穆棱市":newCityName=getString(R.string.c_muleng);break;
            case"东宁市":newCityName=getString(R.string.c_dongning);break;
            case"黑河市":newCityName=getString(R.string.c_heihe);break;
            case"嫩江市":newCityName=getString(R.string.c_nenjiang);break;
            case"北安市":newCityName=getString(R.string.c_beian);break;
            case"五大连池市":newCityName=getString(R.string.c_wudalianchi);break;
            case"绥化市":newCityName=getString(R.string.c_suihua);break;
            case"安达市":newCityName=getString(R.string.c_anda);break;
            case"肇东市":newCityName=getString(R.string.c_zhaodong);break;
            case"海伦市":newCityName=getString(R.string.c_hailun);break;
            case"漠河市":newCityName=getString(R.string.c_mohe);break;
            case"上海市":newCityName=getString(R.string.c_shanghai);break;
            case"南京市":newCityName=getString(R.string.c_nanjing);break;
            case"无锡市":newCityName=getString(R.string.c_wuxi);break;
            case"江阴市":newCityName=getString(R.string.c_jiangyin);break;
            case"宜兴市":newCityName=getString(R.string.c_yixing);break;
            case"徐州市":newCityName=getString(R.string.c_xuzhou);break;
            case"新沂市":newCityName=getString(R.string.c_xinyi);break;
            case"邳州市":newCityName=getString(R.string.c_pizhou);break;
            case"常州市":newCityName=getString(R.string.c_changzhou);break;
            case"溧阳市":newCityName=getString(R.string.c_liyang);break;
            case"苏州市":newCityName=getString(R.string.c_suzhou);break;
            case"常熟市":newCityName=getString(R.string.c_changshu);break;
            case"张家港市":newCityName=getString(R.string.c_zhangjiagang);break;
            case"昆山市":newCityName=getString(R.string.c_kunshan);break;
            case"太仓市":newCityName=getString(R.string.c_taicang);break;
            case"南通市":newCityName=getString(R.string.c_nantong);break;
            case"海安市":newCityName=getString(R.string.c_haian);break;
            case"启东市":newCityName=getString(R.string.c_qidong);break;
            case"如皋市":newCityName=getString(R.string.c_rugao);break;
            case"连云港市":newCityName=getString(R.string.c_lianyungang);break;
            case"淮安市":newCityName=getString(R.string.c_huaian);break;
            case"盐城市":newCityName=getString(R.string.c_yancheng);break;
            case"东台市":newCityName=getString(R.string.c_dongtai);break;
            case"扬州市":newCityName=getString(R.string.c_yangzhou);break;
            case"仪征市":newCityName=getString(R.string.c_yizheng);break;
            case"高邮市":newCityName=getString(R.string.c_gaoyou);break;
            case"镇江市":newCityName=getString(R.string.c_zhenjiang);break;
            case"丹阳市":newCityName=getString(R.string.c_danyang);break;
            case"扬中市":newCityName=getString(R.string.c_yangzhong);break;
            case"句容市":newCityName=getString(R.string.c_jurong);break;
            case"泰州市":newCityName=getString(R.string.c_taizhou);break;
            case"兴化市":newCityName=getString(R.string.c_xinghua);break;
            case"靖江市":newCityName=getString(R.string.c_jingjiang);break;
            case"泰兴市":newCityName=getString(R.string.c_taixing);break;
            case"宿迁市":newCityName=getString(R.string.c_suqian);break;
            case"杭州市":newCityName=getString(R.string.c_hangzhou);break;
            case"建德市":newCityName=getString(R.string.c_jiande);break;
            case"宁波市":newCityName=getString(R.string.c_ningbo);break;
            case"余姚市":newCityName=getString(R.string.c_yuyao);break;
            case"慈溪市":newCityName=getString(R.string.c_cixi);break;
            case"温州市":newCityName=getString(R.string.c_wenzhou);break;
            case"瑞安市":newCityName=getString(R.string.c_ruian);break;
            case"乐清市":newCityName=getString(R.string.c_leqing);break;
            case"龙港市":newCityName=getString(R.string.c_longgang);break;
            case"嘉兴市":newCityName=getString(R.string.c_jiaxing);break;
            case"海宁市":newCityName=getString(R.string.c_haining);break;
            case"平湖市":newCityName=getString(R.string.c_pinghu);break;
            case"桐乡市":newCityName=getString(R.string.c_tongxiang);break;
            case"湖州市":newCityName=getString(R.string.c_huzhou);break;
            case"绍兴市":newCityName=getString(R.string.c_shaoxing);break;
            case"诸暨市":newCityName=getString(R.string.c_zhuji);break;
            case"嵊州市":newCityName=getString(R.string.c_hengzhou);break;
            case"金华市":newCityName=getString(R.string.c_jinhua);break;
            case"兰溪市":newCityName=getString(R.string.c_lanxi);break;
            case"义乌市":newCityName=getString(R.string.c_yiwu);break;
            case"东阳市":newCityName=getString(R.string.c_dongyang);break;
            case"永康市":newCityName=getString(R.string.c_yongkang);break;
            case"衢州市":newCityName=getString(R.string.c_quzhou);break;
            case"江山市":newCityName=getString(R.string.c_jiangshan);break;
            case"舟山市":newCityName=getString(R.string.c_zhoushan);break;
            case"台州市":newCityName=getString(R.string.c_taizhou1);break;
            case"温岭市":newCityName=getString(R.string.c_wenling);break;
            case"临海市":newCityName=getString(R.string.c_linhai);break;
            case"玉环市":newCityName=getString(R.string.c_yuhuan);break;
            case"丽水市":newCityName=getString(R.string.c_lishui);break;
            case"龙泉市":newCityName=getString(R.string.c_longquan);break;
            case"合肥市":newCityName=getString(R.string.c_hefei);break;
            case"巢湖市":newCityName=getString(R.string.c_chaohu);break;
            case"芜湖市":newCityName=getString(R.string.c_wuhu);break;
            case"无为市":newCityName=getString(R.string.c_wuwei);break;
            case"蚌埠市":newCityName=getString(R.string.c_bangbu);break;
            case"淮南市":newCityName=getString(R.string.c_huainan);break;
            case"马鞍山市":newCityName=getString(R.string.c_maanshan);break;
            case"淮北市":newCityName=getString(R.string.c_huaibei);break;
            case"铜陵市":newCityName=getString(R.string.c_tongling);break;
            case"安庆市":newCityName=getString(R.string.c_anqing);break;
            case"潜山市":newCityName=getString(R.string.c_qianshan);break;
            case"桐城市":newCityName=getString(R.string.c_tongcheng);break;
            case"黄山市":newCityName=getString(R.string.c_huangshan);break;
            case"滁州市":newCityName=getString(R.string.c_chuzhou);break;
            case"天长市":newCityName=getString(R.string.c_tianchang);break;
            case"明光市":newCityName=getString(R.string.c_mingguang);break;
            case"阜阳市":newCityName=getString(R.string.c_fuyang);break;
            case"界首市":newCityName=getString(R.string.c_jieshou);break;
            case"宿州市":newCityName=getString(R.string.c_suzhou1);break;
            case"六安市":newCityName=getString(R.string.c_liuan);break;
            case"亳州市":newCityName=getString(R.string.c_haozhou);break;
            case"池州市":newCityName=getString(R.string.c_chizhou);break;
            case"宣城市":newCityName=getString(R.string.c_xuancheng);break;
            case"广德市":newCityName=getString(R.string.c_guangde);break;
            case"宁国市":newCityName=getString(R.string.c_ningguo);break;
            case"福州市":newCityName=getString(R.string.c_fuzhou);break;
            case"福清市":newCityName=getString(R.string.c_fuqing);break;
            case"厦门市":newCityName=getString(R.string.c_xiamen);break;
            case"莆田市":newCityName=getString(R.string.c_putian);break;
            case"三明市":newCityName=getString(R.string.c_sanming);break;
            case"永安市":newCityName=getString(R.string.c_yongan);break;
            case"泉州市":newCityName=getString(R.string.c_quanzhou);break;
            case"石狮市":newCityName=getString(R.string.c_shishi);break;
            case"晋江市":newCityName=getString(R.string.c_jinjiang);break;
            case"南安市":newCityName=getString(R.string.c_nanan);break;
            case"漳州市":newCityName=getString(R.string.c_zhangzhou);break;
            case"龙海市":newCityName=getString(R.string.c_longhai);break;
            case"南平市":newCityName=getString(R.string.c_nanping);break;
            case"邵武市":newCityName=getString(R.string.c_shaowu);break;
            case"武夷山市":newCityName=getString(R.string.c_wuyishan);break;
            case"建瓯市":newCityName=getString(R.string.c_jianoushi);break;
            case"龙岩市":newCityName=getString(R.string.c_longyan);break;
            case"漳平市":newCityName=getString(R.string.c_zhangping);break;
            case"宁德市":newCityName=getString(R.string.c_ningde);break;
            case"福安市":newCityName=getString(R.string.c_fuan);break;
            case"福鼎市":newCityName=getString(R.string.c_fuding);break;
            case"南昌市":newCityName=getString(R.string.c_nanchang);break;
            case"景德镇市":newCityName=getString(R.string.c_jingdezhen);break;
            case"乐平市":newCityName=getString(R.string.c_leping);break;
            case"萍乡市":newCityName=getString(R.string.c_pingxiang);break;
            case"九江市":newCityName=getString(R.string.c_jiujiang);break;
            case"瑞昌市":newCityName=getString(R.string.c_ruichang);break;
            case"共青城市":newCityName=getString(R.string.c_gongqingcheng);break;
            case"庐山市":newCityName=getString(R.string.c_lushan);break;
            case"新余市":newCityName=getString(R.string.c_xinyu);break;
            case"鹰潭市":newCityName=getString(R.string.c_yingtan);break;
            case"贵溪市":newCityName=getString(R.string.c_guixi);break;
            case"赣州市":newCityName=getString(R.string.c_ganzhou);break;
            case"龙南市":newCityName=getString(R.string.c_longnan);break;
            case"瑞金市":newCityName=getString(R.string.c_ruijin);break;
            case"吉安市":newCityName=getString(R.string.c_jian1);break;
            case"井冈山市":newCityName=getString(R.string.c_jinggangshan);break;
            case"宜春市":newCityName=getString(R.string.c_yichun1);break;
            case"丰城市":newCityName=getString(R.string.c_fengcheng1);break;
            case"樟树市":newCityName=getString(R.string.c_zhangshu);break;
            case"高安市":newCityName=getString(R.string.c_gaoan);break;
            case"抚州市":newCityName=getString(R.string.c_fuzhou1);break;
            case"上饶市":newCityName=getString(R.string.c_shangrao);break;
            case"德兴市":newCityName=getString(R.string.c_dexing);break;
            case"济南市":newCityName=getString(R.string.c_jinan);break;
            case"青岛市":newCityName=getString(R.string.c_qingdao);break;
            case"胶州市":newCityName=getString(R.string.c_jiaozhou);break;
            case"平度市":newCityName=getString(R.string.c_pingdu);break;
            case"莱西市":newCityName=getString(R.string.c_laixi);break;
            case"淄博市":newCityName=getString(R.string.c_zibo);break;
            case"枣庄市":newCityName=getString(R.string.c_zaozhuang);break;
            case"滕州市":newCityName=getString(R.string.c_tengzhou);break;
            case"东营市":newCityName=getString(R.string.c_dongying);break;
            case"烟台市":newCityName=getString(R.string.c_yantai);break;
            case"龙口市":newCityName=getString(R.string.c_longkou);break;
            case"莱阳市":newCityName=getString(R.string.c_laiyang);break;
            case"莱州市":newCityName=getString(R.string.c_laizhou);break;
            case"招远市":newCityName=getString(R.string.c_zhaoyuan);break;
            case"栖霞市":newCityName=getString(R.string.c_qixia);break;
            case"海阳市":newCityName=getString(R.string.c_haiyang);break;
            case"潍坊市":newCityName=getString(R.string.c_weifang);break;
            case"青州市":newCityName=getString(R.string.c_qingzhou);break;
            case"诸城市":newCityName=getString(R.string.c_zhucheng);break;
            case"寿光市":newCityName=getString(R.string.c_shouguang);break;
            case"安丘市":newCityName=getString(R.string.c_anqiu);break;
            case"高密市":newCityName=getString(R.string.c_gaomi);break;
            case"昌邑市":newCityName=getString(R.string.c_changyi);break;
            case"济宁市":newCityName=getString(R.string.c_jining);break;
            case"曲阜市":newCityName=getString(R.string.c_qufu);break;
            case"邹城市":newCityName=getString(R.string.c_zoucheng);break;
            case"泰安市":newCityName=getString(R.string.c_taian);break;
            case"新泰市":newCityName=getString(R.string.c_xintai);break;
            case"肥城市":newCityName=getString(R.string.c_feicheng);break;
            case"威海市":newCityName=getString(R.string.c_weihai);break;
            case"荣成市":newCityName=getString(R.string.c_rongcheng);break;
            case"乳山市":newCityName=getString(R.string.c_rushan);break;
            case"日照市":newCityName=getString(R.string.c_rizhao);break;
            case"临沂市":newCityName=getString(R.string.c_linyi);break;
            case"德州市":newCityName=getString(R.string.c_dezhou);break;
            case"乐陵市":newCityName=getString(R.string.c_leling);break;
            case"禹城市":newCityName=getString(R.string.c_yucheng);break;
            case"聊城市":newCityName=getString(R.string.c_liaocheng);break;
            case"临清市":newCityName=getString(R.string.c_linqing);break;
            case"滨州市":newCityName=getString(R.string.c_binzhou);break;
            case"邹平市":newCityName=getString(R.string.c_zouping);break;
            case"菏泽市":newCityName=getString(R.string.c_heze);break;
            case"郑州市":newCityName=getString(R.string.c_zhengzhou);break;
            case"巩义市":newCityName=getString(R.string.c_gongyi);break;
            case"荥阳市":newCityName=getString(R.string.c_yingyang);break;
            case"新密市":newCityName=getString(R.string.c_xinmi);break;
            case"新郑市":newCityName=getString(R.string.c_xinzheng);break;
            case"登封市":newCityName=getString(R.string.c_dengfeng);break;
            case"开封市":newCityName=getString(R.string.c_kaifeng);break;
            case"洛阳市":newCityName=getString(R.string.c_luoyang);break;
            case"平顶山市":newCityName=getString(R.string.c_pingdingshan);break;
            case"舞钢市":newCityName=getString(R.string.c_wugang);break;
            case"汝州市":newCityName=getString(R.string.c_ruzhou);break;
            case"安阳市":newCityName=getString(R.string.c_anyang);break;
            case"林州市":newCityName=getString(R.string.c_linzhou);break;
            case"鹤壁市":newCityName=getString(R.string.c_hebi);break;
            case"新乡市":newCityName=getString(R.string.c_xinxiang);break;
            case"长垣市":newCityName=getString(R.string.c_changyuan);break;
            case"卫辉市":newCityName=getString(R.string.c_weihui);break;
            case"辉县市":newCityName=getString(R.string.c_huixian);break;
            case"焦作市":newCityName=getString(R.string.c_jiaozuo);break;
            case"沁阳市":newCityName=getString(R.string.c_qinyang);break;
            case"孟州市":newCityName=getString(R.string.c_mengzhou);break;
            case"濮阳市":newCityName=getString(R.string.c_puyang);break;
            case"许昌市":newCityName=getString(R.string.c_xuchang);break;
            case"禹州市":newCityName=getString(R.string.c_yuzhou);break;
            case"长葛市":newCityName=getString(R.string.c_changge);break;
            case"漯河市":newCityName=getString(R.string.c_luohe);break;
            case"三门峡市":newCityName=getString(R.string.c_sanmenxia);break;
            case"义马市":newCityName=getString(R.string.c_yima);break;
            case"灵宝市":newCityName=getString(R.string.c_lingbao);break;
            case"南阳市":newCityName=getString(R.string.c_nanyang);break;
            case"邓州市":newCityName=getString(R.string.c_dengzhou);break;
            case"商丘市":newCityName=getString(R.string.c_shangqiu);break;
            case"永城市":newCityName=getString(R.string.c_yongcheng);break;
            case"信阳市":newCityName=getString(R.string.c_xinyang);break;
            case"周口市":newCityName=getString(R.string.c_zhoukou);break;
            case"项城市":newCityName=getString(R.string.c_xiangcheng);break;
            case"驻马店市":newCityName=getString(R.string.c_zhumadian);break;
            case"济源市":newCityName=getString(R.string.c_jiyuan);break;
            case"武汉市":newCityName=getString(R.string.c_wuhan);break;
            case"黄石市":newCityName=getString(R.string.c_huangshi);break;
            case"大冶市":newCityName=getString(R.string.c_daye);break;
            case"十堰市":newCityName=getString(R.string.c_shiyan);break;
            case"丹江口市":newCityName=getString(R.string.c_danjiangkou);break;
            case"宜昌市":newCityName=getString(R.string.c_yichang);break;
            case"宜都市":newCityName=getString(R.string.c_yidu);break;
            case"当阳市":newCityName=getString(R.string.c_dangyang);break;
            case"枝江市":newCityName=getString(R.string.c_zhijiang);break;
            case"襄阳市":newCityName=getString(R.string.c_xiangyang);break;
            case"老河口市":newCityName=getString(R.string.c_laohekou);break;
            case"枣阳市":newCityName=getString(R.string.c_zaoyang);break;
            case"宜城市":newCityName=getString(R.string.c_yicheng);break;
            case"鄂州市":newCityName=getString(R.string.c_ezhou);break;
            case"荆门市":newCityName=getString(R.string.c_jingmen);break;
            case"京山市":newCityName=getString(R.string.c_jingshan);break;
            case"钟祥市":newCityName=getString(R.string.c_zhongxiang);break;
            case"孝感市":newCityName=getString(R.string.c_xiaogan);break;
            case"应城市":newCityName=getString(R.string.c_yingcheng);break;
            case"安陆市":newCityName=getString(R.string.c_anlu);break;
            case"汉川市":newCityName=getString(R.string.c_hanchuan);break;
            case"荆州市":newCityName=getString(R.string.c_jingzhou);break;
            case"沙市区":newCityName=getString(R.string.c_shaqu);break;
            case"监利市":newCityName=getString(R.string.c_jianli);break;
            case"石首市":newCityName=getString(R.string.c_shishou);break;
            case"洪湖市":newCityName=getString(R.string.c_honghu);break;
            case"松滋市":newCityName=getString(R.string.c_songzi);break;
            case"黄冈市":newCityName=getString(R.string.c_huanggang);break;
            case"麻城市":newCityName=getString(R.string.c_macheng);break;
            case"武穴市":newCityName=getString(R.string.c_wuxue);break;
            case"咸宁市":newCityName=getString(R.string.c_xianning);break;
            case"赤壁市":newCityName=getString(R.string.c_chibi);break;
            case"随州市":newCityName=getString(R.string.c_suizhou);break;
            case"广水市":newCityName=getString(R.string.c_guangshui);break;
            case"恩施市":newCityName=getString(R.string.c_enshi);break;
            case"利川市":newCityName=getString(R.string.c_lichuan);break;
            case"仙桃市":newCityName=getString(R.string.c_xiantao);break;
            case"潜江市":newCityName=getString(R.string.c_qianjiang);break;
            case"天门市":newCityName=getString(R.string.c_tianmen);break;
            case"长沙市":newCityName=getString(R.string.c_changsha);break;
            case"浏阳市":newCityName=getString(R.string.c_liuyang);break;
            case"宁乡市":newCityName=getString(R.string.c_ningxiang);break;
            case"株洲市":newCityName=getString(R.string.c_zhuzhou);break;
            case"醴陵市":newCityName=getString(R.string.c_liling);break;
            case"湘潭市":newCityName=getString(R.string.c_xiangtan);break;
            case"湘乡市":newCityName=getString(R.string.c_xiangxiang);break;
            case"韶山市":newCityName=getString(R.string.c_shaoshan);break;
            case"衡阳市":newCityName=getString(R.string.c_hengyang);break;
            case"耒阳市":newCityName=getString(R.string.c_leiyang);break;
            case"常宁市":newCityName=getString(R.string.c_changning);break;
            case"邵阳市":newCityName=getString(R.string.c_shaoyang);break;
            case"邵东市":newCityName=getString(R.string.c_shaodong);break;
            case"武冈市":newCityName=getString(R.string.c_wugang1);break;
            case"岳阳市":newCityName=getString(R.string.c_yueyang);break;
            case"汨罗市":newCityName=getString(R.string.c_miluo);break;
            case"临湘市":newCityName=getString(R.string.c_linxiang);break;
            case"常德市":newCityName=getString(R.string.c_changde);break;
            case"津市市":newCityName=getString(R.string.c_jin);break;
            case"张家界市":newCityName=getString(R.string.c_zhangjiajie);break;
            case"益阳市":newCityName=getString(R.string.c_yiyang);break;
            case"沅江市":newCityName=getString(R.string.c_yuanjiang);break;
            case"郴州市":newCityName=getString(R.string.c_chenzhou);break;
            case"资兴市":newCityName=getString(R.string.c_zixing);break;
            case"永州市":newCityName=getString(R.string.c_yongzhou);break;
            case"祁阳市":newCityName=getString(R.string.c_qiyang);break;
            case"怀化市":newCityName=getString(R.string.c_huaihua);break;
            case"洪江市":newCityName=getString(R.string.c_hongjiang);break;
            case"娄底市":newCityName=getString(R.string.c_loudi);break;
            case"冷水江市":newCityName=getString(R.string.c_lengshuijiang);break;
            case"涟源市":newCityName=getString(R.string.c_lianyuan);break;
            case"吉首市":newCityName=getString(R.string.c_jishou);break;
            case"广州市":newCityName=getString(R.string.c_guangzhou);break;
            case"韶关市":newCityName=getString(R.string.c_shaoguan);break;
            case"乐昌市":newCityName=getString(R.string.c_lechang);break;
            case"南雄市":newCityName=getString(R.string.c_nanxiong);break;
            case"深圳市":newCityName=getString(R.string.c_shenzhen);break;
            case"珠海市":newCityName=getString(R.string.c_zhuhai);break;
            case"汕头市":newCityName=getString(R.string.c_shantou);break;
            case"佛山市":newCityName=getString(R.string.c_foshan);break;
            case"江门市":newCityName=getString(R.string.c_jiangmen);break;
            case"台山市":newCityName=getString(R.string.c_taishan);break;
            case"开平市":newCityName=getString(R.string.c_kaiping);break;
            case"鹤山市":newCityName=getString(R.string.c_heshan);break;
            case"恩平市":newCityName=getString(R.string.c_enping);break;
            case"湛江市":newCityName=getString(R.string.c_zhanjiang);break;
            case"廉江市":newCityName=getString(R.string.c_lianjiang);break;
            case"雷州市":newCityName=getString(R.string.c_leizhou);break;
            case"吴川市":newCityName=getString(R.string.c_wuchuan);break;
            case"茂名市":newCityName=getString(R.string.c_maoming);break;
            case"高州市":newCityName=getString(R.string.c_gaozhou);break;
            case"化州市":newCityName=getString(R.string.c_huazhou);break;
            case"信宜市":newCityName=getString(R.string.c_xinyi1);break;
            case"肇庆市":newCityName=getString(R.string.c_zhaoqing);break;
            case"四会市":newCityName=getString(R.string.c_sihui);break;
            case"惠州市":newCityName=getString(R.string.c_huizhou);break;
            case"梅州市":newCityName=getString(R.string.c_meizhou);break;
            case"兴宁市":newCityName=getString(R.string.c_xingning);break;
            case"汕尾市":newCityName=getString(R.string.c_shanwei);break;
            case"陆丰市":newCityName=getString(R.string.c_lufeng);break;
            case"河源市":newCityName=getString(R.string.c_heyuan);break;
            case"阳江市":newCityName=getString(R.string.c_yangjiang);break;
            case"阳春市":newCityName=getString(R.string.c_yangchun);break;
            case"清远市":newCityName=getString(R.string.c_qingyuan);break;
            case"英德市":newCityName=getString(R.string.c_yingde);break;
            case"连州市":newCityName=getString(R.string.c_lianzhou);break;
            case"东莞市":newCityName=getString(R.string.c_dongguan);break;
            case"中山市":newCityName=getString(R.string.c_zhongshan);break;
            case"潮州市":newCityName=getString(R.string.c_chaozhou);break;
            case"揭阳市":newCityName=getString(R.string.c_jieyang);break;
            case"普宁市":newCityName=getString(R.string.c_puning);break;
            case"云浮市":newCityName=getString(R.string.c_yunfu);break;
            case"罗定市":newCityName=getString(R.string.c_luoding);break;
            case"南宁市":newCityName=getString(R.string.c_nanning);break;
            case"柳州市":newCityName=getString(R.string.c_liuzhou);break;
            case"桂林市":newCityName=getString(R.string.c_guilin);break;
            case"荔浦市":newCityName=getString(R.string.c_lipu);break;
            case"梧州市":newCityName=getString(R.string.c_wuzhou);break;
            case"岑溪市":newCityName=getString(R.string.c_cenxi);break;
            case"北海市":newCityName=getString(R.string.c_beihai);break;
            case"防城港市":newCityName=getString(R.string.c_fangchenggang);break;
            case"东兴市":newCityName=getString(R.string.c_dongxing);break;
            case"钦州市":newCityName=getString(R.string.c_qinzhou);break;
            case"贵港市":newCityName=getString(R.string.c_guigang);break;
            case"桂平市":newCityName=getString(R.string.c_guiping);break;
            case"玉林市":newCityName=getString(R.string.c_yulin);break;
            case"北流市":newCityName=getString(R.string.c_beiliu);break;
            case"百色市":newCityName=getString(R.string.c_baise);break;
            case"平果市":newCityName=getString(R.string.c_pingguo);break;
            case"靖西市":newCityName=getString(R.string.c_jingxi);break;
            case"贺州市":newCityName=getString(R.string.c_hezhou);break;
            case"河池市":newCityName=getString(R.string.c_hechi);break;
            case"来宾市":newCityName=getString(R.string.c_laibin);break;
            case"合山市":newCityName=getString(R.string.c_heshan1);break;
            case"崇左市":newCityName=getString(R.string.c_chongzuo);break;
            case"凭祥市":newCityName=getString(R.string.c_pingxiang1);break;
            case"海口市":newCityName=getString(R.string.c_haikou);break;
            case"三亚市":newCityName=getString(R.string.c_sanya);break;
            case"三沙市":newCityName=getString(R.string.c_sansha);break;
            case"儋州市":newCityName=getString(R.string.c_danzhou);break;
            case"五指山市":newCityName=getString(R.string.c_wuzhishan);break;
            case"琼海市":newCityName=getString(R.string.c_qionghai);break;
            case"文昌市":newCityName=getString(R.string.c_wenchang);break;
            case"万宁市":newCityName=getString(R.string.c_wanning);break;
            case"东方市":newCityName=getString(R.string.c_dongfang);break;
            case"重庆市":newCityName=getString(R.string.c_zhongqing);break;
            case"成都市":newCityName=getString(R.string.c_chengdong);break;
            case"都江堰市":newCityName=getString(R.string.c_dongjiangyan);break;
            case"彭州市":newCityName=getString(R.string.c_pengzhou);break;
            case"邛崃市":newCityName=getString(R.string.c_qionglai);break;
            case"崇州市":newCityName=getString(R.string.c_chongzhou);break;
            case"简阳市":newCityName=getString(R.string.c_jianyang);break;
            case"自贡市":newCityName=getString(R.string.c_zigong);break;
            case"攀枝花市":newCityName=getString(R.string.c_panzhihua);break;
            case"泸州市":newCityName=getString(R.string.c_luzhou);break;
            case"德阳市":newCityName=getString(R.string.c_deyang);break;
            case"广汉市":newCityName=getString(R.string.c_guanghan);break;
            case"什邡市":newCityName=getString(R.string.c_shifangshi);break;
            case"绵竹市":newCityName=getString(R.string.c_mianzhu);break;
            case"绵阳市":newCityName=getString(R.string.c_mianyang);break;
            case"江油市":newCityName=getString(R.string.c_jiangyou);break;
            case"广元市":newCityName=getString(R.string.c_guangyuan);break;
            case"遂宁市":newCityName=getString(R.string.c_suining);break;
            case"射洪市":newCityName=getString(R.string.c_shehong);break;
            case"内江市":newCityName=getString(R.string.c_neijiang);break;
            case"隆昌市":newCityName=getString(R.string.c_longchang);break;
            case"乐山市":newCityName=getString(R.string.c_leshan);break;
            case"峨眉山市":newCityName=getString(R.string.c_emeishan);break;
            case"南充市":newCityName=getString(R.string.c_nanchong);break;
            case"阆中市":newCityName=getString(R.string.c_langzhong);break;
            case"眉山市":newCityName=getString(R.string.c_meishan);break;
            case"宜宾市":newCityName=getString(R.string.c_yibin);break;
            case"广安市":newCityName=getString(R.string.c_guangan);break;
            case"华蓥市":newCityName=getString(R.string.c_huaying);break;
            case"达州市":newCityName=getString(R.string.c_dazhou);break;
            case"万源市":newCityName=getString(R.string.c_wanyuan);break;
            case"雅安市":newCityName=getString(R.string.c_yaan);break;
            case"巴中市":newCityName=getString(R.string.c_bazhong);break;
            case"资阳市":newCityName=getString(R.string.c_ziyang);break;
            case"马尔康市":newCityName=getString(R.string.c_maerkang);break;
            case"康定市":newCityName=getString(R.string.c_kangding);break;
            case"西昌市":newCityName=getString(R.string.c_xichang);break;
            case"贵阳市":newCityName=getString(R.string.c_guiyang);break;
            case"清镇市":newCityName=getString(R.string.c_qingzhen);break;
            case"六盘水市":newCityName=getString(R.string.c_liupanshui);break;
            case"盘州市":newCityName=getString(R.string.c_panzhou);break;
            case"遵义市":newCityName=getString(R.string.c_zunyi);break;
            case"赤水市":newCityName=getString(R.string.c_chishui);break;
            case"仁怀市":newCityName=getString(R.string.c_renhuai);break;
            case"安顺市":newCityName=getString(R.string.c_anshun);break;
            case"毕节市":newCityName=getString(R.string.c_bijie);break;
            case"黔西市":newCityName=getString(R.string.c_qianxi);break;
            case"铜仁市":newCityName=getString(R.string.c_tongren);break;
            case"兴义市":newCityName=getString(R.string.c_xingyi);break;
            case"兴仁市":newCityName=getString(R.string.c_xingren);break;
            case"凯里市":newCityName=getString(R.string.c_kaili);break;
            case"都匀市":newCityName=getString(R.string.c_dongyun);break;
            case"福泉市":newCityName=getString(R.string.c_fuquan);break;
            case"昆明市":newCityName=getString(R.string.c_kunming);break;
            case"安宁市":newCityName=getString(R.string.c_anning);break;
            case"曲靖市":newCityName=getString(R.string.c_qujing);break;
            case"宣威市":newCityName=getString(R.string.c_xuanwei);break;
            case"玉溪市":newCityName=getString(R.string.c_yuxi);break;
            case"澄江市":newCityName=getString(R.string.c_chengjiang);break;
            case"保山市":newCityName=getString(R.string.c_baoshan);break;
            case"腾冲市":newCityName=getString(R.string.c_tengchong);break;
            case"昭通市":newCityName=getString(R.string.c_zhaotong);break;
            case"水富市":newCityName=getString(R.string.c_shuifu);break;
            case"丽江市":newCityName=getString(R.string.c_lijiang);break;
            case"普洱市":newCityName=getString(R.string.c_pudong);break;
            case"临沧市":newCityName=getString(R.string.c_lincang);break;
            case"楚雄市":newCityName=getString(R.string.c_chuxiong);break;
            case"个旧市":newCityName=getString(R.string.c_gejiu);break;
            case"开远市":newCityName=getString(R.string.c_kaiyuan1);break;
            case"蒙自市":newCityName=getString(R.string.c_mengzi);break;
            case"弥勒市":newCityName=getString(R.string.c_mile);break;
            case"文山市":newCityName=getString(R.string.c_wenshan);break;
            case"景洪市":newCityName=getString(R.string.c_jinghong);break;
            case"大理市":newCityName=getString(R.string.c_dali);break;
            case"瑞丽市":newCityName=getString(R.string.c_ruili);break;
            case"芒市":newCityName=getString(R.string.c_mang);break;
            case"泸水市":newCityName=getString(R.string.c_lushui);break;
            case"香格里拉市":newCityName=getString(R.string.c_xianggelila);break;
            case"拉萨市":newCityName=getString(R.string.c_lasa);break;
            case"日喀则市":newCityName=getString(R.string.c_rikaze);break;
            case"昌都市":newCityName=getString(R.string.c_changdu);break;
            case"林芝市":newCityName=getString(R.string.c_linzhi);break;
            case"山南市":newCityName=getString(R.string.c_shannan);break;
            case"那曲市":newCityName=getString(R.string.c_naqu);break;
            case"西安市":newCityName=getString(R.string.c_xian);break;
            case"铜川市":newCityName=getString(R.string.c_tongchuan);break;
            case"宝鸡市":newCityName=getString(R.string.c_baoji);break;
            case"咸阳市":newCityName=getString(R.string.c_xianyang);break;
            case"彬州市":newCityName=getString(R.string.c_binzhou1);break;
            case"兴平市":newCityName=getString(R.string.c_xingping);break;
            case"渭南市":newCityName=getString(R.string.c_weinan);break;
            case"韩城市":newCityName=getString(R.string.c_hancheng);break;
            case"华阴市":newCityName=getString(R.string.c_huayin);break;
            case"延安市":newCityName=getString(R.string.c_yanan);break;
            case"子长市":newCityName=getString(R.string.c_zichang);break;
            case"汉中市":newCityName=getString(R.string.c_hanzhong);break;
            case"榆林市":newCityName=getString(R.string.c_yulin1);break;
            case"神木市":newCityName=getString(R.string.c_shenmu);break;
            case"安康市":newCityName=getString(R.string.c_ankang);break;
            case"商洛市":newCityName=getString(R.string.c_shangluo);break;
            case"兰州市":newCityName=getString(R.string.c_lanzhou);break;
            case"嘉峪关市":newCityName=getString(R.string.c_jiayuguan);break;
            case"金昌市":newCityName=getString(R.string.c_jinchang);break;
            case"白银市":newCityName=getString(R.string.c_baiyin);break;
            case"天水市":newCityName=getString(R.string.c_tianshui);break;
            case"武威市":newCityName=getString(R.string.c_wuwei1);break;
            case"张掖市":newCityName=getString(R.string.c_zhangye);break;
            case"平凉市":newCityName=getString(R.string.c_pingliang);break;
            case"华亭市":newCityName=getString(R.string.c_huating);break;
            case"酒泉市":newCityName=getString(R.string.c_jiuquan);break;
            case"玉门市":newCityName=getString(R.string.c_yumen);break;
            case"敦煌市":newCityName=getString(R.string.c_dunhuang);break;
            case"庆阳市":newCityName=getString(R.string.c_qingyang);break;
            case"定西市":newCityName=getString(R.string.c_dingxi);break;
            case"陇南市":newCityName=getString(R.string.c_longnan1);break;
            case"临夏市":newCityName=getString(R.string.c_linxia);break;
            case"合作市":newCityName=getString(R.string.c_hezuo);break;
            case"西宁市":newCityName=getString(R.string.c_xining);break;
            case"海东市":newCityName=getString(R.string.c_haidong);break;
            case"同仁市":newCityName=getString(R.string.c_tongren1);break;
            case"玉树市":newCityName=getString(R.string.c_yushu1);break;
            case"格尔木市":newCityName=getString(R.string.c_geermu);break;
            case"德令哈市":newCityName=getString(R.string.c_delingha);break;
            case"茫崖市":newCityName=getString(R.string.c_mangya);break;
            case"银川市":newCityName=getString(R.string.c_yinchuan);break;
            case"灵武市":newCityName=getString(R.string.c_lingwu);break;
            case"石嘴山市":newCityName=getString(R.string.c_shizuishan);break;
            case"吴忠市":newCityName=getString(R.string.c_wuzhong);break;
            case"青铜峡市":newCityName=getString(R.string.c_qingtongxia);break;
            case"固原市":newCityName=getString(R.string.c_guyuan);break;
            case"中卫市":newCityName=getString(R.string.c_zhongwei);break;
            case"乌鲁木齐市":newCityName=getString(R.string.c_wulumuqi);break;
            case"新市区":newCityName=getString(R.string.c_xinshi);break;
            case"克拉玛依市":newCityName=getString(R.string.c_kelamayi);break;
            case"吐鲁番市":newCityName=getString(R.string.c_tulufan);break;
            case"胡杨河市":newCityName=getString(R.string.c_huyanghe);break;
            case"哈密市":newCityName=getString(R.string.c_hami);break;
            case"昌吉市":newCityName=getString(R.string.c_changji);break;
            case"阜康市":newCityName=getString(R.string.c_fukang);break;
            case"博乐市":newCityName=getString(R.string.c_bole);break;
            case"阿拉山口市":newCityName=getString(R.string.c_alashankou);break;
            case"库尔勒市":newCityName=getString(R.string.c_kuerle);break;
            case"阿克苏市":newCityName=getString(R.string.c_akesu);break;
            case"库车市":newCityName=getString(R.string.c_kuche);break;
            case"阿图什市":newCityName=getString(R.string.c_atushi);break;
            case"喀什市":newCityName=getString(R.string.c_kashi);break;
            case"和田市":newCityName=getString(R.string.c_hetian);break;
            case"伊宁市":newCityName=getString(R.string.c_yining);break;
            case"奎屯市":newCityName=getString(R.string.c_kuitun);break;
            case"霍尔果斯市":newCityName=getString(R.string.c_huoerguosi);break;
            case"塔城市":newCityName=getString(R.string.c_tacheng);break;
            case"乌苏市":newCityName=getString(R.string.c_wusu);break;
            case"阿勒泰市":newCityName=getString(R.string.c_aletai);break;
            case"石河子市":newCityName=getString(R.string.c_shihezi);break;
            case"阿拉尔市":newCityName=getString(R.string.c_alaer);break;
            case"图木舒克市":newCityName=getString(R.string.c_tumushuke);break;
            case"五家渠市":newCityName=getString(R.string.c_wujiaqu);break;
            case"北屯市":newCityName=getString(R.string.c_beitun);break;
            case"铁门关市":newCityName=getString(R.string.c_tiemenguan);break;
            case"双河市":newCityName=getString(R.string.c_shuanghe);break;
            case"可克达拉市":newCityName=getString(R.string.c_kekedala);break;
            case"昆玉市":newCityName=getString(R.string.c_kunyu);break;
            default: newCityName=getString(R.string.w_unknown);break;
        }
        return newCityName;
    }
    /*
    * banlap: 高德地图 天气字典翻译
    * */
    private String changeWeatherLanguage(String localWeatherLive) {
        String newWeather="";
        switch(localWeatherLive) {
            case "晴": newWeather = getString(R.string.w_clear); break;
            case "少云": newWeather = getString(R.string.w_briefly_cloudy); break;
            case "晴间多云": newWeather = getString(R.string.w_partly_cloudy); break;
            case "多云": newWeather = getString(R.string.w_cloudy); break;
            case "阴": newWeather = getString(R.string.w_overcast); break;
            case "有风": newWeather = getString(R.string.w_windy); break;
            case "平静": newWeather = getString(R.string.w_calm); break;
            case "微风": newWeather = getString(R.string.w_light_breeze); break;
            case "和风": newWeather = getString(R.string.w_gentle_breeze); break;
            case "清风": newWeather = getString(R.string.w_fresh_breeze); break;
            case "强风/劲风": newWeather = getString(R.string.w_strong_breeze); break;
            case "疾风": newWeather = getString(R.string.w_fresh_gale); break;
            case "大风": newWeather = getString(R.string.w_moderate_gale); break;
            case "烈风": newWeather = getString(R.string.w_whole_gale); break;
            case "风暴": newWeather = getString(R.string.w_storm); break;
            case "狂爆风": newWeather = getString(R.string.w_violent_wind); break;
            case "飓风": newWeather = getString(R.string.w_hurricane); break;
            case "热带风暴": newWeather = getString(R.string.w_tropical_storm); break;
            case "霾": newWeather = getString(R.string.w_haze); break;
            case "中度霾": newWeather = getString(R.string.w_moderate_haze); break;
            case "重度霾": newWeather = getString(R.string.w_heavy_haze); break;
            case "严重霾": newWeather = getString(R.string.w_severe_haze); break;
            case "阵雨": newWeather = getString(R.string.w_showers); break;
            case "雷阵雨": newWeather = getString(R.string.w_thunder_shower); break;
            case "雷阵雨并伴有冰雹": newWeather = getString(R.string.w_thunder_showers_accompanied_by_hail); break;
            case "小雨": newWeather = getString(R.string.w_light_rain); break;
            case "中雨": newWeather = getString(R.string.w_moderate_rain); break;
            case "大雨": newWeather = getString(R.string.w_heavy_rain); break;
            case "暴雨": newWeather = getString(R.string.w_rainstorm); break;
            case "大暴雨": newWeather = getString(R.string.w_heavy_rainstorm); break;
            case "特大暴雨": newWeather = getString(R.string.w_extreme_rainstorm); break;
            case "强阵雨": newWeather = getString(R.string.w_strong_showers); break;
            case "强雷阵雨": newWeather = getString(R.string.w_strong_thundershowers); break;
            case "极端降雨": newWeather = getString(R.string.w_extreme_rainfall); break;
            case "毛毛雨/细雨": newWeather = getString(R.string.w_drizzle); break;
            case "雨": newWeather = getString(R.string.w_rain); break;
            case "小雨-中雨": newWeather = getString(R.string.w_light_rain_moderate_rain); break;
            case "中雨-大雨": newWeather = getString(R.string.w_moderate_rain_heavy_rain); break;
            case "大雨-暴雨": newWeather = getString(R.string.w_heavy_rain_heavy_rain); break;
            case "暴雨-大暴雨": newWeather = getString(R.string.w_torrential_rain); break;
            case "大暴雨-特大暴雨": newWeather = getString(R.string.w_heavy_rain_extra_heavy_rain); break;
            case "雨雪天气": newWeather = getString(R.string.w_rain_and_snow); break;
            case "雨夹雪": newWeather = getString(R.string.w_sleet); break;
            case "阵雨夹雪": newWeather = getString(R.string.w_rain_and_snow); break;
            case "冻雨": newWeather = getString(R.string.w_freezing_rain); break;
            case "雪": newWeather = getString(R.string.w_snow); break;
            case "阵雪": newWeather = getString(R.string.w_snow_showers); break;
            case "小雪": newWeather = getString(R.string.w_light_snow); break;
            case "中雪": newWeather = getString(R.string.w_moderate_snow); break;
            case "大雪": newWeather = getString(R.string.w_heavy_snow); break;
            case "暴雪": newWeather = getString(R.string.w_torrential_snow); break;
            case "小雪-中雪": newWeather = getString(R.string.w_light_snow_moderate_snow); break;
            case "中雪-大雪": newWeather = getString(R.string.w_moderate_snow_heavy_snow); break;
            case "大雪-暴雪": newWeather = getString(R.string.w_heavy_snow_torrential_snow); break;
            case "浮尘": newWeather = getString(R.string.w_floating_dust); break;
            case "扬沙": newWeather = getString(R.string.w_blowing_sand); break;
            case "沙尘暴": newWeather = getString(R.string.w_sandstorm); break;
            case "强沙尘暴": newWeather = getString(R.string.w_strong_sandstorm); break;
            case "龙卷风": newWeather = getString(R.string.w_tornado); break;
            case "雾": newWeather = getString(R.string.w_fog); break;
            case "浓雾": newWeather = getString(R.string.w_dense_fog); break;
            case "强浓雾": newWeather = getString(R.string.w_strong_fog); break;
            case "轻雾": newWeather = getString(R.string.w_mist); break;
            case "大雾": newWeather = getString(R.string.w_heavy_fog); break;
            case "特强浓雾": newWeather = getString(R.string.w_extremely_strong_fog); break;
            case "热": newWeather = getString(R.string.w_hot); break;
            case "冷": newWeather = getString(R.string.w_cold); break;
            default: newWeather = getString(R.string.w_unknown); break;

        }
        return newWeather;
    }
    /*
     * banlap: 高德地图 风向字典翻译
     * */
    private String changeWindLanguage(String windDirection) {
        String newWind="";
        switch(windDirection) {
            case "东": newWind=getString(R.string.w_east); break;
            case "南": newWind=getString(R.string.w_south); break;
            case "西": newWind=getString(R.string.w_west); break;
            case "北": newWind=getString(R.string.w_north); break;
            case "东北": newWind=getString(R.string.w_northeast); break;
            case "东南": newWind=getString(R.string.w_southeast); break;
            case "西南": newWind=getString(R.string.w_southwest); break;
            case "西北": newWind=getString(R.string.w_northwest); break;
            case "旋转不定": newWind=getString(R.string.w_uncertainty); break;
            default: newWind=getString(R.string.w_no_wind_direction); break;
        }
        return newWind;
    }

    public void showAndHideFragmentByTag(String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment.getTag() != null && fragment.getTag().equals(tag)) {
                transaction.show(fragment);
                TabLayout.Tab tab = getViewDataBind().mainTab.getTabAt(Integer.parseInt(tag));
                getViewDataBind().mainTab.selectTab(tab);
            } else {
                transaction.hide(fragment);
            }
        }
        transaction.commitAllowingStateLoss();
    }

    public void goRefreshRegion() {
        getViewModel().getAllData();
    }

    public void goRefreshEquip() {
        getViewModel().getUserAllEquip();
    }

    private void goCheckPermission() {

        if (VcomSingleton.getInstance().isUserLogin()) {
            return;
        }

        if (!isLoginSuccess.get()) {
            return;
        }

        VcomSingleton.getInstance().isBleReady.set(Util.isBleOpen());
        VcomSingleton.getInstance().isGpsReady.set(Util.isGpsOPen(this));
        VcomSingleton.getInstance().isLocationReady.set(Util.isLocationPermissionReady(VcomApp.getApp()));

        boolean isBleReady = VcomSingleton.getInstance().isBleReady.get();
        boolean isGpsReady = VcomSingleton.getInstance().isGpsReady.get();
        boolean isPermissionReady = VcomSingleton.getInstance().isLocationReady.get();

        if (!isBleReady || !isGpsReady || !isPermissionReady) {
            showRequestPermissionDialog();
        } else {
            Util.openBle(this);
            //banlap: 发现bug 使用下面自动连接方法会出现首次登录成功后报错bug，禁用后目前没任何影响
            //new Handler().postDelayed(() -> getViewModel().startAutoConnect(), 300);
        }
    }

    private void showRequestPermissionDialog() {
        if (!isForeground) {
            return;
        }

        if (request == null) {
            request = new RequestPermissionDialog();
        }
        if (request.isVisible()) {
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (!request.isAdded()) {
            transaction.add(request, "8");
        }
        transaction.show(request);
        transaction.commitAllowingStateLoss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.msgCode) {
            case MessageEvent.permissionCode:
                if (request != null) {
                    request.refreshParams();
                }
                if (VcomSingleton.getInstance().isBleReady.get() && VcomSingleton.getInstance().isGpsReady.get()
                        && VcomSingleton.getInstance().isLocationReady.get()) {
                    if (request != null && request.isVisible()) {
                        request.dismissAllowingStateLoss();
                    }
                } else {
                    showRequestPermissionDialog();
                }
                break;
            case MessageEvent.refreshRegion:
                goRefreshRegion();
                break;
            case MessageEvent.switchFragment:
                showAndHideFragmentByTag(event.msg);
                break;
            case MessageEvent.refreshEquip:
                goRefreshEquip();
                break;
            case MessageEvent.equipReady:
                new Handler().postDelayed(() -> getViewModel().startAutoConnect(), 300);
                break;
        }
    }

    @Override
    public void validateFailure() {
        /*
         * banlap: 调试 无网络无账号登录主界面（之后需要恢复原样）
         * */
        Toast.makeText(this,getString(R.string.toast_authentication_error),Toast.LENGTH_SHORT).show();
        isLoginSuccess.set(false);
        Intent goLogin = new Intent(this, LoginActivity.class);
        startActivity(goLogin);
        ActivityUtil.finishAllActivity();

        //调试
       /* isLoginSuccess.set(true);
        goCheckPermission();
        getViewModel().getProductInfo();
        //getViewModel().getAllDevType();
        getViewModel().getDefaultMac();
        getViewModel().getUserAllEquip();
        EventBus.getDefault().post(new MessageEvent(MessageEvent.loginSuccess));*/
    }

    @Override
    public void validateSuccess() {
        Toast.makeText(this,getString(R.string.toast_authentication_success),Toast.LENGTH_SHORT).show();
        isLoginSuccess.set(true);
        goCheckPermission();
        getViewModel().getProductInfo();
        //getViewModel().getAllDevType();
        getViewModel().getDefaultMac();
        getViewModel().getUserAllEquip();
        EventBus.getDefault().post(new MessageEvent(MessageEvent.loginSuccess));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}