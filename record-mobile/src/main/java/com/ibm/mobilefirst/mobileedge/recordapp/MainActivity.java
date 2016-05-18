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

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ibm.mobilefirst.mobileedge.connectors.ConnectionStatus;
import com.ibm.mobilefirst.mobileedge.interfaces.ConnectionStatusListener;
import com.ibm.mobilefirst.mobileedge.utils.ApplicationPreferences;
import com.ibm.mobilefirst.mobileedge.utils.GesturesDataUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ConnectionStatusListener {

    ListView listView;
    TextView noGesturesTextView;

    ArrayList<String> gesturesNamesList =new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button testingButton = (Button) findViewById(R.id.test_gestures);
        testingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTestGestureClicked();
            }
        });

        noGesturesTextView = (TextView) findViewById(R.id.no_gestures_text);

        setupListView();

        RecordApplication application = (RecordApplication) getApplication();
        application.controller.setConnectionListener(this);
        application.connectToAndroidWear(this);

        getSupportActionBar().setTitle(R.string.main_activity_title);
    }

    private void onTestGestureClicked() {

        if (!gesturesNamesList.isEmpty()){
            Intent intent = new Intent(this, TestingActivity.class);
            startActivity(intent);
        }

        else{
            RecordApplication.showMessage(this,getString(R.string.no_recorded_gestures));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addGesture:
                onNewGestureClicked();
                break;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        //update the list with new files
        gesturesNamesList.clear();
        gesturesNamesList.addAll(GesturesDataUtils.getSavedGestureNames(this));

        if (gesturesNamesList.isEmpty()){
            noGesturesTextView.setVisibility(View.VISIBLE);
        }
        else{
            noGesturesTextView.setVisibility(View.INVISIBLE);
        }

        adapter.notifyDataSetChanged();
    }


    private void setupListView() {
        listView = (ListView) findViewById(R.id.avalibleGestureslistView);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, gesturesNamesList){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setEnabled(GesturesDataUtils.isGestureEnabled(MainActivity.this, gesturesNamesList.get(position)));
                return view;
            }
        };

        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showSelectedGestureDialog(position,view);
                return false;
            }
        });
    }

    private void showSelectedGestureDialog(final int selectedItem, final View item){

        final boolean isGestureEnabled = ApplicationPreferences.isGestureEnabled(this, gesturesNamesList.get(selectedItem));

        CharSequence colors[] = new CharSequence[] {isGestureEnabled ? getString(R.string.disable) : getString(R.string.enable), getString(R.string.delete)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_action);
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == 0){
                    if (isGestureEnabled){
                        disableGesture(selectedItem,item);
                    }

                    else {
                        enableGesture(selectedItem,item);
                    }
                }

                else{
                    deleteGesture(selectedItem);
                }
            }
        });
        builder.show();
    }

    private void deleteGesture(int itemIndex) {
        String gestureNameToDelete = gesturesNamesList.get(itemIndex);

        //remove from the list
        gesturesNamesList.remove(itemIndex);
        adapter.notifyDataSetChanged();
        //ApplicationPreferences.removeGesture(this,gestureNameToDelete);

        GesturesDataUtils.deleteGesture(this,gestureNameToDelete);
    }

    private void enableGesture(int itemIndex, View selectedItem) {
        selectedItem.setEnabled(true);

        ApplicationPreferences.setGestureEnabled(this, gesturesNamesList.get(itemIndex),true);
    }

    private void disableGesture(int itemIndex, View selectedItem) {
        selectedItem.setEnabled(false);

        ApplicationPreferences.setGestureEnabled(this, gesturesNamesList.get(itemIndex),false);
    }

    private void onNewGestureClicked() {
        Intent intent = new Intent(this, NewGestureActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnectionStatusChanged(String deviceName, ConnectionStatus status) {
        Log.e("Connection Status",status.toString());
    }
}
