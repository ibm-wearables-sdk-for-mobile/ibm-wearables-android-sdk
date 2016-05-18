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

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the watch sensors data, lister for data and send it to the phone
 */
public class DataManager implements SensorEventListener {

    SensorManager sensorManager;
    DataSender dataSender;

    GestureDataHolder gestureDataHolder = new GestureDataHolder();

    //holds the current active sensors (sensors that user wants to be on)
    ArrayList<Integer> activeSensors = new ArrayList<>();

    public DataManager(SensorManager sensorManager, DataSender dataSender){

        //sensorsHolder = new SensorsHolder(sensorManager);
        this.sensorManager = sensorManager;
        sensorManager.unregisterListener(this);
        this.dataSender = dataSender;
    }

    public void turnSensorOn(int type){
        registerSensor(type);
        activeSensors.add(type);
    }

    /**
     * register the default sensor of specific type
     * @param type sensor id
     */
    private void registerSensor(int type){
        Sensor defaultSensor = sensorManager.getDefaultSensor(type);

        //set normalization factor
        if (type == Sensor.TYPE_ACCELEROMETER){
            gestureDataHolder.allowSensorDataCollection(type);

            //normalize to range form -2 to 2
            gestureDataHolder.setNormalizationFactor(type, 2 / defaultSensor.getMaximumRange());
        }

        if (type == Sensor.TYPE_GYROSCOPE){
            gestureDataHolder.allowSensorDataCollection(type);

            //from rad to deg
            gestureDataHolder.setNormalizationFactor(type, 57.2958);
        }

        sensorManager.registerListener(this, defaultSensor, SensorManager.SENSOR_DELAY_GAME);
        //sensorManager.registerListener(this, defaultSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void turnSensorOff(int type){
        unregisterSensor(type);
        activeSensors.remove(Integer.valueOf(type));
    }

    private void unregisterSensor(int type){
        Sensor defaultSensor = sensorManager.getDefaultSensor(type);
        sensorManager.unregisterListener(this, defaultSensor);

        //will prevent collecting data from the sensor
        gestureDataHolder.disableSensorDataCollection(type);
    }

    public void turnAllSensorsOff(){
        sensorManager.unregisterListener(this);
        activeSensors.clear();
    }

    public void pauseAllSensors() {
        for (Integer type : activeSensors){
            unregisterSensor(type);
        }
    }

    public void resumeAllSensors() {
        for (Integer type : activeSensors){
            registerSensor(type);
        }
    }

    /**
     * This method is called each time sensors data is changed
     * @param event sensor event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        final int sensorType = event.sensor.getType();

        if (sensorType == Sensor.TYPE_ACCELEROMETER || sensorType == Sensor.TYPE_GYROSCOPE){
            //add the event for future sending
            gestureDataHolder.addSensorData(event);

            if (gestureDataHolder.isHasDataToSend()){
                sendNextGestureData();
            }
        }

        else if (sensorType == Sensor.TYPE_HEART_RATE){
            sendHeartRateData(event);
        }
    }

    /**
     * @return new Data Map Request
     */
    private PutDataMapRequest getNewSensorsDataMapRequest(){
        return PutDataMapRequest.create("/sensors");
    }

    /**
     * Create new Data Request and send it to the phone
     */
    private void sendNextGestureData() {
        PutDataMapRequest dataMapRequest = getNewSensorsDataMapRequest();
        DataMap dataMap = dataMapRequest.getDataMap();

        List<GestureDataHolder.EventData> nextAccelerometerData = gestureDataHolder.pollNextAccelerometerData();
        if (nextAccelerometerData.size() > 0){
            dataMap.putDataMapArrayList("accelerometer", convertEventsToDataMapList(nextAccelerometerData));
        }

        List<GestureDataHolder.EventData> nextGyroscopeData = gestureDataHolder.pollNextGyroscopeData();
        if (nextGyroscopeData.size() > 0){
            dataMap.putDataMapArrayList("gyroscope", convertEventsToDataMapList(nextGyroscopeData));
        }

        dataSender.sendData(dataMapRequest);
    }

    /**+
     * Convert the sensors event to data that will be sent to the phone
     * @param eventsList all the events to convert
     * @return converted list
     */
    private ArrayList<DataMap> convertEventsToDataMapList(List<GestureDataHolder.EventData> eventsList){
        ArrayList<DataMap> dataMapsList = new ArrayList<>();

        for (GestureDataHolder.EventData event : eventsList){
            DataMap eventDataMap = new DataMap();
            eventDataMap.putLong("timeStamp", event.timestamp);
            eventDataMap.putFloatArray("values", event.values);
            dataMapsList.add(eventDataMap);
        }

        return dataMapsList;
    }


    /**
     * Create next data to send of the hear rate
     * @param event sensor event
     */
    private void sendHeartRateData(SensorEvent event){
        PutDataMapRequest dataMapRequest = getNewSensorsDataMapRequest();

        DataMap dataMap = dataMapRequest.getDataMap();
        dataMap.putFloat("heartrate",event.values[0]);

        dataSender.sendData(dataMapRequest);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
