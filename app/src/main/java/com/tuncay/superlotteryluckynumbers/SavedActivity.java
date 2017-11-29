package com.tuncay.superlotteryluckynumbers;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.tuncay.superlotteryluckynumbers.adapter.CustomSavedListAdapter;
import com.tuncay.superlotteryluckynumbers.constant.Constant;
import com.tuncay.superlotteryluckynumbers.model.Coupon;
import com.tuncay.superlotteryluckynumbers.model.SavedListElement;
import com.tuncay.superlotteryluckynumbers.service.IServerService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SavedActivity extends AppCompatActivity implements CustomSavedListAdapter.IListener{

    ListView lvSavedList;
    CustomSavedListAdapter adapter;
    Realm realm;
    IServerService serverService;
    List<Coupon> coupons;
    ArrayList<SavedListElement> result;
    String userId;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saved_coupons);

        showProgress("Kaydedilmiş kuponlar gösteriyor...");

        lvSavedList = (ListView) findViewById(R.id.lvSavedList);
        adapter = new CustomSavedListAdapter(this);
        lvSavedList.setAdapter(adapter);
        adapter.setListener(this);

        SharedPreferences sharedPref = this.getSharedPreferences("firebaseUserToken", MODE_PRIVATE);
        userId = sharedPref.getString("userId", "");

        Realm.init(this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constant.serverUrlBase)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        serverService = retrofit.create(IServerService.class);

        GetSavedCoupons();
    }

    private void showProgress(String message) {
        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage(message);
        progress.show();
    }

    private void GetSavedCoupons() {
        result = new ArrayList<>();

        try {
            realm = Realm.getDefaultInstance();


            SharedPreferences shaPref = SavedActivity.this.getSharedPreferences("couponsGetTime", MODE_PRIVATE);
            long couponsLastTime = shaPref.getLong("couponsLastTime", 0);

            if (System.currentTimeMillis() - couponsLastTime > 120000){
                SharedPreferences sharedPref = SavedActivity.this.getSharedPreferences("couponsGetTime", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putLong("couponsLastTime", System.currentTimeMillis());
                editor.apply();

                Call<List<Coupon>> couponsCall = serverService.getCoupons(userId);
                couponsCall.enqueue(new Callback<List<Coupon>>() {
                    @Override
                    public void onResponse(Call<List<Coupon>> call, Response<List<Coupon>> response) {

                        if (response.isSuccessful()){
                            coupons = response.body();
                            if (coupons != null && !coupons.isEmpty()){
                                Iterator<Coupon> it = coupons.iterator();
                                while (it.hasNext()) {
                                    Coupon coupon = it.next();
                                    Coupon couponLocal = realm.where(Coupon.class)
                                            .equalTo("couponId", coupon.getCouponId()).findFirst();
                                    if (couponLocal != null && couponLocal.isDeleted()){
                                        it.remove();
                                    }
                                }
                                realm.beginTransaction();
                                realm.copyToRealmOrUpdate(coupons);
                                realm.commitTransaction();
                            }
                        }
                        getCouponsFromLocalDb(userId);
                    }
                    @Override
                    public void onFailure(Call<List<Coupon>> call, Throwable t) {
                        Toast.makeText(SavedActivity.this, "Kuponlar sunucudan alınamadı, İnternet bağlantınızı kontrol edin.",
                                Toast.LENGTH_SHORT).show();
                        getCouponsFromLocalDb(userId);
                    }
                });
            }
            else {
                getCouponsFromLocalDb(userId);
            }

        } catch (IllegalArgumentException e) {
            if (realm.isInTransaction())
                realm.cancelTransaction();
            e.printStackTrace();
            progress.dismiss();
        }

    }

    public void getCouponsFromLocalDb(String userId) {
        RealmResults<Coupon> couponRealmResults = realm.where(Coupon.class).equalTo("user", userId).equalTo("isDeleted", false).findAllSorted("playTime", Sort.DESCENDING);

        if (couponRealmResults != null && !couponRealmResults.isEmpty()){
            for (Coupon coupon : couponRealmResults) {
                String numString = coupon.getNumbers();
                String lotteryDate = coupon.getLotteryTime();
                int winCount = coupon.getWinCount();
                String couponId = coupon.getCouponId();
                SavedListElement le = new SavedListElement(lotteryDate, numString, winCount, couponId);
                result.add(le);
            }
        }
        for (int i = 0; i < result.size(); i++) {
            SavedListElement sle = result.get(i);
            String ld = sle.getLotteryDate();
            if (i == 0 || !ld.equals(result.get(i - 1).getLotteryDate())) {
                adapter.addSectionHeaderItem(ld + " çekilişi");
            }
            adapter.addItem(sle.getNumString() + ";" + sle.getWinCount() + ";" + sle.getCouponId());
        }

        adapter.notifyDataSetChanged();
        progress.dismiss();
    }

    @Override
    public void onDeleteCoupon(View v) {
        int pos = lvSavedList.getPositionForView((View) v.getParent());
        adapter.removeItem(pos);
    }
}
