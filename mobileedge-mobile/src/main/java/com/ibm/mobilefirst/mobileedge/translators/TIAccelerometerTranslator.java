package com.ibm.mobilefirst.mobileedge.translators;

import android.bluetooth.BluetoothGattCharacteristic;

import com.ibm.mobilefirst.mobileedge.translators.ICharacteristicDataTranslator;
import com.ibm.mobilefirst.mobileedge.abstractmodel.BaseSensorData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.AccelerometerData;
public class TIAccelerometerTranslator implements ICharacteristicDataTranslator {

	@Override
	public AccelerometerData translate(BluetoothGattCharacteristic c) {

		final float SCALE = (float) 4096.0;
		byte[] value = c.getValue();

		float x = ((value[7]<<8) + value[6])/SCALE;
		float y = ((value[9]<<8) + value[8])/SCALE;
		float z = ((value[11]<<8) + value[10])/SCALE;
		AccelerometerData accData = new AccelerometerData(x, y, z);
        return accData;
	}

	@Override
	public AccelerometerData translate(BluetoothGattCharacteristic characteristic,
			int[] c) {
		// TODO Auto-generated method stub
		return null;
	}

}
