package com.allpoint.services;

import java.util.ArrayList;
import java.util.List;

import org.androidannotations.annotations.EBean;

import com.allpoint.model.Filter;

/**
 * Created by vyacheslav.shmakin on 20.12.13.
 */
@EBean(scope = EBean.Scope.Singleton)
public class FilterManager {

	private List<Filter> currentFilters;

	public void setActivated(final int position, final boolean isActivated) {
		if (currentFilters != null) {
			currentFilters.get(position).setActivated(isActivated);
		}
	}

	public String getFiltersString(List<Filter> filters) {
		StringBuilder builder = new StringBuilder();

		if (filters == null) {
			if (currentFilters == null) {
				return builder.toString();
			} else {
				filters = currentFilters;
			}
		}

		for (Filter filter : filters) {
			if (filter.isActivated()) {
				if (builder.length() != 0) {
					builder.append(",");
				}
				builder.append(filter.getValue());
			}
		}
		return builder.toString();
	}

	public List<Filter> getUpdatedFilterList(final List<Filter> newFilters) {
		if (currentFilters == null) {
			currentFilters = new ArrayList<Filter>(newFilters);
			return currentFilters;
		} else {
			for (Filter filter : newFilters) {
				int index = currentFilters.indexOf(filter);
				if (index != -1) {
					filter.setActivated(currentFilters.get(index).isActivated());
				}
			}
			return newFilters;
		}
	}
}
