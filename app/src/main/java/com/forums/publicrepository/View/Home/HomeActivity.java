package com.forums.publicrepository.View.Home;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.forums.publicrepository.Arch.Entity.Topic;
import com.forums.publicrepository.Arch.Firebase.FirebaseUtils;
import com.forums.publicrepository.R;
import com.forums.publicrepository.View.Adapters.TopicsAdapter;
import com.forums.publicrepository.View.Thread.ThreadActivity;
import com.forums.publicrepository.ViewModel.mainViewModel;
import com.forums.publicrepository.utils.Constants;
import com.forums.publicrepository.utils.Snack;
import com.forums.publicrepository.utils.visibility;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private mainViewModel mainVM;
    private RecyclerView topics;
    private ProgressBar progressBar;
    private TextView id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
//        Snack.log("Server", "Time: "+FirebaseUtils.getServerTime());
    }

    private void findViews() {
        mainVM = new ViewModelProvider(this).get(mainViewModel.class);
        ImageButton more = findViewById(R.id.more);
        ImageButton back = findViewById(R.id.back);
        back.setVisibility(View.GONE);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        topics = findViewById(R.id.list);
        navigationView = findViewById(R.id.nav_view);
        progressBar = findViewById(R.id.progress);
        more.setVisibility(View.GONE);
        hideList();
        setView();
        AuthCheck();
        TopicsFetch();
    }
    private void showList(){
        topics.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void hideList(){
        topics.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }
    private void AuthCheck(){
        ProgressBar checker = findViewById(R.id.checker);
        ImageView warning = findViewById(R.id.warning);
        AtomicInteger AuthCount = new AtomicInteger();

        mainVM.Auth();
        mainVM.setAuthCount(1);

        mainVM.getAuthCount().observe(this, AuthCount::set);

        mainVM.getErrorCollector().observe(this, s -> {
            if (!Objects.equals(s, Constants.NO_ERROR)){
                Snack.show(drawerLayout, s);
                Snack.log("Auth error", s);
            }
        });

        mainVM.getAuthIsValid().observe(this, b->{
            if (b){
                warning.setVisibility(new visibility().Set(false));
            }else{
                warning.setVisibility(new visibility().Set(true));
            }
        });

        mainVM.getUser().observe(this, user -> {
            if (user!=null){
                id.setText(user.getUid());
                checker.setVisibility(new visibility().Set(false));
            }else{
                Snack.log("Auth", "Null");
                checker.setVisibility(new visibility().Set(true));
            }
        });
    }
    private void TopicsFetch(){
        TopicsAdapter adapter = new TopicsAdapter();
        topics.setAdapter(adapter);
        mainVM.fetchTopics().observe(this, list-> {
            if (list!=null){
                if(list.size()>=1){
                    adapter.setTopics(list);
                    showList();
                }else{
                    hideList();
                }
            }else{
                hideList();
            }
        });
        adapter.setOnItemClickListener(id -> {
            Intent i = new Intent(HomeActivity.this, ThreadActivity.class);
            i.putExtra(Constants.INTENT_TOPIC, id+"");
            startActivity(i);
        });
    }
    private void setView() {
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        actionBarDrawerToggle.syncState();
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        View v = navigationView.getHeaderView(0);
        id = v.findViewById(R.id.uid);
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.settings) {
                Snack.show(drawerLayout, "Settings clicked");
            } else if (item.getItemId() == R.id.refresh) {
                Snack.show(drawerLayout, "Refresh clicked");
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }
}