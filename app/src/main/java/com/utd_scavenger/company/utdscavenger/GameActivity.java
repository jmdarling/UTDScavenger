package com.utd_scavenger.company.utdscavenger;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.utd_scavenger.company.utdscavenger.Data.Item;

import java.util.ArrayList;

/**
 * Primary game screen.
 *
 * Written by Jonathan Darling and Stephen Kuehl
 */
public class GameActivity extends Activity implements OnMapReadyCallback {
    private ArrayList<Item> mItems;
    private ArrayList<Item> mNotFoundItems;
    private ArrayList<Item> mFoundItems;

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
        setContentView(R.layout.activity_game);

        // Get the list of items from the intent.
        Intent intent = getIntent();
        mItems = (ArrayList) intent.getParcelableArrayListExtra("items");

        mNotFoundItems = new ArrayList<>();
        mFoundItems = new ArrayList<>();

        // The user hasn't found any items when they first start the game so
        // populate the notFoundItems list.
        if (mItems != null) {
            mNotFoundItems.addAll(mItems);
        }

        // Set up the map.
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
        IntentFilter tagIntentFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        // Limit our scanner to only read tags with our specified MIME type.
        tagIntentFilter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        tagIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        try {
            tagIntentFilter.addDataType("text/utdscavenger");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
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
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {

            // Read the name of the item on the NFC tag.
            String name = getNfcMessage(intent);

            // "Collect" the item specified by the NFC tag.
            collectItem(name);
        }
    }

    /**
     * Process any intent that is triggered by reading an NFC tag and return the
     * tag's first message. This will work for our use case as we write the
     * message ourselves.
     *
     * @param intent The NFC intent.
     *
     * @return The tag's first message.
     *
     * Written by Jonathan Darling and Stephen Kuehl
     */
    private String getNfcMessage(Intent intent) {

        // Read any NDEF messages present on the tag.
        Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        // Get the first message.
        NdefMessage message = (NdefMessage)messages[0];

        // Get the first record.
        NdefRecord record = message.getRecords()[0];

        // Return the message stored in the first record.
        return new String(record.getPayload());
    }

    /**
     * Collect the item specified by the name. This will change the item's
     * status from not being found to found as will be reflected in the found
     * and not found lists.
     *
     * @param name The name of the item that was collected.
     *
     * Written by Jonathan Darling and Stephen Kuehl
     */
    private void collectItem(String name) {
        // Find the item in the items list.
        if (mItems != null) {
            Item foundItem = null;

            for (Item item : mItems) {
                if (item.getName().equals(name)) {
                    foundItem = item;
                }
            }

            if (foundItem != null) {
                mNotFoundItems.remove(foundItem);
                mFoundItems.add(foundItem);
            }
        }

        //TODO: update adapters for mNotFoundItems and mFoundItems

        Toast.makeText(this, "Collected item: " + name, Toast.LENGTH_SHORT).show();
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into
     * the background, but has not (yet) been killed. The counterpart to
     * onResume.
     *
     * Written by Jonathan Darling and Stephen Kuehl
     */
    @Override
    public void onPause() {
        super.onPause();

        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    /**
     * Called when the map is ready to be used.
     *
     * @param map A non-null instance of a GoogleMap associated with the
     *                  MapFragment or MapView that defines the callback.
     *
     *  Written by Jonathan Darling
     */
    @Override
    public void onMapReady(GoogleMap map) {
        // Enable the user to be able to see their location.
        map.setMyLocationEnabled(true);

        // Set the starting view and zoom level. This will center on UTD.
        LatLng utdLocation = new LatLng(32.9837395, -96.7511764);
        int zoom = 15;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(utdLocation, zoom));

        // Add waypoints for items.
        if (mItems != null) {
            for (Item item : mItems) {
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(item.getLatitude(), item.getLongitude()))
                        .title(item.getName())
                );
            }
        }
    }
}
