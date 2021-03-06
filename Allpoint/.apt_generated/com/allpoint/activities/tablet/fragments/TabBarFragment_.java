//
// DO NOT EDIT THIS FILE, IT HAS BEEN GENERATED USING AndroidAnnotations 3.0.1.
//


package com.allpoint.activities.tablet.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.allpoint.R.layout;
import org.androidannotations.api.view.HasViews;
import org.androidannotations.api.view.OnViewChangedListener;
import org.androidannotations.api.view.OnViewChangedNotifier;

public final class TabBarFragment_
    extends TabBarFragment
    implements HasViews, OnViewChangedListener
{

    private final OnViewChangedNotifier onViewChangedNotifier_ = new OnViewChangedNotifier();
    private View contentView_;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        OnViewChangedNotifier previousNotifier = OnViewChangedNotifier.replaceNotifier(onViewChangedNotifier_);
        init_(savedInstanceState);
        super.onCreate(savedInstanceState);
        OnViewChangedNotifier.replaceNotifier(previousNotifier);
    }

    public View findViewById(int id) {
        if (contentView_ == null) {
            return null;
        }
        return contentView_.findViewById(id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView_ = super.onCreateView(inflater, container, savedInstanceState);
        if (contentView_ == null) {
            contentView_ = inflater.inflate(layout.bottom_bar_fragment, container, false);
        }
        return contentView_;
    }

    private void init_(Bundle savedInstanceState) {
        OnViewChangedNotifier.registerOnViewChangedListener(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onViewChangedNotifier_.notifyViewChanged(this);
    }

    public static TabBarFragment_.FragmentBuilder_ builder() {
        return new TabBarFragment_.FragmentBuilder_();
    }

    @Override
    public void onViewChanged(HasViews hasViews) {
        numberOfMessagesText = ((TextView) hasViews.findViewById(com.allpoint.R.id.tvBarNumberOfMessages));
        moreButton = ((ImageButton) hasViews.findViewById(com.allpoint.R.id.iBtnBottomMore));
        itxtBottomTransaction = ((TextView) hasViews.findViewById(com.allpoint.R.id.iTxtBottomTransaction));
        iBtnBottomTransaction = ((ImageButton) hasViews.findViewById(com.allpoint.R.id.iBtnBottomTransaction));
        tTxtBottomHome = ((TextView) hasViews.findViewById(com.allpoint.R.id.iTxtBottomHome));
        itxtBottomMessages = ((TextView) hasViews.findViewById(com.allpoint.R.id.iTxtBottomMessages));
        itxtBottomSearch = ((TextView) hasViews.findViewById(com.allpoint.R.id.iTxtBottomSearch));
        itxtBottomMore = ((TextView) hasViews.findViewById(com.allpoint.R.id.iTxtBottomMore));
        moreButtonText = ((TextView) hasViews.findViewById(com.allpoint.R.id.iTxtBottomMore));
        messageCountLayout = ((RelativeLayout) hasViews.findViewById(com.allpoint.R.id.layoutBarMessageCount));
        {
            View view = hasViews.findViewById(com.allpoint.R.id.iBtnBottomMore);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        TabBarFragment_.this.onIbtnBottomSettingsClicked();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(com.allpoint.R.id.iBtnBottomHome);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        TabBarFragment_.this.onIbtnBottomHomeClicked();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(com.allpoint.R.id.iBtnBottomMessages);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        TabBarFragment_.this.onIbtnBottomMessagesClicked();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(com.allpoint.R.id.iBtnBottomSearch);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        TabBarFragment_.this.onIbtnBottomSearchClicked();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(com.allpoint.R.id.iBtnBottomTransaction);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        TabBarFragment_.this.onIbtnBottomTransactionClicked();
                    }

                }
                );
            }
        }
    }

    public static class FragmentBuilder_ {

        private Bundle args_;

        private FragmentBuilder_() {
            args_ = new Bundle();
        }

        public TabBarFragment build() {
            TabBarFragment_ fragment_ = new TabBarFragment_();
            fragment_.setArguments(args_);
            return fragment_;
        }

    }

}
