package com.example.chatsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<ChatMessage> mList;

    public ChatAdapter(List<ChatMessage> list) {
        this.mList = list;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftLayout, rightLayout;
        TextView leftMsg, rightMsg, timeTv;

        public ViewHolder(View view) {
            super(view);
            leftLayout = view.findViewById(R.id.layout_left);
            rightLayout = view.findViewById(R.id.layout_right);
            leftMsg = view.findViewById(R.id.tv_left_msg);
            rightMsg = view.findViewById(R.id.tv_right_msg);
            timeTv = view.findViewById(R.id.tv_time);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage msg = mList.get(position);

        holder.timeTv.setText(msg.getTime());

        if (msg.getType() == ChatMessage.TYPE_RECEIVED) {
            // 显示左边，隐藏右边
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(msg.getContent());
        } else {
            // 显示右边，隐藏左边
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightMsg.setText(msg.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}