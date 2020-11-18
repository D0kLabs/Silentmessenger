package com.darklabs.silentmessanger;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.UUID;

import static com.darklabs.silentmessanger.BluetoothTrs.BtFinder;
import static com.darklabs.silentmessanger.BluetoothTrs.getUUID;
import static com.darklabs.silentmessanger.BluetoothTrs.mBluetoothAdapter;
import static com.darklabs.silentmessanger.BluetoothTrs.sListFormatter;
import static com.darklabs.silentmessanger.ChatController.STATE_CONNECTED;
import static com.darklabs.silentmessanger.ChatController.STATE_CONNECTING;
import static com.darklabs.silentmessanger.ChatController.STATE_LISTEN;
import static com.darklabs.silentmessanger.ChatController.STATE_NONE;

public class MainActivity extends AppCompatActivity {
    private Button mServerButton;
    private static TextView mTextView;
    private EditText mEditText;
    private Button mSend;
    private Spinner mSpinner;
    public UUID MY_UUID;
    private static Context mContext;
    private static final String TAG = "DROID ";
    private ArrayAdapter<String> chatAdapter;
    private ArrayList<String> chatMessages;
    private ListView listView;


    public static final int REQUEST_ENABLE_BT = 1;
    public static BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mBTFilter;
    public static String BTdeviceNameTo;
    public BluetoothDevice mmDevice;
    public BluetoothSocket mmSocket;
    public BluetoothServerSocket mmServerSocket = null;
    public BluetoothSocket mmBluetoothSocket = null;
    public static byte[] mBytes;
    private static Handler mHandler;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_OBJECT = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_OBJECT = "device_name";
    public int state;


    public void checkPermission(String permission, int requestCode) { // <<< #!
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
        }
    }
    public static Context getContext() {
        return mContext;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.message_view);
        mSpinner = findViewById(R.id.spinnerTo);
        mEditText = findViewById(R.id.message_edit);
        mSend = findViewById(R.id.Send);
        mServerButton = findViewById(R.id.serverbutton);
        try {
            MY_UUID = getUUID();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        mHandler = new Handler(){
            @SuppressLint("HandlerLeak")
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch (msg.what){
                    case MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case STATE_CONNECTED:
                                Toast.makeText(getContext(),"Connected!",Toast.LENGTH_SHORT).show();
                                break;
                            case STATE_CONNECTING:
                                Toast.makeText(getContext(),"Connecting...",Toast.LENGTH_SHORT).show();
                                break;
                            case STATE_LISTEN:
                                Toast.makeText(getContext(),"Listening...",Toast.LENGTH_SHORT).show();
                                break;
                            case STATE_NONE:
                                Toast.makeText(getContext(),"Nothing in connection",Toast.LENGTH_SHORT).show();
                                break;
                        }
                        break;
                    case MESSAGE_READ:
                        byte[] readBuffer = (byte[]) msg.obj;
                        String sMsg = new String(readBuffer,0, msg.arg1);
                        mTextView.setText("Reading from buffer " + msg.arg1 + " bytes with type of connection " + msg.arg2 + " . There are: " + sMsg); //wrong context!
                }
            }
        };


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mBTFilter = new IntentFilter("android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED");
        registerReceiver(mBTReceiver, mBTFilter);
        registerReceiver(receiver,filter);
        BtFinder(mBTFilter);
        String[] sList = sListFormatter();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sList);
        mSpinner.setAdapter(arrayAdapter);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BTdeviceNameTo = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                parent.setSelection(0);

            }
        });
        chatMessages = new ArrayList<>();
        chatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatMessages);
        listView.setAdapter(chatAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        setContext(this);
        mSend.setOnClickListener(view -> {
            try {
                Sender();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        mSend.setOnKeyListener((view, keyCode, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                try {
                    Sender();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return false;
        });
        mServerButton.setOnClickListener(view -> {
            Start_Server();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void Sender() throws IOException {
        String msg = mEditText.getText().toString();
        if (msg.isEmpty() == false) {
            String mSendTo = mSpinner.getSelectedItem().toString();
            if (mSendTo.isEmpty() == false) {
                Box.setNewMessage(msg, mSendTo);
                SendMessage(0, BluetoothTrs.sDevices[0]); //search what selected

            } else {
                Toast.makeText(this, "Not declared where to send", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Nothing to send!", Toast.LENGTH_SHORT).show();
        }
        mEditText.setText("");

    }

    public void Start_Server() {
        mBluetoothAdapter.cancelDiscovery();
        makeDeviceDiscoverable();
        AcceptThread serverTheat = new AcceptThread();
        serverTheat.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBTReceiver);
        unregisterReceiver(receiver);
    }

    private final BroadcastReceiver mBTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED")) {
            }
        }
    };
    public final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            }
        }
    };

    public void setContext(MainActivity context) {
        mContext = context;
    }

    public class ConnectThread extends Thread {
        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = mBluetoothAdapter.getRemoteDevice(device.getAddress());
            ParcelUuid[] devUUIDS = device.getUuids();
            UUID devUUID=devUUIDS[0].getUuid();
            try {
                tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(devUUID);
                Log.d(TAG, "Creating socket to " + device.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
            Log.d(TAG, "ConnectThread: started.");
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                mmSocket.connect();
                Log.d(TAG, "run: Started Socket.");
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                    Log.e("", "trying fallback...");
                }
                return;
            }
            if (mmSocket.isConnected()) {
                manageConnectedSocket(mmSocket);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }


    private class AcceptThread extends Thread {
        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("Silentmessenger", MY_UUID);
                mmServerSocket = tmp;
            } catch (IOException e) {
            }
        }

        public void run() {
            BluetoothSocket socket = null;
            while (state != STATE_CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (ChatController.this) {
                        switch (state) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate
                                // new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                }
                                break;
                        }
                    }
                }
            }
            while (true) {
                try {
                        socket = mmServerSocket.accept();
                        if (socket != null) {
                            manageConnectedSocket(socket);
                        }

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

        }
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage());
            }
        }

    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        mBluetoothService.ConnectedThread connectedThread = new mBluetoothService.ConnectedThread(socket);
        connectedThread.start();
    }



    /*   public void Start_Server(){
           // Initialize mServer
           mServer = new BluetoothServer(mHandler, new OnReceiveListener() {
               @RequiresApi(api = Build.VERSION_CODES.O)
               @Override
               public void onReceiveLine(String line, BluetoothDevice device) throws UnsupportedEncodingException {
                   // Do something to handle with the line received from a remote device
                   Box.setNewMessage(line, device.getName());
                   String nMessage = Keygen.deRetyping(line);
                   mEditText.setText(nMessage);
               }
           }, new OnLoseConnectionListener() {
               @Override
               public void onLoseConnection(BluetoothDevice device) {
                   // Do something after losing the connection with specific device
               }
           });


   // Start listening for Bluetooth connection requests
           if (!mServer.isBluetoothSupported()) {
               return;
           } else if (!mServer.isBluetoothEnabled()) {
               mServer.startActivityForEnablingBluetooth(this, 1);
           } else {
               // NAME: Name of your service. It's OK to be app name
               // MY_UUID: A unique id used on both client side and server side. See "http://developer.android.com/intl/zh-cn/guide/topics/connectivity/bluetooth.html#ConnectingAsAServer"
               mServer.startListening("Silent Messenger", MY_UUID, new ListenStateListener() {
                   @Override
                   public void onAccept(BluetoothDevice device) {
                       // Do something after establishing connection with a new device
                   }

                   @Override
                   public void onFail() {
                       // Do something after listening task failed
                   }
               });
           }
       }
       public void StartClient(BluetoothDevice device, String sData){
           // Initialize mClient
           mClient = new BluetoothClient(mHandler, new OnReceiveListener() {
               @Override
               public void onReceiveLine(String line, BluetoothDevice device) {
                   // Do something to handle with the line received from a remote device
                   line = sData;
               }
           }, new OnLoseConnectionListener() {
               @Override
               public void onLoseConnection(BluetoothDevice device) {
                   // Do something after losing the connection with a specific devcice
               }
           });

   // Get paired devices
           Set<BluetoothDevice> pairedDevices = mClient.getPairedDevices();
   // Or start discovering new devices
           mClient.startDiscovery(this, new OnNewDeviceFoundListener() {
               @Override
               public void onNewDeviceFound(BluetoothDevice device) {
                   // Do something to handle with newly found device
               }
           });
   // Cancel discovery (MUST be called if startDiscovery was called, because it unregisters a broadcast receiver inside)
           mClient.cancelDiscovery(this);

   // Choose a device (a server) and connect to it
           mDevice = device;
   // MY_UUID: A unique id used on both client side and server side. See "http://developer.android.com/intl/zh-cn/guide/topics/connectivity/bluetooth.html#ConnectingAsAServer"
                   mClient.connectToDevice(mDevice, MY_UUID, new ConnectListener() {
                       @Override
                       public void onSucceed(BluetoothDevice device) {
                           // Do something after establishing connection with a specific device
                       }

                       @Override
                       public void onFail(BluetoothDevice device) {
                           // Do something when failed to connect
                       }
                   });
       }

     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void SendMessage(int index, BluetoothDevice device) {
        // String data = Box.getStringToSend(index);
        // StartClient(device, data);
        mBytes = Box.getMessageToSend(index);
        ConnectThread connect = new ConnectThread(device);
        connect.start();
        mBluetoothService.ConnectedThread connected = new mBluetoothService.ConnectedThread(mmSocket);
        connected.start();
        connected.cancel();
        connect.cancel();

        // BluetoothConnector bluetoothConnector = new BluetoothConnector(device, true, mBluetoothAdapter, uuidGenCandidates);
        //bluetoothConnector.connect();
    }

    public static class mBluetoothService {
        private static final String TAG = "MY_APP_DEBUG_TAG";
        public static class ConnectedThread extends Thread {
            private final BluetoothSocket mmSocket;
            boolean running;
            private byte[] mmBuffer; // mmBuffer store for the stream

            public ConnectedThread(BluetoothSocket socket) {
                mmSocket = socket;
                running = true;
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            public void run() {
                int max = mmSocket.getMaxTransmitPacketSize() / 2;
                mmBuffer = new byte[max];
                int numBytes = 0; // bytes returned from read()

                while (running) {
                    try {
                        numBytes = mmSocket.getInputStream().read(mmBuffer, 0, mmBuffer.length);
                        if(mmBuffer != null){
                            if (numBytes>8){
                                Message msg = mHandler.obtainMessage(MESSAGE_READ,numBytes,1,mmBuffer);
                                msg.sendToTarget();
                            }
                        }
                        if (mBytes != null) {
                            mmSocket.getOutputStream().write(mBytes, 0, mmSocket.getMaxReceivePacketSize() / 2);
                            //mmOutStream.write(bytes,0, mmSocket.getMaxReceivePacketSize());
                            mmSocket.getOutputStream().flush();
                        }
                    } catch (IOException e) {
                        break;
                    }
                }
            }

            // Call this method from the main activity to shut down the connection.
            public void cancel() {
                running = false;
                try {
                    mmSocket.close();

                } catch (IOException e) {
                }
            }
        }
    }
    public void makeDeviceDiscoverable(){
        Intent makeDeviceDiscoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        makeDeviceDiscoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(makeDeviceDiscoverableIntent);
    }  // OganBelema code. Thanks for it!
}
