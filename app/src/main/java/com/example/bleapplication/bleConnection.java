package com.example.bleapplication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;


//https://stackoverflow.com/questions/14812326/android-bluetooth-get-uuids-of-discovered-devices

public class bleConnection extends AppCompatActivity {

    BluetoothGatt gatt;
    BluetoothDevice device;

    UUID HEART_RATE_SERVICE_UUID = convertFromInteger(0x180D);
    UUID HEART_RATE_MEASUREMENT_CHAR_UUID = convertFromInteger(0x2A37);
    UUID HEART_RATE_CONTROL_POINT_CHAR_UUID = convertFromInteger(0x2A39);
    UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902);

    TextView hrmDisplay;
    TextView UUID0;
    TextView UUID1;
    TextView UUID2;
    TextView UUID3;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ble_connection);
        Intent intent = getIntent();
        device = intent.getExtras().getParcelable("BLUETOOTH_DEVICE");
        String deviceName = device.getName();
        String deviceAddress = device.getAddress();

        hrmDisplay = (TextView) findViewById(R.id.hrmReading);
        UUID0 = (TextView) findViewById(R.id.uuid0);
        UUID1 = (TextView) findViewById(R.id.uuid1);
        UUID2 = (TextView) findViewById(R.id.uuid2);
        UUID3 = (TextView) findViewById(R.id.uuid3);




        ParcelUuid[] deviceUUID = device.getUuids();
        if (deviceUUID != null){
            for (int i=0; i<deviceUUID.length; i++){
                Log.e("UUID", deviceUUID[i].toString());
            }
        }
        else{
            Log.e("UUID", "No UUID found");
        }

        Log.e("Tommy", deviceAddress);

    }

    @Override
    protected void onResume() {

        super.onResume();


        final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.e("BluetoothGattCallback",  "onConnectionStateChange");
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == STATE_CONNECTED){
                    gatt.discoverServices();
                    Log.e("BluetoothGattCallback",  "Connected to device");


                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status){
                Log.e("BluetoothGattCallback",  "onServicesDiscovered");

                BluetoothGattCharacteristic characteristic = gatt.getService(HEART_RATE_SERVICE_UUID).getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID);
                gatt.setCharacteristicNotification(characteristic, true);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);

            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status){
                //super.onDescriptorWrite(gatt, descriptor, status);
                Log.e("BluetoothGattCallback",  "onDescriptorWrite");

                BluetoothGattCharacteristic characteristic = gatt.getService(HEART_RATE_SERVICE_UUID).getCharacteristic(HEART_RATE_CONTROL_POINT_CHAR_UUID);
                characteristic.setValue(new byte[]{1, 1, 1, 1});
                gatt.writeCharacteristic(characteristic);

            }


            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

                byte[] value = characteristic.getValue();

                Log.e("BluetoothGattCallback",  "onCharacteristicChanged");

                try{

                    Log.e("Output Byte Value Unsigned [1]", Integer.toString(Byte.toUnsignedInt(value[1])));
                    hrmDisplay.setText(Integer.toString(Byte.toUnsignedInt(value[1])));

                }
                catch (Exception e){
                    Log.e("Output Value",  "Not working");
                }
            }

            /*
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                if (HEART_RATE_MEASUREMENT_CHAR_UUID.equals(characteristic.getUuid())) {
                    Log.d("onCharacteristicChanged", "HEART_RATE_MEASUREMENT_UUID");
                    //PROCESS DATA
                }
                if (HEART_RATE_CONTROL_POINT_CHAR_UUID.equals(characteristic.getUuid())) {
                    Log.d("onCharacteristicChanged", "HEART_RATE_CONTROL_POINT_UUID");
                    //PROCESS DATA
                }
            }
            */

        };


        gatt = device.connectGatt(this, false, gattCallback);


    }

    public UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }




}
