package com.forums.publicrepository.View.Reply;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.forums.publicrepository.Arch.Entity.Thread;
import com.forums.publicrepository.Arch.Firebase.FirebaseUtils;
import com.forums.publicrepository.R;
import com.forums.publicrepository.View.Adapters.ThreadAdapter;
import com.forums.publicrepository.ViewModel.mainViewModel;
import com.forums.publicrepository.utils.Constants;
import com.forums.publicrepository.utils.Snack;

public class ReplyActivity extends AppCompatActivity {

    private mainViewModel viewModel;
    private ThreadAdapter adapter;
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

    private void findViews(String msgLoc){
        viewModel = new ViewModelProvider(this).get(mainViewModel.class);
        findViewById(R.id.back).setOnClickListener(v->finish());
        setThread(msgLoc);
    }

    private void setThread(String msgLoc){
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
        viewModel.getMessageById(msgLoc).observe(this, t->{
            if(t!=null){
                if (t.getImgURL().equals(Constants.NO_PIC)){
                    pic.setVisibility(View.GONE);
                    bar.setVisibility(View.GONE);
                }
                String ID = "@"+t.getId()+"\t\t"+ FirebaseUtils.getTime(t.getCreationTime());
                id.setText(ID);
                title.setText(t.getTitle());
                body.setText(t.getBody());
                reply.setOnClickListener(v->addThreadPopup(msgLoc, null));
            }
        });
        viewModel.getReplies(msgLoc).observe(this, list->{
            if (list!=null){
                adapter.setThreads(list);
            }
        });
    }

    private void addThreadPopup(String msgLoc, @Nullable String replyTo){
        PopupWindow popupWindow = new PopupWindow(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View v = inflater.inflate(R.layout.reply_popup, null);
        popupWindow.setContentView(v);
        TextView tid;
        EditText body;
        Button confirm, cancel;
//        Basic [media upload logic pending.]
        tid = v.findViewById(R.id.rid);
        String s = "";
        if (replyTo==null){
            s = "Replying to @"+msgLoc.split("/")[1];
        }else{
            s = "Replying to @"+replyTo+" in @"+msgLoc.split("/")[1]+"'s thread.";
        }
        tid.setText(s);
        body = v.findViewById(R.id.body);
        confirm = v.findViewById(R.id.confirm);
        cancel = v.findViewById(R.id.cancel);
        cancel.setOnClickListener(V->popupWindow.dismiss());

        confirm.setOnClickListener(V->{
            String b = "";
            if (replyTo==null){
                b = body.getText().toString();
            }else{
                b = "@"+replyTo+"< "+body.getText().toString();
            }
            if (!b.isEmpty()){
                Thread t = new Thread();
                t.setTitle(Constants.NO_TITLE);
                t.setBody(b);
                t.setMsgLoc(msgLoc);
                t.setImgURL(Constants.NO_PIC);
                t.setCreationTime(0);
                viewModel.addThread(t, null);
                popupWindow.dismiss();
            }else{
                Snack.show(v, "Can't post without body");
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        popupWindow.setBackgroundDrawable(null);
        popupWindow.setElevation(6);
        popupWindow.showAsDropDown(reply);
    }
}