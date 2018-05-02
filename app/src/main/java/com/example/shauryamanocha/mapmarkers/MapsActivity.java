package com.example.shauryamanocha.mapmarkers;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.xml.sax.helpers.LocatorImpl;

import java.lang.ref.Reference;
import java.sql.Ref;
import java.util.ArrayList;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    static DatabaseReference reference;
    boolean alreadyExists;
    LocationManager locationManager;
    Location currentLocation;
    String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET
    };
    String currentProvider = LocationManager.GPS_PROVIDER;
    ArrayList<customLatLng> sessionLocations;
    Button newMarkerButton;
    public static FirebaseAuth auth;
    double radiusPerHit = 1;
    double localRadius = 10;
    Button signIn;
    static boolean signedIn = false;
    String currentSchool = "School not selected";



    static FirebaseUser user;

    int userAmount;
    int schoolAmount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        sessionLocations = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        alreadyExists = false;
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        newMarkerButton = findViewById(R.id.newMarker);
        newMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(signedIn) {
                    addNewMarker(currentLocation);
                    reference.child("Schools").child(currentSchool).child("Users").child("User" + auth.getUid()).child("Amount").setValue(userAmount);
                }
            }
        });


        signIn = findViewById(R.id.signIn);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this,googleSignIn.class));
            }
        });

        MapsInitializer.initialize(getApplicationContext());
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        reference = database.getReference("Database");

    }

//    public String uuid(int len){
//        String val = "";
//        Random rand = new Random();
//        for(int i = 0;i<len;i++){
//            val += (rand.nextInt(9));
//        }
//        return val;
//    }


    public double distance(customLatLng first, customLatLng second){
        double x2 = second.latitude;
        double x1 = first.latitude;
        double y2 = second.longitude;
        double y1 = first.longitude;
        double distance = Math.sqrt(Math.pow(x2-x1,2) + Math.pow(y2-y1,2));
        return distance;
    }

    public void addNewMarker(Location location) {
        if(location!=null) {
            reference.child("Schools").child(currentSchool).child("Users").child("User"+auth.getUid()).child("Amount").setValue(1);
            reference.child("Schools").child(currentSchool).child("Amount").setValue(schoolAmount);
            MarkerOptions options = new MarkerOptions();
            customLatLng latlng = new customLatLng(location.getLatitude(), location.getLongitude(), location.getTime(),1,auth.getUid());
            for (customLatLng l : sessionLocations) {
                if (distance(l,latlng)<radiusPerHit) {
                    alreadyExists = true;
                    l.increaseAmount(reference);
                    reference.child("Hits").child("Hit" + l.id).child("Subhit" + System.currentTimeMillis()).setValue(new SubDetection(l.latitude,l.longitude, System.currentTimeMillis()));
                    reference.child("Hits").child("Hit" + l.id).child("Amount").setValue(l.amount);
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(l.getMarkerOptions().getPosition()).title(l.getMarkerOptions().getTitle()).icon(BitmapDescriptorFactory.fromResource(R.drawable.star)));
                    break;
                } else {
                    alreadyExists = false;

                }
            }
            if (!alreadyExists) {
                options = latlng.getMarkerOptions();
                Toast.makeText(getBaseContext().getApplicationContext(), "New Place", Toast.LENGTH_SHORT).show();
                mMap.addMarker(new MarkerOptions().position(options.getPosition()).title(options.getTitle()).icon(BitmapDescriptorFactory.fromResource(R.drawable.star)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng.toLatLng()));
                sessionLocations.add(latlng);
                reference.child("Hits").child("Hit"+location.getTime()).setValue(latlng);
            } else {
                Toast.makeText(getBaseContext().getApplicationContext(), "Already Exists", Toast.LENGTH_SHORT).show();
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng.toLatLng()));
            schoolAmount++;
            userAmount++;
            reference.child("Schools").child(currentSchool).child("Users").child("User"+auth.getUid()).child("Amount").setValue(1);
            reference.child("Schools").child(currentSchool).child("Amount").setValue(schoolAmount);
        }else{
            Toast.makeText(getBaseContext().getApplicationContext(), "Error, Please Try Again", Toast.LENGTH_SHORT).show();

        }
    }

    public void addNewMarker(customLatLng location) {
        if (location != null) {
            MarkerOptions options;
            options = location.getMarkerOptions();
//            Toast.makeText(getBaseContext().getApplicationContext(),"New Place", Toast.LENGTH_SHORT).show();
            mMap.addMarker(new MarkerOptions().position(options.getPosition()).title(options.getTitle()).icon(BitmapDescriptorFactory.fromResource(R.drawable.star)));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            sessionLocations.add(location);
//            reference.child("Positions").child("Hit" + uuid(20)).setValue(latlng); //This one is only called when populating the array from Firebase
        }
    }
    @Override
    public void onStart(){
        super.onStart();
    }

    public void onAuth(String school){
        signedIn = true;
        currentSchool = school;
//        signIn.setText("Signed In");
//        signIn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(MapsActivity.this,"Already Signed In", Toast.LENGTH_SHORT);
//            }
//        });

        reference.child("Schools").child(currentSchool).child("Users").child("User"+auth.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                for(DataSnapshot d : dataSnapshot.getChildren()){
                    try {
                        userAmount = d.getValue(Integer.class);
                    }catch (Exception e){
                        userAmount = 0;
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        reference.child("Schools").child(currentSchool).child("Amount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot d :dataSnapshot.getChildren()){
                    try {
                        schoolAmount = d.getValue(Integer.class);
                    }catch (Exception e){
                        schoolAmount = 0;
                        reference.child("Schools").child(currentSchool).child(currentSchool).child("Amount").setValue(0);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        reference.child("Hits").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                for(DataSnapshot d : dataSnapshot.getChildren()){
                    try {
                        addNewMarker(new customLatLng(d.getValue(customLatLng.class).latitude,d.getValue(customLatLng.class).longitude,d.getValue(customLatLng.class).id,d.getValue(customLatLng.class).amount,d.getValue(customLatLng.class).user));
                    }catch (Exception e){
                        Toast.makeText(MapsActivity.this,e + "-----" + d.getValue(),Toast.LENGTH_LONG).show();
                    }
                    //Toast.makeText(MapsActivity.this,d.getValue().toString(),Toast.LENGTH_LONG);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        for(customLatLng l : sessionLocations){
            addNewMarker(l);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED) {
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,permissions,1);
            return;
        }else {
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                currentProvider = LocationManager.GPS_PROVIDER;
            }else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                currentProvider = LocationManager.NETWORK_PROVIDER;
            }else{
                Toast.makeText(getBaseContext().getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }
            locationManager.requestLocationUpdates(currentProvider, 1000, 1, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    currentLocation = location;
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }
    }
}
