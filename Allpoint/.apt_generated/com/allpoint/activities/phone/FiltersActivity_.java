//
// DO NOT EDIT THIS FILE, IT HAS BEEN GENERATED USING AndroidAnnotations 3.0.1.
//


package com.allpoint.activities.phone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.allpoint.R.id;
import com.allpoint.R.layout;
import com.allpoint.services.FilterManager_;
import com.allpoint.services.xmlParser.TaskManager_;
import org.androidannotations.api.view.HasViews;
import org.androidannotations.api.view.OnViewChangedListener;
import org.androidannotations.api.view.OnViewChangedNotifier;

public final class FiltersActivity_
    extends FiltersActivity
    implements HasViews, OnViewChangedListener
{

    private final OnViewChangedNotifier onViewChangedNotifier_ = new OnViewChangedNotifier();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        OnViewChangedNotifier previousNotifier = OnViewChangedNotifier.replaceNotifier(onViewChangedNotifier_);
        init_(savedInstanceState);
        super.onCreate(savedInstanceState);
        OnViewChangedNotifier.replaceNotifier(previousNotifier);
        setContentView(layout.filters);
    }

    private void init_(Bundle savedInstanceState) {
        OnViewChangedNotifier.registerOnViewChangedListener(this);
        inflater = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        taskManager = TaskManager_.getInstance_(this);
        filterManager = FilterManager_.getInstance_(this);
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

    public static FiltersActivity_.IntentBuilder_ intent(Context context) {
        return new FiltersActivity_.IntentBuilder_(context);
    }

    public static FiltersActivity_.IntentBuilder_ intent(android.app.Fragment fragment) {
        return new FiltersActivity_.IntentBuilder_(fragment);
    }

    public static FiltersActivity_.IntentBuilder_ intent(android.support.v4.app.Fragment supportFragment) {
        return new FiltersActivity_.IntentBuilder_(supportFragment);
    }

    @Override
    public void onViewChanged(HasViews hasViews) {
        listView = ((ListView) hasViews.findViewById(id.listView));
        filtersSelectBy = ((TextView) hasViews.findViewById(id.tvSelectFilterBy));
        filtersDoneButton = ((Button) hasViews.findViewById(id.btnFiltersDone));
        filtersTitle = ((TextView) hasViews.findViewById(id.tvFiltersTitle));
        {
            View view = hasViews.findViewById(id.tvFiltersTitle);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        FiltersActivity_.this.onTvFiltersTitleClick();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(id.btnFiltersDone);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        FiltersActivity_.this.onBtnFiltersDoneClick();
                    }

                }
                );
            }
        }
        {
            View view = hasViews.findViewById(id.iBtnFiltersBack);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        FiltersActivity_.this.onIbtnFiltersBackClicked();
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
            intent_ = new Intent(context, FiltersActivity_.class);
        }

        public IntentBuilder_(android.app.Fragment fragment) {
            fragment_ = fragment;
            context_ = fragment.getActivity();
            intent_ = new Intent(context_, FiltersActivity_.class);
        }

        public IntentBuilder_(android.support.v4.app.Fragment fragment) {
            fragmentSupport_ = fragment;
            context_ = fragment.getActivity();
            intent_ = new Intent(context_, FiltersActivity_.class);
        }

        public Intent get() {
            return intent_;
        }

        public FiltersActivity_.IntentBuilder_ flags(int flags) {
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
