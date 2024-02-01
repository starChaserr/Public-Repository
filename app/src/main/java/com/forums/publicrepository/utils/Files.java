package com.forums.publicrepository.utils;

import android.content.Context;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

public class Files {

    public long getFileSize(Uri fileUri, Context context) {
        try {
            // Open an input stream from the file URI and get the size
            try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri)) {
                return inputStream != null ? inputStream.available() : 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
