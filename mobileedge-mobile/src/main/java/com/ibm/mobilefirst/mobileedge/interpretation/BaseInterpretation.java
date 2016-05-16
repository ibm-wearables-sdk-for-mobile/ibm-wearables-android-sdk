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

package com.ibm.mobilefirst.mobileedge.interpretation;

import com.ibm.mobilefirst.mobileedge.Sensors;

/**
 * Base class for interpretation
 */
public abstract class BaseInterpretation {

    InterpretationListener listener;
    String name;

    public BaseInterpretation(String name){
        this.name = name;
    }

    /**
     * Set listener that will be called once the interpretation detected
     * @param listener interpretationListener
     */
    public void setListener(InterpretationListener listener){
        this.listener = listener;
    }

    /**
     * Clear the interpretation listener
     */
    public void clearListener(){
        this.listener = null;
    }

    /**
     * Register the interpretation for needed sensors
     * @param sensors system sensors
     */
    public abstract void registerForEvents(Sensors sensors);

    /**
     * Unregister the interpretation from using sensors
     * @param sensors system sensors
     */
    public abstract void unregisterEvents(Sensors sensors);


    /**
     * notify the interpretation detection
     * @param additionalInfo extra data that can be sent with the notification
     */
    protected void notifyResult(Object additionalInfo){
        if (listener != null){
            listener.onInterpretationDetected(name,additionalInfo);
        }
    }
}