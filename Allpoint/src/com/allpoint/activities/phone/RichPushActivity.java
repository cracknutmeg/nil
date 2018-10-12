package com.allpoint.activities.phone;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.support.v4.app.FragmentActivity;
import android.widget.ImageButton;
import android.widget.TextView;

import com.allpoint.R;
import com.allpoint.util.Constant;
import com.allpoint.util.Localization;
import com.allpoint.util.Utils;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.widget.RichPushMessageWebView;

/**
 * RichPushActivity
 * 
 * @author: Vyacheslav.Shmakin
 * @version: 23.09.13
 */
@EActivity(R.layout.rich_push_message)
public class RichPushActivity extends FragmentActivity {

	@ViewById(R.id.iBtnBottomMessages)
	protected ImageButton btnMessages;

	@ViewById(R.id.iTxtBottomMessages)
	protected TextView tvMessage;

	// replace ua
	@ViewById(R.id.wViewRichPushView)
	protected RichPushMessageWebView messageView;

	@ViewById(R.id.tvMessageViewTitle)
	protected TextView messageViewTitle;

	@AfterViews
	protected void afterViews() {
		String messageID = getIntent().getStringExtra(Constant.RICH_PUSH_EXTRA);
		// replace RichPushInbox
		RichPushMessage msg = UAirship.shared().getRichPushManager()
				.getRichPushInbox().getMessage(messageID);
		messageView.getSettings().setJavaScriptEnabled(true);
		messageView.loadRichPushMessage(msg);

		btnMessages.setImageResource(R.drawable.bottom_messages_press);
		tvMessage.setTextColor(getResources().getColor(R.color.textColor));
		btnMessages.setEnabled(false);
	}

	@Override
	public void onStart() {
		super.onStart();
		Utils.startFlurry(this, Constant.RICH_PUSH_OPEN_EVENT);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// MainMenu
		messageViewTitle.setText(Localization.getMessageLayoutTitle());
	}

	@Override
	public void onBackPressed() {
		Utils.startActivity(this, MessageActivity_.class, false, false, true);
	}

	@Click(R.id.iBtnMessageViewBack)
	protected void onIbtnMessageViewBackClicked() {
		onBackPressed();
	}

	@Click(R.id.tvMessageViewTitle)
	public void onTvMessageViewTitleClick() {
		onBackPressed();
	}
}
