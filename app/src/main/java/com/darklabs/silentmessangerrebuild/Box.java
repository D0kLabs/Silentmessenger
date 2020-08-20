package com.darklabs.silentmessangerrebuild;

import android.os.Build;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import static javax.crypto.Cipher.ENCRYPT_MODE;
import static javax.crypto.Cipher.getInstance;

import java.util.Arrays;

public class Box {
    private int mSize;
    public static byte[][][] BlackBox;
    static int ResId =0;


    public static void setTextRes(String msg){ // as array working when where still energy or app run, so that are best secure practise. app closed - data lost, messages grabbed by garbage cleaner
        try {
            ResId++;
            byte[] data = mRetyping(msg);
            BlackBox[ResId] = new byte[ResId][data.length]; //array reference needed. && wtf?
            Arrays.fill(BlackBox[ResId],data);
            } catch (NoSuchPaddingException ex) {
            ex.printStackTrace();
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
    }

    private static byte[] mRetyping(String mTextRes) throws NoSuchPaddingException, NoSuchAlgorithmException {
        byte[] toSign = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                byte[] ch = mTextRes.getBytes(StandardCharsets.UTF_8);
                Cipher encode = getInstance("BLOWFISH/OFB32/ISO10126Padding");
                encode.init(ENCRYPT_MODE, Keygen.getgPublicKey(), SecureRandom.getInstanceStrong());
                encode.update(ch, 64, 8, toSign);
            }
        } catch (InvalidKeyException | ShortBufferException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        KeyPairGenerator pSignKey = KeyPairGenerator.getInstance("RSA");
        KeyPair pair = pSignKey.generateKeyPair();
        Signature signature = Signature.getInstance("DiffieHellman");
        try {
            signature.initSign(pair.getPrivate());
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        try {
            signature.update(toSign);
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        // review byte ch vs toSign
        return toSign;
    }
}
