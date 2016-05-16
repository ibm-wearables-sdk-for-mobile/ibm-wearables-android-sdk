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

import com.ibm.mobilefirst.mobileedge.events.SensorEvents;
import com.ibm.mobilefirst.mobileedge.events.messaging.Event;
import com.ibm.mobilefirst.mobileedge.events.messaging.SensorDataListener;

/**
 * Controls single sensor
 */
public class SensorHolder<T> {

    SensorEvents sensorEvents;

    public SensorHolder(SensorEvents sensorEvents) {
        this.sensorEvents = sensorEvents;
    }

    public void registerListener(SensorDataListener<T> dataListener){
        Event<T> event = (Event<T>) sensorEvents.dataEvent;
        event.addHandler(dataListener);
    }

    public void unregisterListener(SensorDataListener<T> dataListener){
        Event<T> event = (Event<T>) sensorEvents.dataEvent;
        event.removeHandler(dataListener);
    }
    
    public void clearListeners(){
        sensorEvents.dataEvent.clearAllHandlers();
    }

    public void on(){
        sensorEvents.turnOnCommand.trigger();
    }

    public void off(){
        sensorEvents.turnOffCommand.trigger();
    }
}
