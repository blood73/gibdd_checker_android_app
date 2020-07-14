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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import ru.bloodsoft.gibddchecker.models.Polis;
import ru.bloodsoft.gibddchecker.models.PolisItem;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;
import ru.bloodsoft.gibddchecker.ui.recycler_views.PolisItemRecyclerViewAdapter;
import ru.bloodsoft.gibddchecker.util.NewWebService;
import ru.bloodsoft.gibddchecker.util.RunCounts;
import ru.bloodsoft.gibddchecker.util.SanitizeHelper;
import ru.bloodsoft.gibddchecker.util.SettingsStorage;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class PolisActivity extends BaseActivity {

    String EaistoVin;
    String EaistoBodyNumber;
    String EaistoFrameNumber;
    String EaistoRegNumber;

    public static final String ARG_VIN = "vin";
    public static final String ARG_BODY_NUMBER = "body_number";
    public static final String ARG_FRAME_NUMBER = "frame_number";
    public static final String ARG_REG_NUMBER = "reg_number";

    public static final String URL_GET_POLIS ="https://dkbm-web.autoins.ru/dkbm-web-1.0/policy.htm";
    ProgressDialog mProgressDialog;
    NewWebService getPolisRequest;
    GetPolisData getPolisData;
    DownloadReCaptcha downloadReCaptcha;
    Activity activity;

    private List<PolisItem> polisList;
    private RecyclerView mRecyclerView;
    private PolisItemRecyclerViewAdapter polisAdapter;
    InterstitialAd mInterstitialAd;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Snackbar mSnackbar;
    NestedScrollView mScrollView;

    @BindView(R.id.captcha_webview)
    WebView captchaWebView;

    @BindView(R.id.progress_layout)
    View progressView;

    private static final String TAG = makeLogTag(PolisActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polis);

        activity = this;
        final EditText eaistoVinEditText = (EditText) this.findViewById(R.id.polis_vin);

        final EditText eaistoBodyNumberEditText = (EditText) this.findViewById(R.id.polis_body_number);

        final EditText eaistoFrameNumberEditText = (EditText) this.findViewById(R.id.polis_frame_number);

        final EditText eaistoRegNumberEditText = (EditText) this.findViewById(R.id.polis_reg_number);

        mScrollView = (NestedScrollView) findViewById(R.id.scrollView);

        Bundle b = activity.getIntent().getExtras();
        if (b != null) {
            String vin = b.getString(ARG_VIN);
            if (vin != null && !vin.isEmpty()) {
                eaistoVinEditText.setText(vin);
            }

            String bodyNumber = b.getString(ARG_BODY_NUMBER);
            if (bodyNumber != null && !bodyNumber.isEmpty()) {
                eaistoBodyNumberEditText.setText(bodyNumber);
            }

            String frameNumber = b.getString(ARG_FRAME_NUMBER);
            if (frameNumber != null && !frameNumber.isEmpty()) {
                eaistoFrameNumberEditText.setText(frameNumber);
            }

            String regNumber = b.getString(ARG_REG_NUMBER);
            if (regNumber != null && !regNumber.isEmpty()) {
                eaistoRegNumberEditText.setText(regNumber);
            }
        }

        mRecyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view_polis);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        polisAdapter = new PolisItemRecyclerViewAdapter(activity, polisList);
        mRecyclerView.setAdapter(polisAdapter);

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

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("E31198FB67C1181AF2CFCEDF4476A9D1")
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    private void showProgressDialog() {
        // instantiate it within the onCreate method
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(activity);
        }
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

    private void reloadWebView() {
        progressView.setVisibility(View.VISIBLE);
        captchaWebView.loadUrl(URL_GET_POLIS);
        captchaWebView.getSettings().setJavaScriptEnabled(true);
        captchaWebView.getSettings().setDomStorageEnabled(true);
        captchaWebView.addJavascriptInterface(new JavaScriptInterface(captchaWebView), "AutoinsInterface");
        captchaWebView.setWebViewClient(new AutoinsWebViewClient());
    }

    @OnClick(R.id.fab)
    public void onFabClicked(View view) {

        final EditText eaistoVinEditText = (EditText) this.findViewById(R.id.polis_vin);
        EaistoVin = eaistoVinEditText.getText().toString();

        final EditText eaistoBodyNumberEditText = (EditText) this.findViewById(R.id.polis_body_number);
        EaistoBodyNumber = eaistoBodyNumberEditText.getText().toString();

        final EditText eaistoFrameNumberEditText = (EditText) this.findViewById(R.id.polis_frame_number);
        EaistoFrameNumber = eaistoFrameNumberEditText.getText().toString();

        final EditText eaistoRegNumberEditText = (EditText) this.findViewById(R.id.polis_reg_number);
        EaistoRegNumber = eaistoRegNumberEditText.getText().toString();
        EaistoRegNumber = SanitizeHelper.sanitizeString(EaistoRegNumber);

        if (isConnectedToInternet()) {
            if (!EaistoVin.trim().isEmpty() || !EaistoBodyNumber.trim().isEmpty() ||
                    !EaistoFrameNumber.trim().isEmpty() || !EaistoRegNumber.trim().isEmpty()) {

                ArrayList<String> passing = new ArrayList<String>();
                passing.add(EaistoVin);
                passing.add(EaistoBodyNumber);
                passing.add(EaistoFrameNumber);
                passing.add(EaistoRegNumber);

                //save to the history
                Polis newPolis = new Polis();
                newPolis.vin = EaistoVin;
                newPolis.bodyNumber = EaistoBodyNumber;
                newPolis.frameNumber = EaistoFrameNumber;
                newPolis.regNumber = EaistoRegNumber;

                // Get singleton instance of database
                HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(activity);
                // Add polis to the database
                databaseHelper.addPolis(newPolis);

                RunCounts requestCounts = new RunCounts();
                requestCounts.increaseCheckAutoCount(activity);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "polis");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, EaistoVin + ";" + EaistoBodyNumber
                        + ";" + EaistoFrameNumber + ";" + EaistoRegNumber);
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "POLIS");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                try {
                    downloadReCaptcha = new DownloadReCaptcha();
                    downloadReCaptcha.execute();
                } catch (Exception e){
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

    @OnClick(R.id.paste1)
    public void onPaste1Clicked(View view) {
        final EditText plateEditText = (EditText) findViewById(R.id.polis_vin);
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

    @OnClick(R.id.paste2)
    public void onPaste2Clicked(View view) {
        final EditText plateEditText = (EditText) findViewById(R.id.polis_body_number);
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

    @OnClick(R.id.paste3)
    public void onPaste3Clicked(View view) {
        final EditText plateEditText = (EditText) findViewById(R.id.polis_frame_number);
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
        final EditText plateEditText = (EditText) findViewById(R.id.polis_reg_number);
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

    @Override
    public void onBackPressed() {
        if (captchaWebView.getVisibility() == View.VISIBLE) {
            captchaWebView.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
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
        return R.id.nav_polis;
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

    private class DownloadReCaptcha extends AsyncTask<Void, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();

            CardView card_results = (CardView) activity.findViewById(R.id.cardResults);
            card_results.setVisibility(View.GONE);

            mRecyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view_polis);
            mRecyclerView.setVisibility(View.GONE);
            reloadWebView();
            captchaWebView.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if (activity != null && activity.isFinishing()) {
                return;
            }

            dismissProgressDialog();

        }
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

            logD(TAG, "get polis data");

            String vin = (String) mStringArray[0];
            String bodyNumber = (String) mStringArray[1];
            String chassisNumber = (String) mStringArray[2];
            String lp = (String) mStringArray[3];
            String date = (String) mStringArray[4];
            String answer = (String) mStringArray[5];

            RequestBody formBody = new FormBody.Builder()
                    .add("vin", vin)
                    .add("bodyNumber", bodyNumber)
                    .add("chassisNumber", chassisNumber)
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
            if (activity != null && activity.isFinishing()) {
                return;
            }

            captchaWebView.setVisibility(View.GONE);

            //{"bodyNumber":"","chassisNumber":"","licensePlate":"","vin":"WF0DXXGBBD8D01928","policyUnqId":null,"policyResponseUIItems":[{"policyIsRestrict":"1","policyUnqId":"251896973","insCompanyName":"РОСГОССТРАХ","policyBsoNumber":"0377148304","policyBsoSerial":"ЕЕЕ"},{"policyIsRestrict":"1","policyUnqId":"264923711","insCompanyName":"РЕСО-Гарантия","policyBsoNumber":"0000795974","policyBsoSerial":"ХХХ"}],"validCaptcha":true,"errorMessage":null,"errorId":0,"warningMessage":null}

            JSONObject responseJsonObject = null;
            Boolean validCaptcha = false;
            String warningMessage = "";
            String error_color = "#CD5C5C";

            TextView textHeader = (TextView) activity.findViewById(R.id.details_header);
            TextView textData = (TextView) activity.findViewById(R.id.details_data);
            LinearLayout layout_result = (LinearLayout) activity.findViewById(R.id.layout_result);
            CardView card_results = (CardView) activity.findViewById(R.id.cardResults);

            mRecyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view_polis);
            mRecyclerView.setVisibility(View.GONE);
            mRecyclerView.setNestedScrollingEnabled(false);

            try {
                responseJsonObject = new JSONObject(response);
                validCaptcha = responseJsonObject.getBoolean("validCaptcha");
                warningMessage = responseJsonObject.getString("warningMessage");
            } catch (JSONException e) {
                e.printStackTrace();
                card_results.setVisibility(View.VISIBLE);
                textHeader.setText(activity.getResources().getString(R.string.error));
                textData.setText(activity.getResources().getString(R.string.insurance_error));
                layout_result.setBackgroundColor(Color.parseColor(error_color));
            }

            if (response.equals("")) {
                card_results.setVisibility(View.VISIBLE);
                textHeader.setText(activity.getResources().getString(R.string.error));
                textData.setText(activity.getResources().getString(R.string.insurance_error));
                layout_result.setBackgroundColor(Color.parseColor(error_color));
                //Hide keyboard
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(layout_result.getWindowToken(), 0);
                scroolToResult();
            }

            if (validCaptcha && warningMessage.equals("null")) {
                try {
                    JSONArray responseJsonArray = responseJsonObject.getJSONArray("policyResponseUIItems");
                    parsePolisResult(responseJsonArray.toString());

                    Integer numberPolis = responseJsonArray.length();

                    if (numberPolis == 0) {
                        card_results = (CardView) activity.findViewById(R.id.cardResults);
                        card_results.setVisibility(View.VISIBLE);
                        textHeader.setText(activity.getResources().getString(R.string.warning));
                        textData.setText(activity.getResources().getString(R.string.polis_warning_not_found));
                        layout_result.setBackgroundColor(Color.parseColor(error_color));
                    }

                    polisAdapter = new PolisItemRecyclerViewAdapter(activity, polisList);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mRecyclerView.setAdapter(polisAdapter);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

                } catch (Exception e) {
                    card_results = (CardView) activity.findViewById(R.id.cardResults);
                    card_results.setVisibility(View.VISIBLE);
                    textHeader.setText(activity.getResources().getString(R.string.warning));
                    textData.setText(activity.getResources().getString(R.string.polis_warning_not_found));
                    layout_result.setBackgroundColor(Color.parseColor(error_color));
                }
            } else if (!validCaptcha && !response.equals("")) {
                card_results.setVisibility(View.VISIBLE);
                textHeader.setText(activity.getResources().getString(R.string.error));
                textData.setText(activity.getResources().getString(R.string.insurance_error_captcha));
                layout_result.setBackgroundColor(Color.parseColor(error_color));

            } else if (!warningMessage.equals("null") && !warningMessage.equals("")){
                card_results.setVisibility(View.VISIBLE);
                textHeader.setText(activity.getResources().getString(R.string.error));
                textData.setText(warningMessage);
                layout_result.setBackgroundColor(Color.parseColor(error_color));
            }

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

            //Hide keyboard
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(layout_result.getWindowToken(), 0);

            scroolToResult();

            /*if (!EaistoVin.isEmpty()) {
                CardView card_next_check = (CardView) activity.findViewById(R.id.cardNextDecoder);
                card_next_check.setVisibility(View.VISIBLE);
                String next_card_color = "#e6e6e6";
                card_next_check.setCardBackgroundColor(Color.parseColor(next_card_color));

                View.OnClickListener card_next_check_listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent detailIntent = new Intent(activity, VinDecoderActivity.class);
                        detailIntent.putExtra(VinDecoderActivity.ARG_VIN, EaistoVin);
                        startActivity(detailIntent);
                    }
                };

                card_next_check.setOnClickListener(card_next_check_listener);
            }*/

        }
    }

    private void parsePolisResult(String result) {
        try {
            JSONArray records = new JSONArray(result);
            polisList = new ArrayList<>();

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.optJSONObject(i);

                PolisItem item = new PolisItem();

                String isRestrict = record.optString("policyIsRestrict");
                if (isRestrict.equals("0")) {
                    item.setIsRestrict(activity.getResources().getString(R.string.polis_restricted_no));
                } else if (isRestrict.equals("1")) {
                    item.setIsRestrict(activity.getResources().getString(R.string.polis_restricted_yes));
                }

                item.setInsCompanyName(record.optString("insCompanyName"));
                item.setPolicyBsoNumber(record.optString("policyBsoNumber"));
                item.setPolicyBsoSerial(record.optString("policyBsoSerial"));

                polisList.add(item);
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
    public class AutoinsWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        public void onReceivedError(android.webkit.WebView view, WebResourceRequest request, WebResourceError error) {
            view.stopLoading();
            captchaWebView.setVisibility(View.GONE);
            final LinearLayout samplesMain = (LinearLayout) findViewById(R.id.samples_main);
            mSnackbar = Snackbar.make(samplesMain, activity.getResources().getString(R.string.insurance_error), Snackbar.LENGTH_INDEFINITE)
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
                view.loadUrl("javascript:var int; int = setInterval(function(){if($('#g-recaptcha-response').val().length > 0){ window.AutoinsInterface.make($('#g-recaptcha-response').val()); clearInterval(int); } },1000);");
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
        public void make(String key) throws Exception {
            if (key != null && key.length() > 0) {

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                String formattedDate = df.format(c.getTime());

                ArrayList<String> passing1 = new ArrayList<String>();
                passing1.add(EaistoVin);
                passing1.add(EaistoBodyNumber);
                passing1.add(EaistoFrameNumber);
                passing1.add(EaistoRegNumber);
                passing1.add(formattedDate);
                passing1.add(key);


                getPolisData = new GetPolisData();
                try {
                    getPolisData.execute(passing1);
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