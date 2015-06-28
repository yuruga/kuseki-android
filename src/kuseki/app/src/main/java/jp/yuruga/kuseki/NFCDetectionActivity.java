package jp.yuruga.kuseki;

import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import static jp.yuruga.kuseki.utils.Util.*;

public class NFCDetectionActivity extends ActionBarActivity implements ApiTask.APICallbackInterface {

    private TextView mTextView;
    private NfcAdapter mNfcAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.textView_sample);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!mNfcAdapter.isEnabled()) {
            //mTextView.setText("NFC is disabled.");
        } else {
            //mTextView.setText(R.string.explanation);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            handleIntent(intent);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }


    private void handleIntent(Intent intent) {
        Tag mNfcTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if ( mNfcTag != null )  {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
                StringBuffer strb = new StringBuffer();
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                    strb.append(readTextRecord(msgs[i].getRecords()[0].getPayload()));

                }
                //log(strb.toString());
                mTextView.setText(strb.toString());

                try {
                    JSONObject json = new JSONObject(strb.toString());
                    String params = json.getJSONObject("params").toString();
                    String adurl = json.getString("url");
                    //start monitoring service
                    Intent i = new Intent(this, NFCMonitorService.class);
                    i.setAction(NFCMonitorService.ACTION_MONITOR_START);
                    i.putExtra(NFCMonitorService.EXTRA_PARAMS, params);
                    i.putExtra(NFCMonitorService.EXTRA_TAG, mNfcTag);
                    this.startService(i);

                    //TODO: open url
                    Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                    viewIntent.setData(Uri.parse(adurl));
                    startActivity(viewIntent);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                finish();



            }
        }
    }



    private String readTextRecord(byte[] payload){
        try
        {
            //Get the Text Encoding
            String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";

            //Get the Language Code
            int languageCodeLength = payload[0] & 0077;
            String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

            //Get the Text
            String text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);

            return text;
        }
        catch(Exception e)
        {
            throw new RuntimeException("Record Parsing Failure!!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAPIResult(String result) {

    }
}
