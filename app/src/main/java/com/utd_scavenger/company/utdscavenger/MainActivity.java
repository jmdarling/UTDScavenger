/**
 * This application is used to play a scavenger hunt game on the UTD campus. A host will create a
 * game and clients can join that game. The host will scan in nfc tagged items that the clients will
 * have to find. The game is over when all items have been found. The winner is the client with the
 * most items collected.
 *
 * Created by:
 *  Jonathan Darling - jxd128130
 *  Stephen Kuehl - skuehl
 *
 * Development started:
 *  23 March 2015.
 *
 * Written for:
 *  Class Project - CS4V95.015 Undergraduate Topics in Computer Science
 */

package com.utd_scavenger.company.utdscavenger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 * Intro activity for the application. This is where the user can decide to
 * join, create, or edit a game.
 *
 * Written by Jonathan Darling
 */
public class MainActivity extends Activity {

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
        setContentView(R.layout.activity_main);
    }

    /**
     * Click listener for the join, create, and edit buttons. Clicking these
     * buttons redirect to their respective activities.
     *
     * @param view The view that was clicked.
     *
     * Written by Jonathan Darling
     */
    public void onClick(View view) {
        int buttonClicked = view.getId();

        // Check which button was clicked and start the button's corresponding
        // activity.
        switch (buttonClicked) {
            case R.id.join_button:
                Intent joinIntent = new Intent(this, JoinActivity.class);
                startActivity(joinIntent);
                break;

            case R.id.create_button:
                Intent createIntent = new Intent(this, CreateActivity.class);
                startActivity(createIntent);
                break;

            case R.id.edit_button:
                Intent editIntent = new Intent(this, EditActivity.class);
                startActivity(editIntent);
                break;
        }
    }

}
