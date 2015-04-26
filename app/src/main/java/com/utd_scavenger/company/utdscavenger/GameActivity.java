package com.utd_scavenger.company.utdscavenger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.utd_scavenger.company.utdscavenger.Data.Item;
import com.utd_scavenger.company.utdscavenger.Exceptions.NfcNotAvailableException;
import com.utd_scavenger.company.utdscavenger.Exceptions.NfcNotEnabledException;
import com.utd_scavenger.company.utdscavenger.Helpers.NfcHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Primary game screen.
 *
 * Written by Jonathan Darling and Stephen Kuehl
 */
public class GameActivity extends Activity implements OnMapReadyCallback {
    // Adapters.
    private ArrayAdapter<String> mFoundItemsNamesAdapter;
    private ArrayAdapter<String> mNotFoundItemsNamesAdapter;

    // Bound UI elements.
    private Map<String, Marker> mMapMarkers;

    // Helpers.
    private NfcHelper mNfcHelper;

    // Storage.
    private ArrayList<Item> mItems;
    private ArrayList<Item> mFoundItems;
    private ArrayList<String> mFoundItemsNames;
    private ArrayList<Item> mNotFoundItems;
    private ArrayList<String> mNotFoundItemsNames;

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
        setContentView(R.layout.activity_game);

        // Initialization.
        mFoundItems = new ArrayList<>();
        mFoundItemsNames = new ArrayList<>();
        mNotFoundItems = new ArrayList<>();
        mNotFoundItemsNames = new ArrayList<>();
        mMapMarkers = new HashMap<>();

        // Set up NfcHelper.
        try {
            mNfcHelper = new NfcHelper(this, getClass());
        } catch (NfcNotAvailableException | NfcNotEnabledException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();

            // We cannot do anything without NFC, redirect back to the main
            // activity.
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        // Set up adapters.
        mFoundItemsNamesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mFoundItemsNames);
        mNotFoundItemsNamesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mNotFoundItemsNames);

        // Set up the map.
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Get the list of items from the intent.
        mItems = (ArrayList) getIntent().getSerializableExtra("items");

        // The user hasn't found any items when they first start the game so
        // populate the notFoundItems list.
        if (mItems != null) {
            mNotFoundItems.addAll(mItems);
        }
        updateItemsNamesListView();
    }

    /**
     * Called after onRestoreInstanceState, onRestart, or onPause.
     * Enables foreground dispatch.
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
     * Called when an NFC tag is read. This handles the high level tasks
     * involved in reading an NFC tag. In our specific case, this should be
     * called by scanning an NFC tag with an item name.
     *
     * @param intent The intent that was started.
     *
     * Written by Jonathan Darling and Stephen Kuehl
     */
    protected void onNewIntent(Intent intent) {
        // Check to see if the Activity was started by an NFC tag being read.
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {

            // Read the name of the item on the NFC tag.
            String name = mNfcHelper.getNfcMessage(intent);

            if (!name.isEmpty()) {
                // "Collect" the item specified by the NFC tag.
                collectItem(name);
            }
        }
    }

    /**
     * Called when the map is ready to be used.
     *
     * @param map A non-null instance of a GoogleMap associated with the
     *                  MapFragment or MapView that defines the callback.
     *
     *  Written by Jonathan Darling
     */
    @Override
    public void onMapReady(GoogleMap map) {
        // Enable the user to be able to see their location.
        map.setMyLocationEnabled(true);

        // Set the starting view and zoom level. This will center on UTD.
        LatLng utdLocation = new LatLng(32.9837395, -96.7511764);
        int zoom = 15;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(utdLocation, zoom));

        // Add waypoints for items.
        if (mItems != null) {
            Marker marker;
            for (Item item : mItems) {
                marker = map.addMarker(new MarkerOptions()
                                .position(new LatLng(item.getLatitude(), item.getLongitude()))
                                .title(item.getName())
                );

                // Save the markers to a Map so they may be deleted later.
                mMapMarkers.put(item.getName(), marker);
            }
        }
    }

    /**
     * Called when the activity has detected the user's press of the back key.
     *
     * Written by Jonathan Darling
     */
    @Override
    public void onBackPressed() {

        // Create a dialog that will let the user know that this will quit
        // quit the game.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This will quit your current game. Are you sure you want to quit?")
                .setTitle("Quit game?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    /**
                     * This method will be invoked when a button in the dialog
                     * is clicked.
                     *
                     * @param dialog The dialog that received the click.
                     * @param which The button that was clicked.
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(GameActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    /**
                     * This method will be invoked when a button in the dialog
                     * is clicked.
                     *
                     * @param dialog The dialog that received the click.
                     * @param which The button that was clicked.
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing.
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Collect the item specified by the name. This will change the item's
     * status from not being found to found as will be reflected in the found
     * and not found lists.
     *
     * @param name The name of the item that was collected.
     *
     * Written by Jonathan Darling and Stephen Kuehl
     */
    private void collectItem(String name) {
        // Find the item in the items list.
        if (mItems != null) {
            Item foundItem = null;

            for (Item item : mItems) {
                if (item.getName().equals(name)) {
                    foundItem = item;
                    break;
                }
            }

            if (foundItem != null) {
                // Update the found items lists.
                mNotFoundItems.remove(foundItem);
                mFoundItems.add(foundItem);
                updateItemsNamesListView();

                // Remove the marker corresponding to this item.
                Marker marker = mMapMarkers.get(name);
                marker.remove();

                Toast.makeText(this, "Collected item: " + name, Toast.LENGTH_SHORT).show();

                // If the user has finished finding all of the items, let them
                // know and allow them to leave the game.
                if (mNotFoundItems.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("You have found all of the items! Return to the game host.")
                            .setTitle("Congratulations!")
                            .setPositiveButton("Leave Game", new DialogInterface.OnClickListener() {

                                /**
                                 * This method will be invoked when a button in the dialog
                                 * is clicked.
                                 *
                                 * @param dialog The dialog that received the click.
                                 * @param which The button that was clicked.
                                 */
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(GameActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        }
    }

    /**
     * Updates the ListViews that display the found and not found items.
     *
     * Written by Jonathan Darling
     */
    private void updateItemsNamesListView() {
        mNotFoundItemsNames.clear();
        mFoundItemsNames.clear();

        for (Item item : mNotFoundItems) {
            mNotFoundItemsNames.add(item.getName());
        }
        mNotFoundItemsNamesAdapter.notifyDataSetChanged();

        for (Item item : mFoundItems) {
            mFoundItemsNames.add(item.getName());
        }
        mFoundItemsNamesAdapter.notifyDataSetChanged();
    }

    /**
     * Click listener that displays the found items dialog.
     *
     * @param view The view that was clicked.
     *
     * Written by Jonathan Darling
     */
    public void foundItemsClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setAdapter(mFoundItemsNamesAdapter, null)
                .setTitle("Found items")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    /**
                     * This method will be invoked when a button in the dialog
                     * is clicked.
                     *
                     * @param dialog The dialog that received the click.
                     * @param which The button that was clicked.
                     *
                     * Written by Jonathan Darling
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing.
                    }
                });

        builder.create().show();
    }

    /**
     * Click listener that displays the not found items dialog.
     *
     * @param view The view that was clicked.
     *
     * Written by Jonathan Darling
     */
    public void notFoundItemsClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setAdapter(mNotFoundItemsNamesAdapter, null)
                .setTitle("Not found items")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    /**
                     * This method will be invoked when a button in the dialog
                     * is clicked.
                     *
                     * @param dialog The dialog that received the click.
                     * @param which The button that was clicked.
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing.
                    }
                });

        builder.create().show();
    }
}
