package me.bluetoothuuidsample;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_LOCATION_REQUEST_CODE = 250;
    BluetoothAdapter bluetoothAdapter;
    BroadcastReceiver receiver;
    IntentFilter intentFilter=new IntentFilter();
    ArrayList<BluetoothDevice> devices=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        
        
        receiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {

                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        actionDiscoveryStarted(intent);
                        break;

                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        actionStateChanged(intent);


                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        actionDiscoveryFinished(intent);
                        break;

                    case BluetoothDevice.ACTION_FOUND:
                        actionDeviceFound(intent);
                        break;

                    case BluetoothDevice.ACTION_UUID:
                        actionUuidFound(intent);
                        break;

                }
            }


        };

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Scanning for Bluetooth devices", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();




                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                } else {
                    BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                    bluetoothAdapter = bluetoothManager.getAdapter();
                }






                if (!bluetoothAdapter.isEnabled())
                {

                    bluetoothAdapter.enable();
                }
                else
                {
                    // set device as discoverable
                    Log.v("BLUETOOTH", "SCAN MODE " + bluetoothAdapter.getScanMode());
                    if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                        startActivity(discoverableIntent);
                    }


                    ////////Request location permission for Android 6 Bluetooth scans

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_LOCATION_REQUEST_CODE);

                }



            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);



        if (requestCode == PERMISSION_LOCATION_REQUEST_CODE) {
            if (permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED)

                bluetoothAdapter.startDiscovery();
        }



    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    private void actionAclDisconnected(Intent intent) {

    }

    public void actionStateChanged(Intent intent) {
        Log.v("BLUETOOTH", "BL STATE_CHANGED");

        switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {

            case BluetoothAdapter.STATE_ON:
                // start BT shizzle
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_LOCATION_REQUEST_CODE);

                // set device as discoverable
                Log.v("BLUETOOTH", "SCAN MODE " + bluetoothAdapter.getScanMode());
                if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                    startActivity(discoverableIntent);
                }
                break;
        }
    }

    private void actionUuidFound(Intent intent) {

        Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);


        if (uuidExtra!=null) {


            for (Parcelable uuid : uuidExtra) {

                Log.i("BLUETOOTH", "A UUID WAS FOUND "+uuid.toString());

            }

        }

    }

    private void actionDeviceFound(Intent intent) {
        BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (foundDevice != null) {


            Log.i("BLUETOOTH", "A DEVICE WAS FOUND "+foundDevice.getName());
            devices.add(foundDevice);
        }

        }

    private void actionDiscoveryFinished(Intent intent) {


        Log.i("BLUETOOTH", "attempting to fetch uuids");

        for (BluetoothDevice device:devices)
        {
            device.fetchUuidsWithSdp();
        }
    }

    private void actionDiscoveryStarted(Intent intent) {
        devices.clear();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
