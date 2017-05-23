package com.example.android.popularmoviesstage1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieDetail extends AppCompatActivity {

    TextView mMovieTitle;
    TextView mMovieSynopsis;
    TextView mMovieRating;
    TextView mReleaseDate;
    ImageView mPosterImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        mMovieTitle = (TextView) findViewById(R.id.detail_movie_title);
        mMovieSynopsis = (TextView) findViewById(R.id.detail_movie_synopsis);
        mMovieRating = (TextView) findViewById(R.id.detail_movie_rating);
        mReleaseDate = (TextView) findViewById(R.id.detail_movie_release_date);
        mPosterImg = (ImageView) findViewById(R.id.detail_movie_poster);

        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
            MovieClass current_movie = getIntent().getParcelableExtra(Intent.EXTRA_TEXT);
            String mPosterUrl = current_movie.getPosterUrl();
            mMovieTitle.setText(current_movie.getMovieTitle());
            mMovieSynopsis.setText(current_movie.getSynopsis());
            mMovieRating.setText(current_movie.getUserRating());
            mReleaseDate.setText(current_movie.getReleaseDate());

            Picasso.with(this).load(mPosterUrl).into(mPosterImg);
        }
    }
}
