/**
 *@ PointDetailsActivity
 */
package com.allpoint.activities.tablet;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.allpoint.R;
import com.allpoint.activities.fragments.HoursFragment;
import com.allpoint.activities.fragments.ShareFragment;
import com.allpoint.activities.tablet.fragments.SettingsFragment;
import com.allpoint.model.PointRecord;
import com.allpoint.util.Constant;
import com.allpoint.util.IconManager;
import com.allpoint.util.Localization;
import com.allpoint.util.Settings;
import com.allpoint.util.Utils;
import com.allpoint.util.adapters.ServiceListAdapter;
import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * PointDetailsActivity
 * 
 * @author: Vyacheslav.Shmakin
 * @version: 08.01.14
 */
@EActivity(R.layout.point_details)
class PointDetailsActivity extends FragmentActivity implements
		GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener,
		ShareFragment.onShareChangeStateListener,
		HoursFragment.onHoursChangeStateListener,
		SettingsFragment.SettingsChangeListener {

	@SystemService
	protected LayoutInflater shareInflater;

	@SystemService
	protected LayoutInflater hoursInflater;

	@Bean
	protected IconManager iconManager;

	@ViewById(R.id.mapDetails)
	protected ViewGroup map;

	@ViewById(R.id.listViewServices)
	protected ListView lvServices;

	@ViewById(R.id.tvDetailsPointName)
	protected TextView tvPointName;

	@ViewById(R.id.tvDetailsPointAddress)
	protected TextView tvPointAddress;

	@ViewById(R.id.tvDetailsPointDistance)
	protected TextView tvPointDistance;

	@ViewById(R.id.tvDetailViewTitle)
	protected TextView detailViewTitle;

	@ViewById(R.id.tvServicesTitle)
	protected TextView detailViewServicesTitle;

	@ViewById(R.id.iViewRetailerLogo)
	protected ImageView logoView;

	@ViewById(R.id.iBtnBottomSearch)
	protected ImageButton bottomSearchButton;

	@ViewById(R.id.iTxtBottomSearch)
	TextView tvSearch;

	@ViewById(R.id.hoursButton)
	ImageButton btnGetHours;

	@ViewById(R.id.callButton)
	ImageButton btnCall;

	@ViewById(R.id.shareButton)
	ImageButton btnShare;

	@FragmentById(R.id.mapDetails)
	protected SupportMapFragment mapFragment;

	private ShareFragment shareFragment;
	private HoursFragment hoursFragment;
	private String phoneNumber;

	private boolean isContainsNumeric(String str) {
		for (int i = 0; i < 10; i++) {
			if (str.contains(String.valueOf(i))) {
				return true;
			}
		}
		return false;
	}

	@AfterViews
	void afterViews() {
		BugSenseHandler.initAndStartSession(this, Constant.BUG_SENSE_API_KEY);

		bottomSearchButton.setImageResource(R.drawable.bottom_search_press);
		tvSearch.setTextColor(getResources().getColor(R.color.textColor));
		bottomSearchButton.setEnabled(false);

		PointRecord record = Utils.getRecord();

		Integer resourceId;
		try {
			resourceId = iconManager.getIconByName(record.getLogoName());
		} catch (NullPointerException npe) {
			resourceId = null;
			npe.printStackTrace();
		}

		if (resourceId == null) {
			resourceId = R.drawable.retailer_allpoint;
		}
		logoView.setImageResource(resourceId);
		phoneNumber = record.getMobileValue();

		if (phoneNumber == null) {
			phoneNumber = "";
		} else if (!isContainsNumeric(phoneNumber)) {
			phoneNumber = "";
		}

		tvPointName.setText(record.getName());
		tvPointAddress.setText(record.getAddressLine() + " " + record.getCity()
				+ ", " + record.getState() + " " + record.getPostalCode());
		tvPointDistance.setText(Utils.getDistanceString(record.getDistance()));

		ServiceListAdapter serviceAdapter = new ServiceListAdapter(this);
		serviceAdapter.addAll(record.getServices());
		lvServices.setAdapter(serviceAdapter);

		if (record.getHours() != null && record.getHours().size() == 0) {
			btnGetHours.setEnabled(false);
			btnGetHours.setImageResource(R.drawable.details_hours_disable);
		}

		if (phoneNumber.length() == 0) {
			btnCall.setEnabled(false);
			btnCall.setImageResource(R.drawable.details_call_disable);
		}

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

		Utils.setPointPosition(record.getPosition());
		mapFragment.getMap().addMarker(record.toMarkerOptions());
		mapFragment.getMap().getUiSettings().setMyLocationButtonEnabled(false);
		mapFragment.getMap().getUiSettings().setCompassEnabled(false);
		mapFragment.getMap().getUiSettings().setAllGesturesEnabled(false);
		mapFragment.getMap().getUiSettings().setZoomControlsEnabled(false);
		mapFragment.getMap().setMyLocationEnabled(false);
		map.setEnabled(false);
		map.setActivated(false);
		map.setSelected(false);
		map.setPressed(false);
		map.setFocusable(false);
		map.setClickable(false);
		mapFragment.getMap().setOnMarkerClickListener(this);
		mapFragment.getMap().setOnMapClickListener(this);

		shareFragment = ShareFragment.getInstance();
		hoursFragment = HoursFragment.getInstance();

		if (shareFragment == null) {
			shareFragment = new ShareFragment(record);
		}

		if (hoursFragment == null) {
			hoursFragment = new HoursFragment(record);
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		Utils.startFlurry(this, Constant.POINT_DETAILS_ACTIVITY_EVENT);
		Settings.setCurrentActivity(Settings.CurrentActivity.PointDetailsActivity);
	}

	@Override
	public void onResume() {
		super.onResume();
		detailViewTitle.setText(Localization.getDetailViewLayoutTitle());
		detailViewServicesTitle.setText(Localization
				.getDetailViewServicesTitle());
	}

	@Click(R.id.directionButton)
	void onDirectionButtonClicked() {
		LatLng currentPosition = Utils.getMyLocation();

		if (currentPosition == null || Utils.getPointPosition() == null) {

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(Localization.getDialogCannotGetPosition())
					.setPositiveButton(Localization.getDialogOk(), null)
					.create().show();
			return;
		}

		String url = "http://maps.google.com/maps?saddr="
				+ String.valueOf(currentPosition.latitude) + ","
				+ String.valueOf(currentPosition.longitude) + "&daddr="
				+ String.valueOf(Utils.getPointPosition().latitude) + ","
				+ String.valueOf(Utils.getPointPosition().longitude);

		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		Utils.startFlurry(this, Constant.DIRECTIONS_ACTIVITY_EVENT);

		startActivity(intent);
	}

	@Click(R.id.shareButton)
	void onShareButtonClicked() {
		shareFragment.show(getFragmentManager(), Constant.ERROR_DIALOG_TAG);

	}

	@Click(R.id.hoursButton)
	void onHoursButtonClicked() {
		hoursFragment.show(getFragmentManager(), Constant.ERROR_DIALOG_TAG);
	}

	@Click(R.id.callButton)
	void onCallButtonClicked() {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		callIntent.setData(Uri.parse("tel:" + phoneNumber));
		startActivity(callIntent);
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		Utils.startActivity(this,
				com.allpoint.activities.PinMapActivity_.class, true, false,
				false);
		return true;
	}

	@Override
	public void onMapClick(LatLng latLng) {
		Utils.startActivity(this,
				com.allpoint.activities.PinMapActivity_.class, true, false,
				false);
	}

	@Click(R.id.iBtnDetailsBack)
	void onIbtnDetailsBackClicked() {
		onBackPressed();
	}

	@Click(R.id.tvDetailViewTitle)
	void onTvDetailViewTitleClick() {
		onBackPressed();
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	@Override
	public void onShareShow() {
		btnShare.setImageResource(R.drawable.details_share_press);
	}

	@Override
	public void onShareDismiss() {
		btnShare.setImageResource(R.drawable.details_share);
	}

	@Override
	public void onHoursShow() {
		btnGetHours.setImageResource(R.drawable.details_hours_press);
	}

	@Override
	public void onHoursDismiss() {
		btnGetHours.setImageResource(R.drawable.details_hours);
	}

	@Override
	public void onSettingsChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSettingsShowed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSettingsDismissed() {
		Settings.SaveSettings();

	}
}
