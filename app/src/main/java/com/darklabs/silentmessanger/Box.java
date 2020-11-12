package com.darklabs.silentmessanger;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.UnsupportedEncodingException;

public class Box {
    public static String[][] Safe = new String[1024][7]; // message, to BTname, to BThardware address, BTuuid to, myBTuuid, publickey, pass
    public static int messagesIndex =0;
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void setNewMessage (String msg, String sendTo) throws UnsupportedEncodingException {
       String encrypted = Keygen.modRetyping(msg);
      // String pubKey = String.valueOf(Keygen.getPublicKey());
      // String signedOnKey = Keygen.setSign(encrypted, pubKey);
      // String pass = Arrays.toString(Keygen.passwd);
       Safe[messagesIndex][0] = encrypted;
       Safe[messagesIndex][1] = sendTo;

       Safe[messagesIndex][5] = BluetoothTrs.oneUUID;
      // Safe[messagesIndex][6] = pubKey;
      // Safe[messagesIndex][7] = pass;
       messagesIndex++;
    }
    public static byte[] getMessageToSend (int index){
        byte[] data = Safe[index][0].getBytes();
        return data;
    }

}
