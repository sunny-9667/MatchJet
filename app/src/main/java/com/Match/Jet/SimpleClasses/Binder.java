package com.Match.Jet.SimpleClasses;

import static com.Match.Jet.SimpleClasses.ImagePipelineConfigUtils.getDefaultImagePipelineConfig;

import android.app.Application;
import android.os.Build;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.Match.binderstatic.Constants;
import com.Match.Jet.R;
import com.Match.binderstatic.SimpleClasses.Functions;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;

/**
 * Created by qboxus on 10/19/2018.
 */

public class Binder extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Fresco.initialize(this,getDefaultImagePipelineConfig(this));
        }
        else
        {
            Fresco.initialize(this);
        }
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
                .enabled(true) //default: true
                .showErrorDetails(true) //default: true
                .showRestartButton(true) //default: true
                .logErrorOnRestart(true) //default: true
                .trackActivities(true) //default: false
                .minTimeBetweenCrashesMs(2000) //default: 3000
                .errorDrawable(R.drawable.image_placeholder) //default: bug image
                .restartActivity(CustomErrorActivity.class) //default: null (your app's launch activity)
                .errorActivity(CustomErrorActivity.class) //default: null (default error activity)
                .eventListener(new CustomEventListener()) //default: null
                .apply();
    }

    private static class CustomEventListener implements CustomActivityOnCrash.EventListener {
        @Override
        public void onLaunchErrorActivity() {
            Functions.printLog( "onLaunchErrorActivity()");
        }

        @Override
        public void onRestartAppFromErrorActivity() {
            Functions.printLog( "onRestartAppFromErrorActivity()");
        }

        @Override
        public void onCloseAppFromErrorActivity() {
            Functions.printLog( "onCloseAppFromErrorActivity()");
        }
    }

}
