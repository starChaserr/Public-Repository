package com.forums.publicrepository.View.Thread;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.forums.publicrepository.Arch.Entity.Thread;
import com.forums.publicrepository.R;
import com.forums.publicrepository.View.Adapters.ThreadAdapter;
import com.forums.publicrepository.View.Adapters.TopicsAdapter;
import com.forums.publicrepository.View.Home.HomeActivity;
import com.forums.publicrepository.View.Reply.ReplyActivity;
import com.forums.publicrepository.ViewModel.mainViewModel;
import com.forums.publicrepository.utils.Constants;
import com.forums.publicrepository.utils.Snack;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ThreadActivity extends AppCompatActivity {
    private mainViewModel viewModel;
    private ThreadAdapter adapter;
    private FloatingActionButton addThread;
    private ImageButton more;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);
        Intent i = getIntent();
        findViews(i.getStringExtra(Constants.INTENT_TOPIC));
    }

    private void findViews(String Topic){
        findViewById(R.id.checker).setVisibility(View.GONE);
        findViewById(R.id.warning).setVisibility(View.GONE);
        more = findViewById(R.id.more);
        more.setOnClickListener(v->morePopup());
        addThread = findViewById(R.id.addThread);
        addThread.setOnClickListener(v->addThreadPopup(Topic));
        findViewById(R.id.back).setOnClickListener(v->finish());
        TextView title = findViewById(R.id.title);
        String t = "Top Threads in "+Topic;
        title.setText(t);
        RecyclerView list = findViewById(R.id.list);
        adapter = new ThreadAdapter(Constants.THREAD_ACTIVITY);
        list.setAdapter(adapter);
        viewModel = new ViewModelProvider(this).get(mainViewModel.class);
        vmStuff(Topic);
        adapter.setOnThreadClickListener(id -> {
            Intent i = new Intent(ThreadActivity.this, ReplyActivity.class);
            i.putExtra(Constants.INTENT_THREAD, id);
            startActivity(i);
        });
    }

    private void vmStuff(String Topic){
        viewModel.getThreads(Topic).observe(this, threads -> {
            if (threads!=null){
                if (!threads.isEmpty()){
//                    Update Adapter here.
                    adapter.setThreads(threads);
                }else{
//                    there is no thread under this topic (Update UI if needed)
                    Snack.log("Thread: "+Topic, "No threads here");
                }
            }else{
//                Loading progressBar here if needed
                Snack.log("Thread: "+Topic, "Loading...");
            }
        });
    }

    private void morePopup(){
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

    private void addThreadPopup(String Topic){
        PopupWindow popupWindow = new PopupWindow(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View v = inflater.inflate(R.layout.add_thread_popup, null);
        popupWindow.setContentView(v);
        EditText title, body;
        Button confirm, cancel;
//        Basic [media upload logic pending.]
        title = v.findViewById(R.id.title);
        body = v.findViewById(R.id.body);
        confirm = v.findViewById(R.id.confirm);
        cancel = v.findViewById(R.id.cancel);
        cancel.setOnClickListener(V->popupWindow.dismiss());

        confirm.setOnClickListener(V->{
            String ti = title.getText().toString(), b = body.getText().toString();
            if (!ti.isEmpty()){
                if (!b.isEmpty()){
                    Thread t = new Thread();
                    t.setTitle(ti);
                    t.setBody(b);
                    t.setMsgLoc(Topic);
                    t.setImgURL(Constants.NO_PIC);
                    t.setCreationTime(0);
                    viewModel.addThread(t, null);
                    popupWindow.dismiss();
                }else{
                    Snack.show(v, "Can't post without body");
                }
            }else{
                Snack.show(v, "Can't post without title");
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        popupWindow.setBackgroundDrawable(null);
        popupWindow.setElevation(6);
        popupWindow.showAsDropDown(addThread);
    }
}
