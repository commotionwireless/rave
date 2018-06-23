package net.commotionwireless.ravedbtest2;

import android.app.Application;

import net.commotionwireless.ravedbtest2.db.RaveDatabase;

/**
 * Android Application class. Used for accessing singletons.
 */
public class BasicApp extends Application {

    private AppExecutors mAppExecutors;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppExecutors = new AppExecutors();
    }

    public RaveDatabase getDatabase() {
        return RaveDatabase.getInstance(this, mAppExecutors);
    }

    public DataRepository getRepository() {
        return DataRepository.getInstance(getDatabase());
    }
}
