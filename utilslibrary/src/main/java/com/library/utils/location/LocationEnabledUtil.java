package com.library.utils.location;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

/**
 * Created by Shripal17 on 11-08-2017.
 */

public class LocationEnabledUtil {
  
  private static GoogleApiClient googleApiClient;
  
  public static void checkAndAskLocation(final Activity ctx, final OnLocationActionCallback action, final int requestCode) {
    final LocationManager manager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
      try {
        googleApiClient = new GoogleApiClient.Builder(ctx)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
              @Override
              public void onConnected(Bundle bundle) {
              
              }
              
              @Override
              public void onConnectionSuspended(int i) {
                googleApiClient.connect();
              }
            })
            .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
              @Override
              public void onConnectionFailed(ConnectionResult connectionResult) {
                
                Log.d("Location error", "Location error " + connectionResult.getErrorCode());
              }
            }).build();
        googleApiClient.connect();
        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest);
        
        builder.setAlwaysShow(true);
        
        PendingResult<LocationSettingsResult> result =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
          @Override
          public void onResult(LocationSettingsResult result) {
            final Status status = result.getStatus();
            switch (status.getStatusCode()) {
              case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                  // Show the dialog by calling startResolutionForResult(),
                  // and check the result in onActivityResult().
                  status.startResolutionForResult(ctx, requestCode);
                  
                } catch (IntentSender.SendIntentException e) {
                  // Ignore the error.
                }
                break;
              case LocationSettingsStatusCodes.SUCCESS:
                action.onEnabled();
                break;
              
              default:
                action.onCancelled();
                break;
            }
          }
        });
      } catch (Exception e) {
        e.printStackTrace();
      }
      
    } else {
      action.onEnabled();
    }
  }
  
  /*public static void checkAndAskLocation(final Activity ctx, final OnLocationActionCallback action, final int requestCode) {
    final LocationManager manager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
      AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
      dialog.setTitle("Enable Device's Location")
          .setMessage("Device's location setting is 'OFF'.\nPlease enable it to continue.")
          .setPositiveButton("Enable now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
              try {
                                    *//*Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    mActivity.startActivity(myIntent);*//*
                googleApiClient = new GoogleApiClient.Builder(ctx)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                      @Override
                      public void onConnected(Bundle bundle) {
                      
                      }
                      
                      @Override
                      public void onConnectionSuspended(int i) {
                        googleApiClient.connect();
                      }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                      @Override
                      public void onConnectionFailed(ConnectionResult connectionResult) {
                        
                        Log.d("Location error", "Location error " + connectionResult.getErrorCode());
                      }
                    }).build();
                googleApiClient.connect();
                final LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(30 * 1000);
                locationRequest.setFastestInterval(5 * 1000);
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
                
                builder.setAlwaysShow(true);
                
                PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
                result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                  @Override
                  public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                      case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                          // Show the dialog by calling startResolutionForResult(),
                          // and check the result in onActivityResult().
                          status.startResolutionForResult(ctx, requestCode);
      
                        } catch (IntentSender.SendIntentException e) {
                          // Ignore the error.
                        }
                        break;
                      case LocationSettingsStatusCodes.SUCCESS:
                        action.onEnabled();
                        break;
                      
                      default:
                        action.onCancelled();
                        break;
                    }
                  }
                });
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          })
          .setCancelable(false)
          .setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
              ctx.finish();
            }
          });
      
      
      dialog.create().show();
      
    } else {
      action.onEnabled();
    }
  }*/
  
  public interface OnLocationActionCallback {
    
    void onEnabled();
    
    void onCancelled();
  }
}
