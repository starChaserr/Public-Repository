package com.forums.publicrepository.View.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.forums.publicrepository.Arch.Entity.Thread;
import com.forums.publicrepository.Arch.Firebase.FirebaseUtils;
import com.forums.publicrepository.R;
import com.forums.publicrepository.utils.Constants;
import com.forums.publicrepository.utils.Snack;

import java.util.ArrayList;
import java.util.List;

public class ThreadAdapter extends RecyclerView.Adapter<ThreadAdapter.ViewHolder> {

    private TopicsAdapter.onItemClickListener listener;
    private final List<Thread> threads = new ArrayList<>();

    public ThreadAdapter() {
    }

    public void setThreads(List<Thread> threads) {
        this.threads.clear();
        this.threads.addAll(threads);
        notifyDataSetChanged();
        Snack.log("Thread: ", "Size: " + this.threads.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.thread_item,
                parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(threads.get(position));
    }

    @Override
    public int getItemCount() {
        return threads.size();
    }

    //    Apply this
//    android:ellipsize="end"
//    android:maxLines="6"
    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView id, title, body, reply;
        private final ProgressBar bar;
        private final ImageView img;

        public ViewHolder(@NonNull View v) {
            super(v);
            id = v.findViewById(R.id.tid);
            title = v.findViewById(R.id.tTitle);
            body = v.findViewById(R.id.tBody);
            bar = v.findViewById(R.id.imgLoading);
            img = v.findViewById(R.id.image);
            reply = v.findViewById(R.id.btnReply);

            itemView.setOnClickListener(v1 -> {
                int pos = getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onItemClick(threads.get(pos).getId());
                }
            });
        }

        public void bind(Thread thread) {
            if (thread.getImgURL().equals(Constants.NO_PIC)){
                img.setVisibility(View.GONE);
                bar.setVisibility(View.GONE);
            }
            reply.setVisibility(View.GONE);
            String idTxt = "@"+thread.getId() + "\t\t" + FirebaseUtils.getTime(thread.getCreationTime());
            id.setText(idTxt);
            title.setText(thread.getTitle());
            body.setText(thread.getBody());
            Snack.log("Title", thread.getTitle());
        }
    }

    public interface onItemClickListener {
        void onItemClick(String id);
    }

    public void setOnItemClickListener(TopicsAdapter.onItemClickListener listener) {
        this.listener = listener;
    }

}
