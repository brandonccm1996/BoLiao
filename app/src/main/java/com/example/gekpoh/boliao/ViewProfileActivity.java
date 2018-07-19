package com.example.gekpoh.boliao;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

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

        textViewName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        textViewDescription.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        if(getIntent().hasExtra("memberName") && getIntent().hasExtra("memberDesc") && getIntent().hasExtra("memberSumRating") && getIntent().hasExtra("memberNumRatings") && getIntent().hasExtra("memberProPic")){
            textViewName.setText(getIntent().getStringExtra("memberName"));
            textViewDescription.setText(getIntent().getStringExtra("memberDesc"));
            textViewNumRatings.setText("Number of ratings: " + getIntent().getIntExtra("memberNumRatings", -1));

            if (getIntent().getIntExtra("memberNumRatings", -1) == 0) ratingBar.setRating(0);
            else ratingBar.setRating(getIntent().getFloatExtra("memberSumRating", -1) / getIntent().getIntExtra("memberNumRatings", -1));

            if (getIntent().getStringExtra("memberProPic").equals("")) {
                Glide.with(imageViewProPic.getContext())
                        .load(R.drawable.profilepic)
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageViewProPic);
            }
            else {
                Glide.with(imageViewProPic.getContext())
                        .load(getIntent().getStringExtra("memberProPic"))
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageViewProPic);
            }
        }
    }
}
