package com.vcom.smartlight.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.vcom.smartlight.database.entity.SceneLightDB;

@Dao
public interface SceneLightDao {

    @Insert
    void insertSceneLight(SceneLightDB... scene);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateSceneLight(SceneLightDB scene);

    @Delete
    void deleteSceneLight(SceneLightDB scene);

    @Query("select * from scenelightdb where meshAddress==:meshAddress and sceneId==:sceneId")
    SceneLightDB getLightScene(int sceneId, int meshAddress);

}
