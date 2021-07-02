package com.vcom.smartlight.model;

import java.io.Serializable;
import java.util.List;

/**
 * @Author Lzz
 * @Date 2020/11/3 11:48
 */
public class Region {

    private String spaceId;
    private String spaceName;
    private String sceneId;    //目前用于保存图片
    private String spaceImg;

    private List<Scene> sceneList;

    public Region() {
    }

    public Region(String spaceName) {
        this.spaceName = spaceName;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public List<Scene> getSceneList() {
        return sceneList;
    }

    public void setSceneList(List<Scene> sceneList) {
        this.sceneList = sceneList;
    }

    public String getSceneId() {
        return sceneId;
    }

    public void setSceneId(String sceneId) {
        this.sceneId = sceneId;
    }

    public String getSpaceImg() {
        return spaceImg;
    }

    public void setSpaceImg(String spaceImg) {
        this.spaceImg = spaceImg;
    }

}
