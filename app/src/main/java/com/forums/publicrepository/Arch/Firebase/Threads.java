package com.forums.publicrepository.Arch.Firebase;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.forums.publicrepository.Arch.Entity.Thread;
import com.forums.publicrepository.utils.Constants;
import com.forums.publicrepository.utils.Snack;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Threads {
    private final FirebaseDatabase database;
    private final FirebaseStorage storage;
    private final StorageReference storageReference;
    private final MutableLiveData<String> errorCollector = new MutableLiveData<>(Constants.NO_ERROR);
    private final MutableLiveData<String> postErrors = new MutableLiveData<>(Constants.NO_ERROR);
    private final MutableLiveData<Long> serverTime = new MutableLiveData<>(null);
    private final MutableLiveData<String> threadNum = new MutableLiveData<>(null);
    private MutableLiveData<String> mID = new MutableLiveData<>(null);
    private final AtomicInteger isPosted = new AtomicInteger(Constants.NOT_POSTED);
    private Observer<Long> timeObserver;
    private Observer<String> mIdObserver;
    private final MessageID mId = new MessageID();

    public Threads() {
        this.database = FirebaseDatabase.getInstance(Constants.dbRef);
        this.storage = FirebaseStorage.getInstance(Constants.storageRef);
        this.storageReference = storage.getReference();
    }

//To be called in repo------------------------------------------------------------------------------
    public LiveData<List<Thread>> getAllThreads(String Topic) {
//        To get all threads under the same topic
        final MutableLiveData<List<Thread>> threads = new MutableLiveData<>(null);
        DatabaseReference dbRef = database.getReference(Constants.chats).child(Topic);

        dbRef.child(Constants.threadIDs).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<String> ids = new ArrayList<>();
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String threadId = String.valueOf(childSnapshot.getValue());
                        ids.add(threadId);
                    }
                    getThreadData(ids, dbRef, threads);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorCollector.postValue(error.getMessage());
            }
        });
        return threads;
    }
    public void addThread(Thread thread, @Nullable Uri filePath, @Nullable String uriType)  {
        List<String> threadLoc = msgLocSplit(thread.getMsgLoc());
        postErrors.postValue(null);
        String topic = threadLoc.get(0);
        isPosted.set(Constants.NOT_POSTED);

        timeObserver = aLong -> {
            if (aLong!=null && aLong!=0L){
               mIdObserver = id ->{
                   if (id!=null){
                       addThreadProcess(thread, id, aLong, filePath, uriType, threadLoc, topic);
                       removeMID_Observer();
                   }
                };
               getMID().observeForever(mIdObserver);
                removeTimeObserver();
            }

        };
        LiveData<Long> time = getServerTime();
        time.observeForever(timeObserver);
    }
    public LiveData<String> getPostErrors() {
        return postErrors;
    }
    public LiveData<String> getErrorCollector() {
        return errorCollector;
    }
    public LiveData<String> isLock() {
        return mId.isLock();
    }
    public LiveData<List<Thread>> getReplies(String tid) {
        MutableLiveData<List<Thread>> allReplies = new MutableLiveData<>(null);
        List<String> location = msgLocSplit(tid);

        database.getReference(Constants.chats).child(location.get(0))
                .child(Constants.messages).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            List<Thread> allMessages = new ArrayList<>();

                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                // Assuming childSnapshot contains the message data directly
                                Map<String, Object> messageData = (Map<String, Object>) childSnapshot.getValue();

                                if (messageData != null && messageData.containsKey("msgLoc") &&
                                        messageData.get("msgLoc").equals(tid)) {
                                    String id = (String) messageData.get("id");
                                    String title = (String) messageData.get("title");
                                    String body = (String) messageData.get("body");
                                    String imgUrl = (String) messageData.get("imgURL");
                                    String msgLoc = (String) messageData.get("msgLoc");
                                    long time = (long) messageData.get("creationTime");
                                    Thread thread = new Thread(id, title, body, imgUrl, msgLoc, time);
                                    allMessages.add(thread);
                                }
                            }

                            allReplies.setValue(allMessages);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        errorCollector.postValue(error.getMessage());
                    }
                });

        return allReplies;
    }
    public LiveData<Thread> getMessageById(String msgLoc) {
        List<String> location = msgLocSplit(msgLoc);
        MutableLiveData<Thread> thread = new MutableLiveData<>(null);

        DatabaseReference msgRef = database.getReference(Constants.chats).child(location.get(0))
                .child(Constants.messages).child(location.get(1));

        msgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() instanceof Map) {
                    Map<String, Object> messageData = (Map<String, Object>) snapshot.getValue();

                    // Ensure the messageData contains the necessary fields
                    if (messageData.containsKey("msgLoc") && messageData.containsKey("id")
                            && messageData.containsKey("title") && messageData.containsKey("body")
                            && messageData.containsKey("imgURL") && messageData.containsKey("creationTime")) {

                        String id = (String) messageData.get("id");
                        String title = (String) messageData.get("title");
                        String body = (String) messageData.get("body");
                        String imgUrl = (String) messageData.get("imgURL");
                        String msgLoc = (String) messageData.get("msgLoc");
                        long time = FirebaseUtils.timeConverter((long) messageData.get("creationTime"));

                        // Create the Thread object
                        Thread message = new Thread(id, title, body, imgUrl, msgLoc, time);

                        // Post the value to the LiveData
                        thread.postValue(message);
                    } else {
                        // Handle missing fields or unexpected data
                        // You might want to log an error or notify the caller
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled, log an error, or notify the caller
            }
        });

        return thread;
    }

//--------------------------------------------------------------------------------------------------

//Hidden stuff--------------------------------------------------------------------------------------
    private LiveData<String> getMID() {
        mID = (MutableLiveData<String>) mId.getMID();
        return mID;
    }
    private void putInMsgs(Thread thread, String topic) {
        if (isPosted.get() == Constants.NOT_POSTED) {
            HashMap<String, Object> map = new HashMap<>();
            map.put(thread.getId(), thread);
            database.getReference(Constants.chats).child(topic).child(Constants.messages)
                    .updateChildren(map, (error, ref) -> {
                        if (error == null && isPosted.get()==Constants.NOT_POSTED) {
                            isPosted.set(Constants.POSTED);
                            mId.incrementMID();
                            mId.ReleaseLock();
                        }
                    });
        }
    }
    private void addThreadProcess(Thread thread, String id, long aLong,
                                  @Nullable Uri filePath, @Nullable String uriType, List<String> threadLoc, String topic) {
        thread.setId(id);
        thread.setCreationTime(aLong);
        mId.LockID();
        if (isPosted.get() == Constants.NOT_POSTED) {
            if (filePath != null && uriType != null) {
                // Upload the media (image) and handle further processing in the uploadMedia method
                uploadMedia(thread, filePath, uriType, imageUrl -> {
                    // Set the uploaded image URL in the Thread entity
                    thread.setImgURL(imageUrl);

                    // Continue with the normal thread addition process
                    threadAdd(threadLoc, topic, thread);
                });
            } else {
                // No media (image) to upload, proceed with the normal thread addition
                threadAdd(threadLoc, topic, thread);
            }

        }
    }
    private void threadAdd(List<String> threadLoc, String topic, Thread thread){
        if (threadLoc.size() < 2) {
            AtomicReference<Observer<String>> threadObserverRef = new AtomicReference<>();
            threadObserverRef.set(integer -> {
                if (integer != null && isPosted.get() == Constants.NOT_POSTED) {
                    setThreadNum(String.valueOf(Integer.parseInt(integer) + 1), topic);

                    HashMap<String, Object> map = new HashMap<>();
                    map.put(integer, thread.getId());
                    DatabaseReference threadIdsRef = database.getReference(Constants.chats).child(topic).child(Constants.threadIDs);
                    threadIdsRef.updateChildren(map, (error, ref) -> {
                        if (error != null) {
                            postErrors.setValue(error.getMessage());
                            } else {
                            putInMsgs(thread, topic);
                            }
                    });
                    removeThreadObserver(threadObserverRef.get());
                }
            });
            getThreadNum(topic).observeForever(threadObserverRef.get());
        } else {
            // a normal message.
            putInMsgs(thread, topic);
        }
    }
    private List<String> msgLocSplit(String r) {
        String[] parts = r.split("/");
        return Arrays.asList(parts);
    }
    private void getThreadData(List<String> ids, DatabaseReference ref, MutableLiveData<List<Thread>> threads) {
        List<Thread> data = new ArrayList<>();
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Thread t = dataSnapshot.getValue(Thread.class);
                    if (t != null) {
                        t.setCreationTime(FirebaseUtils.timeConverter(t.getCreationTime()));
                        data.add(t);
                        threads.setValue(data);
                        ref.removeEventListener(this); // Remove the listener after data is retrieved
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                errorCollector.postValue(databaseError.getMessage());
            }
        };

        for (int i = 0; i < ids.size(); i++) {
            ref.child(Constants.messages).child(ids.get(i)).addValueEventListener(eventListener);
        }
    }
    private LiveData<String> getThreadNum(String Topic) {
        DatabaseReference threadRef = database.getReference(Constants.chats).child(Topic).child(Constants.threadNum);

        threadRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nums = snapshot.getValue(String.class);
                    threadNum.setValue(nums);
                    Snack.log("Debug", "Exists");
                } else {
                    threadRef.setValue("1");
                    threadNum.setValue("1");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorCollector.postValue(error.getMessage());
            }
        });

        return threadNum;
    }
    private void setThreadNum(String num, String Topic){
        DatabaseReference threadRef = database.getReference(Constants.chats)
                .child(Topic).child(Constants.threadNum);
        threadRef.setValue(num);
    }
    private void uploadMedia(Thread entity, Uri filePath, String uriType, UploadCallback callback) {
        StorageReference ref = storageReference.child("media/" + entity.getId());
        // Determine the content type based on the file extension
        String contentType, Video = "video/mp4", Image = "image/jpeg";
        if (uriType.equals(Constants.INTENT_VIDEO)) {
            contentType = Video;
        } else if (uriType.equals(Constants.INTENT_IMAGE)) {
            contentType = Image;
        } else {
            // Default for unknown types (Other types)
            contentType = "application/octet-stream";
        }

        // Upload the file with the determined content type
        if (contentType.equals(Video) || contentType.equals(Image)){
            ref.putFile(filePath, new StorageMetadata.Builder()
                            .setContentType(contentType)
                            .build())
                    .addOnSuccessListener(taskSnapshot -> {
                        if (taskSnapshot.getError() == null) {
                            Snack.log("Media", "Uploaded!");

                            // Get the download URL
                            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                // Set the media URL in the Thread entity
                                entity.setImgURL(uri.toString());

                                // Notify the callback with the media URL
                                callback.onUploadSuccess(entity.getImgURL());
                            });
                        } else {
                            errorCollector.postValue(Objects.requireNonNull(taskSnapshot.getError()).getMessage());
                            Snack.log("Media", "Error: " + taskSnapshot.getError().getMessage());
                        }
                    });
        }
    }


    private LiveData<Long> getServerTime(){
        DatabaseReference timestampRef = FirebaseDatabase.getInstance(Constants.dbRef)
                .getReference(".info/serverTimeOffset");
        timestampRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long serverTimeOffset = snapshot.getValue(Long.class);
                if (serverTimeOffset != null) {
                    long currentServerTime = System.currentTimeMillis() + serverTimeOffset;
                    serverTime.setValue(currentServerTime);
                    timestampRef.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                Snack.log("Server time", error.getMessage());
            }
        });
        return serverTime;
    }
    private void removeMID_Observer(){
        if (mID!=null){
            mID.removeObserver(mIdObserver);
            mIdObserver = null;
        }
    }
    private void removeThreadObserver(Observer<String> observer) {
        if (threadNum != null) {
            threadNum.removeObserver(observer);
        }
    }
    private void removeTimeObserver() {
        if (serverTime != null) {
            serverTime.removeObserver(timeObserver);
            timeObserver = null;
        }
    }
    private interface UploadCallback {
        void onUploadSuccess(String imageUrl);
    }

//--------------------------------------------------------------------------------------------------
}