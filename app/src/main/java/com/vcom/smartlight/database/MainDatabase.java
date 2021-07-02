package com.vcom.smartlight.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.vcom.smartlight.database.dao.DeviceDao;
import com.vcom.smartlight.database.dao.SceneDao;
import com.vcom.smartlight.database.dao.SceneLightDao;
import com.vcom.smartlight.database.dao.SceneSwitchDao;
import com.vcom.smartlight.database.dao.SceneTouchDao;
import com.vcom.smartlight.database.entity.DeviceDB;
import com.vcom.smartlight.database.entity.SceneDB;
import com.vcom.smartlight.database.entity.SceneLightDB;
import com.vcom.smartlight.database.entity.SceneSwitchDB;
import com.vcom.smartlight.database.entity.SceneTouchDB;

@Database(entities = {DeviceDB.class, SceneDB.class, SceneLightDB.class, SceneTouchDB.class, SceneSwitchDB.class}, version = 2, exportSchema = false)
public abstract class MainDatabase extends RoomDatabase {

    public abstract DeviceDao getDeviceDao();

    public abstract SceneDao getSceneDao();

    public abstract SceneLightDao getSceneLightDao();

    public abstract SceneTouchDao getSceneTouchDao();

    public abstract SceneSwitchDao getSceneSwitchDao();

}
