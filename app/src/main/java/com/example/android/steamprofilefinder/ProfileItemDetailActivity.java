package com.example.android.steamprofilefinder;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.widget.ImageView;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.InputStream;

import com.example.android.steamprofilefinder.utils.SteamUtils;
import com.example.android.steamprofilefinder.utils.NetworkUtils;


import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;

/**
 * Created by Daniel Goh on 6/6/2017.
 */

public class ProfileItemDetailActivity extends AppCompatActivity {
    private static final String TAG = ProfileItemDetailActivity.class.getSimpleName();

    private TextView mProfileItemTV;
    private TextView mPersonaNameTV;
    private TextView mPersonaStateTV;
    private TextView mRealNameTV;
    private TextView mLastLogOffTV;
    private ImageView mAvatarIV;

    private ProgressBar mLoadingIndicatorPB;
    private TextView mLoadingErrorMessageTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_item_detail);

        mProfileItemTV = (TextView)findViewById(R.id.tv_profile_item);
        mPersonaNameTV = (TextView)findViewById(R.id.tv_persona_name);
        mPersonaStateTV = (TextView)findViewById(R.id.tv_persona_state);
        mRealNameTV = (TextView)findViewById(R.id.tv_real_name);
        mLastLogOffTV = (TextView)findViewById(R.id.tv_log_off_time);

        mLoadingIndicatorPB = (ProgressBar)findViewById(R.id.pb_loading_indicator);
        mLoadingErrorMessageTV = (TextView)findViewById(R.id.tv_loading_error_message);
        mAvatarIV = (ImageView)findViewById(R.id.iv_avatar);


        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(SteamUtils.ProfileItem.EXTRA_PROFILE_ITEM)) {
            SteamUtils.ProfileItem profileItem = (SteamUtils.ProfileItem)intent.getSerializableExtra(SteamUtils.ProfileItem.EXTRA_PROFILE_ITEM);
            getPlayerSummaries(profileItem.steamid);
        }


    }

    private void getPlayerSummaries(String steamid) {
        String SteamIDURL = SteamUtils.buildUserProfileURL(steamid);
        Log.d(TAG, "got search url: " + SteamIDURL);
        new SteamIDSearchTask().execute(SteamIDURL);
        //new DownloadImageTask().execute(avatarImageURL);
    }

    public class SteamIDSearchTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicatorPB.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String SteamIDURL = params[0];
            String searchResults = null;
            try {
                searchResults = NetworkUtils.doHTTPGet(SteamIDURL);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return searchResults;
        }

        @Override
        protected void onPostExecute(String s) {
            mLoadingIndicatorPB.setVisibility(View.INVISIBLE);
            if (s != null) {
                mLoadingErrorMessageTV.setVisibility(View.INVISIBLE);
                SteamUtils.SteamIDItem searchResultsList = SteamUtils.parseSteamIDJSON(s);

                if (searchResultsList.imgURL != null) {
                    new DownloadImageTask((ImageView) findViewById(R.id.iv_avatar))
                            .execute(searchResultsList.imgURL);
                }

                if (searchResultsList.personaname != null) {
                    //Log.d(TAG, "onPostExecute: " + searchResultsList.personaname);
                    mPersonaNameTV.setText(searchResultsList.personaname);
                } else {
                    mPersonaNameTV.setText("Nameless");
                }

                if (searchResultsList.profilestate.equals("0")) {
                    mPersonaStateTV.setText("OFFLINE");
                } else if (searchResultsList.profilestate.equals("1")) {
                    mPersonaStateTV.setText("ONLINE");
                } else if (searchResultsList.profilestate.equals("2")) {
                    mPersonaStateTV.setText("BUSY");
                } else if (searchResultsList.profilestate.equals("3")) {
                    mPersonaStateTV.setText("AWAY");
                } else if (searchResultsList.profilestate.equals("4")) {
                    mPersonaStateTV.setText("SNOOZE");
                } else if (searchResultsList.profilestate.equals("5")) {
                    mPersonaStateTV.setText("LOOKING TO TRADE");
                } else if (searchResultsList.profilestate.equals("6")) {
                    mPersonaStateTV.setText("LOOKING TO PLAY");
                }

                mRealNameTV.setText(searchResultsList.realname);
                mLastLogOffTV.setText(searchResultsList.lastlogoff);
            } else {
                mLoadingErrorMessageTV.setVisibility(View.VISIBLE);
            }
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;

            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            mAvatarIV.setImageBitmap(result);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return(super.onOptionsItemSelected(item));
    }

}
