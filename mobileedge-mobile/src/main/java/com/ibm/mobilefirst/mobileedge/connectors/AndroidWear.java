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

package com.ibm.mobilefirst.mobileedge.connectors;

import android.content.Context;
import android.hardware.Sensor;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.ibm.mobilefirst.mobileedge.SensorType;
import com.ibm.mobilefirst.mobileedge.abstractmodel.AccelerometerData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.GyroscopeData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.HeartRateData;
import com.ibm.mobilefirst.mobileedge.events.SensorEvents;
import com.ibm.mobilefirst.mobileedge.events.SystemEvents;
import com.ibm.mobilefirst.mobileedge.events.messaging.SensorDataListener;
import com.ibm.mobilefirst.mobileedge.interfaces.ConnectionStatusListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Android wear connector
 */
public class AndroidWear extends DeviceConnector implements GoogleApiClient.ConnectionCallbacks, DataApi.DataListener, MessageApi.MessageListener {

    GoogleApiClient apiClient;
    ExecutorService executorService = Executors.newCachedThreadPool();
    WatchMessaging watchMessaging = new WatchMessaging();

    SensorEvents accelerometerSensorEvents;
    SensorEvents gyroscopeSensorEvents;
    SensorEvents heartRateSensorEvents;

    public AndroidWear() {
        super("Android Wear");
    }

    @Override
    public void connect(Context context, ConnectionStatusListener connectionListener){
        super.connect(context,connectionListener);

        apiClient = new GoogleApiClient.Builder(context)
                .addApi( Wearable.API )
                .build();

        apiClient.registerConnectionCallbacks(this);
        apiClient.connect();
    }

    @Override
    public void disconnect() {
        super.disconnect();
        watchMessaging.turnAllSensorsOff();
        apiClient.disconnect();
        updateConnectionStatus(ConnectionStatus.Disconnected);
    }

    @Override
    public void registerForEvents(SystemEvents systemEvents) {
        super.registerForEvents(systemEvents);

        accelerometerSensorEvents = registerTurnOnAndOffEvents(systemEvents,SensorType.Accelerometer,Sensor.TYPE_ACCELEROMETER);
        gyroscopeSensorEvents = registerTurnOnAndOffEvents(systemEvents,SensorType.Gyroscope,Sensor.TYPE_GYROSCOPE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            heartRateSensorEvents = registerTurnOnAndOffEvents(systemEvents,SensorType.HeartRate,Sensor.TYPE_HEART_RATE);
        }
    }


    @Override
    protected List<SensorType> getSupportedSensors() {

        ArrayList<SensorType> supportedSensors = new ArrayList<>();
        supportedSensors.add(SensorType.Accelerometer);
        supportedSensors.add(SensorType.Gyroscope);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            supportedSensors.add(SensorType.HeartRate);
        }

        return supportedSensors;
    }

    /**
     * Register for the on and off events
     * @param systemEvents system events
     * @param sensorType mobile edge sensor type
     * @param sensorOSType android sensor type
     * @return the sensors events for the sensor
     */
    private SensorEvents registerTurnOnAndOffEvents(SystemEvents systemEvents, SensorType sensorType, final int sensorOSType){

        SensorEvents sensorEvents = systemEvents.getSensorEvents(sensorType);

        sensorEvents.turnOnCommand.addHandler(new SensorDataListener<Void>() {
            @Override
            public void onSensorDataChanged(Void data) {
                watchMessaging.turnWatchSensorOn(sensorOSType);
            }
        });

        sensorEvents.turnOffCommand.addHandler(new SensorDataListener<Void>() {
            @Override
            public void onSensorDataChanged(Void data) {
                watchMessaging.turnWatchSensorOff(sensorOSType);
            }
        });

        return sensorEvents;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(apiClient, this);
        Wearable.MessageApi.addListener(apiClient, this);

        //send ping message to test connection
        watchMessaging.testConnection();
    }

    @Override
    public void onConnectionSuspended(int i) {
        updateConnectionStatus(ConnectionStatus.Disconnected);
    }

    @Override
    public void onDataChanged(final DataEventBuffer dataEvents) {

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {

                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/sensors") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    parseDataMap(dataMap);
                }
            }
        }
    }

    /**
     * Parse the data from the watch
     * @param dataMap - holds the watch sensors data
     */
    private void parseDataMap(DataMap dataMap) {
        triggerAccelerometerUpdate(dataMap);
        triggerGyroscopeUpdate(dataMap);
        trigerHeartRateUpdate(dataMap);
    }

    private void trigerHeartRateUpdate(DataMap dataMap) {
        final String HEART_RATE_KEY = "heartrate";

        if (dataMap.containsKey(HEART_RATE_KEY)){
            float heartRate = dataMap.getFloat(HEART_RATE_KEY);

            HeartRateData data = new HeartRateData(heartRate);
            heartRateSensorEvents.dataEvent.trigger(data);
        }
    }

    private void triggerGyroscopeUpdate(DataMap dataMap) {

        final String GYROSCOPE_KEY = "gyroscope";

        if (dataMap.containsKey(GYROSCOPE_KEY)){
            ArrayList<DataMap> gyroscopeDataArrayList = dataMap.getDataMapArrayList(GYROSCOPE_KEY);

            for (DataMap gyroscopeDataMap : gyroscopeDataArrayList){

                float[] values = gyroscopeDataMap.getFloatArray("values");

                GyroscopeData data = new GyroscopeData();

                data.x = values[0];
                data.y = values[1];
                data.z = values[2];

                gyroscopeSensorEvents.dataEvent.trigger(data);
            }
        }
    }

    private void triggerAccelerometerUpdate(DataMap dataMap){

        final String ACCELEROMETER_KEY = "accelerometer";

        if (dataMap.containsKey(ACCELEROMETER_KEY)){
            ArrayList<DataMap> accelerometerDataArrayList = dataMap.getDataMapArrayList(ACCELEROMETER_KEY);

            for (DataMap accelerometerDataMap : accelerometerDataArrayList){

                float[] values = accelerometerDataMap.getFloatArray("values");

                AccelerometerData data = new AccelerometerData();
                data.x = values[0];
                data.y = values[1];
                data.z = values[2];

                accelerometerSensorEvents.dataEvent.trigger(data);
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        //this is response from ping request
        if (messageEvent.getPath().equals("/connected")){
            updateConnectionStatus(ConnectionStatus.Connected);
        }
    }


    /**
     * Handle the messaging to the watch
     */
    class WatchMessaging{

        public void turnWatchSensorOn(int type){
            sendMessage("/start",String.valueOf(type));
        }

        public void turnWatchSensorOff(int type){
            sendMessage("/stop",String.valueOf(type));
        }

        public void turnAllSensorsOff(){
            sendMessage("/stopAll","");
        }

        public void testConnection(){
            sendMessage("/ping","");
        }

        private void sendMessage( final String path, final String data ) {

            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(apiClient).await();
                    for (Node node : nodes.getNodes()) {
                        if (node.isNearby()) {
                            Wearable.MessageApi.sendMessage(apiClient, node.getId(), path, data.getBytes());
                        }
                    }
                }
            });
        }
    }
}
