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

import android.util.Log;

import com.ibm.mobilefirst.mobileedge.Sensors;
import com.ibm.mobilefirst.mobileedge.abstractmodel.AccelerometerData;
import com.ibm.mobilefirst.mobileedge.events.messaging.SensorDataListener;

import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSException;
import org.liquidplayer.webkit.javascriptcore.JSValue;


public class FallDetectionNew extends BaseInterpretation implements SensorDataListener<AccelerometerData> {



    JSContext context = new JSContext();

    public FallDetectionNew(String code) {
        super("Fall Detection");


        try {
            context.evaluateScript(code);

        } catch (JSException e) {
            e.printStackTrace();
        }
        //jsEngine.loadScript();
    }

    @Override
    public void registerForEvents(Sensors sensors) {
        sensors.accelerometer.registerListener(this);
    }

    @Override
    public void unregisterEvents(Sensors sensors) {

    }

    /*
    @Override
    protected void registerForEvents(SystemEvents systemEvents) {
        super.registerForEvents(systemEvents);


        //systemEvents.getSensorEvents(SensorType.Accelerometer).dataEvent.addHandler(this);
    }
    */

    @Override
    public void onSensorDataChanged(AccelerometerData data) {


        try {
            context.evaluateScript(String.format("var cirill = detect(%s)", data.asJSON().toString()));
            JSValue result = context.property("cirill");

            if (result.toJSON().contains("true")){
                Log.e("JSValue", "SUPER!!!!");
            }
            //Log.e("JSValue", result.toJSON());

        } catch (JSException e) {
            e.printStackTrace();
        }

        //Log.e("Fall Detection", jsonObject.toString());
    }
}
