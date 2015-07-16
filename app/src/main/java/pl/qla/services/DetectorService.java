package pl.qla.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import pl.qla.detector.AccelerometerListener;
import pl.qla.detector.IntrusionListener;
import pl.qla.detector.PacketSentListener;
import pl.qla.network.CommunicationProtocol;
import pl.qla.network.CommunicationProtocol.MESSAGE_TYPE;

public class DetectorService extends Service implements IntrusionListener {
    private static final String TAG = DetectorService.class.getName();
    private final DetectorBinder detectorBinder = new DetectorBinder();
    private STATUS status;
    private AccelerometerListener accelerometerListener;
    private PacketSentListener packetSentListener;
    private IntrusionListener intrusionListener;
    private CommunicationProtocol communicationProtocol;
    private ScheduledExecutorService scheduler;

    public enum STATUS {
        DISABLED,
        ENABLED,
        ALARM,
        OFFLINE,
        ERROR
    }

    public DetectorService() {
        status = STATUS.DISABLED;
    }

    @Override
    public void onCreate() {
        accelerometerListener = new AccelerometerListener(this, 1.0f);
        communicationProtocol = new CommunicationProtocol("192.168.1.11", 5555);
        Log.i(TAG, "Detector service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setIntrusionListener(IntrusionListener intrusionListener) {
        this.intrusionListener = intrusionListener;
    }

    public void setPacketSentListener(PacketSentListener packetSentListener) {
        this.packetSentListener = packetSentListener;
        communicationProtocol.setPacketSentListener(packetSentListener);
    }

    public void enable() {
        try {
            Log.i(TAG, "Enabling detector service ...");
            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometerSensor != null) {
                sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
                communicationProtocol.open();
                communicationProtocol.send(MESSAGE_TYPE.INIT);
                scheduler = Executors.newSingleThreadScheduledExecutor();
                scheduler.scheduleAtFixedRate(new Runnable() {
                    public void run() {
                        try {
                            communicationProtocol.send(MESSAGE_TYPE.PING);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, 0, 2, TimeUnit.SECONDS);
                status = STATUS.ENABLED;
                Log.i(TAG, "Listener has been register to accelerometer");
            } else {
                status = STATUS.ERROR;
                Log.e(TAG, "No accelerometer has been found while enabling detector service");
            }
            Log.i(TAG, "Detector service enabled");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disable() {
        try {
            Log.i(TAG, "Disabling detector service ...");
            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometerSensor != null) {
                sensorManager.unregisterListener(accelerometerListener);
                scheduler.shutdownNow();
                communicationProtocol.send(MESSAGE_TYPE.TERMINATE);
                communicationProtocol.close();
                Log.i(TAG, "Listener has been unregistered from acceleromter");
                status = STATUS.DISABLED;
            } else {
                Log.e(TAG, "No accelerometer has been found while disabling detector service");
                status = STATUS.ERROR;
            }
            Log.i(TAG, "Detector service disabled");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {

        Log.i(TAG, "binded");
        return detectorBinder;
    }

    @Override
    public void onIntrusionListener() {
        try {
            Log.i(TAG, "Intrusion detected. Alarming !");
            status = STATUS.ALARM;
            communicationProtocol.send(MESSAGE_TYPE.ALARM);
            intrusionListener.onIntrusionListener();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Detector binder connects activity with detector service
     */
    public class DetectorBinder extends Binder {
        public DetectorService getDetectorService() {
            return DetectorService.this;
        }

        public void setIntrusionListener(IntrusionListener intrusionListener) {
            DetectorService.this.intrusionListener = intrusionListener;
        }

        public void setPacketSentListener(PacketSentListener packetSentListener) {
            DetectorService.this.packetSentListener = packetSentListener;
        }
    }

    public class ReceiverNotifier implements Runnable {
        @Override
        public void run() {

        }
    }
}
