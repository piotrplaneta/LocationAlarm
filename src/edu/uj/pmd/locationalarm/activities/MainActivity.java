package edu.uj.pmd.locationalarm.activities;

import java.util.List;

import android.content.res.Configuration;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import edu.uj.pmd.locationalarm.*;
import edu.uj.pmd.locationalarm.listeners.MainActivityLocationListener;
import edu.uj.pmd.locationalarm.overlays.DestinationItemizedOverlay;
import edu.uj.pmd.locationalarm.overlays.DestinationRadiusOverlay;
import edu.uj.pmd.locationalarm.utilities.AlarmNotificationUtilities;
import edu.uj.pmd.locationalarm.utilities.AppPreferences;
import edu.uj.pmd.locationalarm.utilities.LocalizationUtilities;

public class MainActivity extends MapActivity {
    private int FAVORITE_DESTINATION_REQUEST_CODE = 12345;
	private Location currentBestLocation;

    private DestinationItemizedOverlay destinationOverlay;
    private DestinationRadiusOverlay destinationRadiusOverlay;

    private GeoPoint currentBestLocationPoint = null;
    private GeoPoint destinationPoint = null;

	public boolean isInDestinationSelectMode = false;
	public boolean scrollToCurrentLocation = true;

    private LocationListener locationListener;
    private AppPreferences appPrefs;

    public void setCurrentBestLocationPoint(GeoPoint currentBestLocationPoint) {
        this.currentBestLocationPoint = currentBestLocationPoint;
    }

    public Location getCurrentBestLocation() {
        return currentBestLocation;
    }

    public void setCurrentBestLocation(Location currentBestLocation) {
        this.currentBestLocation = currentBestLocation;
    }

    public GeoPoint getDestinationPoint() {
        return destinationPoint;
    }

    @Override
	protected boolean isRouteDisplayed() {
	    return false;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.locationListener = new MainActivityLocationListener(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appPrefs = new AppPreferences(getApplicationContext());

        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);

        LocalizationUtilities.checkLocalizationServices(this);

        Drawable drawable_destination = this.getResources().getDrawable(R.drawable.destination_marker);
        destinationOverlay = new DestinationItemizedOverlay(drawable_destination, this);
        setScrollMode();
        setDestinationIfAvailable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        localize();
        if(appPrefs.getDestinationRadius() > 0) {
            AlarmNotificationUtilities.printStartingToast(this);
            AlarmNotificationUtilities.updateNotification(this, (String) getText(R.string.init_update), -1);
            LocalizationUtilities.setLocalizationServices(this);
            LocalizationUtilities.setNoLocationAlarm(this);
        }
    }

    @Override
    public void onBackPressed() {
        if (isInDestinationSelectMode) {
            setScrollMode();
        } else if (destinationPoint != null) {
            clearDestination(null);
        } else {
            finish();
        }
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
    	MapView mapView = (MapView) findViewById(R.id.mapview);
        int actionType = ev.getAction();
        if (isInDestinationSelectMode) {
        	Button destinationButton = (Button) findViewById(R.id.destinationButton);

            int[] pos = new int[2];
            destinationButton.getLocationOnScreen(pos);

            if(ev.getX() >= pos[0] && ev.getX() <= pos[0] + destinationButton.getWidth() &&
                    ev.getY() >= pos[1] && ev.getY() <= pos[1] + destinationButton.getHeight()) {
            	return destinationButton.onTouchEvent(ev);
            } else {
            	switch (actionType) {
    	        	case MotionEvent.ACTION_UP:
    					Projection projection = mapView.getProjection();
    					GeoPoint loc = projection.fromPixels((int)ev.getX(), (int)ev.getY());
    					double longitude = loc.getLongitudeE6()/1000000.0;
    					double latitude = loc.getLatitudeE6()/1000000.0;
                        appPrefs.setDestinationLongitude((float)longitude);
                        appPrefs.setDestinationLatitude((float)latitude);

    					Intent intent = new Intent(this, AlarmDetailsActivity.class);
    			    	this.startActivity(intent);
                        this.finish();
            	}
            }
            return true;
        }
        else {
        	return super.dispatchTouchEvent(ev);
        }
    }
    
    public void toggleMode(View view) {
    	Button button = (Button) findViewById(R.id.destinationButton);
    	if(button.getText() == getText(R.string.adjust_map)) {
    		button.setText(getText(R.string.select_destination));
    		isInDestinationSelectMode = false;
            findViewById(R.id.showFavoritesButton).setVisibility(View.VISIBLE);
    		
    	} else {
    		button.setText(getText(R.string.adjust_map));
    		isInDestinationSelectMode = true;
            findViewById(R.id.showFavoritesButton).setVisibility(View.GONE);
    	}
    }

    public void showFavorites(View view) {
        Intent intent = new Intent(this, FavoritesListActivity.class);
        startActivityForResult(intent, FAVORITE_DESTINATION_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FAVORITE_DESTINATION_REQUEST_CODE) {
            if(resultCode == RESULT_OK){
                Intent intent = new Intent(this, AlarmDetailsActivity.class);
                intent.putExtra("fromFavorites", true);
                this.startActivity(intent);
                this.finish();
            }
        }
    }

    public void setScrollMode() {
        Button button = (Button) findViewById(R.id.destinationButton);
        button.setText(getText(R.string.select_destination));
        isInDestinationSelectMode = false;
    }

    public void clearDestination(View view) {
        appPrefs.clearDestination();
        findViewById(R.id.clearDestinationButton).setVisibility(View.GONE);

        MapView mapView = (MapView) findViewById(R.id.mapview);
        List<Overlay> mapOverlays = mapView.getOverlays();
        mapOverlays.remove(destinationOverlay);
        mapOverlays.remove(destinationRadiusOverlay);

        this.scrollToCurrentLocation = true;
        this.destinationPoint = null;

        LocalizationUtilities.cancelNetworkLocalizationService(this);
        LocalizationUtilities.cancelGpsLocalizationService(this);
        LocalizationUtilities.removeNoLocationAlarm(this);
        AlarmNotificationUtilities.removeNotification(this);
        Toast.makeText(this, getText(R.string.stopped), Toast.LENGTH_SHORT).show();
    }
    
    private void setDestinationIfAvailable() {
    	if (appPrefs.getDestinationRadius() > 0) {
    		scrollToCurrentLocation = false;
            findViewById(R.id.clearDestinationButton).setVisibility(View.VISIBLE);
    		
    		double longitude = appPrefs.getDestinationLongitude();
            double latitude = appPrefs.getDestinationLatitude();
            int radius = appPrefs.getDestinationRadius();

    		MapView mapView = (MapView) findViewById(R.id.mapview);
	        List<Overlay> mapOverlays = mapView.getOverlays();

	        String destinationString = getText(R.string.latitude) + ": " + String.valueOf(latitude) + "\n" + getText(R.string.longitude) + ": " +
                    String.valueOf(longitude) + "\n" + getText(R.string.radius) + ": " + String.valueOf(radius);
	        GeoPoint position = new GeoPoint((int)(latitude*1000000), (int)(longitude*1000000));
            destinationPoint = position;
            String text = (String) getText(R.string.destination);
            OverlayItem item = new OverlayItem(position, text, destinationString);


            mapOverlays.remove(destinationRadiusOverlay);
            destinationRadiusOverlay = new DestinationRadiusOverlay();
            destinationRadiusOverlay.setSource(position, radius*1000);
            mapOverlays.add(destinationRadiusOverlay);

	        mapOverlays.remove(destinationOverlay);
            destinationOverlay.clear();
            destinationOverlay.add(item);
	        mapOverlays.add(destinationOverlay);

            mapView.invalidate();
    	} else {
            scrollToCurrentLocation = true;
        }
    }
    
    private void localize() {
        MainActivityLocationListener.addListeners(this, this.locationListener);
    }

    public void zoomOutToLocationAndDestination() {
        MapView mapView = (MapView) findViewById(R.id.mapview);
        MapController mapController = mapView.getController();
        int minLat = Integer.MAX_VALUE;
        int maxLat = Integer.MIN_VALUE;
        int minLon = Integer.MAX_VALUE;
        int maxLon = Integer.MIN_VALUE;

        GeoPoint[] points = new GeoPoint[2];
        points[0] = currentBestLocationPoint;
        points[1] = destinationPoint;

        for (GeoPoint item : points) {
            int lat = item.getLatitudeE6();
            int lon = item.getLongitudeE6();

            maxLat = Math.max(lat, maxLat);
            minLat = Math.min(lat, minLat);
            maxLon = Math.max(lon, maxLon);
            minLon = Math.min(lon, minLon);
        }

        double fitFactor = 1.2;
        mapController.zoomToSpan((int) (Math.abs(maxLat - minLat) * fitFactor),
                (int)(Math.abs(maxLon - minLon) * fitFactor));
        mapController.animateTo(new GeoPoint( (maxLat + minLat)/2,
                (maxLon + minLon)/2 ));
    }


    @Override
    protected void onPause() {
        MainActivityLocationListener.removeListeners(this, this.locationListener);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LocalizationUtilities.cancelNetworkLocalizationService(this);
        LocalizationUtilities.cancelGpsLocalizationService(this);
        LocalizationUtilities.removeNoLocationAlarm(this);
        AlarmNotificationUtilities.removeNotification(this);
        super.onDestroy();
    }
}
