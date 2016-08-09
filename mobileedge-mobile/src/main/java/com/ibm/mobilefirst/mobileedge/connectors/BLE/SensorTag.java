
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

package com.ibm.mobilefirst.mobileedge.connectors.BLE;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.ibm.mobilefirst.mobileedge.SensorType;
import com.ibm.mobilefirst.mobileedge.abstractmodel.AccelerometerData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.BaseSensorData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.GyroscopeData;
import com.ibm.mobilefirst.mobileedge.events.SensorEvents;
import com.ibm.mobilefirst.mobileedge.events.SystemEvents;
import com.ibm.mobilefirst.mobileedge.events.messaging.SensorDataListener;
import com.ibm.mobilefirst.mobileedge.translators.TIAccelerometerTranslator;
import com.ibm.mobilefirst.mobileedge.translators.TIBarometerTranslator;
import com.ibm.mobilefirst.mobileedge.translators.TIGyroscopeTranslator;
import com.ibm.mobilefirst.mobileedge.translators.TIHumidityTranslator;
import com.ibm.mobilefirst.mobileedge.translators.TILuxometerTranslator;
import com.ibm.mobilefirst.mobileedge.translators.TIMagnetometerTranslator;
import com.ibm.mobilefirst.mobileedge.translators.TITemperatureTranslator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SensorTag extends BLEDevice {
    ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = null;


    //Translators
    private TIAccelerometerTranslator accTranslator = new TIAccelerometerTranslator();
    private TITemperatureTranslator tmpTranslator = new TITemperatureTranslator();
    private TIGyroscopeTranslator gyroTranslator = new TIGyroscopeTranslator();
    private TIMagnetometerTranslator magnetoTranslator = new TIMagnetometerTranslator();
    private TIHumidityTranslator humidTranslator = new TIHumidityTranslator();
    private TIBarometerTranslator barometerTranslator = new TIBarometerTranslator();
    private TILuxometerTranslator luxometerTranslator = new TILuxometerTranslator();

    //tags
    private final String TAG = "Sensor Tag";
    final SensorTag sensorTag = this;


    // flags
    private static boolean isAccelerometerEnabled = false;
    private static boolean isGyroscopeEnabled = false;
    private static boolean isMagnetometerEnabled = false;

    //sensors
    private SensorEvents accelerometerSensorEvents;
    private SensorEvents temperatureSensorEvents;
    private SensorEvents gyroscopeSensorEvents;
    private SensorEvents magnetometerSensorEvents;
    private SensorEvents humiditySensorEvents;
    private SensorEvents barometerSensorEvents;
    private SensorEvents luxometerSensorEvents;



    public SensorTag(Context context) {
        super("BLE Heart Rate", context, new BLEFilter() {
            @Override
            public boolean filter(BLEDevice.ScannedDevice scanRecord) {
                return "CC2650 SensorTag".equals(scanRecord.device.getName());
            }
        });
    }

    @Override
    protected List<SensorType> getSupportedSensors() {
        Log.d(TAG, "Getting supported sensors");
        ArrayList<SensorType> supportedSensors = new ArrayList<>();
        supportedSensors.add(SensorType.Accelerometer);
        supportedSensors.add(SensorType.Temperature);
        supportedSensors.add(SensorType.Gyroscope);
        supportedSensors.add(SensorType.Magnetometer);
        supportedSensors.add(SensorType.Humidity);
        supportedSensors.add(SensorType.Barometer);
        supportedSensors.add(SensorType.AmbientLight);

        return supportedSensors;
    }

    @Override
    public void registerForEvents(SystemEvents systemEvents) {
        super.registerForEvents(systemEvents);
        Log.d(TAG, "Inside registerForEvents");
        registerAccelerometer(systemEvents.getSensorEvents(SensorType.Accelerometer));
        registerTemperature(systemEvents.getSensorEvents(SensorType.Temperature));
        registerGyroscope(systemEvents.getSensorEvents(SensorType.Gyroscope));
        registerMagnetometer(systemEvents.getSensorEvents(SensorType.Magnetometer));
        registerHumidity(systemEvents.getSensorEvents(SensorType.Humidity));

        registerBarometer(systemEvents.getSensorEvents(SensorType.Barometer));
        registerLuxometer(systemEvents.getSensorEvents(SensorType.AmbientLight));

    }


    private void registerAccelerometer(SensorEvents sensorEvents){

        isAccelerometerEnabled = true;
        accelerometerSensorEvents = sensorEvents;
        accelerometerSensorEvents.turnOnCommand.addHandler(new SensorDataListener<Void>() {
            @Override
            public void onSensorDataChanged(Void voidparameter) {
                BluetoothGattService service = getBluetoothGatt().getService(BLEDevice.SENSORTAG_MOTION_SERVICE);

                BluetoothGattCharacteristic data = service.getCharacteristic(BLEDevice.SENSORTAG_MOTION_DATA_CHARACTERISTIC);
                BluetoothGattDescriptor descriptor = data.getDescriptor(BLEDevice.CLIENT_CHARACTERISTIC_CONFIGURATION);

                //turn on data notifications
                getBluetoothGatt().setCharacteristicNotification(data, true);       //locally
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                sensorTag.writeToGatt(descriptor);                  //remotely

                //do bit stuff
                BluetoothGattCharacteristic config = service.getCharacteristic(BLEDevice.SENSORTAG_MOTION_CONFIGURATION);
                config.setValue(127, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                sensorTag.writeToGatt(config);

                BluetoothGattCharacteristic period = service.getCharacteristic(BLEDevice.SENSORTAG_MOTION_PERIOD_CHARACTERISTIC);
                period.setValue(100, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                sensorTag.writeToGatt(period);



                //turnOnService(sc);
            }
        });
        accelerometerSensorEvents.turnOffCommand.addHandler(new SensorDataListener<Void>() {
            @Override
            public void onSensorDataChanged(Void param) {
                isAccelerometerEnabled = false;

                BluetoothGattService service = getBluetoothGatt().getService(BLEDevice.SENSORTAG_MOTION_SERVICE);
                BluetoothGattCharacteristic data = service.getCharacteristic(BLEDevice.SENSORTAG_MOTION_DATA_CHARACTERISTIC);
                BluetoothGattDescriptor descriptor = data.getDescriptor(BLEDevice.CLIENT_CHARACTERISTIC_CONFIGURATION);

                //turn off data notifications
                getBluetoothGatt().setCharacteristicNotification(data, true);
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                sensorTag.writeToGatt(descriptor);


                //change service configuration
                BluetoothGattCharacteristic config = service.getCharacteristic(BLEDevice.SENSORTAG_MOTION_CONFIGURATION);
                config.setValue(0, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                sensorTag.writeToGatt(config);
            }
        });
    }


    private void registerGyroscope(SensorEvents sensorEvents){

        isGyroscopeEnabled = true;
       gyroscopeSensorEvents = sensorEvents;
        gyroscopeSensorEvents.turnOnCommand.addHandler(new SensorDataListener<Void>() {
            @Override
            public void onSensorDataChanged(Void voidparameter) {
                BluetoothGattService service = getBluetoothGatt().getService(BLEDevice.SENSORTAG_MOTION_SERVICE);

                BluetoothGattCharacteristic data = service.getCharacteristic(BLEDevice.SENSORTAG_MOTION_DATA_CHARACTERISTIC);
                BluetoothGattDescriptor descriptor = data.getDescriptor(BLEDevice.CLIENT_CHARACTERISTIC_CONFIGURATION);

                //turn on data notifications
                getBluetoothGatt().setCharacteristicNotification(data, true);       //locally
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                sensorTag.writeToGatt(descriptor);                  //remotely

                //do bit stuff
                BluetoothGattCharacteristic config = service.getCharacteristic(BLEDevice.SENSORTAG_MOTION_CONFIGURATION);
                config.setValue(127, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                sensorTag.writeToGatt(config);

                BluetoothGattCharacteristic period = service.getCharacteristic(BLEDevice.SENSORTAG_MOTION_PERIOD_CHARACTERISTIC);
                period.setValue(100, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                sensorTag.writeToGatt(period);



                //turnOnService(sc);
            }
        });
        gyroscopeSensorEvents.turnOffCommand.addHandler(new SensorDataListener<Void>() {
            @Override
            public void onSensorDataChanged(Void param) {
                isGyroscopeEnabled = false;
                BluetoothGattService service = getBluetoothGatt().getService(BLEDevice.SENSORTAG_MOTION_SERVICE);
                BluetoothGattCharacteristic data = service.getCharacteristic(BLEDevice.SENSORTAG_MOTION_DATA_CHARACTERISTIC);
                BluetoothGattDescriptor descriptor = data.getDescriptor(BLEDevice.CLIENT_CHARACTERISTIC_CONFIGURATION);

                //turn off data notifications
                getBluetoothGatt().setCharacteristicNotification(data, true);
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                sensorTag.writeToGatt(descriptor);


                //change service configuration
                BluetoothGattCharacteristic config = service.getCharacteristic(BLEDevice.SENSORTAG_MOTION_CONFIGURATION);
                config.setValue(0, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                sensorTag.writeToGatt(config);
            }
        });
    }


    private void registerMagnetometer(SensorEvents sensorEvents){

        isMagnetometerEnabled = true;
        magnetometerSensorEvents = sensorEvents;
        magnetometerSensorEvents.turnOnCommand.addHandler(new SensorDataListener<Void>() {
            @Override
            public void onSensorDataChanged(Void voidparameter) {
                BluetoothGattService service = getBluetoothGatt().getService(BLEDevice.SENSORTAG_MOTION_SERVICE);

                BluetoothGattCharacteristic data = service.getCharacteristic(BLEDevice.SENSORTAG_MOTION_DATA_CHARACTERISTIC);
                BluetoothGattDescriptor descriptor = data.getDescriptor(BLEDevice.CLIENT_CHARACTERISTIC_CONFIGURATION);

                //turn on data notifications
                getBluetoothGatt().setCharacteristicNotification(data, true);       //locally
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                sensorTag.writeToGatt(descriptor);                  //remotely

                //do bit stuff
                BluetoothGattCharacteristic config = service.getCharacteristic(BLEDevice.SENSORTAG_MOTION_CONFIGURATION);
                config.setValue(127, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                sensorTag.writeToGatt(config);

                BluetoothGattCharacteristic period = service.getCharacteristic(BLEDevice.SENSORTAG_MOTION_PERIOD_CHARACTERISTIC);
                period.setValue(100, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                sensorTag.writeToGatt(period);



                //turnOnService(sc);
            }
        });
        magnetometerSensorEvents.turnOffCommand.addHandler(new SensorDataListener<Void>() {
            @Override
            public void onSensorDataChanged(Void param) {
                BluetoothGattService service = getBluetoothGatt().getService(BLEDevice.SENSORTAG_MOTION_SERVICE);
                BluetoothGattCharacteristic data = service.getCharacteristic(BLEDevice.SENSORTAG_MOTION_DATA_CHARACTERISTIC);
                BluetoothGattDescriptor descriptor = data.getDescriptor(BLEDevice.CLIENT_CHARACTERISTIC_CONFIGURATION);

                //turn off data notifications
                getBluetoothGatt().setCharacteristicNotification(data, true);
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                sensorTag.writeToGatt(descriptor);


                //change service configuration
                BluetoothGattCharacteristic config = service.getCharacteristic(BLEDevice.SENSORTAG_MOTION_CONFIGURATION);
                config.setValue(0, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                sensorTag.writeToGatt(config);
            }
        });
    }

    private void registerTemperature(final SensorEvents sensorEvents) {
        temperatureSensorEvents = sensorEvents;
        temperatureSensorEvents.turnOnCommand.addHandler(new SensorDataListener<Void>() {
            @Override
            public void onSensorDataChanged(Void param) {
                BluetoothGattService service = getBluetoothGatt().getService(BLEDevice.SENSORTAG_TEMPERATURE_SERIVCE);

                BluetoothGattCharacteristic data = service.getCharacteristic(BLEDevice.SENSORTAG_TEMPERATURE_DATA_CHARACTERISTIC);
                BluetoothGattDescriptor descriptor = data.getDescriptor(BLEDevice.CLIENT_CHARACTERISTIC_CONFIGURATION);

                //turn on data notifications
                getBluetoothGatt().setCharacteristicNotification(data, true);       //locally
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                sensorTag.writeToGatt(descriptor);                  //remotely

                //do bit stuff
                BluetoothGattCharacteristic config = service.getCharacteristic(BLEDevice.SENSORTAG_TEMPERATURE_CONFIGURATION);
                config.setValue(1, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                sensorTag.writeToGatt(config);

                BluetoothGattCharacteristic period = service.getCharacteristic(BLEDevice.SENSORTAG_TEMPERATURE_PERIOD_CHARACTERISTIC);
                period.setValue(100, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                sensorTag.writeToGatt(period);
            }
        });
        temperatureSensorEvents.turnOffCommand.addHandler(new SensorDataListener<Void>() {
            @Override
            public void onSensorDataChanged(Void param) {
                BluetoothGattService service = getBluetoothGatt().getService(BLEDevice.SENSORTAG_TEMPERATURE_SERIVCE);
                BluetoothGattCharacteristic data = service.getCharacteristic(BLEDevice.SENSORTAG_TEMPERATURE_DATA_CHARACTERISTIC);
                BluetoothGattDescriptor descriptor = data.getDescriptor(BLEDevice.CLIENT_CHARACTERISTIC_CONFIGURATION);

                //turn off data notifications
                getBluetoothGatt().setCharacteristicNotification(data, true);
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                sensorTag.writeToGatt(descriptor);


                //change service configuration
                BluetoothGattCharacteristic config = service.getCharacteristic(BLEDevice.SENSORTAG_TEMPERATURE_CONFIGURATION);
                config.setValue(0, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                sensorTag.writeToGatt(config);
            }

        });
    }


    private void registerHumidity(final SensorEvents sensorEvents) {
        humiditySensorEvents = sensorEvents;
        humiditySensorEvents.turnOnCommand.addHandler(new SensorDataListener<Void>() {
            @Override
            public void onSensorDataChanged(Void param) {
                BluetoothGattService service = getBluetoothGatt().getService(BLEDevice.SENSORTAG_HUMIDITY_SERVICE);

                BluetoothGattCharacteristic data = service.getCharacteristic(BLEDevice.SENSORTAG_HUMIDITY_DATA_CHARACTERISTIC);
                BluetoothGattDescriptor descriptor = data.getDescriptor(BLEDevice.CLIENT_CHARACTERISTIC_CONFIGURATION);

                //turn on data notifications
                getBluetoothGatt().setCharacteristicNotification(data, true);       //locally
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                sensorTag.writeToGatt(descriptor);                  //remotely

                //do bit stuff
                BluetoothGattCharacteristic config = service.getCharacteristic(BLEDevice.SENSORTAG_HUMIDITY_CONFIGURATION);
                config.setValue(1, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                sensorTag.writeToGatt(config);

                BluetoothGattCharacteristic period = service.getCharacteristic(BLEDevice.SENSORTAG_HUMIDITY_PERIOD_CHARACTERISTIC);
                period.setValue(100, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                sensorTag.writeToGatt(period);
            }
        });
        humiditySensorEvents.turnOffCommand.addHandler(new SensorDataListener<Void>() {
            @Override
            public void onSensorDataChanged(Void param) {
                BluetoothGattService service = getBluetoothGatt().getService(BLEDevice.SENSORTAG_HUMIDITY_SERVICE);
                BluetoothGattCharacteristic data = service.getCharacteristic(BLEDevice.SENSORTAG_HUMIDITY_DATA_CHARACTERISTIC);
                BluetoothGattDescriptor descriptor = data.getDescriptor(BLEDevice.CLIENT_CHARACTERISTIC_CONFIGURATION);

                //turn off data notifications
                getBluetoothGatt().setCharacteristicNotification(data, true);
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                sensorTag.writeToGatt(descriptor);


                //change service configuration
                BluetoothGattCharacteristic config = service.getCharacteristic(BLEDevice.SENSORTAG_HUMIDITY_CONFIGURATION);
                config.setValue(0, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                sensorTag.writeToGatt(config);
            }

        });
    }


    private void registerBarometer(final SensorEvents sensorEvents) {
        barometerSensorEvents = sensorEvents;
        barometerSensorEvents.turnOnCommand.addHandler(new SensorDataListener<Void>() {
            @Override
            public void onSensorDataChanged(Void param) {
                BluetoothGattService service = getBluetoothGatt().getService(BLEDevice.SENSORTAG_BAROMETER_SERVICE);

                BluetoothGattCharacteristic data = service.getCharacteristic(BLEDevice.SENSORTAG_BAROMETER_DATA_CHARACTERISTIC);
                BluetoothGattDescriptor descriptor = data.getDescriptor(BLEDevice.CLIENT_CHARACTERISTIC_CONFIGURATION);

                //turn on data notifications
                getBluetoothGatt().setCharacteristicNotification(data, true);       //locally
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                sensorTag.writeToGatt(descriptor);                  //remotely

                //do bit stuff
                BluetoothGattCharacteristic config = service.getCharacteristic(BLEDevice.SENSORTAG_BAROMETER_CONFIGURATION);
                config.setValue(1, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                sensorTag.writeToGatt(config);

                BluetoothGattCharacteristic period = service.getCharacteristic(BLEDevice.SENSORTAG_BAROMETER_PERIOD_CHARACTERISTIC);
                period.setValue(100, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                sensorTag.writeToGatt(period);
            }
        });
        barometerSensorEvents.turnOffCommand.addHandler(new SensorDataListener<Void>() {
            @Override
            public void onSensorDataChanged(Void param) {
                BluetoothGattService service = getBluetoothGatt().getService(BLEDevice.SENSORTAG_BAROMETER_SERVICE);
                BluetoothGattCharacteristic data = service.getCharacteristic(BLEDevice.SENSORTAG_BAROMETER_DATA_CHARACTERISTIC);
                BluetoothGattDescriptor descriptor = data.getDescriptor(BLEDevice.CLIENT_CHARACTERISTIC_CONFIGURATION);

                //turn off data notifications
                getBluetoothGatt().setCharacteristicNotification(data, true);
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                sensorTag.writeToGatt(descriptor);


                //change service configuration
                BluetoothGattCharacteristic config = service.getCharacteristic(BLEDevice.SENSORTAG_BAROMETER_CONFIGURATION);
                config.setValue(0, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                sensorTag.writeToGatt(config);
            }

        });
    }

    private void registerLuxometer(final SensorEvents sensorEvents) {
        luxometerSensorEvents = sensorEvents;
        luxometerSensorEvents.turnOnCommand.addHandler(new SensorDataListener<Void>() {
            @Override
            public void onSensorDataChanged(Void param) {
                BluetoothGattService service = getBluetoothGatt().getService(BLEDevice.SENSORTAG_LUXOMETER_SERVICE);

                BluetoothGattCharacteristic data = service.getCharacteristic(BLEDevice.SENSORTAG_LUXOMETER_DATA_CHARACTERISTIC);
                BluetoothGattDescriptor descriptor = data.getDescriptor(BLEDevice.CLIENT_CHARACTERISTIC_CONFIGURATION);

                //turn on data notifications
                getBluetoothGatt().setCharacteristicNotification(data, true);       //locally
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                sensorTag.writeToGatt(descriptor);                  //remotely

                //do bit stuff
                BluetoothGattCharacteristic config = service.getCharacteristic(BLEDevice.SENSORTAG_LUXOMETER_CONFIGURATION);
                config.setValue(1, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                sensorTag.writeToGatt(config);

                BluetoothGattCharacteristic period = service.getCharacteristic(BLEDevice.SENSORTAG_LUXOMETER_PERIOD_CHARACTERISTIC);
                period.setValue(80, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                sensorTag.writeToGatt(period);
            }
        });
        luxometerSensorEvents.turnOffCommand.addHandler(new SensorDataListener<Void>() {
            @Override
            public void onSensorDataChanged(Void param) {
                BluetoothGattService service = getBluetoothGatt().getService(BLEDevice.SENSORTAG_LUXOMETER_SERVICE);
                BluetoothGattCharacteristic data = service.getCharacteristic(BLEDevice.SENSORTAG_LUXOMETER_DATA_CHARACTERISTIC);
                BluetoothGattDescriptor descriptor = data.getDescriptor(BLEDevice.CLIENT_CHARACTERISTIC_CONFIGURATION);

                //turn off data notifications
                getBluetoothGatt().setCharacteristicNotification(data, true);
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                sensorTag.writeToGatt(descriptor);


                //change service configuration
                BluetoothGattCharacteristic config = service.getCharacteristic(BLEDevice.SENSORTAG_LUXOMETER_CONFIGURATION);
                config.setValue(0, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                sensorTag.writeToGatt(config);
            }

        });
    }

    @Override
    protected void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        //Log.d(TAG, "Inside onCharacteristicChanged " + characteristic.getUuid().toString());

        UUID  uuid = characteristic.getUuid();
        if(uuid.equals(BLEDevice.SENSORTAG_MOTION_DATA_CHARACTERISTIC)){
            if(isAccelerometerEnabled) {
                BaseSensorData data = accTranslator.translate(characteristic);
                accelerometerSensorEvents.dataEvent.trigger(data);
            }
            if(isGyroscopeEnabled){
                BaseSensorData data = gyroTranslator.translate(characteristic);
                gyroscopeSensorEvents.dataEvent.trigger(data);
            }
            if(isMagnetometerEnabled){
                BaseSensorData data = magnetoTranslator.translate(characteristic);
                magnetometerSensorEvents.dataEvent.trigger(data);
            }
        }
        else if(uuid.equals(BLEDevice.SENSORTAG_TEMPERATURE_DATA_CHARACTERISTIC)) {
            BaseSensorData data = tmpTranslator.translate(characteristic);
            temperatureSensorEvents.dataEvent.trigger(data);
        }
        else if(uuid.equals(BLEDevice.SENSORTAG_HUMIDITY_DATA_CHARACTERISTIC)){
            BaseSensorData data = humidTranslator.translate(characteristic);
            humiditySensorEvents.dataEvent.trigger(data);
        }
        else if(uuid.equals(BLEDevice.SENSORTAG_BAROMETER_DATA_CHARACTERISTIC)){
            BaseSensorData data = barometerTranslator.translate(characteristic);
            barometerSensorEvents.dataEvent.trigger(data);
        } else if (uuid.equals(BLEDevice.SENSORTAG_LUXOMETER_DATA_CHARACTERISTIC)) {
            BaseSensorData data = luxometerTranslator.translate(characteristic);
            luxometerSensorEvents.dataEvent.trigger(data);
        }
    }
}