package com.example.android.popularmoviesstage1.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
                long db = database.insert(ContractEntry.TABLE_NAME, null, values);

                if (db > 0) {
                    returnUri = ContractEntry.buildUriWithMovieId(db);
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

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
