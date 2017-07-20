package com.example.android.popularmoviesstage1.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.android.popularmoviesstage1.data.FavoritesContract.*;

//To store users favorite movies for access offline
public class FavoritesDbHelper extends SQLiteOpenHelper{

    static final String DATABASE_NAME = "favorites.db";
    static final int DATABASE_VERSION = 2;

    public FavoritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_FAVORITES_TABLE =
            "CREATE TABLE " + FavoritesEntry.TABLE_NAME + " (" +
                FavoritesEntry._ID                          + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FavoritesEntry.COLUMN_MOVIE_TITLE           + " TEXT NOT NULL," +
                FavoritesEntry.COLUMN_MOVIE_ID              + " TEXT NOT NULL," +
                FavoritesEntry.COLUMN_MOVIE_RATING          + " TEXT NOT NULL," +
                FavoritesEntry.COLUMN_MOVIE_RELEASE_DATE    + " TEXT NOT NULL," +
                FavoritesEntry.COLUMN_MOVIE_SYNOPSIS        + " TEXT NOT NULL," +
                FavoritesEntry.COLUMN_MOVIE_POSTER_LOC      + " TEXT NOT NULL,"  +
                " UNIQUE (" + FavoritesEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoritesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
