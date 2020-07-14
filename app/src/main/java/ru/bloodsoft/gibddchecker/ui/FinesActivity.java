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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
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
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.database.HistoryDatabaseHelper;
import ru.bloodsoft.gibddchecker.models.Divisions;
import ru.bloodsoft.gibddchecker.models.Fine;
import ru.bloodsoft.gibddchecker.models.FinesItem;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;
import ru.bloodsoft.gibddchecker.ui.recycler_views.FinesItemRecyclerViewAdapter;
import ru.bloodsoft.gibddchecker.util.WebService;
import ru.bloodsoft.gibddchecker.util.RunCounts;
import ru.bloodsoft.gibddchecker.util.SanitizeHelper;
import ru.bloodsoft.gibddchecker.util.SettingsStorage;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class FinesActivity extends BaseActivity {

    public static final String ARG_FINES_REG_NUMBER = "reg_number";
    public static final String ARG_FINES_STS_NUMBER = "sts_number";
    String RegNumber;
    String StsNumber;
    String vinResult;
    String plateResult;
    DownloadReCaptcha downloadCaptcha;
    public static final String URL_GET_CAPTCHA = "https://xn--b1afk4ade.xn--90adear.xn--p1ai/proxy/captcha.jpg";
    public static final String URL_GET_FINES ="https://xn--b1afk4ade.xn--90adear.xn--p1ai/proxy/check/fines";
    public static final String URL_GET_GIBDD ="https://xn--90adear.xn--p1ai/check/fines";
    ProgressDialog mProgressDialog;
    WebService getFinesRequest;
    GetFinesData getFinesData;
    private Snackbar mSnackbar;
    private RecyclerView mRecyclerView;
    private FinesItemRecyclerViewAdapter finesAdapter;
    private List<FinesItem> finesList;
    Activity activity;
    private static final String TAG = makeLogTag(FinesActivity.class);
    InterstitialAd mInterstitialAd;
    private FirebaseAnalytics mFirebaseAnalytics;
    NestedScrollView mScrollView;

    @BindView(R.id.captcha_webview)
    WebView captchaWebView;

    @BindView(R.id.progress_layout)
    View progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fines);

        activity = this;

        final EditText regNumberEditText = (EditText) this.findViewById(R.id.fines_regnumber);
        final EditText stsNumberEditText = (EditText) this.findViewById(R.id.fines_stsnumber);

        mScrollView = (NestedScrollView) findViewById(R.id.scrollView);

        Bundle b = activity.getIntent().getExtras();
        if (b != null) {
            String regNumber = b.getString(ARG_FINES_REG_NUMBER);
            String stsNumber = b.getString(ARG_FINES_STS_NUMBER);

            if (regNumber != null && !regNumber.isEmpty()) {
                regNumberEditText.setText(regNumber);
            }

            if (stsNumber != null && !stsNumber.isEmpty()) {
                stsNumberEditText.setText(stsNumber);
            }
        }

        RegNumber = regNumberEditText.getText().toString();
        StsNumber = stsNumberEditText.getText().toString();

        mRecyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view_fines);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        finesAdapter = new FinesItemRecyclerViewAdapter(activity, finesList);
        mRecyclerView.setAdapter(finesAdapter);

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

    @OnClick(R.id.paste)
    public void onPasteClicked(View view) {
        final EditText regNumberEditText = (EditText) findViewById(R.id.fines_regnumber);
        try {
            ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = clipboard.getPrimaryClip();
            String regNumberText = "";

            if (clipData != null) {
                ClipData.Item item = clipData.getItemAt(0);
                if (item != null && item.getText() != null) {
                    regNumberText = item.getText().toString();
                }
            }

            if (!regNumberText.isEmpty()) {
                regNumberText = SanitizeHelper.sanitizeString(regNumberText);
                regNumberEditText.setText(regNumberText);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.paste_sts)
    public void onPasteStsClicked(View view) {
        final EditText stsNumberEditText = (EditText) findViewById(R.id.fines_stsnumber);
        try {
            ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = clipboard.getPrimaryClip();
            String stsNumberText = "";

            if (clipData != null) {
                ClipData.Item item = clipData.getItemAt(0);
                if (item != null && item.getText() != null) {
                    stsNumberText = item.getText().toString();
                }
            }

            if (!stsNumberText.isEmpty()) {
                stsNumberText = SanitizeHelper.sanitizeString(stsNumberText);
                stsNumberEditText.setText(stsNumberText);
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

        final EditText regNumberEditText = (EditText) this.findViewById(R.id.fines_regnumber);
        final EditText stsNumberEditText = (EditText) this.findViewById(R.id.fines_stsnumber);

        RegNumber = regNumberEditText.getText().toString();
        StsNumber = stsNumberEditText.getText().toString();

        if (isConnectedToInternet()) {
            if (RegNumber != null && !RegNumber.trim().isEmpty() && StsNumber != null && !StsNumber.trim().isEmpty()) {
                //save vin to the database
                Fine newFine = new Fine();
                newFine.regNumber = RegNumber;
                newFine.stsNumber = StsNumber;

                // Get singleton instance of database
                HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(activity);
                // Add insurance to the database
                databaseHelper.addFine(newFine);

                RunCounts requestCounts = new RunCounts();
                requestCounts.increaseCheckAutoCount(activity);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "fines");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, RegNumber + "/" + StsNumber);
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "FINES");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                downloadCaptcha = new DownloadReCaptcha();
                downloadCaptcha.execute();
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
        return R.id.nav_fines;
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

    private class GetFinesData extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected String doInBackground(ArrayList<String>... passing) {

            ArrayList<String> passed = passing[0]; //get passed arraylist
            Object[] mStringArray = passed.toArray();

            /*
                http://check.gibdd.ru/proxy/check/fines
                regnum:М820НЕ
                regreg:64
                stsnum:6404842201
                captchaWord:10551
             */

            logD(TAG, "get fines data");

            String regNum = (String) mStringArray[0];
            regNum = SanitizeHelper.antiTransliterate(regNum);
            String stsNum = (String) mStringArray[1];
            String captchaWord = (String) mStringArray[2];

            String regReg = "";
            Pattern p = Pattern.compile("[0-9]+$");
            Matcher m = p.matcher(regNum);
            if (m.find()) {
                regReg = m.group();
            }

            regNum = regNum.replace(regReg, "");

            logD(TAG, "regnum: " + regNum);
            logD(TAG, "regreg: " + regReg);
            logD(TAG, "stsnum: " + stsNum);
            logD(TAG, "captchaWord: " + captchaWord);

            /*RequestBody formBody = new FormBody.Builder()
                    .add("regnum", regNum)
                    .add("stsnum", stsNum)
                    .add("captchaWord", captchaWord)
                    .add("regreg", regReg)
                    .build();*/

            String params = "regnum=" + regNum + "&stsnum=" + stsNum +
                             "&captchaWord=" + "&regreg=" + regReg + "&reCaptchaToken=" +
                    captchaWord;

            String response = "";
            try {
                response = getFinesRequest.sendGibddHttpsPost(URL_GET_FINES, params);
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

            dismissProgressDialog();

            captchaWebView.setVisibility(View.GONE);

            String error_color = "#CD5C5C";
            String no_error_color = "#9EF39B";

            TextView textHeader = (TextView) activity.findViewById(R.id.details_header);
            TextView textData = (TextView) activity.findViewById(R.id.details_data);

            Integer code = 0;
            String reqToken = "";
            String status = "";
            JSONObject responseJsonObject = null;

            LinearLayout layout_result = (LinearLayout) activity.findViewById(R.id.layout_result);

            mRecyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view_fines);
            mRecyclerView.setVisibility(View.GONE);
            mRecyclerView.setNestedScrollingEnabled(false);

            try {
                responseJsonObject = new JSONObject(response);
                code = responseJsonObject.optInt("code");
                reqToken = responseJsonObject.optString("reqToken");
                status = responseJsonObject.optString("status");
            } catch (JSONException e) {
                e.printStackTrace();
                CardView card_results = (CardView) activity.findViewById(R.id.cardResults);
                card_results.setVisibility(View.VISIBLE);
                textHeader.setText(activity.getResources().getString(R.string.error));
                textData.setText(activity.getResources().getString(R.string.phone_error_details));
                layout_result.setBackgroundColor(Color.parseColor(error_color));
            }

            logD(TAG, "Code: " + code.toString());

            if (code == 200) {
                try {
                    JSONArray RequestResult = responseJsonObject.getJSONArray("data");
                    JSONObject DivisionObjects = responseJsonObject.getJSONObject("divisions");
                    parseDivisionsResult(DivisionObjects.toString());
                    parseFinesResult(RequestResult.toString(), reqToken);
                    Integer numberFines = RequestResult.length();

                    finesAdapter = new FinesItemRecyclerViewAdapter(activity, finesList);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mRecyclerView.setAdapter(finesAdapter);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

                    if (numberFines == 0) {
                        CardView card_results = (CardView) activity.findViewById(R.id.cardResults);
                        card_results.setVisibility(View.VISIBLE);
                        textHeader.setText(activity.getResources().getString(R.string.notice));
                        textData.setText(activity.getResources().getString(R.string.notice_fines));
                        layout_result.setBackgroundColor(Color.parseColor(no_error_color));
                    }

                } catch (JSONException e) {
                    CardView card_results = (CardView) activity.findViewById(R.id.cardResults);
                    card_results.setVisibility(View.VISIBLE);
                    textHeader.setText(activity.getResources().getString(R.string.notice));
                    textData.setText(activity.getResources().getString(R.string.notice_fines));
                    layout_result.setBackgroundColor(Color.parseColor(no_error_color));
                }
            } else if (status.equals("404")) {
                CardView card_results = (CardView) activity.findViewById(R.id.cardResults);
                card_results.setVisibility(View.VISIBLE);
                textHeader.setText(activity.getResources().getString(R.string.error));
                textData.setText(activity.getResources().getString(R.string.fines_error_details));
                layout_result.setBackgroundColor(Color.parseColor(error_color));
            } else if (status.equals("1")) {
                CardView card_results = (CardView) activity.findViewById(R.id.cardResults);
                card_results.setVisibility(View.VISIBLE);
                textHeader.setText(activity.getResources().getString(R.string.error));
                textData.setText(activity.getResources().getString(R.string.reestr_error_captcha));
                layout_result.setBackgroundColor(Color.parseColor(error_color));
            } else {
                CardView card_results = (CardView) activity.findViewById(R.id.cardResults);
                card_results.setVisibility(View.VISIBLE);
                textHeader.setText(activity.getResources().getString(R.string.error));
                textData.setText(activity.getResources().getString(R.string.phone_error_details));
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

        }
    }

    private void parseFinesResult(String result, String reqToken) {
        try {
            JSONArray records = new JSONArray(result);
            finesList = new ArrayList<>();
            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.optJSONObject(i);
                FinesItem item = new FinesItem();

                item.setDate(record.optString("DateDecis"));
                item.setDivision(record.optString("Division"));
                item.setKoap(record.optString("KoAPcode") + " " + record.optString("KoAPtext").toLowerCase());
                item.setNumPost(record.optString("NumPost"));
                item.setSumma(record.optString("Summa"));
                item.setReqToken(reqToken);
                item.setRegNumber(RegNumber);

                Divisions divisions = new Divisions();
                Divisions.Division division = divisions.getDivisionByDivId(record.optString("Division"));

                if (division != null) {
                    item.setFullAddr(division.fulladdr);
                    item.setCoordinates(division.coords);
                }

                finesList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseDivisionsResult(String result) {
        try {
            JSONObject records = new JSONObject(result);
            Iterator keys = records.keys();

            while(keys.hasNext()) {
                String dynamicKey = (String) keys.next();
                JSONObject division = records.getJSONObject(dynamicKey);
                String fulladdr = division.optString("fulladdr");
                String coords = division.optString("coords");
                Integer divId = division.optInt("divId");

                Divisions.Division.addItem(new Divisions.Division(divId.toString(), fulladdr, coords));
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

    private class DownloadReCaptcha extends AsyncTask<Void, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();

            CardView card_results = (CardView) activity.findViewById(R.id.cardResults);
            card_results.setVisibility(View.GONE);

            mRecyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view_fines);
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

    private void reloadWebView() {
        progressView.setVisibility(View.VISIBLE);
        captchaWebView.loadUrl(URL_GET_GIBDD);
        captchaWebView.getSettings().setJavaScriptEnabled(true);
        captchaWebView.getSettings().setDomStorageEnabled(true);
        captchaWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        captchaWebView.addJavascriptInterface(new JavaScriptInterface(captchaWebView), "AutoinsInterface");
        captchaWebView.setWebChromeClient(new WebChromeClient());
        captchaWebView.setWebViewClient(new AutoinsWebViewClient());
    }

    public class AutoinsWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Toast.makeText(activity, getString(R.string.gibdd_captcha), Toast.LENGTH_LONG).show();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        public void onReceivedError(android.webkit.WebView view, WebResourceRequest request, WebResourceError error) {
            view.stopLoading();
            captchaWebView.setVisibility(View.GONE);
            final LinearLayout samplesMain = (LinearLayout) activity.findViewById(R.id.samples_main);
            mSnackbar = Snackbar.make(samplesMain, activity.getResources().getString(R.string.insurance_error), Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null)
                    .setDuration(2000);
            mSnackbar.show();
            progressView.setVisibility(View.GONE);
        }

        public void onReceivedSslError(android.webkit.WebView view, SslErrorHandler handler, SslError er) {
            SslErrorHandler sslErrorHandler = handler;
            if (er.getUrl().equals(URL_GET_GIBDD)) {
                sslErrorHandler.proceed();
            } else {
                sslErrorHandler.cancel();
            }
            progressView.setVisibility(View.GONE);
        }

        public void onPageFinished(android.webkit.WebView view, String url) {
            super.onPageFinished(view, url);
            if (url.equals(URL_GET_GIBDD)) {
                view.loadUrl("javascript:$('.ln-header').hide()");
                view.loadUrl("javascript:$('.ln-footer').hide()");
                view.loadUrl("javascript:$('.bn-federal-site').hide()");
                view.loadUrl("javascript:$('.bn-top-menu').hide()");
                view.loadUrl("javascript:$('.ln-content-right').hide()");
                view.loadUrl("javascript:$('.widget-mistake').hide()");
                view.loadUrl("javascript:$('.b-mobile-section').hide()");
                view.loadUrl("javascript:$('h1,h2').hide()");
                view.loadUrl("javascript:$('.ln-page').hide()");

                view.loadUrl("javascript:var int; int = setInterval(function(){if(appCheckFines.reCaptchaToken.length > 0){ window.AutoinsInterface.make(appCheckFines.reCaptchaToken); clearInterval(int); } },1000);");
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
        public void make(String captcha_answer) throws Exception {
            if (captcha_answer != null && captcha_answer.length() > 0) {


                ArrayList<String> passing1 = new ArrayList<String>();
                passing1.add(RegNumber);
                passing1.add(StsNumber);
                passing1.add(captcha_answer);


                getFinesData = new GetFinesData();
                try {
                    getFinesData.execute(passing1);
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