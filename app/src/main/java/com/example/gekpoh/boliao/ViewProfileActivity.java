package com.example.gekpoh.boliao;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class ViewProfileActivity extends AppCompatActivity {

    private ImageView imageViewProPic;
    private TextView textViewName;
    private RatingBar ratingBar;
    private TextView textViewNumRatings;
    private TextView textViewDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.common_google_signin_btn_icon_dark);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        imageViewProPic = findViewById(R.id.imageViewProPic);
        textViewName = findViewById(R.id.textViewName);
        ratingBar = findViewById(R.id.ratingBar);
        textViewNumRatings = findViewById(R.id.textViewNumRatings);
        textViewDescription = findViewById(R.id.textViewDescription);
    }
}
