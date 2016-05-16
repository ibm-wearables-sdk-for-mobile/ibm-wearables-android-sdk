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

import com.ibm.mobilefirst.mobileedge.SensorType;
import com.ibm.mobilefirst.mobileedge.events.SystemEvents;
import com.ibm.mobilefirst.mobileedge.interfaces.ConnectionStatusListener;

import java.util.List;

/**
 * Base device connector that will be used to create new connectors to new devices
 */
public abstract class DeviceConnector {

    public String deviceName = "";
    ConnectionStatusListener connectionListener;

    public DeviceConnector(String deviceName){
        this.deviceName = deviceName;
    }

    /**
     * Connect to a device
     * @param context application context
     * @param connectionListener connection status listener
     */
    public void connect(Context context, ConnectionStatusListener connectionListener){
        this.connectionListener = connectionListener;
    }

    /**
     * Disconnect from the device
     */
    public void disconnect(){

    }

    /**
     * Register listeners for the needed sensors of the device
     * @param systemEvents sensors that will be used to register listeners
     */
    public void registerForEvents(SystemEvents systemEvents){

    }

    /**
     * Notify about change of the connection status of the device
     * @param status connection status
     */
    protected void updateConnectionStatus(ConnectionStatus status){
        if (connectionListener != null){
            connectionListener.onConnectionStatusChanged(deviceName,status);
        }
    }

    /**
     * @return A list of the supported sensors of the device
     */
    abstract protected List<SensorType> getSupportedSensors();

}
