package ru.bloodsoft.gibddchecker.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.firebase.analytics.FirebaseAnalytics;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.database.HistoryDatabaseHelper;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;
import ru.bloodsoft.gibddchecker.util.ApiPurchaseVerifier;
import ru.bloodsoft.gibddchecker.util.AsyncResponse;
import ru.bloodsoft.gibddchecker.util.NewWebService;
import ru.bloodsoft.gibddchecker.util.RegexUtil;
import ru.bloodsoft.gibddchecker.util.RunCounts;
import ru.bloodsoft.gibddchecker.util.SanitizeHelper;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class MileageInappActivity extends BaseActivity implements BillingProcessor.IBillingHandler, AsyncResponse {

    @BindView(R.id.mileage_count_text)
    TextView mileageCountText;

    @BindView(R.id.card_1)
    CardView cardCount1;

    @BindView(R.id.card_2)
    CardView cardCount2;

    @BindView(R.id.card_5)
    CardView cardCount5;

    @BindView(R.id.card_10)
    CardView cardCount10;

    @BindView(R.id.price_1)
    TextView price1Text;

    @BindView(R.id.price_2)
    TextView price2Text;

    @BindView(R.id.price_old_2)
    TextView price2OldText;

    @BindView(R.id.discount_2)
    TextView price2DiscountText;

    @BindView(R.id.price_5)
    TextView price5Text;

    @BindView(R.id.price_old_5)
    TextView price5OldText;

    @BindView(R.id.discount_5)
    TextView price5DiscountText;

    @BindView(R.id.price_10)
    TextView price10Text;

    @BindView(R.id.price_old_10)
    TextView price10OldText;

    @BindView(R.id.discount_10)
    TextView price10DiscountText;

    @BindView(R.id.main_content)
    CoordinatorLayout mainContent;

    @BindView(R.id.send_message)
    CardView sendMessage;

    static {
        System.loadLibrary("native-lib");
    }

    public static native String getCheckUserUrl();

    ProgressDialog mProgressDialog;
    private Snackbar mSnackbar;

    BillingProcessor bp;

    private FirebaseAnalytics mFirebaseAnalytics;

    private static final String MILEAGE_1 = "mileage_1";
    private static final String MILEAGE_2 = "mileage_2";
    private static final String MILEAGE_5 = "mileage_5";
    private static final String MILEAGE_10 = "mileage_10";
    private static final String AD_FREE = "ad_free";

    private static final int MILEAGE_1_PRICE = 49;
    private static final int MILEAGE_2_PRICE = 85;
    private static final int MILEAGE_5_PRICE = 209;
    private static final int MILEAGE_10_PRICE = 399;
    private static final String TAG = makeLogTag(MileageInappActivity.class);
    private String ERROR_CODE = "";
    private String ERROR_MESSAGE = "";
    private static final int USER_CANCEL_PURCHASE_CODE = 1;
    private static final int ITEM_ALREADY_OWNED = 7;

    NewWebService getMileageRequest;
    GetUserData getUserData;

    int mileageCountNumber;

    //https://antiperekup.net/api/v1/check_user/
    private static final String URL_CHECK_USER = SanitizeHelper.decryptString(getCheckUserUrl());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mileage_inapp);
        ButterKnife.bind(this);
        setupToolbar();

        bp = new BillingProcessor(this, App.getPublicKey(), ApiPurchaseVerifier.getMerchantId(), this);
        bp.initialize();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(MileageInappActivity.this);
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
            initPrices(MILEAGE_1_PRICE, MILEAGE_2_PRICE, MILEAGE_5_PRICE, MILEAGE_10_PRICE);
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

        bp.consumePurchase(productId);

        if (productId.equals(MILEAGE_1)) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "inapp_mileage_finish");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "1");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "MILEAGE_1");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        } else if (productId.equals(MILEAGE_2)) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "inapp_mileage_finish");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "2");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "MILEAGE_2");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        } else if (productId.equals(MILEAGE_5)) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "inapp_mileage_finish");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "5");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "MILEAGE_5");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        } else if (productId.equals(MILEAGE_10)) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "inapp_mileage_finish");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "10");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "MILEAGE_10");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }

        ArrayList<String> passing = new ArrayList<String>();
        passing.add(details.purchaseInfo.responseData);

        ApiPurchaseVerifier apiPurchaseVerifier = new ApiPurchaseVerifier();
        apiPurchaseVerifier.delegate = MileageInappActivity.this;
        apiPurchaseVerifier.execute(passing);

        updateMileageCount();

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

        logD(TAG, "in-app error: " + Integer.toString(response));
        String errorText = "";
        if (response == USER_CANCEL_PURCHASE_CODE) {
            errorText = MileageInappActivity.this.getResources().getString(R.string.cancelled);
        } else {
            errorText = MileageInappActivity.this.getResources().getString(R.string.inapp_error);
        }
        mSnackbar = Snackbar.make(mainContent, errorText, Snackbar.LENGTH_INDEFINITE)
                .setAction("Action", null)
                .setDuration(2000);
        mSnackbar.show();

        if (response != USER_CANCEL_PURCHASE_CODE) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "inapp_mileage_error");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "error");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "MILEAGE_ERROR");
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
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "inapp_mileage_error");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "error");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "MILEAGE_ERROR");
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

        if (result) {
            mSnackbar = Snackbar.make(mainContent, MileageInappActivity.this.getResources().getString(R.string.inapp_success), Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null)
                    .setDuration(2000);
            mSnackbar.show();
        } else {
            logD(TAG, "in-app error");
            String errorText = MileageInappActivity.this.getResources().getString(R.string.inapp_error);

            mSnackbar = Snackbar.make(mainContent, errorText, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null)
                    .setDuration(2000);
            mSnackbar.show();

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "inapp_mileage_error");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "error");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "MILEAGE_ERROR");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_END, bundle);

            sendMessage.setVisibility(View.VISIBLE);
            ERROR_CODE = "666";
            ERROR_MESSAGE = "Can't validate purchase";

        }
    }

    public void updateInventory(List<SkuDetails> products) {

        int price1 = MILEAGE_1_PRICE;
        int price2 = MILEAGE_2_PRICE;
        int price5 = MILEAGE_5_PRICE;
        int price10 = MILEAGE_10_PRICE;

        for (SkuDetails skuDetails : products) {
            if (skuDetails.productId.equals(MILEAGE_1)) {
                try {
                    price1 = Integer.parseInt(RegexUtil.extractFirstNumber(skuDetails.priceText));
                } catch (Exception e) {
                    price1 = MILEAGE_1_PRICE;
                }
            } else if (skuDetails.productId.equals(MILEAGE_2)) {
                try {
                    price2 = Integer.parseInt(RegexUtil.extractFirstNumber(skuDetails.priceText));
                } catch (Exception e) {
                    price2 = MILEAGE_2_PRICE;
                }
            } else if (skuDetails.productId.equals(MILEAGE_5)) {
                try {
                    price5 = Integer.parseInt(RegexUtil.extractFirstNumber(skuDetails.priceText));
                } catch (Exception e) {
                    price5 = MILEAGE_5_PRICE;
                }
            } else if (skuDetails.productId.equals(MILEAGE_10)) {
                try {
                    price10 = Integer.parseInt(RegexUtil.extractFirstNumber(skuDetails.priceText));
                } catch (Exception e) {
                    price10 = MILEAGE_10_PRICE;
                }
            }
        }

        initPrices(price1, price2, price5, price10);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateMileageCount();
    }

    private void showProgressDialog() {
        // instantiate it within the onCreate method
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(MileageInappActivity.this);
            mProgressDialog.setMessage(MileageInappActivity.this.getResources().getString(R.string.loading));
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
        skus.addAll(Arrays.asList(MILEAGE_1, MILEAGE_2, MILEAGE_5, MILEAGE_10));
        return skus;
    }


    @OnClick(R.id.send_message)
    protected void onSendErrorClicked() {
        Toast.makeText(MileageInappActivity.this, MileageInappActivity.this.getResources().getString(R.string.rate_app_feedback_sent), Toast.LENGTH_SHORT).show();
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

    @OnClick(R.id.card_1)
    protected void onClickCard1() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "inapp_mileage_start");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "1");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "MILEAGE_1");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        final RunCounts settings = new RunCounts();

        if (bp.isPurchased(MILEAGE_1)) {
            bp.consumePurchase(MILEAGE_1);
        } else {
            bp.purchase(this, MILEAGE_1, settings.getSSAD());
        }
    }

    @OnClick(R.id.card_2)
    protected void onClickCard2() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "inapp_mileage_start");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "2");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "MILEAGE_2");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        RunCounts settings = new RunCounts();

        if (bp.isPurchased(MILEAGE_2)) {
            bp.consumePurchase(MILEAGE_2);
        } else {
            bp.purchase(this, MILEAGE_2, settings.getSSAD());
        }
    }

    @OnClick(R.id.card_5)
    protected void onClickCard5() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "inapp_mileage_start");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "1");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "MILEAGE_5");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        RunCounts settings = new RunCounts();

        if (bp.isPurchased(MILEAGE_5)) {
            bp.consumePurchase(MILEAGE_5);
        } else {
            bp.purchase(this, MILEAGE_5, settings.getSSAD());
        }

    }

    @OnClick(R.id.card_10)
    protected void onClickCard10() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "inapp_mileage_start");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "10");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "MILEAGE_10");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        RunCounts settings = new RunCounts();


        if (bp.isPurchased(MILEAGE_10)) {
            bp.consumePurchase(MILEAGE_10);
        } else {
            bp.purchase(this, MILEAGE_10, settings.getSSAD());
        }
    }



    private void updateMileageCount() {
        ArrayList<String> passing = new ArrayList<String>();
        getUserData = new GetUserData();
        getUserData.execute(passing);
    }

    private void initPrices(int price1, int price2, int price5, int price10) {

        /* PRICE 1 */
        String price1String = MileageInappActivity.this.getResources().getString(R.string.price, price1);
        price1Text.setText(price1String);
        /* /PRICE 1 */

        /* PRICE 2 */
        int price2Old = price1 * 2;
        double discount2 = (1 - ((double) price2 / price2Old)) * 100;

        String price2String = MileageInappActivity.this.getResources().getString(R.string.price, price2);
        String price2OldString = MileageInappActivity.this.getResources().getString(R.string.price, price2Old);
        String price2DiscountString = MileageInappActivity.this.getResources().getString(R.string.discount, (int) discount2) + " %";

        price2Text.setText(price2String);
        price2OldText.setText(price2OldString);
        price2DiscountText.setText(price2DiscountString);
        if (discount2 <= 0) {
            price2DiscountText.setVisibility(View.GONE);
        } else {
            price2DiscountText.setVisibility(View.VISIBLE);
        }
        /* /PRICE 2 */

        /* PRICE 5 */
        int price5Old = price1 * 5;
        double discount5 = (1 - ((double) price5 / price5Old)) * 100;

        String price5String = MileageInappActivity.this.getResources().getString(R.string.price, price5);
        String price5OldString = MileageInappActivity.this.getResources().getString(R.string.price, price5Old);
        String price5DiscountString = MileageInappActivity.this.getResources().getString(R.string.discount, (int) discount5) + " %";

        price5Text.setText(price5String);
        price5OldText.setText(price5OldString);
        price5DiscountText.setText(price5DiscountString);
        if (discount5 <= 0) {
            price5DiscountText.setVisibility(View.GONE);
        } else {
            price5DiscountText.setVisibility(View.VISIBLE);
        }
        /* /PRICE 5 */

         /* PRICE 10 */
        int price10Old = price1 * 10;
        double discount10 = (1 - ((double) price10 / price10Old)) * 100;

        String price10String = MileageInappActivity.this.getResources().getString(R.string.price, price10);
        String price10OldString = MileageInappActivity.this.getResources().getString(R.string.price, price10Old);
        String price10DiscountString = MileageInappActivity.this.getResources().getString(R.string.discount, (int) discount10) + " %";

        price10Text.setText(price10String);
        price10OldText.setText(price10OldString);
        price10DiscountText.setText(price10DiscountString);
        if (discount10 <= 0) {
            price10DiscountText.setVisibility(View.GONE);
        } else {
            price10DiscountText.setVisibility(View.VISIBLE);
        }
        /* /PRICE 10 */
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

    @OnClick(R.id.update_mileage)
    public void onUpdateMileageClicked(View view) {
        updateMileageCount();
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
        return R.id.nav_mileage_inapp;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }

    private class GetUserData extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
            mileageCountText.setText(MileageInappActivity.this.getResources().getString(R.string.updating));
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

            if (MileageInappActivity.this.isFinishing()) {
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

                HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(MileageInappActivity.this);
                databaseHelper.updateMileageCount(mileageCountNumber);

                String inAppCount = MileageInappActivity.this.getResources().getQuantityString(R.plurals.mileage_inapp_count,
                        mileageCountNumber, mileageCountNumber);

                mileageCountText.setText(inAppCount);
                if (mileageCountNumber == 0) {
                    mileageCountText.setText(MileageInappActivity.this.getResources().getString(R.string.no_mileage_requests_buy));
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                mileageCountText.setText(MileageInappActivity.this.getResources().getString(R.string.no_mileage_requests_buy));
            }

        }
    }

    public boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }
}