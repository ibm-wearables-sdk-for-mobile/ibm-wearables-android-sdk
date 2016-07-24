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

package com.ibm.mobilefirst.mobileedge.recordapp;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ibm.mobilefirst.mobileedge.MobileEdgeController;
import com.ibm.mobilefirst.mobileedge.abstractmodel.AccelerometerData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.HeartRateData;
import com.ibm.mobilefirst.mobileedge.events.messaging.SensorDataListener;
import com.ibm.mobilefirst.mobileedge.interpretation.Classification;
import com.ibm.mobilefirst.mobileedge.interpretation.InterpretationListener;
import com.ibm.mobilefirst.mobileedge.utils.GesturesDataUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public class TestingActivity extends AppCompatActivity {

    MobileEdgeController controller;
    Classification classification;

    TextView detectedGesture;
    TextView score;

    View detectedLayout;
    View sensingLayout;

    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        detectedGesture = (TextView) findViewById(R.id.detectedGesture);
        score = (TextView) findViewById(R.id.scoring);

        detectedLayout = findViewById(R.id.detectedLayout);
        sensingLayout = findViewById(R.id.sensingLayout);

        getSupportActionBar().setTitle(R.string.testing_title);

        RecordApplication application = (RecordApplication) getApplication();
        controller = application.controller;


        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //init the classification
        classification = new Classification(this);

        loadAllGestures();

        classification.setListener(new InterpretationListener() {
            @Override
            public void onInterpretationDetected(String name, Object additionalInfo) {

                JSONObject jsonResult = (JSONObject) additionalInfo;
                handleResult(jsonResult);
            }
        });

        controller.registerInterpretation(classification);

        controller.sensors.accelerometer.registerListener(new SensorDataListener<AccelerometerData>() {
            @Override
            public void onSensorDataChanged(AccelerometerData data) {
                //temp(data);
            }
        });

        controller.sensors.heartRate.registerListener(new SensorDataListener<HeartRateData>() {
            @Override
            public void onSensorDataChanged(HeartRateData data) {
                System.out.println("HeartRate = " + data.hearRate);
            }
        });

        controller.turnClassificationSensorsOn();
    }


    @Override
    protected void onPause() {
        super.onPause();
        controller.turnClassificationSensorsOff();
        controller.unregisterClassification(classification);
    }

    private void handleResult(JSONObject json) {
        detectedLayout.setVisibility(View.VISIBLE);
        sensingLayout.setVisibility(View.INVISIBLE);

        String recognizedGesture = json.optString("recognized");
        detectedGesture.setText(recognizedGesture);
        score.setText(String.format(getString(R.string.gesture_score),json.optString("score")));
        sayText(recognizedGesture);

        new CountDownTimer(1500, 1500) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                detectedLayout.setVisibility(View.INVISIBLE);
                sensingLayout.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void sayText(String text){
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void loadAllGestures() {
        ArrayList<InputStream> savedGesturesAsInputStream = GesturesDataUtils.getEnabledGesturesAsInputStream(this);

        for (InputStream is : savedGesturesAsInputStream){
            classification.loadGesture(is);
            Log.v("TestingActivity","Loaded gesture == " + is.toString());

//            try {
//                is.close();              //No need to close it, the BufferedReader will close it by itself.
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }
}
