package pl.qla.detector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

/**
 * Created by przemek on 21/06/15.
 */
public class AccelerometerListener implements SensorEventListener {
    private static final String TAG = AccelerometerListener.class.getName();
    private float[] previousValues;
    private float sensitivity = 2.0f;
    private IntrusionListener intrusionListener;

    public AccelerometerListener(IntrusionListener intrusionListener, float sensitivity) {
        this.intrusionListener = intrusionListener;
        this.sensitivity = sensitivity;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (previousValues == null) {
            previousValues = new float[3];
            for (int i = 0; i < 3; i++) {
                previousValues[i] = sensorEvent.values[i];
            }
        }
        if (accelerationDetected(previousValues, sensorEvent.values)) {
            intrusionListener.onIntrusionListener();
        }
        for (int i = 0; i < 3; i++) {
            previousValues[i] = sensorEvent.values[i];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private boolean accelerationDetected(float[] previousValues, float[] currentValues) {
        for (int i = 0; i < 3; i++) {
            if (Math.abs(previousValues[i] - currentValues[i]) > sensitivity) {
                return true;
            }
        }
        return false;
    }
}
