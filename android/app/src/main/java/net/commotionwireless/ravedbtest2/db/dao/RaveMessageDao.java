package net.commotionwireless.ravedbtest2.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import net.commotionwireless.ravedbtest2.db.entity.RaveMessageEntity;

import java.util.List;

@Dao
public interface RaveMessageDao {
    @Query("SELECT * FROM raveMessages where nodeAddress = :nodeAddress")
    LiveData<List<RaveMessageEntity>> loadRaveMessages(int nodeAddress);

    @Query("SELECT * FROM raveMessages where nodeAddress = :nodeAddress")
    List<RaveMessageEntity> loadRaveMessagesSync(int nodeAddress);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RaveMessageEntity> raveMessages);
}
