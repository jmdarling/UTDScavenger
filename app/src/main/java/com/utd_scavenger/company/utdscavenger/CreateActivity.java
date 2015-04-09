package com.utd_scavenger.company.utdscavenger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

/**
 * Handles the process of creating a new game.
 *
 * Written by Jonathan Darling and Stephen Kuehl
 */
public class CreateActivity extends Activity {

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
     *                           contains the data it most recently supplied in
     *                           onSaveInstanceState(Bundle). Otherwise it is
     *                           null.
     *
     * Written by Jonathan Darling
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        // Instantiate the the NFC Adapter.
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // Check to see if the device supports NFC.
        if (mNfcAdapter != null) {
            // Inital text in the textview.
            //mTextView.setText("Awaiting Contact..");
        } else {
            // if the device does not support NFC
           // mTextView.setText("This device does not have NFC enabled.");
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
     * onClick listener that responds when a player taps "Add New Player."
     * This class will first open a dialog that will allow the user to tap and add
     * new players. A listview within the dialog will display showing the newly added
     * players.
     *
     * @param view
     *
     * Written by Stephen Kuehl
     */
    public void onClickPlayers (View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(CreateActivity.this).create();
        alertDialog.setTitle("Add New Players");
        alertDialog.setMessage("NFC is Enabled. Please Scan New Players");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
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
        Intent createActivity = new Intent (this, CreateActivity.class);
        startActivity(createActivity);
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
