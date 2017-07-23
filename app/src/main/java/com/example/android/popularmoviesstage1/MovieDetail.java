package com.example.android.popularmoviesstage1;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmoviesstage1.data.FavoritesContract;
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

public class MovieDetail extends AppCompatActivity implements
        View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor>{

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

    //vars related to loading favorites from db
    FavoritesAdapter mFavAdapter;
    private static final int ID_FAVORITES_LOADER = 42;
    private static final String TAG = MovieDetail.class.getSimpleName();

    //String Arrays to keep projected columns and their matching indices to access the returned data
    public static final String[] MOVIE_DETAIL_PROJECTION = {
            FavoritesEntry.COLUMN_MOVIE_TITLE,
            FavoritesEntry.COLUMN_MOVIE_RATING,
            FavoritesEntry.COLUMN_MOVIE_RELEASE_DATE,
            FavoritesEntry.COLUMN_MOVIE_SYNOPSIS,
            FavoritesEntry.COLUMN_MOVIE_POSTER_LOC
    };

    public static final int INDEX_FAVORITES_TITLE = 0;
    public static final int INDEX_FAVORITES_RATING = 1;
    public static final int INDEX_FAVORITES_RELEASE_DATE = 2;
    public static final int INDEX_FAVORITES_SYNOPSIS = 3;
    public static final int INDEX_FAVORITES_POSTER_LOC = 4;

    //TODO setup runtime permission writing to external storage to support 23APK and above
    //https://developer.android.com/training/permissions/requesting.html

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

        mFavoritesBtn = (Button) findViewById(R.id.add_favorites_button); //TODO want this btn to change if movie is favorited
        mFavoritesBtn.setTag(mFavoritesBtnTag);
        mFavoritesBtn.setOnClickListener(MovieDetail.this); //todo if movie is favorited, clicking should delete from favorites db

        mFavAdapter = new FavoritesAdapter(this);

        Bundle extras = getIntent().getExtras();
        int favoritesOrNot = extras.getInt(getResources().getString(R.string.sort_type));
        mMovieId = extras.getString(getResources().getString(R.string.current_movie_id));

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (favoritesOrNot == MainActivity.SPINNER_POPULAR_SORT || favoritesOrNot == MainActivity.SPINNER_RATED_SORT) {
            if(networkInfo != null && networkInfo.isConnected()) {
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
            } else return; //mEmptyView.setText(R.string.no_internet_connection); //TODO Setup EmptyView in xml
        } else if (favoritesOrNot == MainActivity.SPINNER_FAVORITES_SORT) {
            getSupportLoaderManager().initLoader(ID_FAVORITES_LOADER, null, this);
            //TODO Do I have any network based decisions to make for Favorites view?//
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

        Picasso.with(this).load(mPosterUrl).error(R.drawable.image_error).into(target);
        movieDetails.put(FavoritesEntry.COLUMN_MOVIE_POSTER_LOC, mPosterPath);

        Uri uri = getContentResolver().insert(FavoritesEntry.CONTENT_URI,movieDetails);

        if(uri != null) {
            Toast.makeText(getBaseContext(),
                    String.valueOf(mMovieTitle.getText()) + " " + getString(R.string.saved_to_favorites),
                    Toast.LENGTH_LONG).show();
        }
    }

    //saves poster into a given folder and saves path to be stored in db
    private Target target = new Target() { //TODO Can this be moved out to a util doc?
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            final File file = new File(
                    Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/",
                            mMovieId + ".jpg");
            mPosterPath = String.valueOf(file);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        file.createNewFile();
                        FileOutputStream outputStream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                        outputStream.flush();
                        outputStream.close();
                    }
                    catch (IOException e) {
                        Log.e(TAG, "IOException",e.getCause());
                    }
                }
            }).start();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {}

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}
    };

    //For querying the sql db for Favorites
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {

        Uri movieUri = FavoritesEntry.buildUriWithMovieId(mMovieId);

        switch (loaderId) {
            case ID_FAVORITES_LOADER:
                return new CursorLoader(this,
                        movieUri,
                        MOVIE_DETAIL_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mFavAdapter.swapCursor(data);

        //Check if data is valid before binding data to UI
        boolean cursorHasValidData = false;

        if (data != null && data.moveToFirst()) {
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            return;
        }

        String posterPath = data.getString(INDEX_FAVORITES_POSTER_LOC);
        Picasso.with(this).load(new File(posterPath)).into(mPosterImg);

        mMovieTitle.setText(data.getString(INDEX_FAVORITES_TITLE));
        mMovieRating.setText(data.getString(INDEX_FAVORITES_RATING));
        mReleaseDate.setText(data.getString(INDEX_FAVORITES_RELEASE_DATE));
        mMovieSynopsis.setText(data.getString(INDEX_FAVORITES_SYNOPSIS));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { mFavAdapter.swapCursor(null); }

    //Requesting permissions to read/write
    public void permissionRequest () {
//        if (ContextCompat.checkSelfPermission(thisActivity,
//                Manifest.permission.READ_CONTACTS)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
//                    Manifest.permission.READ_CONTACTS)) {
//
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else {
//
//                // No explanation needed, we can request the permission.
//
//                ActivityCompat.requestPermissions(thisActivity,
//                        new String[]{Manifest.permission.READ_CONTACTS},
//                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
//
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//            }
//        }
    }
}
