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

import android.hardware.SensorManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class MessageReceiverService extends WearableListenerService implements DataSender {

    GoogleApiClient apiClient;
    static DataManager dataManager;

    @Override
    public void onCreate() {
        super.onCreate();

        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        apiClient.connect();

        if (dataManager == null){
            dataManager = new DataManager((SensorManager)getSystemService(SENSOR_SERVICE), this);
        }
    }

    /**
     * Handle messages from the phone
     * @param messageEvent new message from the phone
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals("/start")) {
            dataManager.turnSensorOn(Integer.valueOf(new String(messageEvent.getData())));
        }

        else if (messageEvent.getPath().equals("/stop")){
            dataManager.turnSensorOff(Integer.valueOf(new String(messageEvent.getData())));
        }

        else if (messageEvent.getPath().equals("/stopAll")){
            dataManager.turnAllSensorsOff();
        }

        else if (messageEvent.getPath().equals("/ping")){
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(apiClient).await();
            for(Node node : nodes.getNodes()) {
                Wearable.MessageApi.sendMessage(apiClient, node.getId(), "/connected", null).await();
            }
        }
    }

    /**
     * Send the sensors data to the phone
     * @param dataMapRequest data map to send
     * @return send result
     */
    @Override
    public PendingResult<DataApi.DataItemResult> sendData(PutDataMapRequest dataMapRequest) {
        PutDataRequest dataRequest = dataMapRequest.asPutDataRequest();
        dataRequest.setUrgent();

        return Wearable.DataApi.putDataItem(apiClient, dataRequest);
    }
}
