package com.example.android.popularmoviesstage1.utilities;

import android.net.Uri;

import com.example.android.popularmoviesstage1.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {
    final static String BASE_URL = "http://api.themoviedb.org/3/movie/";
    final static String PARAM_QUERY = "api_key";
    final static String apiKey = BuildConfig.MOVIE_DB_API_KEY;
    final static String SORT_POPULAR_SELECTOR = "Most Popular";
    final static String SORT_POPULAR_ID = "popular";
    final static String SORT_RATED_SELECTOR = "Top Rated";
    final static String SORT_RATED_ID = "top_rated";


    public static URL buildUrl(String sortBy){
        if (sortBy.equals(SORT_POPULAR_SELECTOR)) {
            sortBy = SORT_POPULAR_ID;
        } else if (sortBy.equals(SORT_RATED_SELECTOR)) {
            sortBy = SORT_RATED_ID;
        }
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(sortBy)
                .appendQueryParameter(PARAM_QUERY, apiKey)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

}
