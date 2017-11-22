package uk.ac.solent.marcinwisniewski.bigfoottracker.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

public class StepsService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private int counter = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null)
        {
            stepDetectorSensor =
                    sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sendMessage();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void sendMessage() {
        Intent intent = new Intent("new-step");
        intent.putExtra("message", "New step taken!");

        counter++;
        if (counter == 2) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            counter = 0;
        }
    }
}