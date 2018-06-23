package net.commotionwireless.ravedbtest2.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;

import net.commotionwireless.ravedbtest2.BasicApp;
import net.commotionwireless.ravedbtest2.DataRepository;
import net.commotionwireless.ravedbtest2.db.entity.RaveMessageEntity;
import net.commotionwireless.ravedbtest2.db.entity.RaveNodeEntity;

import java.util.List;

public class NodeViewModel extends AndroidViewModel {

    private final LiveData<RaveNodeEntity> mObservableRaveNode;

    public ObservableField<RaveNodeEntity> raveNode = new ObservableField<>();

    private final int mRaveNodeAddress;

    private final LiveData<List<RaveMessageEntity>> mObservableRaveMessages;

    public NodeViewModel(@NonNull Application application, DataRepository repository,
                            final int nodeAddress) {
        super(application);
        mRaveNodeAddress = nodeAddress;

        mObservableRaveMessages = repository.loadRaveMessages(mRaveNodeAddress);
        mObservableRaveNode = repository.loadRaveNode(mRaveNodeAddress);
    }

    /**
     * Expose the LiveData Messages query so the UI can observe it.
     */
    public LiveData<List<RaveMessageEntity>> getRaveMessages() {
        return mObservableRaveMessages;
    }

    public LiveData<RaveNodeEntity> getObservableRaveNode() {
        return mObservableRaveNode;
    }

    public void setRaveNode(RaveNodeEntity raveNode) {
        this.raveNode.set(raveNode);
    }

    /**
     * A creator is used to inject the node address into the ViewModel
     *
     * This creator is to showcase how to inject dependencies into ViewModels. It's not
     * actually necessary in this case, as the node address can be passed in a public method.
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final int mRaveNodeAddress;

        private final DataRepository mRepository;

        public Factory(@NonNull Application application, int nodeAddress) {
            mApplication = application;
            mRaveNodeAddress = nodeAddress;
            mRepository = ((BasicApp) application).getRepository();
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new NodeViewModel(mApplication, mRepository, mRaveNodeAddress);
        }
    }
}
