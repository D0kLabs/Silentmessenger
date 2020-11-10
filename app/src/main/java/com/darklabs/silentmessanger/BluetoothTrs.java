package com.darklabs.silentmessanger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class BluetoothTrs {
    public static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static Queue<String> trusted = new LinkedBlockingQueue<>();
    public static Queue<String> found = new LinkedBlockingQueue<>();
    public static Queue<String> current = new LinkedBlockingQueue<>();

    /*public static byte[] getMyCurrentConfig{
        byte[] data=null;
        //parce configuration of Wifi (Name, Address, UUID)


        return data;
    }
*/
    public static void BtFinder(IntentFilter mBTFilter) {
        //Switch On and find paired
        mBluetoothAdapter.enable(); // reconfig emulator! bt do nothing
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(mBluetoothAdapter.ACTION_REQUEST_ENABLE);
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();

    }
    private static final BroadcastReceiver mDiscovery = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                found.add(device.getName() + "\t" + device.getAddress() + "\t" + device.getUuids());
            }
            // Send public cert
            // get target public cert
            // sign myConfig by target public
            // get signed target "myConfig" and check it at current
            // if it`s true add target config to trusted

        }
    };

    public static boolean BtCompare() {
        // current = some to deliver
        // parsed from wifi PublicKey and its cert
        boolean inlist = false;

        if (found.contains(trusted.peek())) {
            // send PublicKey, {BTServerThead}
        }
        return inlist;

    }
    /*private class BTServerThead extends Thread{
        private BluetoothServerSocket mBluetoothServerSocket = mBluetoothAdapter.
        String mUuid = null;
        Method getUuid = mBluetoothServerSocket.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
        BluetoothServerSocket tmp= null;
        getUuid = BluetoothServerSocket.("getUuids", null);
        ParcelUuid[] uuids = (ParcelUuid[]) getUuid.invoke(mBluetoothAdapter,null);
                for (ParcelUuid uuid : uuids){
                    mUuid = uuid.getUuid().toString();

                }
                tmp =mBluetoothAdapter.listenUsingRfcommWithServiceRecord(mBluetoothAdapter.getName(), UUID.fromString(mUuid));
            mBluetoothServerSocket = tmp;
        }
        public void run(){
            BluetoothSocket socket = null;
            while (true){ //open/close trigger
                try{
                    socket = mBluetoothServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
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
*/

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
