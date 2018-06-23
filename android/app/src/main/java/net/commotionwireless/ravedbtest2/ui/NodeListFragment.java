package net.commotionwireless.ravedbtest2.ui;

import android.support.v4.app.Fragment;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.commotionwireless.ravedbtest2.R;
import net.commotionwireless.ravedbtest2.databinding.ListFragmentBinding;
import net.commotionwireless.ravedbtest2.db.entity.RaveNodeEntity;
import net.commotionwireless.ravedbtest2.model.RaveNode;
import net.commotionwireless.ravedbtest2.viewmodel.NodeListViewModel;

import java.util.List;


public class NodeListFragment extends Fragment {

    public static final String TAG = "NodeViewModel";

    private NodeAdapter mNodeAdapter;

    private ListFragmentBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.list_fragment, container, false);

        mNodeAdapter = new NodeAdapter(mNodeClickCallback);
        mBinding.nodesList.setAdapter(mNodeAdapter);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final NodeListViewModel viewModel =
                ViewModelProviders.of(this).get(NodeListViewModel.class);

        subscribeUi(viewModel);
    }

    private void subscribeUi(NodeListViewModel viewModel) {
        // Update the list when the data changes
        viewModel.getRaveNodes().observe(this, new Observer<List<RaveNodeEntity>>() {
            @Override
            public void onChanged(@Nullable List<RaveNodeEntity> myNodes) {
                if (myNodes != null) {
                    mBinding.setIsLoading(false);
                    mNodeAdapter.setNodeList(myNodes);
                } else {
                    mBinding.setIsLoading(true);
                }
                // espresso does not know how to wait for data binding's loop so we execute changes
                // sync.
                mBinding.executePendingBindings();
            }
        });
    }

    private final NodeClickCallback mNodeClickCallback = new NodeClickCallback() {
        @Override
        public void onClick(RaveNode raveNode) {

            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                ((MainActivity) getActivity()).show(raveNode);
            }
        }
    };

}
