package com.darklabs.silentmessanger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.ParcelUuid;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;


public class BluetoothTrs {
    public static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static BluetoothDevice[] sDevices = new BluetoothDevice[40];
    private Handler handler;
    public static String[] trusted = new String[1024];
    public static String[][] found = new String[1024][3];
    public static Queue<String> current = new LinkedBlockingQueue<>();
    public static int indexBT = 0;

    public static void BtFinder(IntentFilter mBTFilter) {
        //Switch On and find paired
        mBluetoothAdapter.enable();
        mBluetoothAdapter.startDiscovery();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                trusted[indexBT] = device.getName();
                sDevices[indexBT] = device;
                indexBT++;
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

    public static String[] sListFormatter() {
        String[] sList = new String[indexBT];
        for (int s = 0; s < trusted.length; s++) {
            if (trusted[s] != null) {
                sList[s] = String.valueOf(trusted[s]);
            }
        }
        return sList;
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


    public static UUID getUUID() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        UUID[] sUuids = new UUID[120];
        int i = 0;
        Method getUuidsMethod = mBluetoothAdapter.getClass().getDeclaredMethod("getUuids", null);
        ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(mBluetoothAdapter, null);

        if (uuids != null) {
            for (ParcelUuid uuid : uuids) {
                sUuids[i] = uuid.getUuid();
                i++;
            }
        }
        return sUuids[0];
    }

}


