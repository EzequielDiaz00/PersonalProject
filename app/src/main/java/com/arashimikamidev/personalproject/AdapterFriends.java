package com.arashimikamidev.personalproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class AdapterFriends extends ArrayAdapter<ClassFriends> {
    private List<ClassFriends> originalFriendsList;
    private List<ClassFriends> filteredFriendsList;

    public AdapterFriends(Context context, List<ClassFriends> friends) {
        super(context, 0, friends);
        this.originalFriendsList = new ArrayList<>(friends);
        this.filteredFriendsList = friends;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_friends_main, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvNombre = convertView.findViewById(R.id.lblNameUser);
            viewHolder.tvEmail = convertView.findViewById(R.id.lblEmailUser);
            viewHolder.imgFriend = convertView.findViewById(R.id.imgUser);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ClassFriends friend = getItem(position);

        if (friend != null) {
            viewHolder.tvNombre.setText(friend.getUserName());
            viewHolder.tvEmail.setText(friend.getUserEmail());
            Glide.with(getContext()).load(friend.getUserFoto()).into(viewHolder.imgFriend);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return filteredFriendsList.size();
    }

    @Override
    public ClassFriends getItem(int position) {
        return filteredFriendsList.get(position);
    }

    public void updateList(List<ClassFriends> newFriendsList) {
        this.filteredFriendsList = newFriendsList;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView tvNombre, tvEmail;
        ImageView imgFriend;
    }
}