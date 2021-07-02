package com.vcom.smartlight.singleton;

import com.vcom.smartlight.model.DefaultMac;
import com.vcom.smartlight.model.DeviceType;
import com.vcom.smartlight.model.Equip;
import com.vcom.smartlight.model.Product;
import com.vcom.smartlight.model.Region;
import com.vcom.smartlight.model.Scene;
import com.vcom.smartlight.model.Timing;
import com.vcom.smartlight.model.User;
import com.vcom.smartlight.model.Weather;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class VcomSingleton {

    public AtomicBoolean isBleReady = new AtomicBoolean(false);
    public AtomicBoolean isGpsReady = new AtomicBoolean(false);
    public AtomicBoolean isLocationReady = new AtomicBoolean(false);

    private User loginUser = new User();
    private Weather weather = new Weather();

    private List<Region> userRegion = new CopyOnWriteArrayList<>();
    private List<Scene> userScene = new CopyOnWriteArrayList<>();
    private List<Product> userProduct = new CopyOnWriteArrayList<>();
    private List<DeviceType> deviceTypes = new CopyOnWriteArrayList<>();
    private List<DefaultMac> defaultMacs = new CopyOnWriteArrayList<>();
    private List<Equip> userEquips = new CopyOnWriteArrayList<>();
    private List<Timing> timingList = new CopyOnWriteArrayList<>();

    private List<Equip> currentSceneEquips = new CopyOnWriteArrayList<>();

    private Region userCurrentRegion = new Region();

    //
    private String AMapCity="";
    private String AMapWeather="";
    private String AMapTemp="";
    private String AMapWind="";

    private static final VcomSingleton ourInstance = new VcomSingleton();

    public static VcomSingleton getInstance() {
        return ourInstance;
    }

    private VcomSingleton() {

    }

    public String getAMapCity() {
        return AMapCity;
    }

    public void setAMapCity(String AMapCity) {
        this.AMapCity = AMapCity;
    }

    public String getAMapWeather() {
        return AMapWeather;
    }

    public void setAMapWeather(String AMapWeather) {
        this.AMapWeather = AMapWeather;
    }

    public String getAMapTemp() {
        return AMapTemp;
    }

    public void setAMapTemp(String AMapTemp) {
        this.AMapTemp = AMapTemp;
    }

    public String getAMapWind() {
        return AMapWind;
    }

    public void setAMapWind(String AMapWind) {
        this.AMapWind = AMapWind;
    }

    public void setWeather(Weather weather) {this.weather = weather;}

    public Weather getWeather() { return  weather;}

    public void setLoginUser(User loginUser) {
        this.loginUser = loginUser;
    }

    public User getLoginUser() {
        return loginUser;
    }

    public boolean isUserLogin() {
        return loginUser.isEmpty();
    }

    public List<Region> getUserRegion() {
        return userRegion;
    }

    public void setUserRegion(List<Region> userRegion) {
        this.userRegion = userRegion;
    }

    public Region getUserCurrentRegion() {
        return userCurrentRegion;
    }

    public void setUserCurrentRegion(Region userCurrentRegion) {
        this.userCurrentRegion = userCurrentRegion;
    }

    public List<Scene> getUserScene() {
        return userScene;
    }

    public void setUserScene(List<Scene> userScene) {
        this.userScene = userScene;
    }


    public List<Product> getUserProduct() {
        return userProduct;
    }

    public void setUserProduct(List<Product> userProduct) {
        this.userProduct = userProduct;
    }

    public List<DeviceType> getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(List<DeviceType> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    public List<DefaultMac> getDefaultMacs() {
        return defaultMacs;
    }

    public void setDefaultMacs(List<DefaultMac> defaultMacs) {
        this.defaultMacs = defaultMacs;
    }

    public List<Equip> getUserEquips() {
        return userEquips;
    }

    public void setUserEquips(List<Equip> userEquips) {
        this.userEquips = userEquips;
    }

    public List<Timing> getTimingList() {
        return timingList;
    }

    public void setTimingList(List<Timing> timingList) {
        this.timingList = timingList;
    }

    public List<Equip> getCurrentSceneEquips() {
        return currentSceneEquips;
    }

    public void setCurrentSceneEquips(List<Equip> currentSceneEquips) {
        this.currentSceneEquips = currentSceneEquips;
    }
}
