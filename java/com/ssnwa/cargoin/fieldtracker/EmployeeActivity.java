package com.ssnwa.cargoin.fieldtracker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.nio.channels.Channel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EmployeeActivity extends AppCompatActivity {
    Button start,setlocation;
    String ChannelID="Tracking";
    int notification_id=001;
    FirebaseDatabase database;
    DatabaseReference ref;

    private FusedLocationProviderClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);

        database=FirebaseDatabase.getInstance();
        ref=database.getReference();

        client = LocationServices.getFusedLocationProviderClient(this);
        start=findViewById(R.id.starttracking);
        setlocation=findViewById(R.id.set_current_location);

        setlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(EmployeeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(EmployeeActivity.this
                        , Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M)
                    {
                        int my_Request = 177;
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                                my_Request);
                    }
                    return;
                }

                client.getLastLocation().addOnSuccessListener(EmployeeActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            final Double lat = location.getLatitude();
                            final Double lon = location.getLongitude();
                            String phoneNumber= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                            String Time = format.format(calendar.getTime());
                            Date c = Calendar.getInstance().getTime();
                            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                            String formattedDate = df.format(c);
                            ref.child("Employee").child(phoneNumber).child(formattedDate).child("startLocation")
                                    .child("latitude").setValue(lat);

                            ref.child("Employee").child(phoneNumber).child(formattedDate).child("startLocation")
                                    .child("longitude").setValue(lon);
                        }
                    }
                });

                setlocation.setVisibility(View.GONE);
                start.setVisibility(View.VISIBLE);

            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {

                if (ActivityCompat.checkSelfPermission(EmployeeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(EmployeeActivity.this
                        , Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M)
                    {
                        int my_Request = 177;
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                                my_Request);
                    }
                    return;
                }

                client.getLastLocation().addOnSuccessListener(EmployeeActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            final Double lat = location.getLatitude();
                            final Double lon = location.getLongitude();
                            String phoneNumber= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                            String Time = format.format(calendar.getTime());
                            Date c = Calendar.getInstance().getTime();
                            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                            String formattedDate = df.format(c);
                            GeoFire geoFire=new GeoFire(ref.child("Employee").child(phoneNumber).child(formattedDate));
                            geoFire.setLocation("currentlocation", new GeoLocation(lat, lon), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    if (error != null) {
                                        Toast.makeText(EmployeeActivity.this, "There was an error saving the location to GeoFire: "
                                                + error, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }
                });

                createnotoficationchannel();


                Intent closeapp=new Intent(EmployeeActivity.this,CloseAppActivity.class);
                closeapp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

                PendingIntent close=PendingIntent.getActivity(EmployeeActivity.this,
                        0,closeapp,PendingIntent.FLAG_ONE_SHOT);

                NotificationCompat.Builder builder=new NotificationCompat.Builder(EmployeeActivity.this, ChannelID);
                builder.setSmallIcon(R.drawable.ic_location_on)
                        .setAutoCancel(true)
                        .setContentTitle("Field Tracker")
                        .setContentText("You are being tracked!")
                        .setPriority(NotificationCompat.PRIORITY_HIGH);


                builder.addAction(R.drawable.ic_location_on,"Close",close);

                NotificationManagerCompat notificationManagerCompat= NotificationManagerCompat.from(EmployeeActivity.this);
                notificationManagerCompat.notify(notification_id,builder.build());
                finish();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createnotoficationchannel() {

        String name="On Track";
        String description="Field tracking";

        int importance= NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel notificationChannel=new NotificationChannel(ChannelID,name,importance);
        notificationChannel.setDescription(description);

        NotificationManager notificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);

    }

}
