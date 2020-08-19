package com.darklabs.silentmessangerrebuild;

import android.os.Build;

import java.io.UnsupportedEncodingException;
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

public class Box {
    private int mTextResId;
    private String mTextRes;
    private boolean mFilled = false;
    private int mSize;

    public int getTextResId() {
        return mTextResId;
    }

    public void setTextResId(int textResId) {
        mTextResId = textResId;


    }

    public boolean isFilled() {
        return mFilled;
    }

    public void setFilled(boolean filled) {
        mFilled = filled;
    }

    public int getSize() {
        return mSize;
    }

    public void setSize(int size) {
        mSize = size;
    }

    public String setTextRes(String msg) throws NoSuchAlgorithmException, NoSuchPaddingException {
        mTextRes = msg;
        mRetyping();
        return msg;
    }

    private void mRetyping() throws NoSuchPaddingException, NoSuchAlgorithmException {
        byte[] toSign = null;
        try{
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
    }
}
