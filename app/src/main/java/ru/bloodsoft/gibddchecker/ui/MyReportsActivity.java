package ru.bloodsoft.gibddchecker.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.google.firebase.analytics.FirebaseAnalytics;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.models.ReportItem;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;
import ru.bloodsoft.gibddchecker.ui.recycler_views.EmptyRecyclerView;
import ru.bloodsoft.gibddchecker.ui.recycler_views.ReportItemRecyclerViewAdapter;
import ru.bloodsoft.gibddchecker.util.NewWebService;
import ru.bloodsoft.gibddchecker.util.SanitizeHelper;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class MyReportsActivity extends BaseActivity {

    @BindView(R.id.reports_view)
    EmptyRecyclerView reportsRecyclerView;

    @BindView(R.id.empty_view)
    View emptyView;

    @BindView(R.id.swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;

    static {
        System.loadLibrary("native-lib");
    }

    public static native String getUrlGetReports();

    private ReportItemRecyclerViewAdapter reportAdapter;
    private List<ReportItem> reportList;

    ProgressDialog mProgressDialog;
    private Snackbar mSnackbar;

    private FirebaseAnalytics mFirebaseAnalytics;

    private static final String TAG = makeLogTag(MyReportsActivity.class);

    NewWebService getReportsRequest;
    GetReports getReports;

    private static final String URL_GET_REPORTS = SanitizeHelper.decryptString(getUrlGetReports());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reports);
        ButterKnife.bind(this);
        setupToolbar();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(MyReportsActivity.this);

        reportsRecyclerView.setLayoutManager(new LinearLayoutManager(MyReportsActivity.this));
        reportAdapter = new ReportItemRecyclerViewAdapter(MyReportsActivity.this, reportList);
        reportsRecyclerView.setAdapter(reportAdapter);
        reportsRecyclerView.setEmptyView(emptyView);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                updateReports();
            }
        });
        // Configure the refreshing colors
        swipeRefreshLayout.setColorSchemeResources(R.color.theme_primary_accent,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }

    @Override
    protected void onStart() {
        super.onStart();
        updateReports();
    }

    private void showProgressDialog() {
        // instantiate it within the onCreate method
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(MyReportsActivity.this);
            mProgressDialog.setMessage(MyReportsActivity.this.getResources().getString(R.string.loading));
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

    private void updateReports() {
        ArrayList<String> passing = new ArrayList<String>();
        getReports = new GetReports();
        getReports.execute(passing);
    }

    @Override
    public void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
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

    @OnClick(R.id.new_report)
    public void onNewReportClicked(View view) {
        Intent openFullReportActivity = new Intent(MyReportsActivity.this, FullReportActivity.class);
        MyReportsActivity.this.startActivity(openFullReportActivity);
    }
    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_my_reports;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }

    private class GetReports extends AsyncTask<ArrayList<String>, String, String> {

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
                vin
             */

            String response = "";

            RequestBody formBody = new FormBody.Builder()
                    .build();

            try {
                response = getReportsRequest.sendNewHttpsPost(URL_GET_REPORTS, formBody);
            } catch (Exception e) {
                e.printStackTrace();
            }

            logD(TAG, "Response: " + response);

            return response;
        }

        @Override
        protected void onPostExecute(String response) {

            if (MyReportsActivity.this.isFinishing()) {
                return;
            }

            dismissProgressDialog();
            swipeRefreshLayout.setRefreshing(false);

            JSONObject responseJsonObject = null;
            JSONObject responseJson = null;
            JSONArray responseJsonArray = null;
            Integer totalCount = 0;

            try {
                responseJsonObject = new JSONObject(response);
                responseJson = responseJsonObject.optJSONObject("data");
                responseJsonArray = responseJson.optJSONArray("reports");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                parseReportsResult(responseJsonArray.toString());

                totalCount = responseJsonArray.length();

                if (totalCount == 0) {
                    reportsRecyclerView.setVisibility(View.GONE);
                } else {
                    reportsRecyclerView.setVisibility(View.VISIBLE);
                }

                reportAdapter = new ReportItemRecyclerViewAdapter(MyReportsActivity.this, reportList);
                reportsRecyclerView.setAdapter(reportAdapter);
                reportsRecyclerView.setLayoutManager(new LinearLayoutManager(MyReportsActivity.this));
                reportsRecyclerView.setEmptyView(emptyView);

            } catch (Exception e) {
                reportsRecyclerView.setVisibility(View.GONE);
            }

        }
    }

    private void parseReportsResult(String result) {
        try {
            JSONArray records = new JSONArray(result);
            reportList = new ArrayList<>();

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.optJSONObject(i);

                ReportItem item = new ReportItem();

                item.setReportNumber(record.optString("report_number"));
                item.setVin(record.optString("vin"));
                item.setStatus(record.optString("status"));
                item.setAddedOn(record.optLong("added_on"));
                item.setUpdatedOn(record.optLong("updated_on"));

                reportList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }
}