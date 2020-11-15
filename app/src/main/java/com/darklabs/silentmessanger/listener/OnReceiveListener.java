package com.darklabs.silentmessanger.listener;

import android.bluetooth.BluetoothDevice;

import java.io.UnsupportedEncodingException;

/**
 * BluetoothCommunicator
 * Created by richard on 16/1/11.
 */
public interface OnReceiveListener {
    void onReceiveLine(String line, BluetoothDevice device) throws UnsupportedEncodingException;
}
