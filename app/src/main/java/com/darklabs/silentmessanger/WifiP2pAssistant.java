package com.darklabs.silentmessanger;

/*
 * Created by Bruce Too
 * On 27/04/2018.
 * At 10:21
 * Use example:
 * 1. Init assistant by {@link WifiP2PAssistant#getInstance(Context)}
 *
 * 2. Register broadcaster and check if P2P is enable by {@link WifiP2PAssistant#enable()}
 *    unregister by {@link WifiP2PAssistant#disable()}
 *
 * 3. Create GO(let the caller device be GO) by {@link WifiP2PAssistant#createGroup()}
 *    remove by {@link WifiP2PAssistant#removeGroup()}
 *
 * 4. Discover peer devices by {@link WifiP2PAssistant#discoverPeers()}
 *    cancel operation by {@link WifiP2PAssistant#cancelDiscoverPeers()}
 *
 * 5. Connect one single Peer by {@link WifiP2PAssistant#connect(WifiP2pDevice)}
 *
 * 6. Register key lifecycle event callback by {@link WifiP2PAssistant#setCallback(WifiP2PAssistantCallback)}
 *
 * More apis to see the detail below..
 */

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class WifiP2pAssistant {
    /*
     * Key process event callback listener
     */
    public interface WifiP2PAssistantCallback {
        void onWifiP2PEvent(Event event);
    }

    private static final String TAG = WifiP2PAssistant.class.getSimpleName();
    private static WifiP2PAssistant sWifiP2PAssistant = null;

    private final List<WifiP2pDevice> mCurrentPeers = new ArrayList<WifiP2pDevice>();
    private Context mContext = null;
    private boolean mIsWifiP2pEnabled = false;
    private final IntentFilter mIntentFilter;
    private final WifiP2pManager.Channel mWifiP2pChannel;
    private final WifiP2pManager mWifiP2pManager;
    private WifiP2pBroadcastReceiver mReceiver;
    private final WifiP2PConnectionInfoListener mConnectionListener;
    private final WifiP2PPeerListListener mPeerListListener;
    private final WifiP2PGroupInfoListener mGroupInfoListener;
    private int mFailureReason = WifiP2pManager.ERROR;
    private ConnectStatus mConnectStatus = ConnectStatus.NOT_CONNECTED;
    private Event mLastEvent = null;

    private String mDeviceMacAddress = "";
    private String mDeviceName = "";
    private InetAddress mGroupOwnerAddress = null;
    private String mGroupOwnerMacAddress = "";
    private String mGroupOwnerName = "";
    private String mPassphrase = "";
    private boolean mGroupFormed = false;

    // tracks the number of clients, must be thread safe
    private int clients = 0;

    private WifiP2PAssistantCallback mEventCallback = null;

    /*
     * Key lifecycle event enum
     */
    public enum Event {
        DISCOVERING_PEERS,
        PEERS_AVAILABLE,
        GROUP_CREATED,
        CONNECTING,
        CONNECTED_AS_PEER,
        CONNECTED_AS_GROUP_OWNER,
        DISCONNECTED,
        CONNECTION_INFO_AVAILABLE,
        ERROR
    }

    /*
     * P2P Connect Status
     */
    public enum ConnectStatus {
        NOT_CONNECTED,
        CONNECTING,
        CONNECTED,
        GROUP_OWNER,
        ERROR
    }

    public synchronized static WifiP2PAssistant getInstance(Context context) {
        if (sWifiP2PAssistant == null) sWifiP2PAssistant = new WifiP2PAssistant(context);

        return sWifiP2PAssistant;
    }

    private WifiP2pAssistant(@NonNull Context context) {
        this.mContext = context;

        // Set up the intent filter for wifi P2P
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mWifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        if (mWifiP2pManager == null) {
            throw new RuntimeException("Wifi P2P Manager is null");
        }
        mWifiP2pChannel = mWifiP2pManager.initialize(context, Looper.getMainLooper(), null);
        mReceiver = new WifiP2pBroadcastReceiver();
        mConnectionListener = new WifiP2PConnectionInfoListener();
        mPeerListListener = new WifiP2PPeerListListener();
        mGroupInfoListener = new WifiP2PGroupInfoListener();
    }

    /*
     * Maintains the list of wifi p2p peers available
     */
    private class WifiP2PPeerListListener implements WifiP2pManager.PeerListListener {

        /*
         * mEventCallback method, called by Android when the peer list changes
         */
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            mCurrentPeers.clear();
            mCurrentPeers.addAll(peerList.getDeviceList());

            Log.v(TAG, "Wifi P2P peers found: " + mCurrentPeers.size());
            for (WifiP2pDevice peer : mCurrentPeers) {
                // deviceAddress is the MAC address, deviceName is the human readable name
                String s = "    peer: " + peer.deviceAddress + " " + peer.deviceName;
                Log.v(TAG, s);
            }

            fireEvent(Event.PEERS_AVAILABLE);
        }

    }

    /*
     * Updates when this device connects
     */
    private abstract class WifiP2PConnectionInfoListener extends Context implements WifiP2pManager.ConnectionInfoListener {

        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo info) {

            if (mWifiP2pManager == null) return;
            //when the connection state changes, request group info to find GO
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mWifiP2pManager.requestGroupInfo(mWifiP2pChannel, mGroupInfoListener);
            mGroupOwnerAddress = info.groupOwnerAddress;
            Log.v(TAG, "Group owners address: " + mGroupOwnerAddress.toString());

            if (info.groupFormed && info.isGroupOwner) {
                Log.v(TAG, "Wifi P2P group formed, this device is the group owner (GO)");
                mConnectStatus = ConnectStatus.GROUP_OWNER;
                fireEvent(Event.CONNECTED_AS_GROUP_OWNER);
            } else if (info.groupFormed) {
                Log.v(TAG, "Wifi P2P group formed, this device is a client");
                mConnectStatus = ConnectStatus.CONNECTED;
                fireEvent(Event.CONNECTED_AS_PEER);
            } else {
                Log.v(TAG, "Wifi P2P group NOT formed, ERROR: " + info.toString());
                mFailureReason = WifiP2pManager.ERROR; // there is no error code for this
                mConnectStatus = ConnectStatus.ERROR;
                fireEvent(Event.ERROR);
            }
        }

    }

    private class WifiP2PGroupInfoListener implements WifiP2pManager.GroupInfoListener {

        @Override
        public void onGroupInfoAvailable(WifiP2pGroup group) {
            if (group == null) return;

            if (group.isGroupOwner()) {
                mGroupOwnerMacAddress = mDeviceMacAddress;
                mGroupOwnerName = mDeviceName;
            } else {
                WifiP2pDevice go = group.getOwner();
                mGroupOwnerMacAddress = go.deviceAddress;
                mGroupOwnerName = go.deviceName;
            }

            mPassphrase = group.getPassphrase();

            // make sure passphrase isn't null
            mPassphrase = (mPassphrase != null) ? mPassphrase : "";

            Log.v(TAG, "Wifi P2P connection information available");
            fireEvent(Event.CONNECTION_INFO_AVAILABLE);
        }

    }

    private class WifiP2pBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                mIsWifiP2pEnabled = (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED);
                Log.v(TAG, "Wifi P2P state - enabled: " + mIsWifiP2pEnabled);
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                Log.v(TAG, "Wifi P2P peers changed");
                if (mWifiP2pManager == null) return;
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mWifiP2pManager.requestPeers(mWifiP2pChannel, mPeerListListener);
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                WifiP2pInfo wifip2pinfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                Log.v(TAG, "Wifi P2P connection changed - connected: " + networkInfo.isConnected());
                if (mWifiP2pManager == null) return;
                if (networkInfo.isConnected()) {
                    mWifiP2pManager.requestConnectionInfo(mWifiP2pChannel, mConnectionListener);
                    mWifiP2pManager.stopPeerDiscovery(mWifiP2pChannel, null);
                } else {
                    mConnectStatus = ConnectStatus.NOT_CONNECTED;
                    if (!mGroupFormed) {
                        discoverPeers();
                    }
                    // if we were previously connected, notify that we are now disconnected
                    if (isConnected()) {
                        fireEvent(Event.DISCONNECTED);
                    }
                    mGroupFormed = wifip2pinfo.groupFormed;
                }
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                Log.v(TAG, "Wifi P2P this device changed");
                WifiP2pDevice wifiP2pDevice = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                mDeviceName = wifiP2pDevice.deviceName;
                mDeviceMacAddress = wifiP2pDevice.deviceAddress;
                Log.v(TAG, "Wifi P2P device information: " + mDeviceName + " " + mDeviceMacAddress);
            }
        }

    }

    public synchronized void enable() {
        clients += 1;
        Log.v(TAG, "There are " + clients + " Wifi P2P Assistant Clients (+)");

        if (clients == 1) {
            Log.v(TAG, "Enabling Wifi P2P Assistant");
            if (mReceiver == null) mReceiver = new WifiP2pBroadcastReceiver();
            mContext.registerReceiver(mReceiver, mIntentFilter);
        }
    }

    public synchronized void disable() {
        clients -= 1;
        Log.v(TAG, "There are " + clients + " Wifi P2P Assistant Clients (-)");

        if (clients == 0) {
            Log.v(TAG, "Disabling Wifi P2P Assistant");
            mWifiP2pManager.stopPeerDiscovery(mWifiP2pChannel, null);
            mWifiP2pManager.cancelConnect(mWifiP2pChannel, null);

            try {
                mContext.unregisterReceiver(mReceiver);
            } catch (IllegalArgumentException e) {
                // disable() was called, but enable() was never called; ignore
            }
            mLastEvent = null;
        }
    }

    public synchronized boolean isEnabled() {
        return (clients > 0);
    }

    public ConnectStatus getConnectStatus() {
        return mConnectStatus;
    }

    public List<WifiP2pDevice> getPeersList() {
        return new ArrayList<WifiP2pDevice>(mCurrentPeers);
    }

    public WifiP2PAssistantCallback getCallback() {
        return mEventCallback;
    }

    public void setCallback(WifiP2PAssistantCallback callback) {
        this.mEventCallback = callback;
    }

    /*
     * Get the device mac address
     *
     * @return mac address
     */
    public String getDeviceMacAddress() {
        return mDeviceMacAddress;
    }

    /*
     * Get the device name,if want to set device name,you need
     * reflect call {@link WifiP2pManager#setDeviceName(WifiP2pManager.Channel c, String devName, ActionListener listener)}
     *
     * @return device name
     */
    public String getDeviceName() {
        return mDeviceName;
    }


    /*
     * Get the IP address of the group owner
     *
     * @return ip address
     */
    public InetAddress getGroupOwnerAddress() {
        return mGroupOwnerAddress;
    }

    /*
     * Get the group owners mac address
     *
     * @return mac address
     */
    public String getGroupOwnerMacAddress() {
        return mGroupOwnerMacAddress;
    }

    /*
     * Get the group owners device name
     *
     * @return device name
     */
    public String getGroupOwnerName() {
        return mGroupOwnerName;
    }

    /*
     * Return the passphrase for this network; only valid if this device is the group owner
     *
     * @return the passphrase to this device
     */
    public String getPassphrase() {
        return mPassphrase;
    }

    public boolean isWifiP2pEnabled() {
        return mIsWifiP2pEnabled;
    }

    /*
     * Returns true if connected, or group owner
     *
     * @return true if connected, otherwise false
     */
    public boolean isConnected() {
        return (mConnectStatus == ConnectStatus.CONNECTED
                || mConnectStatus == ConnectStatus.GROUP_OWNER);
    }

    /*
     * Returns true if this device is the group owner
     *
     * @return true if group owner, otherwise false
     */
    public boolean isGroupOwner() {
        return (mConnectStatus == ConnectStatus.GROUP_OWNER);
    }

    /*
     * Discover Wifi P2P peers
     */
    public void discoverPeers() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mWifiP2pManager.discoverPeers(mWifiP2pChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                fireEvent(Event.DISCOVERING_PEERS);
                Log.v(TAG, "Wifi P2P discovering peers");
            }

            @Override
            public void onFailure(int reason) {
                String reasonStr = failureReasonToString(reason);
                mFailureReason = reason;
                Log.v(TAG, "Wifi P2P failure while trying to discover peers - reason: " + reasonStr);
                fireEvent(Event.ERROR);
            }
        });
    }

    /*
     * Cancel discover Wifi P2P peers request
     */
    public void cancelDiscoverPeers() {
        Log.v(TAG, "Wifi P2P stop discovering peers");
        mWifiP2pManager.stopPeerDiscovery(mWifiP2pChannel, null);
    }

    /*
     * Create a Wifi P2P group
     * <p>
     * Will receive a Event.GROUP_CREATED if the group is created. If there is an
     * error creating group Event.ERROR will be sent. If group already exists, no
     * event will be sent. However, a Event.CONNECTED_AS_GROUP_OWNER should be
     * received.
     */
    public void createGroup() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mWifiP2pManager.createGroup(mWifiP2pChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                mConnectStatus = ConnectStatus.GROUP_OWNER;
                fireEvent(Event.GROUP_CREATED);
                Log.v(TAG, "Wifi P2P created group");
            }

            @Override
            public void onFailure(int reason) {
                if (reason == WifiP2pManager.BUSY) {
                    // most likely group is already created
                    Log.v(TAG, "Wifi P2P cannot create group, does group already exist?");
                } else {
                    String reasonStr = failureReasonToString(reason);
                    mFailureReason = reason;
                    Log.v(TAG, "Wifi P2P failure while trying to create group - reason: " + reasonStr);
                    mConnectStatus = ConnectStatus.ERROR;
                    fireEvent(Event.ERROR);
                }
            }
        });
    }

    /*
     * Remove a Wifi P2P group
     */
    public void removeGroup() {
        mWifiP2pManager.removeGroup(mWifiP2pChannel, null);
    }

    public void connect(WifiP2pDevice peer) {
        if (mConnectStatus == ConnectStatus.CONNECTING || mConnectStatus == ConnectStatus.CONNECTED) {
            Log.v(TAG, "WifiP2P connection request to " + peer.deviceAddress + " ignored, already connected");
            return;
        }

        Log.v(TAG, "WifiP2P connecting to " + peer.deviceAddress);
        mConnectStatus = ConnectStatus.CONNECTING;

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = peer.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 1;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mWifiP2pManager.connect(mWifiP2pChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.v(TAG, "WifiP2P connect started");
                fireEvent(Event.CONNECTING);
            }

            @Override
            public void onFailure(int reason) {
                String reasonStr = failureReasonToString(reason);
                mFailureReason = reason;
                Log.v(TAG, "WifiP2P connect cannot start - reason: " + reasonStr);
                fireEvent(Event.ERROR);
            }
        });
    }


    public String getFailureReason() {
        return failureReasonToString(mFailureReason);
    }

    public static String failureReasonToString(int reason) {
        switch (reason) {
            case WifiP2pManager.P2P_UNSUPPORTED:
                return "P2P_UNSUPPORTED";
            case WifiP2pManager.ERROR:
                return "ERROR";
            case WifiP2pManager.BUSY:
                return "BUSY";
            default:
                return "UNKNOWN (reason " + reason + ")";
        }
    }

    private void fireEvent(Event event) {
        // don't send duplicate events
        if (mLastEvent == event && mLastEvent != Event.PEERS_AVAILABLE) return;
        mLastEvent = event;

        if (mEventCallback != null) mEventCallback.onWifiP2PEvent(event);
    }

}
