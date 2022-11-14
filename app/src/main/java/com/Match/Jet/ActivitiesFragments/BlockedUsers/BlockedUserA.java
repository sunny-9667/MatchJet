package com.Match.Jet.ActivitiesFragments.BlockedUsers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.Match.Jet.ActivitiesFragments.Chat.ChatActivity;
import com.Match.Jet.Adapters.BlockedUsersAdapter;
import com.Match.Jet.Models.BlockUsersModel;
import com.Match.Jet.R;
import com.Match.binderstatic.ApiClasses.ApiLinks;
import com.Match.binderstatic.ApiClasses.ApiRequest;
import com.Match.binderstatic.Interfaces.AdapterClickListener;
import com.Match.binderstatic.Interfaces.Callback;
import com.Match.binderstatic.Interfaces.FragmentCallback;
import com.Match.binderstatic.SimpleClasses.Functions;
import com.Match.binderstatic.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class BlockedUserA extends AppCompatActivity {

    RecyclerView recylerview;
    BlockedUsersAdapter adapter;
    ArrayList<BlockUsersModel> arrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_user);

        arrayList=new ArrayList<>();

        recylerview = (RecyclerView) findViewById(R.id.recylerview);
        LinearLayoutManager layout = new LinearLayoutManager(this);
        recylerview.setLayoutManager(layout);
        recylerview.setHasFixedSize(false);
        adapter =new BlockedUsersAdapter(this, arrayList, new AdapterClickListener() {
            @Override
            public void onItemClick(int pos, Object object, View view) {
                BlockUsersModel blockUsersModel=(BlockUsersModel) object;
                if(view.getId()==R.id.unblockBtn){

                    callApiBlockUser(pos,blockUsersModel);
                }
                else {
                    chatFragment(blockUsersModel);
                }
            }

            @Override
            public void onLongItemClick(int pos, Object item, View view) {
            }
        });

        recylerview.setAdapter(adapter);

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        callApiBlockUsersList();
    }


    private void callApiBlockUsersList() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(this).getString(Variables.uid,""));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.showLoader(this,false,false);
        ApiRequest.callApi(this, ApiLinks.showBlockedUsers, parameters, new Callback() {
            @Override
            public void response(String resp) {
                Functions.cancelLoader();

                try {
                    JSONObject jsonObject=new JSONObject(resp);

                    String code=jsonObject.optString("code");
                    if(code.equals("200")) {


                        JSONArray msg=jsonObject.optJSONArray("msg");
                        for(int i=0;i<msg.length();i++){
                            JSONObject object=msg.optJSONObject(i);
                            BlockUsersModel model = null;
                            try {
                                model = new ObjectMapper().readValue(object.toString(), BlockUsersModel.class);
                                arrayList.add(model);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        adapter.notifyDataSetChanged();

                    }


                    if(arrayList.isEmpty()){
                        findViewById(R.id.nodata_found_txt).setVisibility(View.VISIBLE);

                    }
                    else {
                        findViewById(R.id.nodata_found_txt).setVisibility(View.GONE);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }





    private void callApiBlockUser(int pos,BlockUsersModel blockUsersModel) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(this).getString(Variables.uid,""));
            parameters.put("block_user_id",blockUsersModel.blockedUser.id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.showLoader(this,false,false);
        ApiRequest.callApi(this, ApiLinks.blockUser, parameters, new Callback() {
            @Override
            public void response(String resp) {
                Functions.cancelLoader();

                try {
                    JSONObject jsonObject=new JSONObject(resp);

                    String code=jsonObject.optString("code");
                    if(code.equals("200") || code.equals("201")) {
                        arrayList.remove(pos);
                        adapter.notifyDataSetChanged();
                    }

                    if(arrayList.isEmpty()){
                        findViewById(R.id.nodata_found_txt).setVisibility(View.VISIBLE);
                    }
                    else { findViewById(R.id.nodata_found_txt).setVisibility(View.GONE); }


                } catch (JSONException e) {
                    e.printStackTrace();
                }




            }
        });
    }


    public void chatFragment(BlockUsersModel blockUsersModel){
        ChatActivity chatActivity = new ChatActivity(new FragmentCallback() {
            @Override
            public void responce(Bundle bundle) {

            }
        });
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        Bundle args = new Bundle();
        args.putString("Sender_Id", Functions.getSharedPreference(this).getString(Variables.uid,""));
        args.putString("Receiver_Id", blockUsersModel.blockedUser.id);
        args.putString("name",blockUsersModel.blockedUser.username);
        args.putString("picture",blockUsersModel.blockedUser.image);
        args.putBoolean("is_match_exits",false);
        chatActivity.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.BlockF, chatActivity).commit();
    }

}