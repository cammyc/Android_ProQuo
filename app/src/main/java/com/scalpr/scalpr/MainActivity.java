package com.scalpr.scalpr;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.firebase.iid.FirebaseInstanceId;
import com.lapism.searchview.SearchAdapter;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;
import com.scalpr.scalpr.Adapters.MarkerInfoWindowAdapter;
import com.scalpr.scalpr.Adapters.RecyclerItemClickListener;
import com.scalpr.scalpr.Adapters.RecyclerViewImageAdapter;
import com.scalpr.scalpr.BackgroundService.ConversationUpdateService;
import com.scalpr.scalpr.BackgroundService.MyFirebaseInstanceIDService;
import com.scalpr.scalpr.Helpers.AttractionHelper;
import com.scalpr.scalpr.Helpers.BingImageSearchHelper;
import com.scalpr.scalpr.Helpers.ConversationHelper;
import com.scalpr.scalpr.Helpers.DatabaseHelper;
import com.scalpr.scalpr.Helpers.DecimalDigitsInputFilter;
import com.scalpr.scalpr.Helpers.InputFilterMinMax;
import com.scalpr.scalpr.Helpers.UserHelper;
import com.scalpr.scalpr.Helpers.BitmapHelper;
import com.scalpr.scalpr.Helpers.MiscHelper;
import com.scalpr.scalpr.Objects.Attraction;
import com.scalpr.scalpr.Objects.AttractionSerializable;
import com.scalpr.scalpr.Objects.HttpResponseListener;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends BaseActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMarkerClickListener {


    private EditText etVenueName, etAttractionName,etAttractionPrice,etNumberOfTickets, etAttractionDatePicker,etAttractionDescription,etAttractionImageSearch;
    ProgressBar pbInitialLoader;
    private String selectedImageURL;
    private HttpResponseListener imageResponseListener, getInitialAttractionListener, getNewAttractionsListener, getNewSearchAttractionsListener, getConversationInfoListener, checkVersionListener;
    private String[] imageURLs;
    private String selectedAttractionName, selectedVenueName, selectedAttractionPrice, searchViewQuery;

    GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private int SET_LOCATION_AND_POST_TICKET_CODE_SUCCESS = 1;
    LatLng userLocation;
    Location mLastLocation;
    Location mCurrentLocation;
    Context c;

    AttractionHelper attractionHelper;
    BitmapHelper markerHelper;
    DatabaseHelper dbHelper;
    ConversationHelper convoHelper;

    boolean initialAttractionRequestCalled = false;

    private static final String TAG = "MyFirebaseIIDService";



    //STORE MARKERS IN LOCAL SQLITE DB, when bounds change get new markers that haven't already been download and query data from local db that way markers dont need to be cleared
    //also cache bitmap images for markers


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            mMap.setMyLocationEnabled(true);

            createLocationRequest();

            if (mLastLocation != null) {
                if(userLocation == null) {//only want to call initializeMapData when app opens
                    userLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    //mGoogleApiClient.disconnect();//not needed anymore
                    initializeMapData();
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        if (userLocation == null){
            userLocation = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
            initializeMapData();
        }else{
            userLocation = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
        }

       // stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        if(mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //APP SHOULD STORE LAST KNOWN LOCATION OF USER AND GET NEARBY ATTRACTIONS
    }

    protected void createLocationRequest() {
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(15000);
            mLocationRequest.setFastestInterval(10000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                            builder.build());
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c = this;
        searchViewQuery = "";
        setContentView(R.layout.activity_main);

        //JobManager.create(this).addJobCreator(new ConversationJobCreator());
        masterInitialize();


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        }

    }

    private void masterInitialize(){
        checkMinimumVersion();

        pbInitialLoader = (ProgressBar) findViewById(R.id.pbInitialLoader);

        initializeListenersAndHelpers();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }



        userIsLoggedIn = loginHelp.isUserLoggedIn();

        if(userIsLoggedIn){
            loggedInUser = loginHelp.getLoggedInUser();
            String token = FirebaseInstanceId.getInstance().getToken();

            if(token != null){
                loginHelp.updateTokenIfNecessary(token, loggedInUser.getUserID());
            }

            GoogleApiAvailability api = GoogleApiAvailability.getInstance();
            int errorCheck = api.isGooglePlayServicesAvailable(this);
            if(errorCheck == ConnectionResult.SUCCESS) {
                //google play services available, hooray
                ConversationUpdateService.scheduleRepeat(c);
            }
        }

        initializeMapFragment();

        setSearchView();

        setFab();

        mSearchView.setNavigationIconArrowHamburger();
        mSearchView.setOnMenuClickListener(new SearchView.OnMenuClickListener() {
            @Override
            public void onMenuClick() {
                mDrawerLayout.openDrawer(GravityCompat.START); // finish();
            }
        });
        mSearchView.setOnVoiceClickListener(new SearchView.OnVoiceClickListener() {
            @Override
            public void onVoiceClick() {
                //perm(Manifest.permission.RECORD_AUDIO, 0);
            }
        });


        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            String previousText = "";
            @Override
            public boolean onQueryTextChange(String newText) {
                searchViewQuery = newText;
                if(newText.equals("")){//previousText.length() > 1 &&  not needed because going to query local database
                    if(mMap != null){
                        clearMapForSearch();
                        setMarkers(dbHelper.getAttractionsFromDB(newText));
                        VisibleRegion bounds = mMap.getProjection().getVisibleRegion();
                        String IDs = dbHelper.getCommaSepIDsFromDB();
                        attractionHelper.getNewAttractionsRequest(getNewAttractionsListener, bounds.latLngBounds.southwest.latitude, bounds.latLngBounds.northeast.latitude, bounds.latLngBounds.northeast.longitude, bounds.latLngBounds.southwest.longitude, searchViewQuery,IDs);
                    }
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                if(mMap != null){
                    getData(query, 0);
                    clearMapForSearch();
                    ArrayList<Attraction> attractions = dbHelper.getAttractionsFromDB(query.trim());
                    if(attractions.size() > 0){
                        CameraUpdate centerOnMarker = CameraUpdateFactory.newLatLng(new LatLng(attractions.get(0).getLat(),attractions.get(0).getLon()));
                        mMap.animateCamera(centerOnMarker);
                        setMarkers(attractions);
                    }else{
                        VisibleRegion bounds = mMap.getProjection().getVisibleRegion();
                        String IDs = dbHelper.getCommaSepIDsFromDB();
                        attractionHelper.getNewAttractionsRequest(getNewSearchAttractionsListener, bounds.latLngBounds.southwest.latitude, bounds.latLngBounds.northeast.latitude, bounds.latLngBounds.northeast.longitude, bounds.latLngBounds.southwest.longitude, searchViewQuery,IDs);
                        pbInitialLoader.setVisibility(View.VISIBLE);
                    }

                    mSearchView.close(false);
                }

                return true;
            }
        });
    }


    @Override
    protected void onPause() {
        if(mGoogleApiClient != null) {
            stopLocationUpdates();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        if(!userIsLoggedIn){//if user isn't logged in, check to see if they now are, if they are then they came from login page and refresh;
            boolean isNowLoggedIn = loginHelp.isUserLoggedIn();
            if(isNowLoggedIn){
                recreate();//restarts activity when coming from login
                Log.d("ACTIVITY_RESULT","test");
            }
        }

        updateMenuHeader(loginHelp.getLoggedInUser());

        if(DatabaseHelper.hasdbUpdatedSinceLastCheck()){
            if(mMap != null){
                markerHelper.cancelAndClearTasks();
                mMap.clear();
                setMarkers(dbHelper.getAttractionsFromDB(""));
            }
        }
    }

    private void terms(){
        SharedPreferences sharedPref = c.getSharedPreferences(c.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if(!sharedPref.getBoolean("acceptedTerms", false)) {


            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    context);

            // set title
            alertDialogBuilder.setTitle("Terms of Service");

            // set dialog message
            alertDialogBuilder
                    .setMessage("You must agree to ProQuo's Terms of Service to continue using the app.")
                    .setNegativeButton("View Terms of Service", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton("I agree", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    })
                    .setCancelable(false);

            // create alert dialog
            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor sharedPref = c.getSharedPreferences(c.getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();
                    sharedPref.putBoolean("acceptedTerms", true);
                    sharedPref.commit();
                    alertDialog.dismiss();
                }
            });

            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.proquoapp.com/help/policies/terms_of_service.html"));
                    startActivity(browserIntent);
                }
            });
        }
    }

    private void checkMinimumVersion(){
        checkVersionListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {
            }

            @Override
            public void requestCompleted(String response) {
               try{
                   double minVersion = Double.parseDouble(response);
                   PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                   double currentVersion = Double.parseDouble(packageInfo.versionName);

                   if(currentVersion < minVersion){
                       AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                               context);

                       // set title
                       alertDialogBuilder.setTitle("Update Required");

                       // set dialog message
                       alertDialogBuilder
                               .setMessage("An update is required to continue using ProQuo. Please close the app and update it in the Google Play Store.")
                               .setPositiveButton("Update Now", new DialogInterface.OnClickListener() {
                                   public void onClick(DialogInterface dialog, int id) {

                                   }
                               })
                               .setCancelable(false);

                       // create alert dialog
                       final AlertDialog alertDialog = alertDialogBuilder.create();
                       alertDialog.show();

                       alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {

                               final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                               try {
                                   startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                               } catch (android.content.ActivityNotFoundException anfe) {
                                   startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                               }

                               alertDialog.dismiss();
                               finish();
                           }
                       });
                   }else{
                       terms();//no need for terms if app is to be updated
                   }

               }catch (Exception ex){
                   Log.d("MIN_VERSION", ex.toString());
                   terms();
               }

            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                terms();
            }
        };

        MiscHelper miscHelper = new MiscHelper();
        miscHelper.checkForUpdateRequest(checkVersionListener, c);

    }

    @Override
    protected void onStart() {
        if(mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if(mGoogleApiClient != null) {
            stopLocationUpdates();
        }
        super.onStop();
    }

    private void initializeListenersAndHelpers(){
        loginHelp = new UserHelper(this);

        attractionHelper = new AttractionHelper(this);
        markerHelper  = new BitmapHelper(this);
        dbHelper = new DatabaseHelper(this, true);
        convoHelper = new ConversationHelper(this);

        getInitialAttractionListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {
            }

            @Override
            public void requestCompleted(String response) {
                pbInitialLoader.setVisibility(View.GONE);
                initialAttractionRequestCalled = true;
                ArrayList<Attraction> attractions = attractionHelper.getAttractions(response);
                dbHelper.addAttractionsToDB(attractions);
                setMarkers(attractions);

                List<SearchItem> suggestionsList = dbHelper.getSearchSuggestions();

                SearchAdapter searchAdapter = new SearchAdapter(c, suggestionsList);
                searchAdapter.addOnItemClickListener(new SearchAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        TextView textView = (TextView) view.findViewById(R.id.textView_item_text);
                        String query = textView.getText().toString();
                        getData(query, position);
                        mSearchView.setQuery(query.trim());
                        mSearchView.close(false);
                    }
                });
                mSearchView.invalidate();
                mSearchView.setAdapter(searchAdapter);

            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                Toast.makeText(c,"Unable to connect. Please try again.", Toast.LENGTH_LONG).show();
                pbInitialLoader.setVisibility(View.GONE);
            }
        };

        getNewAttractionsListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {

            }

            @Override
            public void requestCompleted(String response) {
                ArrayList<Attraction> attractions = attractionHelper.getAttractions(response);
                dbHelper.addAttractionsToDB(attractions);
                setMarkers(attractions);

//                List<SearchItem> suggestionsList = dbHelper.getSearchSuggestions();
//
//                SearchAdapter searchAdapter = new SearchAdapter(c, suggestionsList);
//                searchAdapter.addOnItemClickListener(new SearchAdapter.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(View view, int position) {
//                        TextView textView = (TextView) view.findViewById(R.id.textView_item_text);
//                        String query = textView.getText().toString();
//                        getData(query, position);
//                        mSearchView.setQuery(query);
//                        mSearchView.close(false);
//                    }
//                });
//                mSearchView.invalidate();
//                mSearchView.setAdapter(searchAdapter);
            }

            @Override
            public void requestEndedWithError(VolleyError error) {

            }
        };

        getNewSearchAttractionsListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {

            }

            @Override
            public void requestCompleted(String response) {
                pbInitialLoader.setVisibility(View.GONE);
                ArrayList<Attraction> attractions = attractionHelper.getAttractions(response);
                dbHelper.addAttractionsToDB(attractions);

                if(attractions.size() > 0){
                    setMarkers(attractions);
                    CameraUpdate centerOnMarker = CameraUpdateFactory.newLatLng(new LatLng(attractions.get(0).getLat(),attractions.get(0).getLon()));
                    mMap.animateCamera(centerOnMarker);
                }else{
                    Toast.makeText(context, "No tickets found. Try another area.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                pbInitialLoader.setVisibility(View.GONE);
                Toast.makeText(context, "Unable to retrieve tickets. Please Try Again.", Toast.LENGTH_LONG).show();
            }
        };

        getConversationInfoListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {
                pbInitialLoader.setVisibility(View.VISIBLE);
            }

            @Override
            public void requestCompleted(String response) {
                if(!response.equals("0")){

                    Intent intent = new Intent(c, ConversationsActivity.class);

                    try{
                        Bundle b = new Bundle();
                        b.putLong("convoID", Long.parseLong(response));
                        intent.putExtra("convoBundle", b);
                    }catch (Exception ex){}

                    startActivity(intent);

                }else{
                    Toast.makeText(c, "Unable to contact seller. Please try again.",Toast.LENGTH_SHORT).show();
                }
                pbInitialLoader.setVisibility(View.GONE);
            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                Toast.makeText(c, "Unable to contact seller. Please try again.",Toast.LENGTH_SHORT).show();
                pbInitialLoader.setVisibility(View.GONE);
            }
        };
    }

    private void sendText(String phone){
        String body = "Hey, I saw your "+selectedAttractionName+" at "+selectedVenueName+" tickets on ProQuo for $" + selectedAttractionPrice + "/Ticket. Are they still for sale?"; // get it from selected contact
        Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( "sms:" + phone ) );
        intent.putExtra( "sms_body", body);
        try {
            startActivity(Intent.createChooser(intent, "Select your primary texting..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(c, "Please Download a texting application.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail(String email){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{email});
        i.putExtra(Intent.EXTRA_SUBJECT, "ProQuo - " + selectedAttractionName + " at " + selectedVenueName + " Tickets");
        i.putExtra(Intent.EXTRA_TEXT , "Hey, I saw your "+selectedAttractionName+" at "+selectedVenueName+" tickets on ProQuo for $" + selectedAttractionPrice + "/Ticket.\n\nAre they still for sale?");
        try {
            startActivity(Intent.createChooser(i, "Select your primary email application..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(c, "Please Download an email application.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setMarkers(ArrayList<Attraction> attractions){
        for (int i = 0; i < attractions.size(); i++){
            Attraction a = attractions.get(i);
            Marker m = mMap.addMarker(new MarkerOptions().snippet(attractionHelper.attractionToJsonShort(a)).position(new LatLng(a.getLat(), a.getLon()))
                    .title("$"+ MiscHelper.formatDouble(a.getTicketPrice()) + " - " + a.getName()));
            m.setVisible(false);

            markerHelper.formatMarker(a.getTicketPrice() + "", a.getImageURL(), m);
        }
    }

    private void setSingleMarker(Attraction a){
        Marker m = mMap.addMarker(new MarkerOptions().snippet(attractionHelper.attractionToJsonShort(a)).position(new LatLng(a.getLat(), a.getLon()))
                .title("$"+ MiscHelper.formatDouble(a.getTicketPrice()) + " - " + a.getName()));
        markerHelper.formatMarker(a.getTicketPrice() + "", a.getImageURL(), m);
    }

    private void initializeMapFragment(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mainMap);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == SET_LOCATION_AND_POST_TICKET_CODE_SUCCESS){

            try {//feels like an error could easily happen here
                Bundle b = data.getBundleExtra("bundle");
                Serializable s = b.getSerializable("attraction");
                AttractionSerializable ap = (AttractionSerializable) s;
                Attraction a = new Attraction();
                a.copyAttractionSerializable(ap);

                setSingleMarker(a);

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(a.getLat(), a.getLon()))      // Sets the center of the map to location user
                        .zoom(16)                    // Sets the orientation of the camera to east
                        //.tilt(40)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                Log.d("ACTIVITY_RESULT", a.getLat() + " - " + a.getLon());
            }catch (Exception ex){
                Log.d("ACTIVITY_RESULT", ex.toString());
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("ACTIVITY_RESULT", "MAP IS READY");
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerClickListener(this);
        mMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter(this));

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                try{
                    JSONObject obj = new JSONObject(marker.getSnippet());
                    long attractionID = obj.getLong("attractionID");
                    long creatorID = obj.getLong("creatorID");
                    String attractionName = obj.getString("attractionName");

                    if(loginHelp.isUserLoggedIn()){
                        if(creatorID == loggedInUser.getUserID()){
                            Intent intent = new Intent(c, EditAttractions.class);
                            startActivity(intent);
                        }else{
                            contactSeller(attractionID, loggedInUser.getUserID(), attractionName);
                        }
                    }else{
                        Toast.makeText(c, "You must be logged in to contact the seller", Toast.LENGTH_LONG).show();
                    }


                }catch (Exception ex){
                    Toast.makeText(c, "Error contacting the seller. Please Try Again.", Toast.LENGTH_LONG).show();
                }


            }
        });

        mMap.getUiSettings().setMapToolbarEnabled(false);

    }

    private void contactSeller(final long attractionID, final long buyerID, final String attractionName){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set title
        alertDialogBuilder.setTitle("Contact Seller");

        // set dialog message
        alertDialogBuilder
                .setMessage("Would you like to contact the seller?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        convoHelper.createConversationRequest(getConversationInfoListener, attractionID, buyerID, attractionName);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        if(!TextUtils.isEmpty(marker.getSnippet())){
            double center = mMap.getCameraPosition().target.latitude;
            double southMap = mMap.getProjection().getVisibleRegion().latLngBounds.southwest.latitude;

            double diff = (center - southMap)/2;

            double newLat = marker.getPosition().latitude + diff;

            CameraUpdate centerCam = CameraUpdateFactory.newLatLng(new LatLng(newLat, marker.getPosition().longitude));

            mMap.animateCamera(centerCam);
            marker.showInfoWindow();
        }else{
            //Toast.makeText(this, "Your Location", Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    private void clearMapForSearch(){
        if(mMap != null){
            mMap.clear();
            markerHelper.cancelAndClearTasks();

//            mMap.addMarker(new MarkerOptions().position(userLocation)
//                    .title("Your Location"));
        }
    }

    private void initializeMapData(){
        mMap.clear();
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(userLocation)      // Sets the center of the map to location user
                .zoom(13)                    // Sets the orientation of the camera to east
               // .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        VisibleRegion bounds = mMap.getProjection().getVisibleRegion();

//        mMap.addMarker(new MarkerOptions().position(userLocation)
//                .title("Your Location"));

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                float zoom = mMap.getCameraPosition().zoom;
                if(zoom < 12){
                    Toast toast = Toast.makeText(c,"You are zoomed too far out.\nPlease zoom in.", Toast.LENGTH_LONG);
                    TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                    if( v != null) v.setGravity(Gravity.CENTER);
                    toast.show();
                }else{
                    if(initialAttractionRequestCalled) {
                        VisibleRegion bounds = mMap.getProjection().getVisibleRegion();
                        String IDs = dbHelper.getCommaSepIDsFromDB();
                        attractionHelper.getNewAttractionsRequest(getNewAttractionsListener, bounds.latLngBounds.southwest.latitude, bounds.latLngBounds.northeast.latitude, bounds.latLngBounds.northeast.longitude, bounds.latLngBounds.southwest.longitude, searchViewQuery,IDs);
                    }
                }
            }
        });

        attractionHelper.getInitialAttractionsRequest(getInitialAttractionListener, bounds.latLngBounds.southwest.latitude, bounds.latLngBounds.northeast.latitude, bounds.latLngBounds.northeast.longitude, bounds.latLngBounds.southwest.longitude);
        //Log.d("BOUNDS",bounds.latLngBounds.southwest.latitude + " - " + bounds.latLngBounds.northeast.latitude + " - " + bounds.latLngBounds.northeast.longitude + " - " + bounds.latLngBounds.southwest.longitude);
    }

    private void attemptPostTicket(Dialog dialog, double lat, double lon){
        etVenueName.setError(null);
        etAttractionName.setError(null);
        etAttractionPrice.setError(null);
        etNumberOfTickets.setError(null);
        etAttractionDatePicker.setError(null);
        etAttractionDescription.setError(null);
        etAttractionImageSearch.setError(null);


        String venueName = etVenueName.getText().toString().trim();
        String attractionName = etAttractionName.getText().toString().trim();
        String price = etAttractionPrice.getText().toString().trim();
        String numberOfTickets = etNumberOfTickets.getText().toString().trim();
        String date = etAttractionDatePicker.getText().toString().trim();
        String description = etAttractionDescription.getText().toString().trim();

        View focusView = null;

        if(TextUtils.isEmpty(attractionName)){
            etAttractionName.setError(getString(R.string.error_field_required));
            focusView = etAttractionName;
            focusView.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(venueName)){
            etVenueName.setError(getString(R.string.error_field_required));
            focusView = etVenueName;
            focusView.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(price)){
            etAttractionPrice.setError(getString(R.string.error_field_required));
            focusView = etAttractionPrice;
            focusView.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(numberOfTickets)){
            etNumberOfTickets.setError(getString(R.string.error_field_required));
            focusView = etNumberOfTickets;
            focusView.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(date)){
            etAttractionDatePicker.setError(getString(R.string.error_field_required));
//            focusView = etAttractionDatePicker;
//            focusView.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(selectedImageURL)){
            etAttractionImageSearch.setError("Selected Image Required.");
            focusView = etAttractionImageSearch;
            focusView.requestFocus();
            return;
        }

        dialog.dismiss();

        Intent intent = new Intent(c, SetLocationAndPostTicket.class);
        Bundle b = new Bundle();
        b.putString("venueName",venueName);
        b.putString("attractionName", attractionName);
        b.putString("ticketPrice",price);
        b.putString("numberOfTickers",numberOfTickets);
        b.putString("description",description);
        b.putString("Date",date);
        b.putString("imageURL",selectedImageURL);
        b.putDouble("lat", lat);
        b.putDouble("lon", lon);
        intent.putExtra("bundle",b);
        startActivityForResult(intent, SET_LOCATION_AND_POST_TICKET_CODE_SUCCESS);

    }


    private void setFab() {
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        if (mFab != null) {
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(userIsLoggedIn){
                        // custom dialog
                        final Dialog dialog = new Dialog(context);
                        dialog.setContentView(R.layout.sell_ticket_dialog);
                        dialog.setTitle("Sell Ticket");

                        final ImageView ivSelectedMarkerIcon = (ImageView) dialog.findViewById(R.id.ivSelectedImage);
                        Button dialogButton = (Button) dialog.findViewById(R.id.bDoneSellTicket);
                        final ProgressBar pbLoadAttractionImages = (ProgressBar) dialog.findViewById(R.id.pbLoadAttractionImages);

                        etVenueName = (EditText) dialog.findViewById(R.id.etVenueName);
                        etAttractionName = (EditText) dialog.findViewById(R.id.etAttractionName);
                        etAttractionPrice = (EditText) dialog.findViewById(R.id.etAttractionPrice);
                        etNumberOfTickets = (EditText) dialog.findViewById(R.id.etAttractionNumberOfTickets);
                        etAttractionDatePicker = (EditText) dialog.findViewById(R.id.etAttractionDatePicker);
                        etAttractionDatePicker.setInputType(InputType.TYPE_NULL);
                        etAttractionDescription = (EditText) dialog.findViewById(R.id.etAttractionDescription);
                        etAttractionImageSearch = (EditText) dialog.findViewById(R.id.etAttractionImageSearch);

                        final TextInputLayout tilMarkerImageSearch = (TextInputLayout) dialog.findViewById(R.id.tilMarkerImageSearchLayout);
                        etAttractionImageSearch.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        final BingImageSearchHelper imageHelper = new BingImageSearchHelper(context);


                        etAttractionPrice.setFilters( new InputFilter[]{new DecimalDigitsInputFilter(2), new InputFilterMinMax(0, 1000000)});
                        etNumberOfTickets.setFilters(new InputFilter[]{new InputFilterMinMax(1,1000000)});

                        final RecyclerView mRecyclerView = (RecyclerView) dialog.findViewById(R.id.my_recycler_view);

                        // use this setting to improve performance if you know that changes
                        // in content do not change the layout size of the RecyclerView
                        mRecyclerView.setHasFixedSize(true);
                        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                        mRecyclerView.setLayoutManager(mLayoutManager);

                        // if button is clicked, close the custom dialog
                        dialogButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                attemptPostTicket(dialog, userLocation.latitude, userLocation.longitude);
                            }
                        });

                        ivSelectedMarkerIcon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ivSelectedMarkerIcon.setVisibility(View.GONE);
                                mRecyclerView.setVisibility(View.VISIBLE);
                                tilMarkerImageSearch.setVisibility(View.VISIBLE);
                            }
                        });

                        mRecyclerView.addOnItemTouchListener(
                                new RecyclerItemClickListener(context, mRecyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                                    @Override public void onItemClick(View view, int position) {
                                        // do whatever
                                        selectedImageURL = imageURLs[position];
                                        ivSelectedMarkerIcon.setVisibility(View.VISIBLE);
                                        Glide.with(context).load(selectedImageURL).centerCrop().into(ivSelectedMarkerIcon);

                                        mRecyclerView.setVisibility(View.GONE);
                                        tilMarkerImageSearch.setVisibility(View.GONE);
                                    }

                                    @Override public void onLongItemClick(View view, int position) {
                                        // do whatever
                                    }
                                })
                        );


                        imageResponseListener = new HttpResponseListener() {
                            @Override
                            public void requestStarted() {
                                pbLoadAttractionImages.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void requestCompleted(String response) {

                                pbLoadAttractionImages.setVisibility(View.GONE);
                                imageURLs = imageHelper.getImageThumbsFromJSON(response);
                                // specify an adapter (see also next example)
                                RecyclerViewImageAdapter mAdapter = new RecyclerViewImageAdapter(imageURLs,context);
                                mRecyclerView.invalidate();
                                mRecyclerView.setAdapter(mAdapter);
                            }

                            @Override
                            public void requestEndedWithError(VolleyError error) {
                                pbLoadAttractionImages.setVisibility(View.GONE);
                            }
                        };

                        etAttractionName.setOnFocusChangeListener(new EditText.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                if (!hasFocus) {
                                    etAttractionImageSearch.setText(etAttractionName.getText().toString());
                                    imageHelper.BingImageRequest(imageResponseListener,etAttractionName.getText().toString() + " " + etAttractionImageSearch.getText().toString());

                                }
                            }
                        });

//                        etAttractionImageSearch.addTextChangedListener(new TextWatcher() {
//
//                            @Override
//                            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                                // TODO Auto-generated method stub
//
//                            }
//
//                            @Override
//                            public void beforeTextChanged(CharSequence s, int start, int count,
//                                                          int after) {
//                                // TODO Auto-generated method stub
//
//                            }
//
//                            @Override
//                            public void afterTextChanged(Editable s) {
//                                if(!s.toString().contains(etAttractionName.getText().toString())){
//                                    etAttractionImageSearch.setText(etAttractionName.getText().toString());
//                                    Selection.setSelection( etAttractionImageSearch.getText(),  etAttractionImageSearch.getText().length());
//                                }
//
//                            }
//                        });

                        etAttractionImageSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                            @Override
                            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                if(actionId== EditorInfo.IME_ACTION_DONE){
                                    imageHelper.BingImageRequest(imageResponseListener,etAttractionImageSearch.getText().toString());
                                }
                                return false;
                            }
                        });


//                        etAttractionDatePicker.setOnFocusChangeListener(new EditText.OnFocusChangeListener() {
//                            @Override
//                            public void onFocusChange(View v, boolean hasFocus) {
//                                if (hasFocus && !etAttractionDatePicker.getError().equals("")) {
//                                    Calendar mcurrentDate=Calendar.getInstance();
//                                    int mYear=mcurrentDate.get(Calendar.YEAR);
//                                    int mMonth=mcurrentDate.get(Calendar.MONTH);
//                                    int mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);
//
//                                    final DatePickerDialog mDatePicker=new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
//                                        public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
//                                            etAttractionDatePicker.setText(selectedmonth+"/"+selectedday+"/"+selectedyear);
//                                            etAttractionDatePicker.setError(null);
//                                        }
//                                    },mYear, mMonth, mDay);
//                                    mDatePicker.setTitle("Select ticket expiration date");
//                                    mDatePicker.show();
//                                }else{
//                                    if(!etAttractionDatePicker.getText().toString().equals("")){
//                                        etAttractionDatePicker.setError(null);
//                                    }
//                                }
//                            }
//                        });


                        etAttractionDatePicker.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Calendar mcurrentDate=Calendar.getInstance();
                                int mYear=mcurrentDate.get(Calendar.YEAR);
                                int mMonth=mcurrentDate.get(Calendar.MONTH);
                                int mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

                                final DatePickerDialog mDatePicker=new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                                        etAttractionDatePicker.setText((selectedmonth + 1)+"/"+selectedday+"/"+selectedyear);
                                        etAttractionDatePicker.setError(null);
                                    }
                                },mYear, mMonth, mDay);
                                mDatePicker.setTitle("Select ticket expiration date");
                                mDatePicker.show();
                            }
                        });


                        dialog.show();
                    }else{
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                context);

                        // set title
                        alertDialogBuilder.setTitle("Login");

                        // set dialog message
                        alertDialogBuilder
                                .setMessage("You must be logged in to sell tickets.")
                                .setCancelable(false)
                                .setPositiveButton("Login",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        Intent intent = new Intent(c, LoginActivity.class);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });

                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.setCanceledOnTouchOutside(true);
                        alertDialog.show();
                    }

                }
            });
        }

        FloatingActionButton goToMyLoc = (FloatingActionButton) findViewById(R.id.fab1);
        if (goToMyLoc != null){
            goToMyLoc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userLocation != null){
                        CameraUpdate centerOnMarker = CameraUpdateFactory.newLatLng(userLocation);
                        mMap.animateCamera(centerOnMarker);
                    }else{
                        Toast.makeText(c,"Unable to determine your location", Toast.LENGTH_LONG).show();
                    }

                }
            });

        }
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    recreate();
                } else {
                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION);
                    if (! showRationale) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                        // set title
                        alertDialogBuilder.setTitle("Location Required");

                        // set dialog message
                        alertDialogBuilder
                                .setMessage("Because you requested never to be asked for your location again, please go into your application settings. Then select this app. Once selected go to permissions and allow for your location to be used. Reinstalling the app will work too.")
                                .setCancelable(false)
                                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                });

                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();

                        alertDialog.show();
                    }else{
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                        // set title
                        alertDialogBuilder.setTitle("Location Required");

                        // set dialog message
                        alertDialogBuilder
                                .setMessage("This app requires your location to be used. Can we request it again?")
                                .setCancelable(false)
                                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.dismiss();
                                        requestPermission();
                                    }
                                })
                                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                });

                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();

                        alertDialog.show();
                    }
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions c app might request
        }
    }


}
