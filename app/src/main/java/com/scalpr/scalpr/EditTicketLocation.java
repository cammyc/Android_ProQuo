package com.scalpr.scalpr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.scalpr.scalpr.Helpers.AttractionHelper;
import com.scalpr.scalpr.Helpers.DatabaseHelper;
import com.scalpr.scalpr.Helpers.UserHelper;
import com.scalpr.scalpr.Objects.Attraction;
import com.scalpr.scalpr.Objects.AttractionSerializable;
import com.scalpr.scalpr.Objects.HttpResponseListener;
import com.scalpr.scalpr.Objects.User;

public class EditTicketLocation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Marker marker;
    SupportMapFragment mapFragment;

    AttractionHelper helper;
    UserHelper loginHelp;
    double lat, lon;
    Context c;
    DatabaseHelper dbHelper;

    private int SET_EDIT_LOCATION_ACTIVITY_RESULT = 2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_location_and_post_ticket);
        c = this;

        helper = new AttractionHelper(this);
        loginHelp = new UserHelper(this);
        dbHelper = new DatabaseHelper(this);

        Bundle b = getIntent().getBundleExtra("bundle");


        final long attractionID = b.getLong("attractionID");
        //final String imageURL = b.getString("imageURL");
        lat = b.getDouble("lat");
        lon = b.getDouble("lon");
        final User user = loginHelp.getLoggedInUser();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapSetSellLocation);
        mapFragment.getMapAsync(this);


        final HttpResponseListener postTicketResponse = new HttpResponseListener() {
            @Override
            public void requestStarted() {

            }

            @Override
            public void requestCompleted(String response) {
                if(response.equals("1")){
//                    AttractionSerializable a = new AttractionSerializable();
//
//                    a.setVenueName(venueName);
//                    a.setName(attractionName);
//                    a.setTicketPrice(Double.parseDouble(ticketPrice));
//                    a.setNumTickets(Integer.parseInt(numberOfTickets));
//                    a.setDescription(description);
//                    a.setDate(date);
//                    a.setImageURL(imageURL);
//                    a.setUser(user);
//                    a.setLat(lat);
//                    a.setLon(lon);
//
//                    Bundle b = new Bundle();
//                    b.putSerializable("attraction",a);
//
//                    Intent data = new Intent();
//                    data.putExtra("bundle",b);
//
//                    setResult(1,data);
                    AttractionSerializable a = new AttractionSerializable();
                    a.setLon(lon);
                    a.setLat(lat);
                    a.setID(attractionID);

                    Attraction attrForDB = new Attraction();
                    attrForDB.copyAttractionSerializable(a);

                    boolean inDB = dbHelper.updateAttractionLocation(attrForDB);

                    Bundle b = new Bundle();
                        b.putSerializable("attraction",a);
                        b.putBoolean("updatedSQLite", inDB);

                    Intent data = new Intent();
                        data.putExtra("bundle",b);

                    setResult(SET_EDIT_LOCATION_ACTIVITY_RESULT,data);
                    Toast.makeText(getApplicationContext(),"Successfully updated marker location!", Toast.LENGTH_LONG).show();
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(),"Error posting tickets. Please try again.", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Unable to post tickets. Please check your internet connection.", Toast.LENGTH_LONG).show();
            }
        };

        Button bPostTicket = (Button) findViewById(R.id.bSetLocationAndPostTicket);
            bPostTicket.setText("Update Marker Location");
        bPostTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.UpdateAttractionLocationRequest(postTicketResponse, user.getUserID(), attractionID, lat, lon);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            LatLng myLocation = new LatLng(lat, lon);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(myLocation)      // Sets the center of the map to location user
                    .zoom(16)                    // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            marker = mMap.addMarker(new MarkerOptions().position(myLocation).title("Hold and drag to move").draggable(true));

            //BitmapHelper bmHelper = new BitmapHelper(c);
            //GET IMAGE


            lat = myLocation.latitude;//CameraMovelistener may not be called if they dont move map
            lon = myLocation.longitude;

            googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                @Override
                public void onCameraMove() {
                    LatLng center = mMap.getCameraPosition().target;
                    marker.setPosition(center);

                    lat = center.latitude;
                    lon = center.longitude;
                }
            });

        } catch (Exception ex) {

        }
    }

}