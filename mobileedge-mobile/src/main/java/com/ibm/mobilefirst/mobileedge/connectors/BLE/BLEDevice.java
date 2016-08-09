package com.ibm.mobilefirst.mobileedge.connectors.BLE;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import com.ibm.mobilefirst.mobileedge.connectors.ConnectionStatus;
import com.ibm.mobilefirst.mobileedge.connectors.DeviceConnector;
import com.ibm.mobilefirst.mobileedge.interfaces.ConnectionStatusListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by valentid on 04/08/2016.
 */
public abstract class BLEDevice extends DeviceConnector {
    /**
     * will be used upon scan so user can decide which device to use
     */
    public class ScannedDevice{
        public BluetoothDevice device;
        public int rssi;
        public byte[] scanRecord;
        ScannedDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
            this.device = device;
            this.rssi = rssi;
            this.scanRecord = scanRecord;
        }
        @Override
        public String toString(){
            return "[" + device.getName() + "|" + device.getAddress() + "|" + rssi + "]";
        }
    }


    static final private String TAG = "BLEDevice";
    /**
     * Known Services UUID's
     */
    static public final UUID DEVICE_INFO_SERVICE = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    static public final UUID HEART_RATE_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
    static public final UUID BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    static public final UUID RUNNING_SPEED_AND_CADENCE_SERVICE = UUID.fromString("00001814-0000-1000-8000-00805f9b34fb");
    static public final UUID GENERIC_ATTRIBUTE_SERVICE = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
    static public final UUID GENERIC_ACCESS_SERVICE = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    static public final UUID SENSORTAG_MOTION_SERVICE = UUID.fromString("f000aa80-0451-4000-b000-000000000000");
    static public final UUID SENSORTAG_TEMPERATURE_SERIVCE = UUID.fromString("f000aa00-0451-4000-b000-000000000000");
    static public final UUID SENSORTAG_HUMIDITY_SERVICE = UUID.fromString("f000aa20-0451-4000-b000-000000000000");
    static public final UUID SENSORTAG_BAROMETER_SERVICE = UUID.fromString("f000aa40-0451-4000-b000-000000000000");
    static public final UUID SENSORTAG_LUXOMETER_SERVICE = UUID.fromString("f000aa70-0451-4000-b000-000000000000");


    /**
     * Known Characteristics UUID's
     */
    static public final UUID HEART_RATE_MEASUREMENT_CHARACTERISTIC = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    static public final UUID SENSORTAG_MOTION_DATA_CHARACTERISTIC  = UUID.fromString("f000aa81-0451-4000-b000-000000000000");
    static public final UUID SENSORTAG_MOTION_PERIOD_CHARACTERISTIC  = UUID.fromString("f000aa83-0451-4000-b000-000000000000");
    static public final UUID SENSORTAG_TEMPERATURE_DATA_CHARACTERISTIC = UUID.fromString("f000aa01-0451-4000-b000-000000000000");
    static public final UUID SENSORTAG_TEMPERATURE_PERIOD_CHARACTERISTIC = UUID.fromString("f000aa03-0451-4000-b000-000000000000");
    static public final UUID SENSORTAG_HUMIDITY_DATA_CHARACTERISTIC = UUID.fromString("f000aa21-0451-4000-b000-000000000000");
    static public final UUID SENSORTAG_HUMIDITY_PERIOD_CHARACTERISTIC = UUID.fromString("f000aa23-0451-4000-b000-000000000000");
    static public final UUID SENSORTAG_BAROMETER_DATA_CHARACTERISTIC = UUID.fromString("f000aa41-0451-4000-b000-000000000000");
    static public final UUID SENSORTAG_BAROMETER_PERIOD_CHARACTERISTIC = UUID.fromString("f000aa44-0451-4000-b000-000000000000");
    static public final UUID SENSORTAG_LUXOMETER_DATA_CHARACTERISTIC = UUID.fromString("f000aa71-0451-4000-b000-000000000000");
    static public final UUID SENSORTAG_LUXOMETER_PERIOD_CHARACTERISTIC = UUID.fromString("f000aa73-0451-4000-b000-000000000000");
    /**
     * Known Descriptors UUID's
     */
    static public final UUID CLIENT_CHARACTERISTIC_CONFIGURATION = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    static public final UUID SENSORTAG_MOTION_CONFIGURATION = UUID.fromString("f000aa82-0451-4000-b000-000000000000");
    static public final UUID SENSORTAG_TEMPERATURE_CONFIGURATION = UUID.fromString("f000aa02-0451-4000-b000-000000000000");
    static public final UUID SENSORTAG_HUMIDITY_CONFIGURATION = UUID.fromString("f000aa22-0451-4000-b000-000000000000");
    static public final UUID SENSORTAG_BAROMETER_CONFIGURATION = UUID.fromString("f000aa42-0451-4000-b000-000000000000");
    static public final UUID SENSORTAG_LUXOMETER_CONFIGURATION = UUID.fromString("f000aa72-0451-4000-b000-000000000000");

    static private final long DEFAULT_SCAN_TIMEOUT = 5000;//2sec
    final private BluetoothManager bluetoothManager;
    final private BluetoothAdapter bluetoothAdapter;
    final private BLEDevice bleDevice = this;
    final private Handler handler = new Handler();
    final private HashMap<String, ScannedDevice> lastScannedDevices = new HashMap<>();
    final protected Context context;
    private BluetoothAdapter.LeScanCallback lastScanCallback = null;
    private BleFindListener lastUserCallback = null;
    protected BluetoothDevice bluetoothDevice = null;
    private BluetoothGatt bluetoothGatt = null;
    protected boolean servicesDiscovered = false;
    private final BLEFilter filter;     //filter that returns true when scanRecord represents an instance of your BLEDevice

    public BLEDevice(String deviceName, Context context, BLEFilter filter) {
        super(deviceName);
        this.context = context;
        this.filter = filter;
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public void findClosestDevice(){
        findClosestDevice(DEFAULT_SCAN_TIMEOUT);
    }

    public void findClosestDevice(long scanTimeout){
        findDevice(scanTimeout, new BleFindListener(){
            @Override
            public BluetoothDevice onDeviceFound(HashMap<String, BLEDevice.ScannedDevice> devices) {
                return null;
            }
            @Override
            public BluetoothDevice onScanTimeout(HashMap<String, BLEDevice.ScannedDevice> devices) {
                Log.i("onScanTimeout", devices.values().toString());
                if(devices.size() == 0){
                    return null;
                }
                //choose here the one with best signal
                Iterator<ScannedDevice> i = devices.values().iterator();
                BLEDevice.ScannedDevice bestRssiDevice = i.next();
                while(i.hasNext()){
                    BLEDevice.ScannedDevice scannedDevice = i.next();
                    if(scannedDevice.rssi < bestRssiDevice.rssi){
                        bestRssiDevice = scannedDevice;
                    }
                }
                return bestRssiDevice.device;
            }
        });
    }

    public void findDevice(long scanTimeout, final BleFindListener userCallback){
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.i(TAG, "BLE not supported on this device");
            return;
        }
        if(lastUserCallback != null){
            Log.e(TAG, "Already searching for device, you cant call findDevice(...) while the instance is scanning");
            return;
        }

        lastUserCallback = userCallback;
        lastScannedDevices.clear();

        final BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                ScannedDevice scannedDevice = new ScannedDevice(device, rssi, scanRecord);
                if(filter.filter(scannedDevice)) {
                    Log.i(TAG, "found " + device.getName() + " - " + device.toString() + " rssi:" + rssi);
                    lastScannedDevices.put(device.getAddress(), scannedDevice);
                    bluetoothDevice = userCallback.onDeviceFound(lastScannedDevices);
                    if(bluetoothDevice != null){
                        stopLastLeScan();
                    }
                }
            }
        };

        scanLeDevice(scanCallback, scanTimeout);
    }

    //scans for device with a timeout, when something is found callback is fired
    private void scanLeDevice(final BluetoothAdapter.LeScanCallback scnaCallback, long scanTimeout){
        //now start the scan with timeout
        lastScanCallback = scnaCallback;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bluetoothDevice = lastUserCallback.onScanTimeout(lastScannedDevices);
                stopLastLeScan();
            }
        }, scanTimeout);
        bluetoothAdapter.startLeScan(scnaCallback);
    }

    private void stopLastLeScan(){
        handler.removeCallbacksAndMessages(null);
        bluetoothAdapter.stopLeScan(lastScanCallback);

        if(bluetoothDevice != null){
            bluetoothGatt = bluetoothDevice.connectGatt(context, true, gattCallback);
        }

        lastScanCallback = null;
        lastUserCallback = null;
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.w(TAG, "onServicesDiscovered(status=" + status + ")");
            servicesDiscovered = true;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {updateConnectionStatus(ConnectionStatus.Connected);
                    }
                });
            } else {
                Log.w(TAG, "Error with discovering services");
            }
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            bleDevice.onCharacteristicChanged(gatt, characteristic);
        }
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.w(TAG, "onDescriptorWrite");
            writeFromQueue();

        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.w(TAG, "onDescriptorWrite");
            writeFromQueue();
        }
    };

    abstract protected void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

    protected boolean isServicesDiscovered(){
        return servicesDiscovered;
    }

    //connect actually that does almost nothing in this case, the connection is done when we find the device
    @Override
    public void connect(final Context context, ConnectionStatusListener connectionListener){
        super.connect(context,connectionListener);
        if(lastUserCallback == null && bluetoothDevice == null){
            findClosestDevice();
        }
    }

    @Override
    public void disconnect() {
        super.disconnect();
        if(bluetoothGatt != null){
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
        updateConnectionStatus(ConnectionStatus.Disconnected);
    }

    protected BluetoothGatt getBluetoothGatt(){
        return bluetoothGatt;
    }

    /**
     * descriptors/characteristics write queue, USE ONLY writeToGatt() TO WRITE,
     * DO NOT USE bluetoothGatt DIRECTLY! NEVER!
     * (you may read from it, coz services were discovered)
     */
    private Queue<BluetoothGattCharacteristic> characteristicWritesQueue = new LinkedBlockingQueue<>();
    private Queue<BluetoothGattDescriptor> descriptorWritesQueue = new LinkedBlockingQueue<>();
    private boolean writing = false;
    protected void writeToGatt(BluetoothGattDescriptor descriptor){
        synchronized (bleDevice) {
            if (!writing && descriptorWritesQueue.isEmpty() && characteristicWritesQueue.isEmpty()) {
                writing = true;
                bluetoothGatt.writeDescriptor(descriptor);
            } else {
                descriptorWritesQueue.add(descriptor);
            }
        }
    }
    protected void writeToGatt(BluetoothGattCharacteristic characteristic){
        synchronized (bleDevice) {
            if (!writing && descriptorWritesQueue.isEmpty() && characteristicWritesQueue.isEmpty()) {
                writing = true;
                bluetoothGatt.writeCharacteristic(characteristic);
            } else {
                characteristicWritesQueue.add(characteristic);
            }
        }
    }

    private void writeFromQueue(){
        synchronized (bleDevice) {
            if (!descriptorWritesQueue.isEmpty()) {
                writing = true;
                bluetoothGatt.writeDescriptor(descriptorWritesQueue.remove());
            } else if (!characteristicWritesQueue.isEmpty()){
                writing = true;
                bluetoothGatt.writeCharacteristic(characteristicWritesQueue.remove());
            } else {
                writing = false;
            }
        }
    }


}
