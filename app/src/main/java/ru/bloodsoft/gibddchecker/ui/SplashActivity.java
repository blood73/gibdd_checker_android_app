package ru.bloodsoft.gibddchecker.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.ui.quote.ListActivity;
import ru.bloodsoft.gibddchecker.util.ApiNotificationTokenAsyncTask;
import ru.bloodsoft.gibddchecker.util.ApiPurchaseVerifier;
import ru.bloodsoft.gibddchecker.util.AsyncResponse;
import ru.bloodsoft.gibddchecker.util.RunCounts;
import ru.bloodsoft.gibddchecker.util.SettingsStorage;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logW;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class SplashActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler, AsyncResponse {

    private static final String TAG = makeLogTag(SplashActivity.class);
    private FirebaseAnalytics mFirebaseAnalytics;
    private ConsentForm consentForm;

    private static final String AD_FREE = "ad_free";

    BillingProcessor bp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RunCounts runCounts = new RunCounts();
        runCounts.increaseRunAppCount(SplashActivity.this);

        SettingsStorage settings = new SettingsStorage();
        settings.remoteConfigInit();

        bp = new BillingProcessor(this, App.getPublicKey(), ApiPurchaseVerifier.getMerchantId(), this);
        bp.initialize();

        Boolean inEea = ConsentInformation.getInstance(SplashActivity.this).isRequestLocationInEeaOrUnknown();
        //inEea = true;
        //ConsentInformation.getInstance(this).addTestDevice("6F6C13A9BB87DD21E3ED3624AE624213");
        //ConsentInformation.getInstance(this).addTestDevice("E31198FB67C1181AF2CFCEDF4476A9D1");
        //ConsentInformation.getInstance(this).setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);

        ConsentInformation consentInformation = ConsentInformation.getInstance(SplashActivity.this);
        String[] publisherIds = {"pub-3078563819949367"};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                // User's consent status successfully updated.
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                // User's consent status failed to update.
            }
        });

        //update notification token
        if (isConnectedToInternet()) {
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                logW(TAG, "getInstanceId failed", task.getException());
                                return;
                            }

                            // Get new Instance ID token
                            String token = task.getResult().getToken();

                            ArrayList<String> passing = new ArrayList<String>();
                            passing.add(token);

                            ApiNotificationTokenAsyncTask apiNotificationTokenAsyncTask = new ApiNotificationTokenAsyncTask();
                            apiNotificationTokenAsyncTask.execute(passing);
                        }
                    });
        }

        if (inEea && !runCounts.getConsentUserExists(SplashActivity.this)) {
            URL privacyUrl = null;
            try {
                privacyUrl = new URL("https://vk.com/antiperekup_app?w=page-142951735_54907591");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            consentForm = new ConsentForm.Builder(SplashActivity.this, privacyUrl)
                    .withListener(new ConsentFormListener() {
                        @Override
                        public void onConsentFormLoaded() {
                            // Consent form loaded successfully.
                            if (!SplashActivity.this.isFinishing()) {
                                consentForm.show();
                            }
                        }

                        @Override
                        public void onConsentFormOpened() {
                            // Consent form was displayed.
                        }

                        @Override
                        public void onConsentFormClosed(
                                ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                            // Consent form was closed.
                            updateUserConsent(consentStatus == ConsentStatus.NON_PERSONALIZED);
                            navigateToApp();
                        }

                        @Override
                        public void onConsentFormError(String errorDescription) {
                            // Consent form error.
                            navigateToApp();
                        }
                    })
                    .withPersonalizedAdsOption()
                    .withNonPersonalizedAdsOption()
                    .build();

            consentForm.load();
        } else {
            navigateToApp();
        }
    }

    private void updateUserConsent (Boolean isNonPersonalizedAds) {
        RunCounts runCounts = new RunCounts();
        runCounts.setConsentUser(SplashActivity.this, isNonPersonalizedAds);
        runCounts.setConsentUserExists(SplashActivity.this, true);
    }

    private void navigateToApp() {
        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                RunCounts runCounts = new RunCounts();
                boolean isPolicyAccepted = runCounts.isPolicyAccepted();

                //  If the activity has never started before...
                if (isFirstStart || !isPolicyAccepted) {

                    //  Launch app intro
                    Intent i = new Intent(SplashActivity.this, IntroActivity.class);
                    startActivity(i);

                    //  Make a new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();

                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);

                    //  Apply changes
                    e.apply();
                } else {
                    Intent intent = new Intent(SplashActivity.this, ListActivity.class);
                    startActivity(intent);
                }
            }
        });

        // Start the thread
        t.start();
        finish();

    }

    public boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    @Override
    public void onBillingInitialized() {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */

        bp.loadOwnedPurchasesFromGoogle();
        bp.listOwnedProducts();

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

        ArrayList<String> passing = new ArrayList<String>();
        passing.add(details.purchaseInfo.responseData);

        ApiPurchaseVerifier apiPurchaseVerifier = new ApiPurchaseVerifier();
        apiPurchaseVerifier.delegate = SplashActivity.this;
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
    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
        for (String sku : bp.listOwnedProducts()) {
            logD(TAG, "Owned Managed Product: " + sku);

            if (sku.equals(AD_FREE)) {
                if (bp.isPurchased(AD_FREE)) {
                    RunCounts settings = new RunCounts();
                    settings.setAdFree();

                    mFirebaseAnalytics = FirebaseAnalytics.getInstance(App.getContext());
                    mFirebaseAnalytics.setUserProperty("pro_status", "paid");
                } else {
                    RunCounts settings = new RunCounts();
                    settings.setNotAdFree();

                    mFirebaseAnalytics = FirebaseAnalytics.getInstance(App.getContext());
                    mFirebaseAnalytics.setUserProperty("pro_status", "free");
                }
            } else {
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
            RunCounts settings = new RunCounts();
            settings.setAdFree();

        } else {
            logD(TAG, "in-app error");

            RunCounts settings = new RunCounts();
            settings.setNotAdFree();

        }
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }
}