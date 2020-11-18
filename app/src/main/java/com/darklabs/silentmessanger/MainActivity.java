package com.darklabs.silentmessanger;

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
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import static com.darklabs.silentmessanger.BluetoothTrs.BtFinder;
import static com.darklabs.silentmessanger.BluetoothTrs.getUUID;
import static com.darklabs.silentmessanger.BluetoothTrs.indexBT;
import static com.darklabs.silentmessanger.BluetoothTrs.mBluetoothAdapter;
import static com.darklabs.silentmessanger.BluetoothTrs.sDevices;
import static com.darklabs.silentmessanger.BluetoothTrs.sListFormatter;
import static com.darklabs.silentmessanger.BluetoothTrs.trusted;
import static com.darklabs.silentmessanger.ChatController.STATE_CONNECTED;
import static com.darklabs.silentmessanger.ChatController.STATE_CONNECTING;
import static com.darklabs.silentmessanger.ChatController.STATE_LISTEN;
import static com.darklabs.silentmessanger.ChatController.STATE_NONE;
import static com.darklabs.silentmessanger.ChatController.handler;

public class MainActivity extends AppCompatActivity {
    private Button mServerButton;
    private static TextView mTextView;
    private EditText mEditText;
    private Button mSend;
    private Spinner mSpinner;
    private static Context mContext;
    private static final String TAG = "DROID ";


    public static final int REQUEST_ENABLE_BT = 1;
    public static BroadcastReceiver mBroadcastReceiver;
    private ArrayAdapter<String> discoveredDevicesAdapter;
    private IntentFilter mBTFilter;
    public static UUID MY_UUID;
    private BluetoothDevice connectingDevice;
    public BluetoothDevice mmDevice;
    public BluetoothSocket mmSocket;
    public BluetoothServerSocket mmServerSocket = null;
    public BluetoothSocket mmBluetoothSocket = null;
    public static byte[] mBytes;
    private ChatController chatController;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_OBJECT = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_OBJECT = "device_name";
    private BluetoothDevice mSendTo;
    private IntentFilter filter;


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
            MY_UUID=getUUID();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        handler = new Handler(new Handler.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                switch (msg.what) {

                    case MESSAGE_WRITE:
                        byte[] writeBuf = (byte[]) msg.obj;
                        Toast.makeText(getContext(), "Writng message", Toast.LENGTH_SHORT).show();
                        break;
                    case MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        String readMessage = new String(readBuf, 0, msg.arg1);
                        try {
                            Box.setNewMessage(readMessage, connectingDevice);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(getContext(), "NEW MESSAGE!", Toast.LENGTH_LONG).show();
                        break;
                    case MESSAGE_DEVICE_OBJECT:
                        connectingDevice = msg.getData().getParcelable(DEVICE_OBJECT);
                        Toast.makeText(getApplicationContext(), "Connected to " + connectingDevice.getName(),
                                Toast.LENGTH_SHORT).show();
                        break;
                    case MESSAGE_TOAST:
                        Toast.makeText(getApplicationContext(), msg.getData().getString("toast"),
                                Toast.LENGTH_SHORT).show();
                        break;
                    case MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case STATE_CONNECTED:
                                Toast.makeText(getContext(), "Connected!", Toast.LENGTH_SHORT).show();
                                break;
                            case STATE_CONNECTING:
                                Toast.makeText(getContext(), "Connecting...", Toast.LENGTH_SHORT).show();
                                break;
                            case STATE_LISTEN:
                                Toast.makeText(getContext(), "Listening...", Toast.LENGTH_SHORT).show();
                                break;
                            case STATE_NONE:
                                Toast.makeText(getContext(), "Nothing in connection", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        break;
                }
                return false;
            }
        });
                      /*  break;
                    case MESSAGE_READ:
                        byte[] readBuffer = (byte[]) msg.obj;
                        String sMsg = new String(readBuffer,0, msg.arg1);
                        mTextView.setText("Reading from buffer " + msg.arg1 + " bytes with type of connection " + msg.arg2 + " . There are: " + sMsg); //wrong context!
                }
            }
        };

                       */

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mBTFilter = new IntentFilter("android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED");
        registerReceiver(mBTReceiver, mBTFilter);
        registerReceiver(receiver, filter);
        BtFinder(mBTFilter);
        String[] sList = sListFormatter();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sList);
        mSpinner.setAdapter(arrayAdapter);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            private String BTdeviceNameTo;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BTdeviceNameTo = (String) parent.getItemAtPosition(position);
                for (int i=0; i<trusted.length; i++){
                    if (trusted[i] == BTdeviceNameTo){
                        connectingDevice = sDevices[i];
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getContext(), "Select device", Toast.LENGTH_SHORT).show();

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        BtFinder(mBTFilter);
        makeDeviceDiscoverable();
        mBluetoothAdapter.startDiscovery();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            chatController = new ChatController(this,handler);
        }
        chatController.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        setContext(this);
        if (chatController != null) {
            if (chatController.getState() == ChatController.STATE_NONE) {
                chatController.start();
            }
        }
        mSend.setOnClickListener(view -> {
            try {
                Sender();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
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
                } catch (InterruptedException e) {
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
    private void Sender() throws IOException, InterruptedException {
        String msg = mEditText.getText().toString();
        if (msg.isEmpty() == false) {
            Box.setNewMessage(msg,connectingDevice);
                SendMessage(0, connectingDevice); //search what selected
        } else {
            Toast.makeText(this, "Nothing to send!", Toast.LENGTH_SHORT).show();
        }
        mEditText.setText("");

    }

    public void Start_Server() {
        mBluetoothAdapter.cancelDiscovery();
        makeDeviceDiscoverable();
        chatController.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBluetoothAdapter.disable();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatController != null)
            chatController.stop();
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
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    sDevices[indexBT] = device;
                    indexBT++;
                }
            }
        }
    };

    public void setContext(MainActivity context) {
        mContext = context;
    }

   /* public class ConnectThread extends Thread {
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
                    synchronized (chatController) {
                        switch (state) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // start the connected thread.
                                chatController.connected(socket, socket.getRemoteDevice());
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



       public void Start_Server(){
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
        mBytes = Box.getMessageToSend(index);
        BluetoothDevice TargetDevice = mBluetoothAdapter.getRemoteDevice(device.getAddress());
            chatController.connect(TargetDevice);
            if (chatController.getState() != STATE_CONNECTED) {
                Toast.makeText(this, "Device busy", Toast.LENGTH_LONG).show();
                mBluetoothAdapter.cancelDiscovery();
            }
        if(chatController.getState() == STATE_CONNECTED){
                chatController.write(mBytes);
                chatController.stop();
        }

        /*ConnectThread connect = new ConnectThread(device);
        connect.start();
        mBluetoothService.ConnectedThread connected = new mBluetoothService.ConnectedThread(mmSocket);
        connected.start();
        connected.cancel();
        connect.cancel();

        // BluetoothConnector bluetoothConnector = new BluetoothConnector(device, true, mBluetoothAdapter, uuidGenCandidates);
        //bluetoothConnector.connect();
        */
    }
/*
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
    */
    public void makeDeviceDiscoverable(){
        Intent makeDeviceDiscoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        makeDeviceDiscoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(makeDeviceDiscoverableIntent);
    }  // OganBelema code. Thanks for it!

}
