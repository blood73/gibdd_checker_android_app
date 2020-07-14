package ru.bloodsoft.gibddchecker.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import ru.bloodsoft.gibddchecker.App;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;


public class RunCounts {
    private final String PREFS_NAME = "GibddPrefsFile";
    private static final String TAG = makeLogTag(RunCounts.class);
    private Context context;
    private FirebaseAnalytics mFirebaseAnalytics;

    public Integer getRunAppCount (Context context) {
        this.context = context;
        return getPrefs(context).getInt("app_run_count", 0);
    }

    public void increaseRunAppCount (Context context) {
        this.context = context;
        SharedPreferences.Editor editor = getPrefs(context).edit();
        Integer appRunCount =  this.getRunAppCount(context);

        Integer sumRunApp = addWithOverflowCheck(appRunCount, 1, "app_run_count");
        logD(TAG, "Number of run: " + sumRunApp.toString());
        editor.putInt("app_run_count", sumRunApp);
        editor.apply();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        mFirebaseAnalytics.setUserProperty("app_run", sumRunApp.toString());
    }

    public Integer getCheckAutoCount (Context context) {
        return getPrefs(context).getInt("auto_check_count", 0);
    }

    public String getUserRegion (Context context) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        String userRegion = getPrefs(context).getString("user_region", "");
        mFirebaseAnalytics.setUserProperty("user_region", userRegion);
        return getPrefs(context).getString("user_region", "");
    }

    public void setUserRegion (Context context, String userRegion) {
        this.context = context;
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString("user_region", userRegion);
        editor.apply();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        mFirebaseAnalytics.setUserProperty("user_region", userRegion);
    }

    public Boolean isAdFree () {
        return getPrefs(App.getContext()).getBoolean("ad_status", false);
    }

    public Boolean isPolicyAccepted () {
        return getPrefs(App.getContext()).getBoolean("isPolicyAccepted", false);
    }

    public void setPolicyAccepted () {
        SharedPreferences.Editor editor = getPrefs(App.getContext()).edit();
        editor.putBoolean("isPolicyAccepted", true);
        editor.apply();
    }

    public void setAdFree () {
        SharedPreferences.Editor editor = getPrefs(App.getContext()).edit();
        editor.putBoolean("ad_status", true);
        editor.apply();
    }

    public void setNotAdFree () {
        SharedPreferences.Editor editor = getPrefs(App.getContext()).edit();
        editor.putBoolean("ad_status", false);
        editor.apply();
    }

    public void increaseCheckAutoCount (Context context) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        Integer autoCheckCount =  this.getCheckAutoCount(context);

        Integer sumCheckCount = addWithOverflowCheck(autoCheckCount, 1, "auto_check_count");
        logD(TAG, "Number of check: " + sumCheckCount.toString());
        editor.putInt("auto_check_count", sumCheckCount);
        editor.apply();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        mFirebaseAnalytics.setUserProperty("number_check", sumCheckCount.toString());
    }

    public Boolean getConsentUser (Context context) {
        return getPrefs(context).getBoolean("is_non_personalized_ads", false);
    }

    public void setConsentUser (Context context, boolean isNonPersonalizedAds) {
        this.context = context;
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean("is_non_personalized_ads", isNonPersonalizedAds);
        editor.apply();
    }

    public Boolean getConsentUserExists (Context context) {
        return getPrefs(context).getBoolean("is_consent_exists", false);
    }

    public void setConsentUserExists (Context context, boolean isConsentExists) {
        this.context = context;
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean("is_consent_exists", isConsentExists);
        editor.apply();
    }

    public SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private int addWithOverflowCheck(int a, int b, String setting) {
        // the cast of a is required, to make the + work with long precision,
        // if we just added (a + b) the addition would use int precision and
        // the result would be cast to long afterwards!
        long result = ((long) a) + b;
        if (result > Integer.MAX_VALUE) {
            SharedPreferences.Editor editor = getPrefs(context).edit();
            editor.putInt(setting, 0);
            editor.apply();
            result = 0;
        } else if (result < Integer.MIN_VALUE) {
            SharedPreferences.Editor editor = getPrefs(context).edit();
            editor.putInt(setting, 0);
            editor.apply();
            result = 0;
        }
        // at this point we can safely cast back to int, we checked before
        // that the value will be withing int's limits
        return (int) result;
    }

    public String getUUID() {
        String uniqueID = getPrefs(App.getContext()).getString("UUID", null);

        if (uniqueID == null) {
            uniqueID = UUID.randomUUID().toString();

            SharedPreferences.Editor editor = getPrefs(App.getContext()).edit();
            editor.putString("UUID", uniqueID);
            editor.apply();
        }

        return uniqueID.toUpperCase();
    }

    public String getSSAD() {
        String uniqueID = getPrefs(App.getContext()).getString("SSAD", null);

        if (uniqueID == null) {
            uniqueID = Settings.Secure.getString(App.getApplication().getContentResolver(), Settings.Secure.ANDROID_ID);
            if (uniqueID == null) {
                uniqueID = getUUID();
            }

            if (uniqueID.equals("null")) {
                uniqueID = getUUID();
            }

            SharedPreferences.Editor editor = getPrefs(App.getContext()).edit();
            editor.putString("SSAD", uniqueID);
            editor.apply();
        }

        return uniqueID.toUpperCase();
    }

    public String getMD5(String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getCurrentVersion() {
        try {
            PackageInfo packageInfo = App.getContext().getPackageManager()
                    .getPackageInfo(App.getContext().getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }
}