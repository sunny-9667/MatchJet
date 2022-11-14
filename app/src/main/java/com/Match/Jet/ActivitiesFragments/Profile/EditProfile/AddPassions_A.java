package com.Match.Jet.ActivitiesFragments.Profile.EditProfile;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.Match.Jet.SimpleClasses.ContextWrapper;
import com.Match.binderstatic.ApiClasses.ApiLinks;
import com.Match.binderstatic.ApiClasses.ApiRequest;
import com.Match.binderstatic.Constants;
import com.Match.binderstatic.SimpleClasses.Functions;
import com.Match.binderstatic.SimpleClasses.Variables;
import com.Match.binderstatic.Interfaces.Callback;
import com.Match.Jet.Models.PassionsModel;
import com.Match.Jet.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class AddPassions_A extends AppCompatActivity {

    Context context;
    List<PassionsModel> list = new ArrayList<>();
    List<String> tempList = new ArrayList<>();
    ArrayList<String> tempIdList = new ArrayList<>();

    ChipGroup chipGroup;
    TextView passionCountTv;

    String previousPassions;
    List<String> stringList = new ArrayList<>();
    StringBuilder sb = new StringBuilder();


    @Override
    protected void attachBaseContext(Context newBase) {
        String[] languageArray = newBase.getResources().getStringArray(R.array.language_code);
        List<String> languageCode = Arrays.asList(languageArray);
        String language = Functions.getSharedPreference(newBase)
                .getString(Variables.selectedLanguage, "");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && languageCode.contains(language)) {
            Locale newLocale = new Locale(language);
            Context context = ContextWrapper.wrap(newBase, newLocale);
            super.attachBaseContext(context);
        } else {
            super.attachBaseContext(newBase);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            setLanguageLocal();
        }
        setContentView(R.layout.activity_add_passions);

        context = this;

        Bundle bundle = getIntent().getBundleExtra(Variables.uPassions);
        if(bundle!=null){
            previousPassions = bundle.getString(Variables.uPassions);
            if(previousPassions != null && !previousPassions.isEmpty()){
                stringList = Arrays.asList(previousPassions.split(","));
                if(stringList.size()>0){
                    tempList.clear();
                    tempList.addAll(stringList);
                }
            }
        }

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        passionCountTv = findViewById(R.id.passionCountTv);
        passionCountTv.setText("("+tempList.size()+"/5)");
        chipGroup = findViewById(R.id.passionsView);

        getPassions();

    }


    public void setLanguageLocal(){
        String [] languageArray=getResources().getStringArray(R.array.language_code);
        List <String> languageCode= Arrays.asList(languageArray);
        String language = Functions.getSharedPreference(getApplicationContext())
                .getString(Variables.selectedLanguage,"");

        if(languageCode.contains(language)) {
            Locale myLocale = new Locale(language);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = new Configuration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
            onConfigurationChanged(conf);
        }
    }


    private void getPassions() {
        String passions = Functions.getSharedPreference(getApplicationContext())
                .getString(Variables.uPassions,"");
        if(passions.isEmpty()){
            callApiShowPassions();
        }else {
            JSONObject jsonObject= null;
            try {
                jsonObject = new JSONObject(passions);
                JSONArray msgArray = jsonObject.optJSONArray("msg");
                list.clear();
                tempIdList.clear();
                for (int i=0; i<msgArray.length();i++){
                    PassionsModel model = new PassionsModel();
                    model.setId(msgArray.getJSONObject(i).optJSONObject("Passion").getString("id"));
                    model.setTitle(msgArray.getJSONObject(i).optJSONObject("Passion").getString("title"));
                    list.add(model);

                    Chip chip1 = (Chip) LayoutInflater.from(AddPassions_A.this).inflate(R.layout.item_passion, null);
                    chip1.setText(msgArray.getJSONObject(i).optJSONObject("Passion").getString("title"));
                    chip1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(tempList.size() == 0){
                                tempList.add(chip1.getText().toString());
                                tempIdList.add(model.getId());
                                chip1.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink_color)));
                                chip1.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink_color)));
                            }else if(tempList.size()>0){
                                for(int i = 0; i<tempList.size(); i++){
                                    if(tempList.get(i).toLowerCase().trim().equals(chip1.getText().toString().toLowerCase().trim())){
                                        chip1.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.chipColor)));
                                        chip1.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.chipBorderColor)));
                                        tempList.remove(i);
                                        tempIdList.remove(model.getId());
                                        break;
                                    }else if(i+1 == tempList.size() && !tempList.get(i).toLowerCase().trim().equals(chip1.getText().toString().toLowerCase().trim())){
                                        if(tempList.size() < 5){
                                            tempList.add(chip1.getText().toString());
                                            tempIdList.add(model.getId());
                                            chip1.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink_color)));
                                            chip1.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink_color)));
                                            break;
                                        }
                                    }
                                }
                            }
                            passionCountTv.setText("("+tempList.size()+"/5)");
                        }
                    });

                    if(tempList.size() > 0){
                        for (int k = 0; k<tempList.size(); k++){
                            if(model.getTitle().trim().equals(tempList.get(k).trim())){
                                tempIdList.add(model.getId());
                                chip1.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink_color)));
                                chip1.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink_color)));
                                break;
                            }
                        }
                    }
                    Functions.printLog( "List: "+tempIdList.toString());
                    chipGroup.addView(chip1);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private void callApiShowPassions() {
        ApiRequest.callApi(this, ApiLinks.showPassions, new JSONObject(), new Callback() {
            @Override
            public void response(String resp) {
                parseUserInfo(resp);
            }
        });
    }


    private void parseUserInfo(String data){
        try {
            JSONObject jsonObject=new JSONObject(data);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                Functions.getSharedPreference(getApplicationContext()).edit()
                        .putString(Variables.uPassions, jsonObject.toString()).apply();
                JSONArray msgArray = jsonObject.optJSONArray("msg");
                list.clear();
                tempIdList.clear();
                if (msgArray != null) {
                    for (int i=0; i<msgArray.length();i++){
                        PassionsModel model = new PassionsModel();
                        model.setId(msgArray.getJSONObject(i).optJSONObject("Passion")
                                .optString("id"));
                        model.setTitle(msgArray.getJSONObject(i).optJSONObject("Passion")
                                .optString("title"));
                        list.add(model);

                        Chip chip1 = (Chip) LayoutInflater.from(AddPassions_A.this)
                                .inflate(R.layout.item_passion, null);
                        chip1.setText(msgArray.getJSONObject(i).optJSONObject("Passion")
                                .optString("title"));
                        chip1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(tempList.size() == 0){
                                    tempList.add(chip1.getText().toString());
                                    tempIdList.add(model.getId());
                                    chip1.setTextColor(ColorStateList.valueOf(ContextCompat
                                            .getColor(context, R.color.pink_color)));
                                    chip1.setChipStrokeColor(ColorStateList.valueOf(ContextCompat
                                            .getColor(context, R.color.pink_color)));
                                }else if(tempList.size()>0){
                                    for(int i = 0; i<tempList.size(); i++){
                                        if(tempList.get(i).toLowerCase().trim().equals(chip1
                                                .getText().toString().toLowerCase().trim())){
                                            chip1.setTextColor(ColorStateList.valueOf(ContextCompat
                                                    .getColor(context, R.color.chipColor)));
                                            chip1.setChipStrokeColor(ColorStateList
                                                    .valueOf(ContextCompat.getColor(context,
                                                            R.color.chipBorderColor)));
                                            tempList.remove(i);
                                            tempIdList.remove(model.getId());
                                            break;
                                        }else if(i+1 == tempList.size() && !tempList.get(i).toLowerCase().trim().equals(chip1.getText().toString().toLowerCase().trim())){
                                            if(tempList.size() < 5){
                                                tempList.add(chip1.getText().toString());
                                                tempIdList.add(model.getId());
                                                chip1.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink_color)));
                                                chip1.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink_color)));
                                                break;
                                            }
                                        }
                                    }
                                }
                                passionCountTv.setText("("+tempList.size()+"/5)");
                            }
                        });

                        if(tempList.size() > 0){
                            for (int k = 0; k<tempList.size(); k++){
                                if(model.getTitle().trim().equals(tempList.get(k).trim())){
                                    tempIdList.add(model.getId());
                                    chip1.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink_color)));
                                    chip1.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink_color)));
                                    break;
                                }
                            }
                        }
                        Functions.printLog( "List: "+tempIdList.toString());
                        chipGroup.addView(chip1);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void showDialog(String header, String desc, boolean isListEmpty){
        Dialog dialog = new Dialog(this);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.item_passion_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        final TextView headerTv = dialog.findViewById(R.id.headerTv);
        final TextView descTv = dialog.findViewById(R.id.descTv);
        final TextView yesTv = dialog.findViewById(R.id.yesTv);
        final TextView cancelTv = dialog.findViewById(R.id.cancelTv);

        headerTv.setText(header);
        descTv.setText(desc);

        if(isListEmpty){
            yesTv.setText(context.getString(R.string.yes_cap));
            cancelTv.setVisibility(View.VISIBLE);
        }else {
            yesTv.setText(context.getString(R.string.okay));
            cancelTv.setVisibility(View.GONE);
        }

        yesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                if(yesTv.getText().equals(context.getString(R.string.yes_cap))){
                    tempList.clear();
                    tempIdList.clear();

                    Intent intent = new Intent();
                    intent.putExtra(Variables.uPassions, "");
                    intent.putStringArrayListExtra("passionIds", tempIdList);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        dialog.show();
    }


    @Override
    public void onBackPressed() {
        if(tempList.size() == 0){
            showDialog(context.getString(R.string.doyouwanttoremove),context.getString(R.string.thiswillremove), true);
        }else if(tempList.size() > 2){
            if(tempList.size()>0){
                for(int i=0; i<tempList.size(); i++){
                    sb.append(tempList.get(i));
                    sb.append(", ");
                }
                sb.deleteCharAt(sb.length()-2);
                Intent intent = new Intent();
                intent.putExtra(Variables.uPassions, String.valueOf(sb));
                intent.putStringArrayListExtra("passionIds", tempIdList);
                setResult(RESULT_OK, intent);
                super.onBackPressed();
            }else {
                super.onBackPressed();
            }
        }else {
            showDialog(context.getString(R.string.youmustselect),context.getString(R.string.todisplaypassions), false);
        }
    }
}