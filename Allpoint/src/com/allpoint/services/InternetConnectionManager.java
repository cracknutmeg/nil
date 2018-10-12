package com.allpoint.services;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * InternetConnectionManager
 * 
 * @author: Vyacheslav.Shmakin
 * @version: 08.01.14
 */
@EBean(scope = EBean.Scope.Singleton)
public class InternetConnectionManager {
	@SystemService
	ConnectivityManager connectivityManager;

	public boolean isConnected() {
		if (connectivityManager == null) {
			return false;
		}

		NetworkInfo[] allNetworks = connectivityManager.getAllNetworkInfo();
		for (NetworkInfo networkInfo : allNetworks) {
			if (networkInfo.isConnected()) {
				return true;
			}
		}
		return false;
	}
}
