package com.darklabs.silentmessangerrebuild;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Enumeration;
import javax.crypto.SecretKey;


public class Keygen {
    static KeyStore BluetoothKeys;
    static KeyStore OwnKeystore;
    static KeyPairGenerator keyPairGen;
    static Enumeration<String> mEnumeration;
    public static Certificate globalPublicCert = null;
    public static char[] passwd = ("1234567890").toCharArray();

    public static boolean findByte (byte[] a, byte[] b){ // replace to class box?
        boolean bool= false;
        for (int i = 0; i <a.length ; i++) {
            if (a[i]==b[0]){
                for (int ii=1; ii<b.length; ii++){
                    if (a[i+ii] != b[ii]) break;
                    else if (ii == b.length-1) bool = true;
                }
            }
        }
        return bool;
    }
    static String fSearchFile(String mName) {
        File actual = new File(mName);
        String fileName = "";
        for (File z : actual.listFiles()) {
            fileName = z.getName();
        }
        return fileName;
    }

    {
        try {
            OwnKeystore = KeyStore.getInstance("AndroidCAStore");
            BluetoothKeys = KeyStore.getInstance("AndroidCAStore");
            InputStream io = new FileInputStream("OWNcertStore.store");
            InputStream bt = new FileInputStream("BTcertStore.store");
            OwnKeystore.load(io, passwd);
            BluetoothKeys.load(bt, passwd);
            mEnumeration = BluetoothKeys.aliases();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //TODO:find to first(if it find), else new
    }

    static {
        try {
            keyPairGen = KeyPairGenerator.getInstance("BLOWFISH");
    } catch (){}
    }

    public static KeyPair NewPair() throws CertificateException {
        KeyPair two = keyPairGen.generateKeyPair();
        byte[] buffer = two.getPublic().getEncoded();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        while (byteArrayInputStream.available() > 0) {
            globalPublicCert = certificateFactory.generateCertificate(byteArrayInputStream);
            if (mEnumeration.hasMoreElements()){
                String alias = mEnumeration.nextElement();
                try {
                    BluetoothKeys.setKeyEntry(alias,two.getPublic(),passwd, new Certificate[]{globalPublicCert});
                    OwnKeystore.setKeyEntry(alias,two.getPrivate(),passwd, new Certificate[]{globalPublicCert}); //need private cert?
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                }
            }

        }
        return two;
    }



    public static PublicKey getgPublicKey() throws CertificateException {
        KeyPair two = NewPair();
        PublicKey gPublicKey = two.getPublic();
        return gPublicKey;
    }



    private void setBluetoothKeys(Key mPublic) { //!?
        //FROM WIFI Public key to mPublic and its current
        if (mPublic != null) {
            javax.crypto.SecretKey currentPubKey = (SecretKey) mPublic;
            KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(currentPubKey);
            if (BluetoothTrs.trusted.peek() != null) {
                String mName = BluetoothTrs.trusted.peek();
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(BluetoothTrs.trusted.poll(), false);
                    fileOutputStream.write(currentPubKey.getEncoded());
                    fileOutputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            // TO ACTIVITY!
            // else toast = R.string.error; ...
        }
    }

    private void getBluetoothKeys() throws KeyStoreException { //!?
        String mfileName = fSearchFile(BluetoothTrs.trusted.peek());
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(mfileName);
            fileInputStream.read();
            javax.crypto.SecretKey currentKey = (SecretKey) fileInputStream;
            if (keyStoreCompare(currentKey)) {
                setBluetoothKeys(currentKey);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public boolean keyStoreCompare(Key Public) throws KeyStoreException { //Wrong <!rewrite! has own alias
        boolean state = false;
        String peek = BluetoothTrs.trusted.peek();
        Enumeration<String> aliases = BluetoothKeys.aliases();
        String mName = null;
        if (BluetoothKeys.containsAlias(peek)) {
            for (String i = null; i == peek; i = aliases.nextElement()) {
                mName = i;
            }
            try {
                while (mName != null) {
                    if (BluetoothKeys.containsAlias(mName)) {
                        if ((BluetoothKeys.getKey(mName, passwd)) == Public) {
                            state = true;
                        }
                    }
                }
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();
            }
        }
        return state;
    }
    public byte[] getEncrypted(byte[] data){
        //Encrypt data
        return data;
    }
    public byte[] getDecrypt(byte[] data){
        //Decrypt data
        return data;
    }
}
