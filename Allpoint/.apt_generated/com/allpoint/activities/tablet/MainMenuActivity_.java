//
// DO NOT EDIT THIS FILE, IT HAS BEEN GENERATED USING AndroidAnnotations 3.0.1.
//


package com.allpoint.activities.tablet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.allpoint.R.id;
import com.allpoint.R.layout;
import com.allpoint.services.InternetConnectionManager_;
import org.androidannotations.api.SdkVersionHelper;
import org.androidannotations.api.view.HasViews;
import org.androidannotations.api.view.OnViewChangedListener;
import org.androidannotations.api.view.OnViewChangedNotifier;

public final class MainMenuActivity_
    extends MainMenuActivity
    implements HasViews, OnViewChangedListener
{

    private final OnViewChangedNotifier onViewChangedNotifier_ = new OnViewChangedNotifier();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        OnViewChangedNotifier previousNotifier = OnViewChangedNotifier.replaceNotifier(onViewChangedNotifier_);
        init_(savedInstanceState);
        super.onCreate(savedInstanceState);
        OnViewChangedNotifier.replaceNotifier(previousNotifier);
        setContentView(layout.main_menu);
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

    public static MainMenuActivity_.IntentBuilder_ intent(Context context) {
        return new MainMenuActivity_.IntentBuilder_(context);
    }

    public static MainMenuActivity_.IntentBuilder_ intent(android.app.Fragment fragment) {
        return new MainMenuActivity_.IntentBuilder_(fragment);
    }

    public static MainMenuActivity_.IntentBuilder_ intent(android.support.v4.app.Fragment supportFragment) {
        return new MainMenuActivity_.IntentBuilder_(supportFragment);
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
        mainMenuSearch = ((TextView) hasViews.findViewById(id.tvMenuSearch));
        mainMenuMessages = ((TextView) hasViews.findViewById(id.tvMenuMessages));
        numberOfMessagesText = ((TextView) hasViews.findViewById(id.tvMenuNumberOfMessages));
        btnHome = ((ImageButton) hasViews.findViewById(id.iBtnBottomHome));
        btnLogin = ((Button) hasViews.findViewById(id.tvLogin));
        mainMenuScan = ((TextView) hasViews.findViewById(id.tvMenuScan));
        imgBtnTrasHistory = ((ImageButton) hasViews.findViewById(id.iBtnMenuTrans));
        messageCountLayout = ((RelativeLayout) hasViews.findViewById(id.layoutMenuMessageCount));
        mainMenuTrans = ((TextView) hasViews.findViewById(id.tvMenuTrans));
        searchBtn = ((ImageButton) hasViews.findViewById(id.iBtnMenuSearch));
        txtHome = ((TextView) hasViews.findViewById(id.iTxtBottomHome));
        {
            View view = hasViews.findViewById(id.iBtnMenuScan);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        MainMenuActivity_.this.onIbtnMenuScanCardClicked();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(id.iBtnMenuSearch);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        MainMenuActivity_.this.onIbtnMenuSearchClicked();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(id.tvLogin);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        MainMenuActivity_.this.onIbtnLoginClicked();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(id.iBtnMenuMessages);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        MainMenuActivity_.this.onIbtnMenuMessagesClicked();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(id.iBtnMenuTrans);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        MainMenuActivity_.this.onIbtnMenuAboutClicked();
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
            intent_ = new Intent(context, MainMenuActivity_.class);
        }

        public IntentBuilder_(android.app.Fragment fragment) {
            fragment_ = fragment;
            context_ = fragment.getActivity();
            intent_ = new Intent(context_, MainMenuActivity_.class);
        }

        public IntentBuilder_(android.support.v4.app.Fragment fragment) {
            fragmentSupport_ = fragment;
            context_ = fragment.getActivity();
            intent_ = new Intent(context_, MainMenuActivity_.class);
        }

        public Intent get() {
            return intent_;
        }

        public MainMenuActivity_.IntentBuilder_ flags(int flags) {
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
