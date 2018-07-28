package com.example.gekpoh.boliao;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

public class HelpActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.logowhitesmall);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addSlide(SampleSlide.newInstance(R.layout.helpslide2));
        addSlide(SampleSlide.newInstance(R.layout.helpslide3));
        addSlide(SampleSlide.newInstance(R.layout.helpslide4));
        addSlide(SampleSlide.newInstance(R.layout.helpslide5));
        addSlide(SampleSlide.newInstance(R.layout.helpslide6));
        addSlide(SampleSlide.newInstance(R.layout.helpslide7));
        addSlide(SampleSlide.newInstance(R.layout.helpslide1));
        addSlide(SampleSlide.newInstance(R.layout.helpslide8));

        showSkipButton(true);
        setProgressButtonEnabled(true);
        setFadeAnimation();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
    }
}
