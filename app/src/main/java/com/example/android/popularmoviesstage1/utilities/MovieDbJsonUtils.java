package com.example.android.popularmoviesstage1.utilities;

import android.content.Context;
import android.net.Uri;

import com.example.android.popularmoviesstage1.MovieClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public final class MovieDbJsonUtils {

    final static String BASE_URL = "http://image.tmdb.org/t/p";
    final static String IMG_SIZE = "w342";

    public static ArrayList<MovieClass> getMovieDbStringsFromJson(Context context, String movieDbJsonString) throws JSONException {
        final String MDB_MOVIE_TITLE = "original_title";
        final String MDB_POSTER_PATH = "poster_path";
        final String MDB_SYNOPSIS = "overview";
        final String MDB_RELEASE_DATE = "release_date";
        final String MDB_USER_RATING = "vote_average";

        String[] parsedMovieData = null;
        JSONObject reader = new JSONObject(movieDbJsonString);
        JSONArray results = reader.getJSONArray("results");

        ArrayList<MovieClass> movies = new ArrayList<>();

        for (int i = 0; i < results.length(); i++){
            JSONObject movie = results.getJSONObject(i);
            String movieTitle = movie.getString(MDB_MOVIE_TITLE);
            String posterPath = movie.getString(MDB_POSTER_PATH);
            String synopsis = movie.getString(MDB_SYNOPSIS);
            String releaseDate = movie.getString(MDB_RELEASE_DATE);
            String userRating = movie.getString(MDB_USER_RATING);

            String posterUrl = buildPosterUrl(posterPath);

            movies.add(new MovieClass(movieTitle,posterUrl,synopsis,releaseDate,userRating));
        }

        return movies;

    }

    public static String buildPosterUrl(String posterPath) {
        String posterUrl;

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(IMG_SIZE)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString() + posterPath);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        posterUrl = url.toString();
        return posterUrl;
    }
}
