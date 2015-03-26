package com.utd_scavenger.company.utdscavenger;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

/**
 * Primary game screen.
 *
 * Written by Jonathan Darling
 */
public class GameActivity extends Activity implements OnMapReadyCallback{

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
        setContentView(R.layout.activity_game);

        // Get the map.
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Called when the map is ready to be used.
     *
     * @param googleMap A non-null instance of a GoogleMap associated with the MapFragment or
     *                  MapView that defines the callback.
     *
     *  Written by Jonathan Darling
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Show the user's location.
        googleMap.setMyLocationEnabled(true);

        // Set the starting view and zoom level.
        LatLng utd = new LatLng(32.9837395, -96.7511764);
        int zoom = 15;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(utd, zoom));
    }
}
