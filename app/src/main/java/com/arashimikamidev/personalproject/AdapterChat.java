package com.arashimikamidev.personalproject;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.ViewHolder> {
    private List<ClassChat> classChats;
    private Context context;

    public AdapterChat(Context context, List<ClassChat> classChats) {
        this.context = context;
        this.classChats = classChats;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mbox_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassChat chat = classChats.get(position);

        if (chat.getmEmisor() != null) {

            if (chat.getmFoto() != null) {

                holder.imgEmi.setVisibility(View.VISIBLE);

                Glide.with(context).load(chat.getmFoto()).into(holder.imgEmi);

                holder.linear2.setGravity(Gravity.RIGHT);
                holder.linear1.setVisibility(View.GONE);
                holder.tvEmisor.setText(chat.getmEmisor());
                holder.tvDateE.setText(chat.getMfEmisor());

            } else if (chat.getmFoto() == null) {
                holder.imgEmi.setVisibility(View.GONE);

                holder.linear2.setGravity(Gravity.RIGHT);
                holder.linear1.setVisibility(View.GONE);
                holder.tvEmisor.setText(chat.getmEmisor());
                holder.tvDateE.setText(chat.getMfEmisor());
            }
        } else if (chat.getmReceptor() != null) {

            if (chat.getmFoto() != null) {

                holder.imgRec.setVisibility(View.VISIBLE);

                Glide.with(context).load(chat.getmFoto()).into(holder.imgRec);

                holder.linear2.setGravity(Gravity.LEFT);
                holder.linear1.setVisibility(View.VISIBLE);
                holder.linear2.setVisibility(View.GONE);
                holder.tvReceptor.setText(chat.getmReceptor());
                holder.tvDateR.setText(chat.getMfReceptor());

            } else if (chat.getmFoto() == null) {
                holder.imgRec.setVisibility(View.GONE);

                holder.linear2.setGravity(Gravity.LEFT);
                holder.linear1.setVisibility(View.VISIBLE);
                holder.linear2.setVisibility(View.GONE);
                holder.tvReceptor.setText(chat.getmReceptor());
                holder.tvDateR.setText(chat.getMfReceptor());
            }
        }
    }

    @Override
    public int getItemCount() {
        return classChats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvReceptor, tvEmisor, tvDateR, tvDateE;
        LinearLayout linear1, linear2;
        ImageView imgEmi, imgRec;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReceptor = itemView.findViewById(R.id.boxRecM);
            tvEmisor = itemView.findViewById(R.id.boxEmiM);
            tvDateR = itemView.findViewById(R.id.boxRecD);
            tvDateE = itemView.findViewById(R.id.boxEmiD);
            linear1 = itemView.findViewById(R.id.boxAllR);
            linear2 = itemView.findViewById(R.id.boxAllE);

            imgEmi = itemView.findViewById(R.id.imgEmi);
            imgRec = itemView.findViewById(R.id.imgRec);
        }

        public void bind(ClassChat chat) {
            tvReceptor.setText(chat.getmReceptor());
            tvEmisor.setText(chat.getmEmisor());
        }
    }
}
