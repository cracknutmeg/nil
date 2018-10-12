/**
 *@ MessageActivity
 */
package com.allpoint.activities.tablet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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

import com.allpoint.R;
import com.allpoint.activities.fragments.AlertDialogFragment;
import com.allpoint.activities.tablet.fragments.SettingsFragment;
import com.allpoint.model.CustomRichPushMessage;
import com.allpoint.util.Compute;
import com.allpoint.util.Constant;
import com.allpoint.util.Localization;
import com.allpoint.util.Settings;
import com.allpoint.util.Utils;
import com.allpoint.util.adapters.MessagesListAdapter;
import com.allpoint.util.adapters.MessagesNoCheckboxListAdapter;
import com.bugsense.trace.BugSenseHandler;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.widget.RichPushMessageWebView;

/**
 * MessageActivity
 * 
 * @author: Vyacheslav.Shmakin
 * @version: 08.01.14
 */
@EActivity(R.layout.messages)
public class MessageActivity extends FragmentActivity implements
		AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener,
		RichPushManager.Listener, SettingsFragment.SettingsChangeListener {

	@ViewById(R.id.layoutBarMessageCount)
	RelativeLayout messageCountLayout;

	@ViewById(R.id.messageViewLayout)
	RelativeLayout messageLayout;

	@ViewById(R.id.editLayout)
	LinearLayout editLayout;

	@ViewById(R.id.layoutNoMessages)
	ViewGroup noMessagesLayout;

	@ViewById(R.id.wViewRichPushView)
	RichPushMessageWebView messageView;

	@ViewById(R.id.listView)
	ListView listViewMessages;

	@ViewById(R.id.iBtnBottomMessages)
	ImageButton btnMessages;

	@ViewById(R.id.btnDeleteMessages)
	Button deleteMessagesButton;

	@ViewById(R.id.btnReadMessages)
	Button readMessagesButton;

	@ViewById(R.id.tBtnEditMode)
	ToggleButton editModeButton;

	@ViewById(R.id.tvMessagesTitle)
	TextView messagesTitle;

	@ViewById(R.id.tvNoMessages)
	TextView noMessagesText;

	@ViewById(R.id.tvBarNumberOfMessages)
	TextView numberOfMessagesText;

	@ViewById(R.id.tvMessageTitle)
	TextView messageTitle;

	@ViewById(R.id.tvMessageTime)
	TextView messageTime;

	@ViewById(R.id.iTxtBottomMessages)
	protected TextView tvMessage;

	AlertDialogFragment alertDialog;

	private NotificationManager nManager;
	private MessagesListAdapter messageAdapter;
	private MessagesNoCheckboxListAdapter messageNoCheckboxAdapter;
	private List<CustomRichPushMessage> messages = new ArrayList<CustomRichPushMessage>();
	private List<CustomRichPushMessage> backupMessages;
	private List<CustomRichPushMessage> afterEditRichPushes = new ArrayList<CustomRichPushMessage>();

	private ProgressDialog dialog;
	private String currentMessageId = "";
	private int checkedCount = 0;

	private List<CustomRichPushMessage> updateListView(
			final List<CustomRichPushMessage> updatedRichPushes) {
		checkedCount = 0;
		Utils.setCheckedMessagesCount(checkedCount);
		deleteMessagesButton.setEnabled(false);
		readMessagesButton.setEnabled(false);

		if (editModeButton.isChecked()) {
			loadCheckBoxMessages(updatedRichPushes);
			editLayout.setVisibility(View.VISIBLE);

			deleteMessagesButton.setText(Localization.getMessagesBtnDelete());
			readMessagesButton.setText(Localization.getMessagesBtnRead());
		} else {
			loadNoCheckBoxMessages(updatedRichPushes);
			editLayout.setVisibility(View.GONE);
		}
		Utils.showMessageCounter(messageCountLayout, numberOfMessagesText);
		updateLayout(updatedRichPushes.size());

		return updatedRichPushes;
	}

	private List<CustomRichPushMessage> setSelectedRichPush(
			List<CustomRichPushMessage> messageList, final String messageId) {
		CustomRichPushMessage tempMessage;

		for (CustomRichPushMessage aMessageList : messageList) {
			tempMessage = aMessageList;
			if (tempMessage.isSelected()) {
				if (!tempMessage.getMessageId().equals(messageId)) {
					aMessageList.setSelected(false);
				}
			} else if (tempMessage.getMessageId().equals(messageId)) {
				aMessageList.setSelected(true);
			}
		}

		return messageList;
	}

	private void loadNoCheckBoxMessages(
			List<CustomRichPushMessage> customRichPushMessages) {
		customRichPushMessages = setSelectedRichPush(customRichPushMessages,
				currentMessageId);

		if (messageNoCheckboxAdapter != null) {
			messageNoCheckboxAdapter.clear();
		}
		messageNoCheckboxAdapter = new MessagesNoCheckboxListAdapter(this,
				R.layout.message_item_no_checkboxes);
		listViewMessages.setAdapter(messageNoCheckboxAdapter);
		messageNoCheckboxAdapter.addAll(customRichPushMessages);
	}

	private void loadCheckBoxMessages(
			final List<CustomRichPushMessage> customRichPushMessages) {
		if (messageAdapter != null) {
			messageAdapter.clear();
		}
		messageAdapter = new MessagesListAdapter(this, R.layout.message_item);
		listViewMessages.setAdapter(messageAdapter);
		messageAdapter.addAll(customRichPushMessages);
	}

	private void updateLayout(final int numberOfMessages) {
		if (numberOfMessages == 0) {
			// If we have no any messages, then show "NO MESSAGES" image
			noMessagesLayout.setVisibility(View.VISIBLE);
			editModeButton.setEnabled(false);

			messageLayout.setVisibility(View.GONE);

			// Disable message edit mode
			editModeButton.setChecked(false);
		} else {
			// If we have any messages (new or old)
			// then hide "NO MESSAGES" image
			noMessagesLayout.setVisibility(View.GONE);
			editModeButton.setEnabled(true);

			if (editModeButton.isChecked()) {
				messageLayout.setVisibility(View.GONE);
			} else {
				messageLayout.setVisibility(View.VISIBLE);
			}
		}
	}

	private void showMessageFromPosition(
			final List<CustomRichPushMessage> allMessages, final int position,
			final boolean markAsRead) {

		// //replace RichPushInbox
		if (UAirship.shared().getRichPushManager().getRichPushInbox()
				.getMessages().size() > 0) {
			// save index and top position
			int index = listViewMessages.getFirstVisiblePosition();
			View v = listViewMessages.getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();

			currentMessageId = allMessages.get(position).getMessageId();

			RichPushMessage msg = allMessages.get(position)
					.getRichPushMessage();

			HashSet<String> markingMessages = new HashSet<String>();
			markingMessages.add(currentMessageId);

			if (markAsRead) {
				// Marking messages as read (at the inbox)
				UAirship.shared().getRichPushManager().getRichPushInbox()
						.markMessagesRead(markingMessages);
			}

			// Getting all messages from inbox after reading
			afterEditRichPushes = Compute.convertRichPushToCustom(UAirship
					.shared().getRichPushManager().getRichPushInbox()
					.getMessages());
			updateListView(afterEditRichPushes);

			markingMessages.clear();

			messageView.loadRichPushMessage(msg);
			messageTitle.setText(msg.getTitle());

			SimpleDateFormat dateFormat = new SimpleDateFormat(
					Constant.DATE_TIME_FORMAT);
			messageTime.setText(dateFormat.format(msg.getSentDate()));

			// restore
			listViewMessages.setSelectionFromTop(index, top);
		}
	}

	@AfterViews
	void afterViews() {
		BugSenseHandler.initAndStartSession(this, Constant.BUG_SENSE_API_KEY);
		Settings.LoadSettings();

		btnMessages.setImageResource(R.drawable.bottom_messages_press);
		tvMessage.setTextColor(getResources().getColor(R.color.textColor));
		btnMessages.setEnabled(false);

		messageView.getSettings().setJavaScriptEnabled(true);
		messageView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				dialog = ProgressDialog.show(MessageActivity.this,
						Localization.getDialogLoadingTitle(),
						Localization.getDialogPleaseWait(), true, false);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
			}
		});

		listViewMessages.setOnItemClickListener(this);
		listViewMessages.setOnItemLongClickListener(this);

		messages = Compute.convertRichPushToCustom(UAirship.shared()
				.getRichPushManager().getRichPushInbox().getMessages());
		afterEditRichPushes = new ArrayList<CustomRichPushMessage>(messages);
		showMessageFromPosition(messages, 0, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		Utils.startFlurry(this, Constant.MESSAGES_ACTIVITY_EVENT);
		Settings.setCurrentActivity(Settings.CurrentActivity.MessageActivity);
		UAirship.shared().getRichPushManager().addListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		messagesTitle.setText(Localization.getMessagesLayoutTitle());
		noMessagesText.setText(Localization.getMessagesNoMessagesText());

		if (Utils.getCheckedMessagesCount() != 0) {
			deleteMessagesButton.setText(Localization.getMessagesBtnRead()
					+ " (" + Utils.getCheckedMessagesCount() + ")");
			readMessagesButton.setText(Localization.getMessagesBtnDelete()
					+ " (" + Utils.getCheckedMessagesCount() + ")");
		} else {
			deleteMessagesButton.setText(Localization.getMessagesBtnDelete());
			readMessagesButton.setText(Localization.getMessagesBtnRead());
		}

		if (!editModeButton.isChecked()) {
			editModeButton.setText(Localization.getMessagesEditTextOff());
		} else {
			editModeButton.setText(Localization.getMessagesEditTextOn());
		}

		editModeButton.setTextOff(Localization.getMessagesEditTextOff());
		editModeButton.setTextOn(Localization.getMessagesEditTextOn());

		nManager = (NotificationManager) getApplicationContext()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nManager.cancelAll();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		CheckBox chBox = (CheckBox) view.findViewById(R.id.chBoxMessages);
		// If checkbox is exists then click = set item checked/unchecked
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

			// Changing counter values
			if (checkedCount == 0) {
				deleteMessagesButton.setText(Localization
						.getMessagesBtnDelete());
				readMessagesButton.setText(Localization.getMessagesBtnRead());
				deleteMessagesButton.setEnabled(false);
				readMessagesButton.setEnabled(false);
			} else {
				deleteMessagesButton.setText(Localization
						.getMessagesBtnDelete() + " (" + checkedCount + ")");
				readMessagesButton.setText(Localization.getMessagesBtnRead()
						+ " (" + checkedCount + ")");
				deleteMessagesButton.setEnabled(true);
				readMessagesButton.setEnabled(true);
			}
			currentMessageId = "";
			Utils.setCheckedMessagesCount(checkedCount);
		} else {
			showMessageFromPosition(messages, position, true);
			Utils.startFlurry(this, Constant.READ_MESSAGE_EVENT);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		if (messages != null && messages.size() != 0) {
			if (!messages.get(position).isRead()) {
				showMessageFromPosition(messages, position, true);

				Vibrator vibe = (Vibrator) this
						.getSystemService(Context.VIBRATOR_SERVICE);
				vibe.vibrate(Constant.VIBRATOR_DURATION);

				Toast.makeText(this, Localization.getDialogMessageMarked(),
						Toast.LENGTH_LONG).show();
			} else {
				showMessageFromPosition(messages, position, false);
			}

		}

		return true;
	}

	@Override
	protected void onDestroy() {
		UAirship.shared().getRichPushManager().removeListener(this);
		super.onDestroy();
	}

	@Click(R.id.btnDeleteMessages)
	void onBtnDeleteMessagesClicked() {
		if (messages != null && messages.size() != 0) {
			backupMessages = new ArrayList<CustomRichPushMessage>(messages);

			// Show confirm Alert message
			alertDialog = new AlertDialogFragment(
					Localization.getDialogRemoveMessages(),
					Localization.getDialogOk(),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							HashSet<String> deletingMessages = new HashSet<String>();
							for (CustomRichPushMessage backupMessage : backupMessages) {
								if (backupMessage.isChecked()) {
									deletingMessages.add(backupMessage
											.getMessageId());
								}
							}
							// Removing all marked messages from account
							UAirship.shared().getRichPushManager()
									.getRichPushInbox()
									.deleteMessages(deletingMessages);

							// Getting all messages from inbox after deleting
							afterEditRichPushes = Compute
									.convertRichPushToCustom(UAirship.shared()
											.getRichPushManager()
											.getRichPushInbox().getMessages());
							messages = updateListView(afterEditRichPushes);

							deletingMessages.clear();
							backupMessages.clear();
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
	void onBtnReadMessagesClicked() {
		if (messages != null && messages.size() != 0) {
			backupMessages = new ArrayList<CustomRichPushMessage>(messages);

			HashSet<String> markingMessages = new HashSet<String>();
			for (CustomRichPushMessage backupMessage : backupMessages) {
				if (backupMessage.isChecked()) {
					markingMessages.add(backupMessage.getMessageId());
				}
			}
			// Marking messages as read (at the inbox)
			UAirship.shared().getRichPushManager().getRichPushInbox()
					.markMessagesRead(markingMessages);

			// Getting all messages from inbox after reading
			afterEditRichPushes = Compute.convertRichPushToCustom(UAirship
					.shared().getRichPushManager().getRichPushInbox()
					.getMessages());
			updateListView(afterEditRichPushes);

			markingMessages.clear();
			backupMessages.clear();
		}
	}

	@CheckedChange(R.id.tBtnEditMode)
	void onTbtnEditModeCheckedChanged(CompoundButton compoundButton,
			boolean isChecked) {
		if (!isChecked) {
			if (UAirship.shared().getRichPushManager().getRichPushInbox()
					.getMessage(currentMessageId) == null) {
				showMessageFromPosition(messages, 0, false);
			}
		}
		messages = updateListView(Compute
				.convertRichPushToCustom(UAirship.shared().getRichPushManager()
						.getRichPushInbox().getMessages()));
	}

	@Override
	public void onUpdateMessages(boolean isSuccess) {
		nManager = (NotificationManager) getApplicationContext()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nManager.cancelAll();

		// replace
		// Receiving list of RichPush messages
		List<RichPushMessage> updatedRichPushes = UAirship.shared()
				.getRichPushManager().getRichPushInbox().getMessages();

		int beforeUpdateCount = listViewMessages.getCount();

		// If RichPush messages less/more than current messages in list, then
		// updating
		if (updatedRichPushes.size() != listViewMessages.getCount()) {
			afterEditRichPushes = updateListView(Compute
					.convertRichPushToCustom(updatedRichPushes));
			messages = afterEditRichPushes;
		} else {
			// If received list of RichPush messages,
			// differs from edited, then updating list
			if (!Compute.isEqualsRichPushes(updatedRichPushes,
					afterEditRichPushes)) {
				afterEditRichPushes = updateListView(Compute
						.convertRichPushToCustom(updatedRichPushes));
				messages = afterEditRichPushes;
			} else {
				updateLayout(updatedRichPushes.size());
			}
		}

		if (listViewMessages.getCount() > 0 && beforeUpdateCount == 0) {
			showMessageFromPosition(messages, 0, false);
		}

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