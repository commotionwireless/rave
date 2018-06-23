package net.commotionwireless.ravedbtest2.db;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import net.commotionwireless.ravedbtest2.AppExecutors;
import net.commotionwireless.ravedbtest2.db.converter.DateConverter;
import net.commotionwireless.ravedbtest2.db.dao.RaveMessageDao;
import net.commotionwireless.ravedbtest2.db.dao.RaveNodeDao;
import net.commotionwireless.ravedbtest2.db.entity.RaveMessageEntity;
import net.commotionwireless.ravedbtest2.db.entity.RaveNodeEntity;

import java.util.List;

@Database(entities = {RaveMessageEntity.class, RaveNodeEntity.class}, version = 1)
@TypeConverters(DateConverter.class)
public abstract class RaveDatabase extends RoomDatabase {

    private static RaveDatabase sInstance;

    @VisibleForTesting
    public static final String DATABASE_NAME = "rave-database";

    public abstract RaveMessageDao raveMessageDao();

    public abstract RaveNodeDao raveNodeDao();

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    public static RaveDatabase getInstance(final Context context, final AppExecutors executors) {
        if (sInstance == null) {
            synchronized (RaveDatabase.class) {
                if (sInstance == null) {
                    sInstance = buildDatabase(context.getApplicationContext(), executors);
                    sInstance.updateDatabaseCreated(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    /**
     * Build the database. {@link Builder#build()} only sets up the database configuration and
     * creates a new instance of the database.
     * The SQLite database is only created when it's accessed for the first time.
     */
    private static RaveDatabase buildDatabase(final Context appContext,
                                             final AppExecutors executors) {
        return Room.databaseBuilder(appContext, RaveDatabase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        executors.diskIO().execute(() -> {
                            // Generate the data for pre-population
                            RaveDatabase database = RaveDatabase.getInstance(appContext, executors);
                            List<RaveNodeEntity> raveNodes = DataGenerator.generateRaveNodes();
                            List<RaveMessageEntity> raveMessages =
                                    DataGenerator.generateMessagesForNodes(raveNodes);

                            insertData(database, raveNodes, raveMessages);
                            // notify that the database was created and it's ready to be used
                            database.setDatabaseCreated();
                        });
                    }
                }).build();
    }

    /**
     * Check whether the database already exists and expose it via {@link #getDatabaseCreated()}
     */
    private void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    private void setDatabaseCreated(){
        mIsDatabaseCreated.postValue(true);
    }

    private static void insertData(final RaveDatabase database, final List<RaveNodeEntity> raveNodes,
                                   final List<RaveMessageEntity> raveMessages) {
        database.runInTransaction(() -> {
            database.raveNodeDao().insertAll(raveNodes);
            database.raveMessageDao().insertAll(raveMessages);
        });
    }

    public LiveData<Boolean> getDatabaseCreated() {
        return mIsDatabaseCreated;
    }

}
