package com.utd_scavenger.company.utdscavenger;

/**
 * Class to represent a real life "item", specified by its name and location.
 *
 * Written by Jonathan Darling
 */
public class Item {

    private String name;
    private double latitude;
    private double longitude;

    /**
     * Constructor.
     *
     * @param name The name of the item.
     * @param latitude The latitude of the item.
     * @param longitude The longitude of the item.
     */
    public Item (String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
