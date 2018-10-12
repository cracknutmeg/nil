/**
 *@ AboutActivity
 */
package com.allpoint.activities;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;

import android.net.ConnectivityManager;
import android.support.v4.app.FragmentActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import com.allpoint.R;
import com.allpoint.activities.fragments.AlertDialogFragment;
import com.allpoint.util.Constant;
import com.allpoint.util.Localization;
import com.allpoint.util.Utils;
import com.bugsense.trace.BugSenseHandler;

/**
 * AboutActivity
 * 
 * @author: Vyacheslav.Shmakin
 * @version: 23.09.13
 */
@EActivity(R.layout.act_terms_condition)
public class TermsAndCondition extends FragmentActivity {
	@SystemService
	ConnectivityManager connectivityManager;

	@ViewById(R.id.terms_condition_web_view)
	WebView webView;

	@ViewById(R.id.tvAboutTitle)
	TextView aboutTitle;

	@ViewById(R.id.iBtnTermsBack)
	ImageButton backButton;

	@AfterViews
	void afterViews() {
		BugSenseHandler.initAndStartSession(this, Constant.BUG_SENSE_API_KEY);
	}

	@Override
	protected void onStart() {
		super.onStart();

		Utils.startFlurry(this, Constant.ABOUT_ACTIVITY_EVENT);

		if (!(connectivityManager.getActiveNetworkInfo() != null && connectivityManager
				.getActiveNetworkInfo().isConnected())) {
			AlertDialogFragment alertDialog = new AlertDialogFragment(
					Localization.getDialogCannotConnectTitle(),
					Localization.getDialogCannotConnectText());
			alertDialog.show(getFragmentManager(), Constant.ERROR_DIALOG_TAG);
		} else {
			webView.setWebViewClient(new MyWebViewClient());
			openURL();
		}

	}

	private void openURL() {

		webView.getSettings().setDomStorageEnabled(true);

		webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setSupportZoom(true);
		// webView.getSettings().setDomStorageEnabled(true);
		// webView.getSettings().setLoadWithOverviewMode(true);
		// webView.getSettings().setUseWideViewPort(true);
		webView.setInitialScale(100);

		webView.loadUrl(Constant.ALLPOINT_TERMS_CONDTION_URL);
		webView.requestFocus();
	}

	@Click(R.id.iBtnTermsBack)
	void onIbtnAboutBackClicked() {
		onBackPressed();
	}

	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}

}
