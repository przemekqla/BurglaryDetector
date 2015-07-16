package pl.qla.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import pl.qla.R;
import pl.qla.detector.IntrusionListener;
import pl.qla.detector.PacketSentListener;
import pl.qla.services.DetectorService;


public class BurglaryDetectorActivity extends Activity {
    private static final String TAG = BurglaryDetectorActivity.class.getName();
    private DetectorService detectorService;
    private boolean isBoundToService;
    private TextView statusTextView;
    private TextView sentPackets;
    private TextView receiverIPTextView;
    private TextView receiverPortTextView;
    private IntrusionListener intrusionListener;
    private PacketSentListener packetSentListener;
    private Handler handler = new Handler();
    private SeekBar frequencySeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_burglary_detector);
        statusTextView = (TextView) findViewById(R.id.status);
        isBoundToService = false;
        sentPackets = (TextView) findViewById(R.id.sent_packets_number);
        intrusionListener = new IntrusionListener() {
            @Override
            public void onIntrusionListener() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateDetectorStatus();
                    }
                });
            }
        };

        packetSentListener = new PacketSentListener() {
            @Override
            public void onPacketSentListener(final int packetNumber) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        sentPackets.setText(String.valueOf(packetNumber));
                    }
                });
            }
        };

        receiverIPTextView = ((TextView) findViewById(R.id.receiver_ip));
        receiverIPTextView.setText("192.168.1.11");
        receiverPortTextView = ((TextView) findViewById(R.id.receiver_port));
        receiverPortTextView.setText("5555");

        final TextView frequencyValueTextView = (TextView) findViewById(R.id.frequency_value);
        frequencySeekBar = (SeekBar) findViewById(R.id.frequency);
        frequencySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                frequencyValueTextView.setText(" : " + i + " " + getString(R.string.seconds));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        frequencySeekBar.setProgress(frequencySeekBar.getMax() / 2);

        Intent detectorIntent = new Intent(BurglaryDetectorActivity.this, DetectorService.class);
        bindService(detectorIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        updateDetectorStatus();
        Button button = (Button) findViewById(R.id.enableDetectorBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button button = (Button) view;
                if (isBoundToService && detectorService.getStatus() == DetectorService.STATUS.DISABLED) {
                    button.setText(R.string.disable_detector);
                    disableConfigurationInterface();
                    detectorService.enable();
                    if (detectorService.getStatus() == DetectorService.STATUS.ENABLED) {
                        updateDetectorStatus();
                    } else {
                        setStatusText(R.string.error_while_enablig_detector_service);
                    }
                } else if (isBoundToService && detectorService.getStatus() != DetectorService.STATUS.DISABLED) {
                    button.setText(R.string.enable_detector);
                    enableConfigurationInterface();
                    detectorService.disable();
                    if (detectorService.getStatus() == DetectorService.STATUS.DISABLED) {
                        updateDetectorStatus();
                    } else {
                        setStatusText(R.string.error_while_disabling_service);
                    }
                } else if (isBoundToService == false) {
                    setStatusText(R.string.error_while_binding_to_detector_serivce);
                }
            }
        });
    }

    private void setStatusText(int resID) {
        statusTextView.setText(resID);
    }

    private void setStatusText(String text) {
        statusTextView.setText(text);
    }

    public void updateDetectorStatus() {
        if (detectorService != null) {
            switch (detectorService.getStatus()) {
                case ENABLED:
                    setStatusText(R.string.detector_service_is_enabled);
                    break;
                case DISABLED:
                    setStatusText(R.string.detector_service_is_disabled);
                    break;
                case ALARM:
                    setStatusText(R.string.detector_service_detect_intrusion);
                    break;
                case ERROR:
                    setStatusText(R.string.detector_service_has_error);
                    break;
                case OFFLINE:
                    setStatusText(R.string.detector_service_is_offline);
                    break;
            }
        } else {
            setStatusText(R.string.detector_not_bind);
        }
    }

    public void disableConfigurationInterface() {
        receiverPortTextView.setEnabled(false);
        receiverIPTextView.setEnabled(false);
        frequencySeekBar.setEnabled(false);
    }

    public void enableConfigurationInterface() {
        receiverPortTextView.setEnabled(true);
        receiverIPTextView.setEnabled(true);
        frequencySeekBar.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_burglary_detector, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            DetectorService.DetectorBinder detectorBinder = (DetectorService.DetectorBinder) iBinder;
            detectorService = detectorBinder.getDetectorService();
            detectorService.setIntrusionListener(intrusionListener);
            detectorService.setPacketSentListener(packetSentListener);
            isBoundToService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBoundToService = false;
        }
    };
}
