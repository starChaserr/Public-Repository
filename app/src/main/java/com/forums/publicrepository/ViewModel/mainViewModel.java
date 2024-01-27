package com.forums.publicrepository.ViewModel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.forums.publicrepository.Arch.Entity.Thread;
import com.forums.publicrepository.Arch.Entity.Topic;
import com.forums.publicrepository.Arch.Entity.User;
import com.forums.publicrepository.Arch.Repository.mainRepo;

import java.util.List;

public class mainViewModel extends AndroidViewModel {

    private final mainRepo repo;
    public mainViewModel(@NonNull Application application) {
        super(application);
        repo = new mainRepo(application);
    }

//Common--------------------------------------------------------------------------------------------
    public void clearErrors(){
        repo.clearErrors();
    }
//--------------------------------------------------------------------------------------------------
//Authenticate--------------------------------------------------------------------------------------
    public void Auth(){
        repo.Auth();
    }

    public LiveData<User> getUser(){
        return repo.getUser();
    }

    public synchronized LiveData<Boolean> getAuthIsValid(){
        return repo.getAuthIsValid();
    }

    public void createAnon(){
        repo.createAnon();
    }

    public LiveData<String> getErrorCollector(){
        return repo.getErrorCollector();
    }

    public void setAuthCount(int count){
        repo.setAuthCount(count);
    }

    public LiveData<Integer> getAuthCount(){
        return repo.getAuthCount();
    }

    public LiveData<Integer> setPolicy(){
        return repo.setPolicy();
    }

//--------------------------------------------------------------------------------------------------
//Topics--------------------------------------------------------------------------------------------
    public LiveData<List<Topic>> fetchTopics(){
        return repo.fetchTopics();
    }
//--------------------------------------------------------------------------------------------------
//Threads-------------------------------------------------------------------------------------------
    public LiveData<List<Thread>> getThreads(String Topic){
        return repo.getThreads(Topic);
    }

    public void addThread(Thread thread,@Nullable Uri filePath){
        repo.addThread(thread, filePath);
    }

    public LiveData<String> getPostError(){
        return repo.getPostError();
    }

    public LiveData<String> errorCollector(){
        return repo.errorCollector();
    }

    public LiveData<String> isLock(){
        return repo.isLock();
    }

    public LiveData<List<Thread>> getReplies(String tid){
        return repo.getReplies(tid);
    }

    public LiveData<Thread> getMessageById(String MsgLoc){
        return repo.getMessageById(MsgLoc);
    }
//--------------------------------------------------------------------------------------------------
}