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
import android.widget.TextView;
import android.widget.Toast;


import com.utd_scavenger.company.utdscavenger.Data.Item;
import com.utd_scavenger.company.utdscavenger.Exceptions.NfcNotAvailableException;
import com.utd_scavenger.company.utdscavenger.Exceptions.NfcNotEnabledException;
import com.utd_scavenger.company.utdscavenger.Helpers.ItemSerializer;
import com.utd_scavenger.company.utdscavenger.Helpers.NfcHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles the process of joining an existing game.
 *
 * Written by Stephen Kuehl
 */
public class JoinActivity extends Activity {

    private NfcHelper mNfcHelper;


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
     * Written by Stephen Kuehl
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        // Set up the NFC helper.
        try {
            mNfcHelper = new NfcHelper(this, getClass());
        } catch (NfcNotAvailableException | NfcNotEnabledException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {

            // Read the name of the item on the NFC tag.
            String msg = mNfcHelper.getNfcMessage(intent);
            ArrayList<Item> items = (ArrayList<Item>)ItemSerializer.deserializeItemList(msg);

            // Start the game with the items.
            Intent gameIntent = new Intent(this, GameActivity.class);
            gameIntent.putExtra("items", items);
            startActivity(gameIntent);
        }
    }
}
