package ru.bloodsoft.gibddchecker.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.support.design.widget.FloatingActionButton;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.database.HistoryDatabaseHelper;
import ru.bloodsoft.gibddchecker.models.Mileage;
import ru.bloodsoft.gibddchecker.models.MileageItem;
import ru.bloodsoft.gibddchecker.models.Vin;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;
import ru.bloodsoft.gibddchecker.ui.recycler_views.MileageItemRecyclerViewAdapter;
import ru.bloodsoft.gibddchecker.util.NewWebService;
import ru.bloodsoft.gibddchecker.util.RunCounts;
import ru.bloodsoft.gibddchecker.util.SanitizeHelper;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class MileageActivity extends BaseActivity {

    String vinNumber;

    static {
        System.loadLibrary("native-lib");
    }

    public static native String getMileageUrl();
    public static native String getCheckUserUrl();

    public static final String ARG_VIN = "vin";

    //https://antiperekup.net/api/v1/mileage/
    private static final String URL_GET_MILEAGE = SanitizeHelper.decryptString(getMileageUrl());
    //https://antiperekup.net/api/v1/check_user/
    private static final String URL_CHECK_USER = SanitizeHelper.decryptString(getCheckUserUrl());

    ProgressDialog mProgressDialog;
    NewWebService getMileageRequest;
    GetMileageData getMileageData;
    GetUserData getUserData;

    private List<MileageItem> mileageList;
    private RecyclerView mRecyclerView;
    private MileageItemRecyclerViewAdapter mileageAdapter;

    private FirebaseAnalytics mFirebaseAnalytics;
    private Snackbar mSnackbar;
    NestedScrollView mScrollView;

    int mileageCountNumber;

    @BindView(R.id.mileage_count_text)
    TextView mileageCountText;

    @BindView(R.id.mileage_count)
    CardView mileageCount;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    private static final String TAG = makeLogTag(MileageActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mileage);

        final EditText vinEditText = (EditText) findViewById(R.id.vinNumber);
        vinNumber = vinEditText.getText().toString();

        mScrollView = (NestedScrollView) findViewById(R.id.scrollView);

        Bundle b = MileageActivity.this.getIntent().getExtras();
        if (b != null) {
            String vin = b.getString(ARG_VIN);
            if (vin != null && !vin.isEmpty()) {
                vinEditText.setText(vin);
            }
        }

        mRecyclerView = (RecyclerView) MileageActivity.this.findViewById(R.id.recycler_view_mileage);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MileageActivity.this));
        mileageAdapter = new MileageItemRecyclerViewAdapter(MileageActivity.this, mileageList);
        mRecyclerView.setAdapter(mileageAdapter);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(MileageActivity.this);

        ButterKnife.bind(this);
        fab.setAlpha(0.5f);
        setupToolbar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateMileageCount();
    }

    private void updateMileageCount() {
        ArrayList<String> passing = new ArrayList<String>();
        getUserData = new GetUserData();
        getUserData.execute(passing);
    }

    private void showProgressDialog() {
        // instantiate it within the onCreate method
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(MileageActivity.this);
            mProgressDialog.setMessage(MileageActivity.this.getResources().getString(R.string.loading));
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

    @OnClick(R.id.mileage_count)
    public void onMileageInAppClicked(View view) {
        Intent intentInAppMileage = new Intent(MileageActivity.this, MileageInappActivity.class);
        MileageActivity.this.startActivity(intentInAppMileage);
    }

    @OnClick(R.id.update_mileage)
    public void onUpdateMileageClicked(View view) {
        updateMileageCount();
    }

    @OnClick(R.id.fab)
    public void onFabClicked(View view) {

        final EditText vinEditText = (EditText) findViewById(R.id.vinNumber);
        vinNumber = vinEditText.getText().toString();

        if (isConnectedToInternet()) {
            if (!vinNumber.trim().isEmpty()) {

                if (mileageCountNumber > 0) {
                    ArrayList<String> passing = new ArrayList<String>();
                    passing.add(vinNumber);

                    //save to the history
                    //save vin to the database
                    Vin newVin = new Vin();
                    newVin.vinText = vinNumber;
                    newVin.vinType = "mileage";

                    // Get singleton instance of database
                    HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(MileageActivity.this);
                    // Add vin to the database
                    databaseHelper.addVin(newVin);

                    RunCounts requestCounts = new RunCounts();
                    requestCounts.increaseCheckAutoCount(MileageActivity.this);

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mileage");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, vinNumber);
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "VIN");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    getMileageData = new GetMileageData();
                    getMileageData.execute(passing);
                } else {
                    mSnackbar = Snackbar.make(view, MileageActivity.this.getResources().getString(R.string.error_no_inapp_mileage), Snackbar.LENGTH_LONG)
                            .setAction(MileageActivity.this.getResources().getString(R.string.pay), snackbarOnClickListener)
                            .setDuration(5000);
                    mSnackbar.show();
                    //Hide keyboard
                    InputMethodManager imm = (InputMethodManager) MileageActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            } else {
                mSnackbar = Snackbar.make(view, MileageActivity.this.getResources().getString(R.string.error_empty_vin), Snackbar.LENGTH_INDEFINITE)
                        .setAction("Action", null)
                        .setDuration(2000);
                mSnackbar.show();
                //Hide keyboard
                InputMethodManager imm = (InputMethodManager) MileageActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } else {
            mSnackbar = Snackbar.make(view, MileageActivity.this.getResources().getString(R.string.error_no_internet), Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null)
                    .setDuration(2000);
            mSnackbar.show();
            //Hide keyboard
            InputMethodManager imm = (InputMethodManager) MileageActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    View.OnClickListener snackbarOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent openInAppMileageActivity = new Intent(MileageActivity.this, MileageInappActivity.class);
            MileageActivity.this.startActivity(openInAppMileageActivity);
        }
    };

    @OnClick(R.id.paste)
    public void onPasteClicked(View view) {
        final EditText vinEditText = (EditText) MileageActivity.this.findViewById(R.id.vinNumber);
        try {
            ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = clipboard.getPrimaryClip();
            String vin = "";

            if (clipData != null) {
                ClipData.Item item = clipData.getItemAt(0);
                if (item != null && item.getText() != null) {
                    vin = item.getText().toString();
                }
            }

            if (!vin.isEmpty()) {
                vin = SanitizeHelper.sanitizeString(vin);
                vinEditText.setText(vin);
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
        return R.id.nav_mileage;
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

    private class GetMileageData extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
            CardView card_results = (CardView) MileageActivity.this.findViewById(R.id.cardResults);
            card_results.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(ArrayList<String>... passing) {

            ArrayList<String> passed = passing[0]; //get passed arraylist
            Object[] mStringArray = passed.toArray();

            /*
                vin
             */

            String vin = (String) mStringArray[0];

            logD(TAG, "vin: " + vin);

            String response = "";

            RequestBody formBody = new FormBody.Builder()
                    .add("vin", vin)
                    .build();

            try {
                response = getMileageRequest.sendNewHttpsPost(URL_GET_MILEAGE, formBody);
            } catch (Exception e) {
                e.printStackTrace();
            }

            logD(TAG, "Response: " + response);

            return response;
        }

        @Override
        protected void onPostExecute(String response) {

            if (MileageActivity.this != null && MileageActivity.this.isFinishing()) {
                return;
            }

            dismissProgressDialog();

            JSONObject responseJsonObject = null;
            JSONArray mileageJsonArray = null;
            String error_color = "#CD5C5C";
            String no_error_color = "#9EF39B";

            TextView textHeader = (TextView) MileageActivity.this.findViewById(R.id.details_header);
            TextView textData = (TextView) MileageActivity.this.findViewById(R.id.details_data);
            LinearLayout layout_result = (LinearLayout) MileageActivity.this.findViewById(R.id.layout_result);
            CardView card_results = (CardView) MileageActivity.this.findViewById(R.id.cardResults);

            mRecyclerView = (RecyclerView) MileageActivity.this.findViewById(R.id.recycler_view_mileage);
            mRecyclerView.setVisibility(View.GONE);
            mRecyclerView.setNestedScrollingEnabled(false);

            try {
                responseJsonObject = new JSONObject(response);
            } catch (JSONException e) {
                e.printStackTrace();
                card_results = (CardView) MileageActivity.this.findViewById(R.id.cardResults);
                card_results.setVisibility(View.VISIBLE);
                textHeader.setText(MileageActivity.this.getResources().getString(R.string.error));
                textData.setText(MileageActivity.this.getResources().getString(R.string.mileage_error_details));
                layout_result.setBackgroundColor(Color.parseColor(error_color));
            }

            try {
                mileageJsonArray = responseJsonObject.optJSONArray("data");

                Mileage newMileage = parseMileageResult(mileageJsonArray.toString());

                mileageAdapter = new MileageItemRecyclerViewAdapter(MileageActivity.this, mileageList);
                mRecyclerView.setVisibility(View.VISIBLE);
                mRecyclerView.setAdapter(mileageAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(MileageActivity.this));

                if (mileageList.size() != 0) {
                    // Get singleton instance of database
                    HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(MileageActivity.this);

                    // Add mileage to the database
                    databaseHelper.addMileage(newMileage);

                    card_results = (CardView) MileageActivity.this.findViewById(R.id.cardResults);
                    card_results.setVisibility(View.VISIBLE);
                    textHeader.setText(MileageActivity.this.getResources().getString(R.string.mileage_success));
                    textData.setText("");
                    layout_result.setBackgroundColor(Color.parseColor(no_error_color));
                } else {
                    card_results = (CardView) MileageActivity.this.findViewById(R.id.cardResults);
                    card_results.setVisibility(View.VISIBLE);
                    textHeader.setText(MileageActivity.this.getResources().getString(R.string.warning));
                    textData.setText(MileageActivity.this.getResources().getString(R.string.mileage_warning_not_found));
                    layout_result.setBackgroundColor(Color.parseColor(error_color));
                }

            } catch (Exception e) {
                card_results = (CardView) MileageActivity.this.findViewById(R.id.cardResults);
                card_results.setVisibility(View.VISIBLE);
                textHeader.setText(MileageActivity.this.getResources().getString(R.string.warning));
                textData.setText(MileageActivity.this.getResources().getString(R.string.mileage_warning_not_found));
                layout_result.setBackgroundColor(Color.parseColor(error_color));
            }

            //Hide keyboard
            final NestedScrollView scrollView = (NestedScrollView) MileageActivity.this.findViewById(R.id.scrollView);
            InputMethodManager imm = (InputMethodManager) MileageActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(scrollView.getWindowToken(), 0);

            scroolToResult();
            updateMileageCount();

        }
    }

    private class GetUserData extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
            mileageCountText.setText(MileageActivity.this.getResources().getString(R.string.updating));
        }

        @Override
        protected String doInBackground(ArrayList<String>... passing) {

            ArrayList<String> passed = passing[0]; //get passed arraylist
            Object[] mStringArray = passed.toArray();

            /*
                vin
             */

            String response = "";

            RunCounts settings = new RunCounts();
            RequestBody formBody = new FormBody.Builder()
                    .add("ssad", settings.getSSAD())
                    .build();

            try {
                response = getMileageRequest.sendNewHttpsPost(URL_CHECK_USER, formBody);
            } catch (Exception e) {
                e.printStackTrace();
            }

            logD(TAG, "Response: " + response);

            return response;
        }

        @Override
        protected void onPostExecute(String response) {

            if (MileageActivity.this.isFinishing()) {
                return;
            }

            dismissProgressDialog();

            JSONObject responseJsonObject = null;
            JSONObject responseJson = null;

            try {
                responseJsonObject = new JSONObject(response);
                responseJson = responseJsonObject.optJSONObject("data");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                mileageCountNumber = responseJson.optInt("mileage_balance");

                HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(MileageActivity.this);
                databaseHelper.updateMileageCount(mileageCountNumber);

                String inAppCount = MileageActivity.this.getResources().getQuantityString(R.plurals.mileage_inapp_count,
                        mileageCountNumber, mileageCountNumber);

                mileageCountText.setText(inAppCount);
                if (mileageCountNumber == 0) {
                    mileageCountText.setText(MileageActivity.this.getResources().getString(R.string.no_mileage_requests_buy));
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                mileageCountText.setText(MileageActivity.this.getResources().getString(R.string.no_mileage_requests_buy));
            }

        }
    }

    private Mileage parseMileageResult(String result) {
        Mileage newMileage = new Mileage();
        try {
            JSONArray records = new JSONArray(result);
            mileageList = new ArrayList<>();

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.optJSONObject(i);

                MileageItem item = new MileageItem();

                item.setTimestamp(record.optLong("timestamp"));
                item.setSource(record.optString("source"));
                item.setCity(record.optString("city"));
                item.setDistance(record.optInt("mileage"));
                item.setPrice(record.optInt("price"));

                mileageList.add(item);

                //save to the history
                newMileage.vin = vinNumber;
                newMileage.mileage = Integer.toString(record.optInt("mileage"));
                newMileage.date = getDate(record.optLong("timestamp"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return newMileage;
    }

    public String getDate(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp * 1000);
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);
        }catch (Exception e) {
        }
        return "";
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