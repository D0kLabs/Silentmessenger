package com.darklabs.silentmessangerrebuild;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {
    private TextView mTextView;
    private EditText mEditText;
    private Button mSend;
    private Box[] mBox;
    private int index = 0;
    public String[] BlackBox = new String[index];
    private final IntentFilter mIntentFilter = new IntentFilter();
    public WifiP2pManager.Channel mChannel;
    public WifiP2pManager mWifiP2pManager;
    public BroadcastReceiver mBroadcastReceiver;
    public List peers = new ArrayList();
    public WifiP2pManager.PeerListListener peerListListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.message_view);
        mEditText = (EditText) findViewById(R.id.message_edit);
        mSend = (Button) findViewById(R.id.Send);
        BluetoothTrs.BtFinder();

        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(this, getMainLooper(), null);
        peerListListener = new WifiP2pManager.PeerListListener() {

            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                peers.clear();
                peers.addAll(wifiP2pDeviceList.getDeviceList());

            }
        };
        // TODO: Seems that will running in loop in {every 5min} <!rewrite!
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            // TODO: Toast to user "SWITCH FINE_LOCATION!"
            return;
        }
        mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // TODO: IF discover init successful, call peerFilter or add task to init request peers
            }

            @Override
            public void onFailure(int i) {
                //wait 5 minutes
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mWifiP2pManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
                String queryData = "0day_silent?"; //wrong! that may be one of owncerts

                Certificate pubCert = Keygen.globalPublicCert; //wrong! use cert from cerstore of owncerts
                @Override
                public void onSuccess() {
                    mWifiP2pManager.addServiceRequest(mChannel, WifiP2pServiceRequest.newInstance(WifiP2pServiceInfo.SERVICE_TYPE_VENDOR_SPECIFIC, queryData), new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            mWifiP2pManager.setServiceResponseListener(mChannel, new WifiP2pManager.ServiceResponseListener() {
                                // service our? service type (255) and string will talk us pubcert
                                @Override
                                public void onServiceAvailable(int i, byte[] bytes, WifiP2pDevice wifiP2pDevice) {
                                    byte q = ((byte) 255);
                                    char[] chQeury = queryData.toCharArray();
                                    byte[] bQeury = null;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                        bQeury = queryData.getBytes(StandardCharsets.UTF_8);
                                    }
                                    for (byte j = 0; j <= bytes.length; j++) {
                                        if (bytes[j] == q) {
                                            boolean answer = (Keygen.findByte(bytes, bQeury));
                                            if (answer){

                                            }
                                            // Huston we have our service!
                                        } else {/* Tast> Huston: Warning! We have unsigned unknown service! */}

                                    }
                                }
                            });

                        }

                        @Override
                        public void onFailure(int i) {
                            mWifiP2pManager.removeServiceRequest(mChannel, WifiP2pServiceRequest.newInstance(WifiP2pServiceInfo.SERVICE_TYPE_VENDOR_SPECIFIC), new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    //reaper what is wrong
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

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Sender();

            }
        });
        mSend.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    Sender();
                }
                return false;
            }
        });
        for (int i = 0; i < index; i++) {
            mTextView.setText(mBox[i].getTextResId());
        }
        mBroadcastReceiver = new mWiFiDirectBroadcastReceiver(mWifiP2pManager, mChannel, this);
        registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    private boolean Sender() {
        String msg = mEditText.getText().toString();

        if (msg.isEmpty() == false) {
            mBox[index].setTextResId(index);
            try {
                BlackBox[index] = mBox[index].setTextRes(msg);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mBox[index].setFilled(true);
            mBox[index].setSize(msg.length());
            index++;
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mBroadcastReceiver);

    }

    public class mWiFiDirectBroadcastReceiver extends BroadcastReceiver {

        private Context mainActivity;

        public mWiFiDirectBroadcastReceiver(WifiP2pManager mWifiP2pManager, WifiP2pManager.Channel channel, MainActivity mainActivity) {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String resiver = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(resiver)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    //Toast something)
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(resiver)) {
                if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mWifiP2pManager.requestPeers(mChannel, peerListListener);
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(resiver)) {
                //AHTUNG!!! Connection FAILS!
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(resiver)) {
                //TOAST "I zachem ti vikluchil wifi?"
            }
        }

    }

    public void connect() {
        WifiP2pDevice device = (WifiP2pDevice) peers.get(0);
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
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




    public byte[] WifiCleint(String SSIDName){
        byte[] data = null;


        return data;
    }
}