package com.utd_scavenger.company.utdscavenger.Exceptions;

public class NfcNotAvailableException extends Exception {
    public NfcNotAvailableException() {
        super("NFC is not available on this device.");
    }
}
