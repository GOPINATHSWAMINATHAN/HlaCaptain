package com.hlacab.hlacaptain.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hlacab.hlacaptain.interfaces.TripDetails;
import com.hlacab.hlacaptain.model.Data;
import com.hlacab.hlacaptain.service.TripService;
import com.hlacab.hlacaptain.R;
import com.hlacab.hlacaptain.interfaces.CaptainDetails;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import at.markushi.ui.CircleButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, RoutingListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    String phone;
    private FirebaseAuth mAuth;
    LocationRequest mLocationRequest;
    long durationInSeconds;

    private Button mLogout, mSettings, mRideStatus, mHistory;


    private CaptainDetails mDetailsHla;
    private Call<Data> mcall;
    String driverReferenceID, vehicleReference;

    private String myDuration;
    private Switch mWorkingSwitch;
    String mName;
    private int status = 0;

    private DatabaseReference mDriverDatabase;
    private Date customerAssignedTime;
    private String userID;
    private String customerId = "", destination;
    private LatLng destinationLatLng, pickupLatLng;
    private float rideDistance;
    //Button navigation;
    private Boolean isLoggingOut = false;

    private SupportMapFragment mapFragment;

    private LinearLayout mCustomerInfo;

    private ImageView mCustomerProfileImage;

    private TextView mCustomerName, mCustomerPhone, mCustomerDestination;
    MediaPlayer mediaPlayer;
    double lat;
    double lng;
    String pickuptime;
    Double completedistance;
    CircleButton navigation;
    public List<String> referenceDetails = new ArrayList();
    private String customerPickupTime, dropOffTime;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);

        //LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBroadcastReceiver, new IntentFilter(MyService.MY_SERVICE_MESSAGE));

        startService(new Intent(this, TripService.class));


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        polylines = new ArrayList<>();
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            mapFragment.getMapAsync(this);
        }
        navigation = findViewById(R.id.startNavi);
        if (pickupLatLng == null) {
            navigation.setVisibility(View.INVISIBLE);
        }
        navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRideStatus.getText() == "picked customer") {
                    lat = pickupLatLng.latitude;

                    lng = pickupLatLng.longitude;


                    Toast.makeText(getApplicationContext(), "pickuptime" + pickuptime, Toast.LENGTH_LONG).show();
                } else {
                    lat = destinationLatLng.latitude;
                    lng = destinationLatLng.longitude;
                }
                String format = "geo:0,0?q=" + lat + "," + lng + "(Pickup Location)";

                Uri uri = Uri.parse(format);


                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

//                Intent myIntent=new Intent(getApplicationContext(),ShowNavigation.class);
////              Bundle b=new Bundle();
////              b.putParcelable("pickuplocation",new LatLng(pickupLatLng.latitude,pickupLatLng.longitude));
////               b.putParcelable("destinationlocation",new LatLng(destinationLatLng.latitude,destinationLatLng.longitude));
//                startActivity(myIntent);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mCustomerInfo = (LinearLayout) findViewById(R.id.customerInfo);

        mCustomerProfileImage = (ImageView) findViewById(R.id.customerProfileImage);

        mCustomerName = (TextView) findViewById(R.id.customerName);
        mCustomerPhone = (TextView) findViewById(R.id.customerPhone);
        mCustomerDestination = (TextView) findViewById(R.id.customerDestination);

        mWorkingSwitch = (Switch) findViewById(R.id.workingSwitch);
        mWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
//                        getRideLater();
                    registerUserWithTeliver();
                    connectDriver();
                    insertUser();
                    startTrip();
                    getReferenceDetails();

                } else {
                    stopTrip();
                    Toast.makeText(getApplicationContext(), "We cannot assign trip to you now.", Toast.LENGTH_LONG).show();
                    disconnectDriver();
                }
            }
        });

        mSettings = (Button) findViewById(R.id.settings);
        mLogout = (Button) findViewById(R.id.logout);
        mRideStatus = (Button) findViewById(R.id.rideStatus);
        mHistory = (Button) findViewById(R.id.history);
        mRideStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (status) {
                    case 1:
                        status = 2;
                        erasePolylines();
                        if (destinationLatLng.latitude != 0.0 && destinationLatLng.longitude != 0.0) {
                            getRouteToMarker(destinationLatLng);
                        }
                        Calendar calendar = Calendar.getInstance();
                        customerPickupTime = getCurrentTimestamp();
                        mRideStatus.setText("drive completed");
                        break;
                    case 2:
                        dropOffTime = getCurrentTimestamp();
                        // recordRide();
                        //   getReferenceDetails();
                        sendCompleteTripDetails();

                        Toast.makeText(getApplicationContext(), "sent complete trip details", Toast.LENGTH_LONG).show();
                        endRide();
                        break;
                }
            }
        });

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoggingOut = true;
                disconnectDriver();
                FirebaseAuth.getInstance().signOut();
                finish();
                return;
            }
        });
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverMapActivity.this, DriverSettingsActivity.class);
                startActivity(intent);
                return;
            }
        });
        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverMapActivity.this, HistoryActivity.class);
                intent.putExtra("customerOrDriver", "Drivers");
                startActivity(intent);
                return;
            }
        });
        getAssignedCustomer();
    }

    void getCustomerAssignedTime() {
        Calendar calendar = Calendar.getInstance();
        customerAssignedTime = calendar.getTime();
    }

    private void getAssignedCustomer() {
        try {
            if (mAuth.getCurrentUser() != null) {
                String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest").child("customerRideId");
                assignedCustomerRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            status = 1;
                            customerId = dataSnapshot.getValue().toString();
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer = MediaPlayer.create(DriverMapActivity.this, R.raw.android_mp3);
                            mediaPlayer.start();
                            getCustomerAssignedTime();
                            getAssignedCustomerPickupLocation();
                            getAssignedCustomerDestination();
                            getAssignedCustomerInfo();
                            navigation.setVisibility(View.VISIBLE);
                        } else {
                            endRide();
                            stopTrip();
                        }
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        } catch (Exception e) {
            finish();
            Intent intent = new Intent(this, TripService.class);
            stopService(intent);
        }
        DatabaseReference childDelete = FirebaseDatabase.getInstance().getReference().child("customerRequest");
        childDelete.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                navigation.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Trip finished!", Toast.LENGTH_LONG).show();
                Log.e("TRIP CANCELLED", "Your Trip has been cancelled!");
                endRide();
                stopTrip();

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    Marker pickupMarker;
    private DatabaseReference assignedCustomerPickupLocationRef;
    private ValueEventListener assignedCustomerPickupLocationRefListener;

    private void getAssignedCustomerPickupLocation() {
        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");
        assignedCustomerPickupLocationRefListener = assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !customerId.equals("")) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    pickupLatLng = new LatLng(locationLat, locationLng);
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                    // navigation.setVisibility(View.VISIBLE);
                    getRouteToMarker(pickupLatLng);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void getRouteToMarker(LatLng pickupLatLng) {
        try {
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), pickupLatLng)
                    .build();
            routing.execute();
        } catch (Exception e) {
//            Intent intent = getIntent();
//            finish();
//            startActivity(intent);
        }
    }

    private void getAssignedCustomerDestination() {
if(mAuth.getCurrentUser()!=null) {
    String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest");
    assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                if (map.get("destination") != null) {
                    destination = map.get("destination").toString();
                    mCustomerDestination.setText("Destination: " + destination);
                } else {
                    mCustomerDestination.setText("Destination: --");
                }

                Double destinationLat = 0.0;
                Double destinationLng = 0.0;
                if (map.get("destinationLat") != null) {
                    destinationLat = Double.valueOf(map.get("destinationLat").toString());
                }
                if (map.get("destinationLng") != null) {
                    destinationLng = Double.valueOf(map.get("destinationLng").toString());
                    destinationLatLng = new LatLng(destinationLat, destinationLng);
                }

            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    });
}
    }


    private void getAssignedCustomerInfo() {
        mCustomerInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
//                    Toast.makeText(getApplicationContext(), "" + map.get("name").toString(), Toast.LENGTH_LONG).show();
//                    Toast.makeText(getApplicationContext(), "" + map.get("phone").toString(), Toast.LENGTH_LONG).show();
//                    Toast.makeText(getApplicationContext(), "" + map.get("profileImageUrl"), Toast.LENGTH_LONG).show();
                    if (map.get("name") != null) {
                        mCustomerName.setText(map.get("name").toString());

                    } else {
                        mCustomerName.setText("Not Provided!");
                    }
                    if (map.get("phone") != null) {
                        phone = map.get("phone").toString();
                        mCustomerPhone.setText(map.get("phone").toString());

                    } else {
                        mCustomerPhone.setText("Not Provided");
                    }
                    if (map.get("profileImageUrl") != null) {
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mCustomerProfileImage);
                    } else {
                        Drawable d = getResources().getDrawable(R.drawable.saudi);
                        mCustomerProfileImage.setImageDrawable(d);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void endRide() {
        mRideStatus.setText("picked customer");
        erasePolylines();
        if(mAuth.getCurrentUser()!=null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("customerRequest");
            driverRef.removeValue();

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
            GeoFire geoFire = new GeoFire(ref);
            geoFire.removeLocation(customerId);
            customerId = "";
            rideDistance = 0;

            if (pickupMarker != null) {
                pickupMarker.remove();
            }
            if (assignedCustomerPickupLocationRefListener != null) {
                assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
            }
            mCustomerInfo.setVisibility(View.GONE);
            mCustomerName.setText("");
            mCustomerPhone.setText("");
            mCustomerDestination.setText("Destination: --");
            mCustomerProfileImage.setImageResource(R.mipmap.ic_default_user);
        }

    }

    private void recordRide(String refId) {
        if(mAuth.getCurrentUser()!=null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("history");
            DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId).child("history");
            DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");
            String requestId = historyRef.push().getKey();
            driverRef.child(requestId).setValue(true);
            customerRef.child(requestId).setValue(true);
//Location.distanceBetween(pickupLatLng.latitude,pickupLatLng.longitude,destinationLatLng.latitude,destinationLatLng.longitude);

            HashMap map = new HashMap();
            map.put("referenceIdELM", refId);
            map.put("rating", 4.5);
            map.put("distanceInMeters", completedistance.intValue() * 1000);
            map.put("durationInSeconds", durationInSeconds);
            map.put("destination", destination);
            map.put("customerWaitingTime", "5mins");
            map.put("originlat", pickupLatLng.latitude);
            map.put("originlng", pickupLatLng.longitude);
            map.put("originCityNameInArabic", "أل رياض");
            map.put("destinationCityNameInArabic", "أل رياض");
            map.put("destinationlat", destinationLatLng.latitude);
            map.put("destinationlng", destinationLatLng.longitude);
            map.put("carReference", vehicleReference);
            map.put("driverReference", driverReferenceID);
            map.put("pickUpTimeStamp", customerPickupTime);
            map.put("dropOffTimeStamp", dropOffTime);
            historyRef.child(requestId).updateChildren(map);
            Log.e("MAP DETAILS ARE", String.valueOf(map));
        }

    }

    private String getCurrentTimestamp() {

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.'000'");
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());
        return nowAsISO;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (getApplicationContext() != null) {

            if (!customerId.equals("")) {

                if (rideDistance != 0.0) {
                    rideDistance += mLastLocation.distanceTo(location) / 1000;
                }
                new RetrieveFeedTask().execute();
//                Toast.makeText(getApplicationContext(), "Complete distance is " + completedistance, Toast.LENGTH_LONG).show();
//                Toast.makeText(getApplicationContext(), "Complete duration is " + myDuration, Toast.LENGTH_LONG).show();
                if (myDuration != null) {
                    try {
                        String check = myDuration.replaceAll(" mins", "");
//                        Toast.makeText(getApplicationContext(), "Y duration" + check, Toast.LENGTH_LONG).show();
                        durationInSeconds = java.util.concurrent.TimeUnit.MINUTES.toSeconds(Integer.parseInt(check));
                        // Toast.makeText(getApplicationContext(), "SECONDS TIME IS " + durationInSeconds, Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        String check = myDuration.replaceAll(" min", "");
//                        Toast.makeText(getApplicationContext(), "Y duration" + check, Toast.LENGTH_LONG).show();
                        durationInSeconds = java.util.concurrent.TimeUnit.MINUTES.toSeconds(Integer.parseInt(check));
//

                    }
                }
            }

            mLastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16.3f));
            if (mAuth.getCurrentUser() != null) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable");
                DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driversWorking");
                GeoFire geoFireAvailable = new GeoFire(refAvailable);
                GeoFire geoFireWorking = new GeoFire(refWorking);

                switch (customerId) {
                    case "":
                        geoFireWorking.removeLocation(userId);
                        geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                        break;

                    default:
                        geoFireAvailable.removeLocation(userId);
                        geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                        break;
                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    private void connectDriver() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        if (mGoogleApiClient != null)
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void disconnectDriver() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        if (mAuth.getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");

            GeoFire geoFire = new GeoFire(ref);
            geoFire.removeLocation(userId);
        }

    }


    final int LOCATION_REQUEST_CODE = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapFragment.getMapAsync(this);
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.routes};

    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            //Toast.makeText(getApplicationContext(), "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - " + route.get(i).getDurationValue(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {
    }

    private void erasePolylines() {
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }

    public void startTrip() {
        userID = mAuth.getCurrentUser().getUid();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userID);
        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        mName = map.get("name").toString();
                        //Teliver.startTracking(new TrackingBuilder(new MarkerOption(mName)).build());

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void stopTrip() {


    }

    public void registerUserWithTeliver() {
        userID = mAuth.getCurrentUser().getUid();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userID);
        mDriverDatabase.addValueEventListener(new ValueEventListener() {

            String mPhone, mCar, mService;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        mName = map.get("name").toString();

                    }
                    if (map.get("phone") != null) {
                        mPhone = map.get("phone").toString();

                    }
                    if (map.get("car") != null) {
                        mCar = map.get("car").toString();

                    }
                    if (map.get("service") != null) {
                        mService = map.get("service").toString();
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocationManager mlocManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabled) {
            showDialogGPS();
        }
    }

    private void showDialogGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Enable GPS");
        builder.setMessage("Please enable GPS");
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startActivity(
                        new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private DatabaseReference assignedCustomerPickupLocationRefLater;
    private ValueEventListener assignedCustomerPickupLocationRefListenerLater;
    private DatabaseReference time;

    private void getRideLater() {
        assignedCustomerPickupLocationRefLater = FirebaseDatabase.getInstance().getReference().child("rideLaterRequest").child(customerId).child("l");

        time = FirebaseDatabase.getInstance().getReference().child("rideLaterRequest").child(customerId).child("time");
        time.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String time = dataSnapshot.getValue(String.class);
                Toast.makeText(getApplicationContext(), "" + time, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        assignedCustomerPickupLocationRefListenerLater = assignedCustomerPickupLocationRefLater.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !customerId.equals("")) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    pickupLatLng = new LatLng(locationLat, locationLng);
                    Toast.makeText(getApplicationContext(), "" + pickupLatLng, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTrip();
        disconnectDriver();
    }


    class RetrieveFeedTask extends AsyncTask<Void, Void, Double> {


        @Override
        protected Double doInBackground(Void... voids) {
            StringBuilder stringBuilder = new StringBuilder();
            Double dist = 0.0;
            try {

                if (pickupLatLng.latitude != 0.0 && destinationLatLng.latitude != 0.0) {
                    //destinationAddress = destinationAddress.replaceAll(" ","%20");
                    String url = "http://maps.googleapis.com/maps/api/directions/json?origin=" + pickupLatLng.latitude + "," + pickupLatLng.longitude + "&destination=" + destinationLatLng.latitude + "," + destinationLatLng.longitude + "&mode=driving&sensor=false";

                    HttpPost httppost = new HttpPost(url);

                    DefaultHttpClient client = new DefaultHttpClient();
                    HttpResponse response;
                    stringBuilder = new StringBuilder();


                    response = client.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    InputStream stream = entity.getContent();
                    int b;
                    while ((b = stream.read()) != -1) {
                        stringBuilder.append((char) b);
                    }
                } else {

                    Toast.makeText(getApplicationContext(), "Try to delete the customer request which has given for this particular driver", Toast.LENGTH_LONG).show();
                }
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }


            JSONObject jsonObject = new JSONObject();
            try {

                jsonObject = new JSONObject(stringBuilder.toString());

                JSONArray array = jsonObject.getJSONArray("routes");

                JSONObject routes = array.getJSONObject(0);

                JSONArray legs = routes.getJSONArray("legs");

                JSONObject steps = legs.getJSONObject(0);

                String duration = steps.getJSONObject("duration").getString("text");

                myDuration = duration;

                JSONObject distance = steps.getJSONObject("distance");


                Log.i("Distance", distance.toString());
                dist = Double.parseDouble(distance.getString("text").replaceAll("[^\\.0123456789]", ""));

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            completedistance = dist;
            return dist;
        }

        @Override
        protected void onPostExecute(Double aDouble) {
            super.onPostExecute(aDouble);
        }
    }


    void getReferenceDetails() {
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("FromMobilyDriver").child(userID).child("cardet");
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("refid") != null) {
                        vehicleReference = map.get("refid").toString();

                    }

                    //getCompleteCaptainDetails();
//                    Toast.makeText(getApplicationContext(), "" + map, Toast.LENGTH_LONG).show();
//                    Log.e("CAPTAIN DETAILS", "" + map);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        DatabaseReference driverReference = FirebaseDatabase.getInstance().getReference().child("FromMobilyDriver").child(userID);
        driverReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("refid") != null) {
                        driverReferenceID = map.get("refid").toString();

                    }

                    //getCompleteCaptainDetails();
//                    Toast.makeText(getApplicationContext(), "" + map, Toast.LENGTH_LONG).show();
//                    Log.e("CAPTAIN DETAILS", "" + map);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void insertUser() {
        Intent intent = new Intent(this, TripService.class);
        startService(intent);

    }

    void sendCompleteTripDetails() {

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create()).baseUrl("http://hlacab.com/ELMCNT/")
                .build();

        TripDetails service = retrofit.create(TripDetails.class);

        //Log.e("TRIPDETAILSVALUE", "" + String.valueOf(vehicleReference) + "/" + String.valueOf(driverReferenceID) + "/" + String.valueOf(completedistance * 1000) + "/" + String.valueOf(durationInSeconds) + "/" + String.valueOf(pickupLatLng.latitude) + "/" + String.valueOf(pickupLatLng.longitude) + "/" + String.valueOf(destinationLatLng.latitude) + "/" + String.valueOf(destinationLatLng.longitude) + "/" + String.valueOf(customerPickupTime) + "/" + String.valueOf(dropOffTime));

        //pickup time stamp and drop off time stamp.
        //distance In meters
        //duration in seconds
        //pickup and destination precision should be 6.

        int cd = completedistance.intValue();
        Call<Data> call = service.requestData(vehicleReference, driverReferenceID, String.valueOf(cd * 1000), "50000", "4.5", "200", "أل رياض", "أل رياض", String.valueOf(pickupLatLng.latitude), String.valueOf(pickupLatLng.longitude), String.valueOf(destinationLatLng.latitude), String.valueOf(destinationLatLng.longitude), customerPickupTime, dropOffTime);

        call.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {

                int statusCode = response.code();
                Data user = response.body();

                assert user != null;
                Log.e("DRIVER REFERENCE NUMBER", "" + user.getReferenceNumber());
                recordRide(user.getReferenceNumber());

//                Log.d("resultCode: ", "" + user.getName());
//                Log.d("resultMessage", "" + user.getResultMessage());

            }

            @Override
            public void onFailure(Call<Data> call, Throwable t) {
                Log.e("FAILURE MESSAGE", "" + t.getMessage());
                Log.e("TOAST FEIL", "TOAST FAIL");

            }


        });

    }


}
//    void getData()
//    {
//
//
//                new Thread(new Runnable() {
//                    public void run() {
//
//                        try{
//                            URL url = new URL("http://hlacab.com/ELMCNT/ex1?ex12=gopinath");
//
//                            URLConnection connection = url.openConnection();
//                            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
//                            String inputString = "Gopinath";
//                            //inputString = URLEncoder.encode(inputString, "UTF-8");
//
//                            Log.d("inputString", inputString);
//
//                            connection.setDoOutput(true);
//                            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
//                            out.write(inputString);
//                            out.close();
//
//                            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//
//                            String returnString="";
//
//
//                            while ((returnString = in.readLine()) != null)
//                            {
//                                Log.e("RETURNED STRING FROM",""+returnString);
//                            }
//                            in.close();
//
//
//
//                        }catch(Exception e)
//                        {
//                            Log.d("Exception",e.toString());
//                        }
//
//                    }
//                }).start();
//
//
//
//    }




/*
*
* }￼
 "apiKey": "API_KEY",
 "vehicleReferenceNumber": 1,
 "captainReferenceNumber": 1000,
 "distanceInMeters": 1215,
 "durationInSeconds": 42145,
 "customerRating": 90.0,
 "customerWaitingTimeInSeconds": 12132,
    ,"الرياض" :"originCityNameInArabic"
     ,"الرياض" :"destinationCityNameInArabic"
 "originLatitude": 24.723437,
 "originLongitude": 46.117452,
 "destinationLatitude": 24.763437,
 "destinationLongitude": 46.547452,
 "pickupTimestamp": "2016-03-28T09:00:00.000",
 "dropoffTimestamp": "2016-03-28T09:15:00.000"
*
*
* */