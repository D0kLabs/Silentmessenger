package com.darklabs.silentmessanger;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.UnsupportedEncodingException;

public class Box {
    public static int messagesIndex =0;
    public static int incomingIndex=0;
    public static String[][] Safe = new String[1024][7]; // message, to BTname, to BThardware address, BTuuid to, myBTuuid, publickey, pass
    public static String[][] Incoming = new String[1024][7];
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static int setNewMessage (String msg) throws UnsupportedEncodingException {
        messagesIndex++;
       String encrypted = Keygen.modRetyping(msg);
      // String pubKey = String.valueOf(Keygen.getPublicKey());
      // String signedOnKey = Keygen.setSign(encrypted, pubKey);
      // String pass = Arrays.toString(Keygen.passwd);
      //  String deviceName = String.valueOf(sendTo.getName());
       Safe[messagesIndex][0] = String.valueOf(encrypted);
      // Safe[messagesIndex][1] = deviceName;

      // Safe[messagesIndex][5] = BluetoothTrs.oneUUID;
      // Safe[messagesIndex][6] = pubKey;
      // Safe[messagesIndex][7] = pass;

       return messagesIndex;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static int setNewIncoming (String msg) {
        incomingIndex++;
        String decrypted = Keygen.deRetyping(msg);
        Incoming[incomingIndex][0] = String.valueOf(decrypted);

        return incomingIndex;
    }
    /*public static byte[] getMessageToSend (int index){
        byte[] data = Safe[index][0].length();
        return data;
    }
    public static String getStringToSend (int index){
        String data = String.valueOf(Safe[index][0]);
        return data;
    }


     */
    public static String compressor (String in){
        String out = lzw.lzw_compress(in);
        RLE rle = new RLE();
        out = rle.compress(out);
        return out;
    }
    public static String decompressor (String in){
        RLE rle = new RLE();
        String out = rle.decompress(in);
        out = lzw.lzw_extract(out);
        return out;
    }
}
