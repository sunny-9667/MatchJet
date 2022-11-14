package com.Match.binderstatic.ApiClasses;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.Match.binderstatic.Constants;
import com.Match.binderstatic.Interfaces.Callback;
import com.Match.binderstatic.R;
import com.Match.binderstatic.SimpleClasses.Functions;
import com.Match.binderstatic.SimpleClasses.Variables;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class ApiRequest {

    public static void callApi(final Context context, String url, JSONObject jsonObject,
                               final Callback callback){
        SharedPreferences pref = context.getSharedPreferences(Variables.prefName, MODE_PRIVATE);

        Functions.printLog(url);
        Functions.printLog(jsonObject.toString());

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Functions.printLog(response.toString());

                        if(callback!=null)
                            callback .response(response.toString());

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Functions.printLog(error.toString());

                if(callback!=null)
                    callback .response(error.toString());

            }
        })
        {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Api-Key", Constants.API_KEY);
                headers.put("User-Id", pref.getString(Variables.uid,null));
                headers.put("Auth-Token", pref.getString(Variables.authToken,null));
                Functions.printLog(headers.toString());
                return headers;

            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(240000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.getCache().clear();
        requestQueue.add(jsonObjReq);
    }


    public static void callApiGetRequest(final Activity context, final String url,
                                         final Callback callback) {

        SharedPreferences pref = context.getSharedPreferences(Variables.prefName, MODE_PRIVATE);
        Functions.printLog(url);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        final String[] urlsplit = url.split("/");
                        Log.d(Constants.tag + urlsplit[urlsplit.length - 1], response.toString());

                        Functions.printLog(response.toString());

                        if(callback!=null)
                            callback .response(response.toString());

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                final String[] urlsplit = url.split("/");
                Log.d(Constants.tag + urlsplit[urlsplit.length - 1], error.toString());


                if (callback != null)
                    callback.response(error.toString());

            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Api-Key", Constants.API_KEY);
                headers.put("User-Id", pref.getString(Variables.uid,null));
                headers.put("Auth-Token", pref.getString(Variables.authToken,null));
                Functions.printLog( headers.toString());
                return headers;
            }
        };

        try {
            if (context != null) {
                RequestQueue requestQueue = Volley.newRequestQueue(context);
                jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(60000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                requestQueue.getCache().clear();
                requestQueue.add(jsonObjReq);
            }
        } catch (Exception e) {
            Functions.printLog( e.toString());
        }
    }


}
