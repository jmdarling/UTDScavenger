package com.utd_scavenger.company.utdscavenger.Helpers;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcF;
import android.os.Parcelable;
import android.widget.Toast;

import com.utd_scavenger.company.utdscavenger.Exceptions.NfcNotAvailableException;
import com.utd_scavenger.company.utdscavenger.Exceptions.NfcNotEnabledException;

import java.io.IOException;

public class NfcHelper {
    private Activity mActivity;
    private Class mClass;
    private NfcAdapter mNfcAdapter;

    public NfcHelper(Activity activity, Class parentClass) throws NfcNotAvailableException, NfcNotEnabledException {
        this.mActivity = activity;
        this.mClass = parentClass;
        mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);

        // Check to see if the device supports NFC.
        if (mNfcAdapter == null) {
            Toast.makeText(activity, "NFC is not available on this device.", Toast.LENGTH_LONG).show();
            throw new NfcNotAvailableException();
        } else if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(activity, "NFC is not enabled on this device. Please enable NFC.", Toast.LENGTH_LONG).show();
            throw new NfcNotEnabledException();
        }
    }

    /**
     * Enable foreground dispatch on the provided NfcAdapter.
     */
    public void enableForegroundDispatch() {
        mNfcAdapter.enableForegroundDispatch(mActivity, createPendingIntent(), createIntentFilter(), createTechListArray());
    }

    /**
     * Set the NDEF Push Message for the NFC adapter.
     *
     * @param ndefMessage The NDEF Push Message to set.
     */
    public void setNdefPushMessage(NdefMessage ndefMessage) {
        mNfcAdapter.setNdefPushMessage(ndefMessage, mActivity);
    }

    /**
     * Create a PendingIntent for NFC tag reading. This intent will be ran when
     * an NFC tag is scanned.
     *
     * Written by Jonathan Darling and Stephen Kuehl
     */
    private PendingIntent createPendingIntent() {

        // Code to identify the origin activity. This intent will be run from
        // this very activity so is not needed.
        int requestCode = 0;

        // Special handling flags. Not needed for this use case.
        int flags = 0;

        // Create a new intent for this class.
        Intent nfcIntent = new Intent(mActivity, mClass);

        // Ensure that the activity is not restarted if it is already running.
        nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Create a pending intent from the intent we already created.
        return PendingIntent.getActivity(mActivity, requestCode, nfcIntent, flags);
    }

    /**
     * Create an Intent Filter limited to the URI or MIME type to intercept TAG
     * scans from.
     *
     * Written by Jonathan Darling and Stephen Kuehl
     */
    private IntentFilter[] createIntentFilter() {

        // Limit our scanner to only read tags with NDEF payloads.
        IntentFilter tagIntentFilter = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        return new IntentFilter[] { tagIntentFilter };
    }

    /**
     * Create an array of technologies to handle.
     *
     * Written by Jonathan Darling and Stephen Kuehl
     */
    private String[][] createTechListArray() {
        return new String[][] {
                new String[] {
                        NfcF.class.getName()
                }
        };
    }

    public void write(String text, Tag tag) throws IOException, FormatException {

        NdefRecord[] records = { createRecord(text) };
        NdefMessage message = new NdefMessage(records);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        ndef.writeNdefMessage(message);
        ndef.close();
    }

    public NdefMessage createNdefMessage(String text) {
        NdefRecord[] records = { createRecord(text) };
        return new NdefMessage(records);
    }

    private NdefRecord createRecord(String text) {
        String mimeType = "text/utdscavenger";

        byte[] textBytes = text.getBytes();
        byte[] mimeTypeBytes = mimeType.getBytes();

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeTypeBytes, new byte[0], textBytes);
        return recordNFC;
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
    public String getNfcMessage(Intent intent) {

        // Read any NDEF messages present on the tag.
        Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        // Get the first message.
        NdefMessage message = (NdefMessage)messages[0];

        // Get the first record.
        NdefRecord record = message.getRecords()[0];

        // Return the message stored in the first record.
        return new String(record.getPayload());
    }
}
