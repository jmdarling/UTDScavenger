package com.utd_scavenger.company.utdscavenger.Exceptions;

/**
 * An exception that is thrown when NFC is not enabled on a device.
 *
 * Written by Jonathan Darling
 */
public class NfcNotEnabledException extends Exception {

    /**
     * Constructor.
     *
     * Written by Jonathan Darling
     */
    public NfcNotEnabledException() {
        super("NFC is not enabled on this device. Please enable NFC.");
    }
}
