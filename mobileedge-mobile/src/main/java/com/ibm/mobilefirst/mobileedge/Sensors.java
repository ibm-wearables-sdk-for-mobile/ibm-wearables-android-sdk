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
import com.ibm.mobilefirst.mobileedge.abstractmodel.GyroscopeData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.HeartRateData;
import com.ibm.mobilefirst.mobileedge.events.SystemEvents;

/**
 * Hold all the supported system sensors
 */
public class Sensors {

    public SensorHolder<AccelerometerData> accelerometer;
    public SensorHolder<GyroscopeData> gyroscope;
    public SensorHolder<HeartRateData> heartRate;

    /*
    Temperature, Humidity, Magnetometer, Gyroscope, Barometer, Optical, AmbientLight, Calories, Gsr, Pedometer, HeartRate, SkinTemperature
    */

    public Sensors(SystemEvents systemEvents) {
        accelerometer = new SensorHolder<>(systemEvents.getSensorEvents(SensorType.Accelerometer));
        gyroscope = new SensorHolder<>(systemEvents.getSensorEvents(SensorType.Gyroscope));
        heartRate = new SensorHolder<>(systemEvents.getSensorEvents(SensorType.HeartRate));
    }


}
