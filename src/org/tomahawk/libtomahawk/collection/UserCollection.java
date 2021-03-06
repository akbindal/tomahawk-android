/* == This file is part of Tomahawk Player - <http://tomahawk-player.org> ===
 *
 *   Copyright 2012, Christopher Reichert <creichert07@gmail.com>
 *   Copyright 2012, Enno Gottschalk <mrmaffen@googlemail.com>
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
package org.tomahawk.libtomahawk.collection;

import org.tomahawk.libtomahawk.database.UserPlaylistsDataSource;
import org.tomahawk.libtomahawk.resolver.Resolver;
import org.tomahawk.tomahawk_android.TomahawkApp;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents a user's local {@link Collection} of all his {@link Track}s.
 */
public class UserCollection extends Collection {

    public static final String USERCOLLECTION_ARTISTCACHED
            = "org.tomahawk.tomahawk_android..USERCOLLECTION_ARTISTCACHED";

    public static final String USERCOLLECTION_ALBUMCACHED
            = "org.tomahawk.tomahawk_android..USERCOLLECTION_ALBUMCACHED";

    public static final String USERCOLLECTION_PLAYLISTCACHED
            = "org.tomahawk.tomahawk_android..USERCOLLECTION_PLAYLISTCACHED";

    public static final int Id = 0;

    private TomahawkApp mTomahawkApp;

    private HandlerThread mCollectionUpdateHandlerThread;

    private Handler mHandler;

    private ConcurrentHashMap<Long, Artist> mArtists = new ConcurrentHashMap<Long, Artist>();

    private Artist mCachedArtist;

    private ConcurrentHashMap<Long, Album> mAlbums = new ConcurrentHashMap<Long, Album>();

    private Album mCachedAlbum;

    private ConcurrentHashMap<Long, Track> mTracks = new ConcurrentHashMap<Long, Track>();

    private UserPlaylist mCachedUserPlaylist;

    private ConcurrentHashMap<Long, UserPlaylist> mCustomPlaylists
            = new ConcurrentHashMap<Long, UserPlaylist>();

    private Runnable mUpdateRunnable = new Runnable() {
        /* 
         * (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            update();
            mCollectionUpdateHandlerThread.getLooper().quit();
        }
    };

    /**
     * This class watches for changes in the Media db.
     */
    private final ContentObserver mLocalMediaObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            mCollectionUpdateHandlerThread.start();
            mHandler.post(mUpdateRunnable);
        }
    };

    /**
     * Construct a new {@link UserCollection} and initializes it.
     */
    public UserCollection(TomahawkApp tomahawkApp) {
        mTomahawkApp = tomahawkApp;

        TomahawkApp.getContext().getContentResolver()
                .registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, false,
                        mLocalMediaObserver);

        mCollectionUpdateHandlerThread = new HandlerThread("CollectionUpdate",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mCollectionUpdateHandlerThread.start();

        mHandler = new Handler(mCollectionUpdateHandlerThread.getLooper());
        mHandler.postDelayed(mUpdateRunnable, 300);
    }

    /**
     * @return A {@link List} of all {@link Artist}s in this {@link UserCollection}
     */
    @Override
    public List<Artist> getArtists() {
        ArrayList<Artist> artists = new ArrayList<Artist>(mArtists.values());
        Collections.sort(artists, new ArtistComparator(ArtistComparator.COMPARE_ALPHA));
        return artists;
    }

    /**
     * Get an {@link Artist} from this {@link UserCollection} by providing an id
     */
    @Override
    public Artist getArtistById(Long id) {
        return mArtists.get(id);
    }

    /**
     * Caches an {@link Artist} in a private member variable
     */
    @Override
    public void setCachedArtist(Artist artist) {
        mCachedArtist = artist;
    }

    /**
     * @return the cached {@link Artist}
     */
    @Override
    public Artist getCachedArtist() {
        return mCachedArtist;
    }

    /**
     * @return A {@link List} of all {@link Album}s in this {@link UserCollection}
     */
    @Override
    public List<Album> getAlbums() {
        ArrayList<Album> albums = new ArrayList<Album>(mAlbums.values());
        Collections.sort(albums, new AlbumComparator(AlbumComparator.COMPARE_ALPHA));
        return albums;
    }

    /**
     * Get an {@link Album} from this {@link UserCollection} by providing an id
     */
    @Override
    public Album getAlbumById(Long id) {
        return mAlbums.get(id);
    }

    /**
     * Caches an {@link Album} in a private member variable
     */
    @Override
    public void setCachedAlbum(Album album) {
        mCachedAlbum = album;
    }

    /**
     * @return the cached {@link Album}
     */
    @Override
    public Album getCachedAlbum() {
        return mCachedAlbum;
    }

    /**
     * @return A {@link List} of all {@link UserPlaylist}s in this {@link UserCollection}
     */
    @Override
    public List<UserPlaylist> getCustomPlaylists() {
        return new ArrayList<UserPlaylist>(mCustomPlaylists.values());
    }

    /**
     * Get an {@link UserPlaylist} from this {@link UserCollection} by providing an id
     */
    @Override
    public UserPlaylist getCustomPlaylistById(Long id) {
        return mCustomPlaylists.get(id);
    }

    /**
     * Add a {@link UserPlaylist} to this {@link UserCollection}
     */
    public void addCustomPlaylist(long playlistId, UserPlaylist userPlaylist) {
        mCustomPlaylists.put(playlistId, userPlaylist);
    }

    /**
     * Store the PlaybackService's currentPlaylist
     */
    public void setCachedPlaylist(UserPlaylist userPlaylist) {
        mCachedUserPlaylist = userPlaylist;
    }

    /**
     * @return the previously cached {@link UserPlaylist}
     */
    public UserPlaylist getCachedUserPlaylist() {
        return mCachedUserPlaylist;
    }

    /**
     * @return A {@link List} of all {@link Track}s in this {@link UserCollection}
     */
    @Override
    public List<Track> getTracks() {
        ArrayList<Track> tracks = new ArrayList<Track>(mTracks.values());
        Collections.sort(tracks, new TrackComparator(TrackComparator.COMPARE_ALPHA));
        return tracks;
    }

    /**
     * Get an {@link Track} from this {@link UserCollection} by providing an id
     */
    @Override
    public Track getTrackById(Long id) {
        return mTracks.get(id);
    }

    /**
     * @return always true
     */
    @Override
    public boolean isLocal() {
        return true;
    }

    /**
     * Initialize this {@link UserCollection}. Pull all local tracks from the {@link MediaStore} and
     * add them to our {@link UserCollection}
     */
    private void initializeCollection() {
        Resolver userCollectionResolver = mTomahawkApp.getPipeLine().getResolver(
                TomahawkApp.RESOLVER_ID_USERCOLLECTION);

        updateUserPlaylists();

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TRACK, MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM};

        ContentResolver resolver = TomahawkApp.getContext().getContentResolver();

        Cursor cursor = resolver
                .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null,
                        null);

        // Go through the complete set of data in the MediaStore
        while (cursor != null && cursor.moveToNext()) {
            Artist artist = mArtists.get(cursor.getLong(5));
            if (artist == null) {
                artist = Artist.get(cursor.getLong(5));
                artist.setName(cursor.getString(6));

                mArtists.put(artist.getId(), artist);
            }

            Album album = mAlbums.get(cursor.getLong(7));
            if (album == null) {
                album = Album.get(cursor.getLong(7));
                album.setName(cursor.getString(8));

                String albumsel = MediaStore.Audio.Albums._ID + " == " + Long
                        .toString(album.getId());

                String[] albumproj = {MediaStore.Audio.Albums.ALBUM_ART,
                        MediaStore.Audio.Albums.FIRST_YEAR, MediaStore.Audio.Albums.LAST_YEAR};

                Cursor albumcursor = resolver
                        .query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumproj, albumsel,
                                null, null);

                if (albumcursor != null && albumcursor.moveToNext()) {

                    album.setAlbumArtPath(albumcursor.getString(0));
                    album.setFirstYear(albumcursor.getString(1));
                    album.setLastYear(albumcursor.getString(2));

                    mAlbums.put(album.getId(), album);
                }

                if (albumcursor != null) {
                    albumcursor.close();
                }
            }

            Track track = mTracks.get(cursor.getLong(0));
            if (track == null) {
                track = Track.get(cursor.getLong(0));
                track.setPath(cursor.getString(1));
                track.setName(cursor.getString(2));
                track.setDuration(cursor.getLong(3));
                track.setTrackNumber(cursor.getInt(4));
                track.setResolver(userCollectionResolver);

                mTracks.put(track.getId(), track);
            }

            artist.addAlbum(album);
            artist.addTrack(track);

            album.addTrack(track);
            album.setArtist(artist);

            track.setAlbum(album);
            track.setArtist(artist);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    /**
     * Fetch all user {@link UserPlaylist} from the app's database via our helper class {@link
     * UserPlaylistsDataSource}
     */
    public void updateUserPlaylists() {
        mCustomPlaylists.clear();
        ArrayList<UserPlaylist> userPlayListList = mTomahawkApp.getUserPlaylistsDataSource()
                .getAllUserPlaylists();
        for (UserPlaylist userPlaylist : userPlayListList) {
            if (userPlaylist.getId() == UserPlaylistsDataSource.CACHED_PLAYLIST_ID) {
                setCachedPlaylist(userPlaylist);
            } else {
                mCustomPlaylists.put(userPlaylist.getId(), userPlaylist);
            }
        }
        TomahawkApp.getContext().sendBroadcast(new Intent(COLLECTION_UPDATED));
    }

    /**
     * Reinitalize this {@link UserCollection} and send a broadcast letting everybody know.
     */
    @Override
    public void update() {
        initializeCollection();

        TomahawkApp.getContext().sendBroadcast(new Intent(COLLECTION_UPDATED));
    }

    /**
     * @return this {@link UserCollection}'s id
     */
    @Override
    public int getId() {
        return Id;
    }
}
