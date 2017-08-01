package com.hitanshudhawan.popcorn.movies;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.solver.widgets.ConstraintAnchor;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hitanshudhawan.popcorn.R;
import com.hitanshudhawan.popcorn.adapters.MoviesBigAdapter;
import com.hitanshudhawan.popcorn.network.ApiClient;
import com.hitanshudhawan.popcorn.network.ApiInterface;
import com.hitanshudhawan.popcorn.network.movies.Genre;
import com.hitanshudhawan.popcorn.network.movies.Movie;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailActivity extends AppCompatActivity {

    private int mMovieId;

    private ConstraintLayout mMovieTabLayout;

    private ImageView mPosterImageView;
    private int mPosterHeight;
    private int mPosterWidth;
    private AVLoadingIndicatorView mPosterProgressBar;
    private ImageView mBackdropImageView;
    private int mBackdropHeight;
    private int mBackdropWidth;
    private AVLoadingIndicatorView mBackdropProgressBar;
    private TextView mTitleTextView;
    private TextView mGenreTextView;
    private TextView mYearTextView;

    private TextView mOverviewTextView;
    private LinearLayout mReleaseAndRuntimeTextLayout;
    private TextView mReleaseAndRuntimeTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(""); //todo

        Intent intent = getIntent();
        mMovieId = intent.getIntExtra("movie_id",-1);

        mPosterWidth = (int)(getResources().getDisplayMetrics().widthPixels * 0.25);
        mPosterHeight = (int)(mPosterWidth/0.66);
        mBackdropWidth = getResources().getDisplayMetrics().widthPixels;
        mBackdropHeight = (int)(mBackdropWidth / 1.77);

        mMovieTabLayout = (ConstraintLayout) findViewById(R.id.layout_toolbar_movie);
        mMovieTabLayout.getLayoutParams().height = mBackdropHeight + (int)(mPosterHeight * 0.9);

        mPosterImageView = (ImageView) findViewById(R.id.image_view_poster);
        mPosterImageView.getLayoutParams().width = mPosterWidth;
        mPosterImageView.getLayoutParams().height = mPosterHeight;
        mPosterProgressBar = (AVLoadingIndicatorView) findViewById(R.id.progress_bar_poster);

        mBackdropImageView = (ImageView) findViewById(R.id.image_view_backdrop);
        mBackdropImageView.getLayoutParams().height = mBackdropHeight;
        mBackdropProgressBar = (AVLoadingIndicatorView) findViewById(R.id.progress_bar_backdrop);

        mTitleTextView = (TextView) findViewById(R.id.textview_title_movie_detail);
        mGenreTextView = (TextView) findViewById(R.id.textview_genre_movie_detail);
        mYearTextView = (TextView) findViewById(R.id.textview_year_movie_detail);

        mOverviewTextView = (TextView) findViewById(R.id.textview_overview_movie_detail);
        mReleaseAndRuntimeTextLayout = (LinearLayout) findViewById(R.id.layout_release_and_runtime_movie_detail);
        mReleaseAndRuntimeTextView = (TextView) findViewById(R.id.textview_release_and_runtime_movie_detail);

        loadActivity();
    }

    private void loadActivity() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<Movie> call = apiService.getMovieDetails(mMovieId, getResources().getString(R.string.MOVIE_DB_API_KEY));
        call.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if(response.code() != 200) return;

                Glide.with(getApplicationContext()).load("https://image.tmdb.org/t/p/w1000/" + response.body().getPosterPath())
                        .asBitmap()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(new RequestListener<String, Bitmap>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                mPosterProgressBar.hide();
                                return false;
                            }
                            @Override
                            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                mPosterProgressBar.hide();
                                return false;
                            }
                        })
                        .into(mPosterImageView);

                Glide.with(getApplicationContext()).load("https://image.tmdb.org/t/p/w1000/" + response.body().getBackdropPath())
                        .asBitmap()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(new RequestListener<String, Bitmap>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                mBackdropProgressBar.hide();
                                return false;
                            }
                            @Override
                            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                mBackdropProgressBar.hide();
                                return false;
                            }
                        })
                        .into(mBackdropImageView);

                mTitleTextView.setText(response.body().getTitle());

                setGenres(mMovieId, response.body().getGenres());

                setYear(mMovieId, response.body().getReleaseDate());

                mOverviewTextView.setText(response.body().getOverview());

                mReleaseAndRuntimeTextLayout.setVisibility(View.VISIBLE);
                String releaseAndRuntimeString = "";
                if(response.body().getReleaseDate() != null) {
                    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat sdf2 = new SimpleDateFormat("MMM d, yyyy");
                    try {
                        Date releaseDate = sdf1.parse(response.body().getReleaseDate());
                        releaseAndRuntimeString += sdf2.format(releaseDate) + "\n";
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    releaseAndRuntimeString = "-\n";
                }
                if(response.body().getRuntime() != 0) {
                    releaseAndRuntimeString += response.body().getRuntime() + " mins";
                }
                else {
                    releaseAndRuntimeString += "-";
                }
                mReleaseAndRuntimeTextView.setText(releaseAndRuntimeString);


            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {

            }
        });
    }

    private void setGenres(Integer movieId, List<Genre> genresList) {
        String genres = "";
        for (int i=0;i<genresList.size();i++) {
            if(i == genresList.size()-1) {
                genres = genres.concat(genresList.get(i).getGenreName());
            }
            else {
                genres = genres.concat(genresList.get(i).getGenreName()+", ");
            }
        }
        mGenreTextView.setText(genres);
    }

    private void setYear(Integer movieId, String releaseDateString) {
        if(releaseDateString != null) {
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy");
            try {
                Date releaseDate = sdf1.parse(releaseDateString);
                mYearTextView.setText(sdf2.format(releaseDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
