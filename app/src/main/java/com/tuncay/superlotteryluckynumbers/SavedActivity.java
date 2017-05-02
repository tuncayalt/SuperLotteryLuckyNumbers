package com.tuncay.superlotteryluckynumbers;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import com.tuncay.superlotteryluckynumbers.adapter.CustomSavedListAdapter;
import com.tuncay.superlotteryluckynumbers.model.Coupon;
import com.tuncay.superlotteryluckynumbers.model.SavedListElement;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class SavedActivity extends AppCompatActivity {

    ListView lvSavedList;
    CustomSavedListAdapter adapter;
    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saved_coupons);


        lvSavedList = (ListView) findViewById(R.id.lvSavedList);
        adapter = new CustomSavedListAdapter(this);

        lvSavedList.setAdapter(adapter);

        Realm.init(this);



        GetSaved task = new GetSaved();
        task.execute();
    }

    class GetSaved extends AsyncTask<String, Void, ArrayList<SavedListElement>> {

        @Override
        protected ArrayList<SavedListElement> doInBackground(String... params) {
            ArrayList<SavedListElement> result = new ArrayList<>();

            realm = Realm.getDefaultInstance();
            String userName = SavedActivity.this.getIntent().getStringExtra("userName");
            final RealmResults<Coupon> coupons = realm.where(Coupon.class).equalTo("User", userName).findAll();

            if (!coupons.isEmpty()){
                for (Coupon coupon : coupons) {
                    String numString = coupon.getNumbers();
                    String lotteryDate = coupon.getLotteryTime();
                    SavedListElement le = new SavedListElement(lotteryDate, numString, 0);
                    result.add(le);
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<SavedListElement> result) {
            for (int i = 0; i < result.size(); i++) {
                SavedListElement sle = result.get(i);
                String ld = sle.getLotteryDate();
                if (i == 0 || !ld.equals(result.get(i - 1).getLotteryDate())) {
                    adapter.addSectionHeaderItem(ld + " çekilişi");
                }
                if (sle.getWinCount() >= 3){
                    adapter.addItem(sle.getNumString() + ";" + sle.getWinCount() + " tuttu.");
                }else{
                    adapter.addItem(sle.getNumString());
                }
            }

            adapter.notifyDataSetChanged();
        }
    }
}
