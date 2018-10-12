/**
 *@ FiltersActivity
 */
package com.allpoint.activities.phone;

import java.util.List;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;

import android.app.Activity;
import android.app.ProgressDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.allpoint.R;
import com.allpoint.activities.fragments.AlertDialogFragment;
import com.allpoint.model.Filter;
import com.allpoint.model.SearchResult;
import com.allpoint.model.VersionInfo;
import com.allpoint.services.FilterManager;
import com.allpoint.services.xmlParser.ErrorType;
import com.allpoint.services.xmlParser.TaskManager;
import com.allpoint.util.Constant;
import com.allpoint.util.Localization;
import com.allpoint.util.Utils;
import com.allpoint.util.adapters.FilterListAdapter;
import com.bugsense.trace.BugSenseHandler;

/**
 * FiltersActivity
 * 
 * @author: Mikhail.Shalagin & Vyacheslav.Shmakin
 * @version: 23.09.13
 */
@EActivity(R.layout.filters)
public class FiltersActivity extends Activity implements
		TaskManager.AsyncTaskListener {

	@Bean
	protected TaskManager taskManager;

	@Bean
	protected FilterManager filterManager;

	@ViewById(R.id.listView)
	protected ListView listView;

	// Filters
	@ViewById(R.id.tvFiltersTitle)
	protected TextView filtersTitle;

	@ViewById(R.id.tvSelectFilterBy)
	protected TextView filtersSelectBy;

	@ViewById(R.id.btnFiltersDone)
	protected Button filtersDoneButton;

	private AlertDialogFragment alertDialog;

	@SystemService
	protected LayoutInflater inflater;

	private ProgressDialog dialog;

	@AfterViews
	protected void afterViews() {
		BugSenseHandler.initAndStartSession(this, Constant.BUG_SENSE_API_KEY);

		dialog = ProgressDialog.show(this,
				Localization.getDialogLoadingTitle(),
				Localization.getDialogPleaseWait(), true);

		taskManager.addTaskListener(this);
		taskManager.searchFilters(Constant.FILTERS_TASK_ID);
	}

	@Click(R.id.btnFiltersDone)
	void onBtnFiltersDoneClick() {
		finish();
	}

	@Override
	protected void onStart() {
		super.onStart();

		Utils.startFlurry(this, Constant.FILTERS_ACTIVITY_EVENT);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Filters
		filtersTitle.setText(Localization.getFiltersLayoutTitle());
		filtersSelectBy.setText(Localization.getFiltersSelectByTitle());
		filtersDoneButton.setText(Localization.getFiltersBtnDone());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		taskManager.removeTaskListener(this);
	}

	@Click(R.id.iBtnFiltersBack)
	protected void onIbtnFiltersBackClicked() {
		onBackPressed();
	}

	@Click(R.id.tvFiltersTitle)
	public void onTvFiltersTitleClick() {
		onBackPressed();
	}

	@Override
	public void onTaskStarted(String taskId, TaskManager.QueryId queryId) {
	}

	@Override
	public void onSearchFinished(String taskId, ErrorType error,
			SearchResult result) {
	}

	@Override
	public void onReceiveFilters(String taskId, final ErrorType error,
			List<Filter> result) {
		if (taskId.equals(Constant.FILTERS_TASK_ID)) {
			switch (error) {
			case NO_FILTERS_FOUND:
				break;
			case TASK_FINISHED:
				FilterListAdapter filterListAdapter = new FilterListAdapter(
						FiltersActivity.this);

				final List<Filter> temp = filterManager
						.getUpdatedFilterList(result);
				for (Filter filter : temp) {
					filterListAdapter.add(filter);
				}
				listView.setAdapter(filterListAdapter);

				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						CheckBox checkBox = (CheckBox) view
								.findViewById(R.id.chBoxFilterName);
						if (checkBox != null) {
							checkBox.setChecked(!checkBox.isChecked());
							filterManager.setActivated(position,
									!temp.get(position).isActivated());
						}
					}
				});
				break;
			case CONNECTION_ERROR:
				alertDialog = new AlertDialogFragment(
						Localization.getDialogCannotConnectTitle(),
						Localization.getDialogCannotConnectText(),
						Localization.getDialogOk());
				break;
			case SERVICE_UNAVAILABLE:
				alertDialog = new AlertDialogFragment(
						Localization.getDialogServiceUnavailable(),
						Localization.getDialogOk());
				break;
			case XML_PARSER_ERROR:
				alertDialog = new AlertDialogFragment(
						Localization.getDialogParsingError(),
						Localization.getDialogOk());
				break;
			case NO_RESULTS_FOUND:
				break;
			case NO_VERSION_INFO_FOUND:
				break;
			case TASK_STOPPED:
				break;
			default:
				break;
			}
			if (AlertDialogFragment.getInstance() != null) {
				if (alertDialog != null) {
					alertDialog.show(getFragmentManager(),
							Constant.ERROR_DIALOG_TAG);
				}
			}
			dialog.dismiss();
		}
	}

	@Override
	public void onReceiveVersionInfo(String taskId, ErrorType error,
			VersionInfo result) {
	}
}
