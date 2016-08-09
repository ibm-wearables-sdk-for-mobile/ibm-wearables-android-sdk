package com.ibm.mobilefirst.mobileedge.translators;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_SINT8;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;

import java.io.Serializable;

import android.bluetooth.BluetoothGattCharacteristic;

import com.ibm.mobilefirst.mobileedge.abstractmodel.HumidityData;

public class TIHumidityTranslator implements ICharacteristicDataTranslator {

	@Override
	public HumidityData translate(BluetoothGattCharacteristic c) {
    	 double data;
		 int a = shortUnsignedAtOffset(c, 2);
		    // bits [1..0] are status bits and need to be cleared according
		    // to the userguide, but the iOS code doesn't bother. It should
		    // have minimal impact.
		   // a = a - (a % 4);
		    

		    data = 100f * (a / 65535f);//((-6f) + 125f * (a / 65535f));


	    return new HumidityData(data);
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




	@Override
	public HumidityData translate(BluetoothGattCharacteristic characteristic,
								  int[] c) {
		// TODO Auto-generated method stub
		return null;
	}
}



