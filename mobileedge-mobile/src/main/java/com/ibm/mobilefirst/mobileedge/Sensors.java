/*
 *    © Copyright 2016 IBM Corp.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.ibm.mobilefirst.mobileedge;

import com.ibm.mobilefirst.mobileedge.abstractmodel.AccelerometerData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.AmbientLightData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.BarometerData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.CaloriesData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.GsrData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.GyroscopeData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.HeartRateData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.HumidityData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.MagnetometerData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.PedometerData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.SkinTemperatureData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.TemperatureData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.UVData;
import com.ibm.mobilefirst.mobileedge.events.SystemEvents;

/**
 * Hold all the supported system sensors
 */
public class Sensors {

    public SensorHolder<AccelerometerData> accelerometer;
    public SensorHolder<HeartRateData> heartRate;
    public SensorHolder<TemperatureData> temperature;
    public SensorHolder<HumidityData> humidity;
    public SensorHolder<MagnetometerData> magnetometer;
    public SensorHolder<GyroscopeData> gyroscope;
    public SensorHolder<BarometerData> barometer;
//    public SensorHolder<OpticalData> optical;
    public SensorHolder<AmbientLightData> ambientLight;
    public SensorHolder<CaloriesData> calories;
    public SensorHolder<PedometerData> pedometer;
    public SensorHolder<GsrData> gsr;
    public SensorHolder<SkinTemperatureData> skinTemperature;
    public SensorHolder<UVData> uv;

    public Sensors(SystemEvents systemEvents) {
        accelerometer = new SensorHolder<>(systemEvents.getSensorEvents(SensorType.Accelerometer));
        gyroscope = new SensorHolder<>(systemEvents.getSensorEvents(SensorType.Gyroscope));
        heartRate = new SensorHolder<>(systemEvents.getSensorEvents(SensorType.HeartRate));
        temperature = new SensorHolder<>(systemEvents.getSensorEvents(SensorType.Temperature));
        heartRate = new SensorHolder<>(systemEvents.getSensorEvents(SensorType.HeartRate));
        humidity = new SensorHolder<>(systemEvents.getSensorEvents(SensorType.Humidity));
        magnetometer = new SensorHolder<>(systemEvents.getSensorEvents(SensorType.Magnetometer));
        gyroscope = new SensorHolder<>(systemEvents.getSensorEvents(SensorType.Gyroscope));
        barometer = new SensorHolder<>(systemEvents.getSensorEvents(SensorType.Barometer));
//        optical = new SensorHolder<>(systemEvents.getSensorEvents(SensorType.Optical));
        ambientLight = new SensorHolder<>(systemEvents.getSensorEvents(SensorType.AmbientLight));
        calories = new SensorHolder<>(systemEvents.getSensorEvents(SensorType.Calories));
        pedometer = new SensorHolder<>(systemEvents.getSensorEvents(SensorType.Pedometer));
        gsr = new SensorHolder<>(systemEvents.getSensorEvents(SensorType.Gsr));
        skinTemperature = new SensorHolder<>(systemEvents.getSensorEvents(SensorType.SkinTemperature));
        uv = new SensorHolder<>(systemEvents.getSensorEvents(SensorType.UV));
    }


}
