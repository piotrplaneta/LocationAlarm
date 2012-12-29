package edu.uj.pmd.locationalarm.overlays;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

@SuppressWarnings("rawtypes")
public class LocationItemizedOverlay extends ItemizedOverlay {
	private ArrayList<OverlayItem> overlayItems = new ArrayList<OverlayItem>(2);
	Context context;
	
	public LocationItemizedOverlay(Drawable arg, Context context) {
		super(boundCenterBottom(arg));
		this.context = context;
	}

	@Override
	protected OverlayItem createItem(int i) {
		return overlayItems.get(i);
	}
	
	@Override
	protected boolean onTap(int i) {
		OverlayItem item = overlayItems.get(i);
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();
		return true;
	}

	@Override
	public int size() {
		return overlayItems.size();
	}

	public void add(OverlayItem overlay) {
		overlayItems.add(overlay);
		populate();
	}

	public void remove(OverlayItem overlay) {
        if(overlay != null && overlayItems.contains(overlay)) {
            overlayItems.remove(overlay);
        }
        populate();
	}

    public void clear() {
        overlayItems.clear();
        populate();
    }

}
