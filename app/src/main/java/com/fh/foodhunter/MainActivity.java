package com.fh.foodhunter;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener {

    private static final int PROXIMITY_RADIUS = 3000;
    GoogleMap mMap;
    LocationManager lm;
    private RequestQueue mRequestQueue;
    private GoogleApiClient mGoogleApiClient;
    private LatLng latLng;
    private List<Restaurant> restaurantsList=new ArrayList<>();
    Button ref_button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ref_button=(Button)findViewById(R.id.ref_button);


        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            //Write log here
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            //Write log here
        }
        if(!isNetworkAvailable())
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(this.getResources().getString(R.string.network_not_enabled));
            dialog.setPositiveButton(this.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                }
            });
            dialog.show();
        }

        if (!gps_enabled && !network_enabled) {
            // notify user

                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage(this.getResources().getString(R.string.gps_network_not_enabled));
                dialog.setPositiveButton(this.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                        //get gps
                    }
                });
//            dialog.setNegativeButton(this.getString(R.string.Cancel), new DialogInterface.OnClickListener() {
//
//                @Override
//                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                    // TODO Auto-generated method stub
//
//                }
//            });

            dialog.show();




        }

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();


        if(isNetworkAvailable()&&gps_enabled&&network_enabled)
        {
            startDetection();
        }



        ref_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkAvailable())
                startDetection();
            }
        });


    }

    private void startDetection(){

        mGoogleApiClient.connect();
        mRequestQueue = Volley.newRequestQueue(this);

        placeDetection();

    }

    private boolean isNetworkAvailable() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }


    private void addMapMarkers() {
        for(int i=0;i<restaurantsList.size();i++)
        {
            MarkerOptions markerOptions= new MarkerOptions();
            markerOptions.title(restaurantsList.get(i).getRestaurantName()+":"+restaurantsList.get(i).getRatings());
            markerOptions.position(restaurantsList.get(i).getLatLng());
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher6teen));
            mMap.addMarker(markerOptions);
        }

    }


    private void placeDetection()  throws SecurityException {
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace( mGoogleApiClient, null );
        result.setResultCallback( new ResultCallback<PlaceLikelihoodBuffer>() {


            @Override
            public void onResult( PlaceLikelihoodBuffer likelyPlaces ) {

                PlaceLikelihood placeLikelihood = likelyPlaces.get( 0 );

                placeLikelihood.getPlace().getLatLng();

                latLng= placeLikelihood.getPlace().getLatLng();

                likelyPlaces.release();

                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
                loadNearByRestaurant(latLng.latitude,latLng.longitude);

            }
        });
        
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.getUiSettings().setCompassEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        mMap.getUiSettings().setMapToolbarEnabled(true);



    }



    public void loadNearByRestaurant(double latitude,double longitude)
    {
        String type="restaurant";
        StringBuilder googlePlacesUrl=
                new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=").append(latitude).append(",").append(longitude);
        googlePlacesUrl.append("&radius=").append(PROXIMITY_RADIUS);
        googlePlacesUrl.append("&types=").append(type);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=AIzaSyCUVqOPvqROX3wfEI2g2J0uLXelmIOYsCE");

        JsonObjectRequest request= new JsonObjectRequest(googlePlacesUrl.toString(),
                new Response.Listener<JSONObject>(){


                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            parseResponse(response);
                        } catch (JSONException e) {


                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener(){


                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }


        );
        mRequestQueue.add(request);


    }

    private void parseResponse(JSONObject response) throws JSONException {

        String id=null,place_name= null,vicinity=null;
        double latitude,longitude,icon,rating=3;

        try{

            JSONArray mJsonArray= response.getJSONArray("results");

            if(response.getString("status").equals("OK"))
            {

                for(int i=0;i<mJsonArray.length();i++)
                {
                    JSONObject place= mJsonArray.getJSONObject(i);

                    Restaurant r=new Restaurant();
                    r.setRestaurantName(place.getString("name"));
                    r.setRestaurantVicinity(place.getString("vicinity"));

                    latitude= place.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    longitude= place.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                    LatLng mLatLng= new LatLng(latitude,longitude);
                    r.setLatLng(mLatLng);
                    if(!place.isNull("rating")) {
                        r.setRatings(place.getDouble("rating"));
                    }
                    else
                    {
                        r.setRatings(3.0);
                    }
                    if(!place.isNull("opening_hours"))
                    {
                        r.setOpen(place.getJSONObject("opening_hours").getString("open_now"));
                    }else
                    {
                        r.setOpen("N.A");
                    }

                    restaurantsList.add(r);
                }
                addMapMarkers();

            } else if (response.getString("status").equalsIgnoreCase("ZERO_RESULTS")) {
                Toast.makeText(getBaseContext(), "No restaurants found in 3KM radius!!!",
                        Toast.LENGTH_LONG).show();
            }


        }catch (JSONException e)
        {
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
        }



    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(this,
                "Google Places API connection failed" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onResume() {

        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //if( mGoogleApiClient != null )
         //   mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();

    }
}
