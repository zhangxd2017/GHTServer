package cn.ght.server.bean;

public class LocationInfo {

    private double longitude;

    private double latitude;

    public LocationInfo() {
    }

    public LocationInfo(double longtitude, double latitude) {
        this.longitude = longtitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
