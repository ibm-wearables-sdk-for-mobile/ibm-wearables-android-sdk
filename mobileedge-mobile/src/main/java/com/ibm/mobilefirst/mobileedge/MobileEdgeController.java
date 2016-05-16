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

import android.content.Context;

import com.ibm.mobilefirst.mobileedge.connectors.DeviceConnector;
import com.ibm.mobilefirst.mobileedge.events.SystemEvents;
import com.ibm.mobilefirst.mobileedge.interfaces.ConnectionStatusListener;
import com.ibm.mobilefirst.mobileedge.interpretation.BaseInterpretation;

/**
 * The main class to control wearables devices
 */
public class MobileEdgeController {

    public Sensors sensors;

    SystemEvents systemEvents;
    ConnectionStatusListener connectionListener;

    public MobileEdgeController() {
        systemEvents = new SystemEvents();
        sensors = new Sensors(systemEvents);
    }

    /**
     * Start connection to a device
     * @param context application context
     * @param connector device connector
     */
    public void connect(Context context, DeviceConnector connector){
        connector.registerForEvents(systemEvents);
        connector.connect(context,connectionListener);
    }

    /**
     * Set listener to get updates about connection status
     * @param connectionListener connection listener
     */
    public void setConnectionListener(ConnectionStatusListener connectionListener){
        this.connectionListener = connectionListener;
    }

    /**
     * Register new interpretation
     * @param interpretation interpretation to register in the system
     */
    public void registerInterpretation(BaseInterpretation interpretation){
        interpretation.registerForEvents(sensors);
    }

    /**
     * Unregister existing interpretation
     * @param interpretation interpretation to unregister from the system
     */
    public void unregisterClassification(BaseInterpretation interpretation){
        interpretation.unregisterEvents(sensors);
    }

    /**
     * Disconnect the connector
     * @param connector device connector
     */
    public void disconnect(DeviceConnector connector){
        connector.disconnect();
    }


    /**
     * Turn on the needed sensors for the classification algorithm
     */
    public void turnClassificationSensorsOn(){
        sensors.accelerometer.on();
        sensors.gyroscope.on();
    }

    /**
     * Turn off the needed sensors for the classification algorithm
     */
    public void turnClassificationSensorsOff(){
        sensors.accelerometer.off();
        sensors.gyroscope.off();
    }
}
