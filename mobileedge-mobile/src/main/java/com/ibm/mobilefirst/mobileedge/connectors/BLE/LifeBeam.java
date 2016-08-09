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

import com.ibm.mobilefirst.mobileedge.SensorType;
import com.ibm.mobilefirst.mobileedge.abstractmodel.HeartRateData;
import com.ibm.mobilefirst.mobileedge.events.SensorEvents;
import com.ibm.mobilefirst.mobileedge.events.SystemEvents;
import com.ibm.mobilefirst.mobileedge.events.messaging.SensorDataListener;
import com.ibm.mobilefirst.mobileedge.translators.HRPHeartRateTranslator;

import java.util.Arrays;
import java.util.List;

/**
 * BLE Heart Rate
 */
public class LifeBeam extends BLEDevice{
    static final private String TAG = "BLEHeartRateConnector";
    static final private HRPHeartRateTranslator hrTranslator = new HRPHeartRateTranslator();

    final private LifeBeam bleHeartRate = this;
    private SensorEvents heartRateSensorEvents;
    public LifeBeam(Context context){
        super("BLE Heart Rate", context, new BLEFilter() {
            @Override
            public boolean filter(ScannedDevice scanRecord) {
                return "LifeBEAM Hat".equals(scanRecord.device.getName());
            }
        });
    }

    @Override
    protected List<SensorType> getSupportedSensors() {
        List<SensorType> supportedSensors = Arrays.asList(SensorType.HeartRate);
        return supportedSensors;
    }

    @Override
    public void registerForEvents(SystemEvents systemEvents) {
        super.registerForEvents(systemEvents);
        registerHeartRate(systemEvents.getSensorEvents(SensorType.HeartRate));
    }

    private void registerHeartRate(SensorEvents sensorEvents){
        heartRateSensorEvents = sensorEvents;
        heartRateSensorEvents.turnOnCommand.addHandler(new SensorDataListener<Void>() {
            @Override
            public void onSensorDataChanged(Void data) {
                BluetoothGattService service = getBluetoothGatt().getService(BLEDevice.HEART_RATE_SERVICE);

                BluetoothGattCharacteristic chara = service.getCharacteristic(BLEDevice.HEART_RATE_MEASUREMENT_CHARACTERISTIC);
                BluetoothGattDescriptor descriptor = chara.getDescriptor(BLEDevice.CLIENT_CHARACTERISTIC_CONFIGURATION);

                //life is easy here, only turn on the notifications
                getBluetoothGatt().setCharacteristicNotification(chara, true);       //locally
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bleHeartRate.writeToGatt(descriptor);                           //remotely
            }
        });
        heartRateSensorEvents.turnOffCommand.addHandler(new SensorDataListener<Void>() {
            @Override
            public void onSensorDataChanged(Void data) {
                if(isServicesDiscovered()){
                    BluetoothGattService service = getBluetoothGatt().getService(BLEDevice.HEART_RATE_SERVICE);

                    BluetoothGattCharacteristic chara = service.getCharacteristic(BLEDevice.HEART_RATE_MEASUREMENT_CHARACTERISTIC);
                    BluetoothGattDescriptor descriptor = chara.getDescriptor(BLEDevice.CLIENT_CHARACTERISTIC_CONFIGURATION);

                    //life is easy here, only turn on the notifications
                    getBluetoothGatt().setCharacteristicNotification(chara, false);       //locally
                    descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    bleHeartRate.writeToGatt(descriptor);
                }
            }
        });
    }

    @Override
    protected void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        HeartRateData hrData = (HeartRateData)hrTranslator.translate(characteristic);
        heartRateSensorEvents.dataEvent.trigger(hrData);
    }
}
