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
import com.allpoint.services.RespForgetPass;
import com.allpoint.services.RespVerifyEmailRequest;
import com.allpoint.services.WebServiceListner;
import com.allpoint.services.parse.ParseXML;
import com.allpoint.util.Constant;
import com.allpoint.util.Utils;
import com.allpoint.util.Validation;

@EActivity(R.layout.forget_password_activity)
public class ForgetPasswordActivity extends Activity implements
WebServiceListner {

	public static String otpForgetPassMail;

	AtmFinderApplication atmfinderappcontext;

	@ViewById(R.id.titleSendMail)
	TextView txtTitle;

	@ViewById(R.id.iEdTxtemailID)
	EditText mEmail;

	@ViewById(R.id.iBtnSubmit)
	Button mSubmit;

	@ViewById(R.id.txtMsgDisplay)
	TextView txtMsgDisplay;


	@Bean
	InternetConnectionManager connectionManager;

	private ProgressDialog dialog;

	String respCardDetails;
	String AlertMessage;
	InputStream is;
	ResgistrationResponseData respData = null;

	ParseXML parseXML;

	/****************************** OnCreate ***********************************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		atmfinderappcontext = (AtmFinderApplication) getApplicationContext();

		parseXML = new ParseXML();
	}


	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		if(atmfinderappcontext.isFirstTimeReg){
			txtTitle.setText(getResources().getString(R.string.Verify_email_title));
			mEmail.setHint(getResources().getString(R.string.register_emailid_hint));
			txtMsgDisplay.setText(getResources().getString(R.string.first_time_reg_msg));
			mSubmit.setText(getResources().getString(R.string.send));
		}else{
			mEmail.setHint(getResources().getString(R.string.username_emailid_hint));
			txtMsgDisplay.setText(getResources().getString(R.string.forgetPassText));
			mSubmit.setText(getResources().getString(R.string.send));
		}
	}

	/****************************** Click Events ***********************************/
	@Click(R.id.cancel)
	void onIbtnBackClicked() {
		Utils.hideKeyboard(ForgetPasswordActivity.this);
		finish();
	}

	@Override
	public void onBackPressed() {
		Utils.hideKeyboard(ForgetPasswordActivity.this);
		super.onBackPressed();
	}

	@Click(R.id.iBtnSubmit)
	void onIbtnSubmitClicked() {
		checkValidation();
	}

	/****************************** Validation ***********************************/
	private boolean checkValidation() {

		if (mEmail.getText().length() != 0) {
			if (!Validation.isValidEmail(mEmail.getText().toString().trim())) {
				mEmail.requestFocus();
				String alertMessage = getResources().getString(
						R.string.Err_Msg_EmailValid);
				Utils.showDialogAlert(alertMessage, ForgetPasswordActivity.this);
			} else {
				InternetConnectionManager connectionManager = com.allpoint.services.InternetConnectionManager_
						.getInstance_(this);
				if (!connectionManager.isConnected()) {
					Utils.showDialogAlert(
							getResources().getString(
									R.string.en_dialogCannotConnectText),
									ForgetPasswordActivity.this);
				} else {
					Utils.hideKeyboard(ForgetPasswordActivity.this);

					if(atmfinderappcontext.isFirstTimeReg){
						//from First time registration
						callFirstTimeRegWebService();
					} else {
						//
						callForgetPasswordWebService();	
					}

				}
			}
		} else {
			String alertMessage = getResources().getString(
					R.string.Err_MSG_EMaild);
			Utils.showDialogAlert(alertMessage, ForgetPasswordActivity.this);

		}
		return false;
	}

	/****************************** Validation ***********************************/

	LoadWebServiceAsync callApi;

	// Login WebApi Call
	private void callForgetPasswordWebService() {

		if (!connectionManager.isConnected()) {
			Utils.showDialogAlert(
					getResources().getString(
							R.string.en_dialogCannotConnectText),
							ForgetPasswordActivity.this);

		} else {

			// forget Pass
			String value = "<LoginRequest>" + "<LoginData>" + "<EmailId>"
					+ mEmail.getText().toString() + "</EmailId>" + "<Password>"
					+ "</Password>" 
					//+ "<ApplicationId>"	+ Constant.ALLPOINT_SERVER_APP_ID + "</ApplicationId>"
					+ "</LoginData></LoginRequest>";

			if(atmfinderappcontext.sessionToken == null) {
				atmfinderappcontext.sessionToken = "";
			}
			
			callApi = new LoadWebServiceAsync(getApplicationContext(),
					ForgetPasswordActivity.this, value,
					Constant.CUSTOMER_MANAGEMENT_URL,
					Constant.FORGET_METHOD_NAME, Constant.FORGET_SOAP_ACTION,
					Constant.CUSTOMER_MANAGEMENT_NAMESPACE,
					Utils.getUserName().trim(), atmfinderappcontext.sessionToken);
			callApi.execute();
			dialog = ProgressDialog.show(ForgetPasswordActivity.this,
					"Please wait....", "Loading..");
			dialog.show();
		}
	}


	// Call 
	private void callFirstTimeRegWebService() {
		/*********************  Main Code *******************/
		if (!connectionManager.isConnected()) {
			Utils.showDialogAlert(
					getResources().getString(
							R.string.en_dialogCannotConnectText),
							ForgetPasswordActivity.this);

		} else {

			if(atmfinderappcontext.sessionToken == null) {
				atmfinderappcontext.sessionToken = "";
			}
			
			// forget Pass
			String value = "<VerifyEmailRequest>" + "<UserData>" 
					+ "<EmailId>"+ mEmail.getText().toString() + "</EmailId>" 
					//+"<ApplicationId>" + Constant.ALLPOINT_SERVER_APP_ID + "</ApplicationId>"
					+ "</UserData></VerifyEmailRequest>";

			callApi = new LoadWebServiceAsync(getApplicationContext(),
					ForgetPasswordActivity.this, value,
					Constant.CUSTOMER_MANAGEMENT_URL,
					Constant.FIRST_TIME_REG_METHOD_NAME, Constant.FIRST_TIME_REG_SOAP_ACTION,
					Constant.CUSTOMER_MANAGEMENT_NAMESPACE,
					Utils.getUserName().trim(), atmfinderappcontext.sessionToken);
			callApi.execute();
			dialog = ProgressDialog.show(ForgetPasswordActivity.this,
					"Please wait....", "Loading..");
			dialog.show();
		}

	}

	@Override
	public void onResult(String result) {

		if(atmfinderappcontext.isFirstTimeReg){

			// Parse For Verfiy Email - First Time Registration
			RespVerifyEmailRequest mResult = parseXML.parseXMLVerifyEmailRequest(result);

			if (mResult != null && mResult.getVerifyEmailStatus()) {
				if (dialog != null) {
					dialog.dismiss();
				}

			}
			
			statOTPScreen();

		} else {
			
			// Parse For Forget Password
			RespForgetPass mResult = parseXML.parseXMLForForgetPass(result);

			if (mResult != null && mResult.getForgetStatus()) {
				if (dialog != null) {
					dialog.dismiss();
				}
			} 
			
			statOTPScreen();
		}


		
	}
	
	
	private void statOTPScreen() {
		// TODO Auto-generated method stub
		Utils.setUsername(mEmail.getText().toString());
		//showCloseDialogAlert(mResult.getVerifyEmailStatusMessage());

		OTPActivity.OTP_Count = 0;

		if( atmfinderappcontext.isFirstTimeReg ) {

			atmfinderappcontext.setEvent = "2";

		} else {
			// forget passwrod flow
			atmfinderappcontext.setEvent = "1";	

		}

		// Start Foreget Password Activity
		if (dialog != null) {
			dialog.dismiss();
		}
		
		//set email text
		mEmail.setText("");
		
		
		Utils.startActivity(ForgetPasswordActivity.this,
				com.allpoint.activities.OTPActivity_.class,
				false, false, false);
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
					// finish();
				}
			});

		}
	}

	/*
	 * private RespForgetPass parseXMLForForgetPass(String resp) {
	 * RespForgetPass ulogindetails = null; try { // is =
	 * getResources().openRawResource(R.raw.resp); is = new
	 * ByteArrayInputStream(resp.getBytes()); SAXParserFactory spf =
	 * SAXParserFactory.newInstance(); SAXParser sp = spf.newSAXParser();
	 * XMLReader xr = sp.getXMLReader();
	 * 
	 * ResponseHandler resp_Handler = new ResponseHandler();
	 * xr.setContentHandler(resp_Handler); InputSource inStream = new
	 * InputSource(is); xr.parse(inStream);
	 * 
	 * ulogindetails = resp_Handler.getForgetResp();
	 * 
	 * is.close();
	 * 
	 * } catch (Exception e) { e.printStackTrace(); }
	 * 
	 * return ulogindetails; }
	 */

	private void showCloseDialogAlert(String msg) {
		AlertDialog alertDialog = new AlertDialog.Builder(
				ForgetPasswordActivity.this).create();
		alertDialog.setMessage(msg);

		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				// set OTP count 0  

				OTPActivity.OTP_Count = 0;

				if( atmfinderappcontext.isFirstTimeReg ) {

					atmfinderappcontext.setEvent = "2";

				} else {
					// forget passwrod flow
					atmfinderappcontext.setEvent = "1";	

				}

				Utils.startActivity(ForgetPasswordActivity.this,
						com.allpoint.activities.OTPActivity_.class,
						false, false, false);
			}
		});
		alertDialog.show();

	}



}
