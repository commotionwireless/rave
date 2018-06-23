package net.commotionwireless.ravedbtest2.ui;

import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import net.commotionwireless.ravedbtest2.databinding.NodeItemBinding;
import net.commotionwireless.ravedbtest2.R;
import net.commotionwireless.ravedbtest2.model.RaveNode;

import java.util.List;
import java.util.Objects;

public class NodeAdapter extends RecyclerView.Adapter<NodeAdapter.RaveNodeViewHolder> {

    List<? extends RaveNode> mRaveNodeList;

    @Nullable
    private final NodeClickCallback mNodeClickCallback;

    public NodeAdapter(@Nullable NodeClickCallback clickCallback) {
        mNodeClickCallback = clickCallback;
    }

    public void setNodeList(final List<? extends RaveNode> nodeList) {
        if (mRaveNodeList == null) {
            mRaveNodeList = nodeList;
            notifyItemRangeInserted(0, nodeList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mRaveNodeList.size();
                }

                @Override
                public int getNewListSize() {
                    return nodeList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mRaveNodeList.get(oldItemPosition).getAddress() ==
                            nodeList.get(newItemPosition).getAddress();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    RaveNode newNode = nodeList.get(newItemPosition);
                    RaveNode oldNode = mRaveNodeList.get(oldItemPosition);
                    return newNode.getAddress() == oldNode.getAddress()
                            && Objects.equals(newNode.getAlias(), oldNode.getAlias());
                }
            });
            mRaveNodeList = nodeList;
            result.dispatchUpdatesTo(this);
        }
    }

    @Override
    public RaveNodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        NodeItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.node_item,
                        parent, false);
        binding.setCallback(mNodeClickCallback);
        return new RaveNodeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(RaveNodeViewHolder holder, int position) {
        holder.binding.setNode(mRaveNodeList.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mRaveNodeList == null ? 0 : mRaveNodeList.size();
    }

    static class RaveNodeViewHolder extends RecyclerView.ViewHolder {

        final NodeItemBinding binding;

        public RaveNodeViewHolder(NodeItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
