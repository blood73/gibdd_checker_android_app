package ru.bloodsoft.gibddchecker.ui.quote;

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
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.database.HistoryDatabaseHelper;
import ru.bloodsoft.gibddchecker.models.AccidentItem;
import ru.bloodsoft.gibddchecker.models.ApiResponse;
import ru.bloodsoft.gibddchecker.models.GibddContent;
import ru.bloodsoft.gibddchecker.models.HistoryItem;
import ru.bloodsoft.gibddchecker.models.RestrictedItem;
import ru.bloodsoft.gibddchecker.models.VehicleTypes;
import ru.bloodsoft.gibddchecker.models.Vin;
import ru.bloodsoft.gibddchecker.models.VinSearchType;
import ru.bloodsoft.gibddchecker.models.WantedItem;
import ru.bloodsoft.gibddchecker.ui.FullReportActivity;
import ru.bloodsoft.gibddchecker.ui.MileageActivity;
import ru.bloodsoft.gibddchecker.ui.ReestrActivity;
import ru.bloodsoft.gibddchecker.ui.SettingsActivity;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;
import ru.bloodsoft.gibddchecker.ui.base.BaseFragment;
import ru.bloodsoft.gibddchecker.ui.recycler_views.AccidentItemRecyclerViewAdapter;
import ru.bloodsoft.gibddchecker.ui.recycler_views.HistoryItemRecyclerViewAdapter;
import ru.bloodsoft.gibddchecker.ui.recycler_views.RestrictedItemRecyclerViewAdapter;
import ru.bloodsoft.gibddchecker.ui.recycler_views.WantedItemRecyclerViewAdapter;
import ru.bloodsoft.gibddchecker.util.NewWebService;
import ru.bloodsoft.gibddchecker.util.RunCounts;
import ru.bloodsoft.gibddchecker.util.SanitizeHelper;
import ru.bloodsoft.gibddchecker.util.SettingsStorage;
import ru.bloodsoft.gibddchecker.util.UpdateVehicleAsyncTask;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

/**
 * Shows the quote detail page.
 *
 */
public class ArticleDetailFragment extends BaseFragment {

    static {
        System.loadLibrary("native-lib");
    }

    public static native String getUrlGetGibddData();

    /**
     * The argument represents the dummy item ID of this fragment.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_VIN = "vin";

    public static final String URL_GET_GIBDD ="https://xn--90adear.xn--p1ai/check/auto";


    private static final String URL_GET_GIBDD_DATA = SanitizeHelper.decryptString(getUrlGetGibddData());

    private static final String TAG = makeLogTag(ArticleDetailFragment.class);
    InterstitialAd mInterstitialAd;
    private FirebaseAnalytics mFirebaseAnalytics;
    NestedScrollView mScrollView;

    /**
     * The dummy content of this fragment.
     */
    private GibddContent.DummyItem dummyItem;

    String ActiveItemId;
    String VinNumber;

    @BindView(R.id.quote)
    TextView quote;

    @BindView(R.id.author)
    TextView author;

    @BindView(R.id.backdrop)
    ImageView backdropImg;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;

    @BindView(R.id.captcha_webview)
    WebView captchaWebView;

    @BindView(R.id.progress_layout)
    View progressView;

    DownloadReCaptcha downloadCaptcha;
    GetGibddData getGibddData;

    NewWebService getGibddRequest;

    private int requestApiCount = 0;
    private static final int MAX_REQUEST_API_COUNT = 1;
    private AutoinsWebViewClient autoinsWebViewClient;

    ProgressDialog mProgressDialog;
    private Snackbar mSnackbar;

    private List<HistoryItem> historyList;
    private RecyclerView mRecyclerView1;
    private HistoryItemRecyclerViewAdapter historyAdapter;

    private List<AccidentItem> accidentList;
    List<String> damageList = new ArrayList<String>();
    private RecyclerView mRecyclerView2;
    private AccidentItemRecyclerViewAdapter accidentAdapter;

    private List<WantedItem> wantedsList;
    private RecyclerView mRecyclerView3;
    private WantedItemRecyclerViewAdapter wantedAdapter;

    private List<RestrictedItem> restrictedList;
    private RecyclerView mRecyclerView4;
    private RestrictedItemRecyclerViewAdapter restrictedAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // load dummy item by using the passed item ID.
            dummyItem = GibddContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
            ActiveItemId = getArguments().getString(ARG_ITEM_ID).toString();
        }

        setHasOptionsMenu(true);
        getGibddRequest = new NewWebService();

        mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId("ca-app-pub-3078563819949367/4695892338");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        requestNewInterstitial();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("E31198FB67C1181AF2CFCEDF4476A9D1")
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    private void showProgressDialog() {
        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getActivity().getResources().getString(R.string.loading));
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflateAndBind(inflater, container, R.layout.fragment_article_detail);

        if (!((BaseActivity) getActivity()).providesActivityToolbar()) {
            // No Toolbar present. Set include_toolbar:
            ((BaseActivity) getActivity()).setToolbar((Toolbar) rootView.findViewById(R.id.toolbar));
        }

        if (dummyItem != null) {
            loadBackdrop();
            collapsingToolbar.setTitle("");
            author.setText(dummyItem.title);
            quote.setText(dummyItem.content);
        }

        final EditText vinNumberEditText = (EditText) rootView.findViewById(R.id.vinNumber);

        Bundle b = getActivity().getIntent().getExtras();
        if (b != null) {
            String vin = b.getString(ARG_VIN);
            if (vin != null && !vin.isEmpty()) {
                vinNumberEditText.setText(vin);
            }
        }

        mRecyclerView1 = (RecyclerView) rootView.findViewById(R.id.recycler_view_1);
        mRecyclerView1.setLayoutManager(new LinearLayoutManager(getActivity()));
        historyAdapter = new HistoryItemRecyclerViewAdapter(getActivity(), historyList);
        mRecyclerView1.setAdapter(historyAdapter);

        mRecyclerView2 = (RecyclerView) rootView.findViewById(R.id.recycler_view_2);
        mRecyclerView2.setLayoutManager(new LinearLayoutManager(getActivity()));
        accidentAdapter = new AccidentItemRecyclerViewAdapter(getActivity(), accidentList);
        mRecyclerView2.setAdapter(accidentAdapter);

        mRecyclerView3 = (RecyclerView) rootView.findViewById(R.id.recycler_view_3);
        mRecyclerView3.setLayoutManager(new LinearLayoutManager(getActivity()));
        wantedAdapter = new WantedItemRecyclerViewAdapter(getActivity(), wantedsList);
        mRecyclerView3.setAdapter(wantedAdapter);

        mRecyclerView4 = (RecyclerView) rootView.findViewById(R.id.recycler_view_4);
        mRecyclerView4.setLayoutManager(new LinearLayoutManager(getActivity()));
        restrictedAdapter = new RestrictedItemRecyclerViewAdapter(getActivity(), restrictedList);
        mRecyclerView4.setAdapter(restrictedAdapter);
        mScrollView = (NestedScrollView) rootView.findViewById(R.id.scrollView);

        RunCounts settings = new RunCounts();
        Boolean isAdFree = settings.isAdFree();

        if (!isAdFree) {
            AdView adView = new AdView(getActivity());

            DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
            float dpWidth = displayMetrics.widthPixels / displayMetrics.density - 40;
            adView.setAdSize(new AdSize((int) dpWidth, 300));
            adView.setAdUnitId("ca-app-pub-3078563819949367/5966252215");

            CardView card_adview = (CardView) rootView.findViewById(R.id.cardAdView);
            card_adview.addView(adView);

            AdRequest request = new AdRequest.Builder()
                    .addTestDevice("E31198FB67C1181AF2CFCEDF4476A9D1")
                    .build();
            adView.loadAd(request);

            AdView adView2 = new AdView(getActivity());

            adView2.setAdSize(new AdSize((int) dpWidth, 80));
            adView2.setAdUnitId("ca-app-pub-3078563819949367/1780251731");

            CardView card_adview_small = (CardView) rootView.findViewById(R.id.cardAdViewSmall);
            card_adview_small.addView(adView2);

            AdRequest request2 = new AdRequest.Builder()
                    .addTestDevice("E31198FB67C1181AF2CFCEDF4476A9D1")
                    .build();
            adView2.loadAd(request2);
        }

        // Adding Floating Action Button to bottom right of main view
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                VinNumber = vinNumberEditText.getText().toString();
                if (isConnectedToInternet()) {
                    if (VinNumber != null && !VinNumber.trim().isEmpty()) {
                        //save vin to the database
                        Vin newVin = new Vin();
                        newVin.vinText = VinNumber;
                        newVin.vinType = "history";

                        switch (ActiveItemId) {
                            case "1":
                                newVin.vinType = "history";
                                break;
                            case "2":
                                newVin.vinType = "aiusdtp";
                                break;
                            case "3":
                                newVin.vinType = "wanted";
                                break;
                            case "4":
                                newVin.vinType = "restricted";
                                break;
                            default:
                                newVin.vinType = "history";
                                break;
                        }

                        if (newVin.vinType.isEmpty()) {
                            newVin.vinType = "history";
                        }

                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, newVin.vinType);
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, VinNumber);
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "VIN");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        RunCounts requestCounts = new RunCounts();
                        requestCounts.increaseCheckAutoCount(getActivity());

                        // Get singleton instance of database
                        HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(getActivity());
                        // Add vin to the database
                        databaseHelper.addVin(newVin);

                        downloadCaptcha = new DownloadReCaptcha();
                        try {
                            downloadCaptcha.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        mSnackbar = Snackbar.make(getView(), getActivity().getResources().getString(R.string.error_empty_vin), Snackbar.LENGTH_INDEFINITE)
                                .setAction("Action", null)
                                .setDuration(2000);
                        mSnackbar.show();
                        //Hide keyboard
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                    }
                } else {
                    mSnackbar = Snackbar.make(getView(), getActivity().getResources().getString(R.string.error_no_internet), Snackbar.LENGTH_INDEFINITE)
                            .setAction("Action", null)
                            .setDuration(2000);
                    mSnackbar.show();
                    //Hide keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                }
            }
        });

        return rootView;
    }

    @OnClick(R.id.paste)
    public void onPasteClicked(View view) {
        final EditText vinEditText = (EditText) getActivity().findViewById(R.id.vinNumber);
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

    private void loadBackdrop() {
        Glide.with(this)
                .load(dummyItem.photoId)
                .apply(new RequestOptions()
                        .centerCrop())
                .into(backdropImg);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sample_actions, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static ArticleDetailFragment newInstance(String itemID) {
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        Bundle args = new Bundle();
        args.putString(ArticleDetailFragment.ARG_ITEM_ID, itemID);
        fragment.setArguments(args);
        return fragment;
    }

    public boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    public ArticleDetailFragment() {}

    private class GetGibddData extends AsyncTask<ArrayList<String>, ApiResponse, ApiResponse> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected ApiResponse doInBackground(ArrayList<String>... passing) {

            ArrayList<String> passed = passing[0]; //get passed arraylist
            Object[] mStringArray = passed.toArray();

            /* url, captcha, checktype, vin
             captchaWord
             checkType
             vin
            */

            String urlParam = (String) mStringArray[0];
            String captcha = (String) mStringArray[1];
            String checktype = (String) mStringArray[2];
            String vin = (String) mStringArray[3];

            logD(TAG, "captcha: " + captcha);
            logD(TAG, "checktype: " + checktype);
            logD(TAG, "VIN: " + vin);

            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setResponse("");
            apiResponse.setSuccess(false);

            if (requestApiCount < MAX_REQUEST_API_COUNT) {
                requestApiCount++;
                RequestBody formBody = new FormBody.Builder()
                        .add("captchaWord", "")
                        .add("checktype", checktype)
                        .add("vin", vin)
                        .add("reCaptchaToken", captcha)
                        .build();

                try {
                    apiResponse.setResponse(getGibddRequest.sendPostWithCookies(urlParam, formBody));
                    apiResponse.setSuccess(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            logD(TAG, "Response: " + apiResponse.getResponse());

            return apiResponse;
        }

        @Override
        protected void onPostExecute(ApiResponse apiResponse) {
            String response = "";

            if (getActivity() != null && getActivity().isFinishing()) {
                return;
            }

            if (getActivity() == null) {
                return;
            }

            dismissProgressDialog();

            captchaWebView.setVisibility(View.GONE);

            if (!apiResponse.isSuccess()) {
                return;
            } else {
                response = apiResponse.getResponse();
            }

            String error_color = "#CD5C5C";
            String no_error_color = "#9EF39B";

            TextView textHeader = (TextView) getActivity().findViewById(R.id.details_header);
            TextView textData = (TextView) getActivity().findViewById(R.id.details_data);

            String status = "0";
            JSONObject responseJsonObject = null;

            LinearLayout layout_result = (LinearLayout) getActivity().findViewById(R.id.layout_result);

            mRecyclerView1 = (RecyclerView) getView().findViewById(R.id.recycler_view_1);
            mRecyclerView1.setVisibility(View.GONE);
            mRecyclerView1.setNestedScrollingEnabled(false);

            mRecyclerView2 = (RecyclerView) getView().findViewById(R.id.recycler_view_2);
            mRecyclerView2.setVisibility(View.GONE);
            mRecyclerView2.setNestedScrollingEnabled(false);

            mRecyclerView3 = (RecyclerView) getView().findViewById(R.id.recycler_view_3);
            mRecyclerView3.setVisibility(View.GONE);
            mRecyclerView3.setNestedScrollingEnabled(false);

            mRecyclerView4 = (RecyclerView) getView().findViewById(R.id.recycler_view_4);
            mRecyclerView4.setVisibility(View.GONE);
            mRecyclerView4.setNestedScrollingEnabled(false);

            try {
                responseJsonObject = new JSONObject(response);
                status = responseJsonObject.getString("status");
            } catch (JSONException e) {
                e.printStackTrace();
                CardView card_results = (CardView) getActivity().findViewById(R.id.cardResults);
                card_results.setVisibility(View.VISIBLE);
                textHeader.setText(getActivity().getResources().getString(R.string.error));
                textData.setText(getActivity().getResources().getString(R.string.error_details));
                layout_result.setBackgroundColor(Color.parseColor(error_color));
            }

            logD(TAG, "Status: " + status);

            if (!status.equals("0") && !status.equals("200")) {
                //Bad request
                try {
                    responseJsonObject = new JSONObject(response);
                    String message = responseJsonObject.getString("message");
                    CardView card_results = (CardView) getActivity().findViewById(R.id.cardResults);
                    card_results.setVisibility(View.VISIBLE);
                    textHeader.setText(getActivity().getResources().getString(R.string.error));

                    if (status.equals("404")) {
                        logD(TAG, "Status: 404");
                        message = getActivity().getResources().getString(R.string.error_404);
                    } else if (status.equals("403")) {
                        logD(TAG, "Status: 403");
                        message = getActivity().getResources().getString(R.string.error_403);
                    }

                    textData.setText(message);
                    layout_result.setBackgroundColor(Color.parseColor(error_color));
                } catch (JSONException e) {
                    e.printStackTrace();
                    CardView card_results = (CardView) getActivity().findViewById(R.id.cardResults);
                    card_results.setVisibility(View.VISIBLE);
                    textHeader.setText(getActivity().getResources().getString(R.string.error));
                    textData.setText(getActivity().getResources().getString(R.string.error_details));
                    layout_result.setBackgroundColor(Color.parseColor(error_color));
                }
            } else if (status.equals("200")) {
                //Good request
                switch (ActiveItemId) {
                    case "1":
                        //vehicle history
                        try {
                            JSONObject RequestResult = responseJsonObject.getJSONObject("RequestResult");
                            JSONObject RequestResult2 = RequestResult.getJSONObject("ownershipPeriods");
                            parseHistoryResult(RequestResult2.toString());

                            historyAdapter = new HistoryItemRecyclerViewAdapter(getActivity(), historyList);
                            mRecyclerView1.setVisibility(View.VISIBLE);
                            mRecyclerView1.setAdapter(historyAdapter);
                            mRecyclerView1.setLayoutManager(new LinearLayoutManager(getActivity()));

                        } catch (JSONException e) {
                            CardView card_results = (CardView) getActivity().findViewById(R.id.cardResults);
                            card_results.setVisibility(View.VISIBLE);
                            textHeader.setText(getActivity().getResources().getString(R.string.warning));
                            textData.setText(getActivity().getResources().getString(R.string.warning_not_found_1));
                            layout_result.setBackgroundColor(Color.parseColor(error_color));
                        }

                        //vehicle passport
                        try {
                            CardView cardVehilePassport = (CardView) getActivity().findViewById(R.id.cardVehiclePassport);
                            cardVehilePassport.setVisibility(View.VISIBLE);

                            JSONObject RequestResult = responseJsonObject.getJSONObject("RequestResult");
                            JSONObject RequestResult2 = RequestResult.getJSONObject("vehiclePassport");
                            String passportNumber = RequestResult2.optString("number");
                            String passportIssue = RequestResult2.optString("issue");

                            TextView headerVehiclePassport = (TextView) getActivity().findViewById(R.id.headerVehiclePassport);
                            TextView numberVehiclePassport = (TextView) getActivity().findViewById(R.id.numberVehiclePassport);
                            TextView issueVehiclePassport = (TextView) getActivity().findViewById(R.id.issueVehiclePassport);

                            headerVehiclePassport.setText(getActivity().getResources().getString(R.string.vehicle_passport));
                            numberVehiclePassport.setText(passportNumber);
                            issueVehiclePassport.setText(passportIssue);

                        } catch (JSONException e) {
                            CardView cardVehilePassport = (CardView) getActivity().findViewById(R.id.cardVehiclePassport);
                            cardVehilePassport.setVisibility(View.GONE);
                        }

                        //vehicle details
                        try {
                            CardView cardVehileDetails = (CardView) getActivity().findViewById(R.id.cardVehicleDetails);
                            cardVehileDetails.setVisibility(View.VISIBLE);

                            JSONObject RequestResult = responseJsonObject.getJSONObject("RequestResult");
                            JSONObject RequestResult2 = RequestResult.getJSONObject("vehicle");
                            String vehicleModel = RequestResult2.optString("model");
                            String vehicleYear = RequestResult2.optString("year");
                            String vehicleVin = RequestResult2.optString("vin");
                            String vehicleBodyNumber = RequestResult2.optString("bodyNumber");
                            String vehicleChassisNumber = RequestResult2.optString("chassisNumber");
                            String vehicleColor = RequestResult2.optString("color");
                            String vehicleEngineVolume = RequestResult2.optString("engineVolume");
                            String vehicleEnginePowerKwt = RequestResult2.optString("powerKwt");
                            String vehicleEnginePowerHp = RequestResult2.optString("powerHp");
                            String vehiclePower = vehicleEnginePowerKwt + "/" + vehicleEnginePowerHp;
                            String vehicleCategory = RequestResult2.optString("category");

                            VehicleTypes vehicleType = new VehicleTypes();
                            String vehicleTypeString = vehicleType.getVehicleType(RequestResult2.optString("type"));

                            TextView headerVehicleDetails = (TextView) getActivity().findViewById(R.id.headerVehicleDetails);
                            TextView textVehicleModel = (TextView) getActivity().findViewById(R.id.vehicleModel);
                            TextView textVehicleYear = (TextView) getActivity().findViewById(R.id.vehicleYear);
                            TextView textVehicleVin = (TextView) getActivity().findViewById(R.id.vehicleVin);
                            TextView textVehicleBodyNumber = (TextView) getActivity().findViewById(R.id.vehicleBodyNumber);
                            TextView textVehicleChassisNumber = (TextView) getActivity().findViewById(R.id.vehicleChassisNumber);
                            TextView textVehicleColor = (TextView) getActivity().findViewById(R.id.vehicleColor);
                            TextView textVehicleEngineVolume = (TextView) getActivity().findViewById(R.id.vehicleEngineVolume);
                            TextView textVehicleEnginePower = (TextView) getActivity().findViewById(R.id.vehicleEnginePower);
                            TextView textVehicleCategory = (TextView) getActivity().findViewById(R.id.vehicleCategory);
                            TextView textVehicleType = (TextView) getActivity().findViewById(R.id.vehicleType);

                            headerVehicleDetails.setText(getActivity().getResources().getString(R.string.vehicle_details));
                            textVehicleModel.setText(vehicleModel);
                            textVehicleYear.setText(vehicleYear);
                            textVehicleVin.setText(vehicleVin);
                            textVehicleBodyNumber.setText(vehicleBodyNumber);
                            textVehicleChassisNumber.setText(vehicleChassisNumber);
                            textVehicleColor.setText(vehicleColor);
                            textVehicleEngineVolume.setText(vehicleEngineVolume);
                            textVehicleEnginePower.setText(vehiclePower);
                            textVehicleCategory.setText(vehicleCategory);
                            textVehicleType.setText(vehicleTypeString);

                            if (!vehicleYear.isEmpty()) {
                                ArrayList<String> passing = new ArrayList<String>();
                                passing.add(VinNumber);
                                passing.add(response);

                                UpdateVehicleAsyncTask updateVehicleAsyncTask = new UpdateVehicleAsyncTask();
                                updateVehicleAsyncTask.execute(passing);
                            }

                        } catch (JSONException e) {
                            CardView cardVehilePassport = (CardView) getActivity().findViewById(R.id.cardVehicleDetails);
                            cardVehilePassport.setVisibility(View.GONE);
                        }

                        CardView card_next_check_dtp = (CardView) getActivity().findViewById(R.id.cardNextDtp);
                        card_next_check_dtp.setVisibility(View.VISIBLE);
                        String next_card_color = "#e6e6e6";
                        card_next_check_dtp.setCardBackgroundColor(Color.parseColor(next_card_color));

                        View.OnClickListener card_next_check_dtp_clk_lstr = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Start the detail activity
                                VinSearchType searchType = new VinSearchType();
                                Integer searchVinInt = searchType.getSearchPosition("aiusdtp");

                                Intent detailIntent = new Intent(getActivity(), ArticleDetailActivity.class);
                                detailIntent.putExtra(ArticleDetailFragment.ARG_ITEM_ID, searchVinInt.toString());
                                detailIntent.putExtra(ArticleDetailFragment.ARG_VIN, VinNumber);
                                startActivity(detailIntent);
                            }
                        };

                        card_next_check_dtp.setOnClickListener(card_next_check_dtp_clk_lstr);

                        break;
                    case "2":
                        //accidents
                        try {
                            JSONObject RequestResult = responseJsonObject.getJSONObject("RequestResult");
                            parseAccidentResult(RequestResult.toString());

                            JSONArray records = RequestResult.optJSONArray("Accidents");
                            Integer numberAccidents = records.length();

                            CardView card_results = (CardView) getActivity().findViewById(R.id.cardResults);
                            card_results.setVisibility(View.VISIBLE);

                            if (numberAccidents >= 1) {
                                textHeader.setText(getActivity().getResources().getString(R.string.warning));
                                textData.setText(getActivity().getResources().getString(R.string.warning_found_2, numberAccidents.toString()));
                                layout_result.setBackgroundColor(Color.parseColor(error_color));
                                accidentAdapter = new AccidentItemRecyclerViewAdapter(getActivity(), accidentList);
                                mRecyclerView2.setVisibility(View.VISIBLE);
                                mRecyclerView2.setAdapter(accidentAdapter);
                                mRecyclerView2.setLayoutManager(new LinearLayoutManager(getActivity()));
                            } else {
                                textHeader.setText(getActivity().getResources().getString(R.string.notice));
                                textData.setText(getActivity().getResources().getString(R.string.notice_2));
                                layout_result.setBackgroundColor(Color.parseColor(no_error_color));
                            }

                        } catch (JSONException e) {
                            CardView card_results = (CardView) getActivity().findViewById(R.id.cardResults);
                            card_results.setVisibility(View.VISIBLE);
                            textHeader.setText(getActivity().getResources().getString(R.string.warning));
                            textData.setText(getActivity().getResources().getString(R.string.warning_not_found_2));
                            layout_result.setBackgroundColor(Color.parseColor(error_color));
                        }

                        CardView card_next_check_wanted = (CardView) getActivity().findViewById(R.id.cardNextWanted);
                        card_next_check_wanted.setVisibility(View.VISIBLE);
                        next_card_color = "#e6e6e6";
                        card_next_check_wanted.setCardBackgroundColor(Color.parseColor(next_card_color));

                        View.OnClickListener card_next_check_wanted_clk_lstr = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Start the detail activity
                                VinSearchType searchType = new VinSearchType();
                                Integer searchVinInt = searchType.getSearchPosition("wanted");

                                Intent detailIntent = new Intent(getActivity(), ArticleDetailActivity.class);
                                detailIntent.putExtra(ArticleDetailFragment.ARG_ITEM_ID, searchVinInt.toString());
                                detailIntent.putExtra(ArticleDetailFragment.ARG_VIN, VinNumber);
                                startActivity(detailIntent);
                            }
                        };

                        card_next_check_wanted.setOnClickListener(card_next_check_wanted_clk_lstr);

                        break;
                    case "3":
                        //wanteds
                        CardView card_results = (CardView) getActivity().findViewById(R.id.cardResults);
                        card_results.setVisibility(View.VISIBLE);

                        try {
                            JSONObject RequestResult = responseJsonObject.getJSONObject("RequestResult");
                            parseWantedResult(RequestResult.toString());

                            JSONArray records = RequestResult.optJSONArray("records");
                            Integer numberWanteds = records.length();

                            if (numberWanteds >= 1) {
                                textHeader.setText(getActivity().getResources().getString(R.string.warning));
                                textData.setText(getActivity().getResources().getString(R.string.warning_found_3, numberWanteds.toString()));
                                layout_result.setBackgroundColor(Color.parseColor(error_color));

                                wantedAdapter = new WantedItemRecyclerViewAdapter(getActivity(), wantedsList);
                                mRecyclerView3.setVisibility(View.VISIBLE);
                                mRecyclerView3.setAdapter(wantedAdapter);
                                mRecyclerView3.setLayoutManager(new LinearLayoutManager(getActivity()));
                            } else {
                                textHeader.setText(getActivity().getResources().getString(R.string.notice));
                                textData.setText(getActivity().getResources().getString(R.string.notice_3));
                                layout_result.setBackgroundColor(Color.parseColor(no_error_color));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        CardView card_next_check_restricted = (CardView) getActivity().findViewById(R.id.cardNextRestricted);
                        card_next_check_restricted.setVisibility(View.VISIBLE);
                        next_card_color = "#e6e6e6";
                        card_next_check_restricted.setCardBackgroundColor(Color.parseColor(next_card_color));

                        View.OnClickListener card_next_check_restricted_clk_lstr = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Start the detail activity
                                VinSearchType searchType = new VinSearchType();
                                Integer searchVinInt = searchType.getSearchPosition("restricted");

                                Intent detailIntent = new Intent(getActivity(), ArticleDetailActivity.class);
                                detailIntent.putExtra(ArticleDetailFragment.ARG_ITEM_ID, searchVinInt.toString());
                                detailIntent.putExtra(ArticleDetailFragment.ARG_VIN, VinNumber);
                                startActivity(detailIntent);
                            }
                        };

                        card_next_check_restricted.setOnClickListener(card_next_check_restricted_clk_lstr);

                        break;
                    case "4":
                        //restricteds
                        card_results = (CardView) getActivity().findViewById(R.id.cardResults);
                        card_results.setVisibility(View.VISIBLE);

                        try {
                            JSONObject RequestResult = responseJsonObject.getJSONObject("RequestResult");
                            parseRestrictedResult(RequestResult.toString());

                            JSONArray records = RequestResult.optJSONArray("records");
                            Integer numberRestricted = records.length();

                            if (numberRestricted >= 1) {
                                textHeader.setText(getActivity().getResources().getString(R.string.warning));
                                textData.setText(getActivity().getResources().getString(R.string.warning_found_4, numberRestricted.toString()));
                                layout_result.setBackgroundColor(Color.parseColor(error_color));

                                restrictedAdapter = new RestrictedItemRecyclerViewAdapter(getActivity(), restrictedList);
                                mRecyclerView4.setVisibility(View.VISIBLE);
                                mRecyclerView4.setAdapter(restrictedAdapter);
                                mRecyclerView4.setLayoutManager(new LinearLayoutManager(getActivity()));
                            } else {
                                textHeader.setText(getActivity().getResources().getString(R.string.notice));
                                textData.setText(getActivity().getResources().getString(R.string.notice_4));
                                layout_result.setBackgroundColor(Color.parseColor(no_error_color));
                            }

                        } catch (JSONException e) {
                            textHeader.setText(getActivity().getResources().getString(R.string.warning));
                            textData.setText(getActivity().getResources().getString(R.string.warning_error));
                            layout_result.setBackgroundColor(Color.parseColor(error_color));
                        }

                        CardView card_next_check_reestr = (CardView) getActivity().findViewById(R.id.cardNextReestr);
                        card_next_check_reestr.setVisibility(View.VISIBLE);
                        next_card_color = "#e6e6e6";
                        card_next_check_reestr.setCardBackgroundColor(Color.parseColor(next_card_color));

                        View.OnClickListener card_next_check_reestr_clk_lstr = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Start the detail activity

                                Intent detailIntent = new Intent(getActivity(), ReestrActivity.class);
                                detailIntent.putExtra(ReestrActivity.ARG_VIN, VinNumber);
                                startActivity(detailIntent);
                            }
                        };

                        card_next_check_reestr.setOnClickListener(card_next_check_reestr_clk_lstr);

                        break;
                }

            }

            //1. {"RequestResult":{"ownershipPeriods":{"ownershipPeriod":[{"simplePersonType":"Natural","from":"2007-10-06T00:00:00.000+04:00","to":"2013-12-26T00:00:00.000+04:00"},{"simplePersonType":"Natural","from":"2013-12-26T00:00:00.000+04:00","to":"2016-07-25T00:00:00.000+03:00"},{"simplePersonType":"Natural","from":"2016-07-25T00:00:00.000+03:00"}]},"vehiclePassport":{"number":"63МН866130","issue":"СИМБИРСКОЕ УРП ОАО АВТОВАЗ"},"vehicle":{"engineVolume":"1596.0","color":"СЕРЕБРИСТЫЙ","bodyNumber":"ХТА11193070037528","year":"2007","engineNumber":"1939823","vin":"ХТА11193070037528","model":"ВАЗ 11193 LАDА КАLINА","category":"В","type":"22","powerHp":"80.9","powerKwt":"59.5"}},"vin":"ХТА11193070037528","regnum":null,"message":"ver.3.1","status":200}
            //2. {"RequestResult":{"errorDescription":"","statusCode":1,"Accidents":[{"AccidentDateTime":"30.05.2015 18:15","VehicleModel":"Kalina","VehicleDamageState":"Повреждено","RegionName":"Ульяновская область","AccidentNumber":"730011441","AccidentType":"Столкновение","VehicleMark":"ВАЗ","DamagePoints":["04"],"VehicleYear":"2007"}]},"vin":"XTA11193070037528","status":200}
            //2. {"RequestResult":{"errorDescription":"","statusCode":1,"Accidents":[{"AccidentDateTime":"04.04.2015 12:00","VehicleModel":"Corolla","VehicleDamageState":"Повреждено","RegionName":"Краснодарский край","AccidentNumber":"030026341","AccidentType":"Столкновение","VehicleMark":"TOYOTA","DamagePoints":["05"],"VehicleYear":"нет данных"}]},"vin":"JTNBV58E203526454","status":200}
            //2. {"RequestResult":{"errorDescription":"","statusCode":1,"Accidents":[{"AccidentDateTime":"15.04.2016 07:45","VehicleModel":"Mazda 3","VehicleDamageState":"Повреждено","RegionName":"Пензенская область","AccidentNumber":"560005843","AccidentType":"Наезд на стоящее ТС","VehicleMark":"MAZDA","DamagePoints":["02","03"],"VehicleYear":"2008"}]},"vin":"JMZBK14F691827592","status":200}
            //3. {"RequestResult":{"records":[],"count":0,"error":0},"vin":"XTA111730B0123490","status":200}
            //3. {"RequestResult":{"records":[{"w_rec":1,"w_reg_inic":"Ульяновская область","w_user":"9998","w_model":"ВАЗ21102","w_data_pu":"09.08.2005","w_god_vyp":"1999","w_vid_uch":"Т","w_un_gic":"685016"}],"count":1,"error":0},"vin":"ХТА211020Y0156189","status":200}
            //4. {"RequestResult":{"records":[],"count":0,"error":0},"vin":"XTA111730B0123490","status":200}
            //4. {"RequestResult":{"records":[{"regname":"Архангельская область","gid":"29#FF000142","tsVIN":"KNMCSHLMS7P656793","codDL":0,"dateogr":"26.07.2016T00:00:00.000+03:00","ogrkod":1,"tsmodel":"НИССАН АLМЕRА СLАSSIС 1.6","tsKuzov":"KNMCSHLMS7P656793","codeTo":47,"dateadd":"26.07.2016T12:44:54.231+03:00","phone":"8 81837 2 35 44","regid":1111,"divtype":2,"divid":1111026},{"regname":"Архангельская область","gid":"29#260501834","tsVIN":"KNMCSHLMS7P656793","codDL":1111,"dateogr":"11.11.2014T00:00:00.000+03:00","ogrkod":1,"tsmodel":"НИССАНАLМЕRАСLАSSIС16","tsKuzov":"KNMCSHLMS7P656793","codeTo":47,"dateadd":"21.11.2014T00:00:00.000+03:00","phone":"412689","regid":1111,"divtype":2,"divid":26}],"count":0,"error":0},"vin":"KNMCSHLMS7P656793","status":200}
            //   {"message":"Цифры с картинки введены неверно","status":201}

            //Hide keyboard
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            RunCounts settings = new RunCounts();
            Boolean isAdFree = settings.isAdFree();

            if (!isAdFree) {
                SettingsStorage settingsStorage = new SettingsStorage();
                Boolean showAd = settingsStorage.isShowInterstitial(getActivity());

                if (showAd && mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                    requestNewInterstitial();
                }

                CardView card_adview = (CardView) getActivity().findViewById(R.id.cardAdView);
                card_adview.setVisibility(View.VISIBLE);
                CardView card_adview_small = (CardView) getActivity().findViewById(R.id.cardAdViewSmall);
                card_adview_small.setVisibility(View.GONE);
            } else {
                CardView card_adview = (CardView) getActivity().findViewById(R.id.cardAdView);
                card_adview.setVisibility(View.GONE);
            }

            CardView cardNextMileage = (CardView) getActivity().findViewById(R.id.cardNextMileage);
            cardNextMileage.setVisibility(View.VISIBLE);
            cardNextMileage.setCardBackgroundColor(Color.parseColor("#e6e6e6"));

            View.OnClickListener card_next_check_reestr_clk_lstr = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start the detail activity

                    Intent detailIntent = new Intent(getActivity(), MileageActivity.class);
                    detailIntent.putExtra(MileageActivity.ARG_VIN, VinNumber);
                    startActivity(detailIntent);
                }
            };

            cardNextMileage.setOnClickListener(card_next_check_reestr_clk_lstr);

            CardView cardNextReport = (CardView) getActivity().findViewById(R.id.cardNextReport);
            cardNextReport.setVisibility(View.VISIBLE);

            View.OnClickListener cardNextReportListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent detailIntent = new Intent(getActivity(), FullReportActivity.class);
                    detailIntent.putExtra(FullReportActivity.ARG_VIN, VinNumber);
                    startActivity(detailIntent);
                }
            };
            cardNextReport.setOnClickListener(cardNextReportListener);

            String nextReportColor = "#9EF39B";
            cardNextReport.setCardBackgroundColor(Color.parseColor(nextReportColor));

            scroolToResult();
        }
    }

    private void parseWantedResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            JSONArray records = response.optJSONArray("records");
            wantedsList = new ArrayList<>();
            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.optJSONObject(i);
                WantedItem item = new WantedItem();
                item.setModel(record.optString("w_model"));
                item.setDataPu(record.optString("w_data_pu"));
                item.setGodVyp(record.optString("w_god_vyp"));
                item.setRegInic(record.optString("w_reg_inic"));

                wantedsList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseHistoryResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            JSONArray records = response.optJSONArray("ownershipPeriod");
            historyList = new ArrayList<>();
            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.optJSONObject(i);
                HistoryItem item = new HistoryItem();

                item.setPersonType(record.optString("simplePersonType"));
                item.setFrom(record.optString("from"));
                item.setTo(record.optString("to"));
                item.setLastOperation(record.optString("lastOperation"));

                historyList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseAccidentResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            JSONArray records = response.optJSONArray("Accidents");
            accidentList = new ArrayList<>();
            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.optJSONObject(i);
                AccidentItem item = new AccidentItem();

                item.setVehicleModel(record.optString("VehicleModel"));
                item.setVehicleMark(record.optString("VehicleMark"));
                item.setVehicleDamageState(record.optString("VehicleDamageState"));
                item.setRegionName(record.optString("RegionName"));
                item.setAccidentTime(record.optString("AccidentDateTime"));
                item.setAccidentNumber(record.optString("AccidentNumber"));
                item.setAccidentType(record.optString("AccidentType"));

                damageList = new ArrayList<>();
                JSONArray damage = record.getJSONArray("DamagePoints");
                for (int j = 0; j < damage.length(); j++) {
                    String damage_value = damage.getString(j);
                    damageList.add(damage_value);
                }

                item.setDamagePoints(damageList);
                accidentList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseRestrictedResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            JSONArray records = response.optJSONArray("records");
            restrictedList = new ArrayList<>();

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.optJSONObject(i);
                RestrictedItem item = new RestrictedItem();

                item.setRegName(record.optString("regname"));
                item.setDateOgr(record.optString("dateogr"));
                item.setOgrKod(record.optString("ogrkod"));
                item.setTsmodel(record.optString("tsmodel"));
                item.setPhone(record.optString("phone"));
                item.setDivType(record.optString("divtype"));
                item.setOsnOgr(record.optString("osnOgr"));

                restrictedList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void scroolToResult() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
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
                AppBarLayout appbar = (AppBarLayout) getActivity().findViewById(R.id.appbar);
                if (appbar != null) {
                    appbar.setExpanded(false);
                }
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

            CardView card_results = (CardView) getActivity().findViewById(R.id.cardResults);
            card_results.setVisibility(View.GONE);

            CardView cardVehilePassport = (CardView) getActivity().findViewById(R.id.cardVehiclePassport);
            cardVehilePassport.setVisibility(View.GONE);

            CardView cardVehileDetails = (CardView) getActivity().findViewById(R.id.cardVehicleDetails);
            cardVehileDetails.setVisibility(View.GONE);

            CardView card_next_check_dtp = (CardView) getActivity().findViewById(R.id.cardNextDtp);
            card_next_check_dtp.setVisibility(View.GONE);

            CardView card_next_check_wanted = (CardView) getActivity().findViewById(R.id.cardNextWanted);
            card_next_check_wanted.setVisibility(View.GONE);

            CardView card_next_check_restricted = (CardView) getActivity().findViewById(R.id.cardNextRestricted);
            card_next_check_restricted.setVisibility(View.GONE);

            CardView card_next_check_reestr = (CardView) getActivity().findViewById(R.id.cardNextReestr);
            card_next_check_reestr.setVisibility(View.GONE);

            CardView card_next_check_mileage = (CardView) getActivity().findViewById(R.id.cardNextMileage);
            card_next_check_mileage.setVisibility(View.GONE);

            CardView cardNextReport = (CardView) getActivity().findViewById(R.id.cardNextReport);
            cardNextReport.setVisibility(View.GONE);

            reloadWebView();
            captchaWebView.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if (getActivity() != null && getActivity().isFinishing()) {
                return;
            }

            if (getActivity() == null) {
                return;
            }

            dismissProgressDialog();

        }
    }

    private void reloadWebView() {
        requestApiCount = 0;
        progressView.setVisibility(View.VISIBLE);
        captchaWebView.getSettings().setJavaScriptEnabled(true);
        captchaWebView.getSettings().setDomStorageEnabled(true);
        captchaWebView.getSettings().setLoadsImagesAutomatically(false);
        captchaWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        captchaWebView.addJavascriptInterface(new JavaScriptInterface(captchaWebView), "AutoinsInterface");

        autoinsWebViewClient = new AutoinsWebViewClient();
        captchaWebView.setWebChromeClient(new WebChromeClient());
        captchaWebView.setWebViewClient(autoinsWebViewClient);
        autoinsWebViewClient.prepareToLoadUrl();

        captchaWebView.loadUrl(URL_GET_GIBDD);
    }

    public class AutoinsWebViewClient extends WebViewClient {

        private int timeout = 30;
        boolean pageLoaded = false;
        // Flag to instruct the client to ignore callbacks after an error
        boolean hasError = false;
        private Handler timeoutHandler;

        private AutoinsWebViewClient() {
            timeoutHandler = new Handler();
        }

        // Called by activity before requesting load of a url
        private void prepareToLoadUrl() {
            this.hasError = false;
            this.pageLoaded = true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (hasError) {
                return;
            }

            // timeout has expired if this flag is still set when the message is handled
            pageLoaded = false;
            Runnable run = new Runnable() {
                public void run() {
                    // Do nothing if we already have an error
                    if (hasError) {
                        return;
                    }

                    // Dismiss any current alerts and progress
                    if (!pageLoaded) {
                        captchaWebView.setVisibility(View.GONE);
                        progressView.setVisibility(View.GONE);
                    }
                }
            };
            timeoutHandler.postDelayed(run, this.timeout * 1000);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        public void onReceivedError(android.webkit.WebView view, WebResourceRequest request, WebResourceError error) {
            view.stopLoading();
            hasError = true;
            if (getActivity() != null) {
                captchaWebView.setVisibility(View.GONE);
                Toast.makeText(getActivity(), getString(R.string.insurance_error), Toast.LENGTH_LONG).show();
                progressView.setVisibility(View.GONE);
            }

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "received_error");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "GIBDD_SITE");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "GIBDD_SITE");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }

        public void onReceivedSslError(android.webkit.WebView view, SslErrorHandler handler, SslError er) {
            SslErrorHandler sslErrorHandler = handler;
            if (er.getUrl().equals(URL_GET_GIBDD)) {
                sslErrorHandler.proceed();
            } else {
                sslErrorHandler.cancel();
            }
            progressView.setVisibility(View.GONE);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "received_ssl_error");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "GIBDD_SITE");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "GIBDD_SITE");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }

        public void onPageFinished(final android.webkit.WebView view, String url) {
            super.onPageFinished(view, url);
            if (hasError) {
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "success");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "GIBDD_SITE");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "GIBDD_SITE");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (url.equals(URL_GET_GIBDD)) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    view.evaluateJavascript("javascript:$('.ln-header').hide()", null);
                    view.evaluateJavascript("javascript:$('.ln-footer').hide()", null);
                    view.evaluateJavascript("javascript:$('.bn-federal-site').hide()", null);
                    view.evaluateJavascript("javascript:$('.bn-top-menu').hide()", null);
                    view.evaluateJavascript("javascript:$('.ln-content-right').hide()", null);
                    view.evaluateJavascript("javascript:$('.widget-mistake').hide()", null);
                    view.evaluateJavascript("javascript:$('.b-mobile-section').hide()", null);
                    view.evaluateJavascript("javascript:$('h1,h2').hide()", null);
                    view.evaluateJavascript("javascript:$('.ln-page').hide()", null);

                    view.evaluateJavascript("javascript:var countRetry = 1;\n" +
                            "var maxCountRetry = 10;\n" +
                            "\n" +
                            "if (countRetry < maxCountRetry) {\n" +
                            "    checkVariable(countRetry);\n" +
                            "}\n" +
                            "\n" +
                            "function checkVariable() {\n" +
                            "   countRetry++;\n" +
                            "   console.log(countRetry);\n" +
                            "   if (appVehicleCheck.reCaptchaToken != undefined) {\n" +
                            "      window.AutoinsInterface.make(appVehicleCheck.reCaptchaToken);\n" +
                            "   } else {\n" +
                            "      if (countRetry < maxCountRetry) {\n" +
                            "          window.setTimeout(\"checkVariable();\", 3000);\n" +
                            "      }\n" +
                            "   }\n" +
                            "}", null);
               } else {
                    view.loadUrl("javascript:$('.ln-header').hide()");
                    view.loadUrl("javascript:$('.ln-footer').hide()");
                    view.loadUrl("javascript:$('.bn-federal-site').hide()");
                    view.loadUrl("javascript:$('.bn-top-menu').hide()");
                    view.loadUrl("javascript:$('.ln-content-right').hide()");
                    view.loadUrl("javascript:$('.widget-mistake').hide()");
                    view.loadUrl("javascript:$('.b-mobile-section').hide()");
                    view.loadUrl("javascript:$('h1,h2').hide()");
                    view.loadUrl("javascript:$('.ln-page').hide()");

                    view.loadUrl("javascript:var countRetry = 1;\n" +
                            "var maxCountRetry = 10;\n" +
                            "\n" +
                            "if (countRetry < maxCountRetry) {\n" +
                            "    checkVariable(countRetry);\n" +
                            "}\n" +
                            "\n" +
                            "function checkVariable() {\n" +
                            "   countRetry++;\n" +
                            "   console.log(countRetry);\n" +
                            "   if (appVehicleCheck.reCaptchaToken != undefined) {\n" +
                            "      window.AutoinsInterface.make(appVehicleCheck.reCaptchaToken);\n" +
                            "   } else {\n" +
                            "      if (countRetry < maxCountRetry) {\n" +
                            "          window.setTimeout(\"checkVariable();\", 3000);\n" +
                            "      }\n" +
                            "   }\n" +
                            "}");
                }
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
            autoinsWebViewClient.pageLoaded = true;
            if (captcha_answer != null && captcha_answer.length() > 0) {


                switch (ActiveItemId) {
                    case "1":

                        /* url, captcha, checktype, vin*/
                        ArrayList<String> passing1 = new ArrayList<String>();
                        passing1.add("https://xn--b1afk4ade.xn--90adear.xn--p1ai/proxy/check/auto/history");
                        passing1.add(captcha_answer);
                        passing1.add("history");
                        passing1.add(VinNumber);

                        getGibddData = new GetGibddData();
                        try {
                            getGibddData.execute(passing1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;
                    case "2":
                        ArrayList<String> passing2 = new ArrayList<String>();
                        passing2.add("https://xn--b1afk4ade.xn--90adear.xn--p1ai/proxy/check/auto/dtp");
                        passing2.add(captcha_answer);
                        passing2.add("aiusdtp");
                        passing2.add(VinNumber);

                        getGibddData = new GetGibddData();
                        try {
                            getGibddData.execute(passing2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;
                    case "3":
                        ArrayList<String> passing3 = new ArrayList<String>();
                        passing3.add("https://xn--b1afk4ade.xn--90adear.xn--p1ai/proxy/check/auto/wanted");
                        passing3.add(captcha_answer);
                        passing3.add("wanted");
                        passing3.add(VinNumber);

                        getGibddData = new GetGibddData();
                        try {
                            getGibddData.execute(passing3);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;
                    case "4":
                        ArrayList<String> passing4 = new ArrayList<String>();
                        passing4.add("https://xn--b1afk4ade.xn--90adear.xn--p1ai/proxy/check/auto/restrict");
                        passing4.add(captcha_answer);
                        passing4.add("restricted");
                        passing4.add(VinNumber);

                        getGibddData = new GetGibddData();
                        try {
                            getGibddData.execute(passing4);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;
                }

            } else {
                captchaWebView.setVisibility(View.GONE);
                progressView.setVisibility(View.GONE);
            }
        }
    }
}