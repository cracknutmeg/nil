/**
 *@ SettingsActivity
 */
package com.allpoint.activities.phone;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.allpoint.AtmFinderApplication;
import com.allpoint.R;
import com.allpoint.activities.phone.fragments.TabBarFragment;
import com.allpoint.services.InternetConnectionManager;
import com.allpoint.util.Constant;
import com.allpoint.util.Localization;
import com.allpoint.util.Settings;
import com.allpoint.util.ShareApp;
import com.allpoint.util.Utils;
import com.bugsense.trace.BugSenseHandler;
import com.flurry.android.FlurryAgent;

/**
 * @author: Mikhail.Shalagin & Vyacheslav.Shmakin
 * @version: 23.09.13
 */
@EActivity(R.layout.settings)
public class SettingsActivity extends FragmentActivity implements
AdapterView.OnItemSelectedListener {

	TabBarFragment frag;
	@Bean
	InternetConnectionManager connectionManager;

	@ViewById(R.id.distance_spinner)
	protected Spinner unitsSpinner;

	@ViewById(R.id.language_spinner)
	protected Spinner languageSpinner;

	@ViewById(R.id.tBtnLaunchNear)
	protected Switch launchWithNearMeButton;

	@ViewById(R.id.tBtnSetGeofence)
	protected Switch launchSettingGeofence;

	// Settings
	@ViewById(R.id.tvSettingsTitle)
	protected TextView settingsTitle;

	@ViewById(R.id.tvSearchPreference)
	protected TextView settingsSearchPreference;

	@ViewById(R.id.btnVisitPlay)
	protected TextView settingsVisitGooglePlayButton;

	@ViewById(R.id.tvLaunchNear)
	protected TextView settingsLaunchNearWithMe;

	@ViewById(R.id.tvDistanceUnits)
	protected TextView settingsDistanceUnits;

	@ViewById(R.id.tvLanguage)
	protected TextView settingsLanguage;

	@ViewById(R.id.tvAppVersion)
	protected TextView settingsVersion;

	@ViewById(R.id.btnFeedback)
	protected TextView settingsFeedbackButton;

	@ViewById(R.id.tvSetGeofence)
	protected TextView tvSetGeofence;

	@ViewById(R.id.layGeoFence)
	protected LinearLayout layGeoFence;
	
	
	@ViewById(R.id.btnChangePassword)
	protected TextView settingsbtnChangePasswordButton;

	@ViewById(R.id.btnChangePasswordLine)
	protected TextView settingsbtnChangePasswordButtonLine;

	@Bean
	protected Settings settings;

	@ViewById(R.id.iBtnBottomMore)
	protected ImageButton settingsButton;

	@ViewById(R.id.iTxtBottomMore)
	protected TextView tvSetting;

	@ViewById(R.id.btnEditProfile)
	protected TextView btnEditprofileIs;

	@ViewById(R.id.btnTermsAndCond)
	protected TextView btnTermsAndCondition;

	AtmFinderApplication atmfinderappcontext;

	private static String AppUrl;

	public static String getAppUrl() {
		return AppUrl;
	}

	public static void setAppUrl(String newAppUrl) {
		AppUrl = newAppUrl;
	}

	@AfterViews
	protected void afterViews() {

		frag = (TabBarFragment) getSupportFragmentManager().findFragmentById(
				R.id.bottom_bar);

		BugSenseHandler.initAndStartSession(this, Constant.BUG_SENSE_API_KEY);

		if(Constant.HISTORY_BUTTON_DISABLE){
			settingsButton.setImageResource(R.drawable.bottom_settings_press);
			tvSetting.setTextColor(getResources().getColor(R.color.textColor));

			// settingsButton.setBackgroundResource(R.drawable.bottom_press_button_bg);
			settingsButton.setEnabled(true);

		} else {
			settingsButton.setImageResource(R.drawable.bottom_more_press);
			tvSetting.setTextColor(getResources().getColor(R.color.textColor));
			settingsButton.setEnabled(true);

		}
		
	}

	@Override
	public void onStart() {
		super.onStart();

		atmfinderappcontext = (AtmFinderApplication) getApplicationContext();

		//Hide old Password Field on Change Password
		atmfinderappcontext.setChangePassFromSettings = true;

		FlurryAgent.onStartSession(this, Constant.FLURRY_API_KEY);
		FlurryAgent.logEvent(Constant.SETTINGS_ACTIVITY_EVENT);
		FlurryAgent.onEndSession(this);

		String[] languages = new String[] { Constant.SETTINGS_LANGUAGE_ENGLISH,
				Constant.SETTINGS_LANGUAGE_SPANISH };
		ArrayAdapter<String> languageAdapter = new ArrayAdapter<String>(this,
				R.layout.spinner_item, languages);
		languageSpinner.setAdapter(languageAdapter);

		languageSpinner.setOnItemSelectedListener(this);

		languageSpinner.setSelection(Settings.getItemCode(Settings
				.getLanguage()));
		launchWithNearMeButton.setChecked(Settings.isLaunchWithNearMe());

		launchSettingGeofence.setChecked(Settings.isSetGeofence());
		
		/*if(atmfinderappcontext.isGeofenceOn && Settings.isSetGeofence()){
			launchSettingGeofence.setChecked(true);
		} else {
			launchSettingGeofence.setChecked(Settings.isSetGeofence());
		}*/
		
	}

	@Override
	public void onResume() {
		super.onResume();
		LoadLocalizedSettings();
		if (!Utils.getLoginStatus()) {
			settingsbtnChangePasswordButton.setVisibility(View.GONE);
			btnEditprofileIs.setVisibility(View.GONE);
			settingsbtnChangePasswordButtonLine.setVisibility(View.GONE);
			
			//disable Geo-fence if user not login
			layGeoFence.setVisibility(View.GONE);
			
		} else {
			settingsbtnChangePasswordButton.setVisibility(View.VISIBLE);
			btnEditprofileIs.setVisibility(View.GONE);
			settingsbtnChangePasswordButtonLine.setVisibility(View.VISIBLE);
			
			layGeoFence.setVisibility(View.VISIBLE);
		}
	}

	@Click(R.id.btnFeedback)
	protected void onBtnFeedbackClick() {
		ShareApp.byEmail(this, Constant.ALLPOINT_FEEDBACK_MAIL,
				Constant.SHARE_SUBJECT, Constant.SHARE_MESSAGE);
		FlurryAgent.onStartSession(this, Constant.FLURRY_API_KEY);
		FlurryAgent.logEvent(Constant.SEND_FEEDBACK_EVENT);
		FlurryAgent.onEndSession(this);
	}

	@Click(R.id.btnChangePassword)
	protected void onBtnChangePswdClick() {

		if (!connectionManager.isConnected()) {

			Utils.showDialogAlert(
					getResources().getString(
							R.string.en_dialogCannotConnectText),
							SettingsActivity.this);
		} else {

			Utils.startActivity(SettingsActivity.this,
					com.allpoint.activities.ChangePasswordActivity_.class,
					false, false, false);
		}
	}

	@Click(R.id.btnEditProfile)
	protected void onBtnEditProfileClicked() {

		atmfinderappcontext.isEditProfile = true;

		if (!connectionManager.isConnected()) {

			Utils.showDialogAlert(
					getResources().getString(
							R.string.en_dialogCannotConnectText),
							SettingsActivity.this);
		} else {
			Utils.startActivity(SettingsActivity.this,
					com.allpoint.activities.RegistrationActivity_.class, false,
					false, false);
		}
	}

	// Terms And Condtions
	@Click(R.id.btnTermsAndCond)
	protected void onBtnTermsAndCondClicked() {

		if (!connectionManager.isConnected()) {
			Utils.showDialogAlert(
					getResources().getString(
							R.string.en_dialogCannotConnectText),
							SettingsActivity.this);
		} else {
			Utils.startActivity(SettingsActivity.this,
					com.allpoint.activities.TermsAndCondition_.class, false,
					false, false);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		switch (parent.getId()) {
		case R.id.distance_spinner: {
			if (parent.getItemIdAtPosition(position) == 0) {
				Settings.setDistanceUnits(Settings.DistanceUnits.Miles);
			} else {
				Settings.setDistanceUnits(Settings.DistanceUnits.Kilometers);
			}

			break;
		}
		case R.id.language_spinner: {
			if (parent.getItemIdAtPosition(position) == 0) {
				Settings.setLanguage(Settings.LanguageList.English);
				Localization.setEnglish();
				// frag.onResume();
			} else {
				Settings.setLanguage(Settings.LanguageList.Spanish);
				Localization.setSpanish();
				// frag.onResume();
			}

			LoadLocalizedSettings();

			break;
		}
		}
	}

	private void LoadLocalizedSettings() {
		// Settings
		settingsTitle.setText(Localization.getSettingsLayoutTitle());
		settingsSearchPreference.setText(Localization
				.getSettingsSearchPreferenceTitle());
		settingsVisitGooglePlayButton.setText(Localization
				.getSettingsVisitGooglePlay());
		settingsLaunchNearWithMe.setText(Localization
				.getSettingsLaunchWithNearMeTitle());
		settingsLanguage.setText(Localization.getSettingsLanguageTitle());
		settingsDistanceUnits.setText(Localization
				.getSettingsDistanceUnitsTitle());
		settingsVersion.setText(Localization.getSettingsVersion() + " "
				+ Constant.PHONE_APP_VERSION);

		settingsFeedbackButton.setText(Localization.getSettingsFeedback());
		tvSetGeofence.setText(Localization.getSettingsbtnFeedback());
		btnTermsAndCondition.setText(Localization.getSettingsbtnTermsAndCond());

		String[] units = new String[] {
				Localization.getSettingsDistanceUnitsMiles(),
				Localization.getSettingsDistanceUnitsKm() };
		ArrayAdapter<String> unitsAdapter = new ArrayAdapter<String>(this,
				R.layout.spinner_item, units);
		unitsSpinner.setOnItemSelectedListener(this);
		unitsSpinner.setAdapter(unitsAdapter);

		unitsSpinner.setSelection(Settings.getItemCode(Settings
				.getDistanceUnits()));

		// frag.changeText();

		// tTxtBottomHome.setText(Localization.getMainMenuTabHome());
		// itxtBottomSearch.setText(Localization.getMainMenuSearchTitle());
		// itxtBottomTransaction.setText(Localization.getMainMenuTransTitle());
		// itxtBottomMessages.setText(Localization.getMainMenuMessagesTitle());
		// itxtBottomMore.setText(Localization.getMainMenuMessagesTitle());
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	@CheckedChange(R.id.tBtnLaunchNear)
	void checkedChangedOnTbtnLaunchNear(CompoundButton compoundButton,
			boolean isChecked) {
		if (isChecked) {
			Settings.setLaunchWithNearMe(true);
		} else {
			Settings.setLaunchWithNearMe(false);
		}
	}

	@CheckedChange(R.id.tBtnSetGeofence)
	void checkedChangedOnSetGeofence(CompoundButton compoundButton,
			boolean isChecked) {
		if (isChecked) {
			atmfinderappcontext.isGeofenceOn = true;
			Settings.setGeofence(true);
		} else {
			atmfinderappcontext.isGeofenceOn = false;
			Settings.setGeofence(false);
		}
	}

	@Override
	protected void onPause() {
		Settings.SaveSettings();
		super.onPause();
	}

	@Click(R.id.btnVisitPlay)
	protected void onBtnVisitPlayClicked() {
		final String appURL = Utils.getAppUrl();

		if (appURL != null && !appURL.isEmpty()) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(Utils.getAppUrl()));
			startActivity(intent);
		}

	}
}
