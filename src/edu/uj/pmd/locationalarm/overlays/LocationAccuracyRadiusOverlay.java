package edu.uj.pmd.locationalarm.overlays;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * User: piotrplaneta
 * Date: 16.11.2012
 * Time: 00:49
 */
public class LocationAccuracyRadiusOverlay extends Overlay {

    private GeoPoint sourcePoint;
    private float radius;

    public LocationAccuracyRadiusOverlay() {
        super();
    }

    public void setSource(GeoPoint geoPoint, float radius) {
        sourcePoint = geoPoint;
        this.radius = radius;
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, false);
        Projection projection = mapView.getProjection();
        Point center = new Point();

        int radiusInPixels = (int) (projection.metersToEquatorPixels(radius));
        projection.toPixels(sourcePoint, center);

        Paint radiusPaint = new Paint();
        radiusPaint.setAntiAlias(true);
        radiusPaint.setStrokeWidth(2.0f);
        radiusPaint.setColor(0xff2F92F5);
        radiusPaint.setStyle(Paint.Style.STROKE);

        canvas.drawCircle(center.x, center.y, radiusInPixels, radiusPaint);

        radiusPaint.setColor(0x182F92F5);
        radiusPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(center.x, center.y, radiusInPixels, radiusPaint);

    }



}
