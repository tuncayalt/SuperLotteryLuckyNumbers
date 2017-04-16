package com.tuncay.superlotteryluckynumbers.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.tuncay.superlotteryluckynumbers.SonCekilisActivity;

/**
 * Created by mac on 16.04.2017.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String REG_TOKEN = "REG_TOKEN";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    public void onTokenRefresh() {
        String recentToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(REG_TOKEN, recentToken);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null){

                }
                else{

                }
            }
        };


        String userMail = mAuth.getCurrentUser().getEmail();

        sendRegistrationToServer(recentToken, userMail);
    }

    private void sendRegistrationToServer(String recentToken, String userMail) {






        SharedPreferences sharedPref = getSharedPreferences("firebaseUserToken", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("token", recentToken);
        editor.putString("userMail", userMail);
        editor.commit();
    }
}
