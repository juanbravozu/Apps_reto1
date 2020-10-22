package com.juanbravo.reto1;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.SphericalUtil;

import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, View.OnClickListener {

    private GoogleMap mMap;
    private Button addHole;
    private TextView closestText;
    private LocationManager manager;
    private Marker mPosMarker;
    private User mUser;
    private ArrayList<User> users;
    private ArrayList<Pothole> potholes;
    private AddHoleFragment modal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        closestText = findViewById(R.id.map_amount_tv);
        addHole = findViewById(R.id.map_add_btn);
        addHole.setOnClickListener(this);

        users = new ArrayList<User>();
        potholes = new ArrayList<Pothole>();
    }

    //Listener cuando carga el mapa
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, this);

        setInitialLocation();
    }

    //Coloca mi marcador por primera vez
    @SuppressLint("MissingPermission")
    public void setInitialLocation() {
        //Setup user marker and information
         Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
         if(location != null) {
             updateMyMarker(location);
             Bundle extras = getIntent().getExtras();
             mUser = new User(extras.getString("name"), extras.getString("id"), location.getLatitude(), location.getLongitude());
             updatePosDatabase();

             //Reads other users information from the database
             new Thread(
                     () -> {
                         while(true) {
                             try {
                                 updateUsers();
                                 updatePotholes();
                                 runOnUiThread(() -> {updateDistanceRelatedText();});
                                 Thread.sleep(5000);
                             } catch (InterruptedException e) {
                                 e.printStackTrace();
                             }
                         }
                     }
             ).start();
         }
    }

    //Compare distance between potholes and the user and returns the distance between the closest pothole and the user
    public int distanceToClosestPothole() {
        int minimumDistance = 1000;

        for(Pothole pothole : potholes) {
            if(minimumDistance > (int)Math.floor(SphericalUtil.computeDistanceBetween(new LatLng(mUser.getLatitude(), mUser.getLongitude()), new LatLng(pothole.getLatitude(), pothole.getLongitude())))) {
                minimumDistance = (int) Math.floor(SphericalUtil.computeDistanceBetween(new LatLng(mUser.getLatitude(), mUser.getLongitude()), new LatLng(pothole.getLatitude(), pothole.getLongitude())));
            }
        }

        return minimumDistance;
    }

    public Pothole getClosestPothole() {
        Pothole closestPothole = null;
        int distance = 1000000;

        for(Pothole pothole : potholes) {
            if(distance > (int)Math.floor(SphericalUtil.computeDistanceBetween(new LatLng(mUser.getLatitude(), mUser.getLongitude()), new LatLng(pothole.getLatitude(), pothole.getLongitude())))) {
                distance = (int)Math.floor(SphericalUtil.computeDistanceBetween(new LatLng(mUser.getLatitude(), mUser.getLongitude()), new LatLng(pothole.getLatitude(), pothole.getLongitude())));
                closestPothole = pothole;
            }
        }

        return closestPothole;
    }

    //Updates the users ArrayList with information from the database
    @SuppressLint("MissingPermission")
    public void updateUsers() {
        HTTPSWebUtilDomi https = new HTTPSWebUtilDomi();

        if(mUser == null) {
            Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Bundle extras = getIntent().getExtras();
            mUser = new User(extras.getString("name"), extras.getString("id"), location.getLatitude(), location.getLongitude());
        }

        new Thread(
                () -> {
                    String response = https.GETrequest(Constants.BASEURL+"users.json");
                    Gson gson = new Gson();

                    Type type = new TypeToken<HashMap<String, User>>(){}.getType();
                    HashMap<String, User> newUsers = gson.fromJson(response, type);

                    newUsers.forEach(
                            (key, value) -> {
                                if(!value.getId().equals(mUser.getId())) {

                                    //Checks if the user is already listed on the ArrayList
                                    boolean isListed = false;

                                    for(User user : users) {
                                        if(user.getId().equals(value.getId())) {
                                            user.setLatitude(value.getLatitude());
                                            user.setLongitude(value.getLongitude());
                                            if(user.getMarker() == null) {
                                                runOnUiThread(
                                                        () -> {
                                                            user.setMarker(mMap.addMarker(new MarkerOptions().position(new LatLng(value.getLatitude(), value.getLongitude())).title(value.getName())));
                                                        }
                                                );
                                            } else {
                                                runOnUiThread(
                                                        () -> {
                                                            user.setMarker(new LatLng(user.getLatitude(), user.getLongitude()));
                                                        }
                                                );
                                            }
                                            isListed = true;
                                        }
                                    }

                                    if(!isListed) {
                                        runOnUiThread(
                                                () -> {
                                                    value.setMarker(mMap.addMarker(new MarkerOptions().position(new LatLng(value.getLatitude(), value.getLongitude())).title(value.getName())));
                                                }
                                        );
                                        users.add(value);
                                    }
                                }
                            }
                    );

                }
        ).start();
    }

    //Updates the potholes ArrayList with new information from the databse
    @SuppressLint("MissingPermission")
    public void updatePotholes() {
        HTTPSWebUtilDomi https = new HTTPSWebUtilDomi();

        new Thread(
                () -> {
                    String response = https.GETrequest(Constants.BASEURL+"potholes.json");
                    Gson gson = new Gson();

                    Type type = new TypeToken<HashMap<String, Pothole>>(){}.getType();
                    HashMap<String, Pothole> newPotholes = gson.fromJson(response, type);

                    if(newPotholes != null) {
                        newPotholes.forEach(
                                (key, value) -> {
                                    boolean isListed = false;
                                    for(Pothole pothole : potholes) {
                                        if(pothole.getId().equals(value.getId())) {
                                            pothole.setConfirmed(value.isConfirmed());
                                            if(pothole.getMarker() == null) {
                                                runOnUiThread(
                                                        () -> {
                                                            pothole.setMarker(mMap.addMarker(new MarkerOptions().position(new LatLng(value.getLatitude(), value.getLongitude())).title("Hueco")));
                                                            pothole.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(50));
                                                        }
                                                );
                                            } else {
                                                runOnUiThread(
                                                        () -> {
                                                            if(pothole.isConfirmed()) {
                                                                pothole.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(35));
                                                            } else {
                                                                pothole.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(50));
                                                            }
                                                            pothole.setMarker(new LatLng(pothole.getLatitude(), pothole.getLongitude()));
                                                        }
                                                );
                                            }
                                            isListed = true;
                                        }
                                    }

                                    if(!isListed) {
                                        potholes.add(value);
                                    }
                                }
                        );
                    }
                }
        ).start();
    }

    //Actualiza mi posición en la base de datos
    public void updatePosDatabase() {
        if(mUser != null) {
            Gson gson = new Gson();
            String json = gson.toJson(mUser);

            HTTPSWebUtilDomi https = new HTTPSWebUtilDomi();
            new Thread(
                    () -> {
                        https.PUTrequest(Constants.BASEURL+"users/"+mUser.getId()+".json", json);
                    }
            ).start();
        }
    }

    //Método que actualiza mi marcador
    public void updateMyMarker(Location location) {
        LatLng mPos = new LatLng(location.getLatitude(), location.getLongitude());
        if(mUser != null) {
            mUser.setLatitude(location.getLatitude());
            mUser.setLongitude(location.getLongitude());
        }

        updatePosDatabase();
        if(mPosMarker == null) {
            mPosMarker = mMap.addMarker(new MarkerOptions().position(mPos).title("Yo"));
            mPosMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        } else {
            mPosMarker.setPosition(mPos);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mPos, 18));
    }

    public void updateDistanceRelatedText() {
        int distance = distanceToClosestPothole();
        if(distance < 1000) {
            closestText.setText("Hueco a "+ distance + " metros");

            if(distance < 20) {
                addHole.setText("Confirmar hueco");
            } else {
                addHole.setText("Reportar hueco");
            }
        } else {
            closestText.setText("No tienes huecos cerca");
        }
    }

    //Listener para moverse
    @Override
    public void onLocationChanged(@NonNull Location location) {
        updateMyMarker(location);
        runOnUiThread(
                () -> {
                    updateDistanceRelatedText();
                }
        );
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.map_add_btn:
                if(distanceToClosestPothole() > 20) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    if(transaction.isEmpty()) {

                        try {
                            //Sets up the information for the "New Pothole" modal
                            @SuppressLint("MissingPermission") Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            Bundle extras = getIntent().getExtras();
                            Geocoder geocoder = new Geocoder(MapsActivity.this.getApplicationContext(), Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            modal = AddHoleFragment.newInstance(location.getLatitude(), location.getLongitude(), addresses.get(0).getAddressLine(0), extras.getString("id"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        transaction.add(R.id.map_modal_layout, modal).commit();
                    }
                } else {
                    Pothole closest = getClosestPothole();
                    if(closest.isConfirmed()) {
                        Toast.makeText(this, "Este hueco ya está confirmado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Hueco confirmado", Toast.LENGTH_SHORT).show();
                        closest.setConfirmed(true);
                    }

                    HTTPSWebUtilDomi https = new HTTPSWebUtilDomi();
                    //Had to do the json manually since Gson was presenting errors
                    String json = "{\"authorId\":\"" + closest.getAuthorId() + "\",\"confirmed\":" +closest.isConfirmed()+ ",\"id\":\"" + closest.getId() + "\",\"latitude\":" + closest.getLatitude() + ",\"longitude\":" + closest.getLongitude() + "}";

                    new Thread(
                            () -> {
                                https.PUTrequest(Constants.BASEURL+"potholes/"+closest.getId()+".json", json);
                            }
                    ).start();
                }
                break;
        }
    }

    //Delete user from database when app is paused
    @Override
    protected void onPause() {
        super.onPause();
        HTTPSWebUtilDomi https = new HTTPSWebUtilDomi();

        new Thread(
                () -> {
                    https.DELETErequest(Constants.BASEURL+"users/"+mUser.getId()+".json");
                }
        ).start();
    }

    //Add again user to database when app is resumed
    @Override
    protected void onResume() {
        super.onResume();
        HTTPSWebUtilDomi https = new HTTPSWebUtilDomi();

        new Thread(
                () -> {
                    updatePosDatabase();
                }
        ).start();
    }

    //No los voy a usar
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}