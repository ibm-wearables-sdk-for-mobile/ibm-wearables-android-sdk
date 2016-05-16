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

package com.ibm.mobilefirst.mobileedge.events.messaging;

import java.util.ArrayList;

/**
 * Single Event
 */
public class Event<T> {

    ArrayList<SensorDataListener<T>> handlers = new ArrayList<>();

    public void addHandler(SensorDataListener<T> handler){
        handlers.add(handler);
    }

    public void removeHandler(SensorDataListener<T> handler){
        handlers.remove(handler);
    }

    public void clearAllHandlers(){
        handlers.clear();
    }

    public void trigger(T data){
        for (SensorDataListener<T> handler : handlers){
            handler.onSensorDataChanged(data);
        }
    }
}
