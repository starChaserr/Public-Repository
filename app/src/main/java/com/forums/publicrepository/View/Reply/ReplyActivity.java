package com.forums.publicrepository.View.Reply;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.forums.publicrepository.Arch.Entity.Thread;
import com.forums.publicrepository.Arch.Firebase.FirebaseUtils;
import com.forums.publicrepository.R;
import com.forums.publicrepository.View.Adapters.ThreadAdapter;
import com.forums.publicrepository.ViewModel.mainViewModel;
import com.forums.publicrepository.utils.Constants;
import com.forums.publicrepository.utils.Snack;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ReplyActivity extends AppCompatActivity {

    private mainViewModel viewModel;
    private ThreadAdapter adapter;
    private MutableLiveData<Map<String, Uri>> media = new MutableLiveData<>(null);
    private boolean mediaToBeUploaded = false;
    //    Thread items----------
    private ImageView pic;
    private ProgressBar bar;
    private TextView id, title, body, reply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
        Intent i = getIntent();
        findViews(i.getStringExtra(Constants.INTENT_THREAD));
    }

    private void findViews(String msgLoc) {
        viewModel = new ViewModelProvider(this).get(mainViewModel.class);
        findViewById(R.id.back).setOnClickListener(v -> finish());
        setThread(msgLoc);
    }

    private void setThread(String msgLoc) {
        pic = findViewById(R.id.image);
        bar = findViewById(R.id.imgLoading);
        title = findViewById(R.id.tTitle);
        body = findViewById(R.id.tBody);
        id = findViewById(R.id.tid);
        reply = findViewById(R.id.btnReply);
        RecyclerView list1 = findViewById(R.id.list);
        adapter = new ThreadAdapter(Constants.REPLY_ACTIVITY, this);
        list1.setAdapter(adapter);
        adapter.setReplyClickListener(id -> {
            addThreadPopup(msgLoc, id);
        });
        viewModel.getMessageById(msgLoc).observe(this, t -> {
            if (t != null) {
                if (t.getImgURL().equals(Constants.NO_PIC)) {
                    pic.setVisibility(View.GONE);
                    bar.setVisibility(View.GONE);
                } else {
                    pic.setVisibility(View.GONE);
                    Glide.with(this).load(t.getImgURL()).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            bar.setVisibility(View.GONE);
                            pic.setVisibility(View.VISIBLE);
                            return false;
                        }
                    }).into(pic);
                }
                String ID = "@" + t.getId() + "\t\t" + FirebaseUtils.getTime(t.getCreationTime());
                id.setText(ID);
                title.setText(t.getTitle());
                body.setText(t.getBody());
                reply.setOnClickListener(v -> addThreadPopup(msgLoc, null));
            }
        });
        viewModel.getReplies(msgLoc).observe(this, list -> {
            if (list != null) {
                adapter.setThreads(list);
            }
        });
    }

    private void addThreadPopup(String msgLoc, @Nullable String replyTo) {
        PopupWindow popupWindow = new PopupWindow(this);
        media = new MutableLiveData<>();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View v = inflater.inflate(R.layout.reply_popup, null);
        popupWindow.setContentView(v);
        ImageView pic;
        TextView addMedia;
        TextView tid;
        EditText body;
        Button confirm, cancel;
        tid = v.findViewById(R.id.rid);
        String s = "";
        if (replyTo == null) {
            s = "Replying to @" + msgLoc.split("/")[1];
        } else {
            s = "Replying to @" + replyTo + " in @" + msgLoc.split("/")[1] + "'s thread.";
        }
        tid.setText(s);
        body = v.findViewById(R.id.body);
        confirm = v.findViewById(R.id.confirm);
        cancel = v.findViewById(R.id.cancel);
        pic = v.findViewById(R.id.image);
        addMedia = v.findViewById(R.id.addMedia);
        AtomicReference<Uri> mediaURI = new AtomicReference<>(null);
        cancel.setOnClickListener(V -> popupWindow.dismiss());
        pic.setOnClickListener(V -> getMediaIntent());
        addMedia.setOnClickListener(V -> getMediaIntent());

        media.observe(this, uri -> {
            if (mediaToBeUploaded) {
                if (uri.containsKey("image/")) {
                    addMedia.setVisibility(View.GONE);
                    pic.setVisibility(View.VISIBLE);
                    if (uri.get("image/") != null) {
                        mediaURI.set(uri.get("image/"));
                    }
                    try {
                        Bitmap bMap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri.get("image/"));
                        pic.setImageBitmap(bMap);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    media.setValue(null);
                    mediaToBeUploaded = false;
                } else if (uri.containsKey("video/")) {
//                  TODO: Handle video
                    Snack.show(pic, "Todo://");
                    media.setValue(null);
                    mediaToBeUploaded = false;
                }
            }
        });

        confirm.setOnClickListener(V -> {
            putThread(popupWindow, msgLoc, V, body, replyTo, mediaURI.get());
        });

        popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        popupWindow.setBackgroundDrawable(null);
        popupWindow.setElevation(6);
        popupWindow.showAsDropDown(reply);
    }

    private void putThread(PopupWindow popupWindow, String msgLoc, View v, EditText body,
                           @Nullable String replyTo, @Nullable Uri uri) {
        String b = "";
        if (replyTo == null) {
            b = body.getText().toString();
        } else {
            b = "@" + replyTo + "< " + body.getText().toString();
        }
        if (!b.isEmpty()) {
            Thread t = new Thread();
            t.setTitle(Constants.NO_TITLE);
            t.setImgURL(Constants.NO_PIC);
            t.setBody(b);
            t.setMsgLoc(msgLoc);
            t.setCreationTime(0);
            viewModel.addThread(t, uri);
            popupWindow.dismiss();
        } else {
            Snack.show(v, "Can't post without body");
        }
    }

    private void getMediaIntent() {
        mediaToBeUploaded = true;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");  // Set MIME type to */* to allow both images and videos
        startActivityForResult(intent, Constants.FILE_PICK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.FILE_PICK_REQUEST && data != null) {
                Uri selectedFileUri = data.getData();

                if (selectedFileUri != null) {
                    // Check the MIME type of the selected file
                    String mimeType = getContentResolver().getType(selectedFileUri);

                    if (mimeType != null) {
                        if (mimeType.startsWith("image/")) {
                            // Selected file is an image
                            Map<String, Uri> map = new HashMap<>();
                            map.put("image/", selectedFileUri);
                            media.setValue(map);
                        } else if (mimeType.startsWith("video/")) {
                            // Selected file is a video
                            Map<String, Uri> map = new HashMap<>();
                            map.put("video/", selectedFileUri);
                            media.setValue(map);
                        } else {
                            // Unsupported file type
                            Snack.show(pic, "Unsupported file type");
                        }
                    }
                }
            } else {
                mediaToBeUploaded = false;
            }
        }
    }
}