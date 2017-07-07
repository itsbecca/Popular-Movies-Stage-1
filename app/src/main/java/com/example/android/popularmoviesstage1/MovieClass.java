package com.example.android.popularmoviesstage1;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieClass implements Parcelable {
    private String movieTitle;
    private String posterUrl;
    private String synopsis;
    private String releaseDate;
    private String userRating;
    private String movieId;

    public MovieClass(String movieTitle, String posterUrl, String synopsis, String releaseDate, String userRating, String movieId) {
        this.movieTitle = movieTitle;
        this.posterUrl = posterUrl;
        this.synopsis = synopsis;
        this.releaseDate = releaseDate;
        this.userRating = userRating;
        this.movieId = movieId;
    }


    protected MovieClass(Parcel in) {
        movieTitle = in.readString();
        posterUrl = in.readString();
        synopsis = in.readString();
        releaseDate = in.readString();
        userRating = in.readString();
        movieId = in.readString();
    }

    public static final Creator<MovieClass> CREATOR = new Creator<MovieClass>() {
        @Override
        public MovieClass createFromParcel(Parcel in) {
            return new MovieClass(in);
        }

        @Override
        public MovieClass[] newArray(int size) {
            return new MovieClass[size];
        }
    };

    public String getMovieTitle() { return movieTitle; }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getUserRating() {
        return userRating;
    }

    public String getMovieId() { return movieId; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(movieTitle);
        dest.writeString(posterUrl);
        dest.writeString(synopsis);
        dest.writeString(releaseDate);
        dest.writeString(userRating);
        dest.writeString(movieId);
    }
}
