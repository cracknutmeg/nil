package com.allpoint.activities;

import java.io.InputStream;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.allpoint.AtmFinderApplication;
import com.allpoint.R;
import com.allpoint.model.ResgistrationResponseData;
import com.allpoint.services.InternetConnectionManager;
import com.allpoint.services.LoadWebServiceAsync;
import com.allpoint.services.RespOTP;
import com.allpoint.services.RespOTPreg;
import com.allpoint.services.WebServiceListner;
import com.allpoint.services.parse.ParseXML;
import com.allpoint.util.Constant;
import com.allpoint.util.Utils;

@EActivity(R.layout.otp_activity)
public class OTPActivity extends Activity implements WebServiceListner {


	public static int OTP_Count = 0;

	public boolean checkCallFromOTP = false;

	public boolean checkCallFromResend_Forget = false;
	@ViewById(R.id.edtOTP)
	EditText txtOTP;

	@ViewById(R.id.iBtnSubmit)
	Button mSubmit;

	@ViewById(R.id.ResendOTP)
	TextView resendOTP;

	@Bean
	InternetConnectionManager connectionManager;

	private ProgressDialog dialog;

	// private ProgressDialog progressDialog;
	String respCardDetails;
	String AlertMessage;
	InputStream is;
	ResgistrationResponseData respData = null;

	AtmFinderApplication atmfinderappcontext;
	LoadWebServiceAsync callApi;

	ParseXML parseXML;

	/****************************** OnCreate ***********************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		atmfinderappcontext = (AtmFinderApplication) getApplicationContext();

		parseXML = new ParseXML();
	}

	/****************************** OnResume ***********************************/

	// not implemented

	/****************************** Handling Click Events ***********************************/
	@Click(R.id.cancel)
	void onIbtnBackClicked() {
		Utils.hideKeyboard(OTPActivity.this);
		finish();
	}

	@Override
	public void onBackPressed() {
		Utils.hideKeyboard(OTPActivity.this);
		super.onBackPressed();
	}

	
	@Click(R.id.ResendOTP)
	void onResendClicked() {

		OTP_Count = 0;

		txtOTP.setText("");

		checkCallFromOTP = false;
		if (atmfinderappcontext.setEvent.equalsIgnoreCase("1") ) {
			// from foget Password
			checkCallFromResend_Forget = true;
		} else {
			// from Registration
			checkCallFromResend_Forget = false;
		}

		callRegResetWebService();

	}
	
	@Click(R.id.iBtnSubmit)
	void onIbtnSubmitClicked() {

		checkValidation();
		
		if (txtOTP.getText().length() != 0) {
			OTP_Count++;	
			
		}
		

		
	}
	
	/****************************** Validation ***********************************/
	private boolean checkValidation() {

		if( OTP_Count < 4 ){

			if (txtOTP.getText().length() != 0) {

				InternetConnectionManager connectionManager = com.allpoint.services.InternetConnectionManager_
						.getInstance_(this);

				if (!connectionManager.isConnected()) {
					Utils.showDialogAlert(
							getResources().getString(
									R.string.en_dialogCannotConnectText),
									OTPActivity.this);
				} else {

					Utils.hideKeyboard(OTPActivity.this);
					// check from login > Reg > OTP
					callOTPWebService();
				}

			} else {

				String alertMessage = getResources()
						.getString(R.string.Err_Msg_OTP);
				Utils.showDialogAlert(alertMessage, OTPActivity.this);

			} 
		} else {
			OTP_Count = 0;
			//finish();
			
			Utils.hideKeyboard(OTPActivity.this);
			showCloseDialogAlert(getResources().getString(
					R.string.en_dialogTxtForMaxAttempt));
		}
		return false;
	}



	/****************************** Server Call ***********************************/

	// web service call on click of Submit OTP

	private void callOTPWebService() {

		if (!connectionManager.isConnected()) {
			Utils.showDialogAlert(
					getResources().getString(
							R.string.en_dialogCannotConnectText),
							OTPActivity.this);
		} else {

			// From Login > Registration flow completion

			// From Forget > 
			checkCallFromOTP = true;

			// Session Token
			String value = "<ValidateToken>" + "<UserName>"
					+ Utils.getUserName().trim() + "</UserName>" + "<Token>"
					+ txtOTP.getText().toString().trim() + "</Token>" + "<Event>"
					+ atmfinderappcontext.setEvent + "</Event>"
					//+ "<ApplicationId>" + Constant.ALLPOINT_SERVER_APP_ID + "</ApplicationId>" 
					+ "</ValidateToken>";

			String SET_METHOD_NAME = "", SET_SOAP_ACTION = "";

			SET_METHOD_NAME = Constant.OTP_BY_MAIL_METHOD_NAME;
			SET_SOAP_ACTION  = Constant.OTP_BY_MAIL_SOAP_ACTION;
			
			/*if(atmfinderappcontext.setEvent.equalsIgnoreCase("1")){
				// For Forget Password 
				SET_METHOD_NAME = Constant.OTP_METHOD_NAME;
				SET_SOAP_ACTION  = Constant.OTP_SOAP_ACTION;
			} else {
				// For Registration 
				SET_METHOD_NAME = Constant.OTP_BY_MAIL_METHOD_NAME;
				SET_SOAP_ACTION  = Constant.OTP_BY_MAIL_SOAP_ACTION;
			} */

			callApi = new LoadWebServiceAsync(getApplicationContext(),
					OTPActivity.this, value, Constant.CUSTOMER_MANAGEMENT_URL,
					SET_METHOD_NAME, SET_SOAP_ACTION,
					Constant.CUSTOMER_MANAGEMENT_NAMESPACE,
					Utils.getUserName().trim(), atmfinderappcontext.sessionToken);

			callApi.execute();
			dialog = ProgressDialog.show(OTPActivity.this, "Please wait....",
					"Loading..");
			dialog.show();
		}
	}

	private void callRegResetWebService() {

		if (!connectionManager.isConnected()) {

			Utils.showDialogAlert(
					getResources().getString(
							R.string.en_dialogCannotConnectText),
							OTPActivity.this);
		} else {


			String SET_RESET_METHOD_NAME = "", SET_RESET_SOAP_ACTION = "";

			// For Forget Password 
			SET_RESET_METHOD_NAME = Constant.OTT_RESEND_REG_METHOD_NAME;
			SET_RESET_SOAP_ACTION  = Constant.OTT_RESEND_REG_SOAP_ACTION;
			
			//if (atmfinderappcontext.setEvent.equalsIgnoreCase("1")) {

			atmfinderappcontext.sessionToken = "";

			String value = "<USEROTT>" + "<USERNAME>" + Utils.getUserName().trim()
					+ "</USERNAME>" 
					//+ "<ApplicationId>"	+ Constant.ALLPOINT_SERVER_APP_ID + "</ApplicationId>"
					+ "<EVENT>" + atmfinderappcontext.setEvent + "</EVENT>"
					+ "</USEROTT>";

			callApi = new LoadWebServiceAsync(getApplicationContext(),
					OTPActivity.this, value,
					Constant.CUSTOMER_MANAGEMENT_URL,
					SET_RESET_METHOD_NAME,
					SET_RESET_SOAP_ACTION,
					Constant.CUSTOMER_MANAGEMENT_NAMESPACE,
					Utils.getUserName().trim(), atmfinderappcontext.sessionToken);
			callApi.execute();
			dialog = ProgressDialog.show(OTPActivity.this,
					"Please wait....", "Loading..");
			dialog.show();

		}
	}



	/****************************** Handling Server Side Response ***********************************/
	@Override
	public void onResult(String result) {

		// Forget Password > True > 
		// SignUp > True >
		if (checkCallFromOTP) {
			
			RespOTP mResult = parseXML.parseXMLForOTP(result);
			// RespOTP mResult = parseXMLForOTP(result);

			if (mResult != null && mResult.getOTPStatus()) {

				Utils.hideKeyboard(OTPActivity.this);

				if (dialog != null) {
					dialog.dismiss();
				}

				if (atmfinderappcontext.setEvent.equalsIgnoreCase("1")) {
					// Start After Forgot Password > If true > move to Change Password Screen.
					atmfinderappcontext.setChangePassFromSettings = false;

					atmfinderappcontext.tempTokenForOTP = mResult.getOTPToken() ;	 
					Utils.startActivity(
							OTPActivity.this,
							com.allpoint.activities.ChangePasswordActivity_.class,
							false, false, true);

				} else {
					// Start After Signup > If true > move to Change Password Screen.
					atmfinderappcontext.tempTokenForOTP = mResult.getOTPToken() ;

					Utils.startActivity(OTPActivity.this,
							com.allpoint.activities.RegistrationActivity_.class,
							false, false, true);
				}

			} else {
				if (dialog != null) {
					dialog.dismiss();
				}
				
				//// Forget Password > false > 
				if (mResult != null) {
					txtOTP.setText("");
					Utils.showDialogAlert(mResult.getOTPStatusMessage()
							.toString().trim(), OTPActivity.this);
				} else {
					Utils.showDialogAlert(
							getResources().getString(
									R.string.err_server_Connection),
									OTPActivity.this);
				}
			}

		} else {

			//Forget Password > Resend True > 
			
			RespOTPreg mResult = null;
			mResult = parseXML.parseXMLforReg(result);
			// mResult = parseXMLforReg(result);

			if (mResult != null && mResult.getOTPRegStatus()) {
				if (dialog != null) {
					dialog.dismiss();
				}

				//showCloseDialogAlert(mResult.getOTPregStatusMessage());

				Utils.showDialogAlert(
						mResult.getOTPregStatusMessage().toString()
						.trim(), OTPActivity.this);

			} else {

				// from login > registration > resend button.

				if (dialog != null) {
					dialog.dismiss();
				}

				if (mResult != null) {
					txtOTP.setText("");
					Utils.showDialogAlert(
							mResult.getOTPregStatusMessage().toString()
							.trim(), OTPActivity.this);
				} else {
					Utils.showDialogAlert(
							getResources().getString(
									R.string.err_server_Connection),
									OTPActivity.this);
				}
			}
		} 
	}
	
	/****************************** Display Dialogs ***********************************/


	private void showCloseDialogAlert(String msg) { AlertDialog alertDialog =
			new AlertDialog.Builder(OTPActivity.this).create();
	alertDialog.setMessage(msg);
	alertDialog.setCancelable(false);
	alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new
			DialogInterface.OnClickListener() { public void onClick(DialogInterface
					dialog, int which) {

				finish();
			}
	}); 
	alertDialog.show();
	}


	@Override
	public void onRunning() {
		if (dialog != null) {
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog1) {
					callApi.cancel(true);
					if (dialog != null) {
						dialog.dismiss();
					}
				}
			});
		}

	}
}
