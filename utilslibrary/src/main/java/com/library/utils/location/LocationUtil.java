package com.library.utils.location;

import android.content.Context;
import android.location.Location;
import com.google.android.gms.maps.model.LatLng;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationParams;

/**
 * Created by Shripal17 on 19-08-2017.
 */

public class LocationUtil {
  
  private static final String TAG = "LocationUtil";
  private static LocationUtil mInstance = null;
  private Location location;
  private double lat;
  private double lon;
  private String city;
  private Context ctx;
  private LatLng latLng;
  private boolean ready = false;
  private MyLocationListener myLocationListener;
  
  private LocationUtil(Context ctx) {
    this.ctx = ctx;
    SmartLocation.with(ctx).location().config(LocationParams.NAVIGATION).start(new OnLocationUpdatedListener() {
      @Override
      public void onLocationUpdated(Location location) {
        LocationUtil.this.location = location;
        LocationUtil.this.lat = location.getLatitude();
        LocationUtil.this.lon = location.getLongitude();
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if(myLocationListener!=null) {
          myLocationListener.onLocationUpdate(location, !ready);
        }
        ready = true;
      }
    });
  }
  
  public static LocationUtil getInstance(Context ctx, final MyLocationListener myLocationListener) {
    if (mInstance == null) {
      mInstance = new LocationUtil(ctx);
    }
    mInstance.myLocationListener = myLocationListener;
    return mInstance;
  }
  public static LocationUtil getInstance(Context ctx) {
    if (mInstance == null) {
      mInstance = new LocationUtil(ctx);
    }
    return mInstance;
  }
  
  public Location getLocation() {
    if (ready) {
      return location;
    }
    return null;
  }
  
  public LatLng getLatLng() {
    if (ready) {
      return latLng;
    }
    return null;
  }
  
  public double getLat() {
    if (ready) {
      return lat;
    }
    return 0;
  }
  
  public double getLon() {
    if(ready) {
      return lon;
    }
    return 0;
  }
  
  public String getCity() {
    if(ready) {
      return city;
    }
    return "not ready yet";
  }
  
  public boolean isReady(){
    return ready;
  }

  public interface MyLocationListener {
    void onLocationUpdate(Location location, boolean isFirstUpdate);
  }


  public static double distance(double lat1, double lon1, double lat2, double lon2) {
    final int R = 6371; // Radius of the earth
    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);
    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double distance = R * c * 1000; // convert to meters
    return distance;
  }

  public double distance(double lat2, double lon2) {
    double lat1 = lat;
    double lon1 = lon;
    final int R = 6371; // Radius of the earth
    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);
    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double distance = R * c * 1000; // convert to meters
    return distance;
  }

  public void destroy(){
    myLocationListener = null;
    try {
      this.finalize();
    }catch (Throwable e){
      e.printStackTrace();
    }
  }
}
