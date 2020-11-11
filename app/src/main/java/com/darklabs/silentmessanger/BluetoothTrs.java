package com.darklabs.silentmessanger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.IntentFilter;
import android.os.ParcelUuid;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class BluetoothTrs {
    public static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static String[] trusted = new String[1024];
    public static String[][] found = new String[1024][3];
    public static Queue<String> current = new LinkedBlockingQueue<>();
    public static int i = 0;
    public static String oneUUID;

    /*public static byte[] getMyCurrentConfig{
        byte[] data=null;
        //parce configuration of Wifi (Name, Address, UUID)


        return data;
    }
*/
    public static void BtFinder(IntentFilter mBTFilter) {
        //Switch On and find paired
        mBluetoothAdapter.enable(); // reconfig emulator! bt do nothing
        mBluetoothAdapter.startDiscovery();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                String deviceUuids = String.valueOf(device.getUuids()[0].getUuid());
                trusted[i] = deviceName;

                found[i][0] = deviceName;
                found[i][1] = deviceHardwareAddress;
                found[i][2] = deviceUuids;
                i++;
            }

        } else {
            trusted[0] = ("Find some else");
        }
        // Send public cert
        // get target public cert
        // sign myConfig by target public
        // get signed target "myConfig" and check it at current
        // if it`s true add target config to trusted
        mBluetoothAdapter.cancelDiscovery();

    }

    public static boolean BtCompare() {
        // current = some to deliver
        // parsed from wifi PublicKey and its cert
        boolean inlist = false;

       /* if (found.contains(trusted.peek())) {
            // send PublicKey, {BTServerThead}
        }

        */
        return inlist;

    }

    private class BTServerThead extends Thread {
        private BluetoothServerSocket mBluetoothServerSocket;

        public void BTAcceptThead() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, IOException {
            BluetoothServerSocket tmp = null;
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(mBluetoothAdapter.getName(), UUID.fromString(String.valueOf(oneUUID)));
            mBluetoothServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            while (true) { //open/close trigger
                try {
                    socket = mBluetoothServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            if (socket != null) {
                try {
                    mBluetoothServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void cancel() {
            try {
                mBluetoothServerSocket.close();
            } catch (IOException e) {
            }
        }


    }

    private class BTClientThead extends Thread {
        private BluetoothSocket mSocket;

        public void ClientThead(BluetoothDevice device) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(oneUUID));
                mSocket = tmp;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run() {
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

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public String getUUID() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        String[] sUuids = new String[120];
        int i = 0;
        Method getUuidsMethod = mBluetoothAdapter.getClass().getDeclaredMethod("getUuids", null);
        ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(mBluetoothAdapter, null);

        if (uuids != null) {
            for (ParcelUuid uuid : uuids) {
                sUuids[i] = uuid.getUuid().toString();
                i++;
            }
        }
        oneUUID = String.valueOf(sUuids[0]);
        return oneUUID;
    }


  /*  private class ReciverThead extends Thread{
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

   */
}

