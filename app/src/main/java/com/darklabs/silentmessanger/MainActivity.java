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
import android.util.Log;
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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.util.UUID;

import static android.content.ContentValues.TAG;
import static com.darklabs.silentmessanger.BluetoothTrs.BtFinder;
import static com.darklabs.silentmessanger.BluetoothTrs.mBluetoothAdapter;
import static com.darklabs.silentmessanger.BluetoothTrs.sListFormatter;

public
class MainActivity extends AppCompatActivity {
    private TextView mTextView;
    private EditText mEditText;
    private Button mSend;
    private Spinner mSpinner;

    private static final int REQUEST_ENABLE_BT = 1;
    public static BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mBTFilter;
    public static String BTdeviceNameTo;
    public UUID oneUUID;
    ConnectedThread mConnectedThread;

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.message_view);
        mSpinner = findViewById(R.id.spinnerTo);
        mEditText = findViewById(R.id.message_edit);
        mSend = findViewById(R.id.Send);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        mBTFilter = new IntentFilter("android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED");
        registerReceiver(mBTReceiver, mBTFilter);
        BtFinder(mBTFilter);
        String [] sList= sListFormatter();
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
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        try {
            oneUUID= BluetoothTrs.getUUID();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        mSend.setOnClickListener(view -> {
            try {
                Sender();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
        mSend.setOnKeyListener((view, keyCode, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                try {
                    Sender();
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            return false;
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void Sender() throws CertificateException, UnsupportedEncodingException {
        String msg = mEditText.getText().toString();
        if (msg.isEmpty() == false) {
            String mSendTo = mSpinner.getSelectedItem().toString();
            if(mSendTo.isEmpty() == false){
                Box.setNewMessage(msg,mSendTo);
                SendMessage(0, BluetoothTrs.sDevices[0]);

            } else {
                Toast.makeText(this, "Not declared where to send", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Nothing to send!", Toast.LENGTH_SHORT).show();
        }
        mEditText.setText("");

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

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            }
        }
    };

    public class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;
        private final UUID deviceUUID;
        private BluetoothSocket mmSocket;
        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        + oneUUID );
                tmp = mmDevice.createRfcommSocketToServiceRecord(oneUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            mmSocket = tmp;

            // Make a connection to the BluetoothSocket

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();

            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + oneUUID );
            }

            //will talk about this in the 3rd video
            connected(mmSocket);
        }
        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }

    private void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    private static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private static OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    final String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                    break;
                }
            }
        }


        public static void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }
        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }


    public void Start_Server(View view) {

        AcceptThread accept = new AcceptThread();
        accept.start();

    }

    private class AcceptThread extends Thread {

        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null ;

            // Create a new listening server socket
            try{
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("appname", oneUUID);

                Log.d(TAG, "AcceptThread: Setting up Server using: " + oneUUID);
            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            mmServerSocket = tmp;
        }

        public void run(){
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try{
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG, "run: RFCOM server socket start.....");

                socket = mmServerSocket.accept();

                Log.d(TAG, "run: RFCOM server socket accepted connection.");

            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            //talk about this is in the 3rd
            if(socket != null){
                connected(socket);
            }

            Log.i(TAG, "END mAcceptThread ");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage() );
            }
        }

    }

    public void SendMessage(int index, BluetoothDevice device) {
        ConnectThread connect = new ConnectThread(device, oneUUID);
        connect.start();
        byte[] bytes = Box.getMessageToSend(index);
        mConnectedThread.write(bytes);
    }


}