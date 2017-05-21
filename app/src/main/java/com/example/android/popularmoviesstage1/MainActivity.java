package com.example.android.popularmoviesstage1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmoviesstage1.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    ProgressBar mProgressBar;
    TextView mSearchResults; //TODO remove once RecyclerView is setup
    TextView mErrorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mSearchResults = (TextView) findViewById(R.id.tv_search_results);
        mErrorMessage = (TextView) findViewById(R.id.tv_error_message_display);
        makeMovieDbSearchQuery();

    }

    private void makeMovieDbSearchQuery() {
        URL movieDbSearchUrl = NetworkUtils.buildUrl();
        new MovieDbQuery().execute(movieDbSearchUrl);
    }


    public class MovieDbQuery extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() { mProgressBar.setVisibility(View.VISIBLE);        }

        @Override
        protected String doInBackground(URL... params) {
            URL searchUrl = params[0];
            String jsonMovieDbSearchResults = null;
            try {
                jsonMovieDbSearchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return jsonMovieDbSearchResults;
        }

        @Override
        protected void onPostExecute(String movieDbSearchResults) {
            mProgressBar.setVisibility(View.INVISIBLE);
            if (movieDbSearchResults != null && !movieDbSearchResults.equals("")) {
                mErrorMessage.setVisibility(View.INVISIBLE);
//              TODO Remove try/catch for testing only
//                try {
//                    MovieDbJsonUtils.getMovieDbStringsFromJson(MainActivity.this, movieDbSearchResults);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
            } else {
                showErrorMessage();
            }
        }
    }

    public void showErrorMessage(){
        mErrorMessage.setVisibility(View.VISIBLE);
    }
}

