package com.ibm.mobilefirst.mobileedge.translators;


import java.io.Serializable;

import android.bluetooth.BluetoothGattCharacteristic;

import com.ibm.mobilefirst.mobileedge.abstractmodel.BaseSensorData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.HeartRateData;

public class HRPHeartRateTranslator implements ICharacteristicDataTranslator{

	@Override
	public BaseSensorData translate(BluetoothGattCharacteristic c) {

		int flag = c.getProperties();
        int format = -1;
        if ((flag & 0x01) != 0) {
            format = BluetoothGattCharacteristic.FORMAT_UINT16;
        } else {
            format = BluetoothGattCharacteristic.FORMAT_UINT8;
        }
		return new HeartRateData(c.getIntValue(format, 1));
	}

	@Override
	public BaseSensorData translate(BluetoothGattCharacteristic characteristic,
			int[] c) {
		// TODO Auto-generated method stub
		return null;
	}

}
