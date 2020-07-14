package ru.bloodsoft.gibddchecker;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;
import com.yandex.metrica.push.YandexMetricaPush;

import ru.bloodsoft.gibddchecker.util.SanitizeHelper;

public class App extends MultiDexApplication {

    private static Application sApplication;

    @Override
    public void onCreate() {
        super.onCreate();

        sApplication = this;

        YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder("dsfg").build();
        // Initializing the AppMetrica SDK.
        YandexMetrica.activate(getApplicationContext(), config);
        // Automatic tracking of user activity.
        YandexMetrica.enableActivityAutoTracking(this);
        YandexMetricaPush.init(getApplicationContext());
    }

    public static Application getApplication() {
        return sApplication;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    /**
     * Returns an instance of {@link App} attached to the passed activity.
     */
    public static App get(Activity activity) {
        return (App) activity.getApplication();
    }

    public static String getPublicKey() {
        String encodedPublicKeyPart1 = SanitizeHelper.decryptString("sdf/DieEFduF6uBeHJQEt3cRGFLhDQ5n0eN6tD5vTwjQQcY1k9XnJ7Oi9UcK3c7+IzZ4hvEuUSufrBIel9IOhzWZB1nHPKva3G2B3kjoEcI4aEWURWw=");
        String encodedPublicKeyPart2 = SanitizeHelper.decryptString("df/sRfVHRafW+h/Lwviqjr50JmxjIxHb49YdIILp6cb6PRkPsC/+U4xy6KP48qUpxBoYKmDX+j7N0qCPjhFJikPMk+EdODsDCEJPyFH578O5D+f39c/IO3K3XhJZ22/ObQiZqr0tQeUi2s=");
        String encodedPublicKeyPart3 = SanitizeHelper.decryptString("dfg/hgh=");
        String encodedPublicKeyPart4 = SanitizeHelper.decryptString("dgsdf/");

        String base64EncodedPublicKey = encodedPublicKeyPart1 + encodedPublicKeyPart2 +
                encodedPublicKeyPart3 + encodedPublicKeyPart4;

        return base64EncodedPublicKey;
    }

}