package com.example.android.steamprofilefinder.utils;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Daniel Goh on 6/6/2017.
 */

public class SteamUtils {

    private final static String STEAM_BASE_URL = "http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/";
    private final static String STEAM_USER_PROFILE_URL = "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/";
    private final static String STEAM_GAME_LIST_URL = "http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/";
    private final static String STEAM_APPID_PARAM = "key";
    private final static String STEAM_VANITYURL_PARAM = "vanityurl";
    private final static String STEAM_STEAMID_PARAM = "steamids";
    private final static String STEAM_GAMELIST_STEAMID_PARAM = "steamid";
    private final static String STEAM_FORMAT_PARAM = "format";

    private final static String STEAM_APPID = "B8F6C5BAF7C8606FFFCD28F5983DFAD3";

    public static class ProfileItem implements Serializable {
        public static final String EXTRA_PROFILE_ITEM = "com.example.android.steamprofilefinder.utils.ProfileItem.SearchResult";
        public String success;
        public String steamid;
        public String description;
    }

    public static class SteamIDItem implements Serializable {
        public static final String EXTRA_STEAMID_ITEM = "com.example.android.steamprofilefinder.utils.SteamIDItem.SearchResult";
        public String personaname;
        public String profilestate;
        public String realname;
        public String lastlogoff;
        public String imgURL;
    }

    public static String buildProfileURL(String profile) {
        return Uri.parse(STEAM_BASE_URL).buildUpon()
                .appendQueryParameter(STEAM_APPID_PARAM, STEAM_APPID)
                .appendQueryParameter(STEAM_VANITYURL_PARAM, profile)
                .build()
                .toString();
    }

    public static ArrayList<ProfileItem> parseProfileJSON(String searchResultsJSON, String name) {
        try {
            JSONObject searchResultsObj = new JSONObject(searchResultsJSON);

            ArrayList<ProfileItem> searchResultsList = new ArrayList<ProfileItem>();
            ProfileItem searchResult = new ProfileItem();
            searchResult.success = searchResultsObj.getJSONObject("response").getString("success");

            if (searchResult.success.equals("1")){
                searchResult.description = "Found " + name + "! Tap for details";
                searchResult.steamid = searchResultsObj.getJSONObject("response").getString("steamid");
                searchResultsList.add(searchResult);
            }
            else {
                searchResult.description = "Did not find player!";
                searchResultsList.add(searchResult);
            }
            return searchResultsList;
        } catch (JSONException e) {
            return null;
        }
    }

    public static String buildUserProfileURL(String profile) {
        return Uri.parse(STEAM_USER_PROFILE_URL).buildUpon()
                .appendQueryParameter(STEAM_APPID_PARAM, STEAM_APPID)
                .appendQueryParameter(STEAM_STEAMID_PARAM, profile)
                .build()
                .toString();
    }

    public static SteamIDItem parseSteamIDJSON(String searchResultsJSON) {
       try {
           JSONObject searchResultsObj = new JSONObject(searchResultsJSON);
           JSONArray searchResultsItems = searchResultsObj.getJSONObject("response").getJSONArray("players");

           SteamIDItem searchResult = new SteamIDItem();
           if(searchResultsItems != null){
               JSONObject searchResultItem = searchResultsItems.getJSONObject(0);
               searchResult.personaname = searchResultItem.getString("personaname");
               searchResult.profilestate = searchResultItem.getString("personastate");
               searchResult.realname = searchResultItem.getString("realname");

               long unixSeconds = searchResultItem.getLong("lastlogoff");
               Date date = new Date(unixSeconds*1000L); // *1000 is to convert seconds to milliseconds
               searchResult.lastlogoff = date.toString();
               if (searchResultItem.getString("avatarfull") != null) {
                   searchResult.imgURL = searchResultItem.getString("avatarfull");
               } else {
                   searchResult.imgURL = "https://i0.wp.com/www.artstation.com/assets/default_avatar.jpg";
               }
           }

           return searchResult;
        } catch (JSONException e) {
            return null;
        }
    }

    public static String buildGameListURL(String profile) {
        return Uri.parse(STEAM_GAME_LIST_URL).buildUpon()
                .appendQueryParameter(STEAM_APPID_PARAM, STEAM_APPID)
                .appendQueryParameter(STEAM_GAMELIST_STEAMID_PARAM, profile)
                .appendQueryParameter(STEAM_FORMAT_PARAM, "json")
                .build()
                .toString();
    }

}
