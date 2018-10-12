/**
 *@ DetailsActionBarFragment
 */
package com.allpoint.activities.phone.fragments;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

import com.allpoint.R;
import com.allpoint.util.Constant;
import com.allpoint.util.Localization;
import com.allpoint.util.Utils;
import com.google.android.gms.maps.model.LatLng;

/**
 * DetailsActionBarFragment
 * 
 * @author: Mikhail.Shalagin & Vyacheslav.Shmakin
 * @version: 23.08.13
 */
@EFragment(R.layout.details_action_bar_fragment)
public class DetailsActionBarFragment extends Fragment {

	@Click(R.id.directionButton)
	void onDirectionButtonClicked() {
		LatLng currentPosition = Utils.getMyLocation();

		if (currentPosition == null || Utils.getPointPosition() == null) {

			AlertDialog.Builder builder = new AlertDialog.Builder(
					this.getActivity());
			builder.setMessage(Localization.getDialogCannotGetPosition())
					.setPositiveButton(Localization.getDialogOk(), null)
					.create().show();
			return;
		}

		String url = "http://maps.google.com/maps?saddr="
				+ String.valueOf(currentPosition.latitude) + ","
				+ String.valueOf(currentPosition.longitude) + "&daddr="
				+ String.valueOf(Utils.getPointPosition().latitude) + ","
				+ String.valueOf(Utils.getPointPosition().longitude);

		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		Utils.startFlurry(this.getActivity(),
				Constant.DIRECTIONS_ACTIVITY_EVENT);

		startActivity(intent);
	}
}
