package com.utd_scavenger.company.utdscavenger;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;


import java.util.Arrays;

/**
 * Handles the process of joining an existing game.
 *
 * Written by Jonathan Darling and Stephen Kuehl
 */
public class JoinActivity extends Activity {

    private TextView mTextView;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mIntentFilters;
    private String[][] mNFCTechLists;

    /**
     * Called when the activity is starting. This is where most initialization
     * should go.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle
     *
     *                           contains the data it most recently supplied in
     *                           onSaveInstanceState(Bundle). Otherwise it is
     *                           null.
     *
     * Written by Jonathan Darling
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        // Set the textview for information output by the NFC Tag.
        mTextView = (TextView)findViewById(R.id.tv);

        // Instantiate the the NFC Adapter.
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // Check to see if the device supports NFC.
        if (mNfcAdapter != null) {
            // Inital text in the textview.
            mTextView.setText("Awaiting Contact..");
        } else {
            // if the device does not support NFC
            mTextView.setText("This device does not have NFC enabled.");
        }

        // Create an intent with the NFC tag data and deliver to the Join Activity.
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Set an intent filter for all MIME data.
        IntentFilter ndefIntent = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefIntent.addDataType("*/*");
            mIntentFilters = new IntentFilter[] { ndefIntent };
        } catch (Exception e) {
            Log.e("TagDispatch", e.toString());
        }

    }

    /**
     * Called when the new intent needs to be created
     *
     * @param intent The new intent that was started for the activity.
     *
     * Written by Stephen Kuehl
     */
    @Override
    public void onNewIntent(Intent intent) {
        String action = intent.getAction();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        String s = "The NFC Text Output:";

        // Parse through all NDEF messages and their records and pick text type only.
        Parcelable[] data = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (data != null) {
            try {
                for (int i = 0; i < data.length; i++) {
                    NdefRecord[] recs = ((NdefMessage)data[i]).getRecords();
                    for (int j = 0; j < recs.length; j++) {
                        if (recs[j].getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                                Arrays.equals(recs[j].getType(), NdefRecord.RTD_TEXT)) {
                            byte[] payload = recs[j].getPayload();
                            String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
                            int langCodeLen = payload[0] & 0077;

                            /*Display the text that is received*/
                            s += ("\n\n\"" + new String(payload, langCodeLen + 1, payload.length - langCodeLen - 1,
                                            textEncoding) + "\"");
                        }
                    }
                }
            } catch (Exception e) {
                // Error reading the tag.
                Log.e("TagDispatch", e.toString());
            }
        }

        // Display a message to the user that the NFC tag has been accepted
        Toast.makeText(getApplicationContext(), "Success! Loading Google Maps..",
                Toast.LENGTH_LONG).show();

        // Navigate to the Maps activity
        Intent mapIntent = new Intent (this, GameActivity.class);
        startActivity(mapIntent);
    }

    /**
     * onResume is called after the onPause to being interaction with the user which
     * includes waiting for the NFC scan
     *
     * Written by Stephen Kuehl
     */
    @Override
    public void onResume() {
        super.onResume();

        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilters, mNFCTechLists);
        }
    }

    /**
     * onPause begins processing the disableForegroundDispatch in the background
     *
     * Written by Stephen Kuehl
     */
    @Override
    public void onPause() {
        super.onPause();

        // NFCadapter is active and awaiting scan
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }
}
