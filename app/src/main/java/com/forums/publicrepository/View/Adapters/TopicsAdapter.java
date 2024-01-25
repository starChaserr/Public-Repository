package com.forums.publicrepository.View.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.forums.publicrepository.Arch.Entity.Topic;
import com.forums.publicrepository.R;
import com.forums.publicrepository.utils.Snack;

import java.util.ArrayList;
import java.util.List;

public class TopicsAdapter extends RecyclerView.Adapter<TopicsAdapter.ViewHolder> {

    private onItemClickListener listener;
    private final List<String> allTopics = new ArrayList<>();

    public TopicsAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.topics_item,
                parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(allTopics.get(position));
    }

    public void setTopics(List<Topic> topics){
        allTopics.clear();
        for (int i = 0;i<topics.size();i++) {
            allTopics.add(topics.get(i)+"");
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return allTopics.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        public ViewHolder(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.topic);
            itemView.setOnClickListener(v1 -> {
                int pos = getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onItemClick(allTopics.get(pos));
                }
            });
        }

        public void bind(String  t) {
            title.setText(t);
        }
    }

    public interface onItemClickListener {
        void onItemClick(String id);
    }
    public void setOnItemClickListener(onItemClickListener listener) {
        this.listener = listener;
    }

}
