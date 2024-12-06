package com.atharvabedekar.smartwheelchair;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter BTAdapter;
    private LinearLayout BTLinearLayout;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> requestBTEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BTLinearLayout = findViewById(R.id.BTList);

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                findBluetooth();
            }
        });

        requestBTEnable = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                findBluetooth();
            }
        });

        TextView addDevice = findViewById(R.id.addDevice);
        addDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bluetoothPicker = new Intent("android.bluetooth.devicepicker.action.LAUNCH");
                startActivity(bluetoothPicker);
            }
        });

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            findBluetooth();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
        }
    }

    public void findBluetooth() {

        BTAdapter = BluetoothAdapter.getDefaultAdapter();

        if (BTAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth not found", Toast.LENGTH_SHORT).show();
        } else if (!BTAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            requestBTEnable.launch(enableBT);
        } else {
            new SearchDevices().execute();
        }
    }

    private class SearchDevices extends AsyncTask<Void, Void, List<BluetoothDevice>> {

        @Override
        protected List<BluetoothDevice> doInBackground(Void... params) {
                @SuppressLint("MissingPermission") Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();
                List<BluetoothDevice> listDevices = new ArrayList<BluetoothDevice>();
                for (BluetoothDevice device : pairedDevices) {
                    listDevices.add(device);
                }
                return listDevices;
//            return new ArrayList<BluetoothDevice>();
        }

        @SuppressLint("MissingPermission")
        @Override
        protected void onPostExecute(List<BluetoothDevice> listDevices) {
            super.onPostExecute(listDevices);
            LayoutInflater layoutInflater = getLayoutInflater();
            if (!listDevices.isEmpty()) {
                BTLinearLayout.removeAllViews();
                for (int i = 0; i < listDevices.size(); i++) {
                    View item = layoutInflater.inflate(R.layout.bt_list_item, BTLinearLayout, false);
                    int finalI = i;
                    ((TextView) item.findViewById(R.id.name)).setText(listDevices.get(finalI).getName());
                    ((TextView) item.findViewById(R.id.address)).setText(listDevices.get(finalI).getAddress());
                    item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            connectBluetooth(listDevices.get(finalI));
                        }
                    });
                    BTLinearLayout.addView(item);

                }
            }
        }

    }

    public void connectBluetooth(BluetoothDevice device) {
        Intent intent = new Intent(getApplicationContext(), Controller.class);
        intent.putExtra("targetDevice", device);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}