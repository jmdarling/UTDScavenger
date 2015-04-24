package com.utd_scavenger.company.utdscavenger;

import android.app.Activity;
import android.content.Intent;

import android.nfc.FormatException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.utd_scavenger.company.utdscavenger.Data.Item;
import com.utd_scavenger.company.utdscavenger.Exceptions.NfcNotAvailableException;
import com.utd_scavenger.company.utdscavenger.Exceptions.NfcNotEnabledException;
import com.utd_scavenger.company.utdscavenger.Helpers.NfcHelper;

import java.io.IOException;
import java.util.List;

/**
 * Handles the process of creating a new game.
 *
 * Written by Jonathan Darling and Stephen Kuehl
 */
public class CreateStepOneActivity extends Activity {
    // Helpers.
    private NfcHelper mNfcHelper;

    // Bound UI elements.
    private EditText mItemName;
    private ListView mItemsListView;

    // Storage.
    private List<Item> mItems;

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

        // Set up the NFC helper.
        try {
            mNfcHelper = new NfcHelper(this, getClass());
        } catch (NfcNotAvailableException | NfcNotEnabledException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Bind UI elements.
        mItemName = (EditText)findViewById(R.id.edit_name);
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
            // Ensure that we have text in the text field. If not, we don't want
            // to write to the tag.
            if (!mItemName.getText().toString().equals("")) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                try {
                    // Write to the tag.
                    mNfcHelper.write(mItemName.getText().toString(), tag);

                    // Clear the text field.
                    mItemName.setText("");

                    // Create a new item to correspond to the tag.
                    addItem(mItemName.getText().toString());

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

    /**
     * Add a new Item to our list of items.
     *
     * @param name The name of the item to add.
     */
    private void addItem(String name) {
        Toast.makeText(this, "Adding item...", Toast.LENGTH_LONG).show();

        Item item = new Item(name, latitude, longitude);
        System.out.println("Item created at latitude " + latitude + " and longitude " + longitude);
    }

    protected class AddItemTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {

        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected String doInBackground(String... params) {
            return null;
        }
    }
}
