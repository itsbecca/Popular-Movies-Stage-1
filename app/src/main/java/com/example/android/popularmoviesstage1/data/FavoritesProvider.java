package com.example.android.popularmoviesstage1.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import static com.example.android.popularmoviesstage1.data.FavoritesContract.*;

public class FavoritesProvider extends ContentProvider{

    public static final int CODE_FAVORITES = 100;
    public static final int CODE_FAVORITES_WITH_MOVIEID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private FavoritesDbHelper mOpenHelper;


    public static UriMatcher buildUriMatcher (){
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = CONTENT_AUTHORITY;

        matcher.addURI(authority, PATH_FAVORITES,
                CODE_FAVORITES);

        matcher.addURI(authority, PATH_FAVORITES + "/#",
                CODE_FAVORITES_WITH_MOVIEID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new FavoritesDbHelper(getContext());
        return true;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase database = mOpenHelper.getWritableDatabase();

        //Uses switch case identify Uri and take appropriate action
        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case CODE_FAVORITES:
                long db = database.insert(FavoritesEntry.TABLE_NAME, null, values);

                if (db > 0) {
                    returnUri = FavoritesEntry.buildUriWithMovieId(db);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri,null);

        return returnUri;
    }


    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            case CODE_FAVORITES:
                retCursor = db.query(FavoritesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_FAVORITES_WITH_MOVIEID:
                //isolate the movie id from the Uri
                String id = uri.getLastPathSegment();

                retCursor = db.query(FavoritesEntry.TABLE_NAME,
                        projection,
                        FavoritesEntry._ID + "=?",
                        new String[]{id},
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);

        return retCursor;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final  SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        int numRowsDeleted;

        switch (match) {
            case CODE_FAVORITES_WITH_MOVIEID:
                //isolate the movie id from the Uri
                String id = uri.getLastPathSegment();

                numRowsDeleted = db.delete(FavoritesEntry.TABLE_NAME,
                        FavoritesEntry.COLUMN_MOVIE_ID + "=?",
                        new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsDeleted;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
