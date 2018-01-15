package com.tuncay.superlotteryluckynumbers.service;

/**
 * Created by mac on 15.01.2018.
 */
import com.tuncay.superlotteryluckynumbers.R;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

public class AppRater {
    private static String APP_TITLE = "";
    private static String APP_PNAME = "";

    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 8;
    private final static int SHOW_ONCE_IN_LAUNCHES = 4;

    public static void app_launched(Context mContext) {
        setVariables(mContext);

        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT && launch_count % SHOW_ONCE_IN_LAUNCHES == 0) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(mContext, editor);
            }
        }

        editor.commit();
    }

    private static void setVariables(Context mContext) {
        Resources resources = mContext.getResources();
        APP_TITLE = resources.getString(R.string.app_name);
        APP_PNAME = resources.getString(R.string.package_name);
    }

    public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        setVariables(mContext);

        final Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.apprater_dialog);
        dialog.setTitle(APP_TITLE + " uygulamasını oylayın" );
        dialog.setCanceledOnTouchOutside(false);

        final CheckBox cbAppRaterGosterme = (CheckBox)dialog.findViewById(R.id.cbAppRaterGosterme);

        Button btnAppRaterRate = (Button)dialog.findViewById(R.id.btnAppRaterRate);
        btnAppRaterRate.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (editor != null && cbAppRaterGosterme.isChecked()) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                dialog.dismiss();
            }
        });

        Button btnAppRaterCancel = (Button) dialog.findViewById(R.id.btnAppRaterCancel);
        btnAppRaterCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (editor != null && cbAppRaterGosterme.isChecked()) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}