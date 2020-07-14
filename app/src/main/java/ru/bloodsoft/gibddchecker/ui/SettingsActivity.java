package ru.bloodsoft.gibddchecker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.util.ArrayList;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.database.HistoryDatabaseHelper;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;
import ru.bloodsoft.gibddchecker.ui.quote.ListActivity;
import ru.bloodsoft.gibddchecker.util.ApiPurchaseVerifier;
import ru.bloodsoft.gibddchecker.util.AsyncResponse;
import ru.bloodsoft.gibddchecker.util.RunCounts;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class SettingsActivity extends BaseActivity implements BillingProcessor.IBillingHandler, AsyncResponse {

    private static Snackbar mSnackbar;
    private static final String TAG = makeLogTag(SettingsActivity.class);
    private static final String AD_FREE = "ad_free";

    private static final int USER_CANCEL_PURCHASE_CODE = 1;
    private static final int ITEM_ALREADY_OWNED = 7;

    BillingProcessor bp;
    private FirebaseAnalytics mFirebaseAnalytics;

    @BindView(R.id.main_content)
    LinearLayout mainContent;

    @BindView(R.id.text_disable_ad)
    TextView disableAdTextView;

    @BindView(R.id.content_disable_ad)
    TextView disableAdMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setupToolbar();

        bp = new BillingProcessor(this, App.getPublicKey(), ApiPurchaseVerifier.getMerchantId(), this);
        bp.initialize();

        RunCounts settings = new RunCounts();
        if (settings.isAdFree()) {
            disableAdTextView.setText(R.string.inapp_already);
            disableAdMessageTextView.setText("");
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(SettingsActivity.this);

    }

    @Override
    public void onBillingInitialized() {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */

        bp.loadOwnedPurchasesFromGoogle();
        bp.listOwnedProducts();
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        /*
         * Called when requested PRODUCT ID was successfully purchased
         */

        ArrayList<String> passing = new ArrayList<String>();
        passing.add(details.purchaseInfo.responseData);

        ApiPurchaseVerifier apiPurchaseVerifier = new ApiPurchaseVerifier();
        apiPurchaseVerifier.delegate = SettingsActivity.this;
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

        logD(TAG, "in-app error: " + Integer.toString(response));
        String errorText = "";
        if (response == USER_CANCEL_PURCHASE_CODE) {
            errorText = SettingsActivity.this.getResources().getString(R.string.cancelled);
        } else {
            errorText = SettingsActivity.this.getResources().getString(R.string.inapp_error);
        }
        mSnackbar = Snackbar.make(mainContent, errorText, Snackbar.LENGTH_INDEFINITE)
                .setAction("Action", null)
                .setDuration(2000);
        mSnackbar.show();

        if (response != USER_CANCEL_PURCHASE_CODE) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "inapp_adfree_error");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "error");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "ADFREE_ERROR");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }

        if (response == ITEM_ALREADY_OWNED) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "inapp_adfree_error");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "error");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "ADFREE_ERROR");
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

            if (bp.isPurchased(AD_FREE)) {
                RunCounts settings = new RunCounts();
                settings.setAdFree();

                disableAdTextView.setText(R.string.inapp_already);
                disableAdMessageTextView.setText("");

                mFirebaseAnalytics = FirebaseAnalytics.getInstance(App.getContext());
                mFirebaseAnalytics.setUserProperty("pro_status", "paid");
            } else {
                RunCounts settings = new RunCounts();
                settings.setNotAdFree();

                mFirebaseAnalytics = FirebaseAnalytics.getInstance(App.getContext());
                mFirebaseAnalytics.setUserProperty("pro_status", "free");
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
            RunCounts settings = new RunCounts();
            settings.setAdFree();

            ListActivity.getInstance().finish();

            disableAdTextView.setText(R.string.inapp_already);
            disableAdMessageTextView.setText("");

            mSnackbar = Snackbar.make(mainContent, SettingsActivity.this.getResources().getString(R.string.inapp_success), Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null)
                    .setDuration(2000);
            mSnackbar.show();
        } else {
            logD(TAG, "in-app error");

            RunCounts settings = new RunCounts();
            settings.setNotAdFree();

            String errorText = SettingsActivity.this.getResources().getString(R.string.inapp_error);

            mSnackbar = Snackbar.make(mainContent, errorText, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null)
                    .setDuration(2000);
            mSnackbar.show();

        }
    }

    public void purchaseAdFree() {
        RunCounts settings = new RunCounts();
        bp.purchase(this, AD_FREE, settings.getSSAD());
    }

    private void setupToolbar() {
        final ActionBar ab = getActionBarToolbar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_settings;
    }

    @Override
    protected void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }

    @OnClick(R.id.pref_settings_1_key)
    protected void onClick1() {
        HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(SettingsActivity.this);
        databaseHelper.deleteAllHistory();

        if (mainContent != null) {
            mSnackbar = Snackbar.make(mainContent, SettingsActivity.this.getResources().getString(R.string.history_clear), Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null)
                    .setDuration(2000);
            mSnackbar.show();
        }
    }

    @OnClick(R.id.pref_settings_2_key)
    protected void onClick2() {
        Intent i = new Intent(App.getContext(), IntroActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.pref_settings_3_key)
    protected void onClick3() {
        RunCounts settings = new RunCounts();
        Boolean isAdFree = settings.isAdFree();

        if (!isAdFree) {
            purchaseAdFree();
        } else {
            if (mainContent != null) {
                mSnackbar = Snackbar.make(mainContent, SettingsActivity.this.getResources().getString(R.string.inapp_thanks), Snackbar.LENGTH_INDEFINITE)
                        .setAction("Action", null)
                        .setDuration(2000);
                mSnackbar.show();
            }
        }
    }
}