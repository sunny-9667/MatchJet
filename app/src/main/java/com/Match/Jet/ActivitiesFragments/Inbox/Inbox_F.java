package com.Match.Jet.ActivitiesFragments.Inbox;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.Match.binderstatic.Models.MatchModel;
import com.Match.Jet.ActivitiesFragments.Chat.ChatActivity;
import com.Match.Jet.Adapters.InboxAdapter;
import com.Match.Jet.Adapters.MatchesAdapter;
import com.Match.binderstatic.ApiClasses.ApiLinks;
import com.Match.binderstatic.Constants;
import com.Match.binderstatic.SimpleClasses.Variables;
import com.Match.binderstatic.Interfaces.AdapterClickListener;
import com.Match.binderstatic.ApiClasses.ApiRequest;
import com.Match.binderstatic.Interfaces.Callback;
import com.Match.binderstatic.Interfaces.FragmentCallback;
import com.Match.Jet.Models.InboxModel;
import com.Match.binderstatic.SimpleClasses.Functions;
import com.Match.binderstatic.RelateToFragment_OnBack.RootFragment;
import com.Match.Jet.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class Inbox_F extends RootFragment implements  View.OnClickListener {

    View view;
    Context context;

    String userId;
    LinearLayout searchLayout;
    EditText searchEt;
    RecyclerView inboxList, matchList;

    ArrayList<InboxModel> inboxArrayList;
    ArrayList<MatchModel> matchUsersList;

    DatabaseReference rootRef;

    MatchesAdapter matchesAdapter;
    InboxAdapter inboxAdapter;

    boolean isviewCreated =false;


    SimpleDraweeView userLikeImage;
    TextView likesCountTxt;
    int likesCount=0;

    public Inbox_F() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_inbox, container, false);

        context=getContext();
        userId = Functions.getSharedPreference(context).getString(Variables.uid,"");
        rootRef =FirebaseDatabase.getInstance().getReference();


        searchLayout =view.findViewById(R.id.searchLayout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            searchLayout.setFocusedByDefault(false);
        }

        searchLayout.setFocusableInTouchMode(true);

        searchEt =view.findViewById(R.id.searchEt);
        inboxList =view.findViewById(R.id.inboxlist);
        matchList =view.findViewById(R.id.match_list);

        // intialize the arraylist and and inboxlist
        inboxArrayList =new ArrayList<>();

        inboxList = (RecyclerView) view.findViewById(R.id.inboxlist);
        LinearLayoutManager layout = new LinearLayoutManager(context);
        inboxList.setLayoutManager(layout);
        inboxList.setHasFixedSize(false);
        inboxAdapter =new InboxAdapter(context, inboxArrayList, new AdapterClickListener() {
            @Override
            public void onItemClick(int pos, Object object, View view) {
                InboxModel item=(InboxModel) object;
                if(checkReadStoragePermission())
                    chatFragment(userId, item.getId(), item.getName(),item.getPicture(),false);
            }

            @Override
            public void onLongItemClick(int pos, Object item, View view) {
            }
        });

        inboxList.setAdapter(inboxAdapter);

        // intialize the arraylist and and upper Match list
        matchList =view.findViewById(R.id.match_list);
        matchUsersList =new ArrayList<>();
        matchList.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));
        OverScrollDecoratorHelper.setUpOverScroll(matchList, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL);
        matchesAdapter =new MatchesAdapter(context, matchUsersList, new AdapterClickListener() {
            @Override
            public void onItemClick(int pos, Object object, View view) {
                MatchModel item=(MatchModel)object;
                if(checkReadStoragePermission())
                    chatFragment(userId,item.getU_id(),item.getUsername(),item.getPicture(),true);

            }

            @Override
            public void onLongItemClick(int pos, Object item, View view) {

            }
        });
        matchList.setAdapter(matchesAdapter);


        userLikeImage =view.findViewById(R.id.userLikeImage);
        likesCountTxt =view.findViewById(R.id.likes_count_txt);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.hideSoftKeyboard(getActivity());
            }
        });

        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()>0){
                    view.findViewById(R.id.likes_count_layout).setVisibility(View.GONE);
                }else {
                    view.findViewById(R.id.likes_count_layout).setVisibility(View.VISIBLE);
                }
                if(inboxArrayList.size()>0){
                    filterInboxList(s.toString());
                }
                if(matchUsersList.size()>0){
                    filterMatchUserList(s.toString());
                }
            }
        });


        isviewCreated = true;

        return view;
    }

    private void filterInboxList(String text){
        ArrayList<InboxModel> filterList = new ArrayList<>();
        filterList.clear();

        for(InboxModel item : inboxArrayList){
            if(item.getName().toLowerCase().contains(text.toString())){
                filterList.add(item);
            }
        }
        inboxAdapter.filterList(filterList);
    }

    private void filterMatchUserList(String text){
        ArrayList<MatchModel> filterList = new ArrayList<>();
        filterList.clear();

        for(MatchModel item : matchUsersList){
            if(item.getUsername().toLowerCase().contains(text)){
                filterList.add(item);
            }
        }
        matchesAdapter.filterList(filterList);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if(menuVisible && isviewCreated){
            callApiShowUserMatches();
        }
    }

    // on start we will get the Inbox Message of user  which is show in bottom list of third tab
    @Override
    public void onStart() {
        super.onStart();
        getInbox();
    }

    @Override
    public void onResume() {
        super.onResume();
        callApiShowUserMatches();
    }

    ValueEventListener eventListener2;
    Query inboxQuery;
    private void getInbox() {
        inboxQuery = rootRef.child("Inbox").child(userId).orderByChild("date");
        eventListener2=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                inboxArrayList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()) {
                    
                    InboxModel model = new InboxModel();
                    model.setId(ds.getKey());
                    model.setName(ds.child("name").getValue().toString());
                    model.setMessage(ds.child("msg").getValue().toString());
                    model.setTimestamp(ds.child("date").getValue().toString());
                    model.setStatus(ds.child("status").getValue().toString());
                    model.setPicture(ds.child("pic").getValue().toString());
                    inboxArrayList.add(model);
                }
                Collections.reverse(inboxArrayList);
                inboxAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        inboxQuery.addValueEventListener(eventListener2);
    }


    // on stop we will remove the listener
    @Override
    public void onStop() {
        super.onStop();
        if(inboxQuery !=null)
            inboxQuery.removeEventListener(eventListener2);
    }


    //open the chat fragment and on item click and pass your id and the other person id in which
    //you want to chat with them and this parameter is that is we move from match list or inbox list
    public void chatFragment(String senderid,String receiverid,String name,String picture,boolean is_match_exits){
        ChatActivity chat_activity = new ChatActivity(new FragmentCallback() {
            @Override
            public void responce(Bundle bundle) {
                callApiShowUserMatches();
            }
        });
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        Bundle args = new Bundle();
        args.putString("Sender_Id",senderid);
        args.putString("Receiver_Id",receiverid);
        args.putString("picture",picture);
        args.putString("name",name);
        args.putBoolean("is_match_exits",is_match_exits);
        chat_activity.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, chat_activity).commit();
    }

    //this method will check there is a storage permission given or not
    private boolean checkReadStoragePermission(){
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else {
            try {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        Variables.PERMISSION_READ_DATA);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
        return false;
    }

    // below two method will get all the  new user that is nearby of us  and parse the data in data set
    // in that case which has a name of Nearby get set
    public void callApiShowUserMatches() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", userId);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(context, ApiLinks.showUserMatches, parameters, new Callback() {
            @Override
            public void response(String resp) {
                parseUserInfo(resp);
            }
        });

    }

    public void parseUserInfo(String loginData){
        try {
            JSONObject jsonObject=new JSONObject(loginData);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                matchUsersList.clear();
                JSONObject msg = jsonObject.optJSONObject("msg");
                JSONArray matchArray = msg.optJSONArray("matches");
                if(matchArray.length() > 0 ){
                    searchLayout.setVisibility(View.VISIBLE);
                    searchEt.setHint(context.getString(R.string.search)+" "+matchArray.length()+" "+context.getString(R.string.matches));
                    for (int i=0; i<matchArray.length();i++){
                        String firstid = matchArray.getJSONObject(i).optJSONObject("OtherUser").getString("id");
                        JSONObject userdata;
                        if(firstid.equals(userId)){
                            userdata = matchArray.getJSONObject(i).optJSONObject("User");
                        }else {
                            userdata=matchArray.getJSONObject(i).optJSONObject("OtherUser");
                        }
                        JSONArray userImagesList = userdata.optJSONArray("UserImage");
                        MatchModel model = new MatchModel();
                        model.setU_id(userdata.optString("id"));
                        model.setUsername(userdata.optString("first_name")+" "+userdata.optString("last_name"));
                        if(userImagesList != null && userImagesList.length()>0){
                            for(int j = 0; j<userImagesList.length(); j++){
                                if(userImagesList.optJSONObject(j).optString("order_sequence").equals("0")){
                                    model.setPicture(userImagesList.optJSONObject(j).optString("image"));
                                    break;
                                }
                            }
                        }
                        matchUsersList.add(model);
                    }
                }else {
                    if(inboxArrayList.size() > 0){
                        searchLayout.setVisibility(View.VISIBLE);
                    }else {
                        searchLayout.setVisibility(View.GONE);
                    }
                }

                matchesAdapter.notifyDataSetChanged();

                if(matchUsersList.isEmpty()){
                    view.findViewById(R.id.no_match_txt).setVisibility(View.VISIBLE);
                }else {
                    view.findViewById(R.id.no_match_txt).setVisibility(View.GONE);
                }


                JSONObject likeUserObject = msg.optJSONObject("like_user");
                JSONObject otherUser;
                if(likeUserObject != null){
                    if(likeUserObject.optJSONObject("User").optString("id").equals(Functions.getSharedPreference(context).getString(Variables.uid,""))){
                        otherUser = likeUserObject.optJSONObject("OtherUser");
                    }else {
                        otherUser = likeUserObject.optJSONObject("User");
                    }
                    JSONArray userImagesArray = otherUser.optJSONArray("UserImage");
                    if(userImagesArray != null){
                        for (int i = 0; i<userImagesArray.length(); i++){
                            JSONObject imageObject = userImagesArray.getJSONObject(i);
                            if(imageObject.optString("order_sequence").equals("0")){
                                if(Functions.getSharedPreference(context).getBoolean(Variables.isProductPurchase,Constants.enableSubscribe)){
                                    Functions.printLog( "ProductPurchaseNot");
                                    userLikeImage.setController(com.Match.Jet.SimpleClasses.Functions.frescoImageLoad(imageObject.optString("image"),R.drawable.ic_user_icon,userLikeImage,false));

                                }else {
                                    Functions.printLog( "ProductPurchase");
                                    userLikeImage.setController(com.Match.Jet.SimpleClasses.Functions.frescoImageLoad(imageObject.optString("image"),R.drawable.ic_user_icon,userLikeImage,false));

                                }
                                break;
                            }
                        }
                    }
                }

                String  myLikes = msg.optString("like_users_count");
                if(myLikes!=null){
                    int count = Integer.valueOf(myLikes);
                    likesCount = count;
                    if(count>0){
                        likesCountTxt.setText(count+" "+context.getString(R.string.likes));

                        view.findViewById(R.id.no_match_txt).setVisibility(View.GONE);
                        view.findViewById(R.id.likes_count_layout).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.likes_count_layout).setOnClickListener(this);
                    } else {
                        view.findViewById(R.id.likes_count_layout).setVisibility(View.GONE);
                    }
                }
            }else {
                view.findViewById(R.id.no_match_txt).setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.likes_count_layout:
                if(Functions.getSharedPreference(context).getBoolean(Variables.isProductPurchase,Constants.enableSubscribe)){
                    openUserList();
                }else {
                    openSubscriptionView();
                }
                break;
        }
    }


    public void openSubscriptionView(){
        try {
            startActivity(new Intent(getActivity(), Class.forName("com.Match.binder_pro.InAppSubscription.InAppSubscription_A")));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void openUserList(){
        try {
            Fragment fragment = (Fragment) Class.forName("com.Match.binder_pro.ActivitiesFragments.Inbox_likes_F").newInstance();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);

            getActivity().getSupportFragmentManager().setFragmentResultListener("1112",
                    this, new FragmentResultListener() {
                @Override
                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                    if(requestKey.equals("1112")){
                        if(result != null){
                            int count = result.getInt("likes", 0);
                            if(count == 0 || count < 0){
                                view.findViewById(R.id.likes_count_layout).setVisibility(View.GONE);
                            }else {
                                likesCountTxt.setText(count +" "+ context.getString(R.string.likes));
                                view.findViewById(R.id.likes_count_layout).setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            });

            Bundle args = new Bundle();
            args.putString("like_count", String.valueOf(likesCount));
            fragment.setArguments(args);

            transaction.addToBackStack(null).replace(R.id.MainMenuFragment, fragment).commit();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        /*Inbox_likes_F user_likes_f = new Inbox_likes_F(new FragmentCallback() {
            @Override
            public void responce(Bundle bundle) {
                if(bundle!=null){
                    int count = bundle.getInt("likes", 0);
                    if(count == 0 || count < 0){
                        view.findViewById(R.id.likes_count_layout).setVisibility(View.GONE);
                    }else {
                        likesCountTxt.setText(count +" "+ context.getString(R.string.likes));
                        view.findViewById(R.id.likes_count_layout).setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("like_count", String.valueOf(likesCount));
        user_likes_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, user_likes_f).commit();*/
    }

}
