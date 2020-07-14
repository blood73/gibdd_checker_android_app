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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import br.com.sapereaude.maskedEditText.MaskedEditText;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.database.HistoryDatabaseHelper;
import ru.bloodsoft.gibddchecker.models.Fssp;
import ru.bloodsoft.gibddchecker.models.FsspItem;
import ru.bloodsoft.gibddchecker.models.Regions;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;
import ru.bloodsoft.gibddchecker.ui.recycler_views.FsspItemRecyclerViewAdapter;
import ru.bloodsoft.gibddchecker.util.NewWebService;
import ru.bloodsoft.gibddchecker.util.RunCounts;
import ru.bloodsoft.gibddchecker.util.SanitizeHelper;
import ru.bloodsoft.gibddchecker.util.SettingsStorage;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logW;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class FsspActivity extends BaseActivity {

    String FsspRegion;
    String FsspLastname;
    String FsspFirstname;
    String FsspPatronymic;
    String FsspDob;

    public static final String ARG_REGION = "region";
    public static final String ARG_LASTNAME = "lastname";
    public static final String ARG_FIRSTNAME = "firstname";
    public static final String ARG_PATRONYMIC = "patronymic";
    public static final String ARG_DOB = "dob";

    public static final String URL_GET_FSSP ="https://api.fssprus.ru/api/v2/search?";
    ProgressDialog mProgressDialog;
    NewWebService getFsspRequest;
    GetFsspData getFsspData;
    Activity activity;

    private List<FsspItem> fsspList;
    private RecyclerView mRecyclerView;
    private FsspItemRecyclerViewAdapter fsspAdapter;
    InterstitialAd mInterstitialAd;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Snackbar mSnackbar;
    NestedScrollView mScrollView;

    private static final String TAG = makeLogTag(FsspActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fssp);

        activity = this;
        mScrollView = (NestedScrollView) findViewById(R.id.scrollView);

        Spinner fsspRegion = (Spinner) this.findViewById(R.id.fssp_region);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.fssp_regions_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        fsspRegion.setAdapter(adapter);

        final EditText fsspLastnameEditText = (EditText) this.findViewById(R.id.fssp_lastname);

        final EditText fsspFirstnameEditText = (EditText) this.findViewById(R.id.fssp_firstname);

        final EditText fsspPatronymicEditText = (EditText) this.findViewById(R.id.fssp_patronymic);

        final MaskedEditText fsspDobEditText = (MaskedEditText) findViewById(R.id.fssp_dob);

        Bundle b = activity.getIntent().getExtras();
        if (b != null) {
            String region = b.getString(ARG_REGION);
            if (region != null && !region.isEmpty()) {
                fsspRegion.setSelection(adapter.getPosition(region));
            }

            String lastName = b.getString(ARG_LASTNAME);
            if (lastName != null && !lastName.isEmpty()) {
                fsspLastnameEditText.setText(lastName);
            }

            String firstName = b.getString(ARG_FIRSTNAME);
            if (firstName != null && !firstName.isEmpty()) {
                fsspFirstnameEditText.setText(firstName);
            }

            String patronymic = b.getString(ARG_PATRONYMIC);
            if (patronymic != null && !patronymic.isEmpty()) {
                fsspPatronymicEditText.setText(patronymic);
            }

            String dob = b.getString(ARG_DOB);
            if (dob != null && !dob.isEmpty()) {
                fsspDobEditText.setText(dob);
            }
        }

        mRecyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view_fssp);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        fsspAdapter = new FsspItemRecyclerViewAdapter(activity, fsspList);
        mRecyclerView.setAdapter(fsspAdapter);

        RunCounts settingsRC = new RunCounts();
        Boolean isAdFree = settingsRC.isAdFree();

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
        final EditText plateEditText = (EditText) findViewById(R.id.fssp_lastname);
        ClipData clipData = null;
        try {
            ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipData = clipboard.getPrimaryClip();

            String plate = "";

            if (clipData != null) {
                ClipData.Item item = clipData.getItemAt(0);
                if (item.getText() != null) {
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

    @OnClick(R.id.paste2)
    public void onPaste2Clicked(View view) {
        final EditText plateEditText = (EditText) findViewById(R.id.fssp_firstname);
        ClipData clipData = null;
        try {
            ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipData = clipboard.getPrimaryClip();

            String plate = "";

            if (clipData != null) {
                ClipData.Item item = clipData.getItemAt(0);
                if (item.getText() != null) {
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

    @OnClick(R.id.paste3)
    public void onPaste3Clicked(View view) {
        final EditText plateEditText = (EditText) findViewById(R.id.fssp_patronymic);
        ClipData clipData = null;
        try {
            ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipData = clipboard.getPrimaryClip();

            String plate = "";

            if (clipData != null) {
                ClipData.Item item = clipData.getItemAt(0);
                if (item.getText() != null) {
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

    @OnClick(R.id.paste4)
    public void onPaste4Clicked(View view) {
        final EditText plateEditText = (EditText) findViewById(R.id.fssp_dob);
        ClipData clipData = null;
        try {
            ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipData = clipboard.getPrimaryClip();

            String plate = "";

            if (clipData != null) {
                ClipData.Item item = clipData.getItemAt(0);
                if (item.getText() != null) {
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

    private void showProgressDialog() {
        // instantiate it within the onCreate method
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(activity);
            mProgressDialog.setMessage(activity.getResources().getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }

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

        Spinner fsspRegion = (Spinner) this.findViewById(R.id.fssp_region);
        FsspRegion = fsspRegion.getSelectedItem().toString();

        final EditText fsspLastnameEditText = (EditText) this.findViewById(R.id.fssp_lastname);
        FsspLastname = fsspLastnameEditText.getText().toString();
        FsspLastname = SanitizeHelper.sanitizeString(FsspLastname);

        final EditText fsspFirstnameEditText = (EditText) this.findViewById(R.id.fssp_firstname);
        FsspFirstname = fsspFirstnameEditText.getText().toString();
        FsspFirstname = SanitizeHelper.sanitizeString(FsspFirstname);

        final EditText fsspPatronymicEditText = (EditText) this.findViewById(R.id.fssp_patronymic);
        FsspPatronymic = fsspPatronymicEditText.getText().toString();
        FsspPatronymic = SanitizeHelper.sanitizeString(FsspPatronymic);

        final MaskedEditText fsspDobEditText = (MaskedEditText) this.findViewById(R.id.fssp_dob);
        FsspDob = fsspDobEditText.getText().toString();

        if (isConnectedToInternet()) {
            if (!FsspRegion.trim().isEmpty() && !FsspLastname.trim().isEmpty() &&
                    !FsspFirstname.trim().isEmpty()) {

                ArrayList<String> passing = new ArrayList<String>();
                passing.add(FsspRegion);
                passing.add(FsspLastname.toUpperCase());
                passing.add(FsspFirstname.toUpperCase());
                passing.add(FsspPatronymic.toUpperCase());
                passing.add(FsspDob);

                //save to the history
                Fssp newFssp = new Fssp();
                newFssp.region = FsspRegion;
                newFssp.lastname = FsspLastname.toUpperCase();
                newFssp.firstname = FsspFirstname.toUpperCase();
                newFssp.patronymic = FsspPatronymic.toUpperCase();
                newFssp.dob = FsspDob;

                // Get singleton instance of database
                HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(activity);
                // Add to the database
                databaseHelper.addFssp(newFssp);

                RunCounts requestCounts = new RunCounts();
                requestCounts.increaseCheckAutoCount(activity);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "FSSP");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, FsspRegion + ";" + FsspLastname
                        + ";" + FsspFirstname + ";" + FsspPatronymic + ";" + FsspDob);
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "FSSP");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                getFsspData = new GetFsspData();
                try {
                    getFsspData.execute(passing);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mSnackbar = Snackbar.make(view, activity.getResources().getString(R.string.error_all_inputs_required), Snackbar.LENGTH_INDEFINITE)
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
        return R.id.nav_fssp;
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

    private class GetFsspData extends AsyncTask<ArrayList<String>, String, String> {

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
                region
                lastname
                firstname
                patronymic
                dob
             */

            Regions regionsList = new Regions();

            String region = (String) regionsList.getRegionIdByName(mStringArray[0].toString());
            String lastname = (String) mStringArray[1];
            String firstname = (String) mStringArray[2];
            String patronymic = (String) mStringArray[3];
            String dob = (String) mStringArray[4];

            logD(TAG, "region: " + region);
            logD(TAG, "lastname: " + lastname);
            logD(TAG, "firstname: " + firstname);
            logD(TAG, "patronymic: " + patronymic);
            logD(TAG, "dob: " + dob);

            try {
                region = URLEncoder.encode(region, "UTF-8");
                lastname = URLEncoder.encode(lastname, "UTF-8");
                firstname = URLEncoder.encode(firstname, "UTF-8");
                patronymic = URLEncoder.encode(patronymic, "UTF-8");
                if (!dob.equals("ДД.ММ.ГГГГ")) {
                    dob = URLEncoder.encode(dob, "UTF-8");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            String params = "region_id=" + region + "&last_name=" + lastname +
                    "&first_name=" + firstname + "&udid=&type=form&ver=28";

            if (!patronymic.isEmpty()) {
                params = params + "&patronymic=" + patronymic;
            }

            if (!dob.isEmpty() && !dob.equals("ДД.ММ.ГГГГ")) {
                params = params + "&date=" + dob;
            }

            logD(TAG, "params: " + params);

            String response = "";

            try {
                response = getFsspRequest.sendGet(URL_GET_FSSP + params);
            } catch (Exception e) {
                logW(TAG, "can't get fssp data");
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
            String error_color = "#CD5C5C";
            String no_error_color = "#9EF39B";
            Integer error_code = 0;
            String error_name = "";

            TextView textHeader = (TextView) activity.findViewById(R.id.details_header);
            TextView textData = (TextView) activity.findViewById(R.id.details_data);
            LinearLayout layout_result = (LinearLayout) activity.findViewById(R.id.layout_result);
            CardView card_results = (CardView) activity.findViewById(R.id.cardResults);

            mRecyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view_fssp);
            mRecyclerView.setVisibility(View.GONE);
            mRecyclerView.setNestedScrollingEnabled(false);

            try {
                responseJsonObject = new JSONObject(response);
                error_code = responseJsonObject.getInt("error_code");
                error_name = responseJsonObject.getString("error_name");
            } catch (JSONException e) {
                e.printStackTrace();
                card_results = (CardView) activity.findViewById(R.id.cardResults);
                card_results.setVisibility(View.VISIBLE);
                textHeader.setText(activity.getResources().getString(R.string.error));
                textData.setText(activity.getResources().getString(R.string.fssp_error_details));
                layout_result.setBackgroundColor(Color.parseColor(error_color));
            }

            try {
                JSONObject data = responseJsonObject.getJSONObject("data");
                JSONArray listArray = data.getJSONArray("list");
                parseFsspResult(listArray.toString());

                Integer numberFssp = listArray.length();

                if (error_code == 0) {
                    if (numberFssp == 0) {
                        card_results = (CardView) activity.findViewById(R.id.cardResults);
                        card_results.setVisibility(View.VISIBLE);
                        textHeader.setText(activity.getResources().getString(R.string.notice));
                        textData.setText(activity.getResources().getString(R.string.fssp_notice_not_found));
                        layout_result.setBackgroundColor(Color.parseColor(no_error_color));
                    } else {
                        card_results = (CardView) activity.findViewById(R.id.cardResults);
                        card_results.setVisibility(View.VISIBLE);
                        textHeader.setText(activity.getResources().getString(R.string.warning));
                        textData.setText(activity.getResources().getString(R.string.fssp_warning_found));
                        layout_result.setBackgroundColor(Color.parseColor(error_color));
                    }
                } else if (!error_name.isEmpty()){
                    card_results = (CardView) activity.findViewById(R.id.cardResults);
                    card_results.setVisibility(View.VISIBLE);
                    textHeader.setText(activity.getResources().getString(R.string.warning));
                    textData.setText(error_name);
                    layout_result.setBackgroundColor(Color.parseColor(error_color));
                }

                fsspAdapter = new FsspItemRecyclerViewAdapter(activity, fsspList);
                mRecyclerView.setVisibility(View.VISIBLE);
                mRecyclerView.setAdapter(fsspAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

            } catch (Exception e) {
                if (error_code == 0) {
                    card_results = (CardView) activity.findViewById(R.id.cardResults);
                    card_results.setVisibility(View.VISIBLE);
                    textHeader.setText(activity.getResources().getString(R.string.notice));
                    textData.setText(activity.getResources().getString(R.string.fssp_notice_not_found));
                    layout_result.setBackgroundColor(Color.parseColor(no_error_color));
                } else if (!error_name.isEmpty()){
                    card_results = (CardView) activity.findViewById(R.id.cardResults);
                    card_results.setVisibility(View.VISIBLE);
                    textHeader.setText(activity.getResources().getString(R.string.warning));
                    textData.setText(error_name);
                    layout_result.setBackgroundColor(Color.parseColor(error_color));
                }
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

    private void parseFsspResult(String result) {
        try {
            JSONArray records = new JSONArray(result);
            fsspList = new ArrayList<>();

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.optJSONObject(i);

                FsspItem item = new FsspItem();

                item.setName(record.optString("name"));
                item.setExeProduction(record.optString("exe_production"));
                item.setDetails(record.optString("details"));
                item.setSubject(record.optString("subject"));
                item.setDepartment(record.optString("department"));
                item.setBailiff(record.optString("bailiff"));

                fsspList.add(item);
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