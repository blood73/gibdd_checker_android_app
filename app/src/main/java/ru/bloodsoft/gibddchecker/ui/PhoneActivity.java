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
import br.com.sapereaude.maskedEditText.MaskedEditText;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.database.HistoryDatabaseHelper;
import ru.bloodsoft.gibddchecker.models.Phone;
import ru.bloodsoft.gibddchecker.models.PhoneItem;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;
import ru.bloodsoft.gibddchecker.ui.recycler_views.PhoneItemRecyclerViewAdapter;
import ru.bloodsoft.gibddchecker.util.NewWebService;
import ru.bloodsoft.gibddchecker.util.RunCounts;
import ru.bloodsoft.gibddchecker.util.SanitizeHelper;
import ru.bloodsoft.gibddchecker.util.SettingsStorage;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class PhoneActivity extends BaseActivity {

    String PhoneNumber;

    public static final String ARG_PHONE = "phone";

    private static final String URL_GET_PHONE = SanitizeHelper.decryptString("GsM7ShuIHsE0vEeI6xq4Ri0dWykvYVRP2j3PgJScIrryR+Yzqj9eIdey5FKzJUYo");
    ProgressDialog mProgressDialog;
    NewWebService getPhoneRequest;
    GetPhoneData getPhoneData;
    Activity activity;

    private List<PhoneItem> phoneList;
    private RecyclerView mRecyclerView;
    private PhoneItemRecyclerViewAdapter phoneAdapter;
    InterstitialAd mInterstitialAd;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Snackbar mSnackbar;
    NestedScrollView mScrollView;

    private static final String TAG = makeLogTag(PhoneActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        activity = this;
        final MaskedEditText phoneEditText = (MaskedEditText) findViewById(R.id.phone);
        PhoneNumber = phoneEditText.getRawText();

        mScrollView = (NestedScrollView) findViewById(R.id.scrollView);

        Bundle b = activity.getIntent().getExtras();
        if (b != null) {
            String phone = b.getString(ARG_PHONE);
            if (phone != null && !phone.isEmpty()) {
                phoneEditText.setText(phone);
            }
        }

        mRecyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view_phone);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        phoneAdapter = new PhoneItemRecyclerViewAdapter(activity, phoneList);
        mRecyclerView.setAdapter(phoneAdapter);

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

        final MaskedEditText phoneEditText = (MaskedEditText) findViewById(R.id.phone);
        PhoneNumber = phoneEditText.getRawText();

        if (isConnectedToInternet()) {
            if (!PhoneNumber.trim().isEmpty()) {

                ArrayList<String> passing = new ArrayList<String>();
                passing.add("7" + PhoneNumber);

                //save to the history
                Phone newPhone = new Phone();
                newPhone.phoneNumber = PhoneNumber;

                // Get singleton instance of database
                HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(activity);
                // Add phone to the database
                databaseHelper.addPhone(newPhone);

                RunCounts requestCounts = new RunCounts();
                requestCounts.increaseCheckAutoCount(activity);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "phone");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, PhoneNumber);
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "PHONE");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                getPhoneData = new GetPhoneData();
                getPhoneData.execute(passing);
            } else {
                mSnackbar = Snackbar.make(view, activity.getResources().getString(R.string.error_empty_phone), Snackbar.LENGTH_INDEFINITE)
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
        final MaskedEditText phoneEditText = (MaskedEditText) findViewById(R.id.phone);
        try {
            ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = clipboard.getPrimaryClip();
            String phone = "";

            if (clipData != null) {
                ClipData.Item item = clipData.getItemAt(0);
                if (item != null && item.getText() != null) {
                    phone = item.getText().toString();
                }
            }

            if (!phone.isEmpty()) {
                String firstSymbol = phone.substring(0, 1);

                if (firstSymbol.equals("8")) {
                    phone = phone.substring(1);
                }

                phone = phone.replace("+7", "");
                phone = phone.replace("+8", "");
                phone = phone.replace("(", "");
                phone = phone.replace(")", "");
                phone = phone.replace("-", "");
                phone = phone.replace(" ", "");
                phoneEditText.setText(phone);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.delete)
    public void onDeleteClicked(View view) {
        final MaskedEditText phoneEditText = (MaskedEditText) findViewById(R.id.phone);
        phoneEditText.setText("");
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
        return R.id.nav_phone;
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

    private class GetPhoneData extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
            CardView card_results = (CardView) activity.findViewById(R.id.cardResults);
            card_results.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(ArrayList<String>... passing) {

            ArrayList<String> passed = passing[0]; //get passed arraylist
            Object[] mStringArray = passed.toArray();

            /*
                phone
             */

            String phone = (String) mStringArray[0];
            String response = "";

            logD(TAG, "phone: " + phone);

            RequestBody formBody = new FormBody.Builder()
                    .add("phone", phone)
                    .build();

            try {
                response = getPhoneRequest.sendNewHttpsPost(URL_GET_PHONE, formBody);
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

            JSONObject responseJsonObject = null;
            JSONArray responseJsonArray = null;
            Integer totalCount = 0;
            String error_color = "#CD5C5C";
            String no_error_color = "#9EF39B";
            String warning_color = "#FFF380";

            TextView textHeader = (TextView) activity.findViewById(R.id.details_header);
            TextView textData = (TextView) activity.findViewById(R.id.details_data);
            LinearLayout layout_result = (LinearLayout) activity.findViewById(R.id.layout_result);
            CardView card_results = (CardView) activity.findViewById(R.id.cardResults);

            mRecyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view_phone);
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
                textData.setText(activity.getResources().getString(R.string.phone_error_details));
                layout_result.setBackgroundColor(Color.parseColor(error_color));
            }

            try {
                parsePhoneResult(responseJsonArray.toString());

                totalCount = responseJsonArray.length();

                if (totalCount == 0) {
                    card_results = (CardView) activity.findViewById(R.id.cardResults);
                    card_results.setVisibility(View.VISIBLE);
                    textHeader.setText(activity.getResources().getString(R.string.warning));
                    textData.setText(activity.getResources().getString(R.string.phone_warning_not_found));
                    layout_result.setBackgroundColor(Color.parseColor(no_error_color));
                } else {
                    card_results = (CardView) activity.findViewById(R.id.cardResults);
                    card_results.setVisibility(View.VISIBLE);
                    textHeader.setText(activity.getResources().getString(R.string.warning));
                    textData.setText(activity.getResources().getString(R.string.phone_warning_found, totalCount.toString()));
                    layout_result.setBackgroundColor(Color.parseColor(warning_color));
                }

                phoneAdapter = new PhoneItemRecyclerViewAdapter(activity, phoneList);
                mRecyclerView.setVisibility(View.VISIBLE);
                mRecyclerView.setAdapter(phoneAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

            } catch (Exception e) {
                card_results = (CardView) activity.findViewById(R.id.cardResults);
                card_results.setVisibility(View.VISIBLE);
                textHeader.setText(activity.getResources().getString(R.string.warning));
                textData.setText(activity.getResources().getString(R.string.phone_warning_not_found));
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

            scroolToResult();

        }
    }

    private void parsePhoneResult(String result) {
        try {
            JSONArray records = new JSONArray(result);
            phoneList = new ArrayList<>();

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.optJSONObject(i);

                PhoneItem item = new PhoneItem();

                item.setCarName(record.optString("car_name"));
                item.setDate(record.optLong("timestamp"));
                item.setRegion(record.optString("region"));
                item.setPrice(record.optString("price"));
                item.setMileage(record.optString("mileage"));
                item.setSource(record.optString("source"));
                item.setImageUrl(record.optString("image_url"));
                item.setYear(record.optString("year"));
                item.setDescription(record.optString("text"));

                phoneList.add(item);
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