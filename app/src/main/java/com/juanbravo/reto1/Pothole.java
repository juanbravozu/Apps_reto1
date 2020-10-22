package com.juanbravo.reto1;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class Pothole {

    private String id;
    private double latitude;
    private double longitude;
    private boolean confirmed;
    private Marker marker;
    private String authorId; //Este nonas xdxd

    public Pothole() {
    }

    public Pothole(String id, double latitude, double longitude, String authorId) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.authorId = authorId;
        this.confirmed = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        if(this.marker == null) {
            this.marker = marker;
        }
    }

    public void setMarker(LatLng pos) {
        this.marker.setPosition(pos);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
