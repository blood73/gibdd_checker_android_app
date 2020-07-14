package ru.bloodsoft.gibddchecker.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.database.HistoryDatabaseHelper;
import ru.bloodsoft.gibddchecker.models.Eaisto;
import ru.bloodsoft.gibddchecker.models.EaistoItem;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;
import ru.bloodsoft.gibddchecker.ui.recycler_views.EaistoItemRecyclerViewAdapter;
import ru.bloodsoft.gibddchecker.util.NewWebService;
import ru.bloodsoft.gibddchecker.util.RunCounts;
import ru.bloodsoft.gibddchecker.util.SanitizeHelper;
import ru.bloodsoft.gibddchecker.util.SettingsStorage;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class EaistoActivity extends BaseActivity {

    String EaistoVin;
    String EaistoRegNumber;

    static {
        System.loadLibrary("native-lib");
    }

    public static native String getEaistoUrl();

    public static final String ARG_VIN = "vin";
    public static final String ARG_REG_NUMBER = "reg_number";

    public static final String URL_GET_EAISTO = SanitizeHelper.decryptString(getEaistoUrl());
    ProgressDialog mProgressDialog;
    NewWebService getEaistoRequest;
    GetEaistoData getEaistoData;
    Activity activity;

    private List<EaistoItem> eaistoList;
    private RecyclerView mRecyclerView;
    private EaistoItemRecyclerViewAdapter eaistoAdapter;
    InterstitialAd mInterstitialAd;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Snackbar mSnackbar;
    NestedScrollView mScrollView;

    private static final String TAG = makeLogTag(EaistoActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eaisto);

        activity = this;
        final EditText eaistoVinEditText = (EditText) this.findViewById(R.id.eaisto_vin);
        EaistoVin = eaistoVinEditText.getText().toString();

        final EditText eaistoRegNumberEditText = (EditText) this.findViewById(R.id.eaisto_reg_number);
        EaistoRegNumber = eaistoRegNumberEditText.getText().toString();

        mScrollView = (NestedScrollView) findViewById(R.id.scrollView);

        Bundle b = activity.getIntent().getExtras();
        if (b != null) {
            String vin = b.getString(ARG_VIN);
            if (vin != null && !vin.isEmpty()) {
                eaistoVinEditText.setText(vin);
            }

            String regNumber = b.getString(ARG_REG_NUMBER);
            if (regNumber != null && !regNumber.isEmpty()) {
                eaistoRegNumberEditText.setText(regNumber);
            }
        }

        mRecyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view_eaisto);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        eaistoAdapter = new EaistoItemRecyclerViewAdapter(activity, eaistoList);
        mRecyclerView.setAdapter(eaistoAdapter);

        RunCounts settings = new RunCounts();
        Boolean isAdFree = settings.isAdFree();

        if (!isAdFree) {
            mInterstitialAd = new InterstitialAd(activity);
            mInterstitialAd.setAdUnitId("ca-app-pub-3078563819949367/4695892338");

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    requestNewInterstitial();
                }
            });

            requestNewInterstitial();

            AdView adView = new AdView(this);

            DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
            float dpWidth = displayMetrics.widthPixels / displayMetrics.density - 40;
            adView.setAdSize(new AdSize((int) dpWidth, 300));
            adView.setAdUnitId("ca-app-pub-3078563819949367/5966252215");

            CardView card_adview = (CardView) activity.findViewById(R.id.cardAdView);
            card_adview.addView(adView);

            AdRequest request = new AdRequest.Builder()
                    .addTestDevice("E31198FB67C1181AF2CFCEDF4476A9D1")
                    .build();
            adView.loadAd(request);

            AdView adView2 = new AdView(this);

            adView2.setAdSize(new AdSize((int) dpWidth, 80));
            adView2.setAdUnitId("ca-app-pub-3078563819949367/1780251731");

            CardView card_adview_small = (CardView) activity.findViewById(R.id.cardAdViewSmall);
            card_adview_small.addView(adView2);

            AdRequest request2 = new AdRequest.Builder()
                    .addTestDevice("E31198FB67C1181AF2CFCEDF4476A9D1")
                    .build();
            adView2.loadAd(request2);
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);

        ButterKnife.bind(this);
        setupToolbar();
    }

    @OnClick(R.id.paste1)
    public void onPaste1Clicked(View view) {
        final EditText plateEditText = (EditText) findViewById(R.id.eaisto_vin);

        try {
            ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = clipboard.getPrimaryClip();
            String plate = "";

            if (clipData != null) {
                ClipData.Item item = clipData.getItemAt(0);
                plate = item.getText().toString();
            }

            if (!plate.isEmpty()) {
                plate = SanitizeHelper.sanitizeString(plate);
                plateEditText.setText(plate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.paste4)
    public void onPaste4Clicked(View view) {
        final EditText plateEditText = (EditText) findViewById(R.id.eaisto_reg_number);
        try {
            ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = clipboard.getPrimaryClip();
            String plate = "";

            if (clipData != null) {
                ClipData.Item item = clipData.getItemAt(0);
                plate = item.getText().toString();
            }

            if (!plate.isEmpty()) {
                plate = SanitizeHelper.sanitizeString(plate);
                plateEditText.setText(plate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showProgressDialog() {
        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setMessage(activity.getResources().getString(R.string.loading));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);

        mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        dismissProgressDialog();
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

        final EditText eaistoVinEditText = (EditText) this.findViewById(R.id.eaisto_vin);
        EaistoVin = eaistoVinEditText.getText().toString();

        final EditText eaistoRegNumberEditText = (EditText) this.findViewById(R.id.eaisto_reg_number);
        EaistoRegNumber = eaistoRegNumberEditText.getText().toString();
        EaistoRegNumber = SanitizeHelper.sanitizeString(EaistoRegNumber);

        if (isConnectedToInternet()) {
            if (!EaistoVin.trim().isEmpty() || !EaistoRegNumber.trim().isEmpty()) {

                ArrayList<String> passing = new ArrayList<String>();
                passing.add(EaistoVin);
                passing.add(EaistoRegNumber);

                //save to the history
                Eaisto newEaisto = new Eaisto();
                newEaisto.vin = EaistoVin;
                newEaisto.bodyNumber = "";
                newEaisto.frameNumber = "";
                newEaisto.regNumber = EaistoRegNumber;

                // Get singleton instance of database
                HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(activity);
                // Add vin to the database
                databaseHelper.addEaisto(newEaisto);

                RunCounts requestCounts = new RunCounts();
                requestCounts.increaseCheckAutoCount(activity);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "eaisto");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, EaistoVin + ";" + EaistoRegNumber);
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "EAISTO");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                getEaistoData = new GetEaistoData();
                try {
                    getEaistoData.execute(passing);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mSnackbar = Snackbar.make(view, activity.getResources().getString(R.string.error_empty_request), Snackbar.LENGTH_INDEFINITE)
                        .setAction("Action", null)
                        .setDuration(2000);
                mSnackbar.show();
                //Hide keyboard
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } else {
            mSnackbar = Snackbar.make(view, activity.getResources().getString(R.string.error_no_internet), Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null)
                    .setDuration(2000);
            mSnackbar.show();
            //Hide keyboard
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
        return R.id.nav_eaisto;
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

    private class GetEaistoData extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
            CardView card_results = (CardView) activity.findViewById(R.id.cardResults);
            card_results.setVisibility(View.GONE);

            CardView card_next_check = (CardView) activity.findViewById(R.id.cardNextPolis);
            card_next_check.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(ArrayList<String>... passing) {

            ArrayList<String> passed = passing[0]; //get passed arraylist
            Object[] mStringArray = passed.toArray();

            /*
                vin
                BodyNumber
                FrameNumber
                regNumber
             */

            String vin = (String) mStringArray[0];
            String regNumber = (String) mStringArray[1];

            logD(TAG, "vin: " + vin);
            logD(TAG, "regNumber: " + regNumber);

            RequestBody formBody = new FormBody.Builder()
                    .add("vin", vin)
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

            if (activity != null && activity.isFinishing()) {
                return;
            }
            boolean isError = false;

            dismissProgressDialog();

            //[{"cardNumber":"201503270831087345397","model":"","vin":"XTA111730B0123490","regNumber":"\u041a957\u0415\u041c73","dateFrom":"27.03.2015","dateTo":"27.03.2017","operator":""},
            //{"cardNumber":"201406281019061660647","model":"","vin":"XTA111730B0123490","regNumber":"\u041a957\u0415\u041c73","dateFrom":"28.06.2014","dateTo":"28.06.2016","operator":""}]

            JSONObject responseJsonObject = null;
            JSONArray responseJsonArray = null;
            String error_color = "#CD5C5C";
            String no_error_color = "#9EF39B";

            TextView textHeader = (TextView) activity.findViewById(R.id.details_header);
            TextView textData = (TextView) activity.findViewById(R.id.details_data);
            LinearLayout layout_result = (LinearLayout) activity.findViewById(R.id.layout_result);
            CardView card_results = (CardView) activity.findViewById(R.id.cardResults);

            mRecyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view_eaisto);
            mRecyclerView.setVisibility(View.GONE);
            mRecyclerView.setNestedScrollingEnabled(false);


            try {
                responseJsonObject = new JSONObject(response);
                responseJsonArray = responseJsonObject.optJSONArray("data");
            } catch (JSONException e) {
                e.printStackTrace();
                card_results = (CardView) activity.findViewById(R.id.cardResults);
                card_results.setVisibility(View.VISIBLE);
                textHeader.setText(activity.getResources().getString(R.string.error));
                textData.setText(activity.getResources().getString(R.string.eaisto_error_details));
                layout_result.setBackgroundColor(Color.parseColor(error_color));
            }

            try {
                parseEaistoResult(responseJsonArray.toString());

                Integer numberEaisto = responseJsonArray.length();

                if (numberEaisto == 0) {
                    card_results = (CardView) activity.findViewById(R.id.cardResults);
                    card_results.setVisibility(View.VISIBLE);
                    textHeader.setText(activity.getResources().getString(R.string.warning));
                    textData.setText(activity.getResources().getString(R.string.eaisto_warning_not_found));
                    layout_result.setBackgroundColor(Color.parseColor(error_color));
                }

                eaistoAdapter = new EaistoItemRecyclerViewAdapter(activity, eaistoList);
                mRecyclerView.setVisibility(View.VISIBLE);
                mRecyclerView.setAdapter(eaistoAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

            } catch (Exception e) {
                card_results = (CardView) activity.findViewById(R.id.cardResults);
                card_results.setVisibility(View.VISIBLE);
                textHeader.setText(activity.getResources().getString(R.string.warning));
                textData.setText(activity.getResources().getString(R.string.eaisto_warning_not_found));
                layout_result.setBackgroundColor(Color.parseColor(error_color));
            }

            //Hide keyboard
            final NestedScrollView scrollView = (NestedScrollView) activity.findViewById(R.id.scrollView);
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(scrollView.getWindowToken(), 0);

            RunCounts settings = new RunCounts();
            Boolean isAdFree = settings.isAdFree();

            if (!isAdFree) {
                SettingsStorage settingsStorage = new SettingsStorage();
                Boolean showAd = settingsStorage.isShowInterstitial(activity);

                if (showAd && mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                    requestNewInterstitial();
                }

                CardView card_adview = (CardView) activity.findViewById(R.id.cardAdView);
                card_adview.setVisibility(View.VISIBLE);
                CardView card_adview_small = (CardView) activity.findViewById(R.id.cardAdViewSmall);
                card_adview_small.setVisibility(View.GONE);
            } else {
                CardView card_adview = (CardView) activity.findViewById(R.id.cardAdView);
                card_adview.setVisibility(View.GONE);
            }

            if (!EaistoVin.isEmpty()) {
                CardView card_next_check = (CardView) activity.findViewById(R.id.cardNextPolis);
                card_next_check.setVisibility(View.VISIBLE);
                String next_card_color = "#e6e6e6";
                card_next_check.setCardBackgroundColor(Color.parseColor(next_card_color));

                View.OnClickListener card_next_check_listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent detailIntent = new Intent(activity, PolisActivity.class);
                        detailIntent.putExtra(PolisActivity.ARG_VIN, EaistoVin);
                        startActivity(detailIntent);
                    }
                };

                card_next_check.setOnClickListener(card_next_check_listener);

            }

            scroolToResult();

        }
    }

    private void parseEaistoResult(String result) {
        try {
            JSONArray records = new JSONArray(result);
            eaistoList = new ArrayList<>();

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.optJSONObject(i);

                EaistoItem item = new EaistoItem();

                item.setCardNumber(record.optString("doc_num"));
                item.setModel(record.optString("mark_model"));
                item.setVin(record.optString("vin"));
                item.setRegNumber(record.optString("gosnumber"));
                item.setDateFrom(record.optString("start_date"));
                item.setDateTo(record.optString("end_date"));

                eaistoList.add(item);
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
}