/* Copyright (c) 2009-2011 Matthias Kaeppler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ignition.core.adapters;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.app.ListActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;

public abstract class EndlessListAdapter<T> extends BaseAdapter {

    private boolean isLoadingData;

    private View progressView;

    private ArrayList<T> data = new ArrayList<T>();

    private AbsListView listView;

    public EndlessListAdapter(ListActivity activity, int progressItemLayoutResId) {
        this(activity, activity.getListView(), progressItemLayoutResId);
    }

    public EndlessListAdapter(ExpandableListActivity activity, int progressItemLayoutResId) {
        this(activity, activity.getExpandableListView(), progressItemLayoutResId);
    }

    public EndlessListAdapter(Activity activity, AbsListView listView, int progressItemLayoutResId) {
        this.listView = listView;
        this.progressView = activity.getLayoutInflater().inflate(progressItemLayoutResId, listView,
                false);
    }

    public AbsListView getListView() {
        return listView;
    }

    public View getProgressView() {
        return progressView;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Don't use this to check for the presence of actual data items; use {@link #hasItems()}
     * instead.
     * </p>
     */
    @Override
    public int getCount() {
        int size = 0;
        if (data != null) {
            size += data.size();
        }
        if (isLoadingData) {
            size += 1;
        }
        return size;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Don't use this to check for the presence of actual data items; use {@link #hasItems()}
     * instead.
     * </p>
     */
    @Override
    public boolean isEmpty() {
        return getCount() == 0 && !isLoadingData;
    }

    /**
     * @return the actual number of data items in this adapter, ignoring the progress item.
     */
    public int getItemCount() {
        if (data != null) {
            return data.size();
        }
        return 0;
    }

    /**
     * @return true if there are actual data items, ignoring the progress item.
     */
    public boolean hasItems() {
        return data != null && !data.isEmpty();
    }

    @Override
    public T getItem(int position) {
        if (data == null) {
            return null;
        }
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        if (isPositionOfProgressElement(position)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    public void setIsLoadingData(boolean isLoadingData) {
        setIsLoadingData(isLoadingData, true);
    }

    public void setIsLoadingData(boolean isLoadingData, boolean redrawList) {
        this.isLoadingData = isLoadingData;
        if (redrawList) {
            notifyDataSetChanged();
        }
    }

    public boolean isLoadingData() {
        return isLoadingData;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (isPositionOfProgressElement(position)) {
            return progressView;
        }

        return doGetView(position, convertView, parent);
    }

    protected abstract View doGetView(int position, View convertView, ViewGroup parent);

    private boolean isPositionOfProgressElement(int position) {
        return isLoadingData && position == data.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionOfProgressElement(position)) {
            return IGNORE_ITEM_VIEW_TYPE;
        }
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public ArrayList<T> getData() {
        return data;
    }

    public void addAll(List<T> items) {
        data.addAll(items);
        notifyDataSetChanged();
    }

    public void addAll(List<T> items, boolean redrawList) {
        data.addAll(items);
        if (redrawList) {
            notifyDataSetChanged();
        }
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    public void remove(int position) {
        data.remove(position);
        notifyDataSetChanged();
    }

    /**
     * Call this method from {@link OnScrollListener#onScroll(AbsListView, int, int, int)} to
     * determine whether the adapter should fetch the next page of data.
     * 
     * <p>
     * A typical implementation in your Activity might look like this:
     * 
     * <pre>
     * public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
     *         int totalItemCount) {
     * 
     *     if (adapter.shouldRequestNextPage(firstVisibleItem, visibleItemCount, totalItemCount)) {
     *         // fetch next page, e.g. make a Web service call
     *         // ...
     *         adapter.setIsLoadingData(true);
     *     }
     * }
     * </pre>
     * 
     * </p>
     * 
     * @param firstVisibleItem
     *            passed down from {@link OnScrollListener#onScroll(AbsListView, int, int, int)}
     * @param visibleItemCount
     *            passed down from {@link OnScrollListener#onScroll(AbsListView, int, int, int)}
     * @param totalItemCount
     *            passed down from {@link OnScrollListener#onScroll(AbsListView, int, int, int)}
     * @return true if the bottom of the list was reached, and hence the next page of data should be
     *         loaded
     */
    public boolean shouldRequestNextPage(int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        // subtract the progress element, or otherwise this will screw up the counts...but do it
        // ONLY if data is currently loading, or this will screw up the counts as well! :)
        if (isLoadingData) {
            totalItemCount -= 1;
        }
        boolean lastItemReached = (totalItemCount > 0)
                && (totalItemCount - visibleItemCount == firstVisibleItem);
        return !isLoadingData && lastItemReached;
    }
}
