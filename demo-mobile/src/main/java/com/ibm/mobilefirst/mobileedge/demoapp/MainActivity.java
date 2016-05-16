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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.mobilefirst.mobileedge.MobileEdgeController;
import com.ibm.mobilefirst.mobileedge.abstractmodel.AccelerometerData;
import com.ibm.mobilefirst.mobileedge.connectors.AndroidWear;
import com.ibm.mobilefirst.mobileedge.connectors.ConnectionStatus;
import com.ibm.mobilefirst.mobileedge.events.messaging.SensorDataListener;
import com.ibm.mobilefirst.mobileedge.interfaces.ConnectionStatusListener;

public class MainActivity extends Activity implements ConnectionStatusListener {

    MobileEdgeController controller;
    Button connectButton;
    TextView accelerometerData;
    Switch accelerometerSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton = (Button) findViewById(R.id.connectButton);
        accelerometerData = (TextView) findViewById(R.id.accelerometerText);
        accelerometerData.setVisibility(View.INVISIBLE);

        findViewById(R.id.connectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.connect(MainActivity.this, new AndroidWear());
            }
        });

        accelerometerSwitch = (Switch)findViewById(R.id.accelerometerSwitch);
        accelerometerSwitch.setEnabled(false);
        accelerometerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    controller.sensors.accelerometer.on();
                    accelerometerData.setVisibility(View.VISIBLE);
                }else{
                    accelerometerData.setVisibility(View.INVISIBLE);
                    controller.sensors.accelerometer.off();
                }
            }
        });

        controller = new MobileEdgeController();
        controller.setConnectionListener(this);

        //will be called each time accelerometer data is changed
        controller.sensors.accelerometer.registerListener(new SensorDataListener<AccelerometerData>() {
            @Override
            public void onSensorDataChanged(AccelerometerData data) {
                onAccelerometerDataChanged(data);
            }
        });
    }

    @Override
    public void onConnectionStatusChanged(String deviceName, ConnectionStatus status) {

        if (status == ConnectionStatus.Connected){
            Toast.makeText(getApplicationContext(), R.string.connected, Toast.LENGTH_SHORT).show();
            connectButton.setEnabled(false);
            accelerometerSwitch.setEnabled(true);
        }

        else if (status == ConnectionStatus.Disconnected){
            Toast.makeText(getApplicationContext(), R.string.disconnected, Toast.LENGTH_SHORT).show();
            connectButton.setEnabled(true);
            accelerometerSwitch.setEnabled(false);
            accelerometerSwitch.setChecked(false);
        }
    }

    /**
     * Update the UI with the new data
     * @param data accelerometer data
     */
    private void onAccelerometerDataChanged(AccelerometerData data){
        String dataString = String.format("x=%.5f y=%.5f z=%.5f",data.x,data.y,data.z);
        accelerometerData.setText(dataString);
    }


    @Override
    protected void onPause() {
        super.onPause();
        controller.sensors.accelerometer.off();
    }
}
