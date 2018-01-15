package com.tuncay.superlotteryluckynumbers;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.tuncay.superlotteryluckynumbers.constant.Constant;
import com.tuncay.superlotteryluckynumbers.model.Coupon;
import com.tuncay.superlotteryluckynumbers.service.AppRater;
import com.tuncay.superlotteryluckynumbers.service.IServerService;
import com.tuncay.superlotteryluckynumbers.service.MyFireBaseInstanceIDService;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MenuActivity";
    private Realm realm;
    private String userId;
    private IServerService serverService;
    private Coupon couponToDelete;
    private String[] firstInfoStrings = {
            "- Süper Loto için kendi şanslı numaralarınızı üretin\n" +
                    "- Şanslı numaralarınızı kaydedin\n" +
                    "- Çekiliş sonrası sonuçları telefonunuza gönderelim!\n",
            "Şanslı Numara Bul ekranından\n" +
                    "- Şanslı kelimeniz yardımıyla numaralarınızı üretin\n" +
                    "veya\n" +
                    "- Şanslı numaralarınızı kendiniz girin\n",
            "- Numaralarınızı girdikten sonra\n" +
                    "- Çekiliş tarihini seçin\n" +
                    "- Kaydet'i tıklayıp şanslı numaralarınızı kaydedin",
            "KUPONLARIM sayfasından,\n" +
                    "Çekilişin sonucu belli ise sonuçları anında görün,\n" +
                    "Belli değil ise sonuçları açıklanınca görün\n"
    };
    private AdView mAdView;
    List<Coupon> notSyncedCouponList;
    RealmResults<Coupon> notSyncedAddedRealmResults;
    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_menu);

        Realm.init(this);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constant.serverUrlBase)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        userId = getUserId();

        serverService = retrofit.create(IServerService.class);

        MobileAds.initialize(this, "ca-app-pub-5819132225601729~6536327892");
        loadBannerAd();

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-5819132225601729/6918771341");
        requestNewInterstitial();

        showFirstInfoDialog();

        AppRater.app_launched(this);
    }

    private void loadBannerAd() {
        mAdView = (AdView) findViewById(R.id.adBannerView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("B919CF34582CDFA602B3A23BBF6A5516")
                .build();
        mAdView.loadAd(adRequest);
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("B919CF34582CDFA602B3A23BBF6A5516")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    private void showFirstInfoDialog() {
        SharedPreferences sPref = getSharedPreferences("firstInfoDialog", MODE_PRIVATE);
        boolean firstInfoDialogShow = sPref.getBoolean("firstInfoDialogShow", true);
        if (!firstInfoDialogShow) {
            return;
        }

        final Dialog d = new Dialog(this);
        d.setTitle("Uygulama Kullanımı");
        d.setContentView(R.layout.first_info_dialog);
        d.setCanceledOnTouchOutside(false);

        final Button btnFirstInfoSonraki = (Button) d.findViewById(R.id.btnFirstInfoSonraki);
        Button btnFirstInfoSkip = (Button) d.findViewById(R.id.btnFirstInfoSkip);
        final TextView tvFirstInfoInfo = (TextView) d.findViewById(R.id.tvFirstInfoInfo);
        final CheckBox cbFirstInfoGosterme = (CheckBox) d.findViewById(R.id.cbFirstInfoGosterme);

        d.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    SharedPreferences sPref = getSharedPreferences("firstInfoDialog", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sPref.edit();
                    editor.putInt("firstInfoCurrentPage", 0);
                    editor.putBoolean("firstInfoDialogShow", !cbFirstInfoGosterme.isChecked());
                    editor.apply();
                    d.dismiss();
                }
                return true;
            }
        });

        NextInfo(tvFirstInfoInfo, d, btnFirstInfoSonraki, cbFirstInfoGosterme, false);

        btnFirstInfoSonraki.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NextInfo(tvFirstInfoInfo, d, btnFirstInfoSonraki, cbFirstInfoGosterme, true);
            }
        });
        btnFirstInfoSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sPref = getSharedPreferences("firstInfoDialog", MODE_PRIVATE);
                SharedPreferences.Editor editor = sPref.edit();
                editor.putInt("firstInfoCurrentPage", 0);
                editor.putBoolean("firstInfoDialogShow", !cbFirstInfoGosterme.isChecked());
                editor.apply();
                d.dismiss();
            }
        });
        d.show();

    }

    private void NextInfo(TextView tvFirstInfoInfo, Dialog d, Button btnFirstInfoSonraki, CheckBox cbFirstInfoGosterme, boolean getFirstInfoCurrentPage) {
        SharedPreferences sPref = getSharedPreferences("firstInfoDialog", MODE_PRIVATE);
        int firstInfoCurrentPage;
        if (getFirstInfoCurrentPage) {
            firstInfoCurrentPage = sPref.getInt("firstInfoCurrentPage", 0) % firstInfoStrings.length;
        } else {
            firstInfoCurrentPage = 0;
        }

        tvFirstInfoInfo.setText(firstInfoStrings[firstInfoCurrentPage]);
        if (btnFirstInfoSonraki.getText().equals("Bitti")) {
            d.dismiss();
        } else if (firstInfoCurrentPage == firstInfoStrings.length - 1) {
            btnFirstInfoSonraki.setText("Bitti");
        } else {
            btnFirstInfoSonraki.setText("Sonraki");
        }
        SharedPreferences.Editor editor = sPref.edit();
        editor.putInt("firstInfoCurrentPage", ++firstInfoCurrentPage);
        editor.putBoolean("firstInfoDialogShow", !cbFirstInfoGosterme.isChecked());
        editor.apply();
    }


    private void syncServer() {
        SyncDeleted();
        SyncAdded();
    }

    private void SyncAdded() {
        realm = Realm.getDefaultInstance();
        notSyncedAddedRealmResults = realm.where(Coupon.class).equalTo("user", userId).equalTo("isDeleted", false).equalTo("serverCalled", "F").findAll();
        notSyncedCouponList = realm.copyFromRealm(notSyncedAddedRealmResults);
        if (notSyncedCouponList != null && !notSyncedCouponList.isEmpty()) {
            final Call<Boolean> couponsAddCall = serverService.insertCoupon(notSyncedCouponList);
            couponsAddCall.enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    if (response.isSuccessful()) {
                        realm = Realm.getDefaultInstance();
                        notSyncedAddedRealmResults = realm.where(Coupon.class).equalTo("user", userId).equalTo("isDeleted", false).equalTo("serverCalled", "F").findAll();
                        realm.beginTransaction();
                        for (Coupon coupon : notSyncedAddedRealmResults) {
                            coupon.setServerCalled("T");
                        }
                        realm.commitTransaction();
                        realm.refresh();
                    } else {
                        //Log.d("CustomListAdapter", "response unsuccessful" + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    //Log.d("CustomListAdapter", "response failure");
                }
            });

        }
    }

    private void SyncDeleted() {
        realm = Realm.getDefaultInstance();
        RealmResults<Coupon> notSyncedDeletedRealmResults = realm.where(Coupon.class).equalTo("user", userId).equalTo("isDeleted", true).equalTo("serverCalled", "F").findAll();

        if (notSyncedDeletedRealmResults != null && !notSyncedDeletedRealmResults.isEmpty()) {
            List<String> couponIdListToDelete = new ArrayList<>();

            for (Coupon coupon : notSyncedDeletedRealmResults) {
                couponIdListToDelete.add(coupon.getCouponId());
            }

            Call<Boolean> couponDeleteCall = serverService.deleteCoupon(couponIdListToDelete.toString());
            couponDeleteCall.enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    if (response.isSuccessful()) {
                        realm = Realm.getDefaultInstance();
                        RealmResults<Coupon> notSyncedDeletedRealmResults = realm.where(Coupon.class).equalTo("user", userId).equalTo("isDeleted", true).equalTo("serverCalled", "F").findAll();
                        realm.beginTransaction();
                        for (Coupon coupon : notSyncedDeletedRealmResults) {
                            coupon.setServerCalled("T");
                        }
                        realm.commitTransaction();
                        realm.refresh();
                    } else {
                        Log.d("CustomListAdapter", "response unsuccessful" + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    Log.d("CustomListAdapter", "response failure");
                }
            });

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userId.equals(""))
            return;
        ServerSyncTask task = new ServerSyncTask();
        task.execute();
    }

    private class ServerSyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            MyFireBaseInstanceIDService fireBaseInstanceIDService = new MyFireBaseInstanceIDService();
            fireBaseInstanceIDService.saveUser(MenuActivity.this);
            syncServer();
            return "";
        }
    }

    public void SevdigimKelimeAl(View view) {
        if (!getSevdigimKelime().isEmpty()) {
            Intent intent = new Intent(MenuActivity.this, MainActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            return;
        }

        final Dialog d = new Dialog(this);
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        d.setTitle("Sevdigim Sayı");
        d.setContentView(R.layout.sevdigim_kelime);
        d.setCanceledOnTouchOutside(false);
        Button btnKelimeTamam = (Button) d.findViewById(R.id.btnKelimeTamam);
        Button btnKelimeIptal = (Button) d.findViewById(R.id.btnKelimeIptal);
        final EditText edtSevdigimKelime = (EditText) d.findViewById(R.id.edtSevdigimKelime);

        btnKelimeTamam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtSevdigimKelime.getText().toString().isEmpty())
                    return;

                SharedPreferences sharedPref = MenuActivity.this.getSharedPreferences("sevdigimKelime", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("kelime", edtSevdigimKelime.getText().toString());
                editor.apply();

                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                d.dismiss();
            }
        });
        btnKelimeIptal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();


    }

    private String getUserId() {
        SharedPreferences sPref = getSharedPreferences("firebaseUserToken", MODE_PRIVATE);
        String userId = sPref.getString("userId", "");
        return userId;
    }

    private String getSevdigimKelime() {
        SharedPreferences sPref = getSharedPreferences("sevdigimKelime", MODE_PRIVATE);
        String sevdigimKelime = sPref.getString("kelime", "");
        return sevdigimKelime;
    }

    public void Kuponlarim(View view) {
        SharedPreferences shaPref = getSharedPreferences("menuInterAd", MODE_PRIVATE);
        long adLastTime = shaPref.getLong("adLastTime", 0);

        if (System.currentTimeMillis() - adLastTime > Constant.interstatialWaitTime) {
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    requestNewInterstitial();
                    goToSaved(userId);
                }
            });
            SharedPreferences sharedPref = getSharedPreferences("menuInterAd", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong("adLastTime", System.currentTimeMillis());
            editor.apply();

            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                goToSaved(userId);
            }
        } else {
            goToSaved(userId);
        }
    }

    private void goToSaved(String userId) {
        Intent intent = new Intent(this, SavedActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    public void SonCekilis(View view) {
        SharedPreferences shaPref = getSharedPreferences("menuInterAd", MODE_PRIVATE);
        long adLastTime = shaPref.getLong("adLastTime", 0);

        if (System.currentTimeMillis() - adLastTime > Constant.interstatialWaitTime) {
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    requestNewInterstitial();
                    goToSonCekilis();
                }
            });
            SharedPreferences sharedPref = getSharedPreferences("menuInterAd", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong("adLastTime", System.currentTimeMillis());
            editor.apply();

            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                goToSonCekilis();
            }
        } else {
            goToSonCekilis();
        }
    }

    private void goToSonCekilis() {
        Intent intent = new Intent(this, SonCekilisActivity.class);
        startActivity(intent);
    }

    public void Ayarlar(View view) {
        Intent intent = new Intent(this, AyarlarActivity.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
