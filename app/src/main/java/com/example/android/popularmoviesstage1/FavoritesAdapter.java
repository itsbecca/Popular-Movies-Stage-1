package com.example.android.popularmoviesstage1;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmoviesstage1.data.FavoritesContract;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesAdapterViewHolder> {

    private Cursor mCursor;
    private Context mContext;
    private final ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    public FavoritesAdapter(Context context, FavoritesAdapter.ListItemClickListener clickListener) {
        mContext = context;
        mOnClickListener = clickListener;
    }

    public FavoritesAdapter(Context context) {
        mContext = context;
        mOnClickListener = null;
    }

    @Override
    public FavoritesAdapterViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.list_item, parent, false);

        return new FavoritesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FavoritesAdapterViewHolder favoritesAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);
        String imgPath = mCursor.getString(mCursor
                .getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_POSTER_LOC));
        String test = mCursor.getString(mCursor
                .getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID));
        String test2 = mCursor.getString(mCursor
                .getColumnIndex(FavoritesContract.FavoritesEntry._ID));
        String test3 = mCursor.getString(mCursor
                .getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_TITLE));
        String test4 = mCursor.getString(mCursor
                .getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_RELEASE_DATE));
        Picasso.with(mContext).load(R.mipmap.ic_launcher_round).into(favoritesAdapterViewHolder.mMoviePosterView);
        //Picasso.with(mContext).load(new File(imgPath)).into(favoritesAdapterViewHolder.mMoviePosterView);
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

    public class FavoritesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView mMoviePosterView;

        public FavoritesAdapterViewHolder(View view) {
            super(view);
            mMoviePosterView = (ImageView) view.findViewById(R.id.movie_poster);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickPosition);
        }
    }
}
