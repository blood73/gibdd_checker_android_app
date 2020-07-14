package ru.bloodsoft.gibddchecker.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.database.HistoryDatabaseHelper;
import ru.bloodsoft.gibddchecker.models.Plate;
import ru.bloodsoft.gibddchecker.models.PlateItem;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;
import ru.bloodsoft.gibddchecker.ui.quote.ArticleDetailActivity;
import ru.bloodsoft.gibddchecker.ui.quote.ArticleDetailFragment;
import ru.bloodsoft.gibddchecker.ui.recycler_views.PlateItemRecyclerViewAdapter;
import ru.bloodsoft.gibddchecker.util.NewWebService;
import ru.bloodsoft.gibddchecker.util.RunCounts;
import ru.bloodsoft.gibddchecker.util.SanitizeHelper;
import ru.bloodsoft.gibddchecker.util.SettingsStorage;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class PlateActivity extends BaseActivity {

    String PlateNumber;

    public static final String ARG_PLATE = "phone";

    private static final String URL_GET_PLATE = SanitizeHelper.decryptString("3s2eTcBgp7+KFxz6ImBwiF0eMK+EVtDAIFIZHyvRnmx46ZTMRW8UuHTNgov5NGVa");

    static {
        System.loadLibrary("native-lib");
    }

    public static native String getEaistoUrl();
    public static native String getVinUrl();


    public static final String URL_GET_EAISTO = SanitizeHelper.decryptString(getEaistoUrl());

    public static final String URL_PUT_VIN = SanitizeHelper.decryptString(getVinUrl());

    private static final String TOKEN = SanitizeHelper.decryptString("dd+gFmxw==");
    public static final String URL_GET_POLIS ="https://dkbm-web.autoins.ru/dkbm-web-1.0/policy.htm";
    private static final String URL_GET_INSURANCE ="https://dkbm-web.autoins.ru/dkbm-web-1.0/osagovehicle.htm";

    ProgressDialog mProgressDialog;
    NewWebService getPlateRequest;
    GetPlateData getPlateData;
    NewWebService getEaistoRequest;
    GetEaistoData getEaistoData;
    NewWebService getInsuranceRequest;
    GetInsuranceData getInsuranceData;
    PutVin putVin;

    private List<PlateItem> plateList;
    private RecyclerView mRecyclerView;
    private PlateItemRecyclerViewAdapter plateAdapter;
    InterstitialAd mInterstitialAd;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Snackbar mSnackbar;
    NestedScrollView mScrollView;
    String polisSerial = "";
    String polisNumber = "";
    String vinResult = "";

    NewWebService getPolisRequest;
    GetPolisData getPolisData;

    @BindView(R.id.cardIncorrectVin)
    View cardVinIncorrect;

    @BindView(R.id.captcha_webview)
    WebView captchaWebView;

    @BindView(R.id.progress_layout)
    View progressView;

    @BindView(R.id.plate)
    EditText plateEditText;

    private static final String TAG = makeLogTag(PlateActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plate);

        ButterKnife.bind(this);

        PlateNumber = plateEditText.getText().toString();

        mScrollView = (NestedScrollView) findViewById(R.id.scrollView);

        Bundle b = PlateActivity.this.getIntent().getExtras();
        if (b != null) {
            String plate = b.getString(ARG_PLATE);
            if (plate != null && !plate.isEmpty()) {
                plateEditText.setText(plate);
            }
        }

        mRecyclerView = (RecyclerView) PlateActivity.this.findViewById(R.id.recycler_view_plate);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(PlateActivity.this));
        plateAdapter = new PlateItemRecyclerViewAdapter(PlateActivity.this, plateList);
        mRecyclerView.setAdapter(plateAdapter);

        RunCounts settings = new RunCounts();
        Boolean isAdFree = settings.isAdFree();

        if (!isAdFree) {
            mInterstitialAd = new InterstitialAd(PlateActivity.this);
            mInterstitialAd.setAdUnitId("ca-app-pub-3078563819949367/4695892338");

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    requestNewInterstitial();
                }
            });

            requestNewInterstitial();

            AdView adView = new AdView(this);

            DisplayMetrics displayMetrics = PlateActivity.this.getResources().getDisplayMetrics();
            float dpWidth = displayMetrics.widthPixels / displayMetrics.density - 40;
            adView.setAdSize(new AdSize((int) dpWidth, 300));
            adView.setAdUnitId("ca-app-pub-3078563819949367/5966252215");

            CardView card_adview = (CardView) PlateActivity.this.findViewById(R.id.cardAdView);
            card_adview.addView(adView);

            AdRequest request = new AdRequest.Builder()
                    .addTestDevice("E31198FB67C1181AF2CFCEDF4476A9D1")
                    .build();
            adView.loadAd(request);

            AdView adView2 = new AdView(this);

            adView2.setAdSize(new AdSize((int) dpWidth, 80));
            adView2.setAdUnitId("ca-app-pub-3078563819949367/1780251731");

            CardView card_adview_small = (CardView) PlateActivity.this.findViewById(R.id.cardAdViewSmall);
            card_adview_small.addView(adView2);

            AdRequest request2 = new AdRequest.Builder()
                    .addTestDevice("E31198FB67C1181AF2CFCEDF4476A9D1")
                    .build();
            adView2.loadAd(request2);

        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(PlateActivity.this);

        setupToolbar();
    }

    @Override
    public void onBackPressed() {
        if (captchaWebView.getVisibility() == View.VISIBLE) {
            captchaWebView.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    private void reloadWebView1() {
        //Hide keyboard
        LinearLayout samplesMain = (LinearLayout) findViewById(R.id.samples_main);
        InputMethodManager imm = (InputMethodManager) PlateActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(samplesMain.getWindowToken(), 0);

        CardView card_results = (CardView) PlateActivity.this.findViewById(R.id.cardResults);
        card_results.setVisibility(View.GONE);

        progressView.setVisibility(View.VISIBLE);
        captchaWebView.loadUrl(URL_GET_POLIS);
        captchaWebView.getSettings().setJavaScriptEnabled(true);
        captchaWebView.getSettings().setDomStorageEnabled(true);
        captchaWebView.addJavascriptInterface(new JavaScriptInterface(captchaWebView), "AutoinsInterface");
        captchaWebView.setWebViewClient(new AutoinsWebViewClient());
    }

    private void reloadWebView2() {
        progressView.setVisibility(View.VISIBLE);

        captchaWebView.loadUrl("about:blank");
        captchaWebView.loadUrl(URL_GET_INSURANCE);
        captchaWebView.getSettings().setJavaScriptEnabled(true);
        captchaWebView.getSettings().setDomStorageEnabled(true);
        captchaWebView.addJavascriptInterface(new JavaScriptInterface2(captchaWebView), "AutoinsInterface2");
        captchaWebView.setWebViewClient(new AutoinsWebViewClient2());
    }

    private void showAd() {
        RunCounts settings = new RunCounts();
        Boolean isAdFree = settings.isAdFree();

        if (!isAdFree) {
            SettingsStorage settingsStorage = new SettingsStorage();
            Boolean showAd = settingsStorage.isShowInterstitialSecond(PlateActivity.this);

            if (mInterstitialAd != null) {
                if (showAd && mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                    requestNewInterstitial();
                }
            }

            CardView card_adview = (CardView) PlateActivity.this.findViewById(R.id.cardAdView);
            card_adview.setVisibility(View.VISIBLE);
            CardView card_adview_small = (CardView) PlateActivity.this.findViewById(R.id.cardAdViewSmall);
            card_adview_small.setVisibility(View.GONE);
        } else {
            CardView card_adview = (CardView) PlateActivity.this.findViewById(R.id.cardAdView);
            card_adview.setVisibility(View.GONE);
        }
    }

    private void showProgressDialog() {
        // instantiate it within the onCreate method
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(PlateActivity.this);
            mProgressDialog.setMessage(PlateActivity.this.getResources().getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);

            mProgressDialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    public void onDestroy() {
        dismissProgressDialog();

        if (getPlateData != null) {
            getPlateData.cancel(true);
        }

        if (getEaistoData != null) {
            getEaistoData.cancel(true);
        }

        if (getInsuranceData != null) {
            getInsuranceData.cancel(true);
        }

        if (getPolisData != null) {
            getPolisData.cancel(true);
        }

        super.onDestroy();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("E31198FB67C1181AF2CFCEDF4476A9D1")
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    @OnClick(R.id.fab)
    public void onFabClicked(View view) {

        PlateNumber = plateEditText.getText().toString();

        if (isConnectedToInternet()) {
            if (!PlateNumber.trim().isEmpty()) {

                ArrayList<String> passing = new ArrayList<String>();
                passing.add(PlateNumber);

                //save to the history
                Plate newPlate = new Plate();
                newPlate.plateNumber = PlateNumber.toUpperCase();

                // Get singleton instance of database
                HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(PlateActivity.this);
                // Add plate to the database
                databaseHelper.addPlate(newPlate);

                RunCounts requestCounts = new RunCounts();
                requestCounts.increaseCheckAutoCount(PlateActivity.this);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "plate");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, PlateNumber);
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "PLATE");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                cardVinIncorrect.setVisibility(View.GONE);

                getEaistoData = new GetEaistoData();
                try {
                    getEaistoData.execute(passing);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                mSnackbar = Snackbar.make(view, PlateActivity.this.getResources().getString(R.string.error_empty_plate), Snackbar.LENGTH_INDEFINITE)
                        .setAction("Action", null)
                        .setDuration(2000);
                mSnackbar.show();
                //Hide keyboard
                InputMethodManager imm = (InputMethodManager) PlateActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } else {
            mSnackbar = Snackbar.make(view, PlateActivity.this.getResources().getString(R.string.error_no_internet), Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null)
                    .setDuration(2000);
            mSnackbar.show();
            //Hide keyboard
            InputMethodManager imm = (InputMethodManager) PlateActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @OnClick(R.id.paste)
    public void onPasteClicked(View view) {
        final EditText plateEditText = (EditText) findViewById(R.id.plate);
        try {
            ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = clipboard.getPrimaryClip();
            String plate = "";

            if (clipData != null) {
                ClipData.Item item = clipData.getItemAt(0);
                if (item != null && item.getText() != null) {
                    plate = item.getText().toString();
                }
            }

            if (!plate.isEmpty()) {
                plate = SanitizeHelper.sanitizeString(plate);
                plateEditText.setText(plate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.delete)
    public void onDeleteClicked(View view) {
        plateEditText.setText("");
    }

    private void setupToolbar() {
        final ActionBar ab = getActionBarToolbar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_plate;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }

    public boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    @OnClick(R.id.cardIncorrectVin)
    protected void onCardIncorrectVinClicked(View view) {
        PlateNumber = plateEditText.getText().toString();

        if (!PlateNumber.isEmpty()) {
            reloadWebView1();
            captchaWebView.setVisibility(View.VISIBLE);
        } else {
            mSnackbar = Snackbar.make(view, PlateActivity.this.getResources().getString(R.string.error_empty_plate), Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null)
                    .setDuration(2000);
            mSnackbar.show();
            //Hide keyboard
            InputMethodManager imm = (InputMethodManager) PlateActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private class GetPlateData extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
            CardView card_results = (CardView) PlateActivity.this.findViewById(R.id.cardResultsPlate);
            card_results.setVisibility(View.GONE);
            CardView card_plate_number = (CardView) PlateActivity.this.findViewById(R.id.cardPlateNumber);
            card_plate_number.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(ArrayList<String>... passing) {

            ArrayList<String> passed = passing[0]; //get passed arraylist
            Object[] mStringArray = passed.toArray();

            /*
                http://avto-nomer.ru/mobile/api_ru_photo.php?nomer=a001aa77&key=63Gcvd3SO
             */

            String plate = SanitizeHelper.transliterate((String) mStringArray[0]);
            String response = "";

            logD(TAG, "plate number: " + plate);

            String finalUrl = URL_GET_PLATE + "?key=" + TOKEN + "&nomer=" + plate;

            try {
                response = getPlateRequest.sendGet(finalUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }

            logD(TAG, "Response: " + response);

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if (PlateActivity.this != null && PlateActivity.this.isFinishing()) {
                return;
            }

            dismissProgressDialog();

            JSONObject responseJsonObject = null;
            JSONArray responseJsonArray = null;
            String plateImageUrl = "";
            Integer totalCount = 0;

            TextView textHeader = (TextView) PlateActivity.this.findViewById(R.id.details_header_plate);
            TextView textData = (TextView) PlateActivity.this.findViewById(R.id.details_data_plate);
            LinearLayout layout_result = (LinearLayout) PlateActivity.this.findViewById(R.id.layout_result_plate);
            CardView card_results = (CardView) PlateActivity.this.findViewById(R.id.cardResultsPlate);
            CardView card_plate_number = (CardView) PlateActivity.this.findViewById(R.id.cardPlateNumber);

            mRecyclerView = (RecyclerView) PlateActivity.this.findViewById(R.id.recycler_view_plate);
            mRecyclerView.setVisibility(View.GONE);
            mRecyclerView.setNestedScrollingEnabled(false);

            try {
                responseJsonObject = new JSONObject(response);
                responseJsonArray = responseJsonObject.getJSONArray("cars");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                responseJsonObject = new JSONObject(response);
                plateImageUrl = responseJsonObject.getString("informer");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!plateImageUrl.equals("") && !plateImageUrl.isEmpty() && !plateImageUrl.equals("null")) {
                card_plate_number.setVisibility(View.VISIBLE);
                ImageView plateNumberImageView = (ImageView) PlateActivity.this.findViewById(R.id.plate_image);

                if (plateNumberImageView != null && PlateActivity.this != null && !PlateActivity.this.isFinishing()) {
                    DisplayMetrics displayMetrics = App.getContext().getResources().getDisplayMetrics();
                    float dpWidth = displayMetrics.widthPixels / displayMetrics.density - 40;
                    Integer imageWidth = Math.round(dpWidth);

                    if (imageWidth == 0) {
                        imageWidth = 800;
                    }

                    if (!PlateActivity.this.isFinishing()) {
                        Glide.with(PlateActivity.this)
                                .load(plateImageUrl)
                                .apply(new RequestOptions()
                                        .placeholder(R.drawable.no_image_auto)
                                        .override(imageWidth, 65))
                                .into(plateNumberImageView);
                    }
                }

            } else {
                card_plate_number.setVisibility(View.GONE);
            }

            try {
                parsePlateResult(responseJsonArray.toString());

                totalCount = responseJsonArray.length();

                if (totalCount > 0) {
                    card_results = (CardView) PlateActivity.this.findViewById(R.id.cardResultsPlate);
                    card_results.setVisibility(View.VISIBLE);

                    card_results.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            String url = "http://avto-nomer.ru";
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);
                        }
                    });

                    textHeader.setText(PlateActivity.this.getResources().getString(R.string.plate_photo_results));
                    textData.setText(PlateActivity.this.getResources().getString(R.string.plate_photo_header));
                }

                plateAdapter = new PlateItemRecyclerViewAdapter(PlateActivity.this, plateList);
                mRecyclerView.setVisibility(View.VISIBLE);
                mRecyclerView.setAdapter(plateAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(PlateActivity.this));

            } catch (Exception e) {
                e.printStackTrace();
            }

            //Hide keyboard
            final NestedScrollView scrollView = (NestedScrollView) PlateActivity.this.findViewById(R.id.scrollView);
            InputMethodManager imm = (InputMethodManager) PlateActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(scrollView.getWindowToken(), 0);

            showAd();

            scroolToResult();

        }
    }

    private class GetEaistoData extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
            CardView card_results = (CardView) PlateActivity.this.findViewById(R.id.cardResults);
            card_results.setVisibility(View.GONE);
            Button copyVinButton = (Button) PlateActivity.this.findViewById(R.id.copy_vin);
            copyVinButton.setVisibility(View.GONE);
            CardView card_next_check = (CardView) PlateActivity.this.findViewById(R.id.cardNextGibdd);
            card_next_check.setVisibility(View.GONE);
            cardVinIncorrect.setVisibility(View.GONE);

            CardView cardNextReport = (CardView) PlateActivity.this.findViewById(R.id.cardNextReport);
            cardNextReport.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(ArrayList<String>... passing) {

            ArrayList<String> passed = passing[0]; //get passed arraylist
            Object[] mStringArray = passed.toArray();

            /*
                regNumber
             */

            String regNumber = SanitizeHelper.antiTransliterate((String) mStringArray[0]);

            logD(TAG, "gosnumber: " + regNumber);

            RequestBody formBody = new FormBody.Builder()
                    .add("gosnumber", regNumber)
                    .build();

            String response = "";

            try {
                response = getEaistoRequest.sendNewHttpsPost(URL_GET_EAISTO, formBody);
            } catch (Exception e) {
                e.printStackTrace();
            }

            logD(TAG, "Response: " + response);

            return response;
        }

        @Override
        protected void onPostExecute(String response) {

            if (PlateActivity.this != null && PlateActivity.this.isFinishing()) {
                return;
            }

            dismissProgressDialog();

            //[{"cardNumber":"201503270831087345397","model":"","vin":"XTA111730B0123490","regNumber":"\u041a957\u0415\u041c73","dateFrom":"27.03.2015","dateTo":"27.03.2017","operator":""},
            //{"cardNumber":"201406281019061660647","model":"","vin":"XTA111730B0123490","regNumber":"\u041a957\u0415\u041c73","dateFrom":"28.06.2014","dateTo":"28.06.2016","operator":""}]

            JSONObject responseJsonObject = null;
            JSONObject responseJson = null;
            String error_color = "#CD5C5C";
            String no_error_color = "#9EF39B";

            TextView textHeader = (TextView) PlateActivity.this.findViewById(R.id.details_header);
            TextView textData = (TextView) PlateActivity.this.findViewById(R.id.details_data);
            LinearLayout layout_result = (LinearLayout) PlateActivity.this.findViewById(R.id.layout_result);
            CardView card_results = (CardView) PlateActivity.this.findViewById(R.id.cardResults);
            Button copyVinButton = (Button) PlateActivity.this.findViewById(R.id.copy_vin);


            try {
                responseJsonObject = new JSONObject(response);
                responseJson = responseJsonObject.optJSONObject("data");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                final String VIN = responseJson.optString("vin");

                if (!VIN.isEmpty()) {
                    card_results = (CardView) PlateActivity.this.findViewById(R.id.cardResults);
                    card_results.setVisibility(View.VISIBLE);
                    textHeader.setText(PlateActivity.this.getResources().getString(R.string.vin_found));
                    textData.setText(PlateActivity.this.getResources().getString(R.string.vin_found_text, VIN));
                    layout_result.setBackgroundColor(Color.parseColor(no_error_color));

                    copyVinButton.setVisibility(View.VISIBLE);
                    copyVinButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // button click event
                            ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText(VIN, VIN);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(App.getContext(), "Скопировано", Toast.LENGTH_LONG).show();
                        }
                    });

                    CardView card_next_check = (CardView) PlateActivity.this.findViewById(R.id.cardNextGibdd);
                    card_next_check.setVisibility(View.VISIBLE);

                    CardView cardNextReport = (CardView) PlateActivity.this.findViewById(R.id.cardNextReport);
                    cardNextReport.setVisibility(View.VISIBLE);

                    View.OnClickListener cardNextReportListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent detailIntent = new Intent(PlateActivity.this, FullReportActivity.class);
                            detailIntent.putExtra(FullReportActivity.ARG_VIN, VIN);
                            startActivity(detailIntent);
                        }
                    };
                    cardNextReport.setOnClickListener(cardNextReportListener);

                    String nextReportColor = "#9EF39B";
                    cardNextReport.setCardBackgroundColor(Color.parseColor(nextReportColor));

                    String next_card_color = "#e6e6e6";
                    card_next_check.setCardBackgroundColor(Color.parseColor(next_card_color));

                    View.OnClickListener card_next_check_listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent detailIntent = new Intent(PlateActivity.this, ArticleDetailActivity.class);
                            detailIntent.putExtra(ArticleDetailFragment.ARG_ITEM_ID, "1");
                            detailIntent.putExtra(ArticleDetailFragment.ARG_VIN, VIN);
                            startActivity(detailIntent);
                        }
                    };

                    card_next_check.setOnClickListener(card_next_check_listener);

                    cardVinIncorrect.setVisibility(View.VISIBLE);

                    ArrayList<String> passing = new ArrayList<String>();
                    passing.add(PlateNumber);

                    getPlateData = new GetPlateData();
                    try {
                        getPlateData.execute(passing);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else {

                    dismissProgressDialog();

                    Toast.makeText(PlateActivity.this, getString(R.string.eaisto_no_data), Toast.LENGTH_LONG).show();

                    reloadWebView1();
                    captchaWebView.setVisibility(View.VISIBLE);
                }

            } catch (Exception e) {

                dismissProgressDialog();

                Toast.makeText(PlateActivity.this, getString(R.string.eaisto_no_data), Toast.LENGTH_LONG).show();

                reloadWebView1();
                captchaWebView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void parsePolisResult(String result) {
        try {
            JSONArray records = new JSONArray(result);

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.optJSONObject(i);

                String tempSerial = record.optString("policyBsoSerial");
                String tempNumber = record.optString("policyBsoNumber");

                if (!tempSerial.equals("") && !tempNumber.equals("")) {
                    polisNumber = tempNumber;
                    polisSerial = tempSerial;
                    break;
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parsePlateResult(String result) {
        try {
            JSONArray records = new JSONArray(result);
            plateList = new ArrayList<>();

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.optJSONObject(i);

                PlateItem item = new PlateItem();

                item.setMake(record.getString("make"));
                item.setModel(record.getString("model"));
                item.setDate(record.getString("date"));

                JSONObject recordPhoto = record.getJSONObject("photo");
                item.setUrl(recordPhoto.getString("link"));
                item.setImage(recordPhoto.getString("original"));

                plateList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void scroolToResult() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int y = metrics.heightPixels / 2;
        int x = 0;

        ObjectAnimator xTranslate = ObjectAnimator.ofInt(mScrollView, "scrollX", x);
        ObjectAnimator yTranslate = ObjectAnimator.ofInt(mScrollView, "scrollY", y);

        AnimatorSet animators = new AnimatorSet();
        animators.setDuration(1000L);
        animators.playTogether(xTranslate, yTranslate);
        animators.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
            }
        });
        animators.start();
    }

    private class GetPolisData extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(ArrayList<String>... passing) {

            ArrayList<String> passed = passing[0]; //get passed arraylist
            Object[] mStringArray = passed.toArray();

            /*
            http://dkbm-web.autoins.ru/dkbm-web-1.0/policy.htm
            vin:XTA111730B0123490
            lp:
            date:03.04.2017
            bodyNumber:
            chassisNumber:
            answer:rap7b
             */

            String lp = (String) mStringArray[0];
            String date = (String) mStringArray[1];
            String answer = (String) mStringArray[2];

            RequestBody formBody = new FormBody.Builder()
                    .add("vin", "")
                    .add("bodyNumber", "")
                    .add("chassisNumber", "")
                    .add("lp", lp)
                    .add("date", date)
                    .add("captcha", answer)
                    .build();

            String response = "";
            try {
                response = getPolisRequest.sendPost(URL_GET_POLIS, formBody);
            } catch (Exception e) {
                e.printStackTrace();
            }

            logD(TAG, "Response: " + response);

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if (PlateActivity.this != null && PlateActivity.this.isFinishing()) {
                return;
            }

            captchaWebView.setVisibility(View.GONE);

            //{"bodyNumber":"","chassisNumber":"","licensePlate":"","vin":"WF0DXXGBBD8D01928","policyUnqId":null,"policyResponseUIItems":[{"policyIsRestrict":"1","policyUnqId":"251896973","insCompanyName":"РОСГОССТРАХ","policyBsoNumber":"0377148304","policyBsoSerial":"ЕЕЕ"},{"policyIsRestrict":"1","policyUnqId":"264923711","insCompanyName":"РЕСО-Гарантия","policyBsoNumber":"0000795974","policyBsoSerial":"ХХХ"}],"validCaptcha":true,"errorMessage":null,"errorId":0,"warningMessage":null}

            JSONObject responseJsonObject = null;
            Boolean validCaptcha = false;
            String warningMessage = "";
            String error_color = "#CD5C5C";
            TextView textData = (TextView) PlateActivity.this.findViewById(R.id.details_data);
            TextView textHeader = (TextView) PlateActivity.this.findViewById(R.id.details_header);
            LinearLayout layout_result = (LinearLayout) PlateActivity.this.findViewById(R.id.layout_result);

            try {
                responseJsonObject = new JSONObject(response);
                validCaptcha = responseJsonObject.getBoolean("validCaptcha");
                warningMessage = responseJsonObject.getString("warningMessage");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (response.equals("")) {
                CardView card_results = (CardView) PlateActivity.this.findViewById(R.id.cardResults);
                layout_result.setBackgroundColor(Color.parseColor(error_color));
                textData.setText(R.string.vin_not_found);
                textHeader.setText(R.string.error);
                card_results.setVisibility(View.VISIBLE);

                Toast.makeText(PlateActivity.this, getString(R.string.no_insurance_rsa_search), Toast.LENGTH_LONG).show();
            }

            if (validCaptcha) {
                try {
                    JSONArray responseJsonArray = responseJsonObject.getJSONArray("policyResponseUIItems");
                    parsePolisResult(responseJsonArray.toString());
                    if (!polisSerial.isEmpty() && !polisNumber.isEmpty()) {
                        reloadWebView2();
                    } else {
                        CardView card_results = (CardView) PlateActivity.this.findViewById(R.id.cardResults);
                        textData.setText(R.string.vin_not_found);
                        textHeader.setText(R.string.error);
                        layout_result.setBackgroundColor(Color.parseColor(error_color));
                        card_results.setVisibility(View.VISIBLE);
                        Toast.makeText(PlateActivity.this, getString(R.string.no_insurance_rsa_search), Toast.LENGTH_LONG).show();

                        ArrayList<String> passing = new ArrayList<String>();
                        passing.add(PlateNumber);

                        getPlateData = new GetPlateData();
                        try {
                            getPlateData.execute(passing);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    CardView card_results = (CardView) PlateActivity.this.findViewById(R.id.cardResults);
                    textData.setText(R.string.vin_not_found);
                    textHeader.setText(R.string.error);
                    layout_result.setBackgroundColor(Color.parseColor(error_color));
                    card_results.setVisibility(View.VISIBLE);
                    Toast.makeText(PlateActivity.this, getString(R.string.no_insurance_rsa_search), Toast.LENGTH_LONG).show();
                    ArrayList<String> passing = new ArrayList<String>();
                    passing.add(PlateNumber);

                    getPlateData = new GetPlateData();
                    try {
                        getPlateData.execute(passing);
                    } catch (Exception ex) {
                        e.printStackTrace();
                    }
                }
            } else {
                CardView card_results = (CardView) PlateActivity.this.findViewById(R.id.cardResults);
                textData.setText(R.string.vin_not_found);
                textHeader.setText(R.string.error);
                layout_result.setBackgroundColor(Color.parseColor(error_color));
                card_results.setVisibility(View.VISIBLE);
                Toast.makeText(PlateActivity.this, getString(R.string.error_rsa_search), Toast.LENGTH_LONG).show();

                ArrayList<String> passing = new ArrayList<String>();
                passing.add(PlateNumber);

                getPlateData = new GetPlateData();
                try {
                    getPlateData.execute(passing);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class GetInsuranceData extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(ArrayList<String>... passing) {

            ArrayList<String> passed = passing[0]; //get passed arraylist
            Object[] mStringArray = passed.toArray();

            /*
            http://dkbm-web.autoins.ru/dkbm-web-1.0/osagovehicle.htm
            xxx 0000795974

            serialOsago:ХХХ
            numberOsago:0000795974
            dateRequest:27.02.2017
            answer:8dkyf
             */

            logD(TAG, "get insurance data");

            String serialOsago = (String) mStringArray[0];
            String numberOsago = (String) mStringArray[1];
            String dateRequest = (String) mStringArray[2];
            String answer = (String) mStringArray[3];

            logD(TAG, "serialOsago: " + serialOsago);
            logD(TAG, "numberOsago: " + numberOsago);
            logD(TAG, "dateRequest: " + dateRequest);
            logD(TAG, "answer: " + answer);

            RequestBody formBody = new FormBody.Builder()
                    .add("serialOsago", serialOsago)
                    .add("numberOsago", numberOsago)
                    .add("dateRequest", dateRequest)
                    .add("captcha", answer)
                    .build();

            String response = "";
            try {
                response = getInsuranceRequest.sendPost(URL_GET_INSURANCE, formBody);
            } catch (Exception e) {
                e.printStackTrace();
            }

            logD(TAG, "Response: " + response);

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if (PlateActivity.this != null && PlateActivity.this.isFinishing()) {
                return;
            }

            captchaWebView.setVisibility(View.GONE);

            //correct
            //{"insurerName":"РЕСО-Гарантия","bodyNumber":null,"chassisNumber":null,"licensePlate":"Е953СА73","policyStatus":"Действует","vin":"WF0DXXGBBD8D01928","errorMessage":null,"warningMessage":null,"errorId":0,"validCaptcha":true}
            //invalid captcha
            //{"insurerName":null,"bodyNumber":null,"chassisNumber":null,"licensePlate":null,"policyStatus":null,"vin":null,"errorMessage":null,"warningMessage":null,"errorId":0,"validCaptcha":false}
            //incorrect number
            //{"insurerName":null,"bodyNumber":null,"chassisNumber":null,"licensePlate":null,"policyStatus":null,"vin":null,"errorMessage":null,"warningMessage":"Сведения о полисе ОСАГО с указанными серией и номером не найдены","errorId":0,"validCaptcha":true}

            JSONObject responseJsonObject = null;
            Boolean validCaptcha = false;
            String warningMessage = "";
            String error_color = "#CD5C5C";
            String no_error_color = "#9EF39B";

            TextView textHeader = (TextView) PlateActivity.this.findViewById(R.id.details_header);
            TextView textData = (TextView) PlateActivity.this.findViewById(R.id.details_data);
            LinearLayout layout_result = (LinearLayout) PlateActivity.this.findViewById(R.id.layout_result);
            CardView card_results = (CardView) PlateActivity.this.findViewById(R.id.cardResults);

            try {
                responseJsonObject = new JSONObject(response);
                validCaptcha = responseJsonObject.getBoolean("validCaptcha");
                warningMessage = responseJsonObject.getString("warningMessage");
            } catch (JSONException e) {
                e.printStackTrace();
                card_results.setVisibility(View.VISIBLE);
            }

            if (response.equals("")) {
                card_results.setVisibility(View.VISIBLE);
            }

            Button copyVinButton = (Button) PlateActivity.this.findViewById(R.id.copy_vin);

            if (validCaptcha && warningMessage.equals("null")) {
                //insurance details
                try {
                    String vin = responseJsonObject.getString("vin");
                    vinResult = vin;

                    ArrayList<String> passing = new ArrayList<String>();
                    passing.add(vinResult);

                    putVin = new PutVin();
                    try {
                        putVin.execute(passing);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    card_results = (CardView) PlateActivity.this.findViewById(R.id.cardResults);
                    card_results.setVisibility(View.VISIBLE);
                    textHeader.setText(PlateActivity.this.getResources().getString(R.string.vin_found));
                    textData.setText(PlateActivity.this.getResources().getString(R.string.vin_found_text, vinResult));
                    layout_result.setBackgroundColor(Color.parseColor(no_error_color));

                    copyVinButton.setVisibility(View.VISIBLE);
                    copyVinButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // button click event
                            ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText(vinResult, vinResult);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(App.getContext(), "Скопировано", Toast.LENGTH_LONG).show();
                        }
                    });

                    CardView card_next_check = (CardView) PlateActivity.this.findViewById(R.id.cardNextGibdd);
                    card_next_check.setVisibility(View.VISIBLE);

                    CardView cardNextReport = (CardView) PlateActivity.this.findViewById(R.id.cardNextReport);
                    cardNextReport.setVisibility(View.VISIBLE);

                    String nextReportColor = "#9EF39B";
                    cardNextReport.setCardBackgroundColor(Color.parseColor(nextReportColor));

                    View.OnClickListener cardNextReportListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent detailIntent = new Intent(PlateActivity.this, FullReportActivity.class);
                            detailIntent.putExtra(FullReportActivity.ARG_VIN, vinResult);
                            startActivity(detailIntent);
                        }
                    };
                    cardNextReport.setOnClickListener(cardNextReportListener);

                    String next_card_color = "#e6e6e6";
                    card_next_check.setCardBackgroundColor(Color.parseColor(next_card_color));

                    View.OnClickListener card_next_check_listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent detailIntent = new Intent(PlateActivity.this, ArticleDetailActivity.class);
                            detailIntent.putExtra(ArticleDetailFragment.ARG_ITEM_ID, "1");
                            detailIntent.putExtra(ArticleDetailFragment.ARG_VIN, vinResult);
                            startActivity(detailIntent);
                        }
                    };

                    card_next_check.setOnClickListener(card_next_check_listener);

                } catch (JSONException e) {
                    card_results.setVisibility(View.VISIBLE);
                }
            } else {
                card_results.setVisibility(View.VISIBLE);
            }

            ArrayList<String> passing = new ArrayList<String>();
            passing.add(PlateNumber);

            getPlateData = new GetPlateData();
            try {
                getPlateData.execute(passing);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Hide keyboard
            InputMethodManager imm = (InputMethodManager) PlateActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(layout_result.getWindowToken(), 0);

            scroolToResult();

        }
    }

    private class PutVin extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(ArrayList<String>... passing) {

            ArrayList<String> passed = passing[0]; //get passed arraylist
            Object[] mStringArray = passed.toArray();

            /*
                vin
             */

            String vin = SanitizeHelper.antiTransliterate((String) mStringArray[0]);

            Long tsLong = System.currentTimeMillis() / 1000;
            String timestamp = tsLong.toString();

            RequestBody formBody = new FormBody.Builder()
                    .add("vin", vin)
                    .add("gosnumber", PlateNumber)
                    .add("timestamp", timestamp)
                    .build();

            String response = "";

            try {
                response = getEaistoRequest.sendNewHttpsPost(URL_PUT_VIN, formBody);
            } catch (Exception e) {
                e.printStackTrace();
            }

            logD(TAG, "Response: " + response);

            return response;
        }

        @Override
        protected void onPostExecute(String response) {}
    }

    public class AutoinsWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Toast.makeText(PlateActivity.this, getString(R.string.start_rsa_search), Toast.LENGTH_LONG).show();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        public void onReceivedError(android.webkit.WebView view, WebResourceRequest request, WebResourceError error) {
            view.stopLoading();
            captchaWebView.setVisibility(View.GONE);
            final LinearLayout samplesMain = (LinearLayout) findViewById(R.id.samples_main);
            mSnackbar = Snackbar.make(samplesMain, PlateActivity.this.getResources().getString(R.string.insurance_error), Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null)
                    .setDuration(2000);
            mSnackbar.show();
            progressView.setVisibility(View.GONE);
        }

        public void onReceivedSslError(android.webkit.WebView view, SslErrorHandler handler, SslError er) {
            SslErrorHandler sslErrorHandler = handler;
            if (er.getUrl().equals(URL_GET_POLIS)) {
                sslErrorHandler.proceed();
            } else {
                sslErrorHandler.cancel();
            }
            progressView.setVisibility(View.GONE);
        }

        public void onPageFinished(android.webkit.WebView view, String url) {
            super.onPageFinished(view, url);
            if (url.equals(URL_GET_POLIS)) {
                view.loadUrl("javascript:$('header').hide()");
                view.loadUrl("javascript:$('p,footer').hide()");
                view.loadUrl("javascript:$('.h3,.form-block').hide()");
                view.loadUrl("javascript:$('#buttonSearch').hide()");
                view.loadUrl("javascript:$('.blue-btn').hide()");
                view.loadUrl("javascript:var int; int = setInterval(function(){if($('#g-recaptcha-response').val().length > 0){ window.AutoinsInterface.make1($('#g-recaptcha-response').val()); clearInterval(int); } },1000);");
            }
            progressView.setVisibility(View.GONE);
        }
    }

    public class JavaScriptInterface {
        private WebView webView;

        public JavaScriptInterface(WebView webView) {
            this.webView = webView;
        }

        @JavascriptInterface
        public void make1(String key) throws Exception {
            if (key != null && key.length() > 0) {

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                String formattedDate = df.format(c.getTime());

                ArrayList<String> passing1 = new ArrayList<String>();
                passing1.add(PlateNumber);
                passing1.add(formattedDate);
                passing1.add(key);


                getPolisData = new GetPolisData();
                try {
                    getPolisData.execute(passing1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(PlateActivity.this, getString(R.string.error_rsa_search), Toast.LENGTH_LONG).show();
                captchaWebView.setVisibility(View.GONE);
                progressView.setVisibility(View.GONE);
            }
        }
    }

    public class AutoinsWebViewClient2 extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Toast.makeText(PlateActivity.this, getString(R.string.start_rsa_search_2), Toast.LENGTH_LONG).show();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        public void onReceivedError(android.webkit.WebView view, WebResourceRequest request, WebResourceError error) {
            view.stopLoading();
            captchaWebView.setVisibility(View.GONE);
            final LinearLayout samplesMain = (LinearLayout) findViewById(R.id.samples_main);
            mSnackbar = Snackbar.make(samplesMain, PlateActivity.this.getResources().getString(R.string.insurance_error), Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null)
                    .setDuration(2000);
            mSnackbar.show();
            progressView.setVisibility(View.GONE);
        }

        public void onReceivedSslError(android.webkit.WebView view, SslErrorHandler handler, SslError er) {
            SslErrorHandler sslErrorHandler = handler;
            if (er.getUrl().equals(URL_GET_INSURANCE)) {
                sslErrorHandler.proceed();
            } else {
                sslErrorHandler.cancel();
            }
            progressView.setVisibility(View.GONE);
        }

        public void onPageFinished(android.webkit.WebView view, String url) {
            super.onPageFinished(view, url);
            captchaWebView.setVisibility(View.VISIBLE);
            if (url.equals(URL_GET_INSURANCE)) {
                view.loadUrl("javascript:$('header').hide()");
                view.loadUrl("javascript:$('p,footer').hide()");
                view.loadUrl("javascript:$('.h3,.form-block').hide()");
                view.loadUrl("javascript:$('#buttonSearch').hide()");
                view.loadUrl("javascript:$('.blue-btn').hide()");
                view.loadUrl("javascript:var int; int = setInterval(function(){if($('#g-recaptcha-response').val().length > 0){ window.AutoinsInterface2.make($('#g-recaptcha-response').val()); clearInterval(int); } },1000);");
            }
            progressView.setVisibility(View.GONE);
        }
    }

    public class JavaScriptInterface2 {
        private WebView webView;

        public JavaScriptInterface2(WebView webView) {
            this.webView = webView;
        }

        @JavascriptInterface
        public void make(String key) throws Exception {
            if (key != null && key.length() > 0) {

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                String formattedDate = df.format(c.getTime());

                ArrayList<String> passing1 = new ArrayList<String>();
                passing1.add(polisSerial);
                passing1.add(polisNumber);
                passing1.add(formattedDate);
                passing1.add(key);


                getInsuranceData = new GetInsuranceData();
                try {
                    getInsuranceData.execute(passing1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                captchaWebView.setVisibility(View.GONE);
                progressView.setVisibility(View.GONE);
            }
        }
    }
}