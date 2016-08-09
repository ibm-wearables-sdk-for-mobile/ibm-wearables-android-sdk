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

package com.ibm.mobilefirst.mobileedge.abstractmodel;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Holds the accelerometer data
 */
public class AccelerometerData extends BaseSensorData {

    public float x;
    public float y;
    public float z;

    public AccelerometerData(){
        this(0.0f,0.0f,0.0f);
    }

    public AccelerometerData(float x, float y, float z){
        super();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public JSONObject asJSON() {
        JSONObject json = super.asJSON();

        try {
            JSONObject data = new JSONObject();
            data.put("x",x);
            data.put("y",y);
            data.put("z",z);
            json.put("accelerometer",data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }
}
