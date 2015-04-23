package com.utd_scavenger.company.utdscavenger;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.utd_scavenger.company.utdscavenger.Data.Item;

import java.io.IOException;

/**
 * Handles the process of creating a new game.
 *
 * Written by Jonathan Darling and Stephen Kuehl
 */
public class CreateActivityStepOne extends Activity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private GoogleApiClient mGoogleApiClient;

    private EditText mItemName;

    // Stores the user's last known location.
    private Location mLastLocation;
    private double mLatitude;
    private double mLongitude;

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
     * Written by Jonathan Darling and Stephen Kuehl
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_stepone);

        // Setup google API services.
        buildGoogleApiClient();

        // Bind UI elements.
        mItemName = (EditText)findViewById(R.id.edit_name);

        // Set up the the NFC Adapter.
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // Check to see if the device supports NFC.
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available on this device.", Toast.LENGTH_LONG).show();
            finish();
            return;
        } else if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC is not enabled on this device. Please enable NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupPendingIntent();
        setupIntentFilter();
        setupTechListArray();

    }

    /**
     * Create a PendingIntent for NFC tag reading. This intent will be ran when
     * an NFC tag is scanned.
     *
     * Written by Jonathan Darling and Stephen Kuehl
     */
    private void setupPendingIntent() {

        // Code to identify the origin activity. This intent will be run from
        // this very activity so is not needed.
        int requestCode = 0;

        // Special handling flags. Not needed for this use case.
        int flags = 0;

        // Create a new intent for this class.
        Intent nfcIntent = new Intent(this, getClass());

        // Ensure that the activity is not restarted if it is already running.
        nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Create a pending intent from the intent we already created.
        mPendingIntent = PendingIntent.getActivity(this, requestCode, nfcIntent, flags);
    }

    /**
     * Create an Intent Filter limited to the URI or MIME type to intercept TAG
     * scans from.
     *
     * Written by Jonathan Darling and Stephen Kuehl
     */
    private void setupIntentFilter() {

        // Limit our scanner to only read tags with NDEF payloads.
        IntentFilter tagIntentFilter = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mIntentFilters = new IntentFilter[] { tagIntentFilter };
    }

    /**
     * Create an array of technologies to handle.
     *
     * Written by Jonathan Darling and Stephen Kuehl
     */
    private void setupTechListArray() {
        mNFCTechLists = new String[][] {
                new String[] {
                        NfcF.class.getName()
                }
        };
    }

    /**
     * Called after onRestoreInstanceState, onRestart, or onPause, for your
     * activity to start interacting with the user.
     *
     * Written by Jonathan Darling and Stephen Kuehl
     */
    @Override
    public void onResume() {
        super.onResume();

        // Enable foreground dispatch. This will ensure that when the NFC tag is
        // read, it will immediately be processed by this activity.
        mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilters, mNFCTechLists);
    }

    /**
     * This is called for activities that set launchMode to "singleTop" in their
     * package, or if a client used the Intent.FLAG_ACTIVITY_SINGLE_TOP flag
     * when calling startActivity.
     *
     * @param intent The intent that was started.
     *
     * Written by Jonathan Darling and Stephen Kuehl
     */
    protected void onNewIntent(Intent intent) {
        // Check to see if the Activity was started by an NFC tag being read.
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            System.out.println("DEBUG: Tag scanned.");

            // Ensure that we have text in the text field.
            if (!mItemName.getText().toString().equals("")) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                try {
                    System.out.println("DEBUG: Writing string " + mItemName.getText().toString() + " to tag.");
                    write(mItemName.getText().toString(), tag);
                    System.out.println("DEBUG: Finished writing string " + mItemName.getText().toString() + " to tag.");

                    // Tell the user that the write was successful and clear out
                    // the text field.
                    Toast.makeText(this, "Write successful!", Toast.LENGTH_LONG).show();
                    mItemName.setText("");

                    // Create a new Item.
                    Item item = new Item(mItemName.getText().toString(), mLatitude, mLongitude);


                } catch (IOException e) {
                    Toast.makeText(this, "Write failed, please try again.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (FormatException e) {
                    Toast.makeText(this, "Write failed, please try again.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private NdefRecord createRecord(String text) {
        String mimeType = "text/utdscavenger";

        byte[] textBytes = text.getBytes();
        byte[] mimeTypeBytes = mimeType.getBytes();

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeTypeBytes, new byte[0], textBytes);
        return recordNFC;
    }

    private void write(String text, Tag tag) throws IOException, FormatException {

        NdefRecord[] records = { createRecord(text) };
        NdefMessage message = new NdefMessage(records);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        ndef.writeNdefMessage(message);
        ndef.close();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionList) {
        System.out.println("FUCKIIN YEEEEEH");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            mLatitude = mLastLocation.getLatitude();
            mLongitude = mLastLocation.getLongitude();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    public void onClickContinue (View view) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
