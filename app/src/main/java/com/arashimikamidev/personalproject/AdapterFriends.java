package com.arashimikamidev.personalproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AdapterFriends extends ArrayAdapter<ClassFriends> {
    public AdapterFriends(Context context, List<ClassFriends> friends) {
        super(context, 0, friends);
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

        ClassFriends friends = getItem(position);

        if (friends != null) {
            viewHolder.tvNombre.setText(friends.getUserName());
            viewHolder.tvEmail.setText(friends.getUserEmail());
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tvNombre, tvEmail;
        ImageView imgFriend;
    }
}
