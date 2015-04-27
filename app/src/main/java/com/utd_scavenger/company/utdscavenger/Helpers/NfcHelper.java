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

import com.utd_scavenger.company.utdscavenger.Exceptions.NfcNotAvailableException;
import com.utd_scavenger.company.utdscavenger.Exceptions.NfcNotEnabledException;

import java.io.IOException;

/**
 * Helper class to perform NFC Related tasks.
 *
 * Written by Jonathan Darling and Stephen Kuehl
 */
public class NfcHelper {
    private Activity mActivity;
    private Class mClass;
    private NfcAdapter mNfcAdapter;

    /**
     * Constructor.
     *
     * @param activity The activity the helper will be used in.
     * @param parentClass The class the helper will be used in.
     *
     * @throws NfcNotAvailableException
     * @throws NfcNotEnabledException
     *
     * Written by Jonathan Darling
     */
    public NfcHelper(Activity activity, Class parentClass) throws NfcNotAvailableException, NfcNotEnabledException {
        this.mActivity = activity;
        this.mClass = parentClass;
        mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);

        // Check to see if the device supports NFC.
        if (mNfcAdapter == null) {
            throw new NfcNotAvailableException();
        } else if (!mNfcAdapter.isEnabled()) {
            throw new NfcNotEnabledException();
        }
    }

    /**
     * Enable foreground dispatch on the NfcAdapter.
     *
     * Written by Jonathan Darling
     */
    public void enableForegroundDispatch() {
        mNfcAdapter.enableForegroundDispatch(mActivity, createPendingIntent(), createIntentFilter(), createTechListArray());
    }

    /**
     * Set the NDEF Push Message for the NfcAdapter.
     *
     * @param ndefMessage The NDEF Push Message to set.
     *
     * Written by Jonathan Darling
     */
    public void setNdefPushMessage(NdefMessage ndefMessage) {
        mNfcAdapter.setNdefPushMessage(ndefMessage, mActivity);
    }

    /**
     * Create a PendingIntent for NFC tag reading. This intent will be ran when
     * a NFC tag is scanned.
     *
     * @return The pending intent that will be ran when a NFC tag is scanned.
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
     * @return An array of intent filters.
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
     * @return An array of technologies to handle.
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

    /**
     * Write the provided text to the provided NFC tag.
     *
     * @param text The text to write to the tag.
     * @param tag The tag to be written to.
     *
     * @throws IOException
     * @throws FormatException
     *
     * Written by Jonathan Darling
     */
    public void write(String text, Tag tag) throws IOException, FormatException {

        NdefRecord[] records = { createRecord(text) };
        NdefMessage message = new NdefMessage(records);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        ndef.writeNdefMessage(message);
        ndef.close();
    }

    /**
     * Creates a NDEF message from the provided text.
     *
     * @param text The text to create the NDEF message from.
     *
     * @return The NDEF message created from the provided text.
     *
     * Written by Jonathan Darling
     */
    public NdefMessage createNdefMessage(String text) {
        NdefRecord[] records = { createRecord(text) };
        return new NdefMessage(records);
    }

    /**
     * Creates a NDEF record from the provided text.
     *
     * @param text The text to create the NDEF record from.
     *
     * @return The NDEF record created from the provided text.
     *
     * Written by Jonathan Darling
     */
    private NdefRecord createRecord(String text) {
        String mimeType = "text/utdscavenger";

        byte[] textBytes = text.getBytes();
        byte[] mimeTypeBytes = mimeType.getBytes();

        return new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeTypeBytes, new byte[0], textBytes);
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
        String mime = new String(record.getType());
        if (mime.equals("text/utdscavenger")) {
            return new String(record.getPayload());
        } else {
            return "";
        }
    }
}
