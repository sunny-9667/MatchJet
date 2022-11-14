package com.Match.Jet.ActivitiesFragments.Users;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Match.binderstatic.Interfaces.Callback;
import com.Match.Jet.Adapters.ReportTypeAdapter;
import com.Match.binderstatic.ApiClasses.ApiLinks;
import com.Match.binderstatic.ApiClasses.ApiRequest;
import com.Match.binderstatic.Models.ReportTypeModel;
import com.Match.binderstatic.SimpleClasses.Functions;
import com.Match.binderstatic.Interfaces.FragmentCallback;
import com.Match.binderstatic.RelateToFragment_OnBack.RootFragment;
import com.Match.Jet.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class ReportType_F extends RootFragment implements View.OnClickListener {

    View view;
    RecyclerView recyclerview;
    ReportTypeAdapter adapter;

    Bundle fragmentCallBack;
    String userId;

    boolean check=false;

    ArrayList<ReportTypeModel> dataList = new ArrayList<>();

    public ReportType_F() {
        //Required Empty
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_report_type, container, false);
        view.findViewById(R.id.back_btn).setOnClickListener(this);
        recyclerview = view.findViewById(R.id.recylerview);

        Bundle bundle = getArguments();
        if (bundle != null) {
            userId = bundle.getString("user_id");
        }

        callApiForGetReportType();

        return view;
    }


    // get the types of reports
    private void callApiForGetReportType() {

        Functions.showLoader(getActivity(), false, false);
        JSONObject parameters = new JSONObject();
        ApiRequest.callApi(getActivity(), ApiLinks.showReportReasons, parameters, new Callback() {
            @Override
            public void response(String resp) {
                Functions.cancelLoader();
                parseData(resp);
            }
        });
    }

    private void parseData(String resp) {
        dataList.clear();
        try {
            JSONObject jsonObject = new JSONObject(resp);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");

                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);
                    JSONObject reportreason = itemdata.optJSONObject("ReportReason");

                    ReportTypeModel item = new ReportTypeModel();
                    item.id = reportreason.optString("id");
                    item.title = reportreason.optString("title");
                    dataList.add(item);
                }
                setAdapter();
                Functions.cancelLoader();

            } else {
                Functions.cancelLoader();
                Toast.makeText(getActivity(), ""+jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            Functions.cancelLoader();
            e.printStackTrace();
        }

    }


    private void setAdapter() {
        adapter = new ReportTypeAdapter(getActivity(), dataList, new ReportTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int positon, ReportTypeModel item, View view) {
                switch (view.getId()) {
                    case R.id.rlt_report:
                        SubmitReport_F submit_report_f = new SubmitReport_F(new FragmentCallback() {
                            @Override
                            public void responce(Bundle bundle) {
                                if(bundle != null && bundle.getBoolean("check",false)){
                                    check = bundle.getBoolean("check", false);
                                    getActivity().onBackPressed();
                                }
                            }
                        });
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
                        transaction.addToBackStack(null);
                        Bundle args = new Bundle();
                        args.putString("report_id", item.id);
                        args.putString("report_type", item.title);
                        args.putString("user_id", userId);
                        submit_report_f.setArguments(args);
                        transaction.replace(R.id.fragment_select_report, submit_report_f).commit();
                        break;

                }
            }
        });

        adapter.setHasStableIds(true);
        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerview.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                getActivity().onBackPressed();
                break;
        }
    }

    @Override
    public void onDetach() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("check", check);
        getParentFragmentManager().setFragmentResult("1122", bundle);
        super.onDetach();
    }

}