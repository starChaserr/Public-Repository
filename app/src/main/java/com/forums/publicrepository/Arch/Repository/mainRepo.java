package com.forums.publicrepository.Arch.Repository;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.forums.publicrepository.Arch.Entity.Thread;
import com.forums.publicrepository.Arch.Entity.Topic;
import com.forums.publicrepository.Arch.Entity.User;
import com.forums.publicrepository.Arch.Firebase.Authenticate;
import com.forums.publicrepository.Arch.Firebase.Interactables;
import com.forums.publicrepository.Arch.Firebase.Threads;
import com.forums.publicrepository.Arch.Firebase.Topics;
import com.forums.publicrepository.utils.Constants;

import java.util.List;

public class mainRepo {
    private final Authenticate fAuth = new Authenticate();
    private final Topics fTopics = new Topics();
    private final Threads fThreads = new Threads();
    private final Interactables fInt = new Interactables();
    private final Application application;
    public mainRepo(Application application) {
        this.application = application;
    }

//Common--------------------------------------------------------------------------------------------
    public void clearErrors(){
        fAuth.clearError();
    }
//--------------------------------------------------------------------------------------------------

//Auth----------------------------------------------------------------------------------------------
    public void Auth(){
        fAuth.Auth(application);
    }

    public LiveData<User> getUser(){
        return fAuth.getUser();
    }

    public void createAnon(){
        fAuth.createAnon(application);
    }

    public synchronized LiveData<Boolean> getAuthIsValid(){
        return fAuth.getAuthIsValid();
    }

    public void setAuthCount(int count){
        fAuth.setAuthCount(count);
    }

    public LiveData<Integer> getAuthCount(){
        return fAuth.getAuthCount();
    }

    public LiveData<Integer> setPolicy(){
        return fAuth.setPolicy(fAuth.getUid().getValue());
    }

    public LiveData<String> getErrorCollector(){
        return fAuth.getErrorCollector();
    }
//--------------------------------------------------------------------------------------------------

//Topics--------------------------------------------------------------------------------------------
    public LiveData<List<Topic>> fetchTopics(){
        return fTopics.fetchTopics();
    }
//--------------------------------------------------------------------------------------------------

//Threads-------------------------------------------------------------------------------------------
    public LiveData<List<Thread>> getThreads(String Topic){
        return fThreads.getAllThreads(Topic);
    }

    public void addThread(Thread thread,@Nullable Uri filePath, @Nullable String uriType){
        fThreads.addThread(thread, filePath, uriType);
    }

    public LiveData<String> getPostError(){
        return fThreads.getPostErrors();
    }

    public LiveData<String> errorCollector(){
        return fThreads.getErrorCollector();
    }

    public LiveData<String> isLock(){
        return fThreads.isLock();
    }

    public LiveData<List<Thread>> getReplies(String tid){
        return fThreads.getReplies(tid);
    }

    public LiveData<Thread> getMessageById(String MsgLoc){
        return fThreads.getMessageById(MsgLoc);
    }
//--------------------------------------------------------------------------------------------------
//Interactables-------------------------------------------------------------------------------------
    public LiveData<Constants.MediaType> getMediaType(String Url){
        return fInt.getMediaType(Url);
    }
//--------------------------------------------------------------------------------------------------
}