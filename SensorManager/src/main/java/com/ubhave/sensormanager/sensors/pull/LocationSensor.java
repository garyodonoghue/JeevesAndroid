/* **************************************************
 Copyright (c) 2012, University of Cambridge
 Neal Lathia, neal.lathia@cl.cam.ac.uk
 Kiran Rachuri, kiran.rachuri@cl.cam.ac.uk

This library was developed as part of the EPSRC Ubhave (Ubiquitous and
Social Computing for Positive Behaviour Change) Project. For more
information, please visit http://www.emotionsense.org

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ************************************************** */

package com.ubhave.sensormanager.sensors.pull;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.config.GlobalConfig;
import com.ubhave.sensormanager.config.pull.LocationConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pull.LocationData;
import com.ubhave.sensormanager.process.pull.LocationProcessor;
import com.ubhave.sensormanager.sensors.SensorUtils;

import static android.support.v4.content.ContextCompat.checkSelfPermission;

public class LocationSensor extends AbstractPullSensor implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	private static final String TAG = "LocationSensor";
	private static Location mLastLocation;
	private static final String[] LOCATION_PERMISSIONS = new String[]{
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.ACCESS_FINE_LOCATION
	};

	private static LocationSensor locationSensor;
	private static Object lock = new Object();

	//private LocationManager locationManager;
	private List<Location> locationList;
	private LocationListener locListener;
	private LocationData locationData;
	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;
//	private Location mLastLocation;

	public static LocationSensor getSensor(final Context context) throws ESException {

		if (locationSensor == null) {
			synchronized (lock) {
				if (locationSensor == null) {
					if (anyPermissionGranted(context, LOCATION_PERMISSIONS)) {
						locationSensor = new LocationSensor(context);
					} else {
						Log.d("BOOM ", "SHKALAKA");
						throw new ESException(ESException.PERMISSION_DENIED, SensorUtils.SENSOR_NAME_LOCATION);
					}
				}
			}
		}
		return locationSensor;
	}

	private LocationSensor(Context context) {
		super(context);
		locationList = new ArrayList<Location>();

		locListener = new LocationListener() {

			public void onLocationChanged(Location loc) {
		//		if (isSensing) {
					locationList.add(loc);
					Log.d(TAG,"Oooh look we changed a bit!");
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
				SharedPreferences.Editor prefseditor = preferences.edit();
				prefseditor.putString("LastLocation",loc.getLatitude()+";" +loc.getLongitude());
				prefseditor.commit();
		//		}
			}
		};
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(context)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.build();

		}
		mGoogleApiClient.connect();

		//	locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

	}

	public String getLogTag() {
		return TAG;
	}

	public int getSensorType() {
		return SensorUtils.SENSOR_TYPE_LOCATION;
	}

	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(LocationConfig.DEFAULT_SLEEP_INTERVAL);
		mLocationRequest.setFastestInterval(LocationConfig.DEFAULT_SLEEP_INTERVAL);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
				.addLocationRequest(mLocationRequest);

		PendingResult<LocationSettingsResult> result =
				LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
						builder.build());
		result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
			@Override
			public void onResult(LocationSettingsResult result) {
				final Status status = result.getStatus();
				switch (status.getStatusCode()) {
					case LocationSettingsStatusCodes.SUCCESS:
						break;
					case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
						Intent intended = new Intent();
						intended.setAction(SensorUtils.LOCATION_REQUIRED);

						applicationContext.sendBroadcast(intended);
						break;
					case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
						break;
				}
			}
		});
	}

	protected boolean startSensing() {
		Log.d("Gonna","do some location sensing");

		//Every time we start sensing, want to make sure that the sensor's actually on
		createLocationRequest();

		return true;
	}

	protected void stopSensing() {
	}

	protected SensorData getMostRecentRawData() {
		return locationData;
	}

	protected void processSensorData() {
		LocationProcessor processor = (LocationProcessor) getProcessor();
		locationData = processor.process(pullSenseStartTimestamp, locationList, sensorConfig.clone());
		locationList = new ArrayList<Location>();

	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		createLocationRequest();
		mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
				mGoogleApiClient);
		if (mLastLocation != null) {
			Log.d(TAG,"Last known location: " + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
			SharedPreferences.Editor prefseditor = preferences.edit();
			prefseditor.putString("LastLocation",mLastLocation.getLatitude()+";" +mLastLocation.getLongitude());
			prefseditor.commit();
		}
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, locListener);

	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}

}
