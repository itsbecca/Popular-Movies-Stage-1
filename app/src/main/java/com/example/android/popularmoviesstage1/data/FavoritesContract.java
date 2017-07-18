package com.example.android.popularmoviesstage1.data;

import android.net.Uri;
import android.provider.BaseColumns;

//To store users favorite movies for access offline
public class FavoritesContract {

    private FavoritesContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.android.popularmoviesstage1";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FAVORITES = "favorites";



    public static final class ContractEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVORITES).build();

        public static final String TABLE_NAME = "favorites";

        public static final String COLUMN_MOVIE_TITLE = "title";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_MOVIE_RATING = "rating";
        public static final String COLUMN_MOVIE_RELEASE_DATE = "date";
        public static final String COLUMN_MOVIE_SYNOPSIS = "synopsis";
        public static final String COLUMN_MOVIE_POSTER_ID = "poster_id";

        //Access the row of a movie by movieId
        public static Uri buildUriWithMovieId(long id) {
            String movieId = String.valueOf(id);
            return CONTENT_URI.buildUpon()
                    .appendPath(movieId).build();
        }

    }
}
