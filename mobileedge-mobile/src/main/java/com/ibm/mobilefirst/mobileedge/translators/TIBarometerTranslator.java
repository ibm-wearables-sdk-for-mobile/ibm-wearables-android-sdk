package com.ibm.mobilefirst.mobileedge.translators;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_SINT8;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;
import static java.lang.Math.pow;
;
import java.util.List;

import android.bluetooth.BluetoothGattCharacteristic;

import com.ibm.mobilefirst.mobileedge.abstractmodel.BarometerCalibrationCoefficients;
import com.ibm.mobilefirst.mobileedge.abstractmodel.BarometerData;

public class TIBarometerTranslator implements ICharacteristicDataTranslator {

	@Override
	public BarometerData translate(BluetoothGattCharacteristic characteristic, final int[] c) {
	    // c holds the calibration coefficients

//	    final Integer t_r;	// Temperature raw value from sensor
//	    final Integer p_r;	// Pressure raw value from sensor
//	    final Double t_a; 	// Temperature actual value in unit centi degrees celsius
//	    final Double S;	// Interim value in calculation
//	    final Double O;	// Interim value in calculation
//	    final Double p_a; 	// Pressure actual value in unit Pascal.
//
//	    t_r = shortSignedAtOffset(characteristic, 0);
//	    p_r = shortUnsignedAtOffset(characteristic, 2);
//
//	    t_a = (100 * (c[0] * t_r / pow(2,8) + c[1] * pow(2,6))) / pow(2,16);
//	    S = c[2] + c[3] * t_r / pow(2,17) + ((c[4] * t_r / pow(2,15)) * t_r) / pow(2,19);
//	    O = c[5] * pow(2,14) + c[6] * t_r / pow(2,3) + ((c[7] * t_r / pow(2,15)) * t_r) / pow(2,4);
//	    p_a = (S * p_r + O) / pow(2,14);

//	    model.setBarometer(p_a);

	    return new BarometerData(0,0);
	}




	/**
	 * Gyroscope, Magnetometer, Barometer, IR temperature
	 * all store 16 bit two's complement values in the awkward format
	 * LSB MSB, which cannot be directly parsed as getIntValue(FORMAT_SINT16, offset)
	 * because the bytes are stored in the "wrong" direction.
	 *
	 * This function extracts these 16 bit two's complement values.
	 * */
	private static Integer shortSignedAtOffset(byte[] c, int offset) {
		Integer lowerByte = (int) c[offset] & 0xFF;
		Integer upperByte = (int) c[offset+1]; // // Interpret MSB as signed
		return (upperByte << 8) + lowerByte;
	}

	private static Integer shortUnsignedAtOffset(byte[] c, int offset) {
		Integer lowerByte = (int) c[offset] & 0xFF;
		Integer upperByte = (int) c[offset+1] & 0xFF;
		return (upperByte << 8) + lowerByte;
	}




	@Override
	public BarometerData translate(BluetoothGattCharacteristic c) {
		// TODO Auto-generated method stub

//		List<Integer> barometerCalibrationCoefficients = BarometerCalibrationCoefficients.INSTANCE.barometerCalibrationCoefficients;
//	      if (barometerCalibrationCoefficients == null) {
//	        System.out.println("Data notification arrived for barometer before it was calibrated.");
//	        return new BarometerData(0,0);
//	      }
//
//	      final int[] c; // Calibration coefficients
//	      final Integer t_r; // Temperature raw value from sensor
//	      final Integer p_r; // Pressure raw value from sensor
//	      final Double S; // Interim value in calculation
//	      final Double O; // Interim value in calculation
//	      final Double p_a; // Pressure actual value in unit Pascal.
//
//	      c = new int[barometerCalibrationCoefficients.size()];
//	      for (int i = 0; i < barometerCalibrationCoefficients.size(); i++) {
//	        c[i] = barometerCalibrationCoefficients.get(i);
//	      }
//
//	      t_r = shortSignedAtOffset(value, 0);
//	      p_r = shortUnsignedAtOffset(value, 2);
//
//	      S = c[2] + c[3] * t_r / pow(2, 17) + ((c[4] * t_r / pow(2, 15)) * t_r) / pow(2, 19);
//	      O = c[5] * pow(2, 14) + c[6] * t_r / pow(2, 3) + ((c[7] * t_r / pow(2, 15)) * t_r) / pow(2, 4);
//	      p_a = (S * p_r + O) / pow(2, 14);
//
//	      return new BarometerData(p_a,0);
//	}
		byte[] value = c.getValue();
		if (value.length > 4) {
			Integer val = twentyFourBitUnsignedAtOffset(value, 2);
			return new BarometerData(0,(double) val / 100.0);
		} else {
			int mantissa;
			int exponent;
			Integer sfloat = shortUnsignedAtOffset(value, 2);

			mantissa = sfloat & 0x0FFF;
			exponent = (sfloat >> 12) & 0xFF;

			double output;
			double magnitude = pow(2.0f, exponent);
			output = (mantissa * magnitude)/10;
			return new BarometerData(0,output / 100.0f);
		}
	}


	private static Integer twentyFourBitUnsignedAtOffset(byte[] c, int offset) {
		Integer lowerByte = (int) c[offset] & 0xFF;
		Integer mediumByte = (int) c[offset+1] & 0xFF;
		Integer upperByte = (int) c[offset + 2] & 0xFF;
		return (upperByte << 16) + (mediumByte << 8) + lowerByte;
	}
}



