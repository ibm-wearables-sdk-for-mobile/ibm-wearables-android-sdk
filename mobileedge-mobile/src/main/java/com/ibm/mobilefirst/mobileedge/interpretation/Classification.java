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

import android.content.Context;
import android.content.res.AssetManager;

import com.ibm.mobilefirst.mobileedge.Sensors;
import com.ibm.mobilefirst.mobileedge.abstractmodel.AccelerometerData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.GyroscopeData;
import com.ibm.mobilefirst.mobileedge.events.messaging.SensorDataListener;
import com.ibm.mobilefirst.mobileedge.js.JSEngine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 * Classification algorithm that used to detect gestures
 */
public class Classification extends BaseInterpretation{

    //the minimum needed data size for sending to classification
    final int DATA_SIZE = 3;

    List<JSONArray> accelerometerData = new LinkedList<>();
    List<JSONArray> gyroscopeData = new LinkedList<>();

    /**
     * listen for accelerometer data
     */
    SensorDataListener<AccelerometerData> accelerometerListener = new SensorDataListener<AccelerometerData>(){
        @Override
        public void onSensorDataChanged(AccelerometerData data) {
            accelerometerData.add(createArrayJSONFromData(data.x,data.y,data.z));
            executeClassification();
        }
    };

    /**
     * listen for gyroscope data
     */
    SensorDataListener<GyroscopeData> gyroscopeListener = new SensorDataListener<GyroscopeData>() {
        @Override
        public void onSensorDataChanged(GyroscopeData data) {
            gyroscopeData.add(createArrayJSONFromData(data.x,data.y,data.z));
            executeClassification();
        }
    };


    public Classification(Context context) {
        super("Classification");

        //load the common classification
        evaluateScriptFromFile(context, "commonClassifier.js");
    }

    /**
     * Load gesture file from the assets folder
     * @param context application context
     * @param fileName name of the gesture
     */
    public void loadGesture(Context context, String fileName){
        evaluateScriptFromFile(context,fileName);
    }

    /**
     * Load gesture from InputStream
     * @param gestureInputStream gesture as input stream
     */
    public void loadGesture(InputStream gestureInputStream){
        JSEngine.getInstance().evaluateScript(getStringFromInputStream(gestureInputStream));
    }

    private void evaluateScriptFromFile(Context context, String fileName){
        JSEngine.getInstance().evaluateScript(getFileAsString(context,fileName));
    }

    private String getFileAsString(Context context, String filename) {
        AssetManager assetManager = context.getAssets();
        try {
            InputStream inputStream = assetManager.open(filename);
            return getStringFromInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }

    @Override
    public void registerForEvents(Sensors sensors) {
        sensors.accelerometer.registerListener(accelerometerListener);
        sensors.gyroscope.registerListener(gyroscopeListener);
    }

    @Override
    public void unregisterEvents(Sensors sensors) {
        sensors.accelerometer.unregisterListener(accelerometerListener);
        sensors.gyroscope.unregisterListener(gyroscopeListener);
    }

    private JSONArray createArrayJSONFromData(float x, float y, float z){
        JSONArray array = new JSONArray();
        try {
            array.put(x);
            array.put(y);
            array.put(z);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return array;
    }

    private void executeClassification() {

        if (accelerometerData.size() >= DATA_SIZE && gyroscopeData.size() >= DATA_SIZE) {

            JSONArray accelerometerArray = getNextDataAsJSONArray(accelerometerData);
            JSONArray gyroscopeArray = getNextDataAsJSONArray(gyroscopeData);

            JSONObject params = new JSONObject();

            try {
                params.put("accelerometer",accelerometerArray);
                params.put("gyroscope",gyroscopeArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject result = JSEngine.getInstance().executeFunction("detectGesture", params);

            if (result.has("detected") && result.optBoolean("detected")){
                //notify the detected gesture
                notifyResult(result.optJSONObject("additionalInfo"));
            }
        }
    }

    /**
     * Get the next data for sending, and remove it from the list
     * @param data list of the next data
     * @return json result
     */
    private JSONArray getNextDataAsJSONArray(List<JSONArray> data) {
        JSONArray result = new JSONArray();

        for (int i = 0; i< DATA_SIZE ; i++ ){
            JSONArray array = data.get(0);
            result.put(array);
            data.remove(0);
        }
        return result;
    }
}
