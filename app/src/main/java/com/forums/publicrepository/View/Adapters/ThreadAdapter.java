package com.forums.publicrepository.View.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.forums.publicrepository.Arch.Entity.Thread;
import com.forums.publicrepository.Arch.Firebase.FirebaseUtils;
import com.forums.publicrepository.R;
import com.forums.publicrepository.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ThreadAdapter extends RecyclerView.Adapter<ThreadAdapter.ViewHolder> {

    private onThreadClickListener tListener;
    private replyClickListener replyClickListener;
    private final List<Thread> threads = new ArrayList<>();
    private final int Activity;
    private final Context context;

    public ThreadAdapter(int Activity, Context context) {
        this.Activity = Activity;
        this.context = context;
    }

    public void setThreads(List<Thread> threads) {
        this.threads.clear();
        this.threads.addAll(threads);
        notifyDataSetChanged();
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

            reply.setOnClickListener(v1->{
                int pos = getAdapterPosition();
                if (replyClickListener != null && pos != RecyclerView.NO_POSITION) {
                    replyClickListener.onItemClick(threads.get(pos).getId());
                }
            });

            itemView.setOnClickListener(v1 -> {
                int pos = getAdapterPosition();
                if (Activity==Constants.THREAD_ACTIVITY){
                    if (tListener != null && pos != RecyclerView.NO_POSITION) {
                        tListener.onItemClick(threads.get(pos).getMsgLoc() + "/" + threads.get(pos).getId());
                    }
                    title.setEllipsize(TextUtils.TruncateAt.END);
                    title.setMaxLines(1);
                    body.setEllipsize(TextUtils.TruncateAt.END);
                    body.setMaxLines(2);
                }
//                else if (Activity == Constants.REPLY_ACTIVITY) {
//                    if (rListener != null && pos != RecyclerView.NO_POSITION) {
//                        rListener.onItemClick(threads.get(pos).getId());
//                    }
//                    String s = threads.get(pos).getMsgLoc() + "/" + threads.get(pos).getId();
//                    rListener.onItemClick(s);
//                }
            });
        }

        public void bind(Thread thread) {
            if (thread.getImgURL().equals(Constants.NO_PIC)){
                img.setVisibility(View.GONE);
                bar.setVisibility(View.GONE);
            }else{
                img.setVisibility(View.GONE);
                Glide.with(context).load(thread.getImgURL()).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                        Reload within 5seconds
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        img.setVisibility(View.VISIBLE);
                        bar.setVisibility(View.GONE);
                        return false;
                    }
                }).into(img);
            }
            if (Activity == Constants.THREAD_ACTIVITY){
                reply.setVisibility(View.GONE);
            }
            String idTxt = "@"+thread.getId() + "\t\t" + FirebaseUtils.getTime(thread.getCreationTime());
            id.setText(idTxt);
            if (thread.getTitle().equals(Constants.NO_TITLE)){
                title.setVisibility(View.GONE);
                body.setTextSize(18f);
            }else {
                title.setText(thread.getTitle());
            }
            body.setText(thread.getBody());
        }
    }

    public interface onThreadClickListener {
        void onItemClick(String id);
    }

    public interface replyClickListener {
        void onItemClick(String id);
    }

    public void setOnThreadClickListener(onThreadClickListener tListener) {
        this.tListener = tListener;
    }

    public void setReplyClickListener(replyClickListener replyClickListener) {
        this.replyClickListener = replyClickListener;
    }

}
