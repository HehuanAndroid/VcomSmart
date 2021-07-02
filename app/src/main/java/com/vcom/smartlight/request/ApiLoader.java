package com.vcom.smartlight.request;

import android.content.Context;

import com.google.gson.Gson;
import com.vcom.smartlight.model.Equip;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class ApiLoader extends ObjectLoader {

    public Context context;

    private static ApiService apiService = null;
    private static ApiService newApiService = null;

    private static final ApiLoader api = new ApiLoader();

    public static ApiLoader getApi() {
        return api;
    }
    public ApiLoader() {
        apiService = RetrofitServiceManager.getInstance().create(ApiService.class);
    }

    public static ApiLoader getApi2(Context context) {
        return new ApiLoader(context);
    }
    public ApiLoader(Context context) {
        this.context = context;
        apiService = RetrofitServiceManager.getInstanceContext(context).create(ApiService.class);
    }

    //banlap: 天气api获取天气信息
    public void getWeather(String version, String appId, String appSecret, Observer<ResponseBody> observer){
        newApiService = RetrofitServiceManager.getInstanceWeather().create(ApiService.class);
        setSubscribe(newApiService.getWeather(version, appId, appSecret), observer);
    }

    //banlap: 后台获取天气信息
    public void getNewWeather(String cityId, Observer<ResponseBody> observer){
        setSubscribe(apiService.getNewWeather(cityId), observer);
    }

    //banlap: 登录接口
    public void userLogin(Map<String, Object> params, Observer<ResponseBody> observer) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.Companion.create(new Gson().toJson(params), mediaType);
        setSubscribe(apiService.userLogin(body), observer);
    }
    //banlap: 认证加密的密码接口
    public void validateLogin(Map<String, Object> params, Observer<ResponseBody> observer) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.Companion.create(new Gson().toJson(params), mediaType);
        setSubscribe(apiService.validateLogin(body), observer);
    }

    //banlap: 更新用户信息 - 密码接口
    public void updateUser(Map<String, Object> params, Observer<ResponseBody> observer) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.Companion.create(new Gson().toJson(params), mediaType);
        setSubscribe(apiService.updateUser(body), observer);
    }

    public void queryRegion(String userId, Observer<ResponseBody> observer) {
        setSubscribe(apiService.queryRegion(userId), observer);
    }

    public void regionalManagement(String userId, String spaceName, Observer<ResponseBody> observer) {
        setSubscribe(apiService.regionalManagement(userId, spaceName), observer);
    }

    //banlap: 删除区域接口
    public void deleteArea(String userId, String spaceId, Observer<ResponseBody> observer) {
        setSubscribe(apiService.deleteArea(userId, spaceId), observer);
    }

    //banlap: 修改区域接口
    public void modifyArea(String userId, String spaceId, String spaceName, String spaceImg, Observer<ResponseBody> observer) {
        setSubscribe(apiService.modifyArea(userId, spaceId, spaceName, spaceImg), observer);
    }

    public void addEquipForScene(String sceneId, String equips, Observer<ResponseBody> observer) {
        setSubscribe(apiService.addEquipForScene(sceneId, equips), observer);
    }

    public void addScene(String userId, String spaceId, String sceneName, String sceneImg, Observer<ResponseBody> observer) {
        setSubscribe(apiService.addScene(userId, spaceId, sceneName, sceneImg), observer);
    }
    //banlap: 删除场景接口
    public void deleteScene(String sceneId, Observer<ResponseBody> observer) {
        setSubscribe(apiService.deleteScene(sceneId), observer);
    }

    public void selectListScene(String spaceId, Observer<ResponseBody> observer) {
        setSubscribe(apiService.selectListScene(spaceId), observer);
    }

    //banlap: 新增区域接口
    public void addRegionList(Map<String, Object> params, Observer<ResponseBody> observer) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.Companion.create(new Gson().toJson(params), mediaType);
        setSubscribe(apiService.addRegionList(body), observer);
    }

    //banlap: 新增场景接口
    public void addSceneList(Map<String, Object> params, Observer<ResponseBody> observer) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.Companion.create(new Gson().toJson(params), mediaType);
        setSubscribe(apiService.addSceneList(body), observer);
    }

    //banlap: 查询所有区域场景信息接口
    public void selectAllData(String userID, Observer<ResponseBody> observer) {
        setSubscribe(apiService.selectAllData(userID), observer);
    }

    //banlap: 查询所有设备信息接口
    public void getProductInfo(Observer<ResponseBody> observer) {
        setSubscribe(apiService.getProductInfo(), observer);
    }

    public void getDefaultMac(String userId, Observer<ResponseBody> observer) {
        setSubscribe(apiService.getDefaultMac(userId), observer);
    }

    public void getAllDevType(Observer<ResponseBody> observer) {
        setSubscribe(apiService.getAllDevType(), observer);
    }

    //banlap: 查询用户绑定的设备信息接口
    public void getAllEquips(String userId, Observer<ResponseBody> observer) {
        setSubscribe(apiService.getAllEquips(userId), observer);
    }

    //banlap: 绑定设备接口
    public void addSingleEquip(Equip equip, Observer<ResponseBody> observer) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.Companion.create(new Gson().toJson(equip), mediaType);
        setSubscribe(apiService.addSingleEquip(body), observer);
    }

    //banlap: 解绑设备接口
    public void deleteEquip(String userId, String equipId, Observer<ResponseBody> observer) {
        setSubscribe(apiService.deleteEquip(userId, equipId), observer);
    }

    //banlap: 添加设备到场景接口
    public void addSceneEquip(String data, Observer<ResponseBody> observer) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.Companion.create(data, mediaType);
        setSubscribe(apiService.addSceneEquip(body), observer);
    }

    public void selectRecord(String userId, String sceneId, String equipId, Observer<ResponseBody> observer) {
        setSubscribe(apiService.selectRecord(userId, sceneId, equipId), observer);
    }

    //banlap: 删除场景里面的设备接口
    public void deleteSceneEquip(String userId, String spaceId, String sceneId, String equipId, Observer<ResponseBody> observer) {
        setSubscribe(apiService.deleteSceneEquip(userId, spaceId, sceneId, equipId), observer);
    }

    //banlap: 删除区域下的设备接口
    public void deleteByUserEquip(String spaceId, String equipId, Observer<ResponseBody> observer) {
        setSubscribe(apiService.deleteByUserEquip(spaceId, equipId), observer);
    }


    //banlap: 更新场景接口
    public void updateScene(String data, Observer<ResponseBody> observer) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.Companion.create(data, mediaType);
        setSubscribe(apiService.updateScene(body), observer);
    }

    //banlap: 获取三位开关信息
    public void getSceneSwitch(String userId, String equipId, Observer<ResponseBody> observer) {
        setSubscribe(apiService.getSceneSwitch(userId, equipId), observer);
    }

    //banlap: 更新三位开关信息
    public void updateSceneSwitch(String data, Observer<ResponseBody> observer) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.Companion.create(data, mediaType);
        setSubscribe(apiService.updateSceneSwitch(body), observer);
    }

    public void addTiming(String data, Observer<ResponseBody> observer) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.Companion.create(data, mediaType);
        setSubscribe(apiService.addTiming(body), observer);
    }

    public void selectTiming(String sceneId, Observer<ResponseBody> observer) {
        setSubscribe(apiService.selectTiming(sceneId), observer);
    }

    public void deleteTiming(String timingId, Observer<ResponseBody> observer) {
        setSubscribe(apiService.deleteTiming(timingId), observer);
    }

    private interface ApiService {
        @GET("api")
        Observable<ResponseBody> getWeather(@Query("version") String version, @Query("appid") String appId, @Query("appsecret") String appSecret);

        @GET("homePage/getWeather")
        Observable<ResponseBody> getNewWeather(@Query("cityId") String cityId);

        @POST("user/userLogin")
        Observable<ResponseBody> userLogin(@Body RequestBody data);

        @POST("user/checkUser")
        Observable<ResponseBody> validateLogin(@Body RequestBody data);

        @POST("user/updateUser")
        Observable<ResponseBody> updateUser(@Body RequestBody data);

        @GET("homePage/queryArea")
        Observable<ResponseBody> queryRegion(@Query("userId") String userId);

        @GET("homePage/regionalManagement")
        Observable<ResponseBody> regionalManagement(@Query("userId") String userId, @Query("spaceName") String spaceName);

        @GET("homePage/deleteArea")
        Observable<ResponseBody> deleteArea(@Query("userId") String userId, @Query("spaceId") String spaceId);

        @GET("homePage/modifyArea")
        Observable<ResponseBody> modifyArea(@Query("userId") String userId, @Query("spaceId") String spaceId, @Query("spaceName") String spaceName, @Query("spaceImg") String spaceImg);

        @GET("homePage/addEquipForScene")
        Observable<ResponseBody> addEquipForScene(@Query("sceneId") String sceneId, @Query("equips") String equips);

        @GET("homePage/addScene")
        Observable<ResponseBody> addScene(@Query("userId") String userId, @Query("spaceId") String spaceId, @Query("sceneName") String sceneName, @Query("sceneImg") String sceneImg);

        @GET("homePage/deleteSceneEquip")
        Observable<ResponseBody> deleteScene(@Query("sceneId") String sceneId);

        @GET("homePage/selectListScene")
        Observable<ResponseBody> selectListScene(@Query("spaceId") String spaceId);

        @POST("space/batchSpace")
        Observable<ResponseBody> addRegionList(@Body RequestBody data);

        @POST("scene/batchScenes")
        Observable<ResponseBody> addSceneList(@Body RequestBody data);

        @GET("homePage/selectAllData")
        Observable<ResponseBody> selectAllData(@Query("userId") String userId);

        @GET("equi/selectAllInfo")
        Observable<ResponseBody> getProductInfo();

        @GET("position/selectPositions")
        Observable<ResponseBody> getDefaultMac(@Query("userId") String userId);

        @GET("dictionary/selectDictionary")
        Observable<ResponseBody> getAllDevType();

        @GET("homePage/selectListEquip")
        Observable<ResponseBody> getAllEquips(@Query("userId") String userId);

        @POST("homePage/addSingleEquip")
        Observable<ResponseBody> addSingleEquip(@Body RequestBody data);

        @GET("homePage/deleteUserEquip")
        Observable<ResponseBody> deleteEquip(@Query("userId") String userId, @Query("userEquipId") String equipId);

        @POST("record/addRecord")
        Observable<ResponseBody> addSceneEquip(@Body RequestBody data);

        @GET("record/selectRecordByUserEquipId")
        Observable<ResponseBody> selectRecord(@Query("userId") String userId, @Query("sceneId") String sceneId, @Query("userEquipId") String userEquipId);

        @GET("homePage/deleteSceneEquipForOne")
        Observable<ResponseBody> deleteSceneEquip(@Query("userId") String userId, @Query("spaceId") String spaceId, @Query("sceneId") String sceneId, @Query("userEquipId") String userEquipId);

        @GET("homePage/deleteByUserEquipId")
        Observable<ResponseBody> deleteByUserEquip(@Query("spaceId") String spaceId, @Query("userEquipId") String userEquipId);

        @POST("scene/updateScene")
        Observable<ResponseBody> updateScene(@Body RequestBody data);

        @GET("homePage/getSceneSwitch")
        Observable<ResponseBody> getSceneSwitch(@Query("userId") String userId, @Query("userEquipId") String userEquipId);

        @POST("homePage/updateSceneSwitch")
        Observable<ResponseBody> updateSceneSwitch(@Body RequestBody data);

        @POST("Timing/addTiming")
        Observable<ResponseBody> addTiming(@Body RequestBody data);

        @GET("Timing/selectTiming")
        Observable<ResponseBody> selectTiming(@Query("sceneId") String sceneId);

        @GET("Timing/deleteTiming")
        Observable<ResponseBody> deleteTiming(@Query("timingId") String timingId);
    }

}
