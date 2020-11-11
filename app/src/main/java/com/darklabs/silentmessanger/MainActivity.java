package com.darklabs.silentmessanger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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

import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;

import static com.darklabs.silentmessanger.BluetoothTrs.BtFinder;
import static com.darklabs.silentmessanger.BluetoothTrs.found;
import static com.darklabs.silentmessanger.BluetoothTrs.i;
import static com.darklabs.silentmessanger.BluetoothTrs.mBluetoothAdapter;
import static com.darklabs.silentmessanger.BluetoothTrs.trusted;

public class MainActivity extends AppCompatActivity {
    private TextView mTextView;
    private EditText mEditText;
    private Button mSend;
    private Spinner mSpinner;
    private TextView selection;

    private final IntentFilter mIntentFilter = new IntentFilter();
    public static BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mBTFilter;
    public ArrayAdapter<String> arrayAdapter;
    public static String BTdeviceNameTo;

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
        selection = findViewById(R.id.selection);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        mBTFilter = new IntentFilter("android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED");
        registerReceiver(mBTReceiver, mBTFilter);
        BtFinder(mBTFilter);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, trusted){
        @Override
        public int getCount(){
            return(trusted.length - 1); // Truncate the list
        }
    };
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(arrayAdapter);

        OnItemSelectedListener itemSelectedListener = new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {

                // Получаем выбранный объект
                selection.setText(trusted[selectedItemPosition]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selection.setText(trusted[0]);

            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        arrayAdapter.notifyDataSetChanged();
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
                found[i][0] = device.getName();
                found[i][1] = device.getAddress();
                found[i][2] = String.valueOf(device.getUuids());
            }
        }
    };


}