package com.utd_scavenger.company.utdscavenger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.utd_scavenger.company.utdscavenger.Data.Item;
import com.utd_scavenger.company.utdscavenger.Exceptions.NfcNotAvailableException;
import com.utd_scavenger.company.utdscavenger.Exceptions.NfcNotEnabledException;
import com.utd_scavenger.company.utdscavenger.Helpers.ItemSerializer;
import com.utd_scavenger.company.utdscavenger.Helpers.NfcHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the process of creating a new game. Specifically, adding the players.
 *
 * Written by Jonathan Darling and Stephen Kuehl
 */
public class CreateStepTwoActivity extends Activity {
    // Helpers.
    NfcHelper mNfcHelper;

    // Storage.
    ArrayList<Item> mItems;

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
        setContentView(R.layout.activity_create_steptwo);

        // Set up NfcHelper.
        try {
            mNfcHelper = new NfcHelper(this, getClass());
        } catch (NfcNotAvailableException | NfcNotEnabledException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Get the list of items form step 1.
        mItems = (ArrayList<Item>)getIntent().getSerializableExtra("items");

        // Re-serialize the items.
        String itemsSerialized = ItemSerializer.serializeItemList(mItems);
        List<Item> itemsTest = ItemSerializer.deserializeItemList(itemsSerialized);

        // Create the NDEF message to send to recipients.
        NdefMessage ndefMessage = mNfcHelper.createNdefMessage(itemsSerialized);
        mNfcHelper.setNdefPushMessage(ndefMessage);
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
        // read, it will immediately be processed by this activity.
        mNfcHelper.enableForegroundDispatch();
    }

    /**
     * Click listener for the Done button.
     *
     * @param view The view that was clicked.
     *
     * Written by Stephen Kuehl
     */
    public void onClickDone (View view){
        new AlertDialog.Builder(this)
                .setTitle("Start Game")
                .setMessage("Are you sure you have added all your players?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent myIntent = new Intent(getBaseContext(), MainActivity.class);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing.
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
