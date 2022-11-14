package com.Match.Jet.SimpleClasses;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.Match.binderstatic.Constants;
import com.Match.binderstatic.SimpleClasses.Variables;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.SwipeDirection;

import org.json.JSONObject;

public class Functions {


    public static void swipeLeft(CardStackView cardStackView) {
        View target = cardStackView.getTopView();
        View targetOverlay = cardStackView.getTopView().getOverlayContainer();

        ValueAnimator rotation = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("rotation", -10f));
        rotation.setDuration(200);
        ValueAnimator translateX = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationX", 0f, -2000f));
        ValueAnimator translateY = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationY", 0f, 500f));
        translateX.setStartDelay(400);
        translateY.setStartDelay(400);
        translateX.setDuration(500);
        translateY.setDuration(500);
        AnimatorSet cardAnimationSet = new AnimatorSet();
        cardAnimationSet.playTogether(rotation, translateX, translateY);

        ObjectAnimator overlayAnimator = ObjectAnimator.ofFloat(targetOverlay, "alpha", 0f, 1f);
        overlayAnimator.setDuration(200);
        AnimatorSet overlayAnimationSet = new AnimatorSet();
        overlayAnimationSet.playTogether(overlayAnimator);

        cardStackView.swipe(SwipeDirection.Left, cardAnimationSet, overlayAnimationSet);
    }

    public static void swipeRight(CardStackView cardStackView) {
        View target = cardStackView.getTopView();
        View targetOverlay = cardStackView.getTopView().getOverlayContainer();

        ValueAnimator rotation = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("rotation", 10f));
        rotation.setDuration(200);
        ValueAnimator translateX = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationX", 0f, 2000f));
        ValueAnimator translateY = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationY", 0f, 500f));
        translateX.setStartDelay(400);
        translateY.setStartDelay(400);
        translateX.setDuration(500);
        translateY.setDuration(500);
        AnimatorSet cardAnimationSet = new AnimatorSet();
        cardAnimationSet.playTogether(rotation, translateX, translateY);

        ObjectAnimator overlayAnimator = ObjectAnimator.ofFloat(targetOverlay, "alpha", 0f, 1f);
        overlayAnimator.setDuration(200);
        AnimatorSet overlayAnimationSet = new AnimatorSet();
        overlayAnimationSet.playTogether(overlayAnimator);

        cardStackView.swipe(SwipeDirection.Right, cardAnimationSet, overlayAnimationSet);
    }
//access private storage
    public static String getAppFolder(Context activity)
    {
        try {
            return activity.getExternalFilesDir(null).getPath()+"/";
        }
        catch (Exception e)
        {
            return Environment.getDataDirectory().getPath()+"/";
        }
    }

    public static void swipeTop(CardStackView cardStackView) {
        View target = cardStackView.getTopView();
        View targetOverlay = cardStackView.getTopView().getOverlayContainer();

        ValueAnimator translateX = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationX", 0f, 0f));
        ValueAnimator translateY = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationY", 0f, -2000f));
        translateX.setStartDelay(400);
        translateY.setStartDelay(400);
        translateX.setDuration(500);
        translateY.setDuration(500);
        AnimatorSet cardAnimationSet = new AnimatorSet();
        cardAnimationSet.playTogether( translateX, translateY);

        ObjectAnimator overlayAnimator = ObjectAnimator.ofFloat(targetOverlay, "alpha", 0f, 1f);
        overlayAnimator.setDuration(200);
        AnimatorSet overlayAnimationSet = new AnimatorSet();
        overlayAnimationSet.playTogether(overlayAnimator);

        cardStackView.swipe(SwipeDirection.Top, cardAnimationSet, overlayAnimationSet);
    }

    public static void updateUserPreference(Context context, JSONObject user){
      SharedPreferences.Editor editor= com.Match.binderstatic.SimpleClasses.Functions.getSharedPreference(context).edit();
    }


    // use for image loader and return controller for image load
    public static DraweeController frescoImageLoad(String url, int resource, SimpleDraweeView simpleDrawee, boolean isGif)
    {
        if (url==null)
        {
            url="null";
        }
        if (!url.contains(Variables.http)) {
            url = Constants.BASE_URL + url;
        }
        Log.d(Constants.tag,"URLURL: "+url);

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .build();

        DraweeController controller;
        simpleDrawee.getHierarchy().setPlaceholderImage(resource);
        simpleDrawee.getHierarchy().setFailureImage(resource);
        if (isGif)
        {
            controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setOldController(simpleDrawee.getController())
                    .setAutoPlayAnimations(true)
                    .build();
        }
        else
        {
            controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setOldController(simpleDrawee.getController())
                    .build();
        }



        return controller;
    }


    public static DraweeController frescoImageLoad(Uri uri, int resource, SimpleDraweeView simpleDrawee, boolean isGif)
    {

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .build();

        DraweeController controller;
        simpleDrawee.getHierarchy().setPlaceholderImage(resource);
        simpleDrawee.getHierarchy().setFailureImage(resource);
        if (isGif)
        {
            controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setOldController(simpleDrawee.getController())
                    .setAutoPlayAnimations(true)
                    .build();
        }
        else
        {
            controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setOldController(simpleDrawee.getController())
                    .build();
        }



        return controller;
    }

    // use for image loader and return controller for image load
    public static DraweeController frescoImageLoad(Drawable drawable, SimpleDraweeView simpleDrawee, boolean isGif)
    {


        DraweeController controller;
        simpleDrawee.getHierarchy().setPlaceholderImage(drawable);
        simpleDrawee.getHierarchy().setFailureImage(drawable);
        if (isGif)
        {
            controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(simpleDrawee.getController())
                    .setAutoPlayAnimations(true)
                    .build();
        }
        else
        {
            controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(simpleDrawee.getController())
                    .build();
        }

        return controller;
    }
}
