package com.darklabs.silentmessangerrebuild;

import android.os.Build;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.crypto.Cipher.ENCRYPT_MODE;
import static javax.crypto.Cipher.getInstance;

import java.util.Arrays;

public class Box<ResId> {
    static int ResId =1;
    public static byte[][] BlackBox = new byte [1][1];



    public static void setTextRes(String msg){ // as array working when where still energy or app run, so that are best secure practise. app closed - data lost, messages grabbed by garbage cleaner
        try {
            //ResId++;
            byte[] data = mRetyping(msg);
            BlackBox[ResId] = new byte[data.length]; //array reference needed. && wtf?
            for (int i = 0; i < data.length ; i++) {
                BlackBox[ResId][i] = data[i];
            }
            } catch (NoSuchPaddingException ex) {
            ex.printStackTrace();
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (ShortBufferException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    private static byte[] mRetyping(String mTextRes) throws NoSuchAlgorithmException, CertificateException, InvalidKeyException, ShortBufferException, NoSuchPaddingException {
        byte[] toSign = new byte[mTextRes.length()];

        byte[] ch = mTextRes.getBytes();
        Cipher encode = Cipher.getInstance("BLOWFISH/OFB32/NoPadding"); // There are no such one
        PrivateKey mPrivkey = Keygen.NewPair().getPrivate();
        encode.init(ENCRYPT_MODE, mPrivkey);
        encode.update(ch, 64, 8, toSign);

        // review byte ch vs toSign
        return toSign;
    }
}
