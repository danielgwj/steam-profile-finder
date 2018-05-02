package com.example.android.steamprofilefinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.steamprofilefinder.utils.NetworkUtils;
import com.example.android.steamprofilefinder.utils.SteamUtils;
import com.example.android.steamprofilefinder.data.SteamPreferences;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ProfileAdapter.OnProfileItemClickListener, LoaderManager.LoaderCallbacks<String>, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String PROFILE_URL_KEY = "profileUrl";
    private static final int PROFILE_LOADER_ID = 0;

    private EditText mSearchBoxET;
    private RecyclerView mProfileItemsRV;
    private ProgressBar mLoadingIndicatorPB;
    private TextView mLoadingErrorMessageTV;
    private ProfileAdapter mProfileAdapter;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private String name;

    ArrayList<SteamUtils.ProfileItem> profileItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profileItems = null;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                Log.d(TAG, "Preference Changed!");
                callLoader("restartLoader", mProfileItemsRV);
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(listener);

        // Remove shadow under action bar.
        getSupportActionBar().setElevation(0);

        mSearchBoxET = (EditText)findViewById(R.id.et_search_box);
        mLoadingIndicatorPB = (ProgressBar)findViewById(R.id.pb_loading_indicator);
        mLoadingErrorMessageTV = (TextView)findViewById(R.id.tv_loading_error_message);
        mProfileItemsRV = (RecyclerView)findViewById(R.id.rv_profile_items);

        mProfileAdapter = new ProfileAdapter(this);
        mProfileItemsRV.setAdapter(mProfileAdapter);
        mProfileItemsRV.setLayoutManager(new LinearLayoutManager(this));
        mProfileItemsRV.setHasFixedSize(true);

        if(savedInstanceState != null) {
            Log.d(TAG, "Delivering cached results");
            mProfileAdapter.updateProfileItems(profileItems);
        } else {
            callLoader("initLoader", mProfileItemsRV);
        }
    }

    public void callLoader(final String loadType, RecyclerView mProfileItemsRV){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String color = sharedPreferences.getString(getString(R.string.pref_color_key), getString(R.string.pref_color_default));

        SteamPreferences.changeDefaultColor(color);
        SteamPreferences.setDefaultColor(mProfileItemsRV);

        Button searchButton = (Button)findViewById(R.id.btn_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = mSearchBoxET.getText().toString();
                name = searchQuery;
                if (!TextUtils.isEmpty(searchQuery)) {
                    doSteamSearch(searchQuery, "restartLoader");
                }
            }
        });
    }

    private void doSteamSearch(String searchQuery, String loadType) {
        String profileURL = SteamUtils.buildProfileURL(searchQuery);
        Bundle argsBundle = new Bundle();
        argsBundle.putString(PROFILE_URL_KEY, profileURL);

        if(loadType.equals("initLoader")){
            Log.d(TAG, "initLoader");
            getSupportLoaderManager().initLoader(PROFILE_LOADER_ID, argsBundle, this);
        } else {
            Log.d(TAG, "restartLoader");
            getSupportLoaderManager().restartLoader(PROFILE_LOADER_ID, argsBundle, this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (profileItems != null) {
            outState.putSerializable(PROFILE_URL_KEY, profileItems);
        }
    }
    //Changed

    @Override
    public void onProfileItemClick(SteamUtils.ProfileItem profileItem) {
        if (profileItem.description != "Did not find player!") {
            Intent intent = new Intent(this, ProfileItemDetailActivity.class);
            intent.putExtra(SteamUtils.ProfileItem.EXTRA_PROFILE_ITEM, profileItem);
            startActivity(intent);
        } else {
            // do nothing
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            String mProfileJSON;

            @Override
            protected void onStartLoading() {
                if (mProfileJSON != null) {
                    Log.d(TAG, "AsyncTaskLoader delivering cached profile");
                    deliverResult(mProfileJSON);
                } else {
                    mLoadingIndicatorPB.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Override
            public String loadInBackground() {
                String profileURL = args.getString(PROFILE_URL_KEY);
                if (profileURL == null || profileURL.equals("")) {
                    return null;
                }
                Log.d(TAG, "AsyncTaskLoader loading steam profile from url: " + profileURL);

                String profileJSON = null;
                try {
                    profileJSON = NetworkUtils.doHTTPGet(profileURL);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return profileJSON;
            }

            @Override
            public void deliverResult(String profileJSON) {
                mProfileJSON = profileJSON;
                super.deliverResult(profileJSON);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String profileJSON) {
        Log.d(TAG, "AsyncTaskLoader load finished");
        mLoadingIndicatorPB.setVisibility(View.INVISIBLE);
        if (profileJSON != null) {
            mLoadingErrorMessageTV.setVisibility(View.INVISIBLE);
            mProfileItemsRV.setVisibility(View.VISIBLE);
            profileItems = SteamUtils.parseProfileJSON(profileJSON, name);
            mProfileAdapter.updateProfileItems(profileItems);
        } else {
            mProfileItemsRV.setVisibility(View.INVISIBLE);
            mLoadingErrorMessageTV.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        // Nothing to do here...
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "SOMETHINGS UP");
    }

}
