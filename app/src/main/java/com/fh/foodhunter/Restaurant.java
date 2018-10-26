package com.fh.foodhunter;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Mehul on 10/23/2016.
 */
public class Restaurant {

    private String mRestaurantName;
    private String mRestaurantVicinity;
    private String mOpen;
    private double mRatings;
    private LatLng mLatLng;

    public LatLng getLatLng() {
        return mLatLng;
    }

    public void setLatLng(LatLng latLng) {
        mLatLng = latLng;
    }

    public String getOpen() {
        return mOpen;
    }

    public void setOpen(String open) {
        mOpen = open;
    }

    public String getRestaurantName() {
        return mRestaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        mRestaurantName = restaurantName;
    }

    public String getRestaurantVicinity() {
        return mRestaurantVicinity;
    }

    public void setRestaurantVicinity(String restaurantVicinity) {
        mRestaurantVicinity = restaurantVicinity;
    }



    public double getRatings() {
        return mRatings;
    }

    public void setRatings(double ratings) {
        mRatings = ratings;
    }
}
