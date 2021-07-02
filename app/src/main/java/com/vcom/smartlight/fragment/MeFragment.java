package com.vcom.smartlight.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;

import com.vcom.smartlight.R;
import com.vcom.smartlight.base.BaseMvvmFragment;
import com.vcom.smartlight.databinding.FragmentMeBinding;
import com.vcom.smartlight.fvm.MeFVM;
import com.vcom.smartlight.model.MessageEvent;
import com.vcom.smartlight.model.Weather;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.ui.AccountSettingsActivity;
import com.vcom.smartlight.ui.AppHelpActivity;
import com.vcom.smartlight.ui.DocActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;
import java.util.Objects;

/**
 * @Author Lzz
 * @Date 2020/10/27 18:44
 */
public class MeFragment extends BaseMvvmFragment<MeFVM, FragmentMeBinding> implements MeFVM.MeFvmCallBack {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_me;
    }

    @Override
    protected void afterCreate() {

    }

    @Override
    protected void initViews() {
        Typeface iconView = Typeface.createFromAsset(requireActivity().getAssets(), "fonts/iconfont.ttf");
        getViewDataBind().tvIconUserAgreement.setTypeface(iconView);
        getViewDataBind().tvIconPrivacyPolicy.setTypeface(iconView);
        getViewDataBind().tvIconHelpCenter.setTypeface(iconView);
        getViewDataBind().tvIconAboutVcom.setTypeface(iconView);
        getViewDataBind().tvIconFaqs.setTypeface(iconView);

    }

    @Override
    protected void initDatum() {
        getViewDataBind().setVm(getViewModel());
        EventBus.getDefault().register(this);
        getViewModel().setCallBack(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (VcomSingleton.getInstance().getLoginUser().isEmpty()){
            return;
        }
        getViewDataBind().mainFragmentUserName.setText(VcomSingleton.getInstance().getLoginUser().getUserName());
        getViewDataBind().mainFragmentUserId.setText(String.format(getString(R.string.user_id), VcomSingleton.getInstance().getLoginUser().getUserId()));
        EventBus.getDefault().post(new MessageEvent(MessageEvent.weatherReady));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.msgCode) {
            case MessageEvent.loginSuccess:
                getViewDataBind().mainFragmentUserName.setText(VcomSingleton.getInstance().getLoginUser().getUserName());
                getViewDataBind().mainFragmentUserId.setText(String.format(getString(R.string.user_id), VcomSingleton.getInstance().getLoginUser().getUserId()));
                break;
            case MessageEvent.weatherReady:
                //banlap: 获取当前位置和天气信息 高德地图sdk
                getAMapLocationAndWeather();
                //Weather weather = VcomSingleton.getInstance().getWeather();
                //loadWeatherData(weather);
                break;
        }

    }

    /*
     * banlap: 获取当前位置和天气
     * */
    @SuppressLint("SetTextI18n")
    private void getAMapLocationAndWeather() {
        getViewDataBind().mainFragmentUserId.setText(
                VcomSingleton.getInstance().getAMapTemp() + "℃ " +
                VcomSingleton.getInstance().getAMapCity());
    }

    /*
    * banlap: 显示天气参数
    * */
    @SuppressLint("SetTextI18n")
    private void loadWeatherData(Weather weather) {
        //banlap: 获取当前系统语言;
        String localeLanguage = Locale.getDefault().getLanguage();
        //banlap: 显示当前天气状况
        if (localeLanguage.equals("en")) {
            getViewDataBind().mainFragmentUserId.setText(weather.getTem()+"°C  "+weather.getCityEn());
        } else {
            getViewDataBind().mainFragmentUserId.setText(weather.getTem()+"°C  "+weather.getCity());
        }
    }

    @Override
    public void viewGoWeb(String tag) {
        Intent intent = new Intent(getActivity(), DocActivity.class);
        intent.putExtra("webTag", tag);
        startActivity(intent);
    }

    @Override
    public void viewGoAppHelp(){
        Intent intent = new Intent(getActivity(), AppHelpActivity.class);
        intent.putExtra("Advance", true);
        startActivity(intent);
    }
    @Override
    public void viewGoAccountSettings() {
        Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
        startActivity(intent);
    }


}
