package com.example.android.popularmoviesstage1;

public class MovieClass {
    private String posterUrl;
    private String synopsis;
    private String releaseDate;
    private String userRating;

    public MovieClass(String posterUrl, String synopsis, String releaseDate, String userRating) {
        this.posterUrl = posterUrl;
        this.synopsis = synopsis;
        this.releaseDate = releaseDate;
        this.userRating = userRating;
    }

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
}
