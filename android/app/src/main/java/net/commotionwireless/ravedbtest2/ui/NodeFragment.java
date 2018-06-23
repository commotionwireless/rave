package net.commotionwireless.ravedbtest2.ui;

import android.support.v4.app.Fragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.commotionwireless.ravedbtest2.R;
import net.commotionwireless.ravedbtest2.databinding.NodeFragmentBinding;
import net.commotionwireless.ravedbtest2.db.entity.RaveMessageEntity;
import net.commotionwireless.ravedbtest2.db.entity.RaveNodeEntity;
import net.commotionwireless.ravedbtest2.model.RaveMessage;
import net.commotionwireless.ravedbtest2.viewmodel.NodeViewModel;

import java.util.List;

public class NodeFragment extends Fragment {

    private static final String NODE_ADDRESS = "12345678";

    private NodeFragmentBinding mBinding;

    private MessageAdapter mMessageAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate this data binding layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.node_fragment, container, false);

        // Create and set the adapter for the RecyclerView.
        mMessageAdapter = new MessageAdapter(mMessageClickCallback);
        mBinding.messageList.setAdapter(mMessageAdapter);
        return mBinding.getRoot();
    }

    private final MessageClickCallback mMessageClickCallback = new MessageClickCallback() {
        @Override
        public void onClick(RaveMessage raveMessage) {
            // no-op

        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        NodeViewModel.Factory factory = new NodeViewModel.Factory(
                getActivity().getApplication(), getArguments().getInt(NODE_ADDRESS));

        final NodeViewModel model = ViewModelProviders.of(this, factory)
                .get(NodeViewModel.class);

        mBinding.setNodeViewModel(model);

        subscribeToModel(model);
    }

    private void subscribeToModel(final NodeViewModel model) {

        // Observe product data
        model.getObservableRaveNode().observe(this, new Observer<RaveNodeEntity>() {
            @Override
            public void onChanged(@Nullable RaveNodeEntity raveNodeEntity) {
                model.setRaveNode(raveNodeEntity);
            }
        });

        // Observe comments
        model.getRaveMessages().observe(this, new Observer<List<RaveMessageEntity>>() {
            @Override
            public void onChanged(@Nullable List<RaveMessageEntity> raveMessageEntities) {
                if (raveMessageEntities != null) {
                    mBinding.setIsLoading(false);
                    mMessageAdapter.setMessageList(raveMessageEntities);
                } else {
                    mBinding.setIsLoading(true);
                }
            }
        });
    }

    /** Creates node fragment for specific address */
    public static NodeFragment forNode(int address) {
        NodeFragment fragment = new NodeFragment();
        Bundle args = new Bundle();
        args.putInt(NODE_ADDRESS, address);
        fragment.setArguments(args);
        return fragment;
    }
}
