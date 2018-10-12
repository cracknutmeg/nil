/**
 *@ TabBarFragment
 */
package com.allpoint.activities.tablet.fragments;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.allpoint.R;
import com.allpoint.activities.tablet.MainMenuActivity;
import com.allpoint.util.Constant;
import com.allpoint.util.Localization;
import com.allpoint.util.Settings;
import com.allpoint.util.Utils;
import com.allpoint.util.adapters.ResultListAdapter;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushManager;

/**
 * TabBarFragment
 * 
 * @author: Vyacheslav Shmakin
 * @version: 08.01.14
 */
@EFragment(R.layout.bottom_bar_fragment)
public class TabBarFragment extends Fragment implements
		SettingsFragment.SettingsChangeListener, RichPushManager.Listener {

	@ViewById(R.id.layoutBarMessageCount)
	RelativeLayout messageCountLayout;

	@ViewById(R.id.tvBarNumberOfMessages)
	TextView numberOfMessagesText;

	@ViewById(R.id.iBtnBottomMore)
	ImageButton moreButton;

	@ViewById(R.id.iTxtBottomMore)
	TextView moreButtonText;

	@ViewById(R.id.iTxtBottomHome)
	TextView tTxtBottomHome;

	@ViewById(R.id.iTxtBottomSearch)
	TextView itxtBottomSearch;

	@ViewById(R.id.iTxtBottomTransaction)
	TextView itxtBottomTransaction;

	@ViewById(R.id.iTxtBottomMessages)
	TextView itxtBottomMessages;

	@ViewById(R.id.iTxtBottomMore)
	TextView itxtBottomMore;

	private SettingsFragment settingsFragment;
	
	@ViewById(R.id.iBtnBottomTransaction)
	ImageButton iBtnBottomTransaction;

	@Override
	public void onResume() {
		super.onResume();
		Utils.showMessageCounter(messageCountLayout, numberOfMessagesText);

		UAirship.shared().getRichPushManager().addListener(this);

		// tTxtBottomHome.setText(Localization.getMainMenuTabHome());
		// itxtBottomSearch.setText(Localization.getMainMenuSearchTitle());
		// itxtBottomTransaction.setText(Localization.getMainMenuTransTitle());
		// itxtBottomMessages.setText(Localization.getMainMenuMessagesTitle());
		// itxtBottomMore.setText(Localization.getMainMenuMessagesTitle());
		
		//History Change
				if(Constant.HISTORY_BUTTON_DISABLE){
					itxtBottomTransaction.setText(Localization.getMainMenuAboutTitle());
					//set bottom bar image
					//iBtnBottomTransaction.setImageResource(R.drawable.bottom_about);
					
					// Manually set tab about drawable into xml
					
					//More Button Disable 
					itxtBottomMore.setText(Localization.getMainMenuSettingsTitle());
					
				} else {
					itxtBottomTransaction.setText(Localization.getMainMenuTransTitle());
					//set bottom bar image
					//iBtnBottomTransaction.setImageResource(R.drawable.bottom_history);
					
					//More Button Enable
					itxtBottomMore.setText(Localization.getMainMenuTabMore());
				}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (settingsFragment == null) {
			settingsFragment = new SettingsFragment();
			settingsFragment.addSettingsListener(this);
		}
	}

	@Override
	public void onStop() {
		settingsFragment.removeSettingsListener(this);
		super.onStop();
	}

	@Override
	public void onPause() {
		UAirship.shared().getRichPushManager().removeListener(this);
		super.onPause();
	}

	@Click(R.id.iBtnBottomTransaction)
	void onIbtnBottomTransactionClicked() {

		
		
		//History Change
				if(Constant.HISTORY_BUTTON_DISABLE){
					
					Utils.startActivity(this.getActivity(),
							com.allpoint.activities.tablet.AboutActivity_.class, false,
							false, false);
					
					
				} else {
				
					Utils.startActivity(this.getActivity(),
							com.allpoint.activities.tablet.CardListActivity_.class, false,
							false, false);
				}
	}

	@Click(R.id.iBtnBottomMessages)
	void onIbtnBottomMessagesClicked() {

		Utils.startActivity(this.getActivity(),
				com.allpoint.activities.tablet.MessageActivity_.class, false,
				false, !Utils.isOnMainActivity());
	}

	@Click(R.id.iBtnBottomSearch)
	void onIbtnBottomSearchClicked() {
		Utils.startActivity(this.getActivity(),
				com.allpoint.activities.tablet.MainActivity_.class, false,
				false, !Utils.isOnMainActivity());
	}

	@Click(R.id.iBtnBottomHome)
	void onIbtnBottomHomeClicked() {
		Utils.startActivity(this.getActivity(),
				com.allpoint.activities.tablet.MainMenuActivity_.class, false,
				false, false);
	}

	@Click(R.id.iBtnBottomMore)
	void onIbtnBottomSettingsClicked() {

		if(Constant.HISTORY_BUTTON_DISABLE){
			showSettingFrag();
		} else {
			showPopUpMenu();
		}
		
		
	}

	private void showPopUpMenu() {
		// TODO Auto-generated method stub
		PopupMenu popup = new PopupMenu(getActivity(), moreButton);
		popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {

				if (item.getTitle().equals("About")) {
					Utils.startActivity(
							getActivity(),
							com.allpoint.activities.tablet.AboutActivity_.class,
							false, false, false);
				} else {
					showSettingFrag();
				}

				return true;
			}
		});

		popup.show();// showing popup menu
	}

	void showSettingFrag() {
		moreButton.setImageResource(R.drawable.bottom_settings_press);
		moreButtonText.setTextColor(getResources().getColor(R.color.textColor));
		moreButton.setEnabled(true);
		settingsFragment.setTargetFragment(this, 0);
		settingsFragment.show(this.getFragmentManager(),
				Constant.SETTINGS_DIALOG_TAG);
	}

	@Override
	public void onUpdateMessages(boolean isSuccess) {
		Utils.showMessageCounter(messageCountLayout, numberOfMessagesText);
	}

	@Override
	public void onUpdateUser(boolean b) {
	}

	@Override
	public void onSettingsChanged() {

		tTxtBottomHome.setText(Localization.getMainMenuTabHome());
		itxtBottomSearch.setText(Localization.getMainMenuSearchTitle());
		itxtBottomTransaction.setText(Localization.getMainMenuTransTitle());
		itxtBottomMessages.setText(Localization.getMainMenuMessagesTitle());
		
		if(Constant.HISTORY_BUTTON_DISABLE){
			itxtBottomMore.setText(Localization.getMainMenuSettingsTitle());
		} else {
			itxtBottomMore.setText(Localization.getMainMenuMessagesTitle());	
		}
		
		// Different Activities have different text fields
		switch (Settings.getCurrentActivity()) {
		case AboutActivity: {
			// ((TextView)
			// this.getActivity().findViewById(R.id.tvAboutTitle)).setText(Localization.getAboutLayoutTitle());
			break;
		}
		case MainActivity: {
			if ((Utils.getInfoWindowMarker() != null)
					&& (Utils.getInfoWindowRecord() != null)) {
				Utils.getInfoWindowMarker().hideInfoWindow();
				Utils.getInfoWindowMarker().setSnippet(
						Utils.getDistanceString(Utils.getInfoWindowRecord()
								.getDistance()));
				Utils.getInfoWindowMarker().showInfoWindow();

				ResultListAdapter listAdapter = (ResultListAdapter) ((ListView) this
						.getActivity().findViewById(R.id.resultListView))
						.getAdapter();
				listAdapter.clear();
				listAdapter.addAll(Utils.getLocationList());
			}
			break;
		}
		case MessageActivity: {
			((TextView) this.getActivity().findViewById(R.id.tvMessagesTitle))
					.setText(Localization.getMessagesLayoutTitle());
			((TextView) this.getActivity().findViewById(R.id.tvNoMessages))
					.setText(Localization.getMessagesNoMessagesText());
			TextView tvDeleteText = (TextView) this.getActivity().findViewById(
					R.id.btnDeleteMessages);
			TextView tvReadText = (TextView) this.getActivity().findViewById(
					R.id.btnReadMessages);
			if (Utils.getCheckedMessagesCount() != 0) {
				tvReadText.setText(Localization.getMessagesBtnRead() + " ("
						+ Utils.getCheckedMessagesCount() + ")");
				tvDeleteText.setText(Localization.getMessagesBtnDelete() + " ("
						+ Utils.getCheckedMessagesCount() + ")");
			} else {
				tvReadText.setText(Localization.getMessagesBtnRead());
				tvDeleteText.setText(Localization.getMessagesBtnDelete());
			}

			ToggleButton editModeButton = (ToggleButton) this.getActivity()
					.findViewById(R.id.tBtnEditMode);

			if (!editModeButton.isChecked()) {
				editModeButton.setText(Localization.getMessagesEditTextOff());
			} else {
				editModeButton.setText(Localization.getMessagesEditTextOn());
			}

			editModeButton.setTextOff(Localization.getMessagesEditTextOff());
			editModeButton.setTextOn(Localization.getMessagesEditTextOn());
			break;
		}
		case PointDetailsActivity: {
			((TextView) this.getActivity().findViewById(R.id.tvDetailViewTitle))
					.setText(Localization.getDetailViewLayoutTitle());
			((TextView) this.getActivity().findViewById(R.id.tvServicesTitle))
					.setText(Localization.getDetailViewServicesTitle());
			((TextView) this.getActivity().findViewById(
					R.id.tvDetailsPointDistance)).setText(Utils
					.getDistanceString(Utils.getRecord().getDistance()));
			break;
		}
		default:
			break;
		}
	}

	public void onSettingsShowed() {

		switch (Settings.getCurrentActivity()) {
		case AboutActivity: {
			((ImageButton) this.getActivity().findViewById(R.id.iBtnBottomMore))
					.setImageResource(R.drawable.bottom_more_press);
			break;
		}
		case MainMenuActivity: {
			View rootView = ((Activity) MainMenuActivity.mContext).getWindow()
					.getDecorView().findViewById(android.R.id.content);
			View v = rootView.findViewById(R.id.iBtnBottomHome);
			View v1 = rootView.findViewById(R.id.iTxtBottomHome);
			((ImageButton) v).setImageResource(R.drawable.bottom_home);
			((TextView) v1).setTextColor(Color.WHITE);
			break;
		}
		case MainActivity: {
			((ImageButton) this.getActivity().findViewById(
					R.id.iBtnBottomSearch))
					.setImageResource(R.drawable.bottom_search);
			((TextView) this.getActivity().findViewById(R.id.iTxtBottomSearch))
					.setTextColor(getResources().getColor(R.color.white));
			break;
		}
		case MessageActivity: {
			((ImageButton) this.getActivity().findViewById(
					R.id.iBtnBottomMessages))
					.setImageResource(R.drawable.bottom_messages);
			((TextView) this.getActivity()
					.findViewById(R.id.iTxtBottomMessages))
					.setTextColor(getResources().getColor(R.color.white));
			break;
		}
		case PointDetailsActivity: {
			((ImageButton) this.getActivity().findViewById(
					R.id.iBtnBottomSearch))
					.setImageResource(R.drawable.bottom_search);
			((TextView) this.getActivity().findViewById(R.id.iTxtBottomSearch))
					.setTextColor(getResources().getColor(R.color.white));
			break;
		}
		case HistoryActivity: {
			((ImageButton) this.getActivity().findViewById(
					R.id.iBtnBottomTransaction))
					.setImageResource(R.drawable.bottom_history);
			((TextView) this.getActivity().findViewById(
					R.id.iTxtBottomTransaction)).setTextColor(getResources()
					.getColor(R.color.white));

			break;
		}
		default:
			break;
		}
	}

	@Override
	public void onSettingsDismissed() {
		Settings.SaveSettings();
		
		if(Constant.HISTORY_BUTTON_DISABLE) {
			moreButton.setImageResource(R.drawable.bottom_settings);
		} else {
			moreButton.setImageResource(R.drawable.bottom_more);	
		}
		
		
		moreButtonText.setTextColor(getResources().getColor(R.color.white));
		switch (Settings.getCurrentActivity()) {
		case AboutActivity: {
			((ImageButton) this.getActivity().findViewById(R.id.iBtnBottomMore))
					.setImageResource(R.drawable.bottom_more_press);
			((TextView) this.getActivity().findViewById(R.id.iTxtBottomMore))
					.setTextColor(getResources().getColor(R.color.textColor));
			break;
		}
		case MainMenuActivity: {
			((ImageButton) this.getActivity().findViewById(R.id.iBtnBottomHome))
					.setImageResource(R.drawable.bottom_home_press);
			((TextView) this.getActivity().findViewById(R.id.iTxtBottomHome))
					.setTextColor(getResources().getColor(R.color.textColor));
			break;
		}
		case MainActivity: {
			((ImageButton) this.getActivity().findViewById(
					R.id.iBtnBottomSearch))
					.setImageResource(R.drawable.bottom_search_press);
			((TextView) this.getActivity().findViewById(R.id.iTxtBottomSearch))
					.setTextColor(getResources().getColor(R.color.textColor));
			break;
		}
		case MessageActivity: {
			((ImageButton) this.getActivity().findViewById(
					R.id.iBtnBottomMessages))
					.setImageResource(R.drawable.bottom_messages_press);
			((TextView) this.getActivity()
					.findViewById(R.id.iTxtBottomMessages))
					.setTextColor(getResources().getColor(R.color.textColor));
			break;
		}
		case PointDetailsActivity: {
			((ImageButton) this.getActivity().findViewById(
					R.id.iBtnBottomSearch))
					.setImageResource(R.drawable.bottom_search_press);
			((TextView) this.getActivity().findViewById(R.id.iTxtBottomSearch))
					.setTextColor(getResources().getColor(R.color.textColor));
			break;
		}
		case HistoryActivity: {
			((ImageButton) this.getActivity().findViewById(
					R.id.iBtnBottomTransaction))
					.setImageResource(R.drawable.bottom_history_press);
			((TextView) this.getActivity().findViewById(
					R.id.iTxtBottomTransaction)).setTextColor(getResources()
					.getColor(R.color.textColor));
			break;
		}
		default:
			break;
		}

	}
}
