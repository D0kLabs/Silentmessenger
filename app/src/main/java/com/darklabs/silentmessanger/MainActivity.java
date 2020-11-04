package com.darklabs.silentmessanger;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.CHANGE_WIFI_STATE;
import static com.darklabs.silentmessanger.Keygen.BluetoothKeys;
import static com.darklabs.silentmessanger.Keygen.mEnumeration;

public class MainActivity extends AppCompatActivity {
    private static Context sContext;
    private TextView mTextView;
    private EditText mEditText;
    private Button mSend;
    private int index = 0;
    private static final int ACCESS_FINE_LOCATION_CODE = 100;
    private static final int ACCESS_NETWORK_STATE_CODE = 101;
    private static final int ACCESS_WIFI_STATE_CODE = 102;
    private static final int BLUETOOTH_CODE = 103;
    private static final int BLUETOOTH_ADMIN_CODE = 104;
    private static final int CHANGE_WIFI_STATE_CODE = 105;

    private final IntentFilter mIntentFilter = new IntentFilter();
    public WifiP2pManager.Channel mChannel;
    public WifiP2pManager mWifiP2pManager;
    public BroadcastReceiver mBroadcastReceiver;
    public WifiP2pManager.ConnectionInfoListener mInfoListener;
    public List peers = new ArrayList();
    public WifiP2pManager.PeerListListener peerListListener;
    String[] permissions = {ACCESS_FINE_LOCATION, ACCESS_NETWORK_STATE, BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_WIFI_STATE, CHANGE_WIFI_STATE};

    // Function to check and request permission
    public void checkerPermission(String permission, int requestCode) {
        // Checking if permission is not granted
        for (int i = 0; i < permissions.length; i++) {
            permission = permissions[i];
            if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        }
    }
    public void checkPermission (String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission},requestCode);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.message_view);
        mEditText = findViewById(R.id.message_edit);
        mSend = findViewById(R.id.Send);


        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(this, getMainLooper(), null);
        mBroadcastReceiver = new mWiFiDirectBroadcastReceiver(mWifiP2pManager,mChannel, mInfoListener);
        peerListListener = new WifiP2pManager.PeerListListener() { //have one. Need some else?

            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                peers.clear();
                wifiP2pDeviceList.getDeviceList().addAll(peers);

            }
        };
        
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
        registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    private boolean Sender() {
        String msg = mEditText.getText().toString();

        if (msg.isEmpty() == false) {
            Box.setTextRes(msg);
            mEditText.setText("");
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mBroadcastReceiver);

    }

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
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
                if (manager != null){
                    final NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()){
                    manager.requestConnectionInfo(channel,mInfoListener);
                } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                checkPermission(ACCESS_FINE_LOCATION,ACCESS_FINE_LOCATION_CODE);
                } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                    //AHTUNG!!! Connection FAILS!
                } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                    //TOAST "I zachem ti vikluchil wifi?"
                }
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    public void connect() {
        WifiP2pDevice device = (WifiP2pDevice) peers.get(0);
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (checkPermission(ACCESS_FINE_LOCATION, ACCESS_FINE_LOCATION_CODE)) {
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
    }


    public byte[] WifiClient(String SSIDName) {
        byte[] data = null;


        return data;
    }
// ON FREE TIME: DO IT BY case/switch
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "FINE_LOCATION Permission Granted", Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if ((checkPermission(ACCESS_FINE_LOCATION, ACCESS_FINE_LOCATION_CODE)) != PackageManager.PERMISSION_GRANTED){
                        mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                mWifiP2pManager.requestPeers(channel, peerListListener);
                            }

                            @Override
                            public void onFailure(int i) {
                                //wait 5 minutes
                            }
                        });
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mWifiP2pManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
                            String queryData = "0day_silent?";

                            @Override
                            public void onSuccess() {
                                mWifiP2pManager.addServiceRequest(mChannel, WifiP2pServiceRequest.newInstance(WifiP2pServiceInfo.SERVICE_TYPE_VENDOR_SPECIFIC, queryData), new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        mWifiP2pManager.setServiceResponseListener(mChannel, new WifiP2pManager.ServiceResponseListener() {
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

                                                } else {/* Tast> Huston: Warning! We have unsigned unknown service! */}

                                            }
                                        });

                                    }

                                    @Override
                                    public void onFailure(int i) {
                                        mWifiP2pManager.removeServiceRequest(mChannel, WifiP2pServiceRequest.newInstance(WifiP2pServiceInfo.SERVICE_TYPE_VENDOR_SPECIFIC), new WifiP2pManager.ActionListener() {
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
            }
            
            } else {
                Toast.makeText(MainActivity.this, "FINE_LOCATION Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == ACCESS_NETWORK_STATE_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "NETWORK_STATE Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "NETWORK_STATE Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == BLUETOOTH_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "BLUETOOTH Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "BLUETOOTH Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == BLUETOOTH_ADMIN_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "BLUETOOTH_ADMIN Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "BLUETOOTH_ADMIN Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == ACCESS_WIFI_STATE_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "ACCESS_WIFI_STATE Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "ACCESS_WIFI_STATE Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == CHANGE_WIFI_STATE_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "CHANGE_WIFI_STATE Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "CHANGE_WIFI_STATE Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
