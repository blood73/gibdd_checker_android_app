package ru.bloodsoft.gibddchecker.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import ru.bloodsoft.gibddchecker.models.ReestrItem;
import ru.bloodsoft.gibddchecker.models.ReestrRequest;
import ru.bloodsoft.gibddchecker.models.Vin;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;
import ru.bloodsoft.gibddchecker.ui.recycler_views.ReestrItemRecyclerViewAdapter;
import ru.bloodsoft.gibddchecker.util.KeyBoardUtils;
import ru.bloodsoft.gibddchecker.util.NewWebService;
import ru.bloodsoft.gibddchecker.util.RunCounts;
import ru.bloodsoft.gibddchecker.util.SanitizeHelper;
import ru.bloodsoft.gibddchecker.util.SettingsStorage;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class ReestrActivity extends BaseActivity {

    String reestrVin;
    public static final String URL_GET_CAPTCHA = "https://antiperekup.net/api/v1/reestr_captcha";
    public static final String URL_GET_REESTR = "https://antiperekup.net/api/v1/reestr";
    public static final String ARG_VIN = "vin";

    ProgressDialog mProgressDialog;
    NewWebService getReestrRequest;
    GetReestrData getReestrData;
    NestedScrollView mScrollView;
    private Snackbar mSnackbar;
    Activity activity;
    private static final String TAG = makeLogTag(ReestrActivity.class);
    InterstitialAd mInterstitialAd;
    private FirebaseAnalytics mFirebaseAnalytics;
    private List<ReestrItem> reestrList;
    private RecyclerView mRecyclerView;
    private ReestrItemRecyclerViewAdapter reestrAdapter;
    DownloadCaptcha downloadReCaptcha;
    private String UUID;
    private String SESSION_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reestr);
        ButterKnife.bind(this);

        activity = this;
        final EditText reestrVinText = (EditText) this.findViewById(R.id.reestr_vin);

        Bundle b = activity.getIntent().getExtras();
        if (b != null) {
            String vin = b.getString(ARG_VIN);
            if (vin != null && !vin.isEmpty()) {
                reestrVinText.setText(vin);
            }
        }

        reestrVin = reestrVinText.getText().toString();

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

        mScrollView = (NestedScrollView) findViewById(R.id.scrollView);

        mRecyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view_reestr);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        reestrAdapter = new ReestrItemRecyclerViewAdapter(activity, reestrList);
        mRecyclerView.setAdapter(reestrAdapter);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);

        setupToolbar();
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

        final EditText reestrVinText = (EditText) this.findViewById(R.id.reestr_vin);

        reestrVin = reestrVinText.getText().toString();

        if (isConnectedToInternet()) {
            if (reestrVin != null && !reestrVin.trim().isEmpty()) {
                //save vin to the history
                Vin newVin = new Vin();
                newVin.vinText = reestrVin;
                newVin.vinType = "reestr";
                // Get singleton instance of database
                HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(activity);
                // Add vin to the database
                databaseHelper.addVin(newVin);

                RunCounts requestCounts = new RunCounts();
                requestCounts.increaseCheckAutoCount(activity);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, newVin.vinType);
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, reestrVin);
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "VIN");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                downloadReCaptcha = new DownloadCaptcha();
                downloadReCaptcha.execute();
            } else {
                mSnackbar = Snackbar.make(view, activity.getResources().getString(R.string.error_empty_vin), Snackbar.LENGTH_INDEFINITE)
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

    @OnClick(R.id.paste)
    public void onPasteClicked(View view) {
        final EditText plateEditText = (EditText) findViewById(R.id.reestr_vin);
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
        return R.id.nav_reestr;
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

    private class DownloadCaptcha extends AsyncTask<Void, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
            UUID = "";
            SESSION_ID = "";

            CardView card_results = (CardView) activity.findViewById(R.id.cardResults);
            card_results.setVisibility(View.GONE);

            mRecyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view_reestr);
            mRecyclerView.setVisibility(View.GONE);

        }

        @Override
        protected String doInBackground(Void... params) {

            String response = "";

            RequestBody formBody = new FormBody.Builder()
                    .build();

            try {
                response = getReestrRequest.sendNewHttpsPost(URL_GET_CAPTCHA, formBody);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if (activity != null && activity.isFinishing()) {
                return;
            }

            if (activity == null) {
                return;
            }

            dismissProgressDialog();

            JSONObject responseJsonObject = null;

            TextView textHeader = (TextView) ReestrActivity.this.findViewById(R.id.details_header);
            TextView textData = (TextView) ReestrActivity.this.findViewById(R.id.details_data);
            LinearLayout layout_result = (LinearLayout) ReestrActivity.this.findViewById(R.id.layout_result);
            CardView card_results = (CardView) ReestrActivity.this.findViewById(R.id.cardResults);

            String error_color = "#CD5C5C";
            String no_error_color = "#9EF39B";

            String imageBase64 = "";

            try {
                responseJsonObject = new JSONObject(result);
                responseJsonObject = responseJsonObject.optJSONObject("data");
            } catch (JSONException e) {
                e.printStackTrace();
                card_results = (CardView) ReestrActivity.this.findViewById(R.id.cardResults);
                card_results.setVisibility(View.VISIBLE);
                textHeader.setText(ReestrActivity.this.getResources().getString(R.string.error));
                textData.setText(ReestrActivity.this.getResources().getString(R.string.mileage_error_details));
                layout_result.setBackgroundColor(Color.parseColor(error_color));
            }

            if (responseJsonObject != null) {
                try {
                    imageBase64 = responseJsonObject.optString("image");
                    UUID = responseJsonObject.optString("uuid");
                    SESSION_ID = responseJsonObject.optString("session_id");
                } catch (Exception e) {
                    //nothing
                }
            }

            final CoordinatorLayout samplesMain = (CoordinatorLayout) ReestrActivity.this.findViewById(R.id.main_content);

            if (imageBase64 != null && !imageBase64.isEmpty() && !UUID.isEmpty() && !SESSION_ID.isEmpty() && samplesMain != null) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ReestrActivity.this);
                builder.setTitle(R.string.enter_captcha);

                ImageView imageView = new ImageView(ReestrActivity.this);
                imageView.setScaleType(ImageView.ScaleType.CENTER);

                DisplayMetrics metrics = new DisplayMetrics();
                ReestrActivity.this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int layoutWidth = metrics.widthPixels;
                Double captchaWidthD = layoutWidth - layoutWidth * 0.5;
                int captchaWidth = captchaWidthD.intValue();
                Double captchaHeidhthD = captchaWidthD / 2;
                int captchaHeight = captchaHeidhthD.intValue();

                if (captchaWidth == 0) {
                    captchaWidth = 420;
                }
                if (captchaHeight == 0) {
                    captchaHeight = 200;
                }

                String pureBase64Encoded = imageBase64.substring(imageBase64.indexOf(",")  + 1);
                final byte[] decodedBytes = Base64.decode(pureBase64Encoded, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                Bitmap captchaScaled = null;
                if (decodedBitmap != null) {
                    captchaScaled = Bitmap.createScaledBitmap(decodedBitmap, captchaWidth, captchaHeight, true);
                }

                if (captchaScaled != null) {
                    imageView.setImageBitmap(captchaScaled);
                }

                final EditText captcha = new EditText(ReestrActivity.this);
                captcha.setInputType(InputType.TYPE_CLASS_TEXT);
                captcha.setHint(R.string.captcha);

                LinearLayout layout = new LinearLayout(ReestrActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(imageView);
                layout.addView(captcha);

                builder.setView(layout);

                builder.setPositiveButton(R.string.captcha_ok, null);

                builder.setNegativeButton(R.string.captcha_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSnackbar = Snackbar.make(samplesMain, ReestrActivity.this.getResources().getString(R.string.cancelled), Snackbar.LENGTH_INDEFINITE)
                                .setAction("Action", null)
                                .setDuration(2000);
                        mSnackbar.show();
                    }
                });

                final AlertDialog captchaDialog = builder.create();

                captchaDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {

                        Button b = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        b.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                String captcha_answer = captcha.getText().toString();

                                if (isConnectedToInternet()) {
                                    if (!captcha_answer.trim().isEmpty()) {

                                        ReestrRequest reestrRequest = new ReestrRequest();
                                        reestrRequest.setToken(captcha_answer);
                                        reestrRequest.setUuid(UUID);
                                        reestrRequest.setSessionId(SESSION_ID);
                                        reestrRequest.setVin(reestrVin);

                                        getReestrData = new GetReestrData();
                                        try {
                                            getReestrData.execute(reestrRequest);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        //Dismiss once everything is OK.
                                        captchaDialog.dismiss();

                                    } else {
                                        mSnackbar = Snackbar.make(samplesMain, ReestrActivity.this.getResources().getString(R.string.error_empty_captcha_code), Snackbar.LENGTH_INDEFINITE)
                                                .setAction("Action", null)
                                                .setDuration(2000);
                                        mSnackbar.show();

                                        //Hide keyboard
                                        KeyBoardUtils.hideKeyboard(ReestrActivity.this);
                                    }
                                } else {
                                    captchaDialog.dismiss();

                                    mSnackbar = Snackbar.make(samplesMain, ReestrActivity.this.getResources().getString(R.string.error_no_internet), Snackbar.LENGTH_INDEFINITE)
                                            .setAction("Action", null)
                                            .setDuration(2000);
                                    mSnackbar.show();

                                    //Hide keyboard
                                    KeyBoardUtils.hideKeyboard(ReestrActivity.this);
                                }
                            }
                        });
                    }
                });

                captchaDialog.show();
            } else {
                if (samplesMain != null) {
                    mSnackbar = Snackbar.make(samplesMain, ReestrActivity.this.getResources().getString(R.string.error_no_captcha_general), Snackbar.LENGTH_INDEFINITE)
                            .setAction("Action", null)
                            .setDuration(2000);
                    mSnackbar.show();
                }
            }
        }
    }

    private class GetReestrData extends AsyncTask<ReestrRequest, String, String> {

        @Override
        protected void onPreExecute() {
            showProgressDialog();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(ReestrRequest... passing) {

            ReestrRequest reestrRequest = passing[0];

            String vin = reestrRequest.getVin();
            String token = reestrRequest.getToken();
            String uuid = reestrRequest.getUuid();
            String sessionId = reestrRequest.getSessionId();

            logD(TAG, "VIN: " + vin);
            logD(TAG, "token: " + token);
            logD(TAG, "uuid: " + uuid);
            logD(TAG, "session_id: " + sessionId);

            /*
            VIN:WB10A040XHZ282269
            formName:vehicle-form
            token:03AO6mBfzk4yu_JuAGEjHUDuFwEG9732M0nMHrDZdrdOsgx-aT3KdF5SXDYadXhwJ-AvlfCUJ6C5dfXiWe3JflNnYeMgUoEahl98ILKRFUOjHZcZ0JwmXNHfwJ8TFreY0igpGCD1fRkTNofJtl4wHBvO5wZiyutnap2j9xf04K32672nyr91JCJynMHudoDom-FHGBLrezQ-79xzM-KYGtcwIhGV8Q3I1VIOHmQ9Nrxc-WIug6yrDfh-L_YiGk1qgqhMbNv4ObbecOvErnPl9mWfT29Cg7VcWofYA3rlx_c2e1umCnYcBlTVA4g-4FCp0NRwVP6Sz7odSspe-WpxDGnu2qN9gQP0bow6TgqGOA4wATpu2X_QY6P0HvsVSeJgSp0iU_XvPG60Ab1joQw2x3auLpKGGSlV_BAlsuhX8bbqN_ZlJ7YSlNxrw
            uuid:4e46f188-3fe0-4d80-8395-a0f3f8ae815a
            */
            String response = "";

            RequestBody formBody = new FormBody.Builder()
                    .add("vin", vin)
                    .add("token", token)
                    .add("uuid", uuid)
                    .add("session_id", sessionId)
                    .build();

            try {
                response = getReestrRequest.sendNewHttpsPost(URL_GET_REESTR, formBody);
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

            if (activity == null) {
                return;
            }

            dismissProgressDialog();

            //{"list":[{"index":1,"registerDate":"20.01.2017","referenceNumber":"2017-001-127267-316","propertiesRemained":0,"properties":[{"prefix":"VIN","value":"WB10A040XHZ282269"}],"pledgors":[{"type":"person","name":"Иванов Иван Иванович","birth":"01.12.1978"}],"pledgees":[{"type":"org","name":"\"БМВ Банк\" OOO"}],"position":0,"notificationDataUrl":"/search/notificationData?pos=0&uuid=4e46f188-3fe0-4d80-8395-a0f3f8ae815a"}],"totalPages":1,"currentPage":1,"location":"/ruzdi/api/v1/PledgeRegistry/search/904df924-61a6-4f9b-9bb1-4e52d93a4546"}

            String error_color = "#CD5C5C";
            String no_error_color = "#9EF39B";
            String warning = "#FFF380";

            JSONObject responseJsonObject = null;
            JSONArray responseJsonArray = null;

            TextView textHeader = (TextView) activity.findViewById(R.id.details_header);
            TextView textData = (TextView) activity.findViewById(R.id.details_data);
            LinearLayout layout_result = (LinearLayout) activity.findViewById(R.id.layout_result);
            CardView card_results = (CardView) activity.findViewById(R.id.cardResults);

            card_results.setVisibility(View.VISIBLE);

            mRecyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view_reestr);
            mRecyclerView.setVisibility(View.GONE);
            mRecyclerView.setNestedScrollingEnabled(false);

            if (response.isEmpty()) {
                textHeader.setText(activity.getResources().getString(R.string.warning));
                textData.setText(activity.getResources().getString(R.string.reestr_notice));
                layout_result.setBackgroundColor(Color.parseColor(no_error_color));
            } else {
                try {
                    responseJsonObject = new JSONObject(response);
                    responseJsonArray = responseJsonObject.optJSONArray("data");
                    parseReestrResult(responseJsonArray.toString());

                    Integer numberReestr = responseJsonArray.length();

                    if (numberReestr != 0) {
                        textHeader.setText(activity.getResources().getString(R.string.warning));
                        textData.setText(activity.getResources().getString(R.string.reestr_warning_found));
                        layout_result.setBackgroundColor(Color.parseColor(error_color));
                    } else {
                        textHeader.setText(activity.getResources().getString(R.string.warning));
                        textData.setText(activity.getResources().getString(R.string.reestr_notice));
                        layout_result.setBackgroundColor(Color.parseColor(no_error_color));
                    }

                    reestrAdapter = new ReestrItemRecyclerViewAdapter(activity, reestrList);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mRecyclerView.setAdapter(reestrAdapter);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

                } catch (JSONException e) {
                    e.printStackTrace();
                    textHeader.setText(activity.getResources().getString(R.string.error));
                    textData.setText(activity.getResources().getString(R.string.reestr_error_details));
                    layout_result.setBackgroundColor(Color.parseColor(error_color));
                }
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

            CardView cardNextCheck = (CardView) activity.findViewById(R.id.cardNextEaisto);
            cardNextCheck.setVisibility(View.VISIBLE);
            String next_card_color = "#e6e6e6";
            cardNextCheck.setCardBackgroundColor(Color.parseColor(next_card_color));

            View.OnClickListener card_next_check_eaisto_clk_lstr = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent detailIntent = new Intent(activity, EaistoActivity.class);
                    detailIntent.putExtra(EaistoActivity.ARG_VIN, reestrVin);
                    startActivity(detailIntent);
                }
            };

            cardNextCheck.setOnClickListener(card_next_check_eaisto_clk_lstr);

            //Hide keyboard
            KeyBoardUtils.hideKeyboard(ReestrActivity.this);

            scroolToResult();

        }
    }

    private void parseReestrResult(String result) {
        try {
            JSONArray records = new JSONArray(result);
            reestrList = new ArrayList<>();

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.optJSONObject(i);

                ReestrItem item = new ReestrItem();
                item.setRegDate(record.optString("register_date"));
                item.setRegInfoText(record.optString("vin"));
                item.setPledgor(record.optString("pledgor_name") + " " +record.optString("pledgor_birth"));
                item.setMortgagees(record.optString("pledgee_name"));

                reestrList.add(item);
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