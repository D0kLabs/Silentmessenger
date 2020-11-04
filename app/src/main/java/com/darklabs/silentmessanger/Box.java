package com.darklabs.silentmessanger;

import android.os.Build;

import androidx.annotation.RequiresApi;

public class Box {
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static int setBox(String data){
        int index =0;
        String[] safe =null;
        for (int i=0; i>safe.length; i++){
            safe[i] = data;
            index++;
        }
        return index;
    }
}
