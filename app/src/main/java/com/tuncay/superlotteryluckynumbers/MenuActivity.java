package com.tuncay.superlotteryluckynumbers;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.tuncay.superlotteryluckynumbers.model.Coupon;
import com.tuncay.superlotteryluckynumbers.model.SavedListElement;
import com.tuncay.superlotteryluckynumbers.service.IServerService;
import com.tuncay.superlotteryluckynumbers.service.MyFireBaseInstanceIDService;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MenuActivity";
    private SignInButton mGoogleButton;
    private Button mLogoutBtn;
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private LinearLayout signInBar;
    private LinearLayout signOutBar;
    private String userMail;
    private Realm realm;
    private String urlBase = "https://superlotteryluckynumbersserver.eu-gb.mybluemix.net/api/";
    private IServerService serverService;
    private Coupon couponToDelete;
    private String[] firstInfoStrings = {
            "- Google hesabınızla uygulamaya giriş yapın\n" +
            "- Süper Loto için kendi şanslı numaralarınızı üretin\n" +
            "- Şanslı numaralarınızı kaydedin\n" +
            "- Çekiliş sonrası sonuçları telefonunuza gönderelim!\n",
            "Şanslı Numara Bul ekranından\n" +
            "- Şanslı kelimeniz yardımıyla numaralarınızı üretin\n" +
            "veya\n" +
            "- Şanslı numaralarınızı kendiniz girin\n",
            "- Numaralarınızı girdikten sonra\n" +
            "- Kaydedilecek olanları seçin\n" +
            "- Çekiliş tarihini seçin\n" +
            "- Kaydet'i tıklayıp şanslı numaralarınızı kaydedin"
    };
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_menu);

        mAuth = FirebaseAuth.getInstance();

        signInBar = (LinearLayout) findViewById(R.id.sign_in_bar);
        signOutBar = (LinearLayout) findViewById(R.id.sign_out_bar);


        if (mAuth.getCurrentUser() == null){
            signOutBar.setVisibility(View.GONE);
            signInBar.setVisibility(View.VISIBLE);
        }
        else{
            signOutBar.setVisibility(View.VISIBLE);
            signInBar.setVisibility(View.GONE);
        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null){
                    signOutBar.setVisibility(View.GONE);
                    signInBar.setVisibility(View.VISIBLE);
                }
                else{
                    signOutBar.setVisibility(View.VISIBLE);
                    signInBar.setVisibility(View.GONE);
                    try {
                        MyFireBaseInstanceIDService fireBaseInstanceIDService = new MyFireBaseInstanceIDService();
                        fireBaseInstanceIDService.saveUserMail(MenuActivity.this);
                        syncServer(firebaseAuth.getCurrentUser().getEmail());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        };

        mGoogleButton = (SignInButton) findViewById(R.id.btnGoogle);
        mLogoutBtn = (Button) findViewById(R.id.btnSignOut);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.DEBUG ? getString(R.string.default_web_client_id) : "730265778713-2mdufhs33m3t71i9at1oo36abbmbmdkd.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MenuActivity.this, "Google bağlantı hatası.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        mLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
            }
        });

        Realm.init(this);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlBase)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        serverService = retrofit.create(IServerService.class);

        MobileAds.initialize(this, "ca-app-pub-5819132225601729~6536327892");
        loadBannerAd();

        showFirstInfoDialog();

    }

    private void loadBannerAd() {
        mAdView = (AdView) findViewById(R.id.adBannerView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("B919CF34582CDFA602B3A23BBF6A5516")
                .build();
        mAdView.loadAd(adRequest);
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
                // TODO Auto-generated method stub
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
        if (getFirstInfoCurrentPage){
            firstInfoCurrentPage = sPref.getInt("firstInfoCurrentPage", 0) % firstInfoStrings.length;
        }else {
            firstInfoCurrentPage = 0;
        }

        tvFirstInfoInfo.setText(firstInfoStrings[firstInfoCurrentPage]);
        if (btnFirstInfoSonraki.getText().equals("Bitti")) {
            d.dismiss();
        }else if (firstInfoCurrentPage == firstInfoStrings.length - 1){
            btnFirstInfoSonraki.setText("Bitti");
        }else {
            btnFirstInfoSonraki.setText("Sonraki");
        }
        SharedPreferences.Editor editor = sPref.edit();
        editor.putInt("firstInfoCurrentPage", ++firstInfoCurrentPage);
        editor.putBoolean("firstInfoDialogShow", !cbFirstInfoGosterme.isChecked());
        editor.apply();
    }


    private void syncServer(String userMail) {
        realm = Realm.getDefaultInstance();
        RealmResults<Coupon> notSyncedRealmResults = realm.where(Coupon.class).equalTo("user", userMail).equalTo("isDeleted", true).equalTo("serverCalled", false).findAll();

        if (notSyncedRealmResults != null && !notSyncedRealmResults.isEmpty()){
            for (Coupon coupon : notSyncedRealmResults) {
                this.couponToDelete = coupon;
                String couponIdToDelete = coupon.getCouponId();
                Call<Boolean> couponDeleteCall = serverService.deleteCoupon(couponIdToDelete);
                couponDeleteCall.enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if (response.isSuccessful()){
                            realm = Realm.getDefaultInstance();
                            realm.beginTransaction();
                            couponToDelete.setServerCalled(true);
                            realm.commitTransaction();
                        }
                        else{
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
    }

    public void SevdigimKelimeAl(View view){
        if (!getSevdigimKelime().isEmpty()){
            Intent intent = new Intent(MenuActivity.this, MainActivity.class);
            intent.putExtra("userMail", mAuth.getCurrentUser() == null ? "" : mAuth.getCurrentUser().getEmail());
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

        btnKelimeTamam.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (edtSevdigimKelime.getText().toString().isEmpty())
                    return;

                SharedPreferences sharedPref = MenuActivity.this.getSharedPreferences("sevdigimKelime", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("kelime", edtSevdigimKelime.getText().toString());
                editor.apply();

                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                intent.putExtra("userMail", mAuth.getCurrentUser() == null ? "" : mAuth.getCurrentUser().getEmail());
                startActivity(intent);
                d.dismiss();
            }
        });
        btnKelimeIptal.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();


    }

    private String getSevdigimKelime(){
        SharedPreferences sPref = getSharedPreferences("sevdigimKelime", MODE_PRIVATE);
        String sevdigimKelime = sPref.getString("kelime", "");
        return sevdigimKelime;
    }

    public void Kuponlarim(View view){
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Kayıtlı kuponlarınızı görmek için giriş yapın", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, SavedActivity.class);
        intent.putExtra("userName", mAuth.getCurrentUser().getEmail());
        startActivity(intent);
    }

    public void SonCekilis(View view){
        Intent intent = new Intent(this, SonCekilisActivity.class);
        startActivity(intent);
    }

    public void Ayarlar(View view){
        Intent intent = new Intent(this, AyarlarActivity.class);
        startActivity(intent);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText(MenuActivity.this, "Google sign-in hatası: " + result.getStatus().getStatusCode()
                                + "-" + result.getStatus().getStatusMessage() + "-" + result.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            //Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(MenuActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
