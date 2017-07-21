package com.example.android.popularmoviesstage1.utilities;

import android.content.Context;
import android.net.Uri;

import com.example.android.popularmoviesstage1.MainActivity;
import com.example.android.popularmoviesstage1.MovieClass;
import com.example.android.popularmoviesstage1.MovieDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public final class MovieDbJsonUtils {

    final static String BASE_URL = "http://image.tmdb.org/t/p";
    final static String IMG_SIZE = "w342";

    public static ArrayList getMovieDbStringsFromJson(Context context, String movieDbJsonString) throws JSONException {
        //constants for main MovieDb query
        final String MDB_MOVIE_TITLE = "original_title";
        final String MDB_POSTER_PATH = "poster_path";
        final String MDB_SYNOPSIS = "overview";
        final String MDB_RELEASE_DATE = "release_date";
        final String MDB_USER_RATING = "vote_average";
        final String MDB_MOVIE_ID = "id";
        final String MDB_RESULTS = "results";

        //constants for movie detail query
        final String MOVIE_TRAILERS = "videos";
        final String MOVIE_TRAILER_ID = "key";
        final String MOVIE_TRAILER_NAME = "name";
        final String MOVIE_REVIEWS = "reviews";
        final String MOVIE_REVIEWER_NAME = "author";
        final String MOVIE_REVIEW_TEXT = "content";
        final String MOVIE_REVIEW_URL = "url"; //TODO Remove here and below if end up not using/needing

        JSONObject reader = new JSONObject(movieDbJsonString);

        //For Movie details from MainActivity Query
        ArrayList<MovieClass> movies = new ArrayList<>();

        //For extra details from movie queried in MovieDetail
        ArrayList<ArrayList<String>> extraMovieDetails = new ArrayList<>();
        ArrayList<String> movieTrailers = new ArrayList<>();
        ArrayList<String> movieReviews = new ArrayList<>();

        //if getting basic movie info for main page
        if (context instanceof MainActivity) {
            JSONArray results = reader.getJSONArray(MDB_RESULTS);
            for (int i = 0; i < results.length(); i++) {
                JSONObject movie = results.getJSONObject(i);
                String movieTitle = movie.getString(MDB_MOVIE_TITLE);
                String posterPath = movie.getString(MDB_POSTER_PATH);
                String synopsis = movie.getString(MDB_SYNOPSIS);
                String releaseDate = movie.getString(MDB_RELEASE_DATE);
                String userRating = movie.getString(MDB_USER_RATING);
                String movieId = movie.getString(MDB_MOVIE_ID);

                String posterUrl = buildPosterUrl(posterPath);

                movies.add(new MovieClass(movieTitle, posterUrl, synopsis, releaseDate, userRating, movieId));
            }
        } else { // if getting trailers and reviews for detail page //TODO can I specify MovieDetail here?
            //Getting trailer information
            JSONObject trailers = reader.getJSONObject(MOVIE_TRAILERS);
            JSONArray trailerResults = trailers.getJSONArray(MDB_RESULTS);

            int itemCounter = 0; //Counter keeps Arraylist information in the proper order
            //Show the first 3 trailers only
            for (int i = 0; i < trailerResults.length() && i < 3; i++) {
                JSONObject video = trailerResults.getJSONObject(i);
                String videoId = video.getString(MOVIE_TRAILER_ID); //unique id for the YouTube clip
                String videoName = video.getString(MOVIE_TRAILER_NAME);

                movieTrailers.add(itemCounter, videoId);
                movieTrailers.add(itemCounter+1, videoName);
                itemCounter = itemCounter + 2;


            }
            extraMovieDetails.add(movieTrailers);

            //Getting review information
            JSONObject reviews = reader.getJSONObject(MOVIE_REVIEWS);
            JSONArray reviewResults = reviews.getJSONArray(MDB_RESULTS);

            itemCounter = 0;
            for (int i = 0; i < reviewResults.length(); i++) {
                JSONObject review = reviewResults.getJSONObject(i);
                String reviewerName = review.getString(MOVIE_REVIEWER_NAME);
                String reviewText = review.getString(MOVIE_REVIEW_TEXT);
                String reviewUrl = review.getString(MOVIE_REVIEW_URL);
                movieReviews.add(itemCounter, reviewerName);
                movieReviews.add(itemCounter + 1, reviewText);
                movieReviews.add(itemCounter + 2, reviewUrl);
                itemCounter = itemCounter + 3;
            }
            extraMovieDetails.add(movieReviews);

            return extraMovieDetails;
        }

        return movies;

    }

    public static String buildPosterUrl(String posterPath) {
        String posterUrl = null;

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(IMG_SIZE)
                .build();

        URL url;
        try {
            url = new URL(builtUri.toString() + posterPath);
            posterUrl = url.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        return posterUrl;
    }
}
