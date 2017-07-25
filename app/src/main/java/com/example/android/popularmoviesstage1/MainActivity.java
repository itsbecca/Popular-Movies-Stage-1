package com.example.android.popularmoviesstage1;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmoviesstage1.data.FavoritesContract;
import com.example.android.popularmoviesstage1.utilities.MovieDbJsonUtils;
import com.example.android.popularmoviesstage1.utilities.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        MovieAdapter.ListItemClickListener,
        FavoritesAdapter.ListItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    ProgressBar mProgressBar;
    TextView mEmptyView;
    MovieAdapter mMovieAdapter;
    FavoritesAdapter mFavAdapter;
    Cursor mFavoritesData;

    RecyclerView mList;
    ArrayList<MovieClass> jsonMovieDbData;

    //To identify which spinner menu choice is selected currently
    String spinnerData;
    String[] spinnerArray;
    public static final int SPINNER_POPULAR_SORT = 0;
    public static final int SPINNER_RATED_SORT = 1;
    public static final int SPINNER_FAVORITES_SORT = 2;


    LoaderManager.LoaderCallbacks mLoaderCallbacks = this;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int ID_FAVORITES_LOADER = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mEmptyView = (TextView) findViewById(R.id.empty_view_main);
        mList = (RecyclerView) findViewById(R.id.recyclerview_movie);
        spinnerArray = getResources().getStringArray(R.array.movie_sort_array);

        //number of columns adjusts depending on layout
        final int columns = getResources().getInteger(R.integer.gallery_columns);
        GridLayoutManager layoutManager = new GridLayoutManager(this, columns, GridLayoutManager.VERTICAL, false);
        mList.setLayoutManager(layoutManager);
        mList.setHasFixedSize(true);

        mMovieAdapter = new MovieAdapter(this);
        mFavAdapter = new FavoritesAdapter(this, this);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new SpinnerSort());
        spinnerData = spinner.getSelectedItem().toString();

        //check for internet connection
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            makeMovieDbSearchQuery();
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
            mEmptyView.setText(R.string.offline_main);
        }
    }

    public void makeMovieDbSearchQuery() {
        URL movieDbSearchUrl = NetworkUtils.buildUrl(spinnerData);
        new MovieDbQuery().execute(movieDbSearchUrl);
    }

    public class SpinnerSort implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            spinnerData = (String) parent.getItemAtPosition(position);
            loadSpinnerData(spinnerData);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }

    public void loadSpinnerData (String spinnerData) {
        if ( spinnerData.equals(spinnerArray[SPINNER_POPULAR_SORT])||
                spinnerData.equals(spinnerArray[SPINNER_RATED_SORT])) {
            mList.setAdapter(mMovieAdapter);
            makeMovieDbSearchQuery();
        } else if (spinnerData.equals((spinnerArray[SPINNER_FAVORITES_SORT]))) {
            mList.setAdapter(mFavAdapter);
            getSupportLoaderManager().initLoader(ID_FAVORITES_LOADER, null, mLoaderCallbacks);
        }
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {

        Context context = MainActivity.this;
        Class destinationActivity = MovieDetail.class;

        Intent intent = new Intent(context, destinationActivity);
        //Data sent with intent is dependent on which sort method they clicked from
        //Popular and Rated both use API query, Favorites can be loaded offline w/ db query.
        if ( spinnerData.equals(spinnerArray[SPINNER_POPULAR_SORT])||
                spinnerData.equals(spinnerArray[SPINNER_RATED_SORT])) {
            intent.putExtra(Intent.EXTRA_TEXT, jsonMovieDbData.get(clickedItemIndex));
            intent.putExtra(getResources().getString(R.string.sort_type),SPINNER_POPULAR_SORT);
        } else if (spinnerData.equals((spinnerArray[SPINNER_FAVORITES_SORT]))){
            //retreive movieId for clicked film to send to MovieDetail
            mFavoritesData.moveToPosition(clickedItemIndex);
            String movieId = mFavoritesData.getString(mFavoritesData.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID));
            intent.putExtra(getResources().getString(R.string.current_movie_id),movieId);

            //store int that tracks if user clicked through favorites sort or not
            intent.putExtra(getResources().getString(R.string.sort_type),SPINNER_FAVORITES_SORT);
        }
        startActivity(intent);

    }

    public class MovieDbQuery extends AsyncTask<URL, Void, ArrayList<MovieClass>> {

        @Override
        protected void onPreExecute() { mProgressBar.setVisibility(View.VISIBLE);        }

        @Override
        protected ArrayList<MovieClass> doInBackground(URL... params) {
            URL searchUrl = params[0];

            try {
                String jsonMovieDbResponse = NetworkUtils.getResponseFromHttpUrl(searchUrl);
                jsonMovieDbData = MovieDbJsonUtils.getMovieDbStringsFromJson(MainActivity.this, jsonMovieDbResponse);
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
        protected void onPostExecute(ArrayList<MovieClass> movieDbSearchResults) {
            mProgressBar.setVisibility(View.INVISIBLE);

            if (movieDbSearchResults != null) {
                mEmptyView.setVisibility(View.INVISIBLE);
                mMovieAdapter.setMovieInfo(movieDbSearchResults);

            } else {
                mEmptyView.setText(R.string.no_results);
            }
        }

    }

    //For querying the sql db for Favorites
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {

        return new AsyncTaskLoader<Cursor>(this) {
            @Override
            protected void onStartLoading() {
                if (mFavoritesData != null) {
                    deliverResult(mFavoritesData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {
                try {
                    return getContentResolver().query(FavoritesContract.FavoritesEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(Cursor data) {
                mFavoritesData = data;
                super.deliverResult(data);

                //if the favorites db is empty we will return a msg informing the user
                if (data.getCount() <= 0) {
                    mEmptyView.setVisibility(View.VISIBLE);
                    mEmptyView.setText(R.string.no_favorites);
                } else mEmptyView.setVisibility(View.GONE);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {mFavAdapter.swapCursor(data);}

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { mFavAdapter.swapCursor(null); }
}

