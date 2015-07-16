package pl.qla.network;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.net.URL;

import pl.qla.R;

/**
 * Created by przemek on 23/06/15.
 */
public class ReadIPAddressTask extends AsyncTask<String, Integer, String> {
    private Context context;
    private TextView textView;

    public ReadIPAddressTask(Context context, TextView textView) {
        this.context = context;
        this.textView = textView;
    }

    @Override
    protected String doInBackground(String... urls) {
        String result = "";
        try {
            HttpClient httpClient = new DefaultHttpClient();
            System.out.println(urls[0]);
            HttpGet httpGet = new HttpGet(urls[0]);
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                result = out.toString();
                out.close();
                if (result != null) {
                    result = result.trim();
                } else {
                    result = context.getString(R.string.error_while_reading_ip);
                }
            } else {
                result = context.getString(R.string.error_while_reading_ip);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = context.getString(R.string.error_while_reading_ip);
        }
        return result;
    }

    @Override
    protected void onPostExecute(String ipAddress) {
        super.onPostExecute(ipAddress);
        textView.setText(ipAddress);
    }
}
