/**
 *@ AboutActivity
 */
package com.allpoint.activities.tablet;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import com.allpoint.R;
import com.allpoint.activities.fragments.AlertDialogFragment;
import com.allpoint.activities.tablet.fragments.SettingsFragment;
import com.allpoint.services.InternetConnectionManager;
import com.allpoint.util.Constant;
import com.allpoint.util.Localization;
import com.allpoint.util.Settings;
import com.allpoint.util.Utils;
import com.bugsense.trace.BugSenseHandler;

/**
 * AboutActivity
 * 
 * @author: Vyacheslav.Shmakin
 * @version: 08.01.14
 */
@EActivity(R.layout.about)
public class AboutActivity extends FragmentActivity implements
		SettingsFragment.SettingsChangeListener {

	@Bean
	InternetConnectionManager connectionManager;

	@ViewById(R.id.about_web_view)
	WebView webView;

	@ViewById(R.id.iBtnBottomMore)
	ImageButton moreButton;

	@ViewById(R.id.iTxtBottomMore)
	TextView aboutButtonText;

	@ViewById(R.id.tvAboutTitle)
	TextView aboutTitle;

	private ProgressDialog progressDialog;
	
	//History Remove
		@ViewById(R.id.iTxtBottomTransaction)
		TextView itxtBottomTransaction;

		@ViewById(R.id.iBtnBottomTransaction)
		ImageButton btnTransaction;

	@AfterViews
	void afterViews() {
		BugSenseHandler.initAndStartSession(this, Constant.BUG_SENSE_API_KEY);

		
		if(Constant.HISTORY_BUTTON_DISABLE){
			btnTransaction.setImageResource(R.drawable.bottom_about_press);
			itxtBottomTransaction.setTextColor(getResources().getColor(R.color.textColor));
			btnTransaction.setEnabled(true);
				
		} else {
			moreButton.setImageResource(R.drawable.bottom_more_press);
			moreButton.setEnabled(true);
			aboutButtonText
					.setTextColor(getResources().getColor(R.color.textColor));
		}
		
		

		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle(Localization.getDialogLoadingTitle());
		progressDialog.setMessage(Localization.getDialogPleaseWait());
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);

		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				progressDialog.show();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if (progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();

		Utils.startFlurry(this, Constant.ABOUT_ACTIVITY_EVENT);
		Settings.setCurrentActivity(Settings.CurrentActivity.AboutActivity);

		if (!connectionManager.isConnected()) {
			AlertDialogFragment alertDialog = new AlertDialogFragment(
					Localization.getDialogCannotConnectTitle(),
					Localization.getDialogCannotConnectText(),
					Localization.getDialogOk());
			alertDialog.show(getFragmentManager(), Constant.ERROR_DIALOG_TAG);
			return;
		}
		webView.loadUrl(Constant.ALLPOINT_ABOUT_URL);
	}

	@Override
	protected void onResume() {
		super.onResume();
		aboutTitle.setText(Localization.getAboutLayoutTitle());
	}

	@Override
	public void onBackPressed() {
		finish();
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
