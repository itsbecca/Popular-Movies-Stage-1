package com.example.android.popularmoviesstage1;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.popularmoviesstage1.utilities.MovieDbJsonUtils;
import com.example.android.popularmoviesstage1.utilities.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MovieAdapter.ListItemClickListener {

    ProgressBar mProgressBar;
    TextView mEmptyView;
    MovieAdapter mAdapter;
    RecyclerView mList;
    ArrayList<MovieClass> jsonMovieDbData;
    String spinnerData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mEmptyView = (TextView) findViewById(R.id.empty_view);
        mList = (RecyclerView) findViewById(R.id.recyclerview_movie);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        mList.setLayoutManager(layoutManager);
        mList.setHasFixedSize(true);

        mAdapter = new MovieAdapter(this);
        mList.setAdapter(mAdapter);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new SpinnerSort());

        spinnerData = spinner.getSelectedItem().toString();

        makeMovieDbSearchQuery();

    }

    public void makeMovieDbSearchQuery() {
        URL movieDbSearchUrl = NetworkUtils.buildUrl(spinnerData);
        new MovieDbQuery().execute(movieDbSearchUrl);
    }


    public class SpinnerSort implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            spinnerData = (String) parent.getItemAtPosition(position);
            makeMovieDbSearchQuery();
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
                mAdapter.setMovieInfo(movieDbSearchResults);
            } else {
                mEmptyView.setText(R.string.no_results);
            }
        }
    }
}

