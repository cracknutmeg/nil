//
// DO NOT EDIT THIS FILE, IT HAS BEEN GENERATED USING AndroidAnnotations 3.0.1.
//


package com.allpoint.services;

import android.content.Context;
import android.net.ConnectivityManager;
import org.androidannotations.api.view.OnViewChangedNotifier;

public final class GPSConnectionManager_
    extends GPSConnectionManager
{

    private Context context_;
    private static GPSConnectionManager_ instance_;

    private GPSConnectionManager_(Context context) {
        context_ = context;
    }

    public static GPSConnectionManager_ getInstance_(Context context) {
        if (instance_ == null) {
            OnViewChangedNotifier previousNotifier = OnViewChangedNotifier.replaceNotifier(null);
            instance_ = new GPSConnectionManager_(context.getApplicationContext());
            instance_.init_();
            OnViewChangedNotifier.replaceNotifier(previousNotifier);
        }
        return instance_;
    }

    private void init_() {
        connectivityManager = ((ConnectivityManager) context_.getSystemService(Context.CONNECTIVITY_SERVICE));
    }

}
