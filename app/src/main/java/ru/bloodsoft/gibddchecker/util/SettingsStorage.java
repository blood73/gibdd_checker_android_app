package ru.bloodsoft.gibddchecker.util;

import android.content.Context;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import ru.bloodsoft.gibddchecker.R;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class SettingsStorage {

    private Context context;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private static final String TAG = makeLogTag(SettingsStorage.class);
    private static final String REQUEST_PER_AD_CONFIG_KEY = "request_per_ad";
    private static final String REQUEST_PER_AD_2_CONFIG_KEY = "request_per_ad_2";
    private static final String CONNECTION_TIMEOUT_MS_CONFIG_KEY = "connection_timeout_ms";
    private static final String RATE_APP_NUMBER_DAYS_CONFIG_KEY = "rate_app_number_of_days_before_show_dialog";
    private static final String SHOW_PHONE_SEARCH_CONFIG_KEY = "show_phone_search";
    private static final String SHOW_VIN_DECODER_CONFIG_KEY = "show_vin_decoder";
    private static final String SHOW_MILEAGE_SEARCH_CONFIG_KEY = "show_vin_decoder";

    public Boolean isShowInterstitial (Context context) {
        this.context = context;
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        RunCounts requestCounts = new RunCounts();
        Integer numberRequests = requestCounts.getCheckAutoCount(this.context);

        return numberRequests % mFirebaseRemoteConfig.getLong(REQUEST_PER_AD_CONFIG_KEY) == 0;
    }

    public Boolean isShowInterstitialSecond (Context context) {
        this.context = context;
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        RunCounts requestCounts = new RunCounts();
        Integer numberRequests = requestCounts.getCheckAutoCount(this.context);

        return numberRequests % mFirebaseRemoteConfig.getLong(REQUEST_PER_AD_2_CONFIG_KEY) == 0;
    }

    public Integer getConnectionTimeout () {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        return (int) mFirebaseRemoteConfig.getLong(CONNECTION_TIMEOUT_MS_CONFIG_KEY);
    }

    public Integer getRateAppNumberDays () {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        return (int) mFirebaseRemoteConfig.getLong(RATE_APP_NUMBER_DAYS_CONFIG_KEY);
    }

    public Boolean showPhoneSearch () {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        return (boolean) mFirebaseRemoteConfig.getBoolean(SHOW_PHONE_SEARCH_CONFIG_KEY);
    }

    public Boolean showVinDecoder () {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        return (boolean) mFirebaseRemoteConfig.getBoolean(SHOW_VIN_DECODER_CONFIG_KEY);
    }

    public Boolean showMileageSearch () {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        return (boolean) mFirebaseRemoteConfig.getBoolean(SHOW_MILEAGE_SEARCH_CONFIG_KEY);
    }

    /*
     * Runned when app is starting
     */
    public void remoteConfigInit () {
        // Get Remote Config instance.
        // [START get_remote_config_instance]
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        // [END get_remote_config_instance]

        // Create a Remote Config Setting to enable developer mode, which you can use to increase
        // the number of fetches available per hour during development. See Best Practices in the
        // README for more information.
        // [START enable_dev_mode]
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                //.setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        // [END enable_dev_mode]

        // Set default Remote Config parameter values. An app uses the in-app default values, and
        // when you need to adjust those defaults, you set an updated value for only the values you
        // want to change in the Firebase console. See Best Practices in the README for more
        // information.
        // [START set_default_values]
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        // [END set_default_values]

        long cacheExpiration = 3600; // 1 hour in seconds.
        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        // [START fetch_config_with_callback]
        // cacheExpirationSeconds is set to cacheExpiration here, indicating the next fetch request
        // will use fetch data from the Remote Config service, rather than cached parameter values,
        // if cached parameter values are more than cacheExpiration seconds old.
        // See Best Practices in the README for more information.

        mFirebaseRemoteConfig.fetch(cacheExpiration);
        mFirebaseRemoteConfig.activateFetched();
        // [END fetch_config_with_callback]
    }
}