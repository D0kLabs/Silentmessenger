package com.darklabs.silentmessanger;

import android.os.Build;

import androidx.annotation.RequiresApi;

public class Box {
    public static String[][] Safe = new String[1024][2]; // message, to, publickey, pass
    public static int messagesIndex =0;
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void setNewMessage (String msg, String sendTo){
       String encrypted = Keygen.getEncrypted(msg);
       Safe[messagesIndex][0] = encrypted;
       Safe[messagesIndex][1] = sendTo;
       messagesIndex++;
    }
}
