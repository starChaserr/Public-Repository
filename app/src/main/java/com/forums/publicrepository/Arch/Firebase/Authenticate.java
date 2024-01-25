package com.forums.publicrepository.Arch.Firebase;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.forums.publicrepository.Arch.Entity.User;
import com.forums.publicrepository.utils.Constants;
import com.forums.publicrepository.utils.Snack;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Authenticate {

    private final MutableLiveData<String> errorCollector = new MutableLiveData<>(Constants.NO_ERROR);
    private final MutableLiveData<Integer> authCount;
    private final MutableLiveData<String> uid = new MutableLiveData<>(null);
    private final MutableLiveData<User> fUser = new MutableLiveData<>(null);
    private final FirebaseDatabase database;
    private final FirebaseAuth fAuth;
    private final FirebaseUser user;

    public Authenticate() {
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance(Constants.dbRef);
        authCount = new MutableLiveData<>(0);
    }

    public void Auth(Application app){
        clearError();
        if(user!=null){
            uid.setValue(user.getUid());
            getUser(user.getUid()).observeForever(user -> {
                FirebaseUtils.setIsValidAuth(true);
                fUser.setValue(user);
            });
        }else {
            AtomicInteger i = new AtomicInteger();
            authCount.observeForever(i::set);
            if (i.get() <1){
                createAnon(app);
            }
            FirebaseUtils.setIsValidAuth(false);
        }
    }

    public synchronized LiveData<Boolean> getAuthIsValid(){
        return FirebaseUtils.isValidAuth();
    }

    public void createAnon(Application app){
        fAuth.signInAnonymously()
                .addOnCompleteListener(app.getMainExecutor(), task -> {
                    if (task.isSuccessful()) {
                        // Authentication successful
                        String uId = Objects.requireNonNull(task.getResult()
                                .getUser()).getUid();
                        setUser(uId);
                        uid.setValue(uId);
                        fUser.setValue(getUser(uId).getValue());
                        FirebaseUtils.setIsValidAuth(true);
                    } else {
                        errorCollector.setValue(Objects.requireNonNull(task.getException()).toString());
                    }
                });
    }

    public void setAuthCount(int count){
        authCount.setValue(count);
    }

    public LiveData<User> getUser(){
        return fUser;
    }

    public LiveData<Integer> getAuthCount(){
        return authCount;
    }

    private LiveData<User> getUser(String uid){
        MutableLiveData<User> u = new MutableLiveData<>(new User());
        clearError();

        DatabaseReference uRef = database.getReference(Constants.users).child(uid);
        uRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                u.setValue(dataSnapshot.getValue(User.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snack.log("Auth", "GetUser error: "+error.getMessage());
                errorCollector.setValue(error.toString());
            }
        });

        return u;
    }
    private void setUser(String uid){
        clearError();
        DatabaseReference ref = database.getReference(Constants.users).child(uid);
        HashMap<String, String> map = new HashMap<>();
        map.put("uid", uid);
        map.put("policy", "false");
        ref.setValue(map, ((error, ref1) -> {
            if (error!=null){
                errorCollector.setValue(error.toString());
            }
        }));
    }

    public LiveData<Integer> setPolicy(String uid){
        MutableLiveData<Integer> isComplete = new MutableLiveData<>(0);
        clearError();
        DatabaseReference ref = database.getReference(Constants.users).child(uid);
        HashMap<String, String> map = new HashMap<>();
        map.put("uid", uid);
        map.put("policy", "false");
        ref.setValue(map, ((error, ref1) -> {
            if (error==null){
                isComplete.setValue(1);
            }else{
                isComplete.setValue(2);
                errorCollector.setValue(error.toString());
            }
        }));
        return isComplete;
    }

//    Fetch all topics
//    Fetch all threads under selected topic
//    Send message

    public void clearError(){
        errorCollector.setValue(Constants.NO_ERROR);
    }

    public MutableLiveData<String> getUid(){
        return uid;
    }
    public MutableLiveData<String> getErrorCollector(){
        return errorCollector;
    }
}