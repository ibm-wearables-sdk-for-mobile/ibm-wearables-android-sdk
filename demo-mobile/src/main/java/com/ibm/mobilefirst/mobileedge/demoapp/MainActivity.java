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

package com.ibm.mobilefirst.mobileedge.demoapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ibm.mobilefirst.mobileedge.MobileEdgeController;
import com.ibm.mobilefirst.mobileedge.abstractmodel.AccelerometerData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.GyroscopeData;
import com.ibm.mobilefirst.mobileedge.connectors.AndroidWear;
import com.ibm.mobilefirst.mobileedge.connectors.ConnectionStatus;
import com.ibm.mobilefirst.mobileedge.events.messaging.SensorDataListener;
import com.ibm.mobilefirst.mobileedge.interfaces.ConnectionStatusListener;


public class MainActivity extends AppCompatActivity implements ConnectionStatusListener {

    MobileEdgeController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        controller = new MobileEdgeController();

        controller.setConnectionListener(this);
        controller.connect(this, new AndroidWear());

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.turnClassificationSensorsOn();
            }
        });
    }

    @Override
    public void onConnectionStatusChanged(String deviceName, ConnectionStatus status) {
        System.out.println("Connect = " + status);

        controller.sensors.accelerometer.registerListener(new SensorDataListener<AccelerometerData>() {
            @Override
            public void onSensorDataChanged(AccelerometerData data) {
                System.out.println("Accelerometer = " + data.asJSON());
            }
        });

        controller.sensors.gyroscope.registerListener(new SensorDataListener<GyroscopeData>() {
            @Override
            public void onSensorDataChanged(GyroscopeData data) {
                System.out.println("Gyroscope = " + data.asJSON());
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        controller.turnClassificationSensorsOff();
    }
}
