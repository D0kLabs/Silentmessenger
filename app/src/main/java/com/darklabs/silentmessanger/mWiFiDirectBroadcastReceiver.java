package com.darklabs.silentmessanger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.Build;

import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static androidx.core.content.PermissionChecker.checkSelfPermission;
import static com.darklabs.silentmessanger.Keygen.BluetoothKeys;
import static com.darklabs.silentmessanger.Keygen.mEnumeration;
import static com.darklabs.silentmessanger.MainActivity.mChannel;
import static com.darklabs.silentmessanger.MainActivity.mWifiP2pManager;
import static com.darklabs.silentmessanger.MainActivity.peerListListener;
import static com.darklabs.silentmessanger.MainActivity.peers;

public class mWiFiDirectBroadcastReceiver extends BroadcastReceiver {
    public WifiP2pManager manager;
    public WifiP2pManager.Channel channel;
    public WifiP2pManager.ConnectionInfoListener mInfoListener;

    public mWiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, WifiP2pManager.ConnectionInfoListener mInfoListener) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.mInfoListener = mInfoListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                manager.requestConnectionInfo(channel, mInfoListener);
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                checkSelfPermission(context, ACCESS_FINE_LOCATION);
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                checkSelfPermission(context, ACCESS_FINE_LOCATION);
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                checkSelfPermission(context, ACCESS_FINE_LOCATION);
            }
        }

        mWifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                peers.clear();
                mWifiP2pManager.requestPeers(channel, peerListListener);
                spinnerAdapter = new WifiPeerListAdaper()
                final WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                checkSelfPermission(context, ACCESS_FINE_LOCATION);
                mWifiP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // It will work)
                    }

                    @Override
                    public void onFailure(int i) {

                    }
                });
            }

            @Override
            public void onFailure(int i) {
                //wait 5 minutes
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mWifiP2pManager.discoverServices(channel, new WifiP2pManager.ActionListener() {
                String queryData = "0day_silent?";

                @Override
                public void onSuccess() {
                    mWifiP2pManager.addServiceRequest(channel, WifiP2pServiceRequest.newInstance(WifiP2pServiceInfo.SERVICE_TYPE_VENDOR_SPECIFIC, queryData), new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            mWifiP2pManager.setServiceResponseListener(channel, new WifiP2pManager.ServiceResponseListener() {
                                @Override
                                public void onServiceAvailable(int i, byte[] bytes, WifiP2pDevice wifiP2pDevice) { // what is "i" ?
                                    byte[] q = new byte[]{((byte) 255)}; // ?wrong?
                                    if (Keygen.findByte(bytes, q)) {
                                        try {
                                            Certificate localcert = BluetoothKeys.getCertificate(mEnumeration.nextElement());
                                            byte[] bQeury = localcert.getEncoded();
                                            boolean answer = (Keygen.findByte(bytes, bQeury));
                                            if (answer) {
                                                //1 get cert alias
                                                //2 get compare hw info
                                            }
                                        } catch (KeyStoreException e) {
                                            e.printStackTrace();
                                        } catch (CertificateEncodingException e) {
                                            e.printStackTrace();
                                        }

                                    } else {

                                    }

                                }
                            });

                        }

                        @Override
                        public void onFailure(int i) {
                            mWifiP2pManager.removeServiceRequest(channel, WifiP2pServiceRequest.newInstance(WifiP2pServiceInfo.SERVICE_TYPE_VENDOR_SPECIFIC), new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    // ...
                                }

                                @Override
                                public void onFailure(int i) {

                                }
                            });

                        }
                    });
                }

                @Override
                public void onFailure(int i) {


                }
            });
        }
        mWifiP2pManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {

            }
        });
    }


    public byte[] WifiClient(String SSIDName) {
        byte[] data = null;


        return data;
    }
}