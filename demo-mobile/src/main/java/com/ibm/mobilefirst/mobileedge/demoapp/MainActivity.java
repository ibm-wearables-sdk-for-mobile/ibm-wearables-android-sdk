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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.ibm.mobilefirst.mobileedge.MobileEdgeController;
import com.ibm.mobilefirst.mobileedge.abstractmodel.AccelerometerData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.AmbientLightData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.BarometerData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.CaloriesData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.GsrData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.GyroscopeData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.HeartRateData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.HumidityData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.MagnetometerData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.PedometerData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.SkinTemperatureData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.TemperatureData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.UVData;
import com.ibm.mobilefirst.mobileedge.connectors.AndroidWear;
import com.ibm.mobilefirst.mobileedge.connectors.BLE.LifeBeam;
import com.ibm.mobilefirst.mobileedge.connectors.BLE.SensorTag;
import com.ibm.mobilefirst.mobileedge.connectors.ConnectionStatus;
import com.ibm.mobilefirst.mobileedge.connectors.DeviceConnector;
import com.ibm.mobilefirst.mobileedge.connectors.MicrosoftBand2;
import com.ibm.mobilefirst.mobileedge.events.messaging.SensorDataListener;
import com.ibm.mobilefirst.mobileedge.interfaces.ConnectionStatusListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements ConnectionStatusListener {

    final Activity activity = this;
    MobileEdgeController controller = new MobileEdgeController();
    Button disconnectButton;
    Button connectBLEButton;
    Switch sensorsSwitch;
    LinearLayout dataLayout;
    Spinner devicesSpinner;
    String selectedDevice ="";
    DeviceConnector connector = null;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectBLEButton = (Button) findViewById(R.id.connectBleButton);
        disconnectButton = (Button) findViewById(R.id.disconnectButton);
        disconnectButton.setEnabled(false);

        devicesSpinner = (Spinner) findViewById(R.id.spinner);
        List<String> categories = new ArrayList<String>();
        categories.add("Please select a device");
        categories.add("Android Wear");
        categories.add("TI Sensor Tag");
        categories.add("LifeBeam Hat");
        categories.add("Microsoft Band");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        devicesSpinner.setAdapter(dataAdapter);
        devicesSpinner.setPrompt("Choose a device");
        devicesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String item = parentView.getItemAtPosition(position).toString();
                Toast.makeText(parentView.getContext(), item, Toast.LENGTH_SHORT).show();
                selectedDevice = item;
                switch(selectedDevice){
                    case("TI Sensor Tag"): {
                        final SensorTag sensorTag = new SensorTag(activity);
                        connector = sensorTag;
                        connectBLEButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestLocationPerissions();
                                controller.connect(MainActivity.this, sensorTag);

                            }
                        });
                        break;
                    }
                    case("LifeBeam Hat"): {
                        final LifeBeam lifeBeam = new LifeBeam(activity);
                        connector = lifeBeam;
                        connectBLEButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestLocationPerissions();
                                controller.connect(MainActivity.this, lifeBeam);
                            }
                        });
                        break;
                    }
                    case("Android Wear"): {
                        final AndroidWear androidWear = new AndroidWear();
                        connector = androidWear;
                        connectBLEButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestLocationPerissions();

                                controller.connect(MainActivity.this, androidWear);
                            }
                        });
                    }

                    break;
                    case("Microsoft Band"): {
                        final MicrosoftBand2 microsoftBand = new MicrosoftBand2();
                        connector = microsoftBand;
                        connectBLEButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestLocationPerissions();
                                controller.connect(MainActivity.this, microsoftBand);
                            }
                        });
                        break;
                    }

                    default:
                        //Toast.makeText(MainActivity.this, "No Device Chosen", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });


        sensorsSwitch = (Switch) findViewById(R.id.sensorsSwitch);
        sensorsSwitch.setEnabled(false);
        sensorsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    enableAll();
                } else {
                    disableAll();
                }
            }
        });

        dataLayout = (LinearLayout) findViewById(R.id.dataLayout);
        dataLayout.setVisibility(View.INVISIBLE);

        //set connection listener to get notifications about connection status
        controller.setConnectionListener(this);


        //register accelerometer listener, will be called each time accelerometer data is changed
        registerAccelerometer();
        registerGyro();
        registerAmbientLight();
        registerBarometer();
        registerCalories();
        registerGsr();
        registerHeartRate();
        registerHumidity();
        registerMagnetometer();
        registerPedometer();
        registerSkinTemperature();
        registerTemperature();
        registerUV();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onConnectionStatusChanged(final String deviceName, ConnectionStatus status) {
        if (status == ConnectionStatus.Connected) {
            Toast.makeText(getApplicationContext(), "Connected to " + selectedDevice, Toast.LENGTH_SHORT).show();

            connectBLEButton.setEnabled(false);
            sensorsSwitch.setEnabled(true);
            disconnectButton.setEnabled(true);
            disconnectButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    sensorsSwitch.setChecked(false);
                    controller.disconnect(connector);
                }
            });

        } else if (status == ConnectionStatus.Disconnected) {
            Toast.makeText(getApplicationContext(), "Disconnected from " + selectedDevice, Toast.LENGTH_SHORT).show();
            connectBLEButton.setEnabled(true);
            sensorsSwitch.setEnabled(false);
            sensorsSwitch.setChecked(false);
            disconnectButton.setEnabled(false);
        }
    }

    void registerAccelerometer() {
        final TextView textView = addSensorTextView("accelerometer");
        final SensorDataListener sensorDataListener = new SensorDataListener<AccelerometerData>() {
            @Override
            public void onSensorDataChanged(final AccelerometerData data) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String dataString = String.format("accelerometer x=%.3f y=%.3f z=%.3f", data.x, data.y, data.z);
                        textView.setText(dataString);
                    }
                });
            }
        };
        controller.sensors.accelerometer.registerListener(sensorDataListener);
    }

    void registerGyro() {
        final TextView textView = addSensorTextView("gyro");
        final SensorDataListener sensorDataListener = new SensorDataListener<GyroscopeData>() {
            @Override
            public void onSensorDataChanged(final GyroscopeData data) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String dataString = String.format("gyro x=%.3f y=%.3f z=%.3f", data.x, data.y, data.z);
                        textView.setText(dataString);
                    }
                });
            }
        };
        controller.sensors.gyroscope.registerListener(sensorDataListener);
    }

    void registerAmbientLight() {
        final TextView textView = addSensorTextView("brightness");
        final SensorDataListener sensorDataListener = new SensorDataListener<AmbientLightData>() {
            @Override
            public void onSensorDataChanged(final AmbientLightData data) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String dataString = String.format("brightness = %d", data.brightness);
                        textView.setText(dataString);
                    }
                });
            }
        };
        controller.sensors.ambientLight.registerListener(sensorDataListener);
    }

    void registerBarometer() {
        final TextView textView = addSensorTextView("barometer");
        final SensorDataListener sensorDataListener = new SensorDataListener<BarometerData>() {
            @Override
            public void onSensorDataChanged(final BarometerData data) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String dataString = String.format("pressure = %.2f, temp = %.2f", data.airPressure, data.temperature);
                        textView.setText(dataString);
                    }
                });
            }
        };
        controller.sensors.barometer.registerListener(sensorDataListener);
    }

    void registerCalories() {
        final TextView textView = addSensorTextView("calories");
        final SensorDataListener sensorDataListener = new SensorDataListener<CaloriesData>() {
            @Override
            public void onSensorDataChanged(final CaloriesData data) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String dataString = String.format("calories = %d", data.calories);
                        textView.setText(dataString);
                    }
                });
            }
        };
        controller.sensors.calories.registerListener(sensorDataListener);
    }

    void registerGsr() {
        final TextView textView = addSensorTextView("gsr");
        final SensorDataListener sensorDataListener = new SensorDataListener<GsrData>() {
            @Override
            public void onSensorDataChanged(final GsrData data) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String dataString = String.format("gsr = %d", data.resistance);
                        textView.setText(dataString);
                    }
                });
            }
        };
        controller.sensors.gsr.registerListener(sensorDataListener);
    }

    void registerHeartRate() {
        final TextView textView = addSensorTextView("heartrate");
        final SensorDataListener sensorDataListener = new SensorDataListener<HeartRateData>() {
            @Override
            public void onSensorDataChanged(final HeartRateData data) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String dataString = String.format("heartrate = %.5f", data.heartRate);
                        textView.setText(dataString);
                    }
                });
            }
        };
        controller.sensors.heartRate.registerListener(sensorDataListener);
    }

    void registerHumidity() {
        final TextView textView = addSensorTextView("humidity");
        final SensorDataListener sensorDataListener = new SensorDataListener<HumidityData>() {
            @Override
            public void onSensorDataChanged(final HumidityData data) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String dataString = String.format("humidity = %.2f", data.humidity);
                        textView.setText(dataString);
                    }
                });
            }
        };
        controller.sensors.humidity.registerListener(sensorDataListener);
    }

    void registerMagnetometer() {
        final TextView textView = addSensorTextView("magnetometer");
        final SensorDataListener sensorDataListener = new SensorDataListener<MagnetometerData>() {
            @Override
            public void onSensorDataChanged(final MagnetometerData data) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String dataString = String.format("magnetometer x = %.3f y = %.3f z = %.3f", data.x, data.y, data.z);
                        textView.setText(dataString);
                    }
                });
            }
        };
        controller.sensors.magnetometer.registerListener(sensorDataListener);
    }

    void registerPedometer() {
        final TextView textView = addSensorTextView("pedometer");
        final SensorDataListener sensorDataListener = new SensorDataListener<PedometerData>() {
            @Override
            public void onSensorDataChanged(final PedometerData data) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String dataString = String.format("pedometer = %d", data.steps);
                        textView.setText(dataString);
                    }
                });
            }
        };
        controller.sensors.pedometer.registerListener(sensorDataListener);
    }

    void registerSkinTemperature() {
        final TextView textView = addSensorTextView("skin temperature");
        final SensorDataListener sensorDataListener = new SensorDataListener<SkinTemperatureData>() {
            @Override
            public void onSensorDataChanged(final SkinTemperatureData data) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String dataString = String.format("skin temperature = %.5f", data.temperature);
                        textView.setText(dataString);
                    }
                });
            }
        };
        controller.sensors.skinTemperature.registerListener(sensorDataListener);
    }

    void registerTemperature() {
        final TextView textView = addSensorTextView("temperature");
        final SensorDataListener sensorDataListener = new SensorDataListener<TemperatureData>() {
            @Override
            public void onSensorDataChanged(final TemperatureData data) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String dataString = String.format("temperature = %.2f", data.temperature);
                        textView.setText(dataString);
                    }
                });
            }
        };
        controller.sensors.temperature.registerListener(sensorDataListener);
    }

    void registerUV() {
        final TextView textView = addSensorTextView("uv level");
        final SensorDataListener sensorDataListener = new SensorDataListener<UVData>() {
            @Override
            public void onSensorDataChanged(final UVData data) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String dataString = String.format("uv level = %d", data.indexLevel);
                        textView.setText(dataString);
                    }
                });
            }
        };
        controller.sensors.uv.registerListener(sensorDataListener);
    }

    @Override
    protected void onPause() {
        sensorsSwitch.callOnClick();
        super.onPause();
    }

    TextView addSensorTextView(String initText) {
        final TextView textView = new TextView(this);
        if (initText != null) {
            textView.setText(initText);
        }
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        dataLayout.addView(textView);
        return textView;
    }

    private void enableAll() {
        controller.sensors.accelerometer.on();
        controller.sensors.gyroscope.on();
        controller.sensors.ambientLight.on();
        controller.sensors.barometer.on();
        controller.sensors.calories.on();
        controller.sensors.gsr.on();
        controller.sensors.heartRate.on();
        controller.sensors.humidity.on();
        controller.sensors.magnetometer.on();
        controller.sensors.pedometer.on();
        controller.sensors.skinTemperature.on();
        controller.sensors.temperature.on();
        controller.sensors.uv.on();
        dataLayout.setVisibility(View.VISIBLE);
    }

    private void disableAll() {
        controller.sensors.accelerometer.off();
        controller.sensors.gyroscope.off();
        controller.sensors.ambientLight.off();
        controller.sensors.barometer.off();
        controller.sensors.calories.off();
        controller.sensors.gsr.off();
        controller.sensors.heartRate.off();
        controller.sensors.humidity.off();
        controller.sensors.magnetometer.off();
        controller.sensors.pedometer.off();
        controller.sensors.skinTemperature.off();
        controller.sensors.temperature.off();
        controller.sensors.uv.off();
        dataLayout.setVisibility(View.INVISIBLE);
    }

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private void requestLocationPerissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                        }
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();



        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.ibm.mobilefirst.mobileedge.demoapp/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);

    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.ibm.mobilefirst.mobileedge.demoapp/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}