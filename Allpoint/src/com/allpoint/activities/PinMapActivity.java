/**
 *@ PointDetailsActivity
 */
package com.allpoint.activities;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.ViewById;

import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.allpoint.AtmFinderApplication;
import com.allpoint.R;
import com.allpoint.model.PointRecord;
import com.allpoint.util.Constant;
import com.allpoint.util.Localization;
import com.allpoint.util.Utils;
import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

/**
 * PinMapActivity
 * 
 * @author: Vyacheslav.Shmakin
 * @version: 08.01.14
 */
@EActivity(R.layout.lonely_pin_map)
public class PinMapActivity extends FragmentActivity implements
		GoogleMap.OnMarkerClickListener {

	@FragmentById(R.id.mapPin)
	SupportMapFragment mapFragment;

	@ViewById(R.id.tvPinViewTitle)
	TextView pinViewTitle;

	@AfterViews
	void afterViews() {
		BugSenseHandler.initAndStartSession(AtmFinderApplication.getContext(),
				Constant.BUG_SENSE_API_KEY);

		PointRecord record = Utils.getRecord();

		if (mapFragment.getMap() == null) {
			return;
		}
		mapFragment
				.getMap()
				.moveCamera(
						CameraUpdateFactory
								.newCameraPosition(new CameraPosition(record
										.getPosition(),
										Constant.DEFAULT_MAP_ZOOM_VALUE, 0, 0)));

		mapFragment
				.getMap()
				.addMarker(
						record.toMarkerOptions()
								.snippet(null)
								.title(record.getAddressLine() + " "
										+ record.getCity() + ", "
										+ record.getState() + " "
										+ record.getPostalCode()))
				.showInfoWindow();
		mapFragment.getMap().getUiSettings().setMyLocationButtonEnabled(false);
		mapFragment.getMap().getUiSettings().setCompassEnabled(false);
		mapFragment.getMap().getUiSettings().setRotateGesturesEnabled(false);
		mapFragment.getMap().getUiSettings().setZoomControlsEnabled(false);
		mapFragment.getMap().getUiSettings().setTiltGesturesEnabled(false);
		mapFragment.getMap().setMyLocationEnabled(true);
		mapFragment.getMap().setOnMarkerClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		Utils.startFlurry(this, Constant.PIN_VIEW_ACTIVITY_EVENT);
	}

	@Override
	public void onResume() {
		super.onResume();
		pinViewTitle.setText(Localization.getPinViewLayoutTitle());
	}

	@Click(R.id.iBtnPinBack)
	void onIbtnPinBackClicked() {
		onBackPressed();
	}

	@Click(R.id.tvPinViewTitle)
	void onTvPinViewTitleClick() {
		onBackPressed();
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		return false;
	}

	@Override
	public void onBackPressed() {
		finish();
	}
}
