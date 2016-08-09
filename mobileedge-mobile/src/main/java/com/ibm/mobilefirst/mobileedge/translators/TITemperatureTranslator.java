package com.ibm.mobilefirst.mobileedge.translators;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_SINT8;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;
import static java.lang.Math.pow;


import java.io.Serializable;

import android.bluetooth.BluetoothGattCharacteristic;

import com.ibm.mobilefirst.mobileedge.abstractmodel.TemperatureData;

public class TITemperatureTranslator implements ICharacteristicDataTranslator {

	@Override
	public TemperatureData translate(BluetoothGattCharacteristic c) {
    			

	    /* The IR Temperature sensor produces two measurements;
	     * Object ( AKA target or IR) Temperature,
	     * and Ambient ( AKA die ) temperature.
	     *
	     * Both need some conversion, and Object temperature is dependent on Ambient temperature.
	     *
	     * They are stored as [ObjLSB, ObjMSB, AmbLSB, AmbMSB] (4 bytes)
	     * Which means we need to shift the bytes around to get the correct values.
	     */

	    double ambient = extractAmbientTemperature(c);
	   // double target = extractTargetTemperature(c, ambient);

	    return new TemperatureData(ambient);
	}

	private double extractAmbientTemperature(BluetoothGattCharacteristic c) {
	    int offset = 2;
	    return shortUnsignedAtOffset(c, offset) / 128.0;
	}

	private double extractTargetTemperature(BluetoothGattCharacteristic c, double ambient) {
	    Integer twoByteValue = shortSignedAtOffset(c, 0);

	    double Vobj2 = twoByteValue.doubleValue();
	    Vobj2 *= 0.00000015625;

	    double Tdie = ambient + 273.15;

	    double S0 = 5.593E-14;	// Calibration factor
	    double a1 = 1.75E-3;
	    double a2 = -1.678E-5;
	    double b0 = -2.94E-5;
	    double b1 = -5.7E-7;
	    double b2 = 4.63E-9;
	    double c2 = 13.4;
	    double Tref = 298.15;
	    double S = S0*(1+a1*(Tdie - Tref)+a2*pow((Tdie - Tref),2));
	    double Vos = b0 + b1*(Tdie - Tref) + b2*pow((Tdie - Tref),2);
	    double fObj = (Vobj2 - Vos) + c2*pow((Vobj2 - Vos),2);
	    double tObj = pow(pow(Tdie,4) + (fObj/S),.25);

	    return tObj - 273.15;
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
	public TemperatureData translate(BluetoothGattCharacteristic characteristic,
			int[] c) {
		// TODO Auto-generated method stub
		return null;
	}
}



