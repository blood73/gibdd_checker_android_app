package ru.bloodsoft.gibddchecker.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.firebase.analytics.FirebaseAnalytics;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.models.ApiResponse;
import ru.bloodsoft.gibddchecker.models.VehicleTypes;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;
import ru.bloodsoft.gibddchecker.util.ApiPurchaseVerifier;
import ru.bloodsoft.gibddchecker.util.AsyncResponse;
import ru.bloodsoft.gibddchecker.util.KeyBoardUtils;
import ru.bloodsoft.gibddchecker.util.NewWebService;
import ru.bloodsoft.gibddchecker.util.RegexUtil;
import ru.bloodsoft.gibddchecker.util.RunCounts;
import ru.bloodsoft.gibddchecker.util.SanitizeHelper;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class FullReportActivity extends BaseActivity implements BillingProcessor.IBillingHandler, AsyncResponse {

    @BindView(R.id.buy_report_card)
    CardView buyReportCard;

    @BindView(R.id.buy_button)
    Button buyButton;

    @BindView(R.id.scrollView)
    NestedScrollView mainScrollView;

    @BindView(R.id.main_content)
    CoordinatorLayout mainContent;

    @BindView(R.id.send_message)
    CardView sendMessage;

    @BindView(R.id.vinNumber)
    EditText vinEditText;

    @BindView(R.id.success_card)
    CardView cardSuccessReport;

    @BindView(R.id.card_vehicle_details)
    CardView cardVehicleDetails;

    @BindView(R.id.headerVehicleDetails)
    TextView headerVehicleDetails;

    @BindView(R.id.vehicleModel)
    TextView textVehicleModel;

    @BindView(R.id.vehicleYear)
    TextView textVehicleYear;

    @BindView(R.id.vehicleVin)
    TextView textVehicleVin;

    @BindView(R.id.vehicleBodyNumber)
    TextView textVehicleBodyNumber;

    @BindView(R.id.vehicleChassisNumber)
    TextView textVehicleChassisNumber;

    @BindView(R.id.vehicleColor)
    TextView textVehicleColor;

    @BindView(R.id.vehicleEngineVolume)
    TextView textVehicleEngineVolume;

    @BindView(R.id.vehicleEnginePower)
    TextView textVehicleEnginePower;

    @BindView(R.id.vehicleCategory)
    TextView textVehicleCategory;

    @BindView(R.id.vehicleType)
    TextView textVehicleType;

    @BindView(R.id.error_card)
    CardView errorCardView;

    @BindView(R.id.error_text)
    TextView textError;

    @BindView(R.id.captcha_webview)
    WebView captchaWebView;

    @BindView(R.id.progress_layout)
    View progressView;

    ProgressDialog mProgressDialog;
    private Snackbar mSnackbar;

    AlertDialog.Builder paymentAlertDialog;

    BillingProcessor bp;

    String vinNumber;

    private FirebaseAnalytics mFirebaseAnalytics;

    public static final String ARG_VIN = "item_id";

    private static final String FULL_REPORT = "full_report";
    private static final String AD_FREE = "ad_free";

    public static final String URL_GET_GIBDD ="https://xn--90adear.xn--p1ai/check/auto";

    private static final int FULL_REPORT_PRICE = 149;
    private static final String TAG = makeLogTag(FullReportActivity.class);

    private String ERROR_CODE = "";
    private String ERROR_MESSAGE = "";
    private static final int USER_CANCEL_PURCHASE_CODE = 1;
    private static final int ITEM_ALREADY_OWNED = 7;

    NewWebService webService;
    GetVehicleData getVehicleData;
    CreateReportRequest createReportRequest;
    SendGibddRequest sendGibddRequest;

    private int requestApiCount = 0;
    private static final int MAX_REQUEST_API_COUNT = 1;

    private AutoinsWebViewClient autoinsWebViewClient;

    static {
        System.loadLibrary("native-lib");
    }

    public static native String getUrlGetVehicle();
    public static native String getUrlGibddRequest();
    public static native String getUrlCreateReport();
    public static native String getUrlGetGibddData();

    //https://antiperekup.net/api/v2/get_vehicle/
    private static final String URL_GET_VEHICLE = SanitizeHelper.decryptString(getUrlGetVehicle());
    //https://antiperekup.net/api/v2/gibdd_request/
    private static final String URL_GIBDD_REQUEST = SanitizeHelper.decryptString(getUrlGibddRequest());
    //https://antiperekup.net/api/v1/create_report/
    private static final String URL_CREATE_REPORT = SanitizeHelper.decryptString(getUrlCreateReport());
    //https://antiperekup.net/api/v1/get_gibdd_data/
    private static final String URL_GET_GIBDD_DATA = SanitizeHelper.decryptString(getUrlGetGibddData());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_report);
        ButterKnife.bind(this);
        setupToolbar();

        bp = new BillingProcessor(this, App.getPublicKey(), ApiPurchaseVerifier.getMerchantId(), this);
        bp.initialize();

        Bundle b = FullReportActivity.this.getIntent().getExtras();
        if (b != null) {
            String vin = b.getString(ARG_VIN);
            if (vin != null && !vin.isEmpty()) {
                vinEditText.setText(vin);
            }
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(FullReportActivity.this);
        sendMessage.setVisibility(View.GONE);
    }

    @Override
    public void onBillingInitialized() {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */

        bp.loadOwnedPurchasesFromGoogle();
        bp.listOwnedProducts();

        List<SkuDetails> skuDetailsList = bp.getPurchaseListingDetails(getInAppSkus());
        if (skuDetailsList != null) {
            updateInventory(skuDetailsList);
        } else {
            initPrices(FULL_REPORT_PRICE);
        }

        for (String sku : bp.listOwnedProducts()) {
            if (!sku.equals(AD_FREE)) {
                bp.consumePurchase(sku);
            }
        }

    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        /*
         * Called when requested PRODUCT ID was successfully purchased
         */

        buyReportCard.setVisibility(View.GONE);

        bp.consumePurchase(productId);

        if (productId.equals(FULL_REPORT)) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "full_report_finish");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "full_report");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "FULL_REPORT");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }

        showProgressDialog();

        ArrayList<String> passing = new ArrayList<String>();
        passing.add(details.purchaseInfo.responseData);

        ApiPurchaseVerifier apiPurchaseVerifier = new ApiPurchaseVerifier();
        apiPurchaseVerifier.delegate = FullReportActivity.this;
        apiPurchaseVerifier.execute(passing);

        bp.loadOwnedPurchasesFromGoogle();
    }

    @Override
    public void onBillingError(int response, Throwable e) {
        /*
         * Called when some error occurred. See Constants class for more details
         *
         * Note - this includes handling the case where the user canceled the buy dialog:
         * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
         */

        dismissProgressDialog();

        logD(TAG, "in-app error: " + Integer.toString(response));
        String errorText = "";
        if (response == USER_CANCEL_PURCHASE_CODE) {
            errorText = FullReportActivity.this.getResources().getString(R.string.cancelled);
        } else {
            errorText = FullReportActivity.this.getResources().getString(R.string.inapp_error);
        }

        mSnackbar = Snackbar.make(mainContent, errorText, Snackbar.LENGTH_INDEFINITE)
                .setAction("Action", null)
                .setDuration(2000);
        mSnackbar.show();

        if (response != USER_CANCEL_PURCHASE_CODE) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "inapp_full_report_error");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "error");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "REPORT_ERROR");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            sendMessage.setVisibility(View.VISIBLE);
            ERROR_CODE = Integer.toString(response);
            if (e != null) {
                ERROR_MESSAGE = e.getMessage() != null ? e.getMessage() : "";
            } else {
                ERROR_MESSAGE = "";
            }
        }

        if (response == ITEM_ALREADY_OWNED) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "inapp_full_report_error");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "error");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "REPORT_ERROR");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle);
        }

    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
        for (String sku : bp.listOwnedProducts()) {
            logD(TAG, "Owned Managed Product: " + sku);
            if (!sku.equals(AD_FREE)) {
                bp.consumePurchase(sku);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void processFinish(boolean result) {

        bp.loadOwnedPurchasesFromGoogle();

        dismissProgressDialog();

        if (result && vinNumber != null && !vinNumber.isEmpty()) {
            /* vin*/
            ArrayList<String> passing = new ArrayList<String>();
            passing.add(vinNumber);

            createReportRequest = new CreateReportRequest();

            try {
                createReportRequest.execute(passing);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            logD(TAG, "in-app error");
            String errorText = FullReportActivity.this.getResources().getString(R.string.inapp_error);

            mSnackbar = Snackbar.make(mainContent, errorText, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null)
                    .setDuration(2000);
            mSnackbar.show();

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "inapp_full_report_error");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "error");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "REPORT_ERROR");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_END, bundle);

            sendMessage.setVisibility(View.VISIBLE);
            ERROR_CODE = "666";
            ERROR_MESSAGE = "Can't validate purchase";

        }
    }

    public void updateInventory(List<SkuDetails> products) {

        int price1 = FULL_REPORT_PRICE;

        for (SkuDetails skuDetails : products) {
            if (skuDetails.productId.equals(FULL_REPORT)) {
                try {
                    price1 = Integer.parseInt(RegexUtil.extractFirstNumber(skuDetails.priceText));
                } catch (Exception e) {
                    e.printStackTrace();
                    price1 = FULL_REPORT_PRICE;
                }
            }
        }

        initPrices(price1);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void showProgressDialog() {
        // instantiate it within the onCreate method
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(FullReportActivity.this);
            mProgressDialog.setMessage(FullReportActivity.this.getResources().getString(R.string.loading));
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

    private static ArrayList<String> getInAppSkus() {
        final ArrayList<String> skus = new ArrayList<>();
        skus.add(FULL_REPORT);
        return skus;
    }

    @OnClick(R.id.send_message)
    protected void onSendErrorClicked() {
        Toast.makeText(FullReportActivity.this, FullReportActivity.this.getResources().getString(R.string.rate_app_feedback_sent), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_SEND);
        RunCounts settings = new RunCounts();

        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"antiperekup.app@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Антиперекуп. Ошибка при оплате");
        intent.putExtra(Intent.EXTRA_TEXT, "\n\n\n\n\n\n\n" +
                "\n===========================\n" +
                "Не удаляйте следующую информацию\n" +
                settings.getSSAD() +
                "\nCODE: " + ERROR_CODE + "\n" +
                "MESSAGE: " + ERROR_MESSAGE
        );

        intent.setType("message/rfc822");

        startActivity(Intent.createChooser(intent, "Выберите ваш email-клиент для отправки письма:"));
    }

    private void initPrices(int price1) {
        String price1String = FullReportActivity.this.getResources().getString(R.string.price_report, price1);
        buyButton.setText(price1String);
    }

    @Override
    public void onDestroy() {
        dismissProgressDialog();
        if (bp != null) {
            bp.release();
        }
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

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_full_report;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }

    private class GetVehicleData extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            cardSuccessReport.setVisibility(View.GONE);
            buyReportCard.setVisibility(View.GONE);
            cardVehicleDetails.setVisibility(View.GONE);
            errorCardView.setVisibility(View.GONE);
            sendMessage.setVisibility(View.GONE);
            showProgressDialog();
        }

        @Override
        protected String doInBackground(ArrayList<String>... passing) {

            ArrayList<String> passed = passing[0]; //get passed arraylist
            Object[] mStringArray = passed.toArray();

            String vin = (String) mStringArray[0];
            String response = "";

            RunCounts settings = new RunCounts();
            RequestBody formBody = new FormBody.Builder()
                    .add("vin", vin)
                    .build();

            try {
                response = webService.sendNewHttpsPost(URL_GET_VEHICLE, formBody);
            } catch (Exception e) {
                e.printStackTrace();
            }

            logD(TAG, "Response: " + response);

            return response;
        }

        @Override
        protected void onPostExecute(String response) {

            if (FullReportActivity.this.isFinishing()) {
                return;
            }

            dismissProgressDialog();

            JSONObject responseJsonObject = null;
            JSONObject responseJson = null;

            String errorMessage = "";
            Boolean isVehicleDataExists = false;

            String vehicleModel = "";
            String vehicleYear = "";
            String vehicleVin = "";
            String vehicleBodyNumber = "";
            String vehicleChassisNumber = "";
            String vehicleColor = "";
            String vehicleEngineVolume = "";
            String vehicleEnginePowerKwt = "";
            String vehicleEnginePowerHp = "";
            String vehiclePower = "";
            String vehicleCategory = "";
            String vehicleTypeString = "";

            try {
                responseJsonObject = new JSONObject(response);
                responseJson = responseJsonObject.optJSONObject("data");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                vehicleModel = responseJson.optString("model");
                vehicleYear = responseJson.optString("year");
                vehicleVin = responseJson.optString("vin");
                vehicleBodyNumber = responseJson.optString("bodyNumber");
                vehicleChassisNumber = responseJson.optString("chassisNumber");
                vehicleColor = responseJson.optString("color");
                vehicleEngineVolume = responseJson.optString("engineVolume");
                vehicleEnginePowerKwt = responseJson.optString("powerKwt");
                vehicleEnginePowerHp = responseJson.optString("powerHp");
                vehiclePower = vehicleEnginePowerKwt + "/" + vehicleEnginePowerHp;
                vehicleCategory = responseJson.optString("category");
                vehicleTypeString = responseJson.optString("type");

                errorMessage = responseJson.optString("error_message");
                isVehicleDataExists = responseJson.optBoolean("result");

            } catch (Exception e) {
                e.printStackTrace();
                buyReportCard.setVisibility(View.GONE);
                cardVehicleDetails.setVisibility(View.GONE);
                errorCardView.setVisibility(View.VISIBLE);
                textError.setText(FullReportActivity.this.getResources().getString(R.string.report_vehicle_error));
            }

            if (errorMessage.isEmpty()) {
                if (isVehicleDataExists) {
                    if (!vehicleVin.isEmpty() || !vehicleBodyNumber.isEmpty() || !vehicleChassisNumber.isEmpty()) {
                        VehicleTypes vehicleType = new VehicleTypes();
                        String vehicleTypeValue = vehicleType.getVehicleType(vehicleTypeString);

                        headerVehicleDetails.setText(FullReportActivity.this.getResources().getString(R.string.report_vehicle_header));
                        textVehicleModel.setText(vehicleModel);
                        textVehicleYear.setText(vehicleYear);
                        textVehicleVin.setText(vehicleVin);
                        textVehicleBodyNumber.setText(vehicleBodyNumber);
                        textVehicleChassisNumber.setText(vehicleChassisNumber);
                        textVehicleColor.setText(vehicleColor);
                        textVehicleEngineVolume.setText(vehicleEngineVolume);
                        textVehicleEnginePower.setText(vehiclePower);
                        textVehicleCategory.setText(vehicleCategory);
                        textVehicleType.setText(vehicleTypeValue);

                        cardVehicleDetails.setVisibility(View.VISIBLE);
                        buyReportCard.setVisibility(View.VISIBLE);

                        scroolToResult();

                    } else {
                        cardVehicleDetails.setVisibility(View.GONE);
                        buyReportCard.setVisibility(View.GONE);

                        errorCardView.setVisibility(View.VISIBLE);
                        textError.setText(FullReportActivity.this.getResources().getString(R.string.report_vehicle_error));
                    }
                } else {
                    //webview
                    reloadWebView();
                    captchaWebView.setVisibility(View.VISIBLE);
                }
            } else {
                errorCardView.setVisibility(View.VISIBLE);
                textError.setText(errorMessage);
            }

            scroolToResult();
            KeyBoardUtils.hideKeyboard(FullReportActivity.this);
        }
    }

    private class SendGibddRequest extends AsyncTask<ArrayList<String>, ApiResponse, ApiResponse> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ApiResponse doInBackground(ArrayList<String>... passing) {

            ArrayList<String> passed = passing[0]; //get passed arraylist
            Object[] mStringArray = passed.toArray();

            /* vin, session_id, captcha*/
            String vin = (String) mStringArray[0];
            String sessionId = (String) mStringArray[1];
            String captcha = (String) mStringArray[2];

            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setResponse("");
            apiResponse.setSuccess(false);

            if (requestApiCount < MAX_REQUEST_API_COUNT) {
                requestApiCount++;
                RequestBody formBody = new FormBody.Builder()
                        .add("vin", vin)
                        .add("session_id", sessionId)
                        .add("captcha", captcha)
                        .build();

                try {
                    apiResponse.setResponse(webService.sendNewHttpsPost(URL_GIBDD_REQUEST, formBody));
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

            if (FullReportActivity.this.isFinishing()) {
                return;
            }

            if (!apiResponse.isSuccess()) {
                return;
            } else {
                response = apiResponse.getResponse();
            }

            progressView.setVisibility(View.GONE);
            captchaWebView.setVisibility(View.GONE);
            buyReportCard.setVisibility(View.GONE);
            errorCardView.setVisibility(View.GONE);
            sendMessage.setVisibility(View.GONE);

            JSONObject responseJsonObject = null;
            JSONObject responseJson = null;

            String errorMessage = "";

            String vehicleModel = "";
            String vehicleYear = "";
            String vehicleVin = "";
            String vehicleBodyNumber = "";
            String vehicleChassisNumber = "";
            String vehicleColor = "";
            String vehicleEngineVolume = "";
            String vehicleEnginePowerKwt = "";
            String vehicleEnginePowerHp = "";
            String vehiclePower = "";
            String vehicleCategory = "";
            String vehicleTypeString = "";

            try {
                responseJsonObject = new JSONObject(response);
                responseJson = responseJsonObject.optJSONObject("data");
            } catch (JSONException e) {
                e.printStackTrace();
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "report_invalid_data");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "api_report");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "API_REPORT");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }

            try {
                vehicleModel = responseJson.optString("model");
                vehicleYear = responseJson.optString("year");
                vehicleVin = responseJson.optString("vin");
                vehicleBodyNumber = responseJson.optString("bodyNumber");
                vehicleChassisNumber = responseJson.optString("chassisNumber");
                vehicleColor = responseJson.optString("color");
                vehicleEngineVolume = responseJson.optString("engineVolume");
                vehicleEnginePowerKwt = responseJson.optString("powerKwt");
                vehicleEnginePowerHp = responseJson.optString("powerHp");
                vehiclePower = vehicleEnginePowerKwt + "/" + vehicleEnginePowerHp;
                vehicleCategory = responseJson.optString("category");
                vehicleTypeString = responseJson.optString("type");

                errorMessage = responseJson.optString("error_message");

            } catch (Exception e) {
                e.printStackTrace();
                buyReportCard.setVisibility(View.GONE);
                cardVehicleDetails.setVisibility(View.GONE);
                errorCardView.setVisibility(View.VISIBLE);
                textError.setText(FullReportActivity.this.getResources().getString(R.string.report_vehicle_error));

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "report_invalid_response");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "api_report");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "API_REPORT");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }

            if (errorMessage.isEmpty()) {
                if (!vehicleVin.isEmpty() || !vehicleBodyNumber.isEmpty() || !vehicleChassisNumber.isEmpty()) {
                    VehicleTypes vehicleType = new VehicleTypes();
                    String vehicleTypeValue = vehicleType.getVehicleType(vehicleTypeString);

                    headerVehicleDetails.setText(FullReportActivity.this.getResources().getString(R.string.report_vehicle_header));
                    textVehicleModel.setText(vehicleModel);
                    textVehicleYear.setText(vehicleYear);
                    textVehicleVin.setText(vehicleVin);
                    textVehicleBodyNumber.setText(vehicleBodyNumber);
                    textVehicleChassisNumber.setText(vehicleChassisNumber);
                    textVehicleColor.setText(vehicleColor);
                    textVehicleEngineVolume.setText(vehicleEngineVolume);
                    textVehicleEnginePower.setText(vehiclePower);
                    textVehicleCategory.setText(vehicleCategory);
                    textVehicleType.setText(vehicleTypeValue);

                    cardVehicleDetails.setVisibility(View.VISIBLE);
                    buyReportCard.setVisibility(View.VISIBLE);

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "report_success");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "api_report");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "API_REPORT");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                } else {
                    cardVehicleDetails.setVisibility(View.GONE);
                    buyReportCard.setVisibility(View.GONE);

                    errorCardView.setVisibility(View.VISIBLE);
                    textError.setText(FullReportActivity.this.getResources().getString(R.string.report_vehicle_error));

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "report_invalid_vehicle");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "api_report");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "API_REPORT");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                }
            } else {
                errorCardView.setVisibility(View.VISIBLE);
                textError.setText(errorMessage);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "report_error");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "api_report");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "API_REPORT");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }

            scroolToResult();
            KeyBoardUtils.hideKeyboard(FullReportActivity.this);
        }
    }

    private class CreateReportRequest extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected String doInBackground(ArrayList<String>... passing) {

            ArrayList<String> passed = null;
            Object[] mStringArray = null;
            if (passing != null) {
                passed = passing[0];
            }

            if (passed != null) {
                mStringArray = passed.toArray();
            }

            /* vin, session_id, captcha*/
            String vin = "";
            if (mStringArray != null) {
                vin = (String) mStringArray[0];
            }

            String response = "";

            if (!vin.isEmpty()) {
                RequestBody formBody = new FormBody.Builder()
                        .add("vin", vin)
                        .build();

                try {
                    response = webService.sendNewHttpsPost(URL_CREATE_REPORT, formBody);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            logD(TAG, "Response: " + response);

            return response;
        }

        @Override
        protected void onPostExecute(String response) {

            if (FullReportActivity.this.isFinishing()) {
                return;
            }

            dismissProgressDialog();

            JSONObject responseJsonObject = null;
            JSONObject responseJson = null;

            String errorMessage = "";
            boolean resultRequest = false;


            try {
                responseJsonObject = new JSONObject(response);
                responseJson = responseJsonObject.optJSONObject("data");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                resultRequest = responseJson.optBoolean("result");
                errorMessage = responseJson.optString("error_message");
            } catch (Exception e) {
                e.printStackTrace();
                buyReportCard.setVisibility(View.GONE);
                cardVehicleDetails.setVisibility(View.GONE);
                errorCardView.setVisibility(View.VISIBLE);
                textError.setText(FullReportActivity.this.getResources().getString(R.string.report_vehicle_error));
                sendMessage.setVisibility(View.VISIBLE);
            }

            if (errorMessage.isEmpty()) {
                if (resultRequest) {
                    cardSuccessReport.setVisibility(View.VISIBLE);
                } else {
                    sendMessage.setVisibility(View.VISIBLE);
                }
            } else {
                errorCardView.setVisibility(View.VISIBLE);
                textError.setText(errorMessage);
                sendMessage.setVisibility(View.VISIBLE);
            }

            scroolToResult();
            KeyBoardUtils.hideKeyboard(FullReportActivity.this);
        }
    }

    public boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    @OnClick(R.id.paste)
    public void onPasteClicked(View view) {
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

    @OnClick(R.id.search)
    public void onSearchClicked(View view) {
        vinNumber = vinEditText.getText().toString();

        if (isConnectedToInternet()) {
            if (vinNumber != null && !vinNumber.trim().isEmpty()) {

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "FULL_REPORT");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, vinNumber);
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "VIN");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);


                ArrayList<String> passing = new ArrayList<String>();
                passing.add(vinNumber);
                getVehicleData = new GetVehicleData();
                getVehicleData.execute(passing);

            } else {
                mSnackbar = Snackbar.make(mainContent, FullReportActivity.this.getResources().getString(R.string.error_empty_vin), Snackbar.LENGTH_INDEFINITE)
                        .setAction("Action", null)
                        .setDuration(2000);
                mSnackbar.show();
                //Hide keyboard
                try {
                    InputMethodManager imm = (InputMethodManager) FullReportActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mainContent.getWindowToken(), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            mSnackbar = Snackbar.make(mainContent,  FullReportActivity.this.getResources().getString(R.string.error_no_internet), Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null)
                    .setDuration(2000);
            mSnackbar.show();
            //Hide keyboard
            try {
                InputMethodManager imm = (InputMethodManager) FullReportActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mainContent.getWindowToken(), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.buy_button)
    public void onBuyClicked(View view) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "inapp_report_start");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "1");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "FULL_REPORT");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        final RunCounts settings = new RunCounts();

        if (bp.isPurchased(FULL_REPORT)) {
            bp.consumePurchase(FULL_REPORT);
        } else {
            bp.purchase(this, FULL_REPORT, settings.getSSAD());
        }
    }

    @OnClick(R.id.success_card)
    public void onSuccessCardClicked(View view) {
        Intent openReportsActivity = new Intent(FullReportActivity.this, MyReportsActivity.class);
        FullReportActivity.this.startActivity(openReportsActivity);
    }

    @OnClick(R.id.open_example_report)
    public void onExampleCardClicked(View view) {
        Intent openExampleReportActivity = new Intent(FullReportActivity.this, ExampleReportActivity.class);
        FullReportActivity.this.startActivity(openExampleReportActivity);
    }

    @OnClick(R.id.no_vin_code)
    public void onNoVinClicked(View view) {
        Intent openPlateActivity = new Intent(FullReportActivity.this, PlateActivity.class);
        FullReportActivity.this.startActivity(openPlateActivity);
    }

    @OnClick(R.id.payments)
    protected void onPaymentsClicked() {
        paymentAlertDialog = new AlertDialog.Builder(FullReportActivity.this);
        paymentAlertDialog.setTitle(getString(R.string.payment_dialog_title));
        paymentAlertDialog.setMessage(getString(R.string.payment_dialog_message));
        paymentAlertDialog.setPositiveButton(getString(R.string.payment_dialog_open), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("https://antiperekup.com/"));
                    startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(FullReportActivity.this, getString(R.string.payment_dialog_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        paymentAlertDialog.setNegativeButton(getString(R.string.payment_dialog_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {}
        });
        paymentAlertDialog.setCancelable(true);

        paymentAlertDialog.show();
    }

    public void scroolToResult() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int y = metrics.heightPixels + 700;
        int x = 0;

        ObjectAnimator xTranslate = ObjectAnimator.ofInt(mainScrollView, "scrollX", x);
        ObjectAnimator yTranslate = ObjectAnimator.ofInt(mainScrollView, "scrollY", y);

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

    private void reloadWebView() {
        dismissProgressDialog();
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
            captchaWebView.setVisibility(View.GONE);
            final RelativeLayout samplesMain = (RelativeLayout) findViewById(R.id.root_layout);
            mSnackbar = Snackbar.make(samplesMain, FullReportActivity.this.getResources().getString(R.string.insurance_error), Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null)
                    .setDuration(2000);
            mSnackbar.show();
            progressView.setVisibility(View.GONE);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "received_error");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "GIBDD_SITE_REPORT");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "GIBDD_SITE_REPORT");
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
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "GIBDD_SITE_REPORT");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "GIBDD_SITE_REPORT");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }

        public void onPageFinished(android.webkit.WebView view, String url) {
            super.onPageFinished(view, url);
            if (hasError) {
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "success");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "GIBDD_SITE_REPORT");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "GIBDD_SITE_REPORT");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

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

                view.loadUrl("javascript:getCookie = function(name) {\n" +
                        "                    var r = document.cookie.match(\"\\\\b\" + name + \"=([^;]*)\\\\b\");\n" +
                        "                    return r ? r[1] : null;\n" +
                        "                };\n" +
                        "\n" +
                        "                var session = getCookie('session');" );

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
                        "      window.AutoinsInterface.make(appVehicleCheck.reCaptchaToken, session);\n" +
                        "   } else {\n" +
                        "      if (countRetry < maxCountRetry) {\n" +
                        "          window.setTimeout(\"checkVariable();\", 3000);\n" +
                        "      }\n" +
                        "   }\n" +
                        "}");
            }
        }
    }

    public class JavaScriptInterface {
        private WebView webView;

        public JavaScriptInterface(WebView webView) {
            this.webView = webView;
        }

        @JavascriptInterface
        public void make(String captcha_answer, String sessionId) throws Exception {
            autoinsWebViewClient.pageLoaded = true;
            if (captcha_answer != null && captcha_answer.length() > 0) {
                /* url, captcha, checktype, vin*/
                ArrayList<String> passing = new ArrayList<String>();
                passing.add(vinNumber);
                passing.add(sessionId);
                passing.add(captcha_answer);

                sendGibddRequest = new SendGibddRequest();

                try {
                    sendGibddRequest.execute(passing);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                captchaWebView.setVisibility(View.GONE);
                progressView.setVisibility(View.GONE);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "report_empty_captcha_answer");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "api_report");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "API_REPORT");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        }
    }
}