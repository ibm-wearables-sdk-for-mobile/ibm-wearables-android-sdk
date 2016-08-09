package com.ibm.mobilefirst.mobileedge.connectors.BLE;

import android.bluetooth.BluetoothDevice;

import java.util.HashMap;

/**
 * Created by valentid on 02/08/2016.
 */
public interface BleFindListener {
    /**
     *
     * triggered each time a ble device is found whule scanning (may be the same with updated rssi)
     * the key is the BT Address
     * @param devices
     * @return the device that you prefer from devices, or null to continue scanning
     */
    BluetoothDevice onDeviceFound(HashMap<String, BLEDevice.ScannedDevice> devices);


    /**
     * triggered on timeout
     * @param devices
     * @return the device you prefer to connect or null if you don't want to connect
     **/
    BluetoothDevice onScanTimeout(HashMap<String, BLEDevice.ScannedDevice> devices);
}
