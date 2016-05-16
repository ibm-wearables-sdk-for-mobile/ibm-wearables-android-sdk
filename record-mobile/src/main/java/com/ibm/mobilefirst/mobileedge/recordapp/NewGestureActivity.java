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
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ibm.mobilefirst.mobileedge.utils.GesturesDataUtils;

public class NewGestureActivity extends AppCompatActivity {

    TextView gestureName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_gesture);

        gestureName = (TextView) findViewById(R.id.newGesture);

        final Button button = (Button) findViewById(R.id.startRecordingButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onStartRecording();
            }
        });

        getSupportActionBar().setTitle(R.string.new_gesture_title);
    }

    private void onStartRecording() {
        if (isEmptyGestureName()){
            RecordApplication.showMessage(this,"Insert valid gesture name");
        }

        else if (isGestureExists()){
            RecordApplication.showMessage(this,String.format("Gesture with name %s already exists. choose different name",gestureName.getText()));
        }

        else{
            Intent intent = new Intent(this, RecordingActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("name", gestureName.getText().toString());
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    private boolean isEmptyGestureName(){
        return gestureName.getText().length() == 0;
    }

    private boolean isGestureExists(){
        return GesturesDataUtils.isGestureExists(this, gestureName.getText().toString());
    }
}
