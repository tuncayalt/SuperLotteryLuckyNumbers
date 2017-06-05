package com.tuncay.superlotteryluckynumbers.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.tuncay.superlotteryluckynumbers.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyFireBaseInstanceIDService extends FirebaseInstanceIdService {

    String urlBase = "https://superlotteryluckynumbersserver.eu-gb.mybluemix.net/api/";
    String recentToken;
    String userId;
    String prevToken;
    String pushCekilis;
    Context context;
    private IServerService serverService;

    @Override
    public void onTokenRefresh() {
        saveToken();
    }

    public MyFireBaseInstanceIDService() {
        this.context = this;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlBase)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        serverService = retrofit.create(IServerService.class);
    }

    public void saveToken() {
        recentToken = FirebaseInstanceId.getInstance().getToken();
        //Log.d(REG_TOKEN, recentToken);

        SharedPreferences sharedPref = this.getSharedPreferences("firebaseUserToken", MODE_PRIVATE);

        if (sharedPref != null) {
            prevToken = sharedPref.getString("token", "");
            userId = sharedPref.getString("userId", "");
            pushCekilis = sharedPref.getString("pushCekilis", "RT");

        } else {
            prevToken = "";
            userId = "";
            pushCekilis = "RT";
        }
        if (userId.equals("")) {
            userId = java.util.UUID.randomUUID().toString();
        }

        sharedPref = this.getSharedPreferences("firebaseUserToken", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("token", recentToken);
        editor.putString("userId", userId);
        editor.apply();

        if (!recentToken.equals(prevToken) || pushCekilis.substring(0, 1).equals("D")) {
            sendRegistrationToServer();
        }
    }

    public void sendRegistrationToServer() {

        User user = new User();
        user.setPrev_token(prevToken);
        user.setRecent_token(recentToken);
        user.setUser_id(userId);
        user.setPush_cekilis(pushCekilis);
        user.setPush_win(pushCekilis);

        Call<Boolean> userCall;
        userCall = serverService.updateUserToken(user);
        userCall.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    SharedPreferences sharedPref = context.getSharedPreferences("firebaseUserToken", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("token", recentToken);
                    editor.putString("pushCekilis", pushCekilis.replace('D', 'R'));
                    editor.apply();
                } else {

                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {

            }
        });

    }
}
