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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FirebaseUtils {
    private static final MutableLiveData<Boolean> isValidAuth = new MutableLiveData<>(false);
    public static MutableLiveData<Boolean> isValidAuth() {
        return isValidAuth;
    }
    public static void setIsValidAuth(boolean validAuth) {
        isValidAuth.setValue(validAuth);
    }
    public static long timeConverter(long currentTimeMillis) {
        TimeZone singaporeTimeZone = TimeZone.getTimeZone(Constants.dbTimeZone);
        int timeZoneOffset = singaporeTimeZone.getOffset(currentTimeMillis);
        return currentTimeMillis - timeZoneOffset;
    }

    public static String getTime(long timeMillis){
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.timeFormat, Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }
}