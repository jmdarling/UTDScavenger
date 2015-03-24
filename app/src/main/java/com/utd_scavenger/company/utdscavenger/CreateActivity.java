package com.utd_scavenger.company.utdscavenger;

import android.app.Activity;
import android.os.Bundle;

/**
 * Handles the process of creating a new game.
 *
 * Written by Jonathan Darling
 */
public class CreateActivity extends Activity {

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
     * Written by Jonathan Darling
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
    }
}
