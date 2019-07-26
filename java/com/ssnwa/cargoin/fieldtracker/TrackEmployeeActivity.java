package com.ssnwa.cargoin.fieldtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TrackEmployeeActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener ,RoutingListener {

    private GoogleMap mMap;

    public static String phonenumber;
    FirebaseDatabase database;
    DatabaseReference reference;

    public static LatLng markeposition;

    private List<Polyline> polylines=new ArrayList<>();
    private static final int[] COLORS = new int[]
            {R.color.colorPrimary,R.color.colorPrimaryDark,R.color.common_google_signin_btn_text_dark_focused,R.color.colorAccent,
                    R.color.primary_dark_material_light};

    public String apikey="your-api-key-here";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_employee);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        database=FirebaseDatabase.getInstance();
        reference=database.getReference("Employee");

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

        // Add a marker in Sydney and move the camera
            reference.child(phonenumber).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Date c = Calendar.getInstance().getTime();
                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                    String formattedDate = df.format(c);
                    Double startlat=dataSnapshot.child(formattedDate).child("startLocation").child("latitude").getValue(Double.class);
                    Double startlon=dataSnapshot.child(formattedDate).child("startLocation").child("longitude").getValue(Double.class);
                    mMap.addMarker(new MarkerOptions().position(new LatLng(startlat,startlon))
                    .title("StartLocation")).showInfoWindow();
                    mMap.addMarker(new MarkerOptions().position(markeposition)
                            .title(phonenumber)).showInfoWindow();
                    Routing routing = new Routing.Builder()
                            .key(apikey)
                            .travelMode(AbstractRouting.TravelMode.DRIVING)
                            .withListener(TrackEmployeeActivity.this)
                            .alternativeRoutes(false)
                            .waypoints(new LatLng(startlat,startlon), markeposition)
                            .build();
                    routing.execute();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markeposition,10));

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
//        mMap.moveCamera(CameraUpdateFactory.newLatLng());
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e!=null)
        {
            Toast.makeText(this,"Error"+e.getMessage(),Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this,"Something went wrong, Please try again later!",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        CameraUpdate center = CameraUpdateFactory.newLatLngZoom(markeposition,15);

        mMap.moveCamera(center);

        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

//            Toast.makeText(getApplicationContext(), "Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()
//                    +": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_LONG).show();
            // timetoreach.setText((route.get(i).getDurationValue())/60+" min");
        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
