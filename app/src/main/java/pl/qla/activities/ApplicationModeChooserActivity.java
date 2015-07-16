package pl.qla.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import pl.qla.R;


public class ApplicationModeChooserActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_mode_chooser);
        Button acceptModeBtn = (Button) findViewById(R.id.accept_device_mode);
        acceptModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioButton detectorRadioBtn = (RadioButton) findViewById(R.id.detector);
                if (detectorRadioBtn.isChecked()) {
                    Intent intent = new Intent(ApplicationModeChooserActivity.this, BurglaryDetectorActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(ApplicationModeChooserActivity.this, BurglaryReceiverActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_application_mode_chooser, menu);
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
}
