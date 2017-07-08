package com.example.android.popularmoviesstage1;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.popularmoviesstage1.utilities.MovieDbJsonUtils;
import com.example.android.popularmoviesstage1.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class MovieDetail extends AppCompatActivity implements View.OnClickListener{

    //Views that will be filled by EXTRA sent by Intent
    TextView mMovieTitle;
    TextView mMovieSynopsis;
    TextView mMovieRating;
    TextView mReleaseDate;
    ImageView mPosterImg;

    //LinearLayout that will hold views created programmatically to hold trailers and reviews if available for movie
    LinearLayout mMainLinearLayout;

    //For forming YouTube uri
    final static String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?";
    final static String YOUTUBE_QUERY = "v";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        mMovieTitle = (TextView) findViewById(R.id.detail_movie_title);
        mMovieSynopsis = (TextView) findViewById(R.id.detail_movie_synopsis);
        mMovieRating = (TextView) findViewById(R.id.detail_movie_rating);
        mReleaseDate = (TextView) findViewById(R.id.detail_movie_release_date);
        mPosterImg = (ImageView) findViewById(R.id.detail_movie_poster);
        mMainLinearLayout = (LinearLayout) findViewById(R.id.mainLinearLayout);

        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
            MovieClass current_movie = getIntent().getParcelableExtra(Intent.EXTRA_TEXT);
            String mPosterUrl = current_movie.getPosterUrl();
            String mMovieId = current_movie.getMovieId();
            mMovieTitle.setText(current_movie.getMovieTitle());
            mMovieSynopsis.setText(current_movie.getSynopsis());
            mMovieRating.setText(current_movie.getUserRating());
            mReleaseDate.setText(current_movie.getReleaseDate());

            Picasso.with(this).load(mPosterUrl).into(mPosterImg);

            URL movieDbSearchUrl = NetworkUtils.buildDetailUrl(mMovieId);
            new MovieDbQuery().execute(movieDbSearchUrl);
        }


    }

    @Override
    public void onClick(View view) {
        //retrieve videoId from view and use it to build video Uri
        String videoId = (String) view.getTag();
        Uri trailerUri = buildTrailerUrl(videoId);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(trailerUri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


    public class MovieDbQuery extends AsyncTask<URL, Void, ArrayList<String>> {

        @Override
        protected void onPreExecute() {
            //    mProgressBar.setVisibility(View.VISIBLE); TODO: implement progress bar in this activity
        }

        @Override
        protected ArrayList<String> doInBackground(URL... params) {
            URL searchUrl = params[0];

            try {
                String jsonMovieDbResponse = NetworkUtils.getResponseFromHttpUrl(searchUrl);
                ArrayList jsonMovieDbData = MovieDbJsonUtils.getMovieDbStringsFromJson(MovieDetail.this, jsonMovieDbResponse);
                return jsonMovieDbData;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<String> movieDbSearchResults) {
//            mProgressBar.setVisibility(View.INVISIBLE);
//
//            if (movieDbSearchResults != null) {
//                mEmptyView.setVisibility(View.INVISIBLE);
//                mAdapter.setMovieInfo(movieDbSearchResults);
//            } else {
//                mEmptyView.setText(R.string.no_results);
//            }
            if (movieDbSearchResults != null) {
                //Creates views for the first three trailers
                for (int i = 0; i < movieDbSearchResults.size() && i < 3; i++) {
                    String movieId = movieDbSearchResults.get(i);

                    TextView trailerText = new TextView(MovieDetail.this);
                    mMainLinearLayout.addView(trailerText);
                    trailerText.setTag(movieId);
                    trailerText.setText("Click to play trailer " + (i+1));
                    trailerText.setId(R.string.trailer_views + i); //TODO do they need an id?
                    trailerText.setOnClickListener(MovieDetail.this);
                }

                //Create views for the first 3 reviews
            }
        }

    }
    public Uri buildTrailerUrl(String videoId) {
        Uri builtUri = Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                .appendQueryParameter(YOUTUBE_QUERY, videoId)
                .build();

        return builtUri;
    }
}
