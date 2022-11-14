package com.Match.Jet.ViewHolders;

import android.view.View;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.Match.Jet.Adapters.ProfilePhotosAdapter;
import com.Match.Jet.R;
import com.Match.binderstatic.Models.UserMultiplePhoto;

public class PhotosviewHolder extends RecyclerView.ViewHolder {
   public View view;
   public SimpleDraweeView image;
   public ImageButton crossButton,addButton;
    public PhotosviewHolder(View itemView) {
        super(itemView);
        view = itemView;
        image=view.findViewById(R.id.image);
        addButton =view.findViewById(R.id.add_btn);
        crossButton =view.findViewById(R.id.cross_btn);
    }


    public void bind(final UserMultiplePhoto item, final int position , final ProfilePhotosAdapter.OnItemClickListener listener) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(item,position,v);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(item,position,v);
            }
        });

        crossButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(item,position,v);
            }
        });
    }

}
