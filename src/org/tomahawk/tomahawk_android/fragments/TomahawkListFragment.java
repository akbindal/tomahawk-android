/* == This file is part of Tomahawk Player - <http://tomahawk-player.org> ===
 *
 *   Copyright 2013, Christopher Reichert <creichert07@gmail.com>
 *   Copyright 2013, Enno Gottschalk <mrmaffen@googlemail.com>
 *
 *   Tomahawk is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Tomahawk is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Tomahawk. If not, see <http://www.gnu.org/licenses/>.
 */
package org.tomahawk.tomahawk_android.fragments;

import org.tomahawk.tomahawk_android.R;
import org.tomahawk.tomahawk_android.adapters.TomahawkGridAdapter;
import org.tomahawk.tomahawk_android.adapters.TomahawkListAdapter;
import org.tomahawk.tomahawk_android.views.TomahawkStickyListHeadersListView;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * More customizable implementation of {@link android.app.ListFragment}
 */
public class TomahawkListFragment extends Fragment {

    public static final String TOMAHAWK_LIST_SCROLL_POSITION
            = "org.tomahawk.tomahawk_android.tomahawk_list_scroll_position";

    private StickyListHeadersAdapter mTomahawkListAdapter;

    private TomahawkGridAdapter mTomahawkGridAdapter;

    private boolean mShowGridView;

    private TomahawkStickyListHeadersListView mList;

    private GridView mGrid;

    private int mListScrollPosition = 0;

    final private Handler mHandler = new Handler();

    final private Runnable mRequestFocus = new Runnable() {
        public void run() {
            (mShowGridView ? mGrid : mList).focusableViewAvailable((mShowGridView ? mGrid : mList));
        }
    };

    /**
     * Get a stored list scroll position, if present
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey(TOMAHAWK_LIST_SCROLL_POSITION)
                    && getArguments().getInt(TOMAHAWK_LIST_SCROLL_POSITION) > 0) {
                mListScrollPosition = getArguments().getInt(TOMAHAWK_LIST_SCROLL_POSITION);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tomahawklistfragment_layout, null, false);
    }

    @Override
    public void onDestroyView() {
        mHandler.removeCallbacks(mRequestFocus);
        mList = null;
        mGrid = null;
        super.onDestroyView();
    }

    /**
     * Get this {@link TomahawkListFragment}'s {@link TomahawkStickyListHeadersListView}
     */
    public TomahawkStickyListHeadersListView getListView() {
        ensureList();
        return mList;
    }

    /**
     * Get this {@link TomahawkListFragment}'s {@link GridView}
     */
    public GridView getGridView() {
        ensureList();
        return mGrid;
    }

    /**
     * Set mList/mGrid to the listview/gridview layout element and catch possible exceptions.
     */
    private void ensureList() {
        if (((mShowGridView) ? mGrid : mList) != null) {
            return;
        }
        View root = getView();
        if (root == null) {
            throw new IllegalStateException("Content view not yet created");
        }
        if (root instanceof TomahawkStickyListHeadersListView) {
            mList = (TomahawkStickyListHeadersListView) root;
        } else if (root instanceof GridView) {
            mGrid = (GridView) root;
        } else {
            if (!mShowGridView) {
                View rawListView = root.findViewById(R.id.listview);
                if (!(rawListView instanceof TomahawkStickyListHeadersListView)) {
                    if (rawListView == null) {
                        throw new RuntimeException(
                                "Your content must have a TomahawkStickyListHeadersListView whose id attribute is "
                                        + "'R.id.listview'");
                    }
                    throw new RuntimeException("Content has view with id attribute 'R.id.listview' "
                            + "that is not a TomahawkStickyListHeadersListView class");
                }
                mList = (TomahawkStickyListHeadersListView) rawListView;
                if (mTomahawkListAdapter != null) {
                    setListAdapter(mTomahawkListAdapter);
                }
            } else {
                View rawListView = root.findViewById(R.id.gridview);
                if (!(rawListView instanceof GridView)) {
                    if (rawListView == null) {
                        throw new RuntimeException(
                                "Your content must have a GridView whose id attribute is "
                                        + "'R.id.gridview'");
                    }
                    throw new RuntimeException("Content has view with id attribute 'R.id.gridview' "
                            + "that is not a GridView class");
                }
                mGrid = (GridView) rawListView;
                if (mTomahawkGridAdapter != null) {
                    setGridAdapter(mTomahawkGridAdapter);
                }
            }
        }
        mHandler.post(mRequestFocus);
    }

    /**
     * @return the current scrolling position of the list- or gridView
     */
    public int getListScrollPosition() {
        if (mShowGridView) {
            return getGridView().getFirstVisiblePosition();
        }
        return getListView().getFirstVisiblePosition();
    }

    /**
     * Get the {@link se.emilsjolander.stickylistheaders.StickyListHeadersAdapter} associated with
     * this activity's ListView.
     */
    public StickyListHeadersAdapter getListAdapter() {
        return mTomahawkListAdapter;
    }

    /**
     * Get the {@link org.tomahawk.tomahawk_android.adapters.TomahawkGridAdapter} associated with
     * this activity's GridView.
     */
    public TomahawkGridAdapter getGridAdapter() {
        return mTomahawkGridAdapter;
    }

    /**
     * Set the {@link org.tomahawk.tomahawk_android.adapters.TomahawkListAdapter} associated with
     * this activity's ListView.
     */
    public void setListAdapter(StickyListHeadersAdapter adapter) {
        mTomahawkListAdapter = adapter;
        mShowGridView = false;
        TomahawkStickyListHeadersListView listView = getListView();
        listView.setAdapter(adapter);
        listView.setSelection(mListScrollPosition);
    }

    /**
     * Set the {@link org.tomahawk.tomahawk_android.adapters.TomahawkGridAdapter} associated with
     * this activity's GridView.
     */
    public void setGridAdapter(TomahawkGridAdapter adapter) {
        mTomahawkGridAdapter = adapter;
        mShowGridView = true;
        GridView gridView = getGridView();
        gridView.setAdapter(adapter);
        gridView.setSelection(mListScrollPosition);
    }
}
