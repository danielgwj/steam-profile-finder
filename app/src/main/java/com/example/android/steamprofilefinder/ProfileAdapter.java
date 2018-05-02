package com.example.android.steamprofilefinder;

import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.steamprofilefinder.utils.SteamUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Daniel Goh on 6/6/2017.
 */

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileItemViewHolder>{

    private ArrayList<SteamUtils.ProfileItem> mProfileItems;
    private OnProfileItemClickListener mProfileItemClickListener;

    public interface OnProfileItemClickListener {
        void onProfileItemClick(SteamUtils.ProfileItem profileItem);
    }

    public ProfileAdapter(OnProfileItemClickListener clickListener) {
        mProfileItemClickListener = clickListener;
    }

    public void updateProfileItems(ArrayList<SteamUtils.ProfileItem> profileItems) {
        mProfileItems = profileItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mProfileItems != null) {
            return mProfileItems.size();
        } else {
            return 0;
        }
    }

    @Override
    public ProfileItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.profile_item, parent, false);
        return new ProfileItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ProfileItemViewHolder holder, int position) {
        holder.bind(mProfileItems.get(position));
    }

    class ProfileItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mProfileTV;

        public ProfileItemViewHolder(View itemView) {
            super(itemView);
            mProfileTV = (TextView)itemView.findViewById(R.id.tv_profile_text);
            itemView.setOnClickListener(this);
        }

        public void bind(SteamUtils.ProfileItem profileItem) {
            mProfileTV.setText(profileItem.description); //FIXME: PROFILE USERNAME
        }

        @Override
        public void onClick(View v) {
            SteamUtils.ProfileItem profileItem = mProfileItems.get(getAdapterPosition());
            mProfileItemClickListener.onProfileItemClick(profileItem);
        }
    }
}
