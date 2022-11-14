package com.Match.Jet.ActivitiesFragments.Accounts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.chaos.view.PinView;
import com.Match.Jet.MainMenu.MainMenuActivity;
import com.Match.Jet.SimpleClasses.GpsUtils;
import com.Match.binderstatic.ApiClasses.ApiLinks;
import com.Match.binderstatic.ApiClasses.ApiRequest;
import com.Match.binderstatic.Constants;
import com.Match.binderstatic.SimpleClasses.Variables;
import com.Match.binderstatic.Models.UserMultiplePhoto;
import com.Match.binderstatic.SimpleClasses.Functions;
import com.Match.binderstatic.Interfaces.Callback;
import com.Match.Jet.Models.UserModel;
import com.Match.Jet.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class Phone_Otp_F extends Fragment implements View.OnClickListener {

    View view;
    Context context;

    CountDownTimer timer;
    TextView tv1, resendCode, continueTv;
    public static TextView edit_num;

    ImageView back;
    RelativeLayout rl1,continueButton;
    SharedPreferences sharedPreferences;
    String phone_num;
    UserModel user_model;
    PinView et_code;
    boolean isFromLogin;

    List<UserMultiplePhoto> imagesList;

    Date c;
    SimpleDateFormat df;
    int currentYear;

    public Phone_Otp_F() {
        // Required empty public constructor
    }

    public Phone_Otp_F(boolean isFromLogin) {
        this.isFromLogin = isFromLogin;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_otp, container, false);

        context = getActivity();

        c = Calendar.getInstance().getTime();
        df = new SimpleDateFormat("yyyy", Locale.getDefault());
        currentYear = Integer.parseInt(df.format(c));

        Bundle bundle = getArguments();
        if(bundle!=null){
            phone_num = bundle.getString("phone_number");
            user_model = (UserModel) bundle.getSerializable("user_data");
            if(!user_model.email.isEmpty()){
                user_model.isFromPh = false;
            }
        }
        sharedPreferences = getActivity().getSharedPreferences(Variables.prefName, MODE_PRIVATE);

        if(!isFromLogin){
            Signup_A.progressBar.setProgress((int) Functions.calculateSegmentProgress(
                    Signup_A.pager.getCurrentItem() + 1,
                    Signup_A.pager.getOffscreenPageLimit()));
        }

        initializeViews();
        setClickListeners();
        if(isFromLogin){
            oneMinuteTimer(context);
        }

        et_code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                String txtName = et_code.getText().toString();
                if (txtName.length() > 3) {
                    continueButton.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_pink_background));
                    continueTv.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                    callApiForCodeVerification();
                } else {
                    continueButton.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_google_background));
                    continueTv.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    public void initializeViews(){
        tv1 = (TextView) view.findViewById(R.id.tv1_id);
        resendCode = (TextView) view.findViewById(R.id.resend_code);
        edit_num = (TextView) view.findViewById(R.id.edit_num_id);
        if(isFromLogin){
            edit_num.setText(phone_num);
        }

        back = view.findViewById(R.id.Goback);
        rl1 = view.findViewById(R.id.rl1_id);

        continueButton = view.findViewById(R.id.continueButton);
        continueTv = view.findViewById(R.id.continue_tv);
        et_code = view.findViewById(R.id.et_code);
    }

    public void setClickListeners(){
        back.setOnClickListener(this);
        resendCode.setOnClickListener(this);
        edit_num.setOnClickListener(this);
        continueButton.setOnClickListener(this);
    }

    public void oneMinuteTimer(Context context){
        rl1 = view.findViewById(R.id.rl1_id);
        rl1.setVisibility(View.VISIBLE);
        timer = new CountDownTimer(60000,1000){
            @Override
            public void onTick(long l) {
                tv1.setText(context.getString(R.string.resend_code)+" "+ l/1000);
            }

            @Override
            public void onFinish() {
                rl1.setVisibility(View.GONE);
                resendCode.setVisibility(View.VISIBLE);
            }

        };
        timer.start();

    }

    private void callApiForCodeVerification() {
        JSONObject parameters = new JSONObject();
        try {
            if(isFromLogin){
                parameters.put("phone", phone_num);
                parameters.put("verify","1");
                parameters.put("code", et_code.getText().toString());
            }else{
                parameters.put("phone", Phone_F.phoneNo);
                parameters.put("verify","1");
                parameters.put("code", et_code.getText().toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.showLoader(getActivity(),false,false);
        ApiRequest.callApi(getActivity(), ApiLinks.verifyPhoneNo, parameters, new Callback() {
            @Override
            public void response(String resp) {
                Functions.cancelLoader();
                parseOtpData(resp);
            }
        });
    }

    public void parseOtpData(String loginData){
        try {
            JSONObject jsonObject=new JSONObject(loginData);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                callApiForPhoneRegistration();
            }else{
                Toast.makeText(getContext(), jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void callApiForPhoneRegistration() {
        JSONObject parameters = new JSONObject();
        try {
            if(isFromLogin){
                parameters.put("phone", phone_num);
            }else {
                parameters.put("phone", Phone_F.phoneNo);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.showLoader(getActivity(),false,false);
        ApiRequest.callApi(getActivity(), ApiLinks.registerUser, parameters, new Callback() {
            @Override
            public void response(String resp) {
                Functions.cancelLoader();
                parseLoginData(resp);
            }
        });
    }

    public void parseLoginData(String loginData){
        try {
            timer.cancel();
            JSONObject jsonObject=new JSONObject(loginData);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                JSONObject jsonArray=jsonObject.getJSONObject("msg");
                JSONObject userdata = jsonArray.getJSONObject("User");
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putString(Variables.uid,userdata.optString("id"));
                editor.putString(Variables.fName,userdata.optString("first_name"));
                editor.putString(Variables.lName,userdata.optString("last_name"));
                editor.putString(Variables.uName,userdata.optString("username"));
                editor.putString(Variables.gender,userdata.optString("gender"));

                JSONArray userImagesArray = jsonObject.optJSONObject("msg").optJSONArray("UserImage");

                imagesList = new ArrayList<>();
                for(int i = 0; i<6; i++){
                    UserMultiplePhoto model = new UserMultiplePhoto();
                    if(i<userImagesArray.length()){
                        model.setImage(userImagesArray.optJSONObject(i).optString("image"));
                        model.setId(userImagesArray.optJSONObject(i).getString("id"));
                        model.setOrderSequence(Integer.parseInt(userImagesArray.optJSONObject(i).getString("order_sequence")));
                        imagesList.add(i, model);
                    }else {
                        model.setOrderSequence(i);
                        imagesList.add(i, model);
                    }
                }

                Collections.sort(imagesList, new Comparator<UserMultiplePhoto>() {
                    @Override public int compare(UserMultiplePhoto p1, UserMultiplePhoto p2) {
                        return p1.getOrderSequence() - p2.getOrderSequence(); // Ascending
                    }
                });

                if(imagesList.size()>0){
                    editor.putString(Variables.uPic, imagesList.get(0).getImage());
                }

                if(!userdata.optString("dob").equals("0000-00-00")){
                    try {
                        Date date = df.parse(userdata.optString("dob"));
                        int age = Integer.parseInt(df.format(date));
                        editor.putString(Variables.birthDay, " " + (currentYear - age));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if(userdata.optString("hide_me").equals("0")){
                    editor.putBoolean(Variables.showMeOnTinder, true);
                }else {
                    editor.putBoolean(Variables.showMeOnTinder, false);
                }

                if(userdata.optString("hide_age").equals("0")){
                    editor.putBoolean(Variables.hideAge, false);
                }else {
                    editor.putBoolean(Variables.hideAge, true);
                }

                if(userdata.optString("hide_location").equals("0")){
                    editor.putBoolean(Variables.hide_distance, false);
                }else {
                    editor.putBoolean(Variables.hide_distance, true);
                }

                editor.putString(Variables.school, ""+jsonObject.optJSONObject("msg").optJSONObject("School").optString("name"));

                editor.putString(Variables.uTotalBoost, userdata.optString("total_boost"));
                editor.putString(Variables.uBoost,userdata.optString("boost"));
                editor.putString(Variables.uWallet,userdata.optString("wallet"));
                editor.putBoolean(Variables.islogin, true);
                editor.putString(Variables.authToken, userdata.optString("auth_token"));
                editor.commit();

                gpsStatus();

            } else if(code.equals("201") && jsonObject.optString("msg").contains("open register screen")){
                if(isFromLogin){
                    user_model.phone_no = phone_num;
                    user_model.isFromPh = true;
                    user_model.isSocialLogin = false;
                    Intent intent = new Intent(getActivity(), Signup_A.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user_model", user_model);
                    intent.putExtra("bundle",bundle);
                    startActivity(intent);
                }else {
                    Signup_A.pager.setCurrentItem(Signup_A.pager.getCurrentItem()+1);
                    Signup_A.progressBar.setProgress((int) Functions.calculateSegmentProgress(
                            Signup_A.pager.getCurrentItem() + 1,
                            Signup_A.pager.getOffscreenPageLimit()));
                }

            }else{
                Toast.makeText(getContext(), jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void gpsStatus() {
        LocationManager locationManager = (LocationManager)
                getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Functions.cancelLoader();
        if (!gpsStatus) {
            new GpsUtils(getActivity()).turnGPSOn(new GpsUtils.onGpsListener() {
                @Override
                public void gpsStatus(boolean isGPSEnable) {

                }
            });
        } else if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            enableLocation();
        }else{
            startActivity(new Intent(getActivity(), MainMenuActivity.class));
            getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            getActivity().finishAffinity();
        }
    }

    private void enableLocation() {
        startActivity(new Intent(getActivity(), EnableLocation_A.class));
        requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        getActivity().finishAffinity();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.Goback:
                if(isFromLogin){
                    getActivity().onBackPressed();
                }else {
                    Signup_A.progressBar.setProgress(13);
                    Signup_A.pager.setCurrentItem(Signup_A.pager.getCurrentItem()-1);
                }
                break;

            case R.id.resend_code:
                oneMinuteTimer(context);
                et_code.setText("");
                resendCode.setVisibility(View.GONE);
                break;


            case R.id.edit_num_id:
                getActivity().onBackPressed();
                break;

            case R.id.continueButton:
                callApiForCodeVerification();
                break;
        }
    }


    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if(!isFromLogin && view!=null){
            oneMinuteTimer(getActivity());
        }
    }
}