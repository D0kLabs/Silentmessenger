package com.darklabs.silentmessanger;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class Box {
    public static int messagesIndex =0;
    public static String[][] Safe = new String[1024][7]; // message, to BTname, to BThardware address, BTuuid to, myBTuuid, publickey, pass
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static int setNewMessage (String msg) throws UnsupportedEncodingException {
        messagesIndex++;
       String encrypted = Keygen.getEncrypted(msg);
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
    /**
     * Returns the unique identifier for the device
     *
     * @return unique identifier for the device
     */
    public static String getSilentUUID(){
        String sDevID = "" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10) + (Build.CPU_ABI.length() % 10) + (Build.DEVICE.length() % 10) + (Build.MANUFACTURER.length() % 10) + (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10);
        String serial = null;
        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();

            // Go ahead and return the serial for api => 9
            return new UUID(sDevID.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            // String needs to be initialized
            serial = "0000"; // some value
        }

        return new UUID(sDevID.hashCode(), serial.hashCode()).toString();
    }
}
