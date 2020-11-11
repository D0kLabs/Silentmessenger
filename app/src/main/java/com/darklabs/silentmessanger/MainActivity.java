package com.darklabs.silentmessanger;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static com.darklabs.silentmessanger.BluetoothTrs.BtFinder;
import static com.darklabs.silentmessanger.BluetoothTrs.mBluetoothAdapter;


public class MainActivity extends AppCompatActivity {
    private TextView mTextView;
    private EditText mEditText;
    private Button mSend;
    private Spinner mSpinner;
    private int index = 0;
    public static final int ACCESS_FINE_LOCATION_CODE = 100; //TEMP VALUES! TO CHANGE!
    private static final int ACCESS_NETWORK_STATE_CODE = 101;
    private static final int ACCESS_WIFI_STATE_CODE = 102;
    private static final int BLUETOOTH_CODE = 103;
    private static final int BLUETOOTH_ADMIN_CODE = 104;
    private static final int CHANGE_WIFI_STATE_CODE = 105;

    private final IntentFilter mIntentFilter = new IntentFilter();
    public static BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mBTFilter;


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
        mBTFilter = new IntentFilter("android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED");
        BtFinder(mBTFilter);
        registerReceiver(mBTReceiver, mBTFilter);
        BtFinder(mBTFilter);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        mSend.setOnClickListener(view -> Sender());
        mSend.setOnKeyListener((view, keyCode, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                Sender();
            }
            return false;
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void Sender() {
        String msg = mEditText.getText().toString();
        if (msg.isEmpty() == false) {
            Keygen.getEncrypted(msg);
        } else {
            Toast.makeText(this, "Nothing to send!", Toast.LENGTH_SHORT).show();
        }
        String mSentTo = mSpinner.getSelectedItem().toString();
        if(mSentTo.isEmpty()){

        } else {
            Toast.makeText(this, "Not declared where to send", Toast.LENGTH_SHORT).show();
        }

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
    }

    private final BroadcastReceiver mBTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED")) {
                System.out.println("Bluetooth find some device");
            }
        }
    };

}