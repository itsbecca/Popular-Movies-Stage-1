package com.example.android.popularmoviesstage1;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private ArrayList<MovieClass> mMovieInfo;

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        MovieAdapterViewHolder viewHolder = new MovieAdapterViewHolder(view);

       return viewHolder;
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder movieAdapterViewHolder, int position) {
        String currentMovieInfo = String.valueOf(mMovieInfo);
        movieAdapterViewHolder.mMovieTextView.setText(currentMovieInfo);
    }

    @Override
    public int getItemCount() {
        if (mMovieInfo == null) {
            return 0;
        }

        return mMovieInfo.size();
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder {

        public final TextView mMovieTextView;
        public MovieAdapterViewHolder(View view) {
            super(view);
            mMovieTextView = (TextView) view.findViewById(R.id.movie_data);
        }

    }

    public void setMovieInfo(ArrayList<MovieClass> movieInfo) {
        mMovieInfo = movieInfo;
        notifyDataSetChanged();
    }
}
