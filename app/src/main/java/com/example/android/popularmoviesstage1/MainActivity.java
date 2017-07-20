package com.example.android.popularmoviesstage1;

import android.app.LauncherActivity;
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

    RecyclerView mList;
    ArrayList<MovieClass> jsonMovieDbData;
    String spinnerData;

    LoaderManager.LoaderCallbacks mLoaderCallbacks = this; //TODO Do I have to use this

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int ID_FAVORITES_LOADER = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mEmptyView = (TextView) findViewById(R.id.empty_view);
        mList = (RecyclerView) findViewById(R.id.recyclerview_movie);

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
        if(networkInfo != null && networkInfo.isConnected()) { //todo pull out most of this, need to setup layout w/o connection too
            makeMovieDbSearchQuery();
        } else {
            mEmptyView.setText(R.string.no_internet_connection); //TODO Change to indicate can only see favs when offline
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
            String[] spinnerArray = getResources().getStringArray(R.array.movie_sort_array);

            if (spinnerData.equals(spinnerArray[0]) || spinnerData.equals(spinnerArray[1])) { //TODO do I need to get rid of these nums?
                mList.setAdapter(mMovieAdapter);
                makeMovieDbSearchQuery();
            } else {
                mList.setAdapter(mFavAdapter);
                getSupportLoaderManager().initLoader(ID_FAVORITES_LOADER, null, mLoaderCallbacks);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    }
    @Override
    public void onListItemClick(int clickedItemIndex) {

        Context context = MainActivity.this;
        Class destinationActivity = MovieDetail.class;

        Intent intent = new Intent(context,destinationActivity);
        intent.putExtra(Intent.EXTRA_TEXT, jsonMovieDbData.get(clickedItemIndex));
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

            Cursor mFavoritesData = null;

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
                            FavoritesContract.FavoritesEntry.COLUMN_MOVIE_POSTER_LOC,
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
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mFavAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

