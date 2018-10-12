/**
 *@ TabBarFragment
 */
package com.allpoint.activities.phone.fragments;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.allpoint.AtmFinderApplication;
import com.allpoint.R;
import com.allpoint.activities.tablet.fragments.SettingsFragment;
import com.allpoint.util.Constant;
import com.allpoint.util.Localization;
import com.allpoint.util.Utils;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushManager;

/**
 * TabBarFragment
 * 
 * @author: Mikhail.Shalagin & Vyacheslav.Shmakin
 * @version: 23.09.13
 */
@EFragment(R.layout.bottom_bar_fragment)
public class TabBarFragment extends Fragment implements
		RichPushManager.Listener, SettingsFragment.SettingsChangeListener {

	@Bean
	protected Utils utils;

	@ViewById(R.id.layoutBarMessageCount)
	RelativeLayout messageCountLayout;

	@ViewById(R.id.tvBarNumberOfMessages)
	TextView numberOfMessagesText;

	@ViewById(R.id.iBtnBottomMore)
	ImageButton moreButton;

	@ViewById(R.id.tvMenuTrans)
	protected TextView mainMenuTrans;

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
	
	@ViewById(R.id.iBtnBottomTransaction)
	ImageButton iBtnBottomTransaction;
	
	AtmFinderApplication atmfinderappcontext;
	@Override
	public void onResume() {
		super.onResume();
		Utils.showMessageCounter(messageCountLayout, numberOfMessagesText);

		UAirship.shared().getRichPushManager().addListener(this);

		tTxtBottomHome.setText(Localization.getMainMenuTabHome());
		itxtBottomSearch.setText(Localization.getMainMenuSearchTitle());
		itxtBottomMessages.setText(Localization.getMainMenuMessagesTitle());
		
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
	public void onPause() {
		UAirship.shared().getRichPushManager().removeListener(this);
		super.onPause();
	}

	@Click(R.id.iBtnBottomTransaction)
	void onIbtnBottomTransactionClicked() {
		
		//History Change
		if(Constant.HISTORY_BUTTON_DISABLE){
			
//			startActivity(new Intent(this.getActivity(),
//					com.allpoint.activities.phone.AboutActivity_.class));
			
			Utils.startActivity(this.getActivity(),
					com.allpoint.activities.phone.AboutActivity_.class, false,
					false, false);
			
		} else {
		
			startActivity(new Intent(this.getActivity(),
					com.allpoint.activities.phone.CardListActivity_.class));
		}
		

	}

	@Click(R.id.iBtnBottomMessages)
	void onIbtnBottomMessagesClicked() {

		Utils.startActivity(this.getActivity(),
				com.allpoint.activities.phone.MessageActivity_.class, false,
				false, false);
	}

	@Click(R.id.iBtnBottomSearch)
	void onIbtnBottomSearchClicked() {
		Utils.startActivity(this.getActivity(),
				com.allpoint.activities.phone.MainActivity_.class, false,
				false, false);
	}

	@Click(R.id.iBtnBottomHome)
	void onIbtnBottomHomeClicked() {
		Utils.startActivity(this.getActivity(),
				com.allpoint.activities.phone.MainMenuActivity_.class, false,
				false, false);
	}

	
	
	
	@Click(R.id.iBtnBottomMore)
	void onIbtnBottomSettingsClicked() {
		
		if(Constant.HISTORY_BUTTON_DISABLE){
//			 Utils.startActivity(this.getActivity(),
//			 com.allpoint.activities.phone.SettingsActivity_.class, false, false,
//			 !Utils.isOnMainActivity());
			
			Utils.startActivity(this.getActivity(),
					com.allpoint.activities.phone.SettingsActivity_.class, false,
					false, false);
			
		} else {
			showPopUpMenu();
		}
		
		
		

	}

	private void showPopUpMenu() {
		// TODO Auto-generated method stub
				// Utils.startActivity(this.getActivity(),
				// com.allpoint.activities.phone.SettingsActivity_.class, false, false,
				// !Utils.isOnMainActivity());

				// Creating the instance of PopupMenu
				PopupMenu popup = new PopupMenu(getActivity(), moreButton);
				// Inflating the Popup using xml file
				
				if(Constant.HISTORY_BUTTON_DISABLE){
					popup.getMenuInflater().inflate(R.menu.popup_menu_about, popup.getMenu());
				} else {
					popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());	
				}
				

				// registering popup with OnMenuItemClickListener
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {

						if (item.getTitle().equals("About")) {
							Utils.startActivity(getActivity(),
									com.allpoint.activities.phone.AboutActivity_.class,
									false, false, false);
						} else {
							Utils.startActivity(
									getActivity(),
									com.allpoint.activities.phone.SettingsActivity_.class,
									false, false, false);
						}

						return true;
					}
				});

				popup.show();// showing popup menu	
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
		// TODO Auto-generated method stub
		tTxtBottomHome.setText(Localization.getMainMenuTabHome());
		itxtBottomSearch.setText(Localization.getMainMenuSearchTitle());
		itxtBottomTransaction.setText(Localization.getMainMenuTransTitle());
		itxtBottomMessages.setText(Localization.getMainMenuMessagesTitle());
		itxtBottomMore.setText(Localization.getMainMenuMessagesTitle());
	}

	@Override
	public void onSettingsShowed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSettingsDismissed() {
		// TODO Auto-generated method stub

	}

	public void changeText() {
		// this textview should be bound in the fragment onCreate as a member
		// variable
		tTxtBottomHome.setText(Localization.getMainMenuTabHome());
		itxtBottomSearch.setText(Localization.getMainMenuSearchTitle());
		itxtBottomTransaction.setText(Localization.getMainMenuTransTitle());
		itxtBottomMessages.setText(Localization.getMainMenuMessagesTitle());
		itxtBottomMore.setText(Localization.getMainMenuTabMore());
	}

	// replace ua
	/*
	 * @Override public void onRetrieveMessage(boolean b, String s) { }
	 */
}
