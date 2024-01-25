package com.forums.publicrepository.utils;

import android.view.View;

public class visibility {

    public int Set(boolean b){
        if (b){
            return View.VISIBLE;
        }else{
            return View.GONE;
        }
    }
}
