/**
 *@ AboutActivity
 */
package com.allpoint.activities.phone;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;

import android.net.ConnectivityManager;
import android.support.v4.app.FragmentActivity;
import android.webkit.WebView;
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
@EActivity(R.layout.about)
public class AboutActivity extends FragmentActivity {
	@SystemService
	ConnectivityManager connectivityManager;

	@ViewById(R.id.about_web_view)
	WebView webView;

	@ViewById(R.id.iBtnBottomMore)
	ImageButton moreButton;

	@ViewById(R.id.iTxtBottomMore)
	TextView moreButtonText;

	@ViewById(R.id.tvAboutTitle)
	TextView aboutTitle;
	
	//History Remove
	@ViewById(R.id.iTxtBottomTransaction)
	TextView itxtBottomTransaction;

	@ViewById(R.id.iBtnBottomTransaction)
	ImageButton btnTransaction;
	
	
	/*
	 * @ViewById(R.id.iBtnAboutBack) ImageButton backButton;
	 */

	@AfterViews
	void afterViews() {

		BugSenseHandler.initAndStartSession(this, Constant.BUG_SENSE_API_KEY);

		
		if(Constant.HISTORY_BUTTON_DISABLE){
			btnTransaction.setImageResource(R.drawable.bottom_about_press);
			itxtBottomTransaction.setTextColor(getResources().getColor(R.color.textColor));
			btnTransaction.setEnabled(true);
				
		} else {
			moreButton.setImageResource(R.drawable.bottom_more_press);
			moreButtonText.setTextColor(getResources().getColor(R.color.textColor));
			// aboutButton.setBackgroundResource(R.drawable.bottom_press_button_bg);
			moreButton.setEnabled(true);
		}
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
		}

		webView.loadUrl(Constant.ALLPOINT_ABOUT_URL);
	}

	@Override
	protected void onResume() {
		super.onResume();

		aboutTitle.setText(Localization.getAboutLayoutTitle());
	}

	/*
	 * @Click(R.id.iBtnAboutBack) void onIbtnAboutBackClicked() {
	 * onBackPressed(); }
	 * 
	 * @Click(R.id.tvAboutTitle) void onTvAboutTitleClick() { onBackPressed(); }
	 */
}
