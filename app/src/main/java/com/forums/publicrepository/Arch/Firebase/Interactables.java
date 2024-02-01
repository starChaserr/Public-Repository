package com.forums.publicrepository.Arch.Firebase;

import static com.forums.publicrepository.utils.Constants.getMediaTypeFromContentType;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.forums.publicrepository.utils.Constants;
import com.forums.publicrepository.utils.Snack;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

public class Interactables {
    private FirebaseStorage storage;

    public Interactables() {
        this.storage = FirebaseStorage.getInstance(Constants.storageRef);
    }

    public LiveData<Constants.MediaType> getMediaType(String Url){
        MutableLiveData<Constants.MediaType> contentType = new MutableLiveData<>(null);
        StorageReference storageRef = storage.getReferenceFromUrl(Url);
        storageRef.getMetadata().addOnSuccessListener(storageMetadata -> {
            String type = storageMetadata.getContentType();
            Snack.log("ContentType", type);
            contentType.setValue(getMediaTypeFromContentType(type));
        });
        return contentType;
    }
}
