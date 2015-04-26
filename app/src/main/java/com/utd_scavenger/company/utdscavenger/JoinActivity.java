package com.utd_scavenger.company.utdscavenger;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.utd_scavenger.company.utdscavenger.Data.Item;
import com.utd_scavenger.company.utdscavenger.Exceptions.NfcNotAvailableException;
import com.utd_scavenger.company.utdscavenger.Exceptions.NfcNotEnabledException;
import com.utd_scavenger.company.utdscavenger.Helpers.ItemSerializer;
import com.utd_scavenger.company.utdscavenger.Helpers.NfcHelper;

import java.util.ArrayList;

/**
 * Handles the process of joining an existing game.
 *
 * Written by Jonathan Darling and Stephen Kuehl
 */
public class JoinActivity extends Activity {
    // Helpers.
    private NfcHelper mNfcHelper;

    // Bound UI elements.
    private ProgressBar mProgressBar;

    /**
     * Called when the activity is starting.
     * Sets up an instance of NfcHelper.
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
        setContentView(R.layout.activity_join);

        // Set up NfcHelper.
        try {
            mNfcHelper = new NfcHelper(this, getClass());
        } catch (NfcNotAvailableException | NfcNotEnabledException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Bind UI elements.
        mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
    }

    /**
     * Called after onRestoreInstanceState, onRestart, or onPause.
     * Enables foreground dispatch.
     *
     * Written by Jonathan Darling and Stephen Kuehl
     */
    @Override
    public void onResume() {
        super.onResume();

        // Enable foreground dispatch. This will ensure that when the NFC tag is
        // read, it will immediately be processed by this activity instead of
        // allowing the user to choose where to route it.
        mNfcHelper.enableForegroundDispatch();
    }

    /**
     * This is called for activities that set launchMode to "singleTop" in their
     * package, or if a client used the Intent.FLAG_ACTIVITY_SINGLE_TOP flag
     * when calling startActivity.
     * Called when an NFC tag is read. This handles the high level tasks
     * involved in reading an NFC tag. In our specific case, this should be
     * called by an AndroidBeam message with game data.
     *
     *
     * @param intent The intent that was started.
     *
     * Written by Jonathan Darling and Stephen Kuehl
     */
    protected void onNewIntent(Intent intent) {
        // Check to see if the Activity was started by an NFC tag being read.
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {

            // Get the items sent via AndroidBeam.
            String message = mNfcHelper.getNfcMessage(intent);
            if (!message.isEmpty()) {
                ArrayList<Item> items = (ArrayList<Item>)ItemSerializer.deserializeItemList(message);

                // Show the progress bar as the game load can take a few
                // seconds.
                mProgressBar.setVisibility(View.VISIBLE);

                // Start the game with the items.
                Intent gameIntent = new Intent(this, GameActivity.class);
                gameIntent.putExtra("items", items);
                startActivity(gameIntent);
            } else {
                Toast.makeText(this, "Unfortunately, we were unable to join this game. Please try again.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
