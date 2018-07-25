package com.example.gekpoh.boliao;

public class UserRating {
    private float sumRating;
    private int numRatings;

    public UserRating() {
    }

    public UserRating(float sumRating, int numRatings) {
        this.sumRating = sumRating;
        this.numRatings = numRatings;
    }

    public float getSumRating() {
        return sumRating;
    }

    public void setSumRating(float sumRating) {
        this.sumRating = sumRating;
    }

    public int getNumRatings() {
        return numRatings;
    }

    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }
}
