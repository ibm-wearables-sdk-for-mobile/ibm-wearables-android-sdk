package com.ibm.mobilefirst.mobileedge.translators;


import android.bluetooth.BluetoothGattCharacteristic;
import com.ibm.mobilefirst.mobileedge.abstractmodel.BaseSensorData;

public interface ICharacteristicDataTranslator {
	BaseSensorData translate (BluetoothGattCharacteristic c);
	BaseSensorData translate (BluetoothGattCharacteristic characteristic, int[] c);
}
