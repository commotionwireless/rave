package net.commotionwireless.ravedbtest2.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import net.commotionwireless.ravedbtest2.BasicApp;
import net.commotionwireless.ravedbtest2.db.entity.RaveNodeEntity;

import java.util.List;

public class NodeListViewModel extends AndroidViewModel {

    // MediatorLiveData can observe other LiveData objects and react on their emissions.
    private final MediatorLiveData<List<RaveNodeEntity>> mObservableRaveNodes;

    public NodeListViewModel(Application application) {
        super(application);

        mObservableRaveNodes = new MediatorLiveData<>();
        // set by default null, until we get data from the database.
        mObservableRaveNodes.setValue(null);

        LiveData<List<RaveNodeEntity>> raveNode = ((BasicApp) application).getRepository()
                .getRaveNodes();

        // observe the changes of the products from the database and forward them
        mObservableRaveNodes.addSource(raveNode, mObservableRaveNodes::setValue);
    }

    /**
     * Expose the LiveData Products query so the UI can observe it.
     */
    public LiveData<List<RaveNodeEntity>> getRaveNodes() {
        return mObservableRaveNodes;
    }

}
