package net.commotionwireless.ravedbtest2.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import net.commotionwireless.ravedbtest2.db.entity.RaveNodeEntity;

import java.util.List;

@Dao
public interface RaveNodeDao {
    @Query("SELECT * FROM raveNodes")
    LiveData<List<RaveNodeEntity>> loadAllNodes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RaveNodeEntity> raveNodes);

    @Query("select * from raveNodes where address = :address")
    LiveData<RaveNodeEntity> loadRaveNode(int address);

    @Query("select * from raveNodes where address = :address")
    RaveNodeEntity loadRaveNodeSync(int address);
}
