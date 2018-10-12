//
// DO NOT EDIT THIS FILE, IT HAS BEEN GENERATED USING AndroidAnnotations 3.0.1.
//


package com.allpoint.services;

import android.content.Context;
import android.net.ConnectivityManager;
import org.androidannotations.api.view.OnViewChangedNotifier;

public final class InternetConnectionManager_
    extends InternetConnectionManager
{

    private Context context_;
    private static InternetConnectionManager_ instance_;

    private InternetConnectionManager_(Context context) {
        context_ = context;
    }

    public static InternetConnectionManager_ getInstance_(Context context) {
        if (instance_ == null) {
            OnViewChangedNotifier previousNotifier = OnViewChangedNotifier.replaceNotifier(null);
            instance_ = new InternetConnectionManager_(context.getApplicationContext());
            instance_.init_();
            OnViewChangedNotifier.replaceNotifier(previousNotifier);
        }
        return instance_;
    }

    private void init_() {
        connectivityManager = ((ConnectivityManager) context_.getSystemService(Context.CONNECTIVITY_SERVICE));
    }

}