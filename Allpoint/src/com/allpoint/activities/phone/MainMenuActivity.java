/**
 *@ SplashActivity
 */
package com.allpoint.activities.phone;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.allpoint.AtmFinderApplication;
import com.allpoint.R;
import com.allpoint.activities.ChangePasswordActivity;
import com.allpoint.activities.LoginActivity;
import com.allpoint.services.InternetConnectionManager;
import com.allpoint.services.LoadWebServiceAsync;
import com.allpoint.services.RespLogin;
import com.allpoint.services.RespResetPassword;
import com.allpoint.services.RespSessionInvalid;
import com.allpoint.services.RespSignOut;
import com.allpoint.services.WebServiceListner;
import com.allpoint.services.parse.ParseXML;
import com.allpoint.util.Constant;
import com.allpoint.util.Localization;
import com.allpoint.util.Settings;
import com.allpoint.util.Utils;
import com.bugsense.trace.BugSenseHandler;
import com.gimbal.android.PlaceManager;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushManager;

/**
 * MainMenuActivity
 * 
 * @author: Vyacheslav.Shmakin
 * @version: 23.08.13
 */

@EActivity(R.layout.main_menu)
public class MainMenuActivity extends FragmentActivity implements
RichPushManager.Listener, WebServiceListner{
	AtmFinderApplication atmfinderappcontext;
	ParseXML parseXML;
	private ProgressDialog dialog;
	@Bean
	InternetConnectionManager connectionManager;
	
	RespSessionInvalid mRespForInvalidSession;
	@Bean
	protected Settings settings;
	
	ParseXML parseXml;

	// MainMenu
	@ViewById(R.id.tvMenuSearch)
	protected TextView mainMenuSearch;

	// @ViewById(R.id.tvMenuAbout)
	// protected TextView mainMenuAbout;

	@ViewById(R.id.tvMenuScan)
	protected TextView mainMenuScan;

	@ViewById(R.id.tvMenuTrans)
	protected TextView mainMenuTrans;

	@ViewById(R.id.tvMenuMessages)
	protected TextView mainMenuMessages;

	@ViewById(R.id.layoutMenuMessageCount)
	protected RelativeLayout messageCountLayout;

	@ViewById(R.id.tvMenuNumberOfMessages)
	protected TextView numberOfMessagesText;

	@ViewById(R.id.iBtnBottomHome)
	protected ImageButton btnHome;

	@ViewById(R.id.iTxtBottomHome)
	protected TextView txtHome;

	@ViewById(R.id.iBtnMenuSearch)
	protected ImageButton searchBtn;

	@ViewById(R.id.tvMenuSearch)
	protected TextView txtSearch;

	@ViewById(R.id.iBtnMenuMessages)
	protected ImageButton messagesBtn;

	@ViewById(R.id.tvMenuMessages)
	protected TextView txtMessage;

	@ViewById(R.id.tvLogin)
	protected Button btnLogin;
	
	//For History Remove
	
	@ViewById(R.id.iBtnMenuTrans)
	protected ImageButton imgBtnTrasHistory;
	


	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		UAirship.shared().getRichPushManager().addListener(this);
		atmfinderappcontext = (AtmFinderApplication) getApplicationContext();
		parseXml = new ParseXML();
	}

	@AfterViews
	void AfterViews() {
		BugSenseHandler.initAndStartSession(this, Constant.BUG_SENSE_API_KEY);
		btnHome.setImageResource(R.drawable.bottom_home_press);
		txtHome.setTextColor(getResources().getColor(R.color.textColor));
		// btnHome.setBackgroundResource(R.drawable.bottom_press_button_bg);
		btnHome.setEnabled(true);
		if (!Utils.isSettingsLoaded()) {
			Settings.LoadSettings();
			Utils.setSettingsLoaded(true);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Utils.startFlurry(this, Constant.MAIN_MENU_ACTIVITY_EVENT);

		parseXML = new ParseXML();
		if (Utils.isFromSplash()) {
			Utils.setFromSplash(false);

			if (Settings.isLaunchWithNearMe()) {
				searchBtn.performClick();
			}

		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		Utils.showMessageCounter(messageCountLayout, numberOfMessagesText);

		if (Utils.getLoginStatus()) {
			// btnLogin.setText(getResources().getString(R.string.app_logout));
			btnLogin.setText(Localization.getlogout());

			PlaceManager.getInstance().startMonitoring();
		} else {
			// btnLogin.setText(getResources().getString(R.string.app_login));

			btnLogin.setText(Localization.getlogin());
		}

		// mainMenuAbout.setText(Localization.getMainMenuAboutTitle());

		mainMenuMessages.setText(Localization.getMainMenuMessagesTitle());
		mainMenuSearch.setText(Localization.getMainMenuSearchTitle());
		mainMenuScan.setText(Localization.getMainMenuScanTitle());
		
		
		
		
		if(Constant.HISTORY_BUTTON_DISABLE){
			//set About
			mainMenuTrans.setText(Localization.getMainMenuAboutTitle());
			imgBtnTrasHistory.setBackgroundResource(R.drawable.select_main_about);
			
			//Set Setting Button
			
			//
			
		} else {
			//set History
			mainMenuTrans.setText(Localization.getMainMenuTransTitle());
			imgBtnTrasHistory.setBackgroundResource(R.drawable.select_main_trans);
			
			//Set More Button
			
			
		}
		

	}

	@Override
	public void onUpdateMessages(boolean isSuccess) {
		Utils.showMessageCounter(messageCountLayout, numberOfMessagesText);
	}

	@Override
	public void onUpdateUser(boolean b) {
	}

	@Click(R.id.tvLogin)
	void onIbtnLoginClicked() {

		if (btnLogin.getText().toString()
				.equals(getResources().getString(R.string.app_login))) {

			Utils.startActivity(this,
					com.allpoint.activities.LoginActivity_.class, true, false,
					true);
		} else {


			//Call Logout Functionality
			callLogout();
		}
	}



	@Click(R.id.iBtnMenuSearch)
	void onIbtnMenuSearchClicked() {
		Utils.startActivity(this, MainActivity_.class, true, false, false);
	}

	@Click(R.id.iBtnMenuMessages)
	void onIbtnMenuMessagesClicked() {
		Utils.startActivity(this, MessageActivity_.class, true, false, false);
	}

	@Click(R.id.iBtnMenuScan)
	void onIbtnMenuCardScanClicked() {
		Utils.startActivity(MainMenuActivity.this,
				com.allpoint.activities.CardCheckActivity_.class, false, false,
				false);
	}

	@Click(R.id.iBtnMenuTrans)
	void onIbtnMenuTransClicked() {
		// Utils.startActivity(MainMenuActivity.this, CardListActivity_.class,
		// false, false, false);
		
		//History Remove Change
		if(Constant.HISTORY_BUTTON_DISABLE){
			Utils.startActivity(MainMenuActivity.this, AboutActivity_.class,
				 false, false, false);
		} else {
			
			startActivity(new Intent(MainMenuActivity.this, CardListActivity_.class));
		
		}
		
		
	}

	// @Override
	// public void onBackPressed() {
	// moveTaskToBack(true);
	//
	//
	// if (MainActivity.instance != null) {
	// MainActivity.instance.finish();
	// }
	// finish();

	// Intent i = new Intent(getApplicationContext(), MainMenuActivity.class);
	// // set the new task and clear flags
	// i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
	// Intent.FLAG_ACTIVITY_CLEAR_TASK);
	// startActivity(i);
	// finish();
	// super.onBackPressed();
	// }

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_BACK) {
	// if (MainActivity.instance != null) {
	// MainActivity.instance.finish();
	// }
	// finish();
	// System.exit(0);
	// return true;
	// }
	// return super.onKeyDown(keyCode, event);
	// }

	@Override
	public void onBackPressed() {
		Utils.setCurrentPosition(null);
		moveTaskToBack(true);
		if (MainActivity.instance != null) {
			MainActivity.instance.finish();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		UAirship.shared().getRichPushManager().removeListener(this);
	}







	/****************************** Server Call ***********************************/
	// Login WebApi Call

	LoadWebServiceAsync callApi;

	private void callLogout() {

		if (!connectionManager.isConnected()) {
			Utils.showDialogAlert(
					getResources().getString(
							R.string.en_dialogCannotConnectText),
							MainMenuActivity.this);

		} else {
			
			//Lock Main Menu
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
			
			String value = null;

			value = "<SignOutUser>" + "<Token>"
					+ atmfinderappcontext.sessionToken + "</Token>"  
					//+ "<ApplicationId>" + Constant.ALLPOINT_SERVER_APP_ID + "</ApplicationId>" 
					+ "</SignOutUser>";



			callApi = new LoadWebServiceAsync(getApplicationContext(),
					MainMenuActivity.this, value,
					Constant.CUSTOMER_MANAGEMENT_URL,
					Constant.LOGOUT_METHOD_NAME, Constant.LOGOUT_SOAP_ACTION,
					Constant.CUSTOMER_MANAGEMENT_NAMESPACE,
					Utils.getUserName().trim(), "");

			// LoadWebServiceAsync callApi = new
			// LoadWebServiceAsync(getApplicationContext(), LoginActivity.this,
			// value, Constant.HOSTNAME_LINK, Constant.FORGET_METHOD_NAME,
			// Constant.FORGET_SOAP_ACTION,Constant.FORGET_NAMESPACE);
			dialog = ProgressDialog.show(MainMenuActivity.this, "Please wait...",
					"Loading...");
			dialog.show();
			callApi.execute();
		}
	}

	@Override
	public void onResult(String result) {

		// session Token Checking
		mRespForInvalidSession = parseXml.parseXMLforSessionInvalid(result);
		// mRespForInvalidSession = parseXMLforSessionInvalid(result);

		// if session is Invalid
		if (mRespForInvalidSession != null
				&& !mRespForInvalidSession.getSessionInvalidStatusCode().toString().trim()
				.equals(Constant.SESSION_ERROR_CODE)) {
			
			if (dialog != null) {
				dialog.dismiss();
			}

			// show message
			showSessionInvalid(getResources().getString(R.string.msg_sessionInvalid));

		} else { 
			if (dialog != null) {
				
				dialog.dismiss();
			}
			RespSignOut mResult = parseXML.parseXMLSignOutRequest(result);

			if (mResult != null && mResult.getSignOutStatus()) {
				

				Utils.setLoginStatus(false);
				Utils.setUsername("");
				atmfinderappcontext.sessionToken =  mResult.getSignOutToken();
				
				if(atmfinderappcontext.sessionToken == null){
					atmfinderappcontext.sessionToken = "";
				}
				
				Utils.startActivity(this,
						com.allpoint.activities.LoginActivity_.class, true, false,
						true);	
			} else {
				
					Utils.showDialogAlert(
							getResources().getString(
									R.string.err_server_Connection),
									MainMenuActivity.this);
				
			}
		}
	}


	@Override
	public void onRunning() {
		// TODO Auto-generated method stub
		if (dialog != null) {
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog1) {
					callApi.cancel(true);
					if (dialog != null) {
						dialog.dismiss();
					}
					// finish();
				}
			});
		}
	}
	
	public void showSessionInvalid(String message) {

		AlertDialog alertDialog = new AlertDialog.Builder(MainMenuActivity.this)
				.create();
		alertDialog.setMessage(message);
		alertDialog.setCancelable(true);
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						Utils.setLoginStatus(false);
						Utils.setUsername("");
						Utils.startActivity(MainMenuActivity.this,
								com.allpoint.activities.LoginActivity_.class,
								false, false, true);

					}
				});
		alertDialog.show();
	}

}
