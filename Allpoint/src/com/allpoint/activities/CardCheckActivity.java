package com.allpoint.activities;

import java.io.InputStream;
import java.util.regex.Pattern;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.allpoint.AtmFinderApplication;
import com.allpoint.R;
import com.allpoint.activities.phone.CardListActivity;
import com.allpoint.services.InternetConnectionManager;
import com.allpoint.services.LoadWebServiceAsync;
import com.allpoint.services.RespSessionInvalid;
import com.allpoint.services.ResponseVarifyPan;
import com.allpoint.services.WebServiceListner;
import com.allpoint.services.parse.ParseXML;
import com.allpoint.util.Constant;
import com.allpoint.util.Utils;
import com.allpoint.util.Validation;

@EActivity(R.layout.cardcheck_activity)
public class CardCheckActivity extends Activity implements WebServiceListner {

	final Pattern CODE_PATTERN = Pattern
			.compile("([0-9]{0,4})|([0-9]{4} )+|([0-9]{4} [0-9]{0,4})+");

	InputStream is;
	
	public static boolean prod_build;
	private ProgressDialog dialog;

	AtmFinderApplication appContext;

	@ViewById(R.id.edtCard_number)
	EditText mCardNumber;

	@ViewById(R.id.btnAddCard)
	Button AddCard;

	@Bean
	InternetConnectionManager connectionManager;

	Bundle bundle;

	RespSessionInvalid mRespForInvalidSession;

	ParseXML parseXml;

	/****************************** OnCreate ***********************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		appContext = (AtmFinderApplication) getApplicationContext();

		parseXml = new ParseXML();
		
		ContextWrapper activity = null;
		ApplicationInfo ai = null;
		try {
			ai = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    Bundle bundle = ai.metaData;
	    	    
	    prod_build = bundle.getBoolean("PRODUCTION_BUILD");
		
	}

	/****************************** After View ***********************************/

	@AfterViews
	void AfterViews() {

		// hide keyboard
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		if (Utils.getLoginStatus()) {

			mCardNumber
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							19) });
			/*************** Check ******************/
			mCardNumber.addTextChangedListener(new TextWatcher() {

				private boolean spaceDeleted;

				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
				}

				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					// check if a space was deleted
					CharSequence charDeleted = s.subSequence(start, start
							+ count);
					spaceDeleted = " ".equals(charDeleted.toString());
				}

				public void afterTextChanged(Editable editable) {
					// disable text watcher
					mCardNumber.removeTextChangedListener(this);

					// record cursor position as setting the text in the
					// textview
					// places the cursor at the end
					int cursorPosition = mCardNumber.getSelectionStart();
					String withSpaces = formatText(editable);
					mCardNumber.setText(withSpaces);
					// set the cursor at the last position + the spaces added
					// since the
					// space are always added before the cursor
					mCardNumber.setSelection(cursorPosition
							+ (withSpaces.length() - editable.length()));

					// if a space was deleted also deleted just move the cursor
					// before the space
					if (spaceDeleted) {
						mCardNumber.setSelection(mCardNumber
								.getSelectionStart() - 1);
						spaceDeleted = false;
					}

					// enable text watcher
					mCardNumber.addTextChangedListener(this);
				}

				private String formatText(CharSequence text) {
					StringBuilder formatted = new StringBuilder();
					int count = 0;
					for (int i = 0; i < text.length(); ++i) {
						if (Character.isDigit(text.charAt(i))) {
							if (count % 4 == 0 && count > 0)
								formatted.append(" ");
							formatted.append(text.charAt(i));
							++count;
						}
					}
					return formatted.toString();
				}
			});
		} else {
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}

			showCloseDialogAlert(getResources()
					.getString(R.string.card_add_msg));
		}

	}

	/****************************** OnResume ***********************************/

	@Override
	protected void onResume() {
		Utils.hideKeyboard(CardCheckActivity.this);
		super.onResume();
	}

	/****************************** Handling Click Events ***********************************/

	@Click(R.id.scan_card)
	void onIbtnScanCardClicked() {
		Utils.startActivity(CardCheckActivity.this,
				com.allpoint.activities.ScanCardActivity_.class, false, false,
				false);
	}

	@Click(R.id.cancel_card_check)
	void onIbtnCancelClicked() {
		if (Utils.getActivityStatus()) {
			final Class<?> activityClass;
			if (Utils.isTablet()) {
				activityClass = com.allpoint.activities.tablet.MainMenuActivity_.class;
			} else {
				activityClass = com.allpoint.activities.phone.MainMenuActivity_.class;
			}
			Utils.startActivity(CardCheckActivity.this, activityClass, false,
					false, true);
			Utils.setActivityStatus(false);
		} else {
			finish();
		}
	}

	@Click(R.id.btnAddCard)
	void onIbtnAddCardClicked() {

		checkCardValidation();

	}

	@Click(R.id.continue_button)
	void onIbtnContinueClicked() {

		checkCardValidation();
	}

	/****************************** Validation ***********************************/

	private void checkCardValidation() {

		if (mCardNumber.getText().length() == 0) {
			String alertMessage = getResources().getString(
					R.string.Err_Msg_cardNumber);
			Utils.showDialogAlert(alertMessage, CardCheckActivity.this);
		} else if (mCardNumber.getText().length() <  18) {
			String alertMessage = getResources().getString(
					R.string.Err_Msg_ValidcardNumber);
			Utils.showDialogAlert(alertMessage, CardCheckActivity.this);
		} else if (!Validation.validateCardNumber(Validation
				.keepNumbersOnly(mCardNumber.getText().toString()))) {
			String alertMessage = getResources().getString(
					R.string.Err_Msg_ValidchackcardNumber);
			Utils.showDialogAlert(alertMessage, CardCheckActivity.this);
		} else {
			// for add card
			callCardWebservice();
		}
	}

	/****************************** Server Call ***********************************/

	LoadWebServiceAsync callApi;

	private void callCardWebservice() {

		if (!connectionManager.isConnected()) {
			Utils.showDialogAlert(
					getResources().getString(
							R.string.en_dialogCannotConnectText),
					CardCheckActivity.this);

		} else {
			String value = null;

			value = "<PanCheckRequest>"
					+ "<PANData>"
					+ "<EmailId>"
					+ Utils.getUserName().trim()
					+ "</EmailId>"
					+ "<PANNumber>"
					+ mCardNumber.getText().toString().trim()
							.replaceAll("\\s+", "") + "</PANNumber>"
					//+ "<ApplicationId>" + Constant.ALLPOINT_SERVER_APP_ID	+ "</ApplicationId>" 
					+ "</PANData>" + "</PanCheckRequest>";

			callApi = new LoadWebServiceAsync(getApplicationContext(),
					CardCheckActivity.this, value,
					Constant.CUSTOMER_MANAGEMENT_TRANS_URL,
					Constant.VARIFY_PAN_METHOD_NAME,
					Constant.VARIFY_PAN_SOAP_ACTION,
					Constant.CUSTOMER_MANAGEMENT_TRANS_NAMESPACE,
					Utils.getUserName().trim(), appContext.sessionToken);

			
			dialog = ProgressDialog.show(CardCheckActivity.this,
					"Please wait...", "Loading...");
			dialog.show();
			callApi.execute();

		}
	}

	/****************************** Handling Response ***********************************/
	
	@Override
	public void onResult(String result) {
		
		
			mRespForInvalidSession = parseXml.parseXMLforSessionInvalid(result);
			// mRespForInvalidSession = parseXMLforSessionInvalid(result);

			// if session is Invalid
			if (mRespForInvalidSession != null
					&& mRespForInvalidSession.getSessionInvalidStatusCode()
							.equals(Constant.SESSION_ERROR_CODE)) {
				if (dialog != null) {
					dialog.dismiss();
				}

				// show message
				// showSessionInvalid(mRespForInvalidSession.getSessionInvalidStatusMessage());
				showSessionInvalid(getResources().getString(
						R.string.msg_sessionInvalid));

			} else {
				
				ResponseVarifyPan mResult = parseXml.parseXMLForVarifyPan(result);
				// ResponseVarifyPan mResult = parseXMLForVarifyPan(result);
				
				
				if (mResult != null && mResult.getVarifyPanStatus()) {
					if (dialog != null) {
						dialog.dismiss();
					}

					// check active & inActive flag
					if (mResult.getVarifyActivtInActiveCode()) {

						showCardScreenSuccess();
						
					} else {

						showCardFailScreen();
						//Utils.showDialogAlert(mResult.getVarifyPanStatusMessage(),
						//		CardCheckActivity.this);
					}
				} else if (mResult != null
						&& Integer.parseInt(mResult.getVarifyPanStatusCode()) == 131) {
					if (dialog != null) {
						dialog.dismiss();
					}

					showCardScreenSuccess();
									
				} else if (mResult != null
						&& Integer.parseInt(mResult.getVarifyPanStatusCode()) == 101) {
					if (dialog != null) {
						dialog.dismiss();
					}

					showCardFailScreen();
									
				} else {
					if (dialog != null) {
						dialog.dismiss();
					}
					
					if (mResult != null) {
						//Utils.showDialogAlert(mResult.getVarifyPanStatusMessage(),
						//		CardCheckActivity.this);
						showCardFailScreen();
					} else {
						Utils.showDialogAlert(
								getResources().getString(
										R.string.err_server_Connection),
								CardCheckActivity.this);
					}
					// finish();
				}
				mCardNumber.setText("");
			}
		
				
	}

	private void showCardScreenSuccess() {
		// TODO Auto-generated method stub
		appContext.isCardSuccess = true;

		String validCardNumber1 = mCardNumber.getText().toString()
				.replaceAll("\\s+", "");
		
		Intent intent = new Intent(CardCheckActivity.this,
				com.allpoint.activities.CardSuccessActivity_.class);
		
		
		intent.putExtra(
					"card_number",
					"XXXX XXXX XXXX "
							+ 
							validCardNumber1.substring(validCardNumber1.length()-4, validCardNumber1.length()));
		
		startActivity(intent);
		finish();
		// Utils.startActivity(CardCheckActivity.this,
		// com.allpoint.activities.CardSuccessActivity_.class,
		// false, false, true);
	}

	private void showCardFailScreen() {
		// TODO Auto-generated method stub
		appContext.isCardSuccess = false;

		Intent intent = new Intent(CardCheckActivity.this,
				com.allpoint.activities.CardSuccessActivity_.class);
		
			String validCardNumber = mCardNumber.getText().toString()
					.replaceAll("\\s+", "");
		
			intent.putExtra(
					"card_number",
					"XXXX XXXX XXXX "
							+ 
							validCardNumber.substring(validCardNumber.length()-4, validCardNumber.length()));
						startActivity(intent);
	}

	private void showCloseDialogAlert(String msg) {
		AlertDialog alertDialog = new AlertDialog.Builder(
				CardCheckActivity.this).create();
		alertDialog.setMessage(msg);
		alertDialog.setCancelable(false);
		alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				// do whatever you want the back key to do
			}
		});
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						finish();
						Utils.startActivity(CardCheckActivity.this,
								com.allpoint.activities.LoginActivity_.class,
								false, false, true);
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
					// finish();
				}
			});

		}

	}

	public void showSessionInvalid(String message) {

		AlertDialog alertDialog = new AlertDialog.Builder(CardCheckActivity.this)
				.create();
		alertDialog.setMessage(message);
		alertDialog.setCancelable(true);
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						Utils.setLoginStatus(false);
						Utils.setUsername("");
						Utils.startActivity(CardCheckActivity.this,
								com.allpoint.activities.LoginActivity_.class,
								false, false, true);

					}
				});
		alertDialog.show();
	}

	/****************************** Parsing Response ***********************************/

	/*
	 * private ResponseVarifyPan parseXMLForVarifyPan(String resp) {
	 * ResponseVarifyPan varifyPandetails = null; try {
	 * 
	 * // is = getResources().openRawResource(R.raw.resp);
	 * 
	 * 
	 * 
	 * is = new ByteArrayInputStream(resp.getBytes()); SAXParserFactory spf =
	 * SAXParserFactory.newInstance(); SAXParser sp = spf.newSAXParser();
	 * XMLReader xr = sp.getXMLReader();
	 * 
	 * ResponseHandlerForTransactions resp_Handler = new
	 * ResponseHandlerForTransactions(); xr.setContentHandler(resp_Handler);
	 * InputSource inStream = new InputSource(is); xr.parse(inStream);
	 * 
	 * varifyPandetails = resp_Handler.getVarifyPanResp();
	 * 
	 * is.close();
	 * 
	 * } catch (Exception e) { e.printStackTrace(); }
	 * 
	 * return varifyPandetails; }
	 */

	/*
	 * private String keepNumbersOnly(CharSequence s) { return
	 * s.toString().replaceAll("[^0-9]", ""); // Should of course be // more
	 * robust }
	 */

	/*
	 * private String formatNumbersAsCode(CharSequence s) { int groupDigits = 0;
	 * String tmp = ""; for (int i = 0; i < s.length(); ++i) { tmp +=
	 * s.charAt(i); ++groupDigits; if (groupDigits == 4) { tmp += " ";
	 * groupDigits = 0; } } return tmp; }
	 */

}
