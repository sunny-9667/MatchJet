package com.Match.Jet.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.Match.binderstatic.Interfaces.AdapterClickListener;
import com.Match.Jet.Models.MySchoolModel;
import com.Match.Jet.R;

import java.util.ArrayList;

public class MySchoolAdapter extends RecyclerView.Adapter<MySchoolAdapter.CustomViewHolder > {
    public Context context;
    ArrayList<MySchoolModel> list = new ArrayList<>();

    AdapterClickListener adapterClickListener;

    public MySchoolAdapter(Context context, ArrayList<MySchoolModel> list, AdapterClickListener adapterClickListener) {
        this.context = context;
        this.list=list;
        this.adapterClickListener=adapterClickListener;
    }

    @Override
    public MySchoolAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_myschool,null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        MySchoolAdapter.CustomViewHolder viewHolder = new MySchoolAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
       return list.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView schoolName;
        RelativeLayout border;

        public CustomViewHolder(View view) {
            super(view);
            schoolName =view.findViewById(R.id.tv);
            border =view.findViewById(R.id.border);
        }

        public void bind(final int pos, final MySchoolModel item, final AdapterClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(pos,item,v);
                }
            });
        }

    }

    @Override
    public void onBindViewHolder(final MySchoolAdapter.CustomViewHolder holder, final int i) {
        final MySchoolModel item=list.get(i);

        holder.schoolName.setText(item.getSchoolName());

        if(i == list.size()){
            holder.border.setVisibility(View.GONE);
        }else {
            holder.border.setVisibility(View.VISIBLE);
        }

        holder.bind(i,item,adapterClickListener);
   }

}