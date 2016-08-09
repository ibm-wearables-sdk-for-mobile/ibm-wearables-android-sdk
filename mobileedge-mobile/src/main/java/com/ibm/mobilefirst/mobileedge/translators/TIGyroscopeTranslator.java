package com.ibm.mobilefirst.mobileedge.translators;

import android.bluetooth.BluetoothGattCharacteristic;

import com.ibm.mobilefirst.mobileedge.abstractmodel.GyroscopeData;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_SINT8;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;

public class TIGyroscopeTranslator implements ICharacteristicDataTranslator {

	@Override
	public GyroscopeData translate(BluetoothGattCharacteristic c) {

		byte[] value = c.getValue();
		final float SCALE = (float) 128.0;

		float x = ((value[1]<<8) + value[0])/SCALE;
		float y = ((value[3]<<8) + value[2])/SCALE;
		float z = ((value[5]<<8) + value[4])/SCALE;

		GyroscopeData gyroData = new GyroscopeData(x,y,z);
        return gyroData;
	}

	@Override
	public GyroscopeData translate(BluetoothGattCharacteristic characteristic,
			int[] c) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	/**
	 * Gyroscope, Magnetometer, Barometer, IR temperature
	 * all store 16 bit two's complement values in the awkward format
	 * LSB MSB, which cannot be directly parsed as getIntValue(FORMAT_SINT16, offset)
	 * because the bytes are stored in the "wrong" direction.
	 *
	 * This function extracts these 16 bit two's complement values.
	 * */
	private static Integer shortSignedAtOffset(BluetoothGattCharacteristic c, int offset) {
	    Integer lowerByte = c.getIntValue(FORMAT_UINT8, offset);
	    Integer upperByte = c.getIntValue(FORMAT_SINT8, offset + 1); // Note: interpret MSB as signed.

	    return (upperByte << 8) + lowerByte;
	}

	private static Integer shortUnsignedAtOffset(BluetoothGattCharacteristic c, int offset) {
	    Integer lowerByte = c.getIntValue(FORMAT_UINT8, offset);
	    Integer upperByte = c.getIntValue(FORMAT_UINT8, offset + 1); // Note: interpret MSB as unsigned.

	    return (upperByte << 8) + lowerByte;
	}

}
