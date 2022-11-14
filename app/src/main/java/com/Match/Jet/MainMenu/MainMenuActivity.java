package com.Match.Jet.MainMenu;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetailsParams;
import com.google.firebase.messaging.FirebaseMessaging;
import com.Match.Jet.BuildConfig;
import com.Match.binderstatic.ApiClasses.ApiLinks;
import com.Match.binderstatic.ApiClasses.ApiRequest;
import com.Match.binderstatic.Constants;
import com.Match.binderstatic.Models.PurchaseModel;
import com.Match.binderstatic.SimpleClasses.Variables;
import com.Match.binderstatic.Interfaces.Callback;
import com.Match.Jet.SimpleClasses.ContextWrapper;
import com.Match.binderstatic.SimpleClasses.Functions;
import com.Match.Jet.SimpleClasses.VersionChecker;
import com.Match.Jet.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.android.billingclient.api.BillingClient.SkuType.SUBS;


public class MainMenuActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    private MainMenuFragment mainMenuFragment;
    long mBackPressed;

    public static String userPic;

    DatabaseReference rootref;

    public static String actionType ="none";
    public static String receiverid="none";
    public static String title="none";
    public static String receiverPic ="none";
    BillingClient billingClient;

    public static MainMenuActivity mainMenuActivity;

    Snackbar snackbar;


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
        setContentView(R.layout.activity_main_menu);

        mainMenuActivity=this;

        userPic = Functions.getSharedPreference(this)
                .getString(Variables.uPic,"null");

        if(userPic != null || userPic.equals("")) {
            userPic = "null";
        }

        rootref = FirebaseDatabase.getInstance().getReference();

        callApiShowLicense();

        if(getIntent().hasExtra("action_type")){
            actionType = getIntent().getExtras().getString("action_type");
            receiverid = getIntent().getExtras().getString("receiverid");
            title = getIntent().getExtras().getString("title");
            receiverPic = getIntent().getExtras().getString("icon");
        }

        if (!checkPermissionForCameraAndMicrophone()) {
            requestPermissionForCameraAndMicrophone();
        }


        try {
            if (savedInstanceState == null) {
                initScreen();
            } else {
                mainMenuFragment = (MainMenuFragment) getSupportFragmentManager().getFragments().get(0);
            }
        }catch (Exception e){}


        getPublicIP();

        Functions.registerConnectivity(this, response -> {
            if(response.equalsIgnoreCase("disconnected")) {
                snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.no_internet, Snackbar.LENGTH_INDEFINITE);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.red));
                snackbar.show();
            }else {
                if(snackbar!=null)
                    snackbar.dismiss();
            }
        });

        initializeBill();

        int usedCoins = Functions.getSharedPreference(this).getInt(Variables.usedCoins, 0);
        String callType = Functions.getSharedPreference(this).getString(Variables.callType, "");
        if(usedCoins > 0){
            callApiForUseCoins(usedCoins, callType);
        }
    }

    public void getPublicIP() {
        ApiRequest.callApiGetRequest(this, "https://api.ipify.org/?format=json", resp -> {
            try {
                JSONObject responce = new JSONObject(resp);
                String ip = responce.optString("ip");

                addFirebaseToken(ip);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    public void addFirebaseToken(String ip) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    Functions.getSharedPreference(MainMenuActivity.this)
                            .edit().putString(Variables.deviceToken, token).apply();

                    JSONObject params = new JSONObject();
                    try {
                        params.put("user_id", Functions.getSharedPreference(MainMenuActivity.this).getString(Variables.uid,""));
                        params.put("device_token", token);
                        params.put("device", getString(R.string.device));
                        params.put("version", BuildConfig.VERSION_NAME);
                        params.put("ip", "" + ip);
                        params.put("lat", Functions
                                .getSharedPreference(MainMenuActivity.this)
                                .getString(Variables.currentLat,""));
                        params.put("long", Functions
                                .getSharedPreference(MainMenuActivity.this)
                                .getString(Variables.currentLon,""));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    ApiRequest.callApi(MainMenuActivity.this, ApiLinks.addDeviceData, params, null);

                });

    }


    public void initializeBill(){
        billingClient= BillingClient.newBuilder(this).enablePendingPurchases()
                .setListener(this::onPurchasesUpdated)
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    InitPurchases();
                    checkAllProducts();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Functions.printLog("onBillingServiceDisconnected");
            }
        });
    }


    private void InitPurchases() {
        final List<String> skuList=new ArrayList<>();
        skuList.add(Constants.productID);
        skuList.add(Constants.productID2);
        skuList.add(Constants.productID3);

        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList);

        params.setType(SUBS);
        billingClient.querySkuDetailsAsync(params.build()
                , (billingResult, list) -> {
                    if (billingResult.getResponseCode()==BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                        Functions.printLog(" Already purchase");
                    }
                });
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {

    }

    public void checkAllProducts(){
        if(billingClient.isReady()) {
            billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS, (billingResult, list) -> {
                if(list != null){
                    if(list.size()>0){
                        Functions.getSharedPreference(MainMenuActivity.this).edit().putBoolean(Variables.isProductPurchase,true).commit();
                        for (PurchaseHistoryRecord purchaseHistoryRecord : list){
                            if(purchaseHistoryRecord.getSkus().contains(Constants.productID)){
                                break;
                            }else if(purchaseHistoryRecord.getSkus().contains(Constants.productID2)){
                                break;
                            }else if(purchaseHistoryRecord.getSkus().contains(Constants.productID3)){
                                 break;
                            }
                        }
                    }

                    else {
                        Functions.getSharedPreference(MainMenuActivity.this).edit().putBoolean(Variables.isProductPurchase,Constants.enableSubscribe).commit();
                        callApiEndSubscription();
                    }
                }else {
                    Functions.printLog( "list != null");
                }
            });
        }
        else {
            Toast.makeText(this, "Billing not Ready", Toast.LENGTH_SHORT).show();
        }
    }


    private void initScreen() {
        mainMenuFragment = new MainMenuFragment();
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, mainMenuFragment).commit();

        findViewById(R.id.container).setOnClickListener(v ->
                Functions.hideSoftKeyboard(MainMenuActivity.this)
        );
    }


    // on start we will save the latest token into the firebase
    @Override
    protected void onStart() {
        super.onStart();
        rootref.child("Users")
                .child(Functions.getSharedPreference(MainMenuActivity.this).getString(Variables.uid,""))
                .child("token")
                .setValue(Functions.getSharedPreference(MainMenuActivity.this).getString(Variables.deviceToken,""));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            setLanguageLocal();
        }
        //checkVersion();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (!mainMenuFragment.onBackPressed()) {
            int count = this.getSupportFragmentManager().getBackStackEntryCount();
            if (count == 0){
                if (mBackPressed + 2000 > System.currentTimeMillis()) {
                    super.onBackPressed();
                    return;
                } else {
                    mBackPressed = System.currentTimeMillis();
                }
            } else {
                super.onBackPressed();
            }
        }
    }


    @Override
    protected void onDestroy() {
        Functions.getSharedPreference(this).edit().putBoolean(Variables.userLikeLimit, false).commit();

        Functions.unRegisterConnectivity(this);
        super.onDestroy();
    }


    public void setLanguageLocal(){
        String[] languageArray = getResources().getStringArray(R.array.language_code);
        List<String> languageCode= Arrays.asList(languageArray);
        String language=Functions.getSharedPreference(MainMenuActivity.this)
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


    // this method will get the version of app from play store and complate it with our present app version
    // and show the update message to update the application
    public void checkVersion(){
        VersionChecker versionChecker = new VersionChecker(this);
        versionChecker.execute();
    }


    private void callApiForUseCoins(int usedCoins, String type) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions
                    .getSharedPreference(MainMenuActivity.this)
                    .getString(Variables.uid,""));
            parameters.put("coin", ""+usedCoins);
            if(type.equals("voice_call")){
                parameters.put("feature", "audio");
            }else {
                parameters.put("feature", "video");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(MainMenuActivity.this, ApiLinks.useCoin, parameters, resp -> {
            try {
                JSONObject jsonObject = new JSONObject(resp);

                String code = jsonObject.optString("code");
                if(code.equals("200")){
                    JSONObject userObject = jsonObject.optJSONObject("msg").optJSONObject("User");
                    Functions.getSharedPreference(MainMenuActivity.this)
                            .edit().putInt(Variables.usedCoins, 0).apply();
                    Functions.getSharedPreference(MainMenuActivity.this).edit()
                            .putString(Variables.uWallet, userObject.getString("wallet")).apply();

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void callApiEndSubscription() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(MainMenuActivity.this).getString(Variables.uid,""));
            parameters.put("package","gold");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(this, ApiLinks.endSubscription, parameters, null);

    }

    private void callApiShowLicense() {
        ApiRequest.callApi(MainMenuActivity.this, ApiLinks.showLicense, new JSONObject(), resp -> {
            try {
                JSONObject jsonObject = new JSONObject(resp);

                String code = jsonObject.optString("code");
                if(code.equals("200")){
                    Functions.getSharedPreference(MainMenuActivity.this).edit().
                            putBoolean(Variables.isCodePurchase, true).commit();
                }
                else {
                    Functions.getSharedPreference(MainMenuActivity.this).edit().
                            putBoolean(Variables.isCodePurchase, false).commit();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }


    private boolean checkPermissionForCameraAndMicrophone() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            return resultCamera == PackageManager.PERMISSION_GRANTED &&
                    resultMic == PackageManager.PERMISSION_GRANTED ;
        } else {
            return true;
        }
    }

    Dialog dialog;
    private void requestPermissionForCameraAndMicrophone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            }
            else {
                dialog = new Dialog(this);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.item_permission_dialog1);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                dialog.findViewById(R.id.okayTv).setOnClickListener(view -> requestPermissions(
                        new String[]{Manifest.permission.CAMERA},
                        Variables.PERMISSION_CAMERA_CODE));
                dialog.show();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Variables.PERMISSION_CAMERA_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                                Variables.PERMISSION_RECORDING_AUDIO);
                    }
                } else {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
                break;
            case Variables.PERMISSION_RECORDING_AUDIO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPermissionForCameraAndMicrophone()) {
                        if(dialog!=null){
                            dialog.cancel();
                        }
                    }
                } else {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
                break;

        }
    }

}
