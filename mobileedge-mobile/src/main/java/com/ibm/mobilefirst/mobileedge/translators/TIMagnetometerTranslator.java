package com.ibm.mobilefirst.mobileedge.translators;


import java.io.Serializable;

import android.bluetooth.BluetoothGattCharacteristic;

import com.ibm.mobilefirst.mobileedge.abstractmodel.MagnetometerData;

public class TIMagnetometerTranslator implements ICharacteristicDataTranslator {
	
	@Override
	public MagnetometerData translate(BluetoothGattCharacteristic c) {

		MagnetometerData data;
		byte[] value = c.getValue();

		final float SCALE = (float) (32768 / 4912);
		if (value.length >= 18) {
			float x = ((value[13] << 8) + value[12]) / SCALE;
			float y = ((value[15] << 8) + value[14]) / SCALE;
			float z = ((value[17] << 8) + value[16]) / SCALE;

			data = new MagnetometerData(x, y, z);
		}
		else{
			data = new MagnetometerData(0,0,0);
		}
	    return data;
	}
	
	 private Integer shortSignedAtOffset(BluetoothGattCharacteristic c, int offset) {
	    Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
	    if (lowerByte == null)
	        return 0;
	    Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, offset + 1); // Note: interpret MSB as signed.
	    if (upperByte == null)
	        return 0;
	    return (upperByte << 8) + lowerByte;
	}

	@Override
	public MagnetometerData translate(BluetoothGattCharacteristic characteristic,
			int[] c) {
		// TODO Auto-generated method stub
		return null;
	}


}
