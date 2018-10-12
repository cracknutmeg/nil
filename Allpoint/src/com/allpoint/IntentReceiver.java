/*
Copyright 2009-2014 Urban Airship Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.allpoint;

import android.content.Context;
import android.content.Intent;

import com.allpoint.activities.LoginActivity;
import com.allpoint.activities.phone.MessageActivity_;
import com.allpoint.util.Constant;
import com.allpoint.util.Utils;
import com.urbanairship.push.BaseIntentReceiver;
import com.urbanairship.push.PushMessage;

public class IntentReceiver extends BaseIntentReceiver {

	@Override
	protected void onChannelRegistrationSucceeded(Context context,
			String channelId) {
//		 Log.i("UA", "Channel registration updated. Channel Id:" + channelId +
//		 ".");
		
		LoginActivity.uaChannelID = channelId;
	}

	@Override
	protected void onChannelRegistrationFailed(Context context) {
		// Log.i(TAG, "Channel registration failed.");
	}

	@Override
	protected void onPushReceived(Context context, PushMessage message,
			int notificationId) {
		// Log.i(TAG, "Channel push notification. Alert: " + message.getAlert()
		// + ". Notification ID: " + notificationId);

	}

	@Override
	protected void onBackgroundPushReceived(Context context, PushMessage message) {
		// Log.i(TAG, "Channel Received background push message: " + message);
	}

	@Override
	protected boolean onNotificationOpened(Context context,
			PushMessage message, int notificationId) {
		// Log.i(TAG, "Channel User clicked notification. Alert: " +
		// message.getAlert());

		//Intent messageIntent = new Intent(context, MessageActivity_.class);
		//Changed after Sim request
		Intent messageIntent;
		boolean prod_build=LoginActivity.prod_build;
		if(prod_build==false)
		{
			messageIntent = new Intent(context, MessageActivity_.class);
		}
		
		
		if (Utils.isTablet()) {
			messageIntent = new Intent(context, com.allpoint.activities.tablet.MessageActivity_.class);
		  } else {
			messageIntent = new Intent(context, com.allpoint.activities.phone.MessageActivity_.class);
		}
		
		/*Intent messageIntent;
		if (Utils.isTablet()) {
			messageIntent = new Intent(context, com.allpoint.activities.tablet.MainMenuActivity_.class);
			
		} else {
			messageIntent = new Intent(context, com.allpoint.activities.phone.MainMenuActivity_.class);
		}*/

		String messageId = message.getRichPushMessageId();
		if (messageId != null && !messageId.isEmpty()) {
			// Logger.debug("Channel Notified of a notification opened with ID "
			// + messageId);

			// Launch the main activity to the message in the inbox
			messageIntent.putExtra(Constant.EXTRA_MESSAGE_ID, messageId);
			messageIntent.putExtra(Constant.EXTRA_NAVIGATE_ITEM,
					Constant.INBOX_ITEM);
		}

		messageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		context.startActivity(messageIntent);

		return true;
	}

	@Override
	protected boolean onNotificationActionOpened(Context context,
			PushMessage message, int notificationId, String buttonId,
			boolean isForeground) {
		// Log.i(TAG, "User clicked notification button. Button ID: " + buttonId
		// + " Alert: " + message.getAlert());

		return true;
	}

	@Override
	protected void onNotificationDismissed(Context context,
			PushMessage message, int notificationId) {
		// Log.i(TAG, "Notification dismissed. Alert: " + message.getAlert() +
		// ". Notification ID: " + notificationId);
	}
}
