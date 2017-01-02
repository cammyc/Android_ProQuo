package com.scalpr.scalpr;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
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
import com.scalpr.scalpr.Helpers.UserHelper;
import com.scalpr.scalpr.Objects.AttractionSerializable;
import com.scalpr.scalpr.Objects.HttpResponseListener;
import com.scalpr.scalpr.Objects.User;

public class SetLocationAndPostTicket extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Marker marker;
    SupportMapFragment mapFragment;

    AttractionHelper helper;
    UserHelper loginHelp;
    double lat, lon;
    Context c;

    Button bPostTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_location_and_post_ticket);
        c = this;

        helper = new AttractionHelper(this);
        loginHelp = new UserHelper(this);

        Bundle b = getIntent().getBundleExtra("bundle");
        final String venueName = b.getString("venueName");
        final String attractionName = b.getString("attractionName");
        final String ticketPrice = b.getString("ticketPrice");
        final String numberOfTickets = b.getString("numberOfTickers");
        final String description = b.getString("description");
        final String date = b.getString("Date");
        final String imageURL = b.getString("imageURL");
        lat = b.getDouble("lat");
        lon = b.getDouble("lon");
        final User user = loginHelp.getLoggedInUser();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapSetSellLocation);
        mapFragment.getMapAsync(this);

        bPostTicket = (Button) findViewById(R.id.bSetLocationAndPostTicket);


        final HttpResponseListener postTicketResponse = new HttpResponseListener() {
            @Override
            public void requestStarted() {
                Toast.makeText(c, "Posting Ticket...",Toast.LENGTH_SHORT).show();
                bPostTicket.setVisibility(View.GONE);
            }

            @Override
            public void requestCompleted(String response) {
                bPostTicket.setVisibility(View.VISIBLE);
                if(!response.equals("0")){
                    AttractionSerializable a = new AttractionSerializable();
                    a.setCreatorID(user.getUserID());
                    a.setVenueName(venueName);
                    a.setName(attractionName);
                    a.setTicketPrice(Double.parseDouble(ticketPrice));
                    a.setNumTickets(Integer.parseInt(numberOfTickets));
                    a.setDescription(description);
                    a.setDate(date);
                    a.setImageURL(imageURL);
                    a.setUser(user);
                    a.setLat(lat);
                    a.setLon(lon);

                    Bundle b = new Bundle();
                    b.putSerializable("attraction",a);

                    Intent data = new Intent();
                    data.putExtra("bundle",b);

                    setResult(1,data);
                    finish();
                    Toast.makeText(getApplicationContext(),"Ticket Posted!", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Unable to post ticket. Please try again.", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                bPostTicket.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Unable to post ticket. Please try again.", Toast.LENGTH_LONG).show();
            }
        };

        bPostTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                helper.PostAttractionRequest(postTicketResponse, user.getUserID(), venueName, attractionName, ticketPrice, numberOfTickets, description, date, imageURL, marker.getPosition().latitude,  marker.getPosition().longitude);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }


        try {
                LatLng myLocation = new LatLng(lat, lon);

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(myLocation)      // Sets the center of the map to location user
                        .zoom(16)                    // Sets the orientation of the camera to east
                       // .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            marker = mMap.addMarker(new MarkerOptions().position(myLocation).title("Hold and drag to move").draggable(true));
            marker.setDraggable(true);


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