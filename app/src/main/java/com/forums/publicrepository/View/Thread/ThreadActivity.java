package com.forums.publicrepository.View.Thread;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.forums.publicrepository.Arch.Entity.Thread;
import com.forums.publicrepository.R;
import com.forums.publicrepository.View.Adapters.ThreadAdapter;
import com.forums.publicrepository.View.Reply.ReplyActivity;
import com.forums.publicrepository.ViewModel.mainViewModel;
import com.forums.publicrepository.utils.Constants;
import com.forums.publicrepository.utils.Snack;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ThreadActivity extends AppCompatActivity {
    private mainViewModel viewModel;
    private ThreadAdapter adapter;
    private FloatingActionButton addThread;
    private ImageButton more;
    private MutableLiveData<Map<String,Uri>> media = new MutableLiveData<>(null);
    private boolean mediaToBeUploaded = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);
        Intent i = getIntent();
        findViews(i.getStringExtra(Constants.INTENT_TOPIC));
    }

    private void findViews(String Topic) {
        findViewById(R.id.checker).setVisibility(View.GONE);
        findViewById(R.id.warning).setVisibility(View.GONE);
        more = findViewById(R.id.more);
        more.setOnClickListener(v -> morePopup());
        addThread = findViewById(R.id.addThread);
        addThread.setOnClickListener(v -> addThreadPopup(Topic));
        findViewById(R.id.back).setOnClickListener(v -> finish());
        TextView title = findViewById(R.id.title);
        String t = "Top Threads in " + Topic;
        title.setText(t);
        RecyclerView list = findViewById(R.id.list);
        adapter = new ThreadAdapter(Constants.THREAD_ACTIVITY, this);
        list.setAdapter(adapter);
        viewModel = new ViewModelProvider(this).get(mainViewModel.class);
        vmStuff(Topic);
        adapter.setOnThreadClickListener(id -> {
            Intent i = new Intent(ThreadActivity.this, ReplyActivity.class);
            i.putExtra(Constants.INTENT_THREAD, id);
            startActivity(i);
        });
    }

    private void vmStuff(String Topic) {
        viewModel.getThreads(Topic).observe(this, threads -> {
            if (threads != null) {
                if (!threads.isEmpty()) {
//                    Update Adapter here.
                    adapter.setThreads(threads);
                } else {
//                    there is no thread under this topic (Update UI if needed)
                    Snack.log("Thread: " + Topic, "No threads here");
                }
            } else {
//                Loading progressBar here if needed
                Snack.log("Thread: " + Topic, "Loading...");
            }
        });
    }

    private void morePopup() {
        PopupWindow popupWindow = new PopupWindow(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View v = inflater.inflate(R.layout.more_popup, null);
        popupWindow.setContentView(v);


        popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(null);
        popupWindow.setElevation(6);
        popupWindow.showAsDropDown(more);
    }

    private void addThreadPopup(String Topic) {
        PopupWindow popupWindow = new PopupWindow(this);
        media = new MutableLiveData<>();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View v = inflater.inflate(R.layout.add_thread_popup, null);
        popupWindow.setContentView(v);
        EditText title, body;
        Button confirm, cancel;
        TextView addMedia;
        ImageView pic;
        AtomicReference<Uri> mediaURI = new AtomicReference<>(null);
        title = v.findViewById(R.id.title);
        body = v.findViewById(R.id.body);
        confirm = v.findViewById(R.id.confirm);
        cancel = v.findViewById(R.id.cancel);
        cancel.setOnClickListener(V -> popupWindow.dismiss());
        addMedia = v.findViewById(R.id.addMedia);
        pic = v.findViewById(R.id.image);

        media.observe(this, uri->{
            if (mediaToBeUploaded){
                if (uri.containsKey("image/")) {
                    addMedia.setVisibility(View.GONE);
                    pic.setVisibility(View.VISIBLE);
                    if (uri.get("image/")!=null) {
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
                    Snack.show(more, "Todo://");
                    media.setValue(null);
                    mediaToBeUploaded = false;
                }
            }
        });

        addMedia.setOnClickListener(V->getMediaIntent());
        pic.setOnClickListener(V->getMediaIntent());

        confirm.setOnClickListener(V -> {
            String ti = title.getText().toString(), b = body.getText().toString();
            setThread(ti, b, Topic, popupWindow, V, mediaURI.get());
        });

        popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        popupWindow.setBackgroundDrawable(null);
        popupWindow.setElevation(6);
        popupWindow.showAsDropDown(addThread);
    }

    private void setThread(String ti, String b, String Topic, PopupWindow popupWindow, View v, @Nullable Uri Uri){
        if (!ti.isEmpty()) {
            if (!b.isEmpty()) {
                Thread t = new Thread();
                t.setTitle(ti);
                t.setBody(b);
                t.setMsgLoc(Topic);
                t.setImgURL(Constants.NO_PIC);
                t.setCreationTime(0);
                viewModel.addThread(t, Uri);
                popupWindow.dismiss();
            } else {
                Snack.show(v, "Can't post without body");
            }
        } else {
            Snack.show(v, "Can't post without title");
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
            if (requestCode == Constants.FILE_PICK_REQUEST && data!=null) {
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
                            Snack.show(more, "Unsupported file type");
                        }
                    }
                }
            }else {
                mediaToBeUploaded = false;
            }
        }
    }
}
