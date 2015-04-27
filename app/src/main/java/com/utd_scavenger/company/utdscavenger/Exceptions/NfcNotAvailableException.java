package com.utd_scavenger.company.utdscavenger.Exceptions;

/**
 * An exception that is thrown when NFC is not available on a device.
 *
 * Written by Jonathan Darling
 */
public class NfcNotAvailableException extends Exception {

    /**
     * Constructor.
     *
     * Written by Jonathan Darling
     */
    public NfcNotAvailableException() {
        super("NFC is not available on this device.");
    }
}
