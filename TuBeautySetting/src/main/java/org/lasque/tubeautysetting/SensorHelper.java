package org.lasque.tubeautysetting;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.lasque.tusdkpulse.core.utils.TLog;

/**
 * TuSDK
 * org.lasque.effectcamerademo.utils
 * android-ec-demo
 *
 * @author H.ys
 * @Date 2021/9/13  18:45
 * @Copyright (c) 2020 tusdk.com. All rights reserved.
 */
public class SensorHelper {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SensorEventListener listener;

    private double mAngle;

    private int mRate = SensorManager.SENSOR_DELAY_NORMAL;

    public SensorHelper(Context context){
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        TLog.e("current sensor %s",mSensor);
        if (mSensor != null){
            listener = new SensorEventListenerImpl();
            mSensorManager.registerListener(listener,mSensor,20);
        }
    }

    public void onOrientationChanged(double angle){
        mAngle = angle;
    }

    public double getDeviceAngle(){
        return mAngle;
    }

    public void release(){
        mSensorManager.unregisterListener(listener);
        listener = null;
    }

    class SensorEventListenerImpl implements SensorEventListener{

        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            float x = values[0];
            float y = values[1];
            float z = values[2];

            double rotDegree = Math.atan2(x,y) * 180 / Math.PI;
            double horDegree = Math.atan2(z,Math.sqrt(x * x + y * y) * 180 / Math.PI);

            horDegree = Math.round(horDegree);

            double angle = 360 - rotDegree;
            angle = Math.round(angle);

            if (horDegree == 1.0){
                angle = 0;
            }

            int orientation = (int) angle;
            // normalize to 0 - 359 range
            while (orientation >= 360) {
                orientation -= 360;
            }
            while (orientation < 0) {
                orientation += 360;
            }

            onOrientationChanged(orientation);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}
