/**
 *@ MainActivity
 */
package com.allpoint.activities.phone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.allpoint.R;
import com.allpoint.activities.fragments.AlertDialogFragment;
import com.allpoint.model.Filter;
import com.allpoint.model.PointRecord;
import com.allpoint.model.SearchResult;
import com.allpoint.model.VersionInfo;
import com.allpoint.services.FilterManager;
import com.allpoint.services.xmlParser.ErrorType;
import com.allpoint.services.xmlParser.TaskManager;
import com.allpoint.util.Compute;
import com.allpoint.util.Constant;
import com.allpoint.util.Localization;
import com.allpoint.util.Settings;
import com.allpoint.util.Utils;
import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

/**
 * MainActivity
 * 
 * @author: Vyacheslav.Shmakin
 * @version: 08.01.14
 */
@EActivity(R.layout.main)
public class MainActivity extends FragmentActivity implements
		SearchView.OnQueryTextListener, GoogleMap.OnInfoWindowClickListener,
		GooglePlayServicesClient.ConnectionCallbacks, LocationListener,
		GooglePlayServicesClient.OnConnectionFailedListener,
		GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraChangeListener,
		AdapterView.OnItemClickListener, TaskManager.AsyncTaskListener {
	@Bean
	FilterManager filterManager;
	@Bean
	TaskManager taskManager;

	@SystemService
	LayoutInflater inflater;

	@ViewById(R.id.iBtnShowMyPosition)
	ImageButton myPositionButton;

	@ViewById(R.id.iBtnBottomSearch)
	ImageButton bottomSearchButton;

	@ViewById(R.id.iTxtBottomSearch)
	TextView tvSearch;

	@ViewById(R.id.iBtnShowResultList)
	ImageButton listsButton;

	@ViewById(R.id.searchView)
	SearchView searchView;

	@ViewById(R.id.progressBarLoading)
	ProgressBar loadingProgressBar;

	@FragmentById(R.id.mapMain)
	MapFragment mapFragment;

	public static Activity instance;
	private static String queryText;
	private static LatLng currentPosition;

	public static String getQueryText() {
		return queryText;
	}

	public static void setQueryText(final String queryText) {
		MainActivity.queryText = queryText;
	}

	public static LatLng getCurrentPosition() {
		return currentPosition;
	}

	public static void setCurrentPosition(final LatLng location) {
		MainActivity.currentPosition = location;
	}

	private List<PointRecord> setSelectedItem(final List<PointRecord> records,
			final int position) {
		for (int i = 0; i < records.size(); i++) {
			if (i == position) {
				records.get(i).setSelected(true);
			} else {
				records.get(i).setSelected(false);
			}
		}
		return records;
	}

	private List<PointRecord> clearSelection(final List<PointRecord> records) {
		for (PointRecord record : records) {
			record.setSelected(false);
		}
		return records;
	}

	private final Map<Long, Marker> markers = new ConcurrentHashMap<Long, Marker>();

	private AlertDialogFragment alertDialog;
	private SearchResult tempResult;

	// replace ua locaiton
	private LocationClient locationClient;

	private ProgressDialog dialog;
	private Marker querySearchMarker;

	private boolean isQuerySearch = false;
	private boolean firstStart = false;
	private boolean isSearchMyLocation = false;

	/**
	 * Extra to launch a Message ID.
	 */
	public static final String EXTRA_MESSAGE_ID = "com.urbanairship.richpush.sample1.EXTRA_MESSAGE_ID";

	/**
	 * Extra to select what item the fragment. Either {@link #HOME_ITEM} or
	 * {@link #INBOX_ITEM}.
	 */
	public static final String EXTRA_NAVIGATE_ITEM = "com.urbanairship.richpush.sample1.EXTRA_NAVIGATE_ITEM";

	/**
	 * Home fragment position.
	 */
	public static final int HOME_ITEM = 0;

	/**
	 * Inbox fragment position.
	 */
	public static final int INBOX_ITEM = 1;

	private final GoogleMap.CancelableCallback cancelableCallback = new GoogleMap.CancelableCallback() {
		@Override
		public void onFinish() {
			if (dialog != null) {
				dialog.dismiss();
			}
		}

		@Override
		public void onCancel() {
		}
	};

	private void openDetailsActivity(final List<PointRecord> records,
			final Marker marker) {
		for (PointRecord record : records) {
			if (Compute.isEqualsLatLng(record.getPosition(),
					marker.getPosition(), Constant.ROUND_VALUE)) {
				Utils.setRecord(record);
				Utils.startActivity(this, PointDetailsActivity_.class, true,
						false, false);
				break;
			}
		}
	}

	private int getMarkerPositionFromList(final List<PointRecord> records,
			final Marker marker) {
		for (int i = 0; i < records.size(); i++) {
			if (Compute.isEqualsLatLng(records.get(i).getPosition(),
					marker.getPosition(), Constant.ROUND_VALUE)) {
				if (records.get(i).getName().equals(marker.getTitle())) {
					marker.setSnippet(Utils.getDistanceString(records.get(i)
							.getDistance()));
					Utils.setInfoWindowRecord(records.get(i));
					return i;
				}
			}
		}
		return -1;
	}

	@AfterViews
	public void AfterViews() {
		BugSenseHandler.initAndStartSession(this, Constant.BUG_SENSE_API_KEY);

		bottomSearchButton.setImageResource(R.drawable.bottom_search_press);
		tvSearch.setTextColor(getResources().getColor(R.color.textColor));
		// bottomSearchButton.setBackgroundResource(R.drawable.bottom_home_press);
		bottomSearchButton.setEnabled(false);

		int isGooglePlayServiceAvailable = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getApplicationContext());

		// replace ua locaiton
		if (isGooglePlayServiceAvailable == ConnectionResult.SUCCESS) {
			locationClient = new LocationClient(this, this, this);
			locationClient.connect();
		} else {
			GooglePlayServicesUtil.getErrorDialog(isGooglePlayServiceAvailable,
					this, Constant.GOOGLE_PLAY_REQUEST_CODE).show();
		}

		mapFragment = (MapFragment) getFragmentManager().findFragmentById(
				R.id.mapMain);

		mapFragment.getMap().getUiSettings().setMyLocationButtonEnabled(false);
		mapFragment.getMap().getUiSettings().setCompassEnabled(false);
		mapFragment.getMap().getUiSettings().setRotateGesturesEnabled(false);
		mapFragment.getMap().getUiSettings().setZoomControlsEnabled(false);
		mapFragment.getMap().getUiSettings().setTiltGesturesEnabled(false);
		mapFragment.getMap().setMyLocationEnabled(true);
		mapFragment.getMap().setOnInfoWindowClickListener(this);
		mapFragment.getMap().setOnMarkerClickListener(this);
		mapFragment.getMap().setOnCameraChangeListener(this);

		listsButton.setEnabled(true);
		firstStart = true;
		instance = this;

		searchView.setOnQueryTextListener(this);
		// searchView.setBackground(getResources().getDrawable(R.color.apGreen));
		taskManager.addTaskListener(this);
	}

	@Click(R.id.iBtnFilters)
	void onIbtnFiltersClick() {
		Utils.startActivity(this, FiltersActivity_.class, false, false,
				!Utils.isOnMainActivity());
	}

	@Click(R.id.iBtnShowResultList)
	void onIbtnShowResultListClick() {
		setCurrentPosition(mapFragment.getMap().getCameraPosition().target);
		Utils.startActivity(this, ResultListActivity_.class, false, false,
				!Utils.isOnMainActivity());
	}

	@Click(R.id.zoomInButton)
	void onZoomInButtonClick() {
		mapFragment.getMap().animateCamera(CameraUpdateFactory.zoomIn());
	}

	@Click(R.id.zoomOutButton)
	void onZoomOutButtonClick() {
		mapFragment.getMap().animateCamera(CameraUpdateFactory.zoomOut());
	}

	@Click(R.id.iBtnShowMyPosition)
	void onIbtnShowMyPositionClicked() {
		// replace ua locaiton
		if (locationClient == null) {
			return;
		}

		if (locationClient.isConnected()) {
			if (locationClient.getLastLocation() != null) {
				if (Utils.getMyLocation() != null) {
					if (!Compute.isEqualsLatLng(mapFragment.getMap()
							.getCameraPosition().target, Utils.getMyLocation(),
							Constant.ROUND_VALUE)) {

						taskManager
								.stopTask(Constant.MAIN_ACTIVITY_POSITION_SEARCH);
						taskManager.searchLocations(
								Constant.MAIN_ACTIVITY_QUERY_SEARCH,
								Utils.getMyLocation(),
								filterManager.getFiltersString(null));
					}
					isSearchMyLocation = true;
				}
			} else {
				// If it is impossible to receive own position, then shown alert
				// message
				alertDialog = new AlertDialogFragment(
						Localization.getDialogCannotGetPosition(),
						Localization.getDialogOk());
				alertDialog.show(getFragmentManager(),
						Constant.ERROR_DIALOG_TAG);
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		Utils.startFlurry(this, Constant.MAP_ACTIVITY_EVENT);
		Settings.setCurrentActivity(Settings.CurrentActivity.MainActivity);

		// replace ua locaiton
		// Connect the client.
		locationClient.connect();

		Utils.setOnMainActivity(true);

		if (Utils.isActionEnabled()) {
			if (Utils.getActionCode() == Constant.OPEN_MAP_VALUE) {
				double latitudeCameraPosition = Compute.round(mapFragment
						.getMap().getCameraPosition().target.latitude, 5);
				double longitudeCameraPosition = Compute.round(mapFragment
						.getMap().getCameraPosition().target.longitude, 5);

				double latitudeCurrentPosition = Compute.round(
						getCurrentPosition().latitude, 5);
				double longitudeCurrentPosition = Compute.round(
						getCurrentPosition().longitude, 5);

				LatLng cameraPosition = new LatLng(latitudeCameraPosition,
						longitudeCameraPosition);

				LatLng shortedCurrentPosition = new LatLng(
						latitudeCurrentPosition, longitudeCurrentPosition);

				if (!cameraPosition.equals(shortedCurrentPosition)) {
					taskManager.searchLocations(
							Constant.MAIN_ACTIVITY_QUERY_SEARCH,
							getCurrentPosition(),
							filterManager.getFiltersString(null));
				}
			}

			Utils.setActionEnabled(false);
		}

		if (Utils.getInfoWindowMarker() != null) {
			onMarkerClick(Utils.getInfoWindowMarker());
		}
	}

	@Override
	public void onTaskStarted(final String taskId,
			final TaskManager.QueryId queryId) {
		if (taskId.equalsIgnoreCase(Constant.MAIN_ACTIVITY_QUERY_SEARCH)) {
			
			
				dialog = ProgressDialog.show(this,
					Localization.getDialogLoadingTitle(),
					Localization.getDialogPleaseWait(), true, false);
			
		}
	}

	@Override
	public void onSearchFinished(final String taskId, final ErrorType error,
			final SearchResult result) {
		if (taskId.equals(Constant.MAIN_ACTIVITY_POSITION_SEARCH)
				|| taskId.equals(Constant.MAIN_ACTIVITY_QUERY_SEARCH)) {
			loadingProgressBar.setVisibility(View.GONE);
			switch (error) {
			case NO_RESULTS_FOUND:
				if (taskId.equals(Constant.MAIN_ACTIVITY_QUERY_SEARCH)) {

					// Simply moving to selected location
					if (isSearchMyLocation) {
						isQuerySearch = true;

						if (mapFragment.getMap() != null) {
							mapFragment
									.getMap()
									.animateCamera(
											CameraUpdateFactory
													.newCameraPosition(new CameraPosition(
															Utils.getMyLocation(),
															Constant.DEFAULT_MAP_ZOOM_VALUE,
															0, 0)),
											cancelableCallback);
						}
					} else {
						dialog.dismiss();
						alertDialog = new AlertDialogFragment(
								Localization.getDialogNoResults(),
								Localization.getDialogOk());
					}
				}

				break;
			case TASK_FINISHED:

				if (this.mapFragment.getMap() != null) {
					this.mapFragment.getMap().clear();

					List<PointRecord> locationList = result.getPoints();
					markers.clear();

					int locationsCount = locationList.size();

					for (int i = 0; i < locationsCount; i++) {
						Marker marker = this.mapFragment.getMap().addMarker(
								locationList.get(i).toMarkerOptions());
						markers.put(locationList.get(i).getLocationId(), marker);

						if (i == 0) {
							Utils.setInfoWindowMarker(marker);
							Utils.setInfoWindowRecord(locationList.get(i));
							marker.showInfoWindow();
						}
					}

					loadingProgressBar.setVisibility(View.GONE);
					if (taskId.equals(Constant.MAIN_ACTIVITY_QUERY_SEARCH)) {
						isQuerySearch = true;

						if (mapFragment.getMap() != null) {
							mapFragment
									.getMap()
									.animateCamera(
											CameraUpdateFactory
													.newCameraPosition(new CameraPosition(
															result.getStartPosition(),
															Compute.zoomLevel(
																	locationList
																			.get(locationList
																					.size() - 1)
																			.getDistance(),
																	true), 0, 0)),
											cancelableCallback);
						}
					}
					Utils.setLocationList(setSelectedItem(result.getPoints(), 0));
				}
				tempResult = new SearchResult(result);
				break;
			case CONNECTION_ERROR:
				try {
					dialog.dismiss();	
				} catch (Exception e) {
					// TODO: handle exception
				}
				
				
				alertDialog = new AlertDialogFragment(
						Localization.getDialogCannotConnectTitle(),
						Localization.getDialogCannotConnectText(),
						Localization.getDialogOk());
				break;
			case SERVICE_UNAVAILABLE:
				dialog.dismiss();
				alertDialog = new AlertDialogFragment(
						Localization.getDialogServiceUnavailable(),
						Localization.getDialogOk());
				break;
			case XML_PARSER_ERROR:
				dialog.dismiss();
				alertDialog = new AlertDialogFragment(
						Localization.getDialogParsingError(),
						Localization.getDialogOk());
				break;
			default:
				break;
			}

			// if (AlertDialogFragment.getInstance() != null) {
			// alertDialog.show(getFragmentManager(),
			// Constant.ERROR_DIALOG_TAG);
			// }

			if (alertDialog != null) {
				alertDialog.show(getFragmentManager(),
						Constant.ERROR_DIALOG_TAG);
			}

		}
	}

	@Override
	public void onReceiveFilters(final String taskId, final ErrorType error,
			final List<Filter> result) {
	}

	@Override
	public void onReceiveVersionInfo(final String taskId,
			final ErrorType error, final VersionInfo result) {
	}

	@Override
	public void onConnected(final Bundle bundle) {

		// replace ua locaiton
		if (locationClient == null) {
			return;
		}
		if (locationClient.getLastLocation() != null) {
			Utils.setMyLocation(new LatLng(locationClient.getLastLocation()
					.getLatitude(), locationClient.getLastLocation()
					.getLongitude()));

			if (firstStart) {
				myPositionButton.performClick();
				firstStart = false;
			}
			locationClient.requestLocationUpdates(new LocationRequest()
					.setInterval(Constant.LOCATION_REQUEST_INTERVAL), this);
		} else if (firstStart) {
			alertDialog = new AlertDialogFragment(
					Localization.getDialogCannotGetPosition(),
					Localization.getDialogOk());
			alertDialog.show(getFragmentManager(), Constant.ERROR_DIALOG_TAG);
			firstStart = false;
		}
	}

	@Override
	public void onDisconnected() {
	}

	@Override
	public void onLocationChanged(final Location location) {
		Utils.setMyLocation(new LatLng(location.getLatitude(), location
				.getLongitude()));
	}

	@Override
	public void onCameraChange(final CameraPosition cameraPosition) {
		// If we have no Current Position value and it is not Query Search
		if (Utils.getCurrentPosition() != null && !isQuerySearch) {

			// If now is running QUERY SEARCHING
			if (!taskManager.getRunningTaskList().contains(
					Constant.MAIN_ACTIVITY_QUERY_SEARCH)) {
				// If previous position (CURRENT POSITION value) does not equals
				// current position (from camera)
				if (!Utils.getCurrentPosition().equals(cameraPosition.target)) {

					// Counting delta - distance between current position and
					// previous position
					float delta = Compute.distanceBetweenPoints(
							Utils.getCurrentPosition(), cameraPosition.target);

					// This is protection from accidental movings over device
					// display
					VisibleRegion vr = mapFragment.getMap().getProjection()
							.getVisibleRegion();
					float deltaByDisplay = Compute.distanceBetweenPoints(
							vr.latLngBounds.northeast,
							vr.latLngBounds.southwest)
							/ Constant.STEP_CONSTANT;

					// If distance more than minimum value or value from
					// accidental movings,
					// then starts POSITION SEARCH
					if (delta > Constant.MINIMUM_DISTANCE
							|| delta > deltaByDisplay) {

						// If map has Query search marker, then marker removing
						// from map
						if (querySearchMarker != null) {
							querySearchMarker.remove();
						}

						taskManager.searchLocations(
								Constant.MAIN_ACTIVITY_POSITION_SEARCH,
								cameraPosition.target,
								filterManager.getFiltersString(null));
						loadingProgressBar.setVisibility(View.VISIBLE);
					}
				}
				// Memorize position where we have started SEARCHING
				Utils.setCurrentPosition(cameraPosition.target);
			}
		} else {
			// It is first launch or QUERY SEARCH
			// Memorize position where we have started SEARCHING
			Utils.setCurrentPosition(cameraPosition.target);

			// If is QUERY SEARCH
			if (isQuerySearch) {
				// If map has Query search marker, then marker removing from map
				if (querySearchMarker != null) {
					querySearchMarker.remove();
				}

				// If it is not searching my location
				if (!isSearchMyLocation) {
					String searchPointTitle = tempResult.getStartAddress();

					if (searchPointTitle != null) {
						if (searchPointTitle.length() == 0) {
							searchPointTitle = Utils.getQueryText();
						}
					}
					// Creating Query search marker on searching location
					querySearchMarker = this.mapFragment
							.getMap()
							.addMarker(
									new MarkerOptions()
											.icon(BitmapDescriptorFactory
													.fromResource(R.drawable.location_marker))
											.position(
													tempResult
															.getStartPosition())
											.title(searchPointTitle));
				} else {
					// If it is searching my location then flag setting off
					isSearchMyLocation = false;
				}
				// QUERY SEARCH is ended
				isQuerySearch = false;
			}
		}
	}

	@Override
	public void onConnectionFailed(final ConnectionResult connectionResult) {
	}

	@Override
	public void onBackPressed() {
		Utils.setCurrentPosition(null);
		finish();
	}

	@Override
	protected void onPause() {
		if (Utils.getInfoWindowMarker() != null) {
			if (!Utils.getInfoWindowMarker().isInfoWindowShown()) {
				Utils.setInfoWindowMarker(null);
			}
		}
		super.onPause();
	}

	@Override
	public void onStop() {
		// replace ua locaiton
		// Disconnecting the client invalidates it.
		locationClient.disconnect();
		Utils.setOnMainActivity(false);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		taskManager.removeTaskListener(this);
	}

	@Override
	public void onInfoWindowClick(final Marker marker) {
		if (querySearchMarker != null) {
			if (!marker.equals(querySearchMarker)) {
				openDetailsActivity(Utils.getLocationList(), marker);
			}
		} else {
			openDetailsActivity(Utils.getLocationList(), marker);
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view,
			final int position, final long id) {
		List<Marker> markerList = new ArrayList<Marker>();
		markerList.addAll(markers.values());

		PointRecord record = Utils.getLocationList().get(position);
		Utils.setRecord(record);

		for (Marker marker : markerList) {
			if (Compute.isEqualsLatLng(record.getPosition(),
					marker.getPosition(), Constant.ROUND_VALUE)) {
				if (record.getName().equals(marker.getTitle())) {
					marker.setSnippet(Utils.getDistanceString(record
							.getDistance()));
					Utils.setInfoWindowRecord(record);

					onMarkerClick(marker);
					break;
				}
			}
		}
		Utils.startActivity(this, PointDetailsActivity_.class, true, false,
				false);
	}

	@Override
	public boolean onMarkerClick(final Marker marker) {
		if (querySearchMarker != null) {
			if (marker.equals(querySearchMarker)) {
				Utils.setInfoWindowMarker(null);
				Utils.setLocationList(clearSelection(Utils.getLocationList()));
			} else {
				int positionInList = getMarkerPositionFromList(
						Utils.getLocationList(), marker);
				Utils.setInfoWindowMarker(marker);
				Utils.setLocationList(setSelectedItem(Utils.getLocationList(),
						positionInList));
			}
		} else {
			int positionInList = getMarkerPositionFromList(
					Utils.getLocationList(), marker);
			Utils.setInfoWindowMarker(marker);
			Utils.setLocationList(setSelectedItem(Utils.getLocationList(),
					positionInList));
		}

		marker.showInfoWindow();
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(final String query) {
		taskManager.searchLocations(Constant.MAIN_ACTIVITY_QUERY_SEARCH, query,
				filterManager.getFiltersString(null));

		searchView.onActionViewCollapsed();

		Utils.setQueryText(query);
		searchView.setIconified(false);
		searchView.clearFocus();
		return false;
	}

	@Override
	public boolean onQueryTextChange(final String newText) {
		return false;
	}
}
