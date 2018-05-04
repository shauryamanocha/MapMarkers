package com.example.shauryamanocha.mapmarkers;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;


import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import android.widget.Button;

import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


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


    int schoolAmount = 0;
    //static FirebaseUser user;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static User user = new User();
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
                }else{
                    Toast.makeText(MapsActivity.this,"Sign In",Toast.LENGTH_SHORT).show();
                }
            }
        });

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        db.setFirestoreSettings(settings);

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
            //reference.child("Schools").child(currentSchool).child("Users").child("User"+auth.getUid()).child("Amount").setValue(1);
            //reference.child("Schools").child(currentSchool).child("Amount").setValue(schoolAmount);
            MarkerOptions options = new MarkerOptions();
            customLatLng latlng = new customLatLng(location.getLatitude(), location.getLongitude(), location.getTime(),1,auth.getUid());
//            for (customLatLng l : sessionLocations) {
//                if (distance(l,latlng)<radiusPerHit) {
//                    alreadyExists = true;
//                    l.increaseAmount(reference);
//                    //reference.child("Hits").child("Hit" + l.id).child("Subhit" + System.currentTimeMillis()).setValue(new SubDetection(l.latitude,l.longitude, System.currentTimeMillis()));
//                    //reference.child("Hits").child("Hit" + l.id).child("Amount").setValue(l.amount);
//                    mMap.clear();
//                    mMap.addMarker(new MarkerOptions().position(l.getMarkerOptions().getPosition()).title(l.getMarkerOptions().getTitle()).icon(BitmapDescriptorFactory.fromResource(R.drawable.star)));
//                    break;
//                } else {
//                    alreadyExists = false;
//
//                }
//            }
//            if (!alreadyExists) {
                options = latlng.getMarkerOptions();
                Toast.makeText(getBaseContext().getApplicationContext(), "New Place", Toast.LENGTH_SHORT).show();
                mMap.addMarker(new MarkerOptions().position(options.getPosition()).title(options.getTitle()).icon(BitmapDescriptorFactory.fromResource(R.drawable.star)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng.toLatLng()));
                sessionLocations.add(latlng);
                user.amount++;
                db.collection("Schools").document(user.school).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.getData() == null){
                            Map<String,Object> amount = new HashMap<>();
                            amount.put("amount",1);
                            db.collection("Schools").document(user.school).set(amount);
                        }else if(documentSnapshot.getData().get("amount") != null){
                            Map<String,Object> amount = new HashMap<>();
                            amount.put("amount",(long)documentSnapshot.getData().get("amount")+1);
                             db.collection("Schools").document(user.school).set(amount);
                        }
                    }
                });
                user.hits.add(latlng);
                updateDatabase();
                //reference.child("Hits").child("Hit"+location.getTime()).setValue(latlng);
//            } else {
//                Toast.makeText(getBaseContext().getApplicationContext(), "Already Exists", Toast.LENGTH_SHORT).show();
//            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng.toLatLng()));
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

    public void onAuth(User user){
        signedIn = true;
        user = googleSignIn.user;
    }

    public void updateDatabase(){
        db.collection("Schools").document(user.school)
                .collection("Users").document("User "+user.id)
                .set(user);

//        Map<String, Object> reference = new HashMap<>();
//        DocumentReference ref =
//                db.collection("Schools").document(user.school)
//                .collection("Users").document("User "+user.id);
//        reference.put("Reference",ref);
//        reference.put("Reference","Schools/"+user.school+"/Users"+user.id+"/hits");
//        db.collection("References").add(reference);
        db.collection("References").document(user.name+" "+user.id).set(new HitReference(user.school,"User "+user.id));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        db.collection("References")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot document : task.getResult()){
                            if(task.isSuccessful()) {
                                //document.getData();
                                DocumentReference currentReference = db.collection("Schools").document((String) document.getData().get("school")).collection("Users").document((String) document.getData().get("user"));
                                currentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        Log.w("TAG",documentSnapshot.getData().get("hits").toString());
                                        ArrayList<customLatLng> currentHits = (ArrayList<customLatLng>)documentSnapshot.getData().get("hits");
                                        Map<String,Object> hitsHash = new HashMap<>();
                                        Log.w("LATLNG",currentHits+"");
                                        for(Object o : currentHits){
                                            hitsHash.put(o.toString(),o);
                                        }
                                        Log.w("print",hitsHash.toString());
                                        for(Map.Entry<String,Object> h : hitsHash.entrySet()){
                                            HashMap<String,Object> currentHit = (HashMap)h.getValue();
                                            Log.w("hitdata",currentHit.get("longitude")+"");
                                            addNewMarker(new customLatLng((double)currentHit.get("latitude"),(double)currentHit.get("longitude"),(long)currentHit.get("id"),(long)currentHit.get("amount"),(String)currentHit.get("user")));
                                        }
                                    }
                                });
                            }else{
                                Log.w("TAG","task unsuccessful");
                            }
                        }
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
