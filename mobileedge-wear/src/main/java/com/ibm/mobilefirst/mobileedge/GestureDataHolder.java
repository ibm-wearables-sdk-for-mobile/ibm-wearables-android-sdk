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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Collects the sensors data needed for gesture recognition
 *
 * Created by cirilla on 12/04/2016.
 */
public class GestureDataHolder {

    //the minimum amount of data to send
    static final int MINIMUM_DATA_SIZE = 4;

    //holds the events for next data sending
    HashMap<Integer,LinkedList<EventData>> eventHashMap = new HashMap<>();

    //holds the normalization factors for each sensor
    HashMap<Integer,Double> normalizationFactorsHashMap = new HashMap<>();

    //will hold the last added data to prevent non consistent data
    int latestAddedDataType = Sensor.TYPE_ACCELEROMETER;


    /**
     * Add new sensor data to the data structure
     * @param event sensor event
     */
    public void addSensorData(SensorEvent event){
        int type = event.sensor.getType();

        //don't add new data if both the sensors are on and the new type is the same like the previous one
        if (!isBothSensorsOn() || (isBothSensorsOn() && latestAddedDataType != type)){

            latestAddedDataType = type;
            EventData eventData = new EventData(event);
            //normalize each new data
            eventData.normalizeData(normalizationFactorsHashMap.get(event.sensor.getType()));
            eventHashMap.get(event.sensor.getType()).add(eventData);
        }
    }

    private boolean isBothSensorsOn(){
        return eventHashMap.size() == 2;
    }

    public void setNormalizationFactor(int type, double factor){
        normalizationFactorsHashMap.put(type,factor);
    }

    public void allowSensorDataCollection(int type){
        eventHashMap.put(type,new LinkedList<EventData>());
    }

    public void disableSensorDataCollection(int type){
        eventHashMap.remove(type);
    }

    /**
     * @return is holds enough data for sending
     */
    public boolean isHasDataToSend(){

        boolean hasEnoughAccelerometerData = isHasEnoughData(Sensor.TYPE_ACCELEROMETER);
        boolean hasEnoughGyroscopeData = isHasEnoughData(Sensor.TYPE_GYROSCOPE);

        if (isBothSensorsOn()){
            return hasEnoughAccelerometerData && hasEnoughGyroscopeData;
        }

        else{
            return hasEnoughAccelerometerData || hasEnoughGyroscopeData;
        }
    }

    private boolean isHasEnoughData(int type){
        return eventHashMap.containsKey(type) && eventHashMap.get(type).size() >= MINIMUM_DATA_SIZE;
    }

    /**
     * @return the next available accelerometer data for sending
     */
    public List<EventData> pollNextAccelerometerData() {
        return pollNextData(Sensor.TYPE_ACCELEROMETER);
    }

    /**
     * @return the next available gyroscope data for sending
     */
    public List<EventData> pollNextGyroscopeData() {
        return pollNextData(Sensor.TYPE_GYROSCOPE);
    }

    /**
     * @return the next available data for sending for specific type, and remove it from queue
     */
    private List<EventData> pollNextData(int type){

        List<EventData> sensorEvents = new ArrayList<>();
        if (isHasEnoughData(type)){

            for (int i = 0; i < MINIMUM_DATA_SIZE; ++i){
                sensorEvents.add(eventHashMap.get(type).pop());
            }
        }

        return sensorEvents;
    }

    public class EventData{

        float[] values;
        long timestamp;

        public EventData(SensorEvent event) {
            values = event.values.clone();
            timestamp = event.timestamp;
        }

        public void normalizeData(double factor){
            values[0] *= factor;
            values[1] *= factor;
            values[2] *= factor;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(values);
        }
    }
}
