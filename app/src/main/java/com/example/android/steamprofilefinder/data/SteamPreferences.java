package com.example.android.steamprofilefinder.data;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by Daniel Goh on 6/6/2017.
 */

public class SteamPreferences {

    private static String DEFAULT_COLOR;

    public static int changeDefaultColor(String color) {
        if(color.equals("BLACK")){
            DEFAULT_COLOR = "#80484443";
        } else if(color.equals("BROWN")) {
            DEFAULT_COLOR = "#80381c11";
        } else if(color.equals("BLUE")) {
            DEFAULT_COLOR = "#80414467";
        }

        return 1;
    }

    public static void setDefaultColor(RecyclerView mProfileItemsRV) {
        mProfileItemsRV.setBackgroundColor(Color.parseColor(DEFAULT_COLOR));
    }
}
