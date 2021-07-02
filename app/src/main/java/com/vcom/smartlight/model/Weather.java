package com.vcom.smartlight.model;

/**
 * @author Banlap on 2021/2/25
 */
public class Weather {
    private String cityId;      //城市ID
    private String date;        //当前日期
    private String week;        //当前星期
    private String updateTime;  //气象台更新时间
    private String city;        //城市名称
    private String cityEn;      //城市英文名称
    private String country;     //国家名称
    private String countryEn;   //国家英文名称
    private String wea;         //天气情况
    private String weaImg;      //天气对应图标
    private String tem;         //实时温度
    private String tem1;        //高温
    private String tem2;        //低温
    private String win;         //风向
    private String winSpeed;    //风力等级
    private String winMeter;    //风速
    private String humidity;    //湿度
    private String pressure;    //气压hPa
    private String air;         //空气质量
    private String airPm25;
    private String air_level;    //空气质量等级
    private String airTips;     //空气质量描述

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityEn() {
        return cityEn;
    }

    public void setCityEn(String cityEn) {
        this.cityEn = cityEn;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryEn() {
        return countryEn;
    }

    public void setCountryEn(String countryEn) {
        this.countryEn = countryEn;
    }

    public String getWea() {
        return wea;
    }

    public void setWea(String wea) {
        this.wea = wea;
    }

    public String getWeaImg() {
        return weaImg;
    }

    public void setWeaImg(String weaImg) {
        this.weaImg = weaImg;
    }

    public String getTem() {
        return tem;
    }

    public void setTem(String tem) {
        this.tem = tem;
    }

    public String getTem1() {
        return tem1;
    }

    public void setTem1(String tem1) {
        this.tem1 = tem1;
    }

    public String getTem2() {
        return tem2;
    }

    public void setTem2(String tem2) {
        this.tem2 = tem2;
    }

    public String getWin() {
        return win;
    }

    public void setWin(String win) {
        this.win = win;
    }

    public String getWinSpeed() {
        return winSpeed;
    }

    public void setWinSpeed(String winSpeed) {
        this.winSpeed = winSpeed;
    }

    public String getWinMeter() {
        return winMeter;
    }

    public void setWinMeter(String winMeter) {
        this.winMeter = winMeter;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getAir() {
        return air;
    }

    public void setAir(String air) {
        this.air = air;
    }

    public String getAirPm25() {
        return airPm25;
    }

    public void setAirPm25(String airPm25) {
        this.airPm25 = airPm25;
    }

    public String getAir_level() {
        return air_level;
    }

    public void setAir_level(String air_level) {
        this.air_level = air_level;
    }

    public String getAirTips() {
        return airTips;
    }

    public void setAirTips(String airTips) {
        this.airTips = airTips;
    }
}
