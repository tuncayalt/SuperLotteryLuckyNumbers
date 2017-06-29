package com.tuncay.superlotteryluckynumbers;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.tuncay.superlotteryluckynumbers.adapter.CustomMainListAdapter;
import com.tuncay.superlotteryluckynumbers.model.Coupon;
import com.tuncay.superlotteryluckynumbers.model.MainListElement;
import com.tuncay.superlotteryluckynumbers.service.IServerService;
import com.tuncay.superlotteryluckynumbers.tools.BitmapUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

    EditText edtNum1, edtNum2, edtNum3, edtNum4, edtNum5, edtNum6;
    ListView listView;
    ArrayList<Integer> currentNums;
    ArrayList<View> views;
    ArrayList<MainListElement> elements;
    ArrayAdapter<MainListElement> adapter;
    int maxNumber = 54;
    int numOfFields = 6;
    Spinner s;
    ArrayList<String> array_spinner;
    ArrayAdapter<String> spinnerAdapter;
    int pickerViewNumber = 0;
    int pickerMinNumber = 0;
    int pickerMaxNumber = 54;
    String urlBase = "https://superlotteryluckynumbersserver.eu-gb.mybluemix.net/api/";
    IServerService serverService;

    List<Coupon> couponList;
    List<Coupon> managedCouponList;
    String userId;
    Realm realm;
    ProgressDialog progress;
    InterstitialAd mInterstitialAd;
    private static final int CAMERA_REQUEST = 1888;
    private TessBaseAPI mTess; //Tess API reference
    String datapath = ""; //path to folder containing language data file
    Uri imageFileUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentNums = new ArrayList<Integer>();
        edtNum1 = (EditText) findViewById(R.id.edtNum1);
        edtNum2 = (EditText) findViewById(R.id.edtNum2);
        edtNum3 = (EditText) findViewById(R.id.edtNum3);
        edtNum4 = (EditText) findViewById(R.id.edtNum4);
        edtNum5 = (EditText) findViewById(R.id.edtNum5);
        edtNum6 = (EditText) findViewById(R.id.edtNum6);
        views = getViewsInLayout((LinearLayout) findViewById(R.id.linearLayout));

        for (View v : views) {
            if (v instanceof EditText) {
                ((EditText) v).setKeyListener(null);
            }
        }

        listView = (ListView) findViewById(R.id.listView);
        elements = new ArrayList<>();
        adapter = new CustomMainListAdapter(this, elements);
        listView.setAdapter(adapter);

        array_spinner = new ArrayList<>();
        spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, array_spinner);
        s = (Spinner) findViewById(R.id.spnDate);
        s.setAdapter(spinnerAdapter);

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlBase)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        serverService = retrofit.create(IServerService.class);

        datapath = getFilesDir() + "/tesseract/";
        //make sure training data has been copied
        checkFile(new File(datapath + "tessdata/"));
        //initialize Tesseract API
        String lang = "eng";
        mTess = new TessBaseAPI();
        mTess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);
//        mTess.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890");
//        mTess.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-qwertyuiop[]}{POIU" +
//                "YTREWQasdASDfghFGHjklJKLl;L:'\"\\|~`xcvXCVbnmBNM,./<>?");

        GetDates task = new GetDates();
        task.execute();
        MobileAds.initialize(this, "ca-app-pub-5819132225601729~6536327892");


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-5819132225601729/8013061097");
        requestNewInterstitial();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("B919CF34582CDFA602B3A23BBF6A5516")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
    }

    public void Sifirla() {
        for (View v : views) {
            if (v instanceof EditText) {
                ((EditText) v).setText("");
                v.setBackgroundResource(R.drawable.textview_round);
            }
        }
        pickerMinNumber = 0;
        pickerViewNumber = 0;
    }

    public void KupondanAl(View view) {
//        File file = new File(getFilesDir().getAbsolutePath() + "/kupon.jpg");
//        Uri imageFileUri = Uri.fromFile(file);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("MainActivity", ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                imageFileUri = FileProvider.getUriForFile(this,
                        "com.tuncay.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "kupon";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap image = null;
            try {
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageFileUri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4; // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
                image = BitmapFactory.decodeFile(imageFileUri.getPath(), options);


                image = modifyImage(image);
                showKupondanAlDialog(image);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private Bitmap modifyImage(Bitmap bitmap) throws IOException {

        ExifInterface exif = new ExifInterface(imageFileUri.getPath());
        int exifOrientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);

        int rotate = 0;

        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
        }
        //rotate = 90;

        if (rotate != 0) {

            // Getting width & height of the given image.
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            // Setting pre rotate
            Matrix mtx = new Matrix();
            mtx.postRotate(rotate);

            // Rotating Bitmap
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
        }

        bitmap = Bitmap.createScaledBitmap(bitmap, 1191, 2000, false);
        bitmap = BitmapUtils.setGrayscale(bitmap);
        //bitmap = BitmapUtils.removeNoise(bitmap);

        return bitmap;

    }

    private void copyFiles() {
        try {
            //location we want the file to be at
            String filepath = datapath + "/tessdata/eng.traineddata";

            //get access to AssetManager
            AssetManager assetManager = getAssets();

            //open byte streams for reading/writing
            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            //copy the file to the location specified by filepath
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkFile(File dir) {
        //directory does not exist, but we can successfully create it
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles();
        }
        //The directory exists, but there is no data file in it
        if (dir.exists()) {
            String datafilepath = datapath + "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    private void showKupondanAlDialog(Bitmap image) {
        String OCRresult = null;
        mTess.setImage(image);
        OCRresult = mTess.getUTF8Text();
        Log.d("MainActivity", "OCRresult:" + OCRresult);
        mTess.end();

        Dialog d = new Dialog(this);
        d.setTitle("Numara düzelt");
        d.setContentView(R.layout.kupondan_al_dialog);
        d.setCanceledOnTouchOutside(false);
        ImageView imgFoto = (ImageView) d.findViewById(R.id.imgFoto);
        imgFoto.setImageBitmap(image);
        d.show();
    }

    public void NumPicker(final View view) {
        Sifirla();
        final Dialog d = new Dialog(this);
        d.setTitle("NumberPicker");
        d.setContentView(R.layout.numpicker);
        d.setCanceledOnTouchOutside(false);
        Button b1 = (Button) d.findViewById(R.id.btnNumPickTamam);
        Button b2 = (Button) d.findViewById(R.id.btnNumPickIptal);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.npNumPiker);
        np.setMaxValue(pickerMaxNumber);
        np.setMinValue(pickerMinNumber + 1);
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(this);
        (views.get(0)).setBackgroundResource(R.drawable.textview_selected);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((EditText) views.get(pickerViewNumber)).setText(leftPadding(np.getValue()));
                views.get(pickerViewNumber).setBackgroundResource(R.drawable.textview_round);
                pickerMinNumber = np.getValue();
                if (pickerMinNumber >= pickerMaxNumber && pickerViewNumber < 5) {
                    Sifirla();
                    np.setMinValue(1);
                    np.setValue(1);
                    views.get(pickerViewNumber).setBackgroundResource(R.drawable.textview_selected);
                    return;
                }
                np.setMinValue(pickerMinNumber + 1);
                pickerViewNumber++;
                if (pickerViewNumber >= 6) {
                    pickerMinNumber = 0;
                    pickerViewNumber = 0;
                    ListeyeEkle();
                    d.dismiss();
                } else {
                    views.get(pickerViewNumber).setBackgroundResource(R.drawable.textview_selected);
                }
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sifirla();

                d.dismiss();
            }
        });
        d.show();
    }

    public void Kaydet(View view) {
        if (elements == null || elements.size() == 0) {
            Toast.makeText(this, "Henüz bir kupon oluşturmadınız.", Toast.LENGTH_LONG).show();
            return;
        }

        SharedPreferences sharedPref = this.getSharedPreferences("firebaseUserToken", MODE_PRIVATE);
        userId = sharedPref.getString("userId", "");

        showProgress("Kuponları kaydediyor...");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                goToSaved(userId);
            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS.SSS");

        String date = sdf.format(new Date());
        String lotteryTime = s.getSelectedItem().toString();
        String[] dateLotteryArr = lotteryTime.split("/");
        String dateLottery = dateLotteryArr[2] + "/" + dateLotteryArr[1] + "/" + dateLotteryArr[0];
        couponList = new ArrayList<>();

        //JSONArray couponJsonArr = new JSONArray();

        for (MainListElement li : elements) {
            if (li.getOyna()) {

                Coupon coupon = new Coupon();
                coupon.setCouponId(java.util.UUID.randomUUID().toString());
                coupon.setUser(userId);
                coupon.setGameType("Sup");
                coupon.setNumbers(li.getNumString());
                coupon.setPlayTime(date);
                coupon.setLotteryTime(dateLottery);
                coupon.setToRemind("T");
                coupon.setServerCalled(false);
                coupon.setWinCount(-1);
                coupon.setDeleted(false);
                couponList.add(coupon);
            }
        }
        realm.beginTransaction();
        managedCouponList = realm.copyToRealm(couponList);
        realm.commitTransaction();

        Call<Boolean> couponsCall = serverService.insertCoupon(couponList);
        couponsCall.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    for (Coupon coupon : managedCouponList) {
                        coupon.setServerCalled(true);
                    }
                    realm.commitTransaction();
                    realm.refresh();
                }
                progress.dismiss();
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    goToSaved(userId);
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                progress.dismiss();
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    goToSaved(userId);
                }
            }
        });

    }

    private void showProgress(String message) {
        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage(message);
        progress.show();
    }

    private void goToSaved(String userId) {
        Intent intent = new Intent(this, SavedActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }

    public void SayiSil(View view) {
        int position = (Integer) view.getTag();
        MainListElement element = adapter.getItem(position);
        elements.remove(element);
        adapter.notifyDataSetChanged();
    }

    public void Doldur(View view) {
        Random random = new Random(getRanSeed());
        ArrayList<Integer> randomIntegers = getRandomIntegers(random, numOfFields);
        Collections.sort(randomIntegers);
        int i = 0;
        for (View v : views) {
            if (v instanceof EditText) {

                fillWithLuckyNum(((EditText) v), randomIntegers.get(i));
                i++;
            }
        }
        ListeyeEkle();
    }

    public long getRanSeed() {
        SharedPreferences sp = getSharedPreferences("sevdigimKelime", MODE_PRIVATE);
        String kelime = sp.getString("kelime", "");
        return kelime.hashCode() ^ System.nanoTime();
    }

    public void ListeyeEkle() {

        ArrayList<Integer> currentNums = getCurrentNums();
        if (currentNums.size() < numOfFields)
            return;

        String currentNumString = "";
        for (int i = 0; i < currentNums.size(); i++) {
            currentNumString += leftPadding(currentNums.get(i));
            if (i < currentNums.size() - 1)
                currentNumString += "-";
        }

        MainListElement listElement = new MainListElement(currentNumString, true);

        elements.add(listElement);
        adapter.notifyDataSetChanged();

    }

    public void Sec(View view) {
        MainListElement le = elements.get((Integer) view.getTag());

        le.setOyna(((CheckBox) view).isChecked());
        adapter.notifyDataSetChanged();
    }

    public ArrayList<Integer> getRandomIntegers(Random random, int count) {
        int randomNum = 0;
        ArrayList<Integer> result = new ArrayList<Integer>();

        for (int i = 0; i < count; i++) {
            do {
                randomNum = random.nextInt(maxNumber) + 1;
            } while (result.contains(randomNum));
            result.add(randomNum);
        }
        return result;
    }

    private void fillWithLuckyNum(EditText edtNum, int number) {
        edtNum.setText(leftPadding(number));
    }

    private String leftPadding(Integer number) {
        String unpadded = Integer.toString(number);
        return "00".substring(unpadded.length()) + unpadded;
    }


    public ArrayList<Integer> getCurrentNums() {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (View v : views) {
            if (v instanceof EditText && isNumeric(((EditText) v).getText().toString())) {
                result.add(Integer.parseInt(((EditText) v).getText().toString()));
            }
        }
        return result;
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public ArrayList<View> getViewsInLayout(LinearLayout ll) {
        ArrayList<View> result = new ArrayList<View>();
        final int childcount = ll.getChildCount();
        for (int i = 0; i < childcount; i++) {
            View v = ll.getChildAt(i);
            result.add(v);
        }
        return result;
    }


    class GetDates extends AsyncTask<Object, Void, ArrayList<Date>> {

        @Override
        protected ArrayList<Date> doInBackground(Object... objects) {
            ArrayList<Date> result = new ArrayList<>();

            Calendar date = Calendar.getInstance();
            while (date.get(Calendar.DAY_OF_WEEK) != Calendar.THURSDAY)
                date.add(Calendar.DATE, 1);

            for (int i = 0; i < 5; i++) {
                result.add(date.getTime());
                date.add(Calendar.DATE, 7);
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Date> result) {
            Format df = new SimpleDateFormat("dd/MM/yyyy");

            for (int i = 0; i < result.size(); i++) {
                array_spinner.add(df.format(result.get(i)));
            }

            spinnerAdapter.notifyDataSetChanged();
        }
    }
}
