package com.utd_scavenger.company.utdscavenger;

import android.app.Activity;
import android.content.Intent;

import android.location.Location;
import android.nfc.FormatException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.utd_scavenger.company.utdscavenger.Data.Item;
import com.utd_scavenger.company.utdscavenger.Exceptions.NfcNotAvailableException;
import com.utd_scavenger.company.utdscavenger.Exceptions.NfcNotEnabledException;
import com.utd_scavenger.company.utdscavenger.Helpers.NfcHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the process of creating a new game.
 *
 * Written by Jonathan Darling and Stephen Kuehl
 */
public class CreateStepOneActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {
    // Helpers.
    private NfcHelper mNfcHelper;

    // Bound UI elements.
    private EditText mItemName;
    private ListView mItemsListView;
    private Button mSubmitButton;

    // Storage.
    private ArrayList<Item> mItems;
    private List<String> mItemsNames;

    // Adapters.
    private ArrayAdapter<String> mItemsNamesAdapter;

    // Utility classes.
    private GoogleApiClient mGoogleApiClient;

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

        // Set up the Google API services.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Set up the NFC helper.
        try {
            mNfcHelper = new NfcHelper(this, getClass());
        } catch (NfcNotAvailableException | NfcNotEnabledException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Bind UI elements.
        mItemName = (EditText)findViewById(R.id.edit_name);
        mItemsListView = (ListView)findViewById(R.id.items);
        mSubmitButton = (Button)findViewById(R.id.submit);

        // Instantiate array lists.
        mItems = new ArrayList<>();
        mItemsNames = new ArrayList<>();

        // Set up the array adapter and assign it to the list view.
        mItemsNamesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mItemsNames);
        mItemsListView.setAdapter(mItemsNamesAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
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
        mNfcHelper.enableForegroundDispatch();
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
        // Note that we are using ACTION_TAG_DISCOVERED. This is generic so that
        // we can write to a fresh tag, not just one with NDEF data already on
        // it.
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            String itemName = mItemName.getText().toString();
            // Ensure that we have text in the text field. If not, we don't want
            // to write to the tag.
            if (!itemName.equals("")) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                try {
                    // Write to the tag.
                    mNfcHelper.write(itemName, tag);

                    // Clear the text field.
                    mItemName.setText("");

                    // Create a new item to correspond to the tag.
                    new AddItemTask().execute(itemName);

                } catch (IOException | FormatException e) {
                    // This is expected to happen from time to time. The tag has
                    // to make contact for long enough to be successfully
                    // written to.
                    Toast.makeText(this, "Write failed, please try again.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else {
                // There is no text in the text field, don't write.
                Toast.makeText(this, "Please enter an item name before scanning.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateItemsNamesListView() {
        mItemsNames.clear();
        for (Item item : mItems) {
            mItemsNames.add(item.getName());
        }
        mItemsNamesAdapter.notifyDataSetChanged();
    }

    protected void onClickContinue(View view) {
        Intent intent = new Intent(this, CreateStepTwoActivity.class);
        intent.putExtra("items", mItems);
        startActivity(intent);
    }

    @Override
    public void onConnected(Bundle bundle) {}

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    protected class AddItemTask extends AsyncTask<String, Void, Location> {
        private String mItemName;

        protected void onPreExecute() {
            // Disable the submit button, this task needs to finish before we
            // go to the next step of the game creation process.
            mSubmitButton.setEnabled(false);
        }

        /**
         * Perform a computation on a background thread.
         * Fetches the users current location.
         *
         *
         * @param params The parameters of the task.
         *
         * @return A result, defined by the subclass of this task.
         */
        protected Location doInBackground(String... params) {
            int attempts = 1;
            int sleepTime = 1000; // 1000 ms = 1 second

            // Save the item name to be added.
            mItemName = params[0];

            // Get the current location.
            Location location;
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            // Ensure that the location was updated, if not, keep trying.
            if (location == null) {
                Toast.makeText(CreateStepOneActivity.this, "We're having a bit of trouble getting your location. Give us a few seconds.", Toast.LENGTH_LONG).show();

                while (location == null && attempts <= 10) {
                    // Sleep the thread for a second to allow the API services
                    // to try and catch up.
                    try {
                        Thread.sleep(sleepTime, 0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                }
            }

            return location;
        }

        protected void onPostExecute(Location location) {
            // Check if the user's location is available.
            if (location != null) {
                // Create a new item and add it to the items list.
                Item item = new Item(mItemName, location.getLatitude(), location.getLongitude());
                mItems.add(item);

                // Update the ListView
                updateItemsNamesListView();
            } else {
                Toast.makeText(CreateStepOneActivity.this, "Unfortunately, we could not get your location at this time. Please try again later.", Toast.LENGTH_LONG).show();
            }

            // Re-enable the submit button.
            mSubmitButton.setEnabled(true);
        }
    }
}
