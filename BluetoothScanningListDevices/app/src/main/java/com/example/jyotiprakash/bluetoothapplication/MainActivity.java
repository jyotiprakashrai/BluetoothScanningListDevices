package com.example.jyotiprakash.bluetoothapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();

    BluetoothAdapter mBluetoothAdapter;
    Button btnONOF, btnDiscoverable, btnFindDevicesToPair;
    ListView deviceListView;
    public ListDeviceAdapter mListDeviceAdapter;
    public ArrayList<BluetoothDevice> mBTDeviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btnONOF.setOnClickListener(this);
        btnDiscoverable.setOnClickListener(this);
        btnFindDevicesToPair.setOnClickListener(this);
    }

    public void enableDisableBT(){
        if (mBluetoothAdapter == null){
            Toast.makeText(this, "Your device does not have bluetooth capabilities", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"Inside enableDisableBT: device does not have bluetooth capabilities");
        }
        if (!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "Enabling Bluetooth");
            startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));

            IntentFilter BTIntentFilter = new IntentFilter(mBluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1,BTIntentFilter);

        }
        if (mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.disable();
            Log.d(TAG, "Disabling Bluetooth");
            IntentFilter BTIntentFilter = new IntentFilter(mBluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1,BTIntentFilter);
        }
    }

    /*
     * Create a BroadcastReceiver for listening Bluetooth state change.
     */
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //When discovery find a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state= intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }

            }
        }
    };

    /*
     * Broadcast Receiver for Discoverability mode on/off or expire.
     */
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    /*Device is now in Discoverable Mode*/
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability of device Enabled.");
                        break;
                    /*Device is now not in discoverable mode*/
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability of device Disabled. Device is able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability of device Disabled.  Device is not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };

    /**
     * Broadcast Receiver for discovering unpaired devices
     */
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                mBTDeviceList.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mListDeviceAdapter = new ListDeviceAdapter(context, R.layout.list_device_adapter, mBTDeviceList);
                deviceListView.setAdapter(mListDeviceAdapter);
            }
        }
    };


    public void initView(){
        btnONOF= (Button) findViewById(R.id.btnONOFF);
        btnDiscoverable = (Button) findViewById(R.id.btnDiscoverable);
        btnFindDevicesToPair = (Button) findViewById(R.id.btnFindDevicesToPair);
        deviceListView = (ListView) findViewById(R.id.deviceListView);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnONOFF:
                Log.d(TAG, "Bluetooth STATE Toggling");
                enableDisableBT();
                break;

            case R.id.btnDiscoverable:
                Log.d(TAG, "Making device discoverable");
                makeDeviceDiscoverable();
                break;
            case R.id.btnFindDevicesToPair:
                Log.d(TAG, "Finding unpaired devices to show in ListView");
                findDevicesToPair();

                break;
        }

    }

    public void makeDeviceDiscoverable(){
        Log.d(TAG, "makeDeviceDiscoverable(): Making device discoverable for 400 seconds.");

        Intent deviceDiscoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        deviceDiscoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 400);
        startActivity(deviceDiscoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentFilter);
    }

    public void findDevicesToPair(){
        Log.d(TAG, "Discovering unpaired devices....");
        if (mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "Canceling discovery.");

            //check permissions in manifest
            checkPermissions();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
            mBluetoothAdapter.startDiscovery();

        }
        if(!mBluetoothAdapter.isDiscovering()){

            checkPermissions();
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: unRegistering Broadcast Receiver");
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
    }

    private void checkPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        }else{
            Log.d(TAG, "Need to check permissions. SDK version < LOLLIPOP.");
        }
    }
}
