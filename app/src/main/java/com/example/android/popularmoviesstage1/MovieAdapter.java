package com.example.android.popularmoviesstage1;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private ArrayList<MovieClass> mMovieInfo;
    private View view;
    private final ListItemClickListener mOnClickListener;

    public MovieAdapter(ListItemClickListener clickListener) {
        mOnClickListener = clickListener;
    }

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        MovieAdapterViewHolder viewHolder = new MovieAdapterViewHolder(view);

       return viewHolder;
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder movieAdapterViewHolder, int position) {
        String currentMovieInfo = String.valueOf(mMovieInfo.get(position).getPosterUrl());
        Picasso.with(view.getContext()).load(currentMovieInfo).into(movieAdapterViewHolder.mMoviewPosterView);
    }

    @Override
    public int getItemCount() {
        if (mMovieInfo == null) {
            return 0;
        }

        return mMovieInfo.size();
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mMoviewPosterView;

        public MovieAdapterViewHolder(View view) {
            super(view);
            mMoviewPosterView = (ImageView) view.findViewById(R.id.movie_poster);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickPosition = getAdapterPosition();
            //MovieClass clickedMovie = mMovieInfo.get(clickPosition);
            mOnClickListener.onListItemClick(clickPosition);
        }
    }

    public void setMovieInfo(ArrayList<MovieClass> movieInfo) {
        mMovieInfo = movieInfo;
        notifyDataSetChanged();
    }
}
