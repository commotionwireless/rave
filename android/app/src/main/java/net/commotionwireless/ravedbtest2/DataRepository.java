package net.commotionwireless.ravedbtest2;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import net.commotionwireless.ravedbtest2.db.RaveDatabase;
import net.commotionwireless.ravedbtest2.db.entity.RaveMessageEntity;
import net.commotionwireless.ravedbtest2.db.entity.RaveNodeEntity;

import java.util.List;

/**
 * Repository handling the work with RAVE nodes and messages.
 */
public class DataRepository {

    private static DataRepository sInstance;

    private final RaveDatabase mDatabase;
    private MediatorLiveData<List<RaveNodeEntity>> mObservableProducts;

    private DataRepository(final RaveDatabase database) {
        mDatabase = database;
        mObservableProducts = new MediatorLiveData<>();

        mObservableProducts.addSource(mDatabase.raveNodeDao().loadAllNodes(),
                raveNodeEntities -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        mObservableProducts.postValue(raveNodeEntities);
                    }
                });
    }

    public static DataRepository getInstance(final RaveDatabase database) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(database);
                }
            }
        }
        return sInstance;
    }

    /**
     * Get the list of products from the database and get notified when the data changes.
     */
    public LiveData<List<RaveNodeEntity>> getRaveNodes() {
        return mObservableProducts;
    }

    public LiveData<RaveNodeEntity> loadRaveNode(final int address) {
        return mDatabase.raveNodeDao().loadRaveNode(address);
    }

    public LiveData<List<RaveMessageEntity>> loadRaveMessages(final int nodeAddress) {
        return mDatabase.raveMessageDao().loadRaveMessages(nodeAddress);
    }
}
