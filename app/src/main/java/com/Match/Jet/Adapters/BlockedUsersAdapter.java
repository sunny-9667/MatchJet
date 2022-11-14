package com.Match.Jet.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.Match.Jet.Models.BlockUsersModel;
import com.Match.Jet.R;
import com.Match.binderstatic.Constants;
import com.Match.binderstatic.Interfaces.AdapterClickListener;

import java.util.ArrayList;

/**
 * Created by qboxus on 3/20/2018.
 */

public class BlockedUsersAdapter extends RecyclerView.Adapter<BlockedUsersAdapter.CustomViewHolder>{
    public Context context;
    ArrayList<BlockUsersModel> dataList = new ArrayList<>();

    AdapterClickListener adapterClickListener;


    public BlockedUsersAdapter(Context context, ArrayList<BlockUsersModel> user_dataList, AdapterClickListener adapterClickListener) {
        this.context = context;
        this.dataList =user_dataList;

        this.adapterClickListener=adapterClickListener;

    }

    @Override
    public BlockedUsersAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_blockuser_list,null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, (int) context.getResources().getDimension(R.dimen._80sdp)));
        BlockedUsersAdapter.CustomViewHolder viewHolder = new BlockedUsersAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
       return dataList.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        SimpleDraweeView userImage;
        Button unblockBtn;

        public CustomViewHolder(View view) {
            super(view);
            userImage =itemView.findViewById(R.id.user_image);
            username=itemView.findViewById(R.id.username);
            unblockBtn=itemView.findViewById(R.id.unblockBtn);
        }

        public void bind(final int pos, final BlockUsersModel item, final AdapterClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(pos,item,v);
                }
            });

            unblockBtn.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(pos,item,v);
                }
            });


        }

    }

    @Override
    public void onBindViewHolder(final BlockedUsersAdapter.CustomViewHolder holder, final int i) {

        final BlockUsersModel item= dataList.get(i);

        holder.username.setText(item.blockedUser.username);


        holder.userImage.setController(com.Match.Jet.SimpleClasses.Functions.frescoImageLoad(item.blockedUser.image,R.drawable.ic_user_icon,holder.userImage,false));



        holder.bind(i,item,adapterClickListener);

   }




}