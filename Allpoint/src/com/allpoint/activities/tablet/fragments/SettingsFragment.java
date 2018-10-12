package com.allpoint.activities.tablet.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.allpoint.AtmFinderApplication;
import com.allpoint.R;
import com.allpoint.activities.ChangePasswordActivity_;
import com.allpoint.activities.RegistrationActivity_;
import com.allpoint.services.InternetConnectionManager;
import com.allpoint.services.InternetConnectionManager_;
import com.allpoint.util.Constant;
import com.allpoint.util.Localization;
import com.allpoint.util.Settings;
import com.allpoint.util.ShareApp;
import com.allpoint.util.Utils;

/**
 * SettingsFragment
 * 
 * @author: Vyacheslav.Shmakin
 * @version: 08.01.14
 */
public class SettingsFragment extends DialogFragment implements
		View.OnClickListener, CompoundButton.OnCheckedChangeListener {

	private TextView settingsChangeButton, termsAndConditionButton;
	private TextView editProfileButton;

	RelativeLayout layChangePass,layGeoFence;

	AtmFinderApplication atmfinderappcontext;

	public interface SettingsChangeListener {
		void onSettingsChanged();

		void onSettingsShowed();

		void onSettingsDismissed();
	}

	private static final List<SettingsChangeListener> settingsListeners = new ArrayList<SettingsChangeListener>();

	public void addSettingsListener(final SettingsChangeListener listener) {
		settingsListeners.add(listener);
	}

	public void removeSettingsListener(final SettingsChangeListener listener) {
		settingsListeners.remove(listener);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		final View dialogLayout = inflater.inflate(R.layout.settings_fragment,
				container, false);

		getDialog().getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		atmfinderappcontext = (AtmFinderApplication) getActivity()
				.getApplicationContext();

		//Hide old Password Field on Change Password
		atmfinderappcontext.setChangePassFromSettings = true;
		
		// Text fiends on Settings popup
		final TextView searchPreferencesText = (TextView) dialogLayout
				.findViewById(R.id.tvSearchPreference);
		searchPreferencesText.setText(Localization
				.getSettingsSearchPreferenceTitle());

		final TextView launchNearMeText = (TextView) dialogLayout
				.findViewById(R.id.tvLaunchNear);
		launchNearMeText.setText(Localization
				.getSettingsLaunchWithNearMeTitle());

		final TextView distanceUnitText = (TextView) dialogLayout
				.findViewById(R.id.tvDistanceUnits);
		distanceUnitText.setText(Localization.getSettingsDistanceUnitsTitle());

		final TextView languageText = (TextView) dialogLayout
				.findViewById(R.id.tvLanguage);
		languageText.setText(Localization.getSettingsLanguageTitle());

		final TextView versionText = (TextView) dialogLayout
				.findViewById(R.id.tvAppVersion);
		versionText.setText(Localization.getSettingsVersion() + " "
				+ Constant.TABLET_APP_VERSION);

		final TextView visitPlayButton = (TextView) dialogLayout
				.findViewById(R.id.btnVisitPlay);
		visitPlayButton.setOnClickListener(this);
		visitPlayButton.setText(Localization.getSettingsVisitGooglePlay());

		final TextView sendFeedbackButton = (TextView) dialogLayout
				.findViewById(R.id.btnFeedback);
		sendFeedbackButton.setOnClickListener(this);
		sendFeedbackButton.setText(Localization.getSettingsFeedback());

		editProfileButton = (TextView) dialogLayout
				.findViewById(R.id.btnEditProfile);
		editProfileButton.setOnClickListener(this);
		editProfileButton.setText(Localization.getSettingsEditProfile());

		settingsChangeButton = (TextView) dialogLayout
				.findViewById(R.id.btnChangePassword);
		settingsChangeButton.setOnClickListener(this);

		layChangePass = (RelativeLayout) dialogLayout
				.findViewById(R.id.layChangePass);

		termsAndConditionButton = (TextView) dialogLayout
				.findViewById(R.id.btnTermsAndCond);
		termsAndConditionButton.setOnClickListener(this);

		// Distance Units Spinner data
		String[] units = new String[] {
				Localization.getSettingsDistanceUnitsMiles(),
				Localization.getSettingsDistanceUnitsKm() };
		ArrayAdapter<String> unitsAdapter = new ArrayAdapter<String>(
				dialogLayout.getContext(), R.layout.spinner_item, units);

		final Spinner unitsSpinner = (Spinner) dialogLayout
				.findViewById(R.id.distance_spinner);
		unitsSpinner.setAdapter(unitsAdapter);

		unitsSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						if (parent.getItemIdAtPosition(position) == 0) {
							Settings.setDistanceUnits(Settings.DistanceUnits.Miles);
						} else {
							Settings.setDistanceUnits(Settings.DistanceUnits.Kilometers);
						}
						for (SettingsChangeListener changeListener : settingsListeners) {
							changeListener.onSettingsChanged();
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});
		unitsSpinner.setSelection(Settings.getItemCode(Settings
				.getDistanceUnits()));

		// Languages Spinner data
		String[] languages = new String[] { Constant.SETTINGS_LANGUAGE_ENGLISH,
				Constant.SETTINGS_LANGUAGE_SPANISH };
		ArrayAdapter<String> languageAdapter = new ArrayAdapter<String>(
				dialogLayout.getContext(), R.layout.spinner_item, languages);

		Spinner languageSpinner = (Spinner) dialogLayout
				.findViewById(R.id.language_spinner);
		languageSpinner.setAdapter(languageAdapter);

		languageSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						if (parent.getItemIdAtPosition(position) == 0) {
							Settings.setLanguage(Settings.LanguageList.English);
							Localization.setEnglish();
						} else {
							Settings.setLanguage(Settings.LanguageList.Spanish);
							Localization.setSpanish();
						}

						searchPreferencesText.setText(Localization
								.getSettingsSearchPreferenceTitle());
						launchNearMeText.setText(Localization
								.getSettingsLaunchWithNearMeTitle());
						distanceUnitText.setText(Localization
								.getSettingsDistanceUnitsTitle());
						languageText.setText(Localization
								.getSettingsLanguageTitle());
						versionText.setText(Localization.getSettingsVersion()
								+ " " + Constant.TABLET_APP_VERSION);
						visitPlayButton.setText(Localization
								.getSettingsVisitGooglePlay());
						sendFeedbackButton.setText(Localization
								.getSettingsFeedback());
						editProfileButton.setText(Localization
								.getSettingsEditProfile());

						String[] tempUnits = new String[] {
								Localization.getSettingsDistanceUnitsMiles(),
								Localization.getSettingsDistanceUnitsKm() };

						ArrayAdapter<String> tempAdapter = new ArrayAdapter<String>(
								dialogLayout.getContext(),
								R.layout.spinner_item, tempUnits);
						unitsSpinner.setAdapter(tempAdapter);

						unitsSpinner.setSelection(Settings.getItemCode(Settings
								.getDistanceUnits()));

						for (SettingsChangeListener changeListener : settingsListeners) {
							changeListener.onSettingsChanged();
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});
		languageSpinner.setSelection(Settings.getItemCode(Settings
				.getLanguage()));

		// Near with Me button data
		Switch nearMeButton = (Switch) dialogLayout
				.findViewById(R.id.tBtnLaunchNear);
		nearMeButton.setChecked(Settings.isLaunchWithNearMe());
		nearMeButton.setOnCheckedChangeListener(this);

		Switch geofenceButton = (Switch) dialogLayout
				.findViewById(R.id.tBtnSetGeofence);
		geofenceButton.setChecked(Settings.isSetGeofence());
		geofenceButton.setOnCheckedChangeListener(this);

		return dialogLayout;
	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		SettingsChangeListener listener;
		if (getTargetFragment() != null) {
			listener = (SettingsChangeListener) getTargetFragment();
		} else {
			listener = (SettingsChangeListener) activity;
		}
		listener.onSettingsShowed();
		Utils.startFlurry(this.getActivity(), Constant.SETTINGS_ACTIVITY_EVENT);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		for (SettingsChangeListener changeListener : settingsListeners) {
			changeListener.onSettingsDismissed();
		}
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.btnVisitPlay: {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(Utils.getAppUrl()));
			startActivity(intent);
			Utils.startFlurry(this.getActivity(),
					Constant.VISIT_PLAY_STORE_EVENT);
			break;
		}
		case R.id.btnFeedback: {
			ShareApp.byEmail(this.getActivity(),
					Constant.ALLPOINT_FEEDBACK_MAIL, Constant.SHARE_SUBJECT,
					Constant.SHARE_MESSAGE);
			Utils.startFlurry(this.getActivity(), Constant.SEND_FEEDBACK_EVENT);
			break;
		}
		case R.id.btnChangePassword: {

			InternetConnectionManager connectionManager = InternetConnectionManager_
					.getInstance_(getActivity());

			if (!connectionManager.isConnected()) {
				Utils.showDialogAlert(
						getResources().getString(
								R.string.en_dialogCannotConnectText),
						getActivity());
			} else {
				Utils.startActivity(getActivity(),
						ChangePasswordActivity_.class, false, false, false);
			}
			break;
		}

		case R.id.btnTermsAndCond: {

			InternetConnectionManager connectionManager = InternetConnectionManager_
					.getInstance_(getActivity());

			if (!connectionManager.isConnected()) {
				Utils.showDialogAlert(
						getResources().getString(
								R.string.en_dialogCannotConnectText),
						getActivity());
			} else {
				Utils.startActivity(getActivity(),
						com.allpoint.activities.TermsAndCondition_.class,
						false, false, false);
			}

			break;
		}

		case R.id.btnEditProfile: {

			atmfinderappcontext.isEditProfile = true;
			InternetConnectionManager connectionManager = InternetConnectionManager_
					.getInstance_(getActivity());
			if (!connectionManager.isConnected()) {

				Utils.showDialogAlert(
						getResources().getString(
								R.string.en_dialogCannotConnectText),
						getActivity());
			} else {
				Utils.startActivity(getActivity(), RegistrationActivity_.class,
						false, false, false);
			}
			break;
		}
		}
	}

	@Override
	public void onCheckedChanged(final CompoundButton buttonView,
			final boolean isChecked) {
		if (buttonView.getId() == R.id.tBtnSetGeofence) {
			if (isChecked) {
				atmfinderappcontext.isGeofenceOn = true;
				Settings.setGeofence(true);
			} else {
				atmfinderappcontext.isGeofenceOn = false;
				Settings.setGeofence(false);
			}
		} else {
			if (isChecked) {
				Settings.setLaunchWithNearMe(true);
			} else {
				Settings.setLaunchWithNearMe(false);
			}
		}
	}

	@Override
	public void onDestroyView() {
		// removes the dismiss message to avoid shown dialogs being dismissed.
		if (getDialog() != null && getRetainInstance()) {
			getDialog().setDismissMessage(null);
		}
		super.onDestroyView();
	}

	@Override
	public void onResume() {
		if (!Utils.getLoginStatus()) {
			settingsChangeButton.setVisibility(View.GONE);
			editProfileButton.setVisibility(View.GONE);
			layChangePass.setVisibility(View.GONE);
		} else {
			settingsChangeButton.setVisibility(View.VISIBLE);
			editProfileButton.setVisibility(View.GONE);
			layChangePass.setVisibility(View.VISIBLE);
		}
		super.onResume();
	}
}
