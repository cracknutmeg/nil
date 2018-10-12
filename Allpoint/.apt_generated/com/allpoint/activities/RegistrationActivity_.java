//
// DO NOT EDIT THIS FILE, IT HAS BEEN GENERATED USING AndroidAnnotations 3.0.1.
//


package com.allpoint.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.allpoint.R.id;
import com.allpoint.R.layout;
import com.allpoint.services.InternetConnectionManager_;
import org.androidannotations.api.SdkVersionHelper;
import org.androidannotations.api.view.HasViews;
import org.androidannotations.api.view.OnViewChangedListener;
import org.androidannotations.api.view.OnViewChangedNotifier;

public final class RegistrationActivity_
    extends RegistrationActivity
    implements HasViews, OnViewChangedListener
{

    private final OnViewChangedNotifier onViewChangedNotifier_ = new OnViewChangedNotifier();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        OnViewChangedNotifier previousNotifier = OnViewChangedNotifier.replaceNotifier(onViewChangedNotifier_);
        init_(savedInstanceState);
        super.onCreate(savedInstanceState);
        OnViewChangedNotifier.replaceNotifier(previousNotifier);
        setContentView(layout.activity_registration);
    }

    private void init_(Bundle savedInstanceState) {
        OnViewChangedNotifier.registerOnViewChangedListener(this);
        connectionManager = InternetConnectionManager_.getInstance_(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        onViewChangedNotifier_.notifyViewChanged(this);
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        super.setContentView(view, params);
        onViewChangedNotifier_.notifyViewChanged(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        onViewChangedNotifier_.notifyViewChanged(this);
    }

    public static RegistrationActivity_.IntentBuilder_ intent(Context context) {
        return new RegistrationActivity_.IntentBuilder_(context);
    }

    public static RegistrationActivity_.IntentBuilder_ intent(android.app.Fragment fragment) {
        return new RegistrationActivity_.IntentBuilder_(fragment);
    }

    public static RegistrationActivity_.IntentBuilder_ intent(android.support.v4.app.Fragment supportFragment) {
        return new RegistrationActivity_.IntentBuilder_(supportFragment);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (((SdkVersionHelper.getSdkInt()< 5)&&(keyCode == KeyEvent.KEYCODE_BACK))&&(event.getRepeatCount() == 0)) {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onViewChanged(HasViews hasViews) {
        laySub = ((RelativeLayout) hasViews.findViewById(id.subscribe_layout));
        email_Msg_Is = ((TextView) hasViews.findViewById(id.email_msg_is));
        cancel = ((TextView) hasViews.findViewById(id.cancel));
        email = ((EditText) hasViews.findViewById(id.email_edit_text));
        layPass = ((LinearLayout) hasViews.findViewById(id.password_layout));
        regTitle = ((TextView) hasViews.findViewById(id.registrationTitle));
        registrationTitle = ((TextView) hasViews.findViewById(id.registrationTitle));
        password = ((EditText) hasViews.findViewById(id.password_edit_text));
        subscribeToggleButton = ((Switch) hasViews.findViewById(id.subscribe_toggle_button));
        contBtn = ((TextView) hasViews.findViewById(id.continue_button));
        termsAndCond = ((TextView) hasViews.findViewById(id.tc));
        launchSettingGeofence = ((Switch) hasViews.findViewById(id.tBtnSetGeofence));
        passMsg = ((TextView) hasViews.findViewById(id.password_msg_textview));
        confirmPswd = ((EditText) hasViews.findViewById(id.confirm_edit_text));
        {
            View view = hasViews.findViewById(id.tc);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        RegistrationActivity_.this.onTermsAndConditionClicked();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(id.cancel);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        RegistrationActivity_.this.onIbtnCancelClicked();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(id.continue_button);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        RegistrationActivity_.this.onIbtnContinueClicked();
                    }

                }
                );
            }
        }
        AfterViews();
    }

    public static class IntentBuilder_ {

        private Context context_;
        private final Intent intent_;
        private android.app.Fragment fragment_;
        private android.support.v4.app.Fragment fragmentSupport_;

        public IntentBuilder_(Context context) {
            context_ = context;
            intent_ = new Intent(context, RegistrationActivity_.class);
        }

        public IntentBuilder_(android.app.Fragment fragment) {
            fragment_ = fragment;
            context_ = fragment.getActivity();
            intent_ = new Intent(context_, RegistrationActivity_.class);
        }

        public IntentBuilder_(android.support.v4.app.Fragment fragment) {
            fragmentSupport_ = fragment;
            context_ = fragment.getActivity();
            intent_ = new Intent(context_, RegistrationActivity_.class);
        }

        public Intent get() {
            return intent_;
        }

        public RegistrationActivity_.IntentBuilder_ flags(int flags) {
            intent_.setFlags(flags);
            return this;
        }

        public void start() {
            context_.startActivity(intent_);
        }

        public void startForResult(int requestCode) {
            if (fragmentSupport_!= null) {
                fragmentSupport_.startActivityForResult(intent_, requestCode);
            } else {
                if (fragment_!= null) {
                    fragment_.startActivityForResult(intent_, requestCode);
                } else {
                    if (context_ instanceof Activity) {
                        ((Activity) context_).startActivityForResult(intent_, requestCode);
                    } else {
                        context_.startActivity(intent_);
                    }
                }
            }
        }

    }

}
