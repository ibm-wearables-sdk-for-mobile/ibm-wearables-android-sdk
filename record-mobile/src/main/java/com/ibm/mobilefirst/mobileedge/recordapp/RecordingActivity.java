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

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ibm.mobilefirst.mobileedge.MobileEdgeController;
import com.ibm.mobilefirst.mobileedge.abstractmodel.AccelerometerData;
import com.ibm.mobilefirst.mobileedge.abstractmodel.GyroscopeData;
import com.ibm.mobilefirst.mobileedge.events.messaging.SensorDataListener;
import com.ibm.mobilefirst.mobileedge.utils.RequestUtils;

import java.util.LinkedList;
import java.util.List;

public class RecordingActivity extends AppCompatActivity {

    final static int COUNTER_TIME = 3200;
    final int MINIMUM_ITERATIONS_NUMBER = 4;

    TextView iterationNumberView;
    TextView statusView;
    TextView counterView;

    ProgressBar progressBar;

    Button continueButton;
    Button endButton;
    Button finishButton;

    int iterationNumber = 1;

    String gestureName = "";
    String id = "";
    String url = "";

    List<AccelerometerData> accelerometerDataList = new LinkedList<>();
    List<GyroscopeData> gyroscopeDataList = new LinkedList<>();

    MobileEdgeController controller;


    SensorDataListener<AccelerometerData> accelerometerListener = new SensorDataListener<AccelerometerData>() {
        @Override
        public void onSensorDataChanged(AccelerometerData data) {
            accelerometerDataList.add(data);
        }
    };

    SensorDataListener<GyroscopeData> gyroscopeListener = new SensorDataListener<GyroscopeData>() {
        @Override
        public void onSensorDataChanged(GyroscopeData data) {
            gyroscopeDataList.add(data);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        getSupportActionBar().setTitle(R.string.recording_title);

        iterationNumberView = (TextView) findViewById(R.id.interationNumber);
        statusView = (TextView) findViewById(R.id.status);
        counterView = (TextView) findViewById(R.id.counter);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        continueButton = (Button) findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onContinueButtonClicked();
            }
        });

        endButton = (Button) findViewById(R.id.endButton);
        endButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onEndButtonClicked();
            }
        });


        finishButton = (Button) findViewById(R.id.finishButton);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFinishButtonClicked();
            }
        });

        continueButton.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        finishButton.setVisibility(View.INVISIBLE);

        gestureName = getIntent().getExtras().getString("name");
    }


    @Override
    protected void onStart() {
        super.onStart();

        initController();
        startNextIteration();
    }

    @Override
    protected void onPause() {
        super.onPause();

        controller.turnClassificationSensorsOff();
    }

    @Override
    protected void onStop() {
        super.onStop();

        controller.sensors.accelerometer.unregisterListener(accelerometerListener);
        controller.sensors.gyroscope.unregisterListener(gyroscopeListener);
    }

    private void initController() {
        RecordApplication application = (RecordApplication)getApplication();
        controller = application.controller;

        controller.sensors.accelerometer.registerListener(accelerometerListener);
        controller.sensors.gyroscope.registerListener(gyroscopeListener);
    }

    private void startNextIteration(){
        showEndButton();
        disableButtons();

        counterView.setVisibility(View.VISIBLE);
        iterationNumberView.setText(String.format(getResources().getString(R.string.iteration_number), iterationNumber));
        setStatus(R.string.get_ready_status);

        //update the text counter
        new CountDownTimer(COUNTER_TIME, 1000) {
            public void onTick(long millisUntilFinished) {
                setCounter(millisUntilFinished / 1000);
            }

            public void onFinish() {
            }
        }.start();

        //the recording will start one second before the count is finished
        new CountDownTimer(COUNTER_TIME - 800, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                startRecording();
            }
        }.start();


    }

    private void startRecording() {
        controller.turnClassificationSensorsOn();
        setStatus(R.string.recording_status);
        progressBar.setVisibility(View.VISIBLE);
        counterView.setVisibility(View.INVISIBLE);
        showEndButton();


    }

    private void setStatus(int resId){
        statusView.setText(getString(resId));
    }

    private void setCounter(long second){
        counterView.setText(String.format(getString(R.string.counter),second));
    }

    private void onContinueButtonClicked() {
        startNextIteration();
    }

    private void onEndButtonClicked() {

        controller.turnClassificationSensorsOff();
        setStatus(R.string.validating_status);

        disableButtons();

        RequestUtils.sendTrainRequest(gestureName, id, accelerometerDataList, gyroscopeDataList, new RequestUtils.RequestResult() {
            @Override
            public void onSuccess(String uuid, String jsUrl) {
                id = uuid;
                url = jsUrl;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleIterationSucceeded();
                    }
                });
            }

            @Override
            public void onFailure(final String error) {
                Log.e("onError",error);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleIterationFailed(error);
                    }
                });
            }
        });
    }

    private void onFinishButtonClicked() {
        Intent intent = new Intent(this, ResultActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("id",id);
        bundle.putString("url",url);
        bundle.putString("name",gestureName);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    private void handleIterationFailed(String error){

        progressBar.setVisibility(View.INVISIBLE);
        setStatus(R.string.iteration_failed_status);
        RecordApplication.showMessage(this,getString(R.string.iteration_failed_error) + error);

        showContinueButton();
    }

    private void handleIterationSucceeded(){

        progressBar.setVisibility(View.INVISIBLE);
        setStatus(R.string.iteration_accepted_status);
        showContinueButton();
        iterationNumber++;

        if (iterationNumber > MINIMUM_ITERATIONS_NUMBER){
            finishButton.setVisibility(View.VISIBLE);
        }
    }

    private void cleanRecordedData(){
        accelerometerDataList.clear();
        gyroscopeDataList.clear();
    }



    private void showEndButton(){
        enableButtons();
        continueButton.setVisibility(View.INVISIBLE);
        endButton.setVisibility(View.VISIBLE);
    }

    private void showContinueButton(){
        enableButtons();
        cleanRecordedData();
        continueButton.setVisibility(View.VISIBLE);
        endButton.setVisibility(View.INVISIBLE);
    }

    private void disableButtons(){
        continueButton.setEnabled(false);
        endButton.setEnabled(false);
    }

    private void enableButtons(){
        continueButton.setEnabled(true);
        endButton.setEnabled(true);
    }
}
