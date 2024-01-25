package com.forums.publicrepository.utils;

import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class Snack {
    public static void show(View view, String Text){
        Snackbar.make(view, Text, Snackbar.LENGTH_SHORT).show();
    }

    public static void log(String head, String msg){
        Log.e(head, msg);
    }
}
