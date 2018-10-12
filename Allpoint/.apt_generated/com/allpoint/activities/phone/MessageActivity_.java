//
// DO NOT EDIT THIS FILE, IT HAS BEEN GENERATED USING AndroidAnnotations 3.0.1.
//


package com.allpoint.activities.phone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.allpoint.R.id;
import com.allpoint.R.layout;
import com.allpoint.util.Settings_;
import com.allpoint.util.Utils_;
import org.androidannotations.api.SdkVersionHelper;
import org.androidannotations.api.view.HasViews;
import org.androidannotations.api.view.OnViewChangedListener;
import org.androidannotations.api.view.OnViewChangedNotifier;

public final class MessageActivity_
    extends MessageActivity
    implements HasViews, OnViewChangedListener
{

    private final OnViewChangedNotifier onViewChangedNotifier_ = new OnViewChangedNotifier();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        OnViewChangedNotifier previousNotifier = OnViewChangedNotifier.replaceNotifier(onViewChangedNotifier_);
        init_(savedInstanceState);
        super.onCreate(savedInstanceState);
        OnViewChangedNotifier.replaceNotifier(previousNotifier);
        setContentView(layout.messages);
    }

    private void init_(Bundle savedInstanceState) {
        OnViewChangedNotifier.registerOnViewChangedListener(this);
        settings = Settings_.getInstance_(this);
        utils = Utils_.getInstance_(this);
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

    public static MessageActivity_.IntentBuilder_ intent(Context context) {
        return new MessageActivity_.IntentBuilder_(context);
    }

    public static MessageActivity_.IntentBuilder_ intent(android.app.Fragment fragment) {
        return new MessageActivity_.IntentBuilder_(fragment);
    }

    public static MessageActivity_.IntentBuilder_ intent(android.support.v4.app.Fragment supportFragment) {
        return new MessageActivity_.IntentBuilder_(supportFragment);
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
        listViewMessages = ((ListView) hasViews.findViewById(id.listView));
        btnMessages = ((ImageButton) hasViews.findViewById(id.iBtnBottomMessages));
        editLayout = ((LinearLayout) hasViews.findViewById(id.editLayout));
        tvMessage = ((TextView) hasViews.findViewById(id.iTxtBottomMessages));
        deleteMessagesButton = ((Button) hasViews.findViewById(id.btnDeleteMessages));
        readMessagesButton = ((Button) hasViews.findViewById(id.btnReadMessages));
        editModeButton = ((ToggleButton) hasViews.findViewById(id.tBtnEditMode));
        messagesTitle = ((TextView) hasViews.findViewById(id.tvMessagesTitle));
        messageCountLayout = ((RelativeLayout) hasViews.findViewById(id.layoutBarMessageCount));
        noMessagesText = ((TextView) hasViews.findViewById(id.tvNoMessages));
        noMessagesLayout = ((ViewGroup) hasViews.findViewById(id.layoutNoMessages));
        numberOfMessagesText = ((TextView) hasViews.findViewById(id.tvBarNumberOfMessages));
        {
            View view = hasViews.findViewById(id.btnReadMessages);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        MessageActivity_.this.onBtnReadMessagesClicked();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(id.btnDeleteMessages);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        MessageActivity_.this.onBtnDeleteMessagesClicked();
                    }

                }
                );
            }
        }
        {
            CompoundButton view = ((CompoundButton) hasViews.findViewById(id.tBtnEditMode));
            if (view!= null) {
                view.setOnCheckedChangeListener(new OnCheckedChangeListener() {


                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        MessageActivity_.this.onTbtnEditModeCheckedChanged(buttonView, isChecked);
                    }

                }
                );
            }
        }
        afterViews();
    }

    public static class IntentBuilder_ {

        private Context context_;
        private final Intent intent_;
        private android.app.Fragment fragment_;
        private android.support.v4.app.Fragment fragmentSupport_;

        public IntentBuilder_(Context context) {
            context_ = context;
            intent_ = new Intent(context, MessageActivity_.class);
        }

        public IntentBuilder_(android.app.Fragment fragment) {
            fragment_ = fragment;
            context_ = fragment.getActivity();
            intent_ = new Intent(context_, MessageActivity_.class);
        }

        public IntentBuilder_(android.support.v4.app.Fragment fragment) {
            fragmentSupport_ = fragment;
            context_ = fragment.getActivity();
            intent_ = new Intent(context_, MessageActivity_.class);
        }

        public Intent get() {
            return intent_;
        }

        public MessageActivity_.IntentBuilder_ flags(int flags) {
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