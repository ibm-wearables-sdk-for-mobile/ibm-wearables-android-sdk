package com.ibm.mobilefirst.mobileedge.connectors.BLE;

/**
 * Created by valentid on 04/08/2016.
 */
public interface BLEFilter{
    boolean filter(BLEDevice.ScannedDevice scanRecord);
}
