package com.tuncay.superlotteryluckynumbers.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.tuncay.superlotteryluckynumbers.R;
import com.tuncay.superlotteryluckynumbers.SavedActivity;
import com.tuncay.superlotteryluckynumbers.constant.Constant;
import com.tuncay.superlotteryluckynumbers.model.Coupon;
import com.tuncay.superlotteryluckynumbers.service.IServerService;

import java.util.ArrayList;
import java.util.TreeSet;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CustomSavedListAdapter extends BaseAdapter implements ListAdapter {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;

    private ArrayList<String> mData = new ArrayList<String>();
    private TreeSet<Integer> sectionHeader = new TreeSet<Integer>();
    private IServerService serverService;
    private Realm realm;
    private LayoutInflater mInflater;
    private String couponIdToDelete;
    private Context context;

    private IListener listener;

    public interface IListener {
        void onDeleteCoupon(View v);
    }

    public void setListener(IListener listener) {
        this.listener = listener;
    }

    public CustomSavedListAdapter(Context context) {
        this.context = context;
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Realm.init(context);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constant.serverUrlBase)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        serverService = retrofit.create(IServerService.class);
    }

    public void addItem(final String item) {
        mData.add(item);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mData.remove(position);
        notifyDataSetChanged();
    }

    public void addSectionHeaderItem(final String item) {
        mData.add(item);
        sectionHeader.add(mData.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return sectionHeader.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public String getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int arg0) {
        return true;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int rowType = getItemViewType(position);
        holder = new ViewHolder();
        if (convertView == null) {

            switch (rowType) {
                case TYPE_ITEM:
                    convertView = mInflater.inflate(R.layout.saved_item, null);
                    break;
                case TYPE_SEPARATOR:
                    convertView = mInflater.inflate(R.layout.saved_header, null);
                    break;

            }
        }
        switch (rowType) {
            case TYPE_ITEM:
                holder.btnSavedSil = (Button) convertView.findViewById(R.id.btnSavedSil);
                holder.btnSavedSil.setTag(position);
                holder.btnSavedSil.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO: realm ve servere kaydet

                        final ProgressDialog dialog = new ProgressDialog(context, ProgressDialog.THEME_HOLO_DARK);
                        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        dialog.setTitle("Lütfen bekleyin");
                        dialog.setMessage("Kupon siliniyor...");
                        dialog.show();

                        try {
                            Thread.sleep(600);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        realm = Realm.getDefaultInstance();

                        couponIdToDelete = mData.get(position).split(";")[2];
                        final Coupon couponToDelete = realm.where(Coupon.class)
                                .equalTo("couponId", couponIdToDelete).findFirst();
                        realm.beginTransaction();
                        couponToDelete.setDeleted(true);
                        couponToDelete.setServerCalled("F");
                        realm.commitTransaction();
                        realm.refresh();

                        Call<Boolean> couponDeleteCall = serverService.deleteCoupon(couponIdToDelete);
                        couponDeleteCall.enqueue(new Callback<Boolean>() {
                            @Override
                            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                dialog.dismiss();
                                if (response.isSuccessful()) {
                                    realm = Realm.getDefaultInstance();
                                    realm.beginTransaction();
                                    couponToDelete.setServerCalled("T");
                                    realm.commitTransaction();
                                    realm.refresh();
                                } else {
                                    //Log.d("CustomListAdapter", "response unsuccessful" + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<Boolean> call, Throwable t) {
                                dialog.dismiss();
                                //Log.d("CustomListAdapter", "response failure");
                            }
                        });
                        removeItem(position);
                    }
                });

                holder.tvSaved = (TextView) convertView.findViewById(R.id.tvSaved);
                holder.tvSavedWin = (TextView) convertView.findViewById(R.id.tvSavedWin);
                String[] data = mData.get(position).split(";");
                holder.tvSaved.setText(data[0]);

                int myNum = 0;
                try {
                    myNum = Integer.parseInt(data[1]);
                } catch (NumberFormatException nfe) {
                    System.out.println("Could not parse " + nfe);
                }

                if (myNum < 1) {
                    holder.tvSavedWin.setText("");
                    holder.tvSavedWin.setTextColor(Color.parseColor("#000000"));
                } else if (myNum < 3) {
                    holder.tvSavedWin.setText(data[1] + " tuttu");
                    holder.tvSavedWin.setTextColor(Color.parseColor("#EEEEEE"));
                } else {
                    holder.tvSavedWin.setText(data[1] + " tuttu!");
                    holder.tvSavedWin.setTextColor(Color.parseColor("#EE0000"));
                }
                break;
            case TYPE_SEPARATOR:
                holder.tvSaved = (TextView) convertView.findViewById(R.id.textSeparator);
                holder.tvSaved.setText(mData.get(position));
                break;
        }
        convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }

        return convertView;
    }

    private static class ViewHolder {
        private Button btnSavedSil;
        private TextView tvSaved;
        private TextView tvSavedWin;
    }

}
