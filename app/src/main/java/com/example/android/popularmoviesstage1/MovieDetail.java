package com.example.android.popularmoviesstage1;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmoviesstage1.utilities.MovieDbJsonUtils;
import com.example.android.popularmoviesstage1.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import static com.example.android.popularmoviesstage1.data.FavoritesContract.*;

public class MovieDetail extends AppCompatActivity implements View.OnClickListener{

    //Views that will be filled by EXTRA sent by Intent
    TextView mMovieTitle;
    TextView mMovieSynopsis;
    TextView mMovieRating;
    TextView mReleaseDate;
    ImageView mPosterImg;

    //For holding movie info pulled from intent not used in a view
    String mPosterUrl;
    String mMovieId;
    String mPosterPath;

    Button mFavoritesBtn;
    // to identify btn click
    String mFavoritesBtnTag = "add_favorites";

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

        mFavoritesBtn = (Button) findViewById(R.id.add_favorites_button);
        mFavoritesBtn.setTag(mFavoritesBtnTag);
        mFavoritesBtn.setOnClickListener(MovieDetail.this);

        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
            MovieClass current_movie = getIntent().getParcelableExtra(Intent.EXTRA_TEXT);
            mPosterUrl = current_movie.getPosterUrl();
            mMovieId = current_movie.getMovieId();
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
        String viewTag = (String) view.getTag();

        if (viewTag.equals(mFavoritesBtnTag)) {
            addMovieToFavorites();

        } else if (viewTag != null){
            //retrieve videoId from view and use it to build video Uri
            String videoId = viewTag;
            Uri trailerUri = buildTrailerUrl(videoId);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(trailerUri);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    //New API call with current movie idto get trailers and reviews that are not avl in MainActivity
    //API call
    public class MovieDbQuery extends AsyncTask<URL, Void, ArrayList<ArrayList<String>>> {

        @Override
        protected void onPreExecute() {
            //    mProgressBar.setVisibility(View.VISIBLE); TODO: implement progress bar in this activity
        }

        @Override
        protected ArrayList<ArrayList<String>> doInBackground(URL... params) {
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
        protected void onPostExecute(ArrayList<ArrayList<String>> movieDbSearchResults) {
//            mProgressBar.setVisibility(View.INVISIBLE);
//
//            if (movieDbSearchResults != null) {
//                mEmptyView.setVisibility(View.INVISIBLE);
//                mMovieAdapter.setMovieInfo(movieDbSearchResults);
//            } else {
//                mEmptyView.setText(R.string.no_results);
//            }
            if (movieDbSearchResults != null) {
                //separate results into the two relevant array lists: Trailers and Reviews
                ArrayList<String> trailerResults = movieDbSearchResults.get(0);
                ArrayList<String> reviewResults = movieDbSearchResults.get(1);

                for (int i = 0; i < movieDbSearchResults.size(); i++) {
                    //Creates views for the first three trailers
                    if(i==0) {
                        for (int f = 0; f < trailerResults.size(); f = f+2) {
                            String videoId = trailerResults.get(f);
                            String videoTitle = trailerResults.get(f+1);

                            TextView trailerTextView = new TextView(MovieDetail.this);
                            mMainLinearLayout.addView(trailerTextView);
                            trailerTextView.setTag(videoId);
                            trailerTextView.setText("Click to play " + videoTitle);
                            //trailerTextView.setId(R.string.trailer_views + f); //TODO do they need an id?
                            trailerTextView.setOnClickListener(MovieDetail.this);
                        }
                    }

                    //Create views for the reviews
                    if(i==1) {
                        for (int g = 0; g < reviewResults.size(); g = g+3) {
                            String reviewerName = reviewResults.get(g);
                            String reviewText = reviewResults.get(g+1);
                            String reviewUrl = reviewResults.get(g+2);

                            TextView reviewTextView = new TextView(MovieDetail.this);
                            mMainLinearLayout.addView(reviewTextView);
                            reviewTextView.setTag(reviewUrl);
                            reviewTextView.setText("Reviewer: " + reviewerName +
                                    "\n\n" + reviewText);
                            reviewTextView.setOnClickListener(MovieDetail.this); //TODO adjust onClick method if I would like to use this
                        }

                    }

                }
            }
        }

    }

    public Uri buildTrailerUrl(String videoId) {
        Uri builtUri = Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                .appendQueryParameter(YOUTUBE_QUERY, videoId)
                .build();

        return builtUri;
    }

    public void addMovieToFavorites() {
        //Pull information from Views to store in ContentValues
        ContentValues movieDetails = new ContentValues();
        movieDetails.put(FavoritesEntry.COLUMN_MOVIE_TITLE,String.valueOf(mMovieTitle.getText()));
        movieDetails.put(FavoritesEntry.COLUMN_MOVIE_ID,mMovieId);
        movieDetails.put(FavoritesEntry.COLUMN_MOVIE_RATING,String.valueOf(mMovieRating.getText()));
        movieDetails.put(FavoritesEntry.COLUMN_MOVIE_RELEASE_DATE,String.valueOf(mReleaseDate.getText()));
        movieDetails.put(FavoritesEntry.COLUMN_MOVIE_SYNOPSIS,String.valueOf(mMovieSynopsis.getText()));

        Picasso.with(this).load(mPosterUrl).into(target);
        movieDetails.put(FavoritesEntry.COLUMN_MOVIE_POSTER_LOC, mPosterPath);

        Uri uri = getContentResolver().insert(FavoritesEntry.CONTENT_URI,movieDetails);

        if(uri != null) {
            Toast.makeText(getBaseContext(),
                    String.valueOf(mMovieTitle.getText()) + " saved to favorites", //TODO I'm a string, remove me
                    Toast.LENGTH_LONG).show();
        }
    }

    //saves poster into a given folder and saves path to be stored in db
    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            final File file = new File(
                    Environment.getExternalStorageDirectory().getPath()
                            + "/moviePosters/" + mMovieId + ".jpg");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        file.createNewFile();
                        FileOutputStream ostream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,ostream);
                        ostream.close();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            mPosterPath = String.valueOf(file);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {}

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}
    };

    private String savePoster(String posterUrl) {










//        File test = getCacheDir();
//        Drawable drawable = mPosterImg.getDrawable();
//        String uriString = "com.squareup.picasso.PicassoDrawable@151558c";
//        InputStream inputStream = null;
//        try {
//            inputStream = getContentResolver().openInputStream(Uri.parse(uriString));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        drawable = Drawable.createFromStream(inputStream, uriString);
//        Bitmap bitmap = BitmapFactory.decodeFile(uri);
//        mPosterImg.setImageBitmap(BitmapFactory.decodeFile(uri));
//        mPosterImg.invalidate();
//        Picasso.with(this).load(uri).into(mPosterImg);
//        mPosterImg.invalidate();
        String other = "stuff";
        return other;
    }
}
