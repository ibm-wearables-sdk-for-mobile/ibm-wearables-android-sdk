package com.ibm.mobilefirst.mobileedge.translators;

import android.bluetooth.BluetoothGattCharacteristic;

import com.ibm.mobilefirst.mobileedge.abstractmodel.AccelerometerData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.AmbientLightData;
import static java.lang.Math.pow;

public class TILuxometerTranslator implements ICharacteristicDataTranslator {

	@Override
	public AmbientLightData translate(BluetoothGattCharacteristic c) {

		byte[] value = c.getValue();
		int mantissa;
		int exponent;
		Integer sfloat= shortUnsignedAtOffset(value, 0);

		mantissa = sfloat & 0x0FFF;
		exponent = (sfloat >> 12) & 0xFF;

		double output;
		double magnitude = pow(2.0f, exponent);
		output = (mantissa * magnitude)/100;
		AmbientLightData data = new AmbientLightData((int)output);
        return data;
	}

	@Override
	public AmbientLightData translate(BluetoothGattCharacteristic characteristic,
			int[] c) {
		// TODO Auto-generated method stub
		return null;
	}

	private static Integer shortUnsignedAtOffset(byte[] c, int offset) {
		Integer lowerByte = (int) c[offset] & 0xFF;
		Integer upperByte = (int) c[offset+1] & 0xFF;
		return (upperByte << 8) + lowerByte;
	}

}
