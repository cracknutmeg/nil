/**
 *@ MessageActivity
 */
package com.allpoint.activities.phone;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.allpoint.AtmFinderApplication;
import com.allpoint.R;
import com.allpoint.activities.fragments.AlertDialogFragment;
import com.allpoint.model.CustomRichPushMessage;
import com.allpoint.util.Constant;
import com.allpoint.util.Localization;
import com.allpoint.util.Settings;
import com.allpoint.util.Utils;
import com.allpoint.util.adapters.MessagesListAdapter;
import com.allpoint.util.adapters.MessagesNoCheckboxListAdapter;
import com.bugsense.trace.BugSenseHandler;
import com.flurry.android.FlurryAgent;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;

/**
 * MessageActivity
 * 
 * @author: Vyacheslav.Shmakin
 * @version: 23.09.13
 */

@EActivity(R.layout.messages)
public class MessageActivity extends FragmentActivity implements
		AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener,
		RichPushManager.Listener {

	@Bean
	protected Utils utils;

	@Bean
	protected Settings settings;

	@ViewById(R.id.listView)
	protected ListView listViewMessages;

	@ViewById(R.id.layoutNoMessages)
	protected ViewGroup noMessagesLayout;

	@ViewById(R.id.iBtnBottomMessages)
	protected ImageButton btnMessages;

	@ViewById(R.id.iTxtBottomMessages)
	protected TextView tvMessage;

	// Messages
	@ViewById(R.id.tvMessagesTitle)
	protected TextView messagesTitle;

	@ViewById(R.id.tvNoMessages)
	protected TextView noMessagesText;

	@ViewById(R.id.btnDeleteMessages)
	protected Button deleteMessagesButton;

	@ViewById(R.id.btnReadMessages)
	protected Button readMessagesButton;

	@ViewById(R.id.layoutBarMessageCount)
	protected RelativeLayout messageCountLayout;

	@ViewById(R.id.editLayout)
	protected LinearLayout editLayout;

	@ViewById(R.id.tvBarNumberOfMessages)
	protected TextView numberOfMessagesText;

	@ViewById(R.id.tBtnEditMode)
	protected ToggleButton editModeButton;
	private AlertDialogFragment alertDialog;
	private ProgressDialog dialog;

	private MessagesListAdapter messageAdapter;
	private MessagesNoCheckboxListAdapter messageNoCheckboxAdapter;
	private List<CustomRichPushMessage> messages = new ArrayList<CustomRichPushMessage>();
	private List<CustomRichPushMessage> backupMessages;
	private List<CustomRichPushMessage> afterEditRichPushes = new ArrayList<CustomRichPushMessage>();

	private int checkedCount = 0;

	@Override
	public void onStart() {
		super.onStart();

		editModeButton.setChecked(false);
		deleteMessagesButton.setEnabled(false);
		readMessagesButton.setEnabled(false);

		FlurryAgent.onStartSession(this, Constant.FLURRY_API_KEY);
		FlurryAgent.logEvent(Constant.MESSAGES_ACTIVITY_EVENT);
		FlurryAgent.onEndSession(this);

		messages = convertRichPushToCustom(UAirship.shared()
				.getRichPushManager().getRichPushInbox().getMessages());
		afterEditRichPushes = messages;

		// Log.i("LOG:::", String.valueOf(messages.size()));
		messageNoCheckboxAdapter = new MessagesNoCheckboxListAdapter(this,
				R.layout.message_item_no_checkboxes);

		listViewMessages.setAdapter(messageNoCheckboxAdapter);
		messageNoCheckboxAdapter.addAll(messages);

		// Ãƒï¿½Ã¢â‚¬Â¢Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
		// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
		// Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â‚¬Å¡,
		// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾
		// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â·Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
		// Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â€šÂ¬
		if (messages.size() == 0) {
			noMessagesLayout.setVisibility(View.VISIBLE);
			editModeButton.setEnabled(false);
		} else {
			// Ãƒï¿½Ã¢â‚¬Â¢Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
			// Ãƒï¿½Ã‚Â¶Ãƒï¿½Ã‚Âµ
			// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸
			// Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™,
			// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾
			// Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
			// Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â³Ãƒï¿½Ã‚Â¾
			noMessagesLayout.setVisibility(View.GONE);
			editModeButton.setEnabled(true);
		}

		editLayout.setVisibility(View.GONE);
		checkedCount = 0;
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Messages
		messagesTitle.setText(Localization.getMessagesLayoutTitle());
		noMessagesText.setText(Localization.getMessagesNoMessagesText());
		deleteMessagesButton.setText(Localization.getMessagesBtnDelete());
		readMessagesButton.setText(Localization.getMessagesBtnRead());

		if (!editModeButton.isChecked()) {
			editModeButton.setText(Localization.getMessagesEditTextOff());
		} else {
			editModeButton.setText(Localization.getMessagesEditTextOn());
		}

		editModeButton.setTextOff(Localization.getMessagesEditTextOff());
		editModeButton.setTextOn(Localization.getMessagesEditTextOn());

		NotificationManager nManager = (NotificationManager) getApplicationContext()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nManager.cancelAll();

		if (AtmFinderApplication.getUnreadCounter() != 0) {
			messageCountLayout.setVisibility(View.VISIBLE);
			// Ãƒï¿½Ã‚Â£Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â·Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
			// Ãƒï¿½Ã‚Â²
			// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚ÂºÃƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Âµ
			// Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¾
			numberOfMessagesText.setText(String.valueOf(AtmFinderApplication
					.getUnreadCounter()));
		} else {
			messageCountLayout.setVisibility(View.GONE);
		}

		UAirship.shared().getRichPushManager().addListener(this);
		// Ãƒï¿½Ã…Â¸Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â½Ãƒâ€˜Ã†â€™Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â»Ãƒâ€˜Ã…â€™Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾
		// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â»Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
		// Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Âº
		// Ãƒâ€˜Ã¯Â¿Â½
		// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¼Ãƒï¿½Ã‚Â¸
		// Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¸
		// Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚ÂºÃƒâ€˜Ã¢â€šÂ¬Ãƒâ€˜Ã¢â‚¬Â¹Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¸
		// Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â¸

		if (dialog != null) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
		}

		// Ãƒï¿½Ã…Â¸Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â·Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
		// Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â³
		// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Âµ
		// Ãƒï¿½Ã‚Â·Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã†â€™Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°
		// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
		// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
		dialog = ProgressDialog.show(this,
				Localization.getDialogLoadingTitle(),
				Localization.getDialogPleaseWait(), true);

		UAirship.shared().getRichPushManager().refreshMessages();
	}

	@AfterViews
	protected void afterViews() {
		BugSenseHandler.initAndStartSession(this, Constant.BUG_SENSE_API_KEY);

		Settings.LoadSettings();

		btnMessages.setImageResource(R.drawable.bottom_messages_press);
		tvMessage.setTextColor(getResources().getColor(R.color.textColor));
		// btnMessages.setBackgroundResource(R.drawable.bottom_press_button_bg);
		btnMessages.setEnabled(false);

		listViewMessages.setOnItemClickListener(this);
		listViewMessages.setOnItemLongClickListener(this);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		CheckBox chBox = (CheckBox) view.findViewById(R.id.chBoxMessages);
		// Ãƒï¿½Ã¢â‚¬Â¢Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
		// Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚ÂºÃƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â°
		// Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â‚¬Å¡,
		// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾ longclick =
		// Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â°Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™
		// Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â·
		// Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚ÂºÃƒâ€˜Ã¢â€šÂ¬Ãƒâ€˜Ã¢â‚¬Â¹Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
		if (chBox == null) {
			if (!messages.get(position).isRead()) {
				// save index and top position
				int index = listViewMessages.getFirstVisiblePosition();
				View v = listViewMessages.getChildAt(0);
				int top = (v == null) ? 0 : v.getTop();

				HashSet<String> messageId = new HashSet<String>();

				Vibrator vibe = (Vibrator) this
						.getSystemService(Context.VIBRATOR_SERVICE);
				vibe.vibrate(Constant.VIBRATOR_DURATION);

				afterEditRichPushes.get(position).setRead(true);
				messageId.add(afterEditRichPushes.get(position).getMessageId());

				// replace RichPushInbox
				UAirship.shared().getRichPushManager().getRichPushInbox()
						.markMessagesRead(messageId);

				messageId.clear();

				// Ãƒï¿½Ã…Â¸Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â·Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
				// Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â³
				// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Âµ
				// Ãƒï¿½Ã‚Â·Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã†â€™Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°
				// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
				// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
				dialog = ProgressDialog.show(this,
						Localization.getDialogLoadingTitle(),
						Localization.getDialogPleaseWait(), true);

				UAirship.shared().getRichPushManager().refreshMessages();

				checkedCount = 0;

				if (editModeButton.isChecked()) {
					if (messageAdapter != null) {
						messageAdapter.clear();
					}
					messageAdapter = new MessagesListAdapter(this,
							R.layout.message_item);

					listViewMessages.setAdapter(messageAdapter);
					messageAdapter.addAll(messages);
				} else {
					if (messageNoCheckboxAdapter != null) {
						messageNoCheckboxAdapter.clear();
					}
					messageNoCheckboxAdapter = new MessagesNoCheckboxListAdapter(
							this, R.layout.message_item_no_checkboxes);

					listViewMessages.setAdapter(messageNoCheckboxAdapter);
					messageNoCheckboxAdapter.addAll(messages);
				}

				if (AtmFinderApplication.getUnreadCounter() != 0) {
					messageCountLayout.setVisibility(View.VISIBLE);
					// Ãƒï¿½Ã‚Â£Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â·Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
					// Ãƒï¿½Ã‚Â²
					// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚ÂºÃƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Âµ
					// Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¾
					numberOfMessagesText.setText(String
							.valueOf(AtmFinderApplication.getUnreadCounter()));
				} else {
					messageCountLayout.setVisibility(View.GONE);
				}

				deleteMessagesButton.setEnabled(false);
				readMessagesButton.setEnabled(false);

				Toast.makeText(this, Localization.getDialogMessageMarked(),
						Toast.LENGTH_LONG).show();

				// restore
				listViewMessages.setSelectionFromTop(index, top);
			}
		} else {
			// Ãƒï¿½Ã¢â‚¬Â¢Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
			// Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚ÂºÃƒâ€˜Ã¯Â¿Â½
			// Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™,
			// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾ longclick =
			// Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â°Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™
			// Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚ÂºÃƒâ€˜Ã¢â€šÂ¬Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â²
			messages.get(position).setRead(true);
			Intent newIntent = new Intent(this, RichPushActivity_.class);
			newIntent.putExtra(Constant.RICH_PUSH_EXTRA, messages.get(position)
					.getMessageId());
			newIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
					| Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(newIntent);
			finish();
		}

		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		CheckBox chBox = (CheckBox) view.findViewById(R.id.chBoxMessages);
		// Ãƒï¿½Ã¢â‚¬Â¢Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
		// Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚ÂºÃƒâ€˜Ã¯Â¿Â½
		// Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™,
		// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾
		// Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Âº
		// =
		// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¼Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™
		if (chBox != null) {
			if (!chBox.isChecked()) {
				chBox.setChecked(true);
				messages.get(position).setChecked(true);
				checkedCount++;
			} else {
				chBox.setChecked(false);
				messages.get(position).setChecked(false);
				checkedCount--;
			}

			if (checkedCount == 0) {
				// editLayout.setVisibility(View.GONE);
				deleteMessagesButton.setText(Localization
						.getMessagesBtnDelete());
				readMessagesButton.setText(Localization.getMessagesBtnRead());
				deleteMessagesButton.setEnabled(false);
				readMessagesButton.setEnabled(false);
			} else {
				// editLayout.setVisibility(View.VISIBLE);
				deleteMessagesButton.setText(Localization
						.getMessagesBtnDelete() + " (" + checkedCount + ")");
				readMessagesButton.setText(Localization.getMessagesBtnRead()
						+ " (" + checkedCount + ")");
				deleteMessagesButton.setEnabled(true);
				readMessagesButton.setEnabled(true);
			}
		} else { // Ãƒï¿½Ã¢â‚¬Â¢Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
					// Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚ÂºÃƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â°
					// Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â‚¬Å¡,
					// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾
					// Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Âº
					// =
					// Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â°Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™
					// (Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚ÂºÃƒâ€˜Ã¢â€šÂ¬Ãƒâ€˜Ã¢â‚¬Â¹Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™)
			messages.get(position).setRead(true);
			Intent newIntent = new Intent(this, RichPushActivity_.class);
			newIntent.putExtra(Constant.RICH_PUSH_EXTRA, messages.get(position)
					.getMessageId());
			newIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
					| Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(newIntent);
			finish();
		}
	}

	@Click(R.id.btnDeleteMessages)
	protected void onBtnDeleteMessagesClicked() {
		// Ãƒï¿½Ã…Â¸Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â€šÂ¬Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
		// Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™
		// Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
		// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
		// Ãƒï¿½Ã‚Â²
		// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Âµ
		if (messages != null && messages.size() != 0) {

			backupMessages = new ArrayList<CustomRichPushMessage>(messages);
			final int tempChecks = checkedCount;

			// Ãƒï¿½Ã‚Â¡Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â°Ãƒâ€˜Ã‹â€ Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
			// Ãƒâ€˜Ã†â€™
			// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â»Ãƒâ€˜Ã…â€™Ãƒï¿½Ã‚Â·Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â»Ãƒâ€˜Ã¯Â¿Â½
			// Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¹Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â»Ãƒâ€˜Ã…â€™Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾
			// Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
			// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â½
			// Ãƒâ€˜Ã¢â‚¬Â¦Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â‚¬Å¡
			// Ãƒâ€˜Ã†â€™Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™
			// Ãƒï¿½Ã‚Â²Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â½Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Âµ
			// Push-Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
			alertDialog = new AlertDialogFragment(
					Localization.getDialogRemoveMessages(),
					Localization.getDialogOk(),
					new DialogInterface.OnClickListener() {
						// Ãƒï¿½Ã¢â‚¬ï¿½Ãƒï¿½Ã¯Â¿Â½
						@Override
						public void onClick(DialogInterface alertDialog,
								int which) {
							// Ãƒï¿½Ã…Â¸Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â·Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
							// Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â³
							// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Âµ
							// Ãƒï¿½Ã‚Â·Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã†â€™Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°
							// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
							// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
							dialog = ProgressDialog.show(MessageActivity.this,
									Localization.getDialogLoadingTitle(),
									Localization.getDialogPleaseWait(), true);

							HashSet<String> deletingMessages = new HashSet<String>();
							// Ãƒï¿½Ã‚Â¡Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â·Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â°Ãƒâ€˜Ã¢â‚¬ËœÃƒï¿½Ã‚Â¼
							// Ãƒï¿½Ã‚Â²Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â½Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â¹
							// Ãƒï¿½Ã‚Â¼Ãƒï¿½Ã‚Â°Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â²,
							// Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â€šÂ¬Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â¹
							// Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã†â€™Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â‚¬Å¡
							// Ãƒâ€˜Ã¢â‚¬Â¦Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™
							// Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â²Ãƒâ€˜Ã‹â€ Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¯Â¿Â½
							// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã¢â‚¬Â¹
							// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Âµ
							// Ãƒâ€˜Ã†â€™Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
							List<CustomRichPushMessage> tempList = new ArrayList<CustomRichPushMessage>(
									backupMessages.size() - tempChecks);

							// Ãƒï¿½Ã…Â¸Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â³Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¯Â¿Â½
							// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾
							// Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â¶Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¼Ãƒâ€˜Ã†â€™
							// Ãƒï¿½Ã‚Â²Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¼Ãƒâ€˜Ã†â€™
							// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã…Â½
							// Ãƒï¿½Ã‚Â¸
							// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
							// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Âº
							// Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â»Ãƒâ€˜Ã¯Â¿Â½
							// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â´Ãƒâ€˜Ã†â€™Ãƒâ€˜Ã…Â½Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â³Ãƒï¿½Ã‚Â¾
							// Ãƒâ€˜Ã†â€™Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
							for (int i = 0; i < backupMessages.size(); i++) {
								if (backupMessages.get(i).isChecked()) {
									// Log.i("LOG:::", i +
									// " is Checked. AND ID = " +
									// backupMessages.get(i).getMessageId());
									deletingMessages.add(backupMessages.get(i)
											.getMessageId());
								} else {
									// Ãƒï¿½Ã¢â‚¬â€�Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
									// Ãƒï¿½Ã‚Â²
									// Ãƒï¿½Ã‚Â¼Ãƒï¿½Ã‚Â°Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â²
									// Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã†â€™Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â»Ãƒâ€˜Ã¢â‚¬ËœÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â½Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Âµ
									// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã¢â‚¬Â¹
									tempList.add(backupMessages.get(i));
								}
							}
							// Ãƒï¿½Ã‚Â£Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â»Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
							// Ãƒï¿½Ã‚Â²Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Âµ
							// Ãƒï¿½Ã‚Â²Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â½Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Âµ
							// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã¢â‚¬Â¹

							// replace RichPushInbox
							UAirship.shared().getRichPushManager()
									.getRichPushInbox()
									.deleteMessages(deletingMessages);

							// Ãƒï¿½Ã¢â‚¬â€�Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¼Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼,
							// Ãƒâ€˜Ã¢â‚¬Â¡Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾
							// Ãƒï¿½Ã‚Â¼Ãƒâ€˜Ã¢â‚¬Â¹
							// Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Âµ
							// Ãƒâ€˜Ã†â€™Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
							// Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â·
							// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°.
							// Ãƒï¿½Ã…Â¸Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¼
							// Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Å¡
							// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Âº
							// Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã†â€™Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â‚¬Å¡
							// Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¯Â¿Â½
							// Ãƒâ€˜Ã¯Â¿Â½
							// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â»Ãƒâ€˜Ã†â€™Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â½Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â¼
							// Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¸
							// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¸
							// Ãƒï¿½Ã¢â‚¬Â¢Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
							// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â½
							// Ãƒï¿½Ã‚Â¸
							// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â»Ãƒâ€˜Ã†â€™Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â½Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â¹
							// Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã†â€™Ãƒï¿½Ã‚Â´Ãƒâ€˜Ã†â€™Ãƒâ€˜Ã¢â‚¬Å¡
							// Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â·Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â°Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¯Â¿Â½,
							// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾
							// Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â·Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¹Ãƒï¿½Ã‚Â´Ãƒâ€˜Ã¢â‚¬ËœÃƒâ€˜Ã¢â‚¬Å¡
							// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Âµ
							// Ãƒï¿½Ã‹Å“
							// Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Å¡
							// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¯Â¿Â½
							afterEditRichPushes = tempList;

							// Ãƒï¿½Ã…Â¸Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â½Ãƒâ€˜Ã†â€™Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â»Ãƒâ€˜Ã…â€™Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾
							// Ãƒï¿½Ã‚Â·Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â°Ãƒâ€˜Ã‹â€ Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
							// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Âµ
							// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
							UAirship.shared().getRichPushManager()
									.refreshMessages();

							deletingMessages.clear();
							backupMessages.clear();
							tempList.clear();

							alertDialog.dismiss();
						}
					}, Localization.getDialogCancel(),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							alertDialog.dismiss();
						}
					});
			alertDialog.show(getFragmentManager(), Constant.ERROR_DIALOG_TAG);
		}
	}

	@Click(R.id.btnReadMessages)
	protected void onBtnReadMessagesClicked() {
		// Ãƒï¿½Ã…Â¸Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â€šÂ¬Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
		// Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™
		// Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
		// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
		// Ãƒï¿½Ã‚Â²
		// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Âµ
		if (messages != null && messages.size() != 0) {
			// Ãƒï¿½Ã…Â¸Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â·Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
			// Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â³
			// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Âµ
			// Ãƒï¿½Ã‚Â·Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã†â€™Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°
			// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
			// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
			dialog = ProgressDialog.show(MessageActivity.this,
					Localization.getDialogLoadingTitle(),
					Localization.getDialogPleaseWait(), true);

			backupMessages = new ArrayList<CustomRichPushMessage>(messages);

			HashSet<String> markingMessages = new HashSet<String>();
			// Ãƒï¿½Ã…Â¸Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â³Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¯Â¿Â½
			// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾
			// Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â¶Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¼Ãƒâ€˜Ã†â€™
			// Ãƒï¿½Ã‚Â²Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¼Ãƒâ€˜Ã†â€™
			// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã…Â½
			// Ãƒï¿½Ã‚Â¸
			// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
			// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Âº
			// Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â»Ãƒâ€˜Ã¯Â¿Â½
			// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â´Ãƒâ€˜Ã†â€™Ãƒâ€˜Ã…Â½Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¹
			// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¼Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â¸
			// Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒï¿½Ã‚Âº
			// Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Âµ
			for (int i = 0; i < backupMessages.size(); i++) {
				if (backupMessages.get(i).isChecked()) {
					// Log.i("LOG:::", i + " is Checked. AND ID = " +
					// backupMessages.get(i).getMessageId());
					markingMessages.add(backupMessages.get(i).getMessageId());
					// Ãƒï¿½Ã…Â¸Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¼Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
					// Ãƒï¿½Ã‚Â²
					// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Âµ
					// Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Âµ
					// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Âµ
					// Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒï¿½Ã‚Âº
					// Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Âµ
					backupMessages.get(i).setRead(true);
				}
			}

			// Ãƒï¿½Ã…Â¸Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¼Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
			// Ãƒï¿½Ã‚Â²Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Âµ
			// Ãƒï¿½Ã‚Â²Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â½Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Âµ
			// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã¢â‚¬Â¹
			// Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒï¿½Ã‚Âº
			// Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â½Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Âµ
			// Ãƒï¿½Ã‚Â²
			// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Â¡Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¼
			// Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Âµ
			// replace RichPushInbox
			UAirship.shared().getRichPushManager().getRichPushInbox()
					.markMessagesRead(markingMessages);

			// Ãƒï¿½Ã¢â‚¬â€�Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¼Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼,
			// Ãƒâ€˜Ã¢â‚¬Â¡Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾
			// Ãƒï¿½Ã‚Â¼Ãƒâ€˜Ã¢â‚¬Â¹
			// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¼Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
			// Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒï¿½Ã‚Âº
			// Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Âµ.
			// Ãƒï¿½Ã…Â¸Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¼
			// Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Å¡
			// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Âº
			// Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã†â€™Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â‚¬Å¡
			// Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¯Â¿Â½
			// Ãƒâ€˜Ã¯Â¿Â½
			// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â»Ãƒâ€˜Ã†â€™Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â½Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â¼
			// Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¸
			// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¸
			// Ãƒï¿½Ã¢â‚¬Â¢Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
			// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â½ Ãƒï¿½Ã‚Â¸
			// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â»Ãƒâ€˜Ã†â€™Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â½Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â¹
			// Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã†â€™Ãƒï¿½Ã‚Â´Ãƒâ€˜Ã†â€™Ãƒâ€˜Ã¢â‚¬Å¡
			// Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â·Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â°Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¯Â¿Â½,
			// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾
			// Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â·Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¹Ãƒï¿½Ã‚Â´Ãƒâ€˜Ã¢â‚¬ËœÃƒâ€˜Ã¢â‚¬Å¡
			// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Âµ
			// Ãƒï¿½Ã‹Å“
			// Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Å¡
			// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¯Â¿Â½
			afterEditRichPushes = backupMessages;

			// Ãƒï¿½Ã…Â¸Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â½Ãƒâ€˜Ã†â€™Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â»Ãƒâ€˜Ã…â€™Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾
			// Ãƒï¿½Ã‚Â·Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â°Ãƒâ€˜Ã‹â€ Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
			// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Âµ
			// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
			UAirship.shared().getRichPushManager().refreshMessages();

			markingMessages.clear();
			backupMessages.clear();

			dialog.dismiss();
		}
	}

	@Override
	protected void onPause() {
		UAirship.shared().getRichPushManager().removeListener(this);
		super.onPause();
	}

	/*
	 * @Click(R.id.iBtnMessagesBack) protected void onIbtnMessagesBackClicked()
	 * { onBackPressed(); }
	 */

	/*
	 * @Click(R.id.tvMessagesTitle) public void onTvMessagesClick() {
	 * onBackPressed(); }
	 */

	@CheckedChange(R.id.tBtnEditMode)
	public void onTbtnEditModeCheckedChanged(CompoundButton compoundButton,
			boolean isChecked) {
		messages = convertRichPushToCustom(UAirship.shared()
				.getRichPushManager().getRichPushInbox().getMessages());

		if (isChecked) {
			if (messageAdapter != null) {
				messageAdapter.clear();
			}
			checkedCount = 0;

			messageAdapter = new MessagesListAdapter(this,
					R.layout.message_item);

			listViewMessages.setAdapter(messageAdapter);

			messageAdapter.addAll(messages);
			editLayout.setVisibility(View.VISIBLE);

			deleteMessagesButton.setText(Localization.getMessagesBtnDelete());
			readMessagesButton.setText(Localization.getMessagesBtnRead());

			deleteMessagesButton.setEnabled(false);
			readMessagesButton.setEnabled(false);
		} else {
			if (messageNoCheckboxAdapter != null) {
				messageNoCheckboxAdapter.clear();
			}
			checkedCount = 0;

			messageNoCheckboxAdapter = new MessagesNoCheckboxListAdapter(this,
					R.layout.message_item_no_checkboxes);

			listViewMessages.setAdapter(messageNoCheckboxAdapter);

			messageNoCheckboxAdapter.addAll(messages);
			editLayout.setVisibility(View.GONE);

			deleteMessagesButton.setEnabled(false);
			readMessagesButton.setEnabled(false);
		}
	}

	/**
	 * Ãƒï¿½Ã‚Â¤Ãƒâ€˜Ã†â€™Ãƒï¿½Ã‚Â½Ãƒï¿½
	 * Ã‚�
	 * �ºÃƒâ€˜Ã¢â‚¬Â Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
	 * Ã�
	 * �ï¿½Ã‚ÂºÃƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â
	 * ²Ã�
	 * �ï¿½Ã‚ÂµÃƒâ€˜Ã¢â€šÂ¬Ãƒâ€˜Ã¢â‚¬Å¡Ã�
	 * �ï
	 * ¿½Ã‚Â¸Ãƒâ€˜Ã¢â€šÂ¬Ãƒâ€˜Ã†â€™Ãƒï¿
	 * ½Ã‚ÂµÃƒâ€˜Ã¢â‚¬Å¡
	 * Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚�
	 * �¿Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã�
	 * �Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Âº
	 * RichPush-Ãƒâ€˜Ã¯Â
	 * ¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒ
	 * ï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚�
	 * �Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚�
	 * �½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹ Ãƒï¿½Ã‚Â²
	 * Ãƒâ€˜Ã�
	 * �Â¿Â½Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã�
	 * �Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Âº
	 * CustomRichPush-Ãƒâ€˜Ã�
	 * �Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â
	 * ¾Ãƒï¿½Ã‚Â±Ãƒâ�
	 * �˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿
	 * ½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
	 * 
	 * @param richPushMessages
	 *            Ãƒï¿½Ã‹Å“Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€
	 *            ˜Ã¢â‚�
	 *            �Â¦Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â´Ãƒï¿
	 *            ½Ã‚Â½Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â¹
	 *            Ãƒâ�
	 *            ��˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¸Ã�
	 *            �â€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Âº
	 *            RichPush
	 *            -Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚�
	 *            �¾Ã�
	 *            �ï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚�
	 *            �µÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
	 * @return 
	 *         Ãƒï¿½Ã¢â‚¬â„¢Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â·
	 *         Ã�
	 *         �ï¿½Ã‚Â²Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â°Ã�
	 *         �â�
	 *         ��˜Ã¢â‚¬Â°Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒâ�
	 *         ��˜Ã¢â‚¬Å¡
	 *         Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¿Ãƒ
	 *         ï¿½Ã‚Â¸Ãƒâ�
	 *         ��˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Âº
	 *         CustomRichPush
	 *         -Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿�
	 *         �Ã‚Â¾Ãƒ
	 *         ï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚�
	 *         �µÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹,
	 *         Ãƒ
	 *         ï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â»Ãƒâ€
	 *         ˜Ã�
	 *         �â€™Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â¸Ãƒï
	 *         ¿½Ã‚�
	 *         �²Ãƒâ€˜Ã‹â€ Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚�
	 *         �¹Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¯Â¿Â½
	 *         Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â·
	 *         Ãƒï¿½Ã‚Â¸Ãƒâ�
	 *         �˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Â
	 *         ¦Ãƒï¿½Ã‚Â¾Ãƒï
	 *         ¿½Ã‚Â´Ãƒï¿½Ã‚Â½Ãƒï�
	 *         �½Ã‚Â¾Ãƒï¿½Ã‚Â³Ãƒï¿½Ã‚Â¾
	 *         Ãƒâ€˜Ã¯
	 *         Â¿Â½Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¸Ãƒâ�
	 *         ��˜Ã¯Â¿Â½Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°
	 *         RichPush-Ã�
	 *         �â€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚�
	 *         �¾Ãƒï¿
	 *         ½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃ�
	 *         �ï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
	 */
	private List<CustomRichPushMessage> convertRichPushToCustom(
			final List<RichPushMessage> richPushMessages) {
		List<CustomRichPushMessage> messageList = new ArrayList<CustomRichPushMessage>();

		for (RichPushMessage rp : richPushMessages) {
			messageList.add(new CustomRichPushMessage(rp));
		}

		return messageList;
	}

	/**
	 * Ãƒï¿½Ã‚Â¤Ãƒâ€˜Ã†â€™Ãƒï¿½Ã‚Â½Ãƒï¿½
	 * Ã‚�
	 * �ºÃƒâ€˜Ã¢â‚¬Â Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
	 * Ã�
	 * �â€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â°Ãƒï
	 * ¿�
	 * �Ã‚Â²Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â²Ãƒ
	 * ï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â‚¬Å¡
	 * Ãƒâ€˜Ã�
	 * �Â¿Â½Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã�
	 * �Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Âº
	 * RichPush-Ãƒâ€˜Ã¯Â
	 * ¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒ
	 * ï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚�
	 * �Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚�
	 * �½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
	 * Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾
	 * Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿
	 * ½Ã‚Â¿Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã�
	 * �Â¿Â½Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¼
	 * CustomRichPush
	 * -Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â
	 * ¾Ãƒï¿½Ã‚�
	 * �±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿
	 * ½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
	 * 
	 * @param richPushList
	 *            Ãƒï¿½Ã‚Â¡Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â
	 *            °Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ã�
	 *            �ï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿
	 *            ½Ã‚Â¼Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â¹
	 *            Ãƒâ�
	 *            ��˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¸Ã�
	 *            �â€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Âº
	 *            RichPush
	 *            -Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚�
	 *            �¾Ã�
	 *            �ï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚�
	 *            �µÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
	 * @param customRichPushList
	 *            Ãƒï¿½Ã‚Â¡Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â
	 *            °Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ã�
	 *            �ï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿
	 *            ½Ã‚Â¼Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â¹
	 *            Ãƒâ�
	 *            ��˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¸Ã�
	 *            �â€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Âº
	 *            CustomRichPush
	 *            -Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï
	 *            ¿½Ã‚Â¾Ã�
	 *            �ï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒ�
	 *            �¿½Ã‚ÂµÃ�
	 *            �ï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
	 * @return 
	 *         Ãƒï¿½Ã¢â‚¬â„¢Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â·
	 *         Ã�
	 *         �ï¿½Ã‚Â²Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â°Ã�
	 *         �â�
	 *         ��˜Ã¢â‚¬Â°Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒâ�
	 *         ��˜Ã¢â‚¬Å¡
	 *         Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚ÂµÃ�
	 *         �ï¿½Ã‚Â·Ã�
	 *         �â€˜Ã†â€™Ãƒï¿½Ã‚Â»Ãƒâ�
	 *         ��˜Ã…â€™Ã�
	 *         �â€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â°Ãƒ
	 *         â€˜Ã¢â‚¬Å¡
	 *         Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â€
	 *         šÂ¬Ãƒï¿½Ã‚Â
	 *         °Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â½Ã�
	 *         �ï¿½Ã‚ÂµÃƒï�
	 *         �½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
	 *         Ãƒâ€˜Ã�
	 *         �Â¿Â½Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¸Ãƒâ�
	 *         ��˜Ã¯Â¿
	 *         Â½Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²
	 */
	private boolean isEqualsRichPushes(
			final List<RichPushMessage> richPushList,
			final List<CustomRichPushMessage> customRichPushList) {
		// Ãƒï¿½Ã¢â‚¬Â¢Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
		// Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â·Ãƒï¿½Ã‚Â¼Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â€šÂ¬
		// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²
		// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â½
		// Ãƒï¿½Ã‚Â¸
		// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Å¡
		// Ãƒï¿½Ã‚Â¶Ãƒï¿½Ã‚Âµ,
		// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾
		// Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â°Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
		// Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™
		if (richPushList != null && customRichPushList != null) {
			if (richPushList.size() == customRichPushList.size()) {
				for (int i = 0; i < richPushList.size(); i++) {
					if (!richPushList.get(i).equals(
							customRichPushList.get(i).getRichPushMessage())) {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public void onUpdateMessages(boolean isSuccessful) {
		NotificationManager nManager = (NotificationManager) getApplicationContext()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nManager.cancelAll();

		// Ãƒï¿½Ã…Â¸Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â»Ãƒâ€˜Ã†â€™Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
		// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â»Ãƒâ€˜Ã¢â‚¬ËœÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â½Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â¹
		// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Âº
		// RichPush'Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¹
		List<RichPushMessage> updatedRichPushes = UAirship.shared()
				.getRichPushManager().getRichPushInbox().getMessages();

		// Ãƒï¿½Ã¢â‚¬Â¢Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
		// Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã‹â€ Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¾
		// RichPush-Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
		// Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â»Ãƒâ€˜Ã…â€™Ãƒâ€˜Ã‹â€ Ãƒï¿½Ã‚Âµ/Ãƒï¿½Ã‚Â¼Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒâ€˜Ã…â€™Ãƒâ€˜Ã‹â€ Ãƒï¿½Ã‚Âµ,
		// Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
		// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¹Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â°Ãƒâ€˜Ã¯Â¿Â½,
		// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾
		// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â»Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¯Â¿Â½
		if (updatedRichPushes.size() != listViewMessages.getCount()) {
			if (updatedRichPushes.size() != 0) {
				messages = convertRichPushToCustom(updatedRichPushes);

				if (editModeButton.isChecked()) {
					if (messageAdapter != null) {
						messageAdapter.clear();
					}
					checkedCount = 0;

					messageAdapter = new MessagesListAdapter(this,
							R.layout.message_item);

					listViewMessages.setAdapter(messageAdapter);

					messageAdapter.addAll(messages);
					editLayout.setVisibility(View.VISIBLE);

					deleteMessagesButton.setText(Localization
							.getMessagesBtnDelete());
					readMessagesButton.setText(Localization
							.getMessagesBtnRead());

					deleteMessagesButton.setEnabled(false);
					readMessagesButton.setEnabled(false);
				} else {
					if (messageNoCheckboxAdapter != null) {
						messageNoCheckboxAdapter.clear();
					}

					checkedCount = 0;

					messageNoCheckboxAdapter = new MessagesNoCheckboxListAdapter(
							this, R.layout.message_item_no_checkboxes);

					messages = convertRichPushToCustom(UAirship.shared()
							.getRichPushManager().getRichPushInbox()
							.getMessages());
					messageNoCheckboxAdapter.addAll(messages);

					listViewMessages.setAdapter(messageNoCheckboxAdapter);

					editLayout.setVisibility(View.GONE);

					deleteMessagesButton.setEnabled(false);
					readMessagesButton.setEnabled(false);
				}

				// Ãƒï¿½Ã‚Â¢.Ãƒï¿½Ã‚Âº.
				// Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Âµ
				// Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
				// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚ÂºÃƒâ€˜Ã†â€™Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Âµ
				// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
				// Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™,
				// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾
				// Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
				// Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â€šÂ¬
				// Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã†â€™Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
				// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
				noMessagesLayout.setVisibility(View.GONE);
				editModeButton.setEnabled(true);
			} else {
				// Ãƒï¿½Ã¢â‚¬Â¢Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
				// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
				// Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â‚¬Å¡,
				// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾
				// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â·Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
				// Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â€šÂ¬
				noMessagesLayout.setVisibility(View.VISIBLE);
				editModeButton.setEnabled(false);

				// Ãƒï¿½Ã¯Â¿Â½Ãƒï¿½Ã‚Âµ
				// Ãƒï¿½Ã‚Â·Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
				// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â»Ãƒâ€˜Ã…Â½Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™
				// Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¶Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¼
				// Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂºÃƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
				editModeButton.setChecked(false);
			}
		} else { // Ãƒï¿½Ã¢â‚¬Â¢Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
					// Ãƒï¿½Ã‚Â¶Ãƒï¿½Ã‚Âµ
					// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²
					// Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â»Ãƒâ€˜Ã…â€™Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â¾
					// Ãƒï¿½Ã‚Â¶Ãƒï¿½Ã‚Âµ,
					// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â»Ãƒâ€˜Ã…â€™Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â¾
					// Ãƒï¿½Ã‚Â¸
					// Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¾
			// Ãƒï¿½Ã¢â‚¬Â¢Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
			// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â»Ãƒâ€˜Ã†â€™Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â½Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Âµ
			// RichPush'Ãƒï¿½Ã‚Â¸
			// Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â°Ãƒâ€˜Ã…Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¯Â¿Â½
			// Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Å¡
			// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â‚¬Â¦,
			// Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â€šÂ¬Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Âµ
			// Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂºÃƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã…â€™
			// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾
			// Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â»Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¯Â¿Â½
			if (!isEqualsRichPushes(updatedRichPushes, afterEditRichPushes)) {
				messages = convertRichPushToCustom(updatedRichPushes);

				if (editModeButton.isChecked()) {
					if (messageAdapter != null) {
						messageAdapter.clear();
					}
					checkedCount = 0;

					messageAdapter = new MessagesListAdapter(this,
							R.layout.message_item);

					listViewMessages.setAdapter(messageAdapter);

					messageAdapter.addAll(messages);
					editLayout.setVisibility(View.VISIBLE);

					deleteMessagesButton.setText(Localization
							.getMessagesBtnDelete());
					readMessagesButton.setText(Localization
							.getMessagesBtnRead());

					deleteMessagesButton.setEnabled(false);
					readMessagesButton.setEnabled(false);
				} else {
					if (messageNoCheckboxAdapter != null) {
						messageNoCheckboxAdapter.clear();
					}

					checkedCount = 0;

					messageNoCheckboxAdapter = new MessagesNoCheckboxListAdapter(
							this, R.layout.message_item_no_checkboxes);

					listViewMessages.setAdapter(messageNoCheckboxAdapter);

					messageNoCheckboxAdapter.addAll(messages);
					editLayout.setVisibility(View.GONE);

					deleteMessagesButton.setEnabled(false);
					readMessagesButton.setEnabled(false);
				}

				// Ãƒï¿½Ã‚Â¢.Ãƒï¿½Ã‚Âº.
				// Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Âµ
				// Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
				// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚ÂºÃƒâ€˜Ã†â€™Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Âµ
				// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
				// Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™,
				// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾
				// Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
				// Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â€šÂ¬
				// Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã†â€™Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
				// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
				noMessagesLayout.setVisibility(View.GONE);
				editModeButton.setEnabled(true);

				afterEditRichPushes = convertRichPushToCustom(updatedRichPushes);
			} else {
				if (updatedRichPushes.size() == 0) {
					// Ãƒï¿½Ã¢â‚¬Â¢Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
					// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
					// Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â‚¬Å¡,
					// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾
					// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â·Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
					// Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â€šÂ¬
					noMessagesLayout.setVisibility(View.VISIBLE);
					editModeButton.setEnabled(false);

					// Ãƒï¿½Ã¯Â¿Â½Ãƒï¿½Ã‚Âµ
					// Ãƒï¿½Ã‚Â·Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
					// Ãƒï¿½Ã‚Â¿Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â»Ãƒâ€˜Ã…Â½Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™
					// Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¶Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¼
					// Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚ÂºÃƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¢â€šÂ¬Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â°Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
					editModeButton.setChecked(false);
				} else {
					// Ãƒï¿½Ã‚Â¢.Ãƒï¿½Ã‚Âº.
					// Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â²Ãƒâ€˜Ã¢â‚¬Â¹Ãƒï¿½Ã‚Âµ
					// Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â»Ãƒï¿½Ã‚Â¸
					// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚ÂºÃƒâ€˜Ã†â€™Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Âµ
					// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
					// Ãƒï¿½Ã‚ÂµÃƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã…â€™,
					// Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾
					// Ãƒï¿½Ã‚Â¿Ãƒâ€˜Ã¢â€šÂ¬Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Â¡Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â¼
					// Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â´Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚ÂºÃƒï¿½Ã‚Â°Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â€šÂ¬
					// Ãƒï¿½Ã‚Â¾Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã†â€™Ãƒâ€˜Ã¢â‚¬Å¡Ãƒâ€˜Ã¯Â¿Â½Ãƒâ€˜Ã¢â‚¬Å¡Ãƒï¿½Ã‚Â²Ãƒï¿½Ã‚Â¸Ãƒâ€˜Ã¯Â¿Â½
					// Ãƒâ€˜Ã¯Â¿Â½Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â¾Ãƒï¿½Ã‚Â±Ãƒâ€˜Ã¢â‚¬Â°Ãƒï¿½Ã‚ÂµÃƒï¿½Ã‚Â½Ãƒï¿½Ã‚Â¸Ãƒï¿½Ã‚Â¹
					noMessagesLayout.setVisibility(View.GONE);
					editModeButton.setEnabled(true);
				}
			}
		}
		dialog.dismiss();
		// replace RichPushInbox
		Utils.setLastUnreadCount(UAirship.shared().getRichPushManager()
				.getRichPushInbox().getUnreadCount());
	}

	@Override
	public void onUpdateUser(boolean b) {
	}

	// replace ua
	/*
	 * @Override public void onRetrieveMessage(boolean b, String s) { }
	 */
}
