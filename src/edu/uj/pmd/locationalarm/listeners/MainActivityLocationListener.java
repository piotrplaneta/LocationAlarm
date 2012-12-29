package edu.uj.pmd.locationalarm.listeners;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import com.google.android.maps.*;
import edu.uj.pmd.locationalarm.R;
import edu.uj.pmd.locationalarm.activities.MainActivity;
import edu.uj.pmd.locationalarm.overlays.LocationAccuracyRadiusOverlay;
import edu.uj.pmd.locationalarm.overlays.LocationItemizedOverlay;
import edu.uj.pmd.locationalarm.utilities.LocalizationUtilities;

import java.util.List;

/**
 * User: piotrplaneta
 * Date: 19.11.2012
 * Time: 22:17
 */
public class MainActivityLocationListener implements LocationListener {
    private MainActivity activity;
    private LocalizationUtilities localizationUtilities = new LocalizationUtilities();
    private LocationItemizedOverlay locationOverlay;
    private LocationAccuracyRadiusOverlay locationAccuracyRadiusOverlay;

    public static void addListeners(Context context, LocationListener listener) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getAllProviders();
        if(providers.contains("network")) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
        }
        if(providers.contains("gps")) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        }
    }

    public static void removeListeners(Context context, LocationListener listener) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(listener);
    }


    public MainActivityLocationListener(MainActivity activity) {
        this.activity = activity;

        Drawable drawable_location = activity.getResources().getDrawable(R.drawable.location_marker);
        this.locationOverlay = new LocationItemizedOverlay(drawable_location, activity);
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }

    public void onLocationChanged(Location location) {
        if(localizationUtilities.isBetterLocation(location)) {
            activity.setCurrentBestLocation(location);

            MapView mapView = (MapView) activity.findViewById(R.id.mapview);
            List<Overlay> mapOverlays = mapView.getOverlays();
            String positionString = activity.getString(R.string.latitude) + ": " + String.valueOf(activity.getCurrentBestLocation().getLatitude()) +
                    "\n" + activity.getString(R.string.longitude) + ": " + String.valueOf(activity.getCurrentBestLocation().getLongitude());

            GeoPoint newPosition = new GeoPoint((int)(activity.getCurrentBestLocation().getLatitude()*1000000),
                    (int)(activity.getCurrentBestLocation().getLongitude()*1000000));

            String text = (String) activity.getText(R.string.my_location);
            OverlayItem newItem = new OverlayItem(newPosition, text, positionString);
            activity.setCurrentBestLocationPoint(newPosition);

            mapOverlays.remove(this.locationAccuracyRadiusOverlay);
            this.locationAccuracyRadiusOverlay = new LocationAccuracyRadiusOverlay();
            this.locationAccuracyRadiusOverlay.setSource(newPosition, location.getAccuracy());
            mapOverlays.add(this.locationAccuracyRadiusOverlay);

            mapOverlays.remove(this.locationOverlay);
            this.locationOverlay.clear();
            this.locationOverlay.add(newItem);
            mapOverlays.add(this.locationOverlay);
            mapView.invalidate();

            if (activity.scrollToCurrentLocation) {
                MapController mapController = mapView.getController();
                mapController.animateTo(newPosition);
                activity.scrollToCurrentLocation = false;
            } else if (!activity.scrollToCurrentLocation && activity.getDestinationPoint() != null) {
                activity.zoomOutToLocationAndDestination();
            }
        }
    }

}
