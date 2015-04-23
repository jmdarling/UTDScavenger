package com.utd_scavenger.company.utdscavenger;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;


public class CreateActivityStepTwo extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_stepone);

    }


    /**
     * onClick listener that responds when a player taps "Add New Player."
     * This class will first open a dialog that will allow the user to tap and add
     * new players. A listview within the dialog will display showing the newly added
     * players.
     *
     * @param view
     *
     * Written by Stephen Kuehl
     */
    public void onClickPlayers (View view) {




    }

}
