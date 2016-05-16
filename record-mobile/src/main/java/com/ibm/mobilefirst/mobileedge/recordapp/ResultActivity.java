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

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ibm.mobilefirst.mobileedge.utils.DownloadUtils;
import com.ibm.mobilefirst.mobileedge.utils.GesturesDataUtils;

public class ResultActivity extends AppCompatActivity {

    String id;
    String url;
    String gestureName;

    ProgressDialog progressDialog;
    TextView urlTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        urlTextView = (TextView) findViewById(R.id.urlTextView);

        progressDialog = ProgressDialog.show(this, "Please wait ...", "Generating Algorithm", false);
        progressDialog.setCanceledOnTouchOutside(false);

        final Button doneButton = (Button) findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDoneButtonClicked();
            }
        });

        Bundle extras = getIntent().getExtras();

        id = extras.getString("id");
        url = extras.getString("url");
        gestureName = extras.getString("name");

        getSupportActionBar().setTitle(R.string.result_title);

        downloadGestureFile(url,gestureName);
    }

    private void onDoneButtonClicked() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void downloadGestureFile(String url, String gestureName) {


        GesturesDataUtils.downloadGesture(this, url, gestureName, new DownloadUtils.DownloadResult() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onGestureDownloaded();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onFailedToDownloadGesture();
                    }
                });
            }
        });
    }

    private void onGestureDownloaded() {
        progressDialog.dismiss();
        urlTextView.setText(url.replace("https://",""));
    }

    private void onFailedToDownloadGesture() {

    }


}
