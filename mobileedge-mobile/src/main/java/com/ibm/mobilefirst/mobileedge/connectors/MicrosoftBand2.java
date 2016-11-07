///*
// *    © Copyright 2016 IBM Corp.
// *
// *    Licensed under the Apache License, Version 2.0 (the "License");
// *    you may not use this file except in compliance with the License.
// *    You may obtain a copy of the License at
// *
// *        http://www.apache.org/licenses/LICENSE-2.0
// *
// *    Unless required by applicable law or agreed to in writing, software
// *    distributed under the License is distributed on an "AS IS" BASIS,
// *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *    See the License for the specific language governing permissions and
// *    limitations under the License.
// */
//
//package com.ibm.mobilefirst.mobileedge.connectors;
//
//import android.app.Activity;
//import android.content.Context;
//import android.util.Log;
//import com.ibm.mobilefirst.mobileedge.SensorType;
//import com.ibm.mobilefirst.mobileedge.abstractmodel.AccelerometerData;
//import com.ibm.mobilefirst.mobileedge.abstractmodel.AmbientLightData;
//import com.ibm.mobilefirst.mobileedge.abstractmodel.BarometerData;
//import com.ibm.mobilefirst.mobileedge.abstractmodel.CaloriesData;
//import com.ibm.mobilefirst.mobileedge.abstractmodel.GsrData;
//import com.ibm.mobilefirst.mobileedge.abstractmodel.GyroscopeData;
//import com.ibm.mobilefirst.mobileedge.abstractmodel.HeartRateData;
//import com.ibm.mobilefirst.mobileedge.abstractmodel.PedometerData;
//import com.ibm.mobilefirst.mobileedge.abstractmodel.SkinTemperatureData;
//import com.ibm.mobilefirst.mobileedge.abstractmodel.UVData;
//import com.ibm.mobilefirst.mobileedge.events.SensorEvents;
//import com.ibm.mobilefirst.mobileedge.events.SystemEvents;
//import com.ibm.mobilefirst.mobileedge.events.messaging.SensorDataListener;
//import com.ibm.mobilefirst.mobileedge.interfaces.ConnectionStatusListener;
//
//import com.microsoft.band.BandClient;
//import com.microsoft.band.BandClientManager;
//import com.microsoft.band.BandInfo;
//import com.microsoft.band.UserConsent;
//import com.microsoft.band.sensors.BandAccelerometerEvent;
//import com.microsoft.band.sensors.BandAccelerometerEventListener;
//import com.microsoft.band.sensors.BandAmbientLightEvent;
//import com.microsoft.band.sensors.BandAmbientLightEventListener;
//import com.microsoft.band.sensors.BandBarometerEvent;
//import com.microsoft.band.sensors.BandBarometerEventListener;
//import com.microsoft.band.sensors.BandCaloriesEvent;
//import com.microsoft.band.sensors.BandCaloriesEventListener;
//import com.microsoft.band.sensors.BandGsrEvent;
//import com.microsoft.band.sensors.BandGsrEventListener;
//import com.microsoft.band.sensors.BandGyroscopeEvent;
//import com.microsoft.band.sensors.BandGyroscopeEventListener;
//import com.microsoft.band.sensors.BandHeartRateEvent;
//import com.microsoft.band.sensors.BandHeartRateEventListener;
//import com.microsoft.band.sensors.BandPedometerEvent;
//import com.microsoft.band.sensors.BandPedometerEventListener;
//import com.microsoft.band.sensors.BandSkinTemperatureEvent;
//import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
//import com.microsoft.band.sensors.BandUVEvent;
//import com.microsoft.band.sensors.BandUVEventListener;
//import com.microsoft.band.sensors.HeartRateConsentListener;
//import com.microsoft.band.sensors.SampleRate;
//import com.microsoft.band.sensors.UVIndexLevel;
//
//import java.util.Arrays;
//import java.util.List;
//
///**
// * Microsoft Band 2 connector
// */
//public class MicrosoftBand2 extends DeviceConnector {
//    final private String logTagString = "MicrosoftBand2Connector";
//    private BandClient client = null;
//
//    private SensorEvents accelerometerSensorEvents;
//    private SensorEvents gyroscopeSensorEvents;
//    private SensorEvents heartRateSensorEvents;
//    private SensorEvents ambientLightSensorEvents;
//    private SensorEvents caloriesSensorEvents;
//    private SensorEvents gsrSensorEvents;
//    private SensorEvents pedometerSensorEvents;
//    private SensorEvents skinTemperatureSensorEvents;
//    private SensorEvents barometerSensorEvents;
//    private SensorEvents uvSensorEvents;
//
//    public MicrosoftBand2(){
//        super("Microsoft Band 2");
//    }
//
//    @Override
//    protected List<SensorType> getSupportedSensors() {
//        List<SensorType> supportedSensors = Arrays.asList(
//                SensorType.Accelerometer,
//                SensorType.Gyroscope,
//                SensorType.HeartRate,
//                SensorType.AmbientLight,
//                SensorType.Calories,
//                SensorType.Gsr,
//                SensorType.Pedometer,
//                SensorType.SkinTemperature,
//                SensorType.Barometer,
//                SensorType.UV);
//        return supportedSensors;
//    }
//
//    @Override
//    public void connect(Context context, ConnectionStatusListener connectionListener){
//        super.connect(context,connectionListener);
//
//        BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
//        if(devices.length < 1){
//            Log.e(logTagString, "MicrosoftBand2 is unavailable");
//            updateConnectionStatus(ConnectionStatus.DeviceUnavailable);
//            return;
//        }
//
//        client = BandClientManager.getInstance().create(context, devices[0]);
//        client.connect();
//        Log.i(logTagString, "MicrosoftBand2 is available");
//        updateConnectionStatus(ConnectionStatus.Connected);
//    }
//
//    @Override
//    public void disconnect() {
//        super.disconnect();
//        client.disconnect();
//        updateConnectionStatus(ConnectionStatus.Disconnected);
//    }
//
//    public void askForHeartRateConsent(Activity activity, HeartRateConsentListener heartRateConsentListener){
//        client.getSensorManager().requestHeartRateConsent(activity, heartRateConsentListener);
//    }
//
//    public boolean isHearRateConsentGranted(){
//        return client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED;
//    }
//
//    @Override
//    public void registerForEvents(SystemEvents systemEvents) {
//        super.registerForEvents(systemEvents);
//        registerAccelerometer(systemEvents.getSensorEvents(SensorType.Accelerometer));
//        registerGyroscope(systemEvents.getSensorEvents(SensorType.Gyroscope));
//        registerHeartRate(systemEvents.getSensorEvents(SensorType.HeartRate));
//        registerAmbientLight(systemEvents.getSensorEvents(SensorType.AmbientLight));
//        registerCalories(systemEvents.getSensorEvents(SensorType.Calories));
//        registerGsr(systemEvents.getSensorEvents(SensorType.Gsr));
//        registerPedometer(systemEvents.getSensorEvents(SensorType.Pedometer));
//        registerSkinTemperature(systemEvents.getSensorEvents(SensorType.SkinTemperature));
//        registerBarometer(systemEvents.getSensorEvents(SensorType.Barometer));
//        registerUV(systemEvents.getSensorEvents(SensorType.UV));
//    }
//
//    private void registerAccelerometer(SensorEvents sensorEvents){
//        accelerometerSensorEvents = sensorEvents;
//        accelerometerSensorEvents.turnOnCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().registerAccelerometerEventListener(new BandAccelerometerEventListener() {
//                        @Override
//                        public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
//                            if (event != null) {
//                                AccelerometerData data = new AccelerometerData(
//                                        event.getAccelerationX(),
//                                        event.getAccelerationY(),
//                                        event.getAccelerationZ());
//                                accelerometerSensorEvents.dataEvent.trigger(data);
//                            }
//                        }
//                    }, SampleRate.MS128);
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//        accelerometerSensorEvents.turnOffCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().unregisterAccelerometerEventListeners();
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//    }
//
//    private void registerGyroscope(SensorEvents sensorEvents){
//        gyroscopeSensorEvents = sensorEvents;
//        gyroscopeSensorEvents.turnOnCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().registerGyroscopeEventListener(new BandGyroscopeEventListener() {
//                        @Override
//                        public void onBandGyroscopeChanged(BandGyroscopeEvent event) {
//                            if (event != null) {
//                                GyroscopeData data = new GyroscopeData(
//                                        event.getAngularVelocityX(),
//                                        event.getAngularVelocityY(),
//                                        event.getAngularVelocityZ());
//                                gyroscopeSensorEvents.dataEvent.trigger(data);
//                            }
//                        }
//                    }, SampleRate.MS128);
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//        gyroscopeSensorEvents.turnOffCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().unregisterGyroscopeEventListeners();
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//    }
//
//    private void registerHeartRate(SensorEvents sensorEvents){
//        heartRateSensorEvents = sensorEvents;
//
//        heartRateSensorEvents.turnOnCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().registerHeartRateEventListener(new BandHeartRateEventListener() {
//                        @Override
//                        public void onBandHeartRateChanged(BandHeartRateEvent event) {
//                            if (event != null) {
//                                HeartRateData data = new HeartRateData(event.getHeartRate());
//                                heartRateSensorEvents.dataEvent.trigger(data);
//                            }
//                        }
//                    });
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//        heartRateSensorEvents.turnOffCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().unregisterHeartRateEventListeners();
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//    }
//
//    private void registerAmbientLight(SensorEvents sensorEvents){
//        ambientLightSensorEvents = sensorEvents;
//        ambientLightSensorEvents.turnOnCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().registerAmbientLightEventListener(new BandAmbientLightEventListener() {
//                        @Override
//                        public void onBandAmbientLightChanged(BandAmbientLightEvent bandAmbientLightEvent) {
//                            AmbientLightData data = new AmbientLightData(bandAmbientLightEvent.getBrightness());
//                            ambientLightSensorEvents.dataEvent.trigger(data);
//                        }
//                    });
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//        ambientLightSensorEvents.turnOffCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().unregisterAmbientLightEventListeners();
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//    }
//
//    private void registerCalories(SensorEvents sensorEvents){
//        caloriesSensorEvents = sensorEvents;
//        caloriesSensorEvents.turnOnCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().registerCaloriesEventListener(new BandCaloriesEventListener(){
//                        @Override
//                        public void onBandCaloriesChanged(BandCaloriesEvent bandCaloriesEvent) {
//                            CaloriesData data = new CaloriesData((int) bandCaloriesEvent.getCalories());
//                            caloriesSensorEvents.dataEvent.trigger(data);
//                            return;
//                        }
//                    });
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//        caloriesSensorEvents.turnOffCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().unregisterCaloriesEventListeners();
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//    }
//
//    private void registerGsr(SensorEvents sensorEvents){
//        gsrSensorEvents = sensorEvents;
//        gsrSensorEvents.turnOnCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().registerGsrEventListener(new BandGsrEventListener() {
//                        @Override
//                        public void onBandGsrChanged(BandGsrEvent bandGsrEvent) {
//                            GsrData data = new GsrData(bandGsrEvent.getResistance());
//                            gsrSensorEvents.dataEvent.trigger(data);
//                        }
//                    });
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//        gsrSensorEvents.turnOffCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().unregisterGsrEventListeners();
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//    }
//
//    private void registerPedometer(SensorEvents sensorEvents){
//        pedometerSensorEvents = sensorEvents;
//        pedometerSensorEvents.turnOnCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().registerPedometerEventListener(new BandPedometerEventListener() {
//                        @Override
//                        public void onBandPedometerChanged(BandPedometerEvent bandPedometerEvent) {
//                            PedometerData data = new PedometerData((int)bandPedometerEvent.getTotalSteps());
//                            pedometerSensorEvents.dataEvent.trigger(data);
//                        }
//                    });
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//        pedometerSensorEvents.turnOffCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().unregisterPedometerEventListeners();
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//    }
//
//    private void registerSkinTemperature(SensorEvents sensorEvents){
//        skinTemperatureSensorEvents = sensorEvents;
//        skinTemperatureSensorEvents.turnOnCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().registerSkinTemperatureEventListener(new BandSkinTemperatureEventListener() {
//                        @Override
//                        public void onBandSkinTemperatureChanged(BandSkinTemperatureEvent bandSkinTemperatureEvent) {
//                            SkinTemperatureData data = new SkinTemperatureData(bandSkinTemperatureEvent.getTemperature());
//                            skinTemperatureSensorEvents.dataEvent.trigger(data);
//                        }
//                    });
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//        skinTemperatureSensorEvents.turnOffCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().unregisterSkinTemperatureEventListeners();
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//    }
//
//    private void registerBarometer(SensorEvents sensorEvents){
//        barometerSensorEvents = sensorEvents;
//        barometerSensorEvents.turnOnCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().registerBarometerEventListener(new BandBarometerEventListener() {
//                        @Override
//                        public void onBandBarometerChanged(BandBarometerEvent bandBarometerEvent) {
//                            BarometerData data = new BarometerData(
//                                    bandBarometerEvent.getTemperature(),
//                                    bandBarometerEvent.getAirPressure());
//                            barometerSensorEvents.dataEvent.trigger(data);
//                        }
//                    });
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//        barometerSensorEvents.turnOffCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().unregisterBarometerEventListeners();
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//    }
//
//    private void registerUV(SensorEvents sensorEvents){
//        uvSensorEvents = sensorEvents;
//        uvSensorEvents.turnOnCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().registerUVEventListener(new BandUVEventListener() {
//                        @Override
//                        public void onBandUVChanged(BandUVEvent bandUVEvent) {
//                            UVData data = new UVData(bandUVtoInt(bandUVEvent.getUVIndexLevel()));
//                            uvSensorEvents.dataEvent.trigger(data);
//                        }
//                    });
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//        uvSensorEvents.turnOffCommand.addHandler(new SensorDataListener<Void>() {
//            @Override
//            public void onSensorDataChanged(Void data) {
//                try {
//                    client.getSensorManager().unregisterUVEventListeners();
//                } catch (Exception e) {
//                    Log.e(logTagString, e.getMessage());
//                }
//            }
//        });
//    }
//
//    int bandUVtoInt(UVIndexLevel uvLevel){
//        switch (uvLevel) {
//            case NONE:  return 0;
//            case LOW:  return 25;
//            case MEDIUM:  return 50;
//            case HIGH:  return 75;
//            case VERY_HIGH:  return 100;
//        }
//        return -1;
//    }
//}
//
