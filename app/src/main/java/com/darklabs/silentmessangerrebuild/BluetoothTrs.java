package com.darklabs.silentmessangerrebuild;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class BluetoothTrs {
    public static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static Queue<String> trusted = new LinkedBlockingQueue<>();
    public static Queue<String> found = new LinkedBlockingQueue<>();
    public static Queue<String> current = new LinkedBlockingQueue<>();
    private static String[] mBTtrustedIDs =null;
    private static final File BTNetworkTrusted = new File(String.valueOf(R.string.BTTrustedIDs));

    /*public static byte[] getMyCurrentConfig{
        byte[] data=null;
        //parce configuration of Wifi (Name, Address, UUID)


        return data;
    }
*/
    public static void BtFinder() {
        //Switch On and find paired
            mBluetoothAdapter.enable(); // reconfig emulator! bt do nothing
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            mBluetoothAdapter.startDiscovery();
            BroadcastReceiver mDiscovery = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        found.add(device.getName() + "\t" + device.getAddress() + "\t" + device.getUuids());
                    }
                    if (BtCompare()){
                        // Send public cert
                        // get target public cert
                        // sign myConfig by target public
                        // get signed target "myConfig" and check it at current
                        // if it`s true add target config to trusted

                    }
                }
            };
    }

    public static boolean BtCompare(){
        // current = some to deliver or to hear
        // parsed from wifi PublicKey and its cert
        boolean inlist = false;
        try {
            FileInputStream mfileInputStream = new FileInputStream(BTNetworkTrusted);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(mfileInputStream));
            for (int i = 0; i <mBTtrustedIDs.length ; i++) {
                mBTtrustedIDs[i] = bufferedReader.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (found.contains(current.peek())){
            // its must on other instances
            if (trusted.contains(current.peek())){
                // found - listof founded Names \t MACAdreses \t UUID
                // trusted - already paired and synced IDs
                // current - target Name \t MACAdress \t UUID
                inlist = true;
            }
        }
        return inlist;

    }
    private class BTServerThead extends Thread{
        private BluetoothServerSocket mBluetoothServerSocket =null;
        String mUuid = null;
        Method getUuid; // wrong method

        {
            try {
                getUuid = BluetoothServerSocket.class.getDeclaredMethod("getUuids", null);
                ParcelUuid[] uuids = (ParcelUuid[]) getUuid.invoke(mBluetoothAdapter,null);

                for (ParcelUuid uuid : uuids){
                    mUuid = uuid.getUuid().toString();

                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        public BTServerThead() {
            BluetoothServerSocket tmp= null;
            try{

                tmp =mBluetoothAdapter.listenUsingRfcommWithServiceRecord(mBluetoothAdapter.getName(), UUID.fromString(mUuid));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mBluetoothServerSocket = tmp;
        }
        public void run(){
            BluetoothSocket socket = null;
            while (true){
                try{
                    socket = mBluetoothServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        public void cancel(){
            try{
                mBluetoothServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    private class ClientThead extends Thread{
        private BluetoothSocket mSocket;
        private BluetoothDevice mDevice = null;

        public ClientThead(BluetoothDevice device){
            BluetoothSocket tmp = null;
            mDevice=device;
  /*          try {
                tmp = device.createRfcommSocketToServiceRecord(mCurrent());
                mSocket = tmp;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

   /*         private UUID mCurrent() {
            UUID current = (UUID)
                return UUID;
            }

            public void run(){
            mBluetoothAdapter.cancelDiscovery();
            try {
                mSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /// Upravlenie soketom
            public void cancel(){
            try{
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
*/            }
    }

    private class ReciverThead extends Thread{
        private BluetoothSocket mSocket;
        private InputStream mInputStream;
        private OutputStream mOutputStream;

        public ReciverThead (BluetoothSocket socket){
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try{
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mInputStream = tmpIn;
            mOutputStream= tmpOut;
        }
        public byte[] read() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true){
                try{
                    bytes = mInputStream.read(buffer);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // return buffer;
        }
        public void write (byte[] bytes) {
            try{
                mOutputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void cancel(){
            try{
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
