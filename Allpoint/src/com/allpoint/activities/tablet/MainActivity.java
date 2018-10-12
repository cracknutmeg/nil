/**
 *@ MainActivity
 */
package com.allpoint.activities.tablet;

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

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.allpoint.R;
import com.allpoint.activities.fragments.AlertDialogFragment;
import com.allpoint.activities.tablet.fragments.SettingsFragment;
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
import com.allpoint.util.adapters.FilterListAdapter;
import com.allpoint.util.adapters.ResultListAdapter;
import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
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
		AdapterView.OnItemClickListener, TaskManager.AsyncTaskListener,
		SettingsFragment.SettingsChangeListener {
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

	@ViewById(R.id.iBtnFilters)
	ImageButton filtersButton;

	@ViewById(R.id.iBtnShowResultList)
	ImageButton listsButton;

	@ViewById(R.id.searchView)
	SearchView searchView;

	@ViewById(R.id.iTxtBottomSearch)
	TextView tvSearch;

	@ViewById(R.id.resultListView)
	ListView resultsListView;

	@ViewById(R.id.progressBarLoading)
	ProgressBar loadingProgressBar;

	@FragmentById(R.id.mapMain)
	SupportMapFragment mapFragment;

	public static Activity instance;

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

	private ResultListAdapter resultListAdapter;

	private final Map<Long, Marker> markers = new ConcurrentHashMap<Long, Marker>();

	private AlertDialogFragment alertDialog;
	private SearchResult tempResult;
	// replace ua locaiton
	// private LocationClient locationClient;
	private ProgressDialog dialog;
	private Marker querySearchMarker;

	private boolean isQuerySearch = false;
	private boolean firstStart = false;
	private boolean isSearchMyLocation = false;

	private int positionInList = 0;

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

	private void showFiltersPopup(final List<Filter> result) {
		// Sending message to Flurry
		Utils.startFlurry(this, Constant.SHOW_FILTERS_EVENT);

		inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.filters_popup, null,
				false);
		final PopupWindow filtersWindow = new PopupWindow(layout,
				ActionBar.LayoutParams.WRAP_CONTENT,
				ActionBar.LayoutParams.WRAP_CONTENT, true);

		// Filters popup window data

		TextView filtersTitle = (TextView) layout
				.findViewById(R.id.tvSelectFilterBy);
		filtersTitle.setText(Localization.getFiltersSelectByTitle());

		filtersWindow.setOutsideTouchable(true);
		filtersWindow.setBackgroundDrawable(new BitmapDrawable(null,
				(Bitmap) null));

		filtersWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);

		FilterListAdapter filterListAdapter = new FilterListAdapter(
				layout.getContext());

		ListView filtersListView = (ListView) layout
				.findViewById(R.id.filtersListView);
		final List<Filter> temp = filterManager.getUpdatedFilterList(result);
		for (Filter filter : temp) {
			filterListAdapter.add(filter);
		}
		filtersListView.setAdapter(filterListAdapter);

		filtersListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						CheckBox checkBox = (CheckBox) view
								.findViewById(R.id.chBoxFilterName);
						if (checkBox != null) {
							checkBox.setChecked(!checkBox.isChecked());
							filterManager.setActivated(position,
									!temp.get(position).isActivated());
						}
					}
				});

		filtersWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				filtersButton.setImageResource(R.drawable.main_filter);
			}
		});
	}

	@SuppressLint("NewApi")
	@AfterViews
	public void AfterViews() {
		BugSenseHandler.initAndStartSession(this, Constant.BUG_SENSE_API_KEY);

		bottomSearchButton.setImageResource(R.drawable.bottom_search_press);
		tvSearch.setTextColor(getResources().getColor(R.color.textColor));
		bottomSearchButton.setEnabled(false);

		int isGooglePlayServiceAvailable = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getApplicationContext());

		// replace ua locaiton
		/*
		 * if (isGooglePlayServiceAvailable == ConnectionResult.SUCCESS) {
		 * locationClient = new LocationClient(this, this, this);
		 * locationClient.connect(); } else {
		 * GooglePlayServicesUtil.getErrorDialog(isGooglePlayServiceAvailable,
		 * this, Constant.GOOGLE_PLAY_REQUEST_CODE).show(); }
		 */

		mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.mapMain);

		mapFragment.getMap().getUiSettings().setMyLocationButtonEnabled(false);
		mapFragment.getMap().getUiSettings().setCompassEnabled(false);
		mapFragment.getMap().getUiSettings().setRotateGesturesEnabled(false);
		mapFragment.getMap().getUiSettings().setZoomControlsEnabled(false);
		mapFragment.getMap().getUiSettings().setTiltGesturesEnabled(false);
		mapFragment.getMap().setMyLocationEnabled(true);
		mapFragment.getMap().setOnInfoWindowClickListener(this);
		mapFragment.getMap().setOnMarkerClickListener(this);
		mapFragment.getMap().setOnCameraChangeListener(this);

		filtersButton.setEnabled(true);
		listsButton.setEnabled(true);
		firstStart = true;
		instance = this;

		resultListAdapter = new ResultListAdapter(this);
		resultsListView.setAdapter(resultListAdapter);
		searchView.setOnQueryTextListener(this);
		searchView.setBackground(getResources().getDrawable(R.color.apGreen));
		resultsListView.setOnItemClickListener(this);
		taskManager.addTaskListener(this);
	}

	@Click(R.id.iBtnFilters)
	void onIbtnFiltersClick() {
		filtersButton.setImageResource(R.drawable.main_filter_press);
		taskManager.searchFilters(Constant.FILTERS_TASK_ID);
	}

	@Click(R.id.iBtnShowResultList)
	void onIbtnShowResultListClick() {
		if (resultsListView.getVisibility() == View.VISIBLE) {
			resultsListView.setVisibility(View.GONE);
			listsButton.setImageResource(R.drawable.main_list);
		} else {
			resultsListView.setVisibility(View.VISIBLE);
			listsButton.setImageResource(R.drawable.main_list_press);
		}
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
		/*
		 * if (locationClient == null) { return; }
		 * 
		 * if (locationClient.isConnected()) { if
		 * (locationClient.getLastLocation() != null) { if
		 * (Utils.getMyLocation() != null) { if
		 * (!Compute.isEqualsLatLng(mapFragment
		 * .getMap().getCameraPosition().target, Utils.getMyLocation(),
		 * Constant.ROUND_VALUE)) {
		 * 
		 * taskManager.stopTask(Constant.MAIN_ACTIVITY_POSITION_SEARCH);
		 * taskManager.searchLocations(Constant.MAIN_ACTIVITY_QUERY_SEARCH,
		 * Utils.getMyLocation(), filterManager.getFiltersString(null)); }
		 * isSearchMyLocation = true; } } else { // If it is impossible to
		 * receive own position, then shown alert message alertDialog = new
		 * AlertDialogFragment(Localization.getDialogCannotGetPosition(),
		 * Localization.getDialogOk()); alertDialog.show(getFragmentManager(),
		 * Constant.ERROR_DIALOG_TAG); } }
		 */
	}

	@Override
	public void onStart() {
		super.onStart();
		Utils.startFlurry(this, Constant.MAP_ACTIVITY_EVENT);
		Settings.setCurrentActivity(Settings.CurrentActivity.MainActivity);

		// replace ua locaiton
		// Connect the client.
		// locationClient.connect();

		Utils.setOnMainActivity(true);

		if (Utils.getInfoWindowMarker() != null) {
			onMarkerClick(Utils.getInfoWindowMarker());
		}
	}

	@Override
	public void onTaskStarted(final String taskId,
			final TaskManager.QueryId queryId) {
		if (taskId.equalsIgnoreCase(Constant.MAIN_ACTIVITY_QUERY_SEARCH)
				|| taskId.equalsIgnoreCase(Constant.FILTERS_TASK_ID)) {
			dialog = ProgressDialog.show(this,
					Localization.getDialogLoadingTitle(),
					Localization.getDialogPleaseWait(), true, false);
		}
	}

	@Override
	public void onSearchFinished(final String taskId, final ErrorType error,
			final SearchResult result) {
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

				resultListAdapter.clear();
				resultListAdapter.addAll(Utils.getLocationList());
			}
			tempResult = new SearchResult(result);
			break;
		case CONNECTION_ERROR:
			dialog.dismiss();
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
		}

		if (AlertDialogFragment.getInstance() != null) {
			alertDialog.show(getFragmentManager(), Constant.ERROR_DIALOG_TAG);
		}
	}

	@Override
	public void onReceiveFilters(final String taskId, final ErrorType error,
			final List<Filter> result) {
		switch (error) {
		case NO_FILTERS_FOUND:
			break;
		case TASK_FINISHED:
			showFiltersPopup(result);
			break;
		case CONNECTION_ERROR:
			alertDialog = new AlertDialogFragment(
					Localization.getDialogCannotConnectTitle(),
					Localization.getDialogCannotConnectText(),
					Localization.getDialogOk());
			break;
		case SERVICE_UNAVAILABLE:
			alertDialog = new AlertDialogFragment(
					Localization.getDialogServiceUnavailable(),
					Localization.getDialogOk());
			break;
		case XML_PARSER_ERROR:
			alertDialog = new AlertDialogFragment(
					Localization.getDialogParsingError(),
					Localization.getDialogOk());
			break;
		}
		if (AlertDialogFragment.getInstance() != null) {
			alertDialog.show(getFragmentManager(), Constant.ERROR_DIALOG_TAG);
		}
		dialog.dismiss();
	}

	@Override
	public void onReceiveVersionInfo(final String taskId,
			final ErrorType error, final VersionInfo result) {
	}

	@Override
	public void onConnected(final Bundle bundle) {

		// replace ua locaiton
		/*
		 * if (locationClient == null) { return; }
		 * 
		 * if (locationClient.getLastLocation() != null) {
		 * Utils.setMyLocation(new
		 * LatLng(locationClient.getLastLocation().getLatitude(),
		 * locationClient.getLastLocation().getLongitude()));
		 * 
		 * if (firstStart) { myPositionButton.performClick(); firstStart =
		 * false; } locationClient.requestLocationUpdates(new
		 * LocationRequest().setInterval(Constant.LOCATION_REQUEST_INTERVAL),
		 * this); } else if (firstStart) { alertDialog = new
		 * AlertDialogFragment(Localization.getDialogCannotGetPosition(),
		 * Localization.getDialogOk()); alertDialog.show(getFragmentManager(),
		 * Constant.ERROR_DIALOG_TAG); firstStart = false; }
		 */
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

						// Memorize position where we have started SEARCHING
						Utils.setCurrentPosition(cameraPosition.target);
					}
				}
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
		Utils.startActivity(MainActivity.this,
				com.allpoint.activities.tablet.MainMenuActivity_.class, false,
				false, !Utils.isOnMainActivity());
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
		// locationClient.disconnect();
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
				positionInList = getMarkerPositionFromList(
						Utils.getLocationList(), marker);
				Utils.setInfoWindowMarker(marker);
				Utils.setLocationList(setSelectedItem(Utils.getLocationList(),
						positionInList));
			}
		} else {
			positionInList = getMarkerPositionFromList(Utils.getLocationList(),
					marker);
			Utils.setInfoWindowMarker(marker);
			Utils.setLocationList(setSelectedItem(Utils.getLocationList(),
					positionInList));
		}
		resultListAdapter.clear();
		resultListAdapter.addAll(Utils.getLocationList());
		resultsListView.setSelectionFromTop(positionInList, 0);

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

	@Override
	public void onSettingsChanged() {

	}

	@Override
	public void onSettingsShowed() {

	}

	@Override
	public void onSettingsDismissed() {
		Settings.SaveSettings();
	}
}
