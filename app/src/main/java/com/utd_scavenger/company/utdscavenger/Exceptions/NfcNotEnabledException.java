package com.utd_scavenger.company.utdscavenger.Exceptions;

public class NfcNotEnabledException extends Exception {
    public NfcNotEnabledException() {
        super("NFC is not enabled on this device. Please enable NFC.");
    }
}
