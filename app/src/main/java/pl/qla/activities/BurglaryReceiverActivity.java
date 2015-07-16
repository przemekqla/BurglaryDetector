package pl.qla.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import pl.qla.R;
import pl.qla.network.ReadIPAddressTask;

public class BurglaryReceiverActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_burglary_receiver);
        readReciverIPAddress();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_burglary_reciver, menu);
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

    private void readReciverIPAddress() {
        ReadIPAddressTask readIPAddressTask = new ReadIPAddressTask(this, (TextView) findViewById(R.id.reciver_ip_address));
        readIPAddressTask.execute("http://ifcfg.me/ip");
    }
}
