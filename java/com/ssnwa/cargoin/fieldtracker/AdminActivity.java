package com.ssnwa.cargoin.fieldtracker;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AdminActivity extends FragmentActivity implements OnMapReadyCallback
        , GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;

    FirebaseDatabase database;
    DatabaseReference ref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        database=FirebaseDatabase.getInstance();
        ref=database.getReference("Employee");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnInfoWindowClickListener(this);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(final DataSnapshot d:dataSnapshot.getChildren())
                {
                    Date c = Calendar.getInstance().getTime();
                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                    String formattedDate = df.format(c);
                    GeoFire getlocation=new GeoFire(d.child(formattedDate).getRef());
                    getlocation.getLocation("currentlocation", new LocationCallback() {
                        @Override
                        public void onLocationResult(String key, GeoLocation location) {
                            if(location!=null)
                            {
                                Double lat = location.latitude;
                                Double lon = location.longitude;
                                mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lon)).snippet("Click")
                                .title(d.getKey())).showInfoWindow();
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon),10));
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        TrackEmployeeActivity.phonenumber=marker.getTitle();
        TrackEmployeeActivity.markeposition=marker.getPosition();
        startActivity(new Intent(AdminActivity.this,TrackEmployeeActivity.class));
    }
}
