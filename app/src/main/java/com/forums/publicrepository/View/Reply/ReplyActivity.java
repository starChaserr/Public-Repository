package com.forums.publicrepository.View.Reply;

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
    private RecyclerView list;
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
        list = findViewById(R.id.list);
        adapter = new ThreadAdapter(Constants.REPLY_ACTIVITY);
        list.setAdapter(adapter);
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
                reply.setOnClickListener(v->addThreadPopup(msgLoc));
            }
        });
        viewModel.getReplies(msgLoc).observe(this, list->{
            if (list!=null){
                adapter.setThreads(list);
            }
        });
    }

    private void addThreadPopup(String msgLoc){
        PopupWindow popupWindow = new PopupWindow(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View v = inflater.inflate(R.layout.reply_popup, null);
        popupWindow.setContentView(v);
        EditText title, body;
        Button confirm, cancel;
//        Basic [media upload logic pending.]
        body = v.findViewById(R.id.body);
        confirm = v.findViewById(R.id.confirm);
        cancel = v.findViewById(R.id.cancel);
        cancel.setOnClickListener(V->popupWindow.dismiss());

        confirm.setOnClickListener(V->{
            String b = body.getText().toString();
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