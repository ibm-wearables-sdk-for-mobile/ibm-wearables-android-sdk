package com.ibm.mobilefirst.mobileedge.abstractmodel;

import java.util.List;

public enum BarometerCalibrationCoefficients {
	INSTANCE;
	  volatile public List<Integer> barometerCalibrationCoefficients;
	  volatile public double heightCalibration;
}
