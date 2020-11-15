package com.darklabs.silentmessanger.listener;

import android.bluetooth.BluetoothDevice;

/**
 * BluetoothCommunicator
 * Created by richard on 16/1/11.
 */
public interface OnLoseConnectionListener {
    void onLoseConnection(BluetoothDevice device);
}
