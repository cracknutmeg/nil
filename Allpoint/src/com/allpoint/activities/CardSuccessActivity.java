package com.allpoint.activities;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.allpoint.AtmFinderApplication;
import com.allpoint.R;
import com.allpoint.util.Constant;
import com.allpoint.util.Utils;

@EActivity(R.layout.card_success)
public class CardSuccessActivity extends Activity {

	@ViewById(R.id.txt_CardNo)
	TextView cardNoIs;

	@ViewById(R.id.txtCardValidMsg)
	TextView cardValidMsg;

	@ViewById(R.id.cardTitleMsg)
	TextView cardTitleMsg;

	@ViewById(R.id.validCardIcon)
	ImageView cardIcon;

	@ViewById(R.id.cardSuccessFail)
	TextView cardSuccesFail;

//	@ViewById(R.id.atmpassSignUpButton)
//	Button atmpassSignUp;

	String muskCard;

	AtmFinderApplication appContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		muskCard = getIntent().getStringExtra("card_number");
		appContext = (AtmFinderApplication) getApplicationContext();
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	protected void onStart() {
		if (!Utils.getLoginStatus()) {
			showDialogAlert("Login to Proceed");
		} else {
			cardNoIs.setText(muskCard);
		}
		if (appContext.isCardSuccess) {

			cardTitleMsg.setText(getResources()
					.getString(R.string.card_success));
			cardSuccesFail.setText(getResources().getString(
					R.string.card_success_msg));
			//atmpassSignUp.setVisibility(View.GONE);
			cardIcon.setVisibility(View.VISIBLE);
		    cardValidMsg.setText(getResources().getString(R.string.success_message));
		} else {

			cardTitleMsg.setText(getResources().getString(
					R.string.card_failue_title));
			cardSuccesFail.setText(getResources().getString(
					R.string.card_fail_msg));
			cardSuccesFail.setTextColor(getResources().getColor(R.color.red));
			//atmpassSignUp.setVisibility(View.VISIBLE);
			cardIcon.setVisibility(View.VISIBLE);
			cardValidMsg.setText(getResources().getString(
					R.string.failure_message));

			// cardIcon.setBackground(getResources().getDrawable(R.drawable.card_invalid));

			int sdk = android.os.Build.VERSION.SDK_INT;
			if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
				cardIcon.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.card_invalid));
			} else {
				cardIcon.setBackground(getResources().getDrawable(
						R.drawable.card_invalid));
			}
		}
		super.onStart();
	}

	@Click(R.id.done_button)
	void onIbtnContinueClicked() {

		if (appContext.isCardSuccess) {

			if (Utils.getLoginStatus()) {

				//History Remove
				if(Constant.HISTORY_BUTTON_DISABLE){
					
					//showDialogHistoryDisable(getResources().getString(R.string.en_card_history_disable));
					finish();
					
				} else {
					
					if (Utils.isTablet()) {

						startActivity(new Intent(
								CardSuccessActivity.this,
								com.allpoint.activities.tablet.CardListActivity_.class));
						finish();
						// activityClass =
						// com.allpoint.activities.tablet.CardListActivity_.class;
					} else {
						startActivity(new Intent(
								CardSuccessActivity.this,
								com.allpoint.activities.phone.CardListActivity_.class));
						finish();

					}
					// Utils.startActivity(CardSuccessActivity.this, activityClass,
					// false, false, true);
					
				}
				
				
			} else {
				Utils.showDialogAlert("User need to login first !",
						CardSuccessActivity.this);
			}

		} else {
			finish();
		}

	}

	/*@Click(R.id.atmpassSignUpButton)
	void onIbtnATMPassSigunpClicked() {
		String url = Constant.ATMPASS_LINK;
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}*/

	public void showDialogAlert(String message) {

		AlertDialog alertDialog = new AlertDialog.Builder(
				CardSuccessActivity.this).create();
		alertDialog.setMessage(message);

		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						Utils.startActivity(CardSuccessActivity.this,
								LoginActivity_.class, false, false, true);

					}
				});
		alertDialog.show();
	}
	
	public void showDialogHistoryDisable(String message) {

		AlertDialog alertDialog = new AlertDialog.Builder(
				CardSuccessActivity.this).create();
		alertDialog.setMessage(message);
		alertDialog.setCancelable(false);

		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						finish();

					}
				});
		alertDialog.show();
	}
}
