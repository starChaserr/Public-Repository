package com.forums.publicrepository.Arch.Firebase;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.forums.publicrepository.Arch.Entity.Topic;
import com.forums.publicrepository.utils.Constants;
import com.forums.publicrepository.utils.Snack;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class Topics {

    private final MutableLiveData<List<Topic>> allTopics = new MutableLiveData<>();
    private final FirebaseDatabase database;
    public Topics() {
        database = FirebaseDatabase.getInstance(Constants.dbRef);
    }

    public LiveData<List<Topic>> fetchTopics(){
        allTopics.setValue(null);
        database.getReference(Constants.topics).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.getValue() instanceof List && ((List<?>) dataSnapshot.getValue()).size()
                            > 0 && ((List<?>) dataSnapshot.getValue()).get(0) instanceof String) {
                        allTopics.setValue((List<Topic>) dataSnapshot.getValue());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snack.log("Topics", "Error: "+error.getMessage());
            }
        });
        return allTopics;
    }

}
