package com.forums.publicrepository.Arch.Firebase;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.forums.publicrepository.utils.Constants;
import com.forums.publicrepository.utils.Snack;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MessageID {

    private final FirebaseDatabase database;

    public MessageID() {
        database = FirebaseDatabase.getInstance(Constants.dbRef);
    }

    public LiveData<String> getMID(){
        MutableLiveData<String> id = new MutableLiveData<>(null);
        DatabaseReference idRef = database.getReference(Constants.mID).child("mID");
        idRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String mId = snapshot.getValue(String.class);
                    if (mId!=null){
                        id.setValue(mId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snack.log("Message ID error", error.getMessage());
            }
        });
        return id;
    }

    public void incrementMID(){
        DatabaseReference incRef = database.getReference(Constants.mID).child("mID");
        incRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String mId = snapshot.getValue(String.class);
                    if (mId!=null){
                        incRef.removeEventListener(this);
                        int increment = Integer.parseInt(mId) + 1;
                        String mid = String.valueOf(increment);
                        database.getReference(Constants.mID).child("mID").setValue(mid, (error, ref) -> {
                            if (error != null) {
                                Snack.log("Message ID set error", error.getMessage());
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snack.log("Message ID error", error.getMessage());
            }
        });
    }

    public LiveData<String> isLock(){
//        returns: null/ Constants.nullLock/ Locked ID.
        MutableLiveData<String> lock = new MutableLiveData<>(null);
        database.getReference(Constants.Lock).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    lock.setValue(snapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snack.log("isLock Error", error.getMessage());
            }
        });
        return lock;
    }

    public void LockID(){
        DatabaseReference lockRef = database.getReference(Constants.mID).child("mID");
        lockRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String mId = snapshot.getValue(String.class);
                    setLockValue(mId);
                    lockRef.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snack.log("Message ID error", error.getMessage());
            }
        });
    }

    private void setLockValue(String id){
        database.getReference(Constants.Lock).setValue(id);
    }
    public void ReleaseLock(){
        DatabaseReference ref = database.getReference(Constants.Lock);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String id = snapshot.getValue(String.class);
                    if (id!=null&&!id.equals(Constants.nullLock)){
                        setLockValue(Constants.nullLock);
                        ref.removeEventListener(this);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snack.log("ClearFreeze", error.getMessage());
            }
        });
    }
}
