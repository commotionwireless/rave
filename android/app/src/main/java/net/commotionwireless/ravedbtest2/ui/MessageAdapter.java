package net.commotionwireless.ravedbtest2.ui;

import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import net.commotionwireless.ravedbtest2.databinding.MessageItemBinding;
import net.commotionwireless.ravedbtest2.R;
import net.commotionwireless.ravedbtest2.model.RaveMessage;

import java.util.List;
import java.util.Objects;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<? extends RaveMessage> mRaveMessageList;

    @Nullable
    private final MessageClickCallback mMessageClickCallback;

    public MessageAdapter(@Nullable MessageClickCallback messageClickCallback) {
        mMessageClickCallback = messageClickCallback;
    }

    public void setMessageList(final List<? extends RaveMessage> raveMessages) {
        if (mRaveMessageList == null) {
            mRaveMessageList = raveMessages;
            notifyItemRangeInserted(0, raveMessages.size());
        } else {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mRaveMessageList.size();
                }

                @Override
                public int getNewListSize() {
                    return mRaveMessageList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    RaveMessage old = mRaveMessageList.get(oldItemPosition);
                    RaveMessage raveMessage = mRaveMessageList.get(newItemPosition);
                    return old.getId() == raveMessage.getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    RaveMessage old = mRaveMessageList.get(oldItemPosition);
                    RaveMessage raveMessage = raveMessages.get(newItemPosition);
                    return old.getId() == raveMessage.getId()
                            && old.getTimestamp() == raveMessage.getTimestamp()
                            && old.getNodeAddress() == raveMessage.getNodeAddress()
                            && Objects.equals(old.getContents(), raveMessage.getContents());
                }
            });
            mRaveMessageList = raveMessages;
            diffResult.dispatchUpdatesTo(this);
        }
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MessageItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.message_item,
                        parent, false);
        binding.setCallback(mMessageClickCallback);
        return new MessageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        holder.binding.setRaveMessage(mRaveMessageList.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mRaveMessageList == null ? 0 : mRaveMessageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        final MessageItemBinding binding;

        MessageViewHolder(MessageItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
