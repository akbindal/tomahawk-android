/* == This file is part of Tomahawk Player - <http://tomahawk-player.org> ===
 *
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
import org.tomahawk.tomahawk_android.activities.TomahawkMainActivity;
import org.tomahawk.tomahawk_android.adapters.TomahawkMenuAdapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * {@link TomahawkListFragment} which shows a simple listview menu to the user, so that he can
 * choose between a {@link TracksFragment}, an {@link AlbumsFragment} and an {@link
 * ArtistsFragment}, which display the {@link org.tomahawk.libtomahawk.collection.UserCollection}'s
 * content to the user.
 */
public class UserCollectionFragment extends TomahawkListFragment implements OnItemClickListener {

    protected TomahawkMainActivity mTomahawkMainActivity;

    /**
     * Store the reference to the {@link Activity}, in which this {@link UserCollectionFragment} has
     * been created
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof TomahawkMainActivity) {
            mTomahawkMainActivity = (TomahawkMainActivity) activity;
        }
    }

    /**
     * Called, when this {@link UserCollectionFragment}'s {@link View} has been created
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TomahawkMenuAdapter tomahawkMenuAdapter = new TomahawkMenuAdapter(getActivity(),
                getResources().getStringArray(R.array.local_collection_menu_items),
                getResources().obtainTypedArray(R.array.local_collection_menu_items_icons));
        setListAdapter(tomahawkMenuAdapter);
        getListView().setOnItemClickListener(this);
    }

    /**
     * Null the reference to this {@link FakePreferenceFragment}'s {@link Activity}
     */
    @Override
    public void onDetach() {
        super.onDetach();

        mTomahawkMainActivity = null;
    }

    /**
     * Called every time an item inside the {@link org.tomahawk.tomahawk_android.views.TomahawkStickyListHeadersListView}
     * is clicked
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this will be a view
     *                 provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch ((int) id) {
            case 0:
                mTomahawkMainActivity.getContentViewer()
                        .replace(TomahawkMainActivity.HUB_ID_COLLECTION, TracksFragment.class, -1,
                                null, false);
                break;
            case 1:
                mTomahawkMainActivity.getContentViewer()
                        .replace(TomahawkMainActivity.HUB_ID_COLLECTION, AlbumsFragment.class, -1,
                                null, false);
                break;
            case 2:
                mTomahawkMainActivity.getContentViewer()
                        .replace(TomahawkMainActivity.HUB_ID_COLLECTION, ArtistsFragment.class, -1,
                                null, false);
                break;
        }
    }
}
