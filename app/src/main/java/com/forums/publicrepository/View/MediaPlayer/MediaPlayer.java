package com.forums.publicrepository.View.MediaPlayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.forums.publicrepository.R;
import com.forums.publicrepository.utils.Constants;
import com.forums.publicrepository.utils.Snack;

public class MediaPlayer extends AppCompatActivity {

    private ImageView imageView;
    private VideoView videoView;
    private FrameLayout vView;
    private LinearLayout iView;
    private ProgressBar bar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        findViews(getIntentType());
    }
    private String[] getIntentType(){
        Intent i = getIntent();
        return i.getStringArrayExtra(Constants.MEDIA_KEY);
    }

    private void findViews(String[] intent){
        imageView = findViewById(R.id.image);
        videoView = findViewById(R.id.video);
        vView = findViewById(R.id.vLayout);
        iView = findViewById(R.id.iLayout);
        bar = findViewById(R.id.progress);
        if (intent[0].equals(Constants.INTENT_VIDEO)){
            setVideo(intent[1]);
        } else if (intent[0].equals(Constants.INTENT_IMAGE)) {
            setImage(intent[1]);
        }
    }

    private void setVideo(String url){
        iView.setVisibility(View.GONE);
        Uri videoUri = Uri.parse(url);
        videoView.setVideoURI(videoUri);
        MediaController controller = new MediaController(this);
        controller.setAnchorView(videoView);
        videoView.setMediaController(controller);
        videoView.start();
    }

    private void setImage(String url){
        vView.setVisibility(View.GONE);
        Snack.log("Media Player", "Loading URL: "+url);
        Glide.with(this).load(url).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                        Reload within 5seconds

                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                bar.setVisibility(View.GONE);
                return false;
            }
        }).into(imageView);
    }
}