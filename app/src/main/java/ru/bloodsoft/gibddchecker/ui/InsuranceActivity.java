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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.database.HistoryDatabaseHelper;
import ru.bloodsoft.gibddchecker.models.Insurance;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;
import ru.bloodsoft.gibddchecker.util.NewWebService;
import ru.bloodsoft.gibddchecker.util.RunCounts;
import ru.bloodsoft.gibddchecker.util.SanitizeHelper;
import ru.bloodsoft.gibddchecker.util.SettingsStorage;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class InsuranceActivity extends BaseActivity {

    public static final String ARG_INSURANCE_NUMBER = "insurance_number";
    public static final String ARG_INSURANCE_SERIAL = "insurance_serial";
    String InsuranceNumber;
    String InsuranceSerial;
    String vinResult;
    String plateResult;
    DownloadReCaptcha downloadReCaptcha;
    public static final String URL_GET_INSURANCE ="https://dkbm-web.autoins.ru/dkbm-web-1.0/osagovehicle.htm";
    ProgressDialog mProgressDialog;
    NewWebService getInsuranceRequest;
    GetInsuranceData getInsuranceData;
    private Snackbar mSnackbar;
    Activity activity;
    private static final String TAG = makeLogTag(InsuranceActivity.class);
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
        setContentView(R.layout.activity_insurance);
        ButterKnife.bind(this);

        progressView.setVisibility(View.GONE);
        Spinner spinner = (Spinner) findViewById(R.id.insurance_series);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.insurance_series_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        activity = this;
        vinResult = "";
        plateResult = "";

        final EditText insuranceNumber = (EditText) this.findViewById(R.id.insurance_number);
        mScrollView = (NestedScrollView) findViewById(R.id.scrollView);

        Bundle b = activity.getIntent().getExtras();
        if (b != null) {
            String insNumber = b.getString(ARG_INSURANCE_NUMBER);
            String insSerial = b.getString(ARG_INSURANCE_SERIAL);
            if (insNumber != null && !insNumber.isEmpty()) {
                insuranceNumber.setText(insNumber);
                spinner.setSelection(adapter.getPosition(insSerial));
            }
        }

        InsuranceNumber = insuranceNumber.getText().toString();

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

    private void reloadWebView() {
        progressView.setVisibility(View.VISIBLE);
        captchaWebView.loadUrl(URL_GET_INSURANCE);
        captchaWebView.getSettings().setJavaScriptEnabled(true);
        captchaWebView.getSettings().setDomStorageEnabled(true);
        captchaWebView.addJavascriptInterface(new JavaScriptInterface(captchaWebView), "AutoinsInterface");
        captchaWebView.setWebViewClient(new AutoinsWebViewClient());
    }

    @OnClick(R.id.paste)
    public void onPasteClicked(View view) {
        final EditText insuranceEditText = (EditText) findViewById(R.id.insurance_number);
        try {
            ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = clipboard.getPrimaryClip();
            String insuranceNumber = "";

            if (clipData != null) {
                ClipData.Item item = clipData.getItemAt(0);
                if (item != null && item.getText() != null) {
                    insuranceNumber = item.getText().toString();
                }
            }

            if (!insuranceNumber.isEmpty()) {
                insuranceNumber = SanitizeHelper.sanitizeString(insuranceNumber);
                insuranceEditText.setText(insuranceNumber);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("E31198FB67C1181AF2CFCEDF4476A9D1")
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    @OnClick(R.id.fab)
    public void onFabClicked(View view) {

        final EditText insuranceNumber = (EditText) this.findViewById(R.id.insurance_number);
        Spinner insuranceSerial = (Spinner) this.findViewById(R.id.insurance_series);

        InsuranceNumber = insuranceNumber.getText().toString();
        InsuranceSerial = insuranceSerial.getSelectedItem().toString();

        if (isConnectedToInternet()) {
            if (InsuranceNumber != null && !InsuranceNumber.trim().isEmpty()) {
                //save vin to the database
                Insurance newInsurance = new Insurance();
                newInsurance.insuranceText = InsuranceNumber;
                newInsurance.insuranceSerial = InsuranceSerial;

                // Get singleton instance of database
                HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(activity);
                // Add insurance to the database
                databaseHelper.addInsurance(newInsurance);

                RunCounts requestCounts = new RunCounts();
                requestCounts.increaseCheckAutoCount(activity);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "insurance");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, InsuranceSerial + InsuranceNumber);
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "INSURANCE");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                downloadReCaptcha = new DownloadReCaptcha();
                downloadReCaptcha.execute();
            } else {
                mSnackbar = Snackbar.make(view, activity.getResources().getString(R.string.error_empty_insurance), Snackbar.LENGTH_INDEFINITE)
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

    @OnClick(R.id.copy_vin)
    public void onCopyVinClicked(View view) {
        Button copyButton = (Button) view.findViewById(R.id.copy_vin);

        ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(vinResult, vinResult);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(App.getContext(), "Скопировано", Toast.LENGTH_LONG).show();

    }

    @OnClick(R.id.copy_plate)
    public void onCopyPlateClicked(View view) {
        Button copyButton = (Button) view.findViewById(R.id.copy_plate);

        ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(plateResult, plateResult);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(App.getContext(), "Скопировано", Toast.LENGTH_LONG).show();

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
        return R.id.nav_insurance;
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

            CardView cardInsuranseDetails = (CardView) activity.findViewById(R.id.cardInsuranceDetails);
            cardInsuranseDetails.setVisibility(View.GONE);
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
            if (activity != null && activity.isFinishing()) {
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

            TextView textHeader = (TextView) activity.findViewById(R.id.details_header);
            TextView textData = (TextView) activity.findViewById(R.id.details_data);
            LinearLayout layout_result = (LinearLayout) activity.findViewById(R.id.layout_result);
            CardView card_results = (CardView) activity.findViewById(R.id.cardResults);

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
            }

            if (validCaptcha && warningMessage.equals("null")) {
                //insurance details
                try {
                    CardView cardInsuranceDetails = (CardView) activity.findViewById(R.id.cardInsuranceDetails);
                    cardInsuranceDetails.setVisibility(View.VISIBLE);

                    String insurerName = responseJsonObject.getString("insurerName");
                    String licensePlate = responseJsonObject.getString("licensePlate");
                    String policyStatus = responseJsonObject.getString("policyStatus");
                    String vin = responseJsonObject.getString("vin");

                    TextView insurerNameText = (TextView) activity.findViewById(R.id.insuranceName);
                    TextView licensePlateText = (TextView) activity.findViewById(R.id.insuranceLicensePlate);
                    TextView policyStatusText = (TextView) activity.findViewById(R.id.insuranceStatus);
                    TextView vinText = (TextView) activity.findViewById(R.id.insuranceVin);

                    insurerNameText.setText(insurerName);
                    licensePlateText.setText(licensePlate);
                    policyStatusText.setText(policyStatus);
                    vinText.setText(vin);
                    vinResult = vin;
                    plateResult = licensePlate;

                } catch (JSONException e) {
                    CardView cardInsuranceDetails = (CardView) activity.findViewById(R.id.cardInsuranceDetails);
                    cardInsuranceDetails.setVisibility(View.GONE);
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
            if (er.getUrl().equals(URL_GET_INSURANCE)) {
                sslErrorHandler.proceed();
            } else {
                sslErrorHandler.cancel();
            }
            progressView.setVisibility(View.GONE);
        }

        public void onPageFinished(android.webkit.WebView view, String url) {
            super.onPageFinished(view, url);
            if (url.equals(URL_GET_INSURANCE)) {
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
                passing1.add(InsuranceSerial);
                passing1.add(InsuranceNumber);
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