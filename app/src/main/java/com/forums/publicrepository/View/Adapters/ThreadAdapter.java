package com.forums.publicrepository.View.Adapters;

import static com.forums.publicrepository.utils.Constants.getMediaTypeFromContentType;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.forums.publicrepository.Arch.Entity.Thread;
import com.forums.publicrepository.Arch.Firebase.FirebaseUtils;
import com.forums.publicrepository.Arch.Firebase.Threads;
import com.forums.publicrepository.R;
import com.forums.publicrepository.View.MediaPlayer.MediaPlayer;
import com.forums.publicrepository.utils.Constants;
import com.forums.publicrepository.utils.Snack;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ThreadAdapter extends RecyclerView.Adapter<ThreadAdapter.ViewHolder> {

    private onThreadClickListener tListener;
    private replyClickListener replyClickListener;
    private FirebaseStorage storage;
    private final List<Thread> threads = new ArrayList<>();
    private final int Activity;
    private final Context context;

    public ThreadAdapter(int Activity, Context context) {
        this.Activity = Activity;
        this.context = context;
        this.storage = FirebaseStorage.getInstance(Constants.storageRef);
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

    public LiveData<Integer> getCount(){
        return new MutableLiveData<>(threads.size());
    }

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
//                if needed...
//                }
            });
        }

        public void bind(Thread thread) {
            if (thread.getImgURL().equals(Constants.NO_PIC)){
                img.setVisibility(View.GONE);
                bar.setVisibility(View.GONE);
            }else{
                img.setVisibility(View.GONE);
                StorageReference storageRef = storage.getReferenceFromUrl(thread.getImgURL());
                storageRef.getMetadata().addOnSuccessListener(storageMetadata -> {
                    String contentType = storageMetadata.getContentType();
                    Snack.log("ContentType", contentType);
                    Constants.MediaType mediaType = getMediaTypeFromContentType(contentType);

                    if (mediaType == Constants.MediaType.IMAGE) {
                        putImage(thread, img, bar);
                        imgClickListener(img, Constants.INTENT_IMAGE, thread.getImgURL());
                    } else if (mediaType == Constants.MediaType.VIDEO) {
                        putThumbnail(thread, img, bar);
                        imgClickListener(img, Constants.INTENT_VIDEO, thread.getImgURL());
                    } else {
                        Snack.log("ThreadAdapter","Unsupported file type");
                    }
                }).addOnFailureListener(exception -> {
                    // Handle failure to get metadata
                    Snack.log("ThreadAdapter","Media load failed");
                });
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
    private void imgClickListener(ImageView img, String urlType, String url){
//        urlType: Image or Video.
        if (Activity==Constants.REPLY_ACTIVITY){
            img.setOnClickListener(v->{
                Snack.log("ThreadAdapter", "ImageView clicked.");
                Intent i = new Intent(context, MediaPlayer.class);
                String[] media = {urlType, url};
                i.putExtra(Constants.MEDIA_KEY, media);
                context.startActivity(i);
            });
        }
    }

    private void putImage(Thread thread, ImageView img, ProgressBar bar){
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

    private void putThumbnail(Thread thread, ImageView img, ProgressBar bar){
        Glide.with(context)
                .load(Uri.parse(thread.getImgURL()))
                .thumbnail(0.1f) // Load a thumbnail (10% of the original video)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        bar.setVisibility(View.GONE);
                        img.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(img);
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
