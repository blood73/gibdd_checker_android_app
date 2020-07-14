package ru.bloodsoft.gibddchecker.util;

import android.util.Log;
import org.riversun.okhttp3.OkHttp3CookieHelper;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.database.HistoryDatabaseHelper;
import ru.bloodsoft.gibddchecker.ui.base.BaseClassAct;

public class NewWebService {

    static {
        System.loadLibrary("native-lib");
    }

    public static native String getMsgFromJni();
    private static final String TAG = "NewWebService";
    private static final String CLIENT_TYPE = "android";
    public static final String APP_NAME = "autocheck_free";

    private static final OkHttpClient client = new OkHttpClient();
    private static OkHttp3CookieHelper cookieHelper = new OkHttp3CookieHelper();

    public static String sendNewHttpsPost(String requestURL, RequestBody requestBody) {

        RunCounts settings = new RunCounts();
        Boolean isAdFree = settings.isAdFree();
        String userType = null;
        if (isAdFree) {
            userType = "paid";
        } else {
            userType = "free";
        }

        HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(App.getContext());
        Integer mileageCountNumber = databaseHelper.getMileageCount();

        //md5(md5(uuid + secret) + secret2) all in uppercase

        //==gLmy9pOv7GlnOXXLk2zBUz
        String reverseKey2 = updateString(getPart() + DateHelper.getPart() + BaseClassAct.getPart());

        String signUUID = settings.getUUID().toUpperCase();
        String secretKey1 = updateString(getMsgFromJni()).toUpperCase();
        String secretKey2 = SanitizeHelper.decryptString(reverseKey2).toUpperCase();

        String md5String1 = (signUUID + secretKey1).toUpperCase();
        String hashPart1 = settings.getMD5(md5String1).toUpperCase();

        String md5String2 = hashPart1 + secretKey2;
        String sign = settings.getMD5(md5String2).toUpperCase();

        Request request = new Request.Builder()
                .url(requestURL)
                .header("User-Agent", System.getProperty("http.agent"))
                .addHeader(getHeader1(), settings.getUUID())
                .addHeader(getHeader2(), sign)
                .addHeader(getHeader3(), CLIENT_TYPE)
                .addHeader(getHeader4(), settings.getCurrentVersion())
                .addHeader(getHeader5(), UUID.randomUUID().toString())
                .addHeader(getHeader6(), userType)
                .addHeader(getHeader7(), Integer.toString(mileageCountNumber))
                .addHeader(getHeader8(), APP_NAME)
                .addHeader(getHeader9(), settings.getSSAD())
                .post(requestBody)
                .build();

        String responseString = "";

        try {
            OkHttpClient okHttpClient = client.newBuilder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

            Response response = okHttpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            responseString = response.body().string();
        } catch (Exception exception) {
            Log.w(TAG, "sendNewHttpsPost: " + exception.getMessage());
        }

        return responseString;
    }

    public static String sendPost(String requestURL, RequestBody requestBody) {
        return sendPostRequest(requestURL, requestBody, new SettingsStorage().getConnectionTimeout());
    }

    public static String sendPost(String requestURL, RequestBody requestBody, int connectionTimeout) {
        return sendPostRequest(requestURL, requestBody, connectionTimeout);
    }

    public static String sendPostRequest(String requestURL, RequestBody requestBody, int connectionTimeout) {

        Request request = new Request.Builder()
                .url(requestURL)
                .header("User-Agent", System.getProperty("http.agent"))
                .post(requestBody)
                .build();

        String responseString = "";

        try {
            OkHttpClient okHttpClient = client.newBuilder()
                    .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                    .readTimeout(connectionTimeout, TimeUnit.SECONDS)
                    .build();

            Response response = okHttpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            responseString = response.body().string();
        } catch (Exception exception) {
            Log.w(TAG, "sendPost: " + exception.getMessage());
        }

        return responseString;
    }

    public static String sendGet(String requestURL) {

        String responseString = "";

        Request request = new Request.Builder()
                .header("User-Agent", System.getProperty("http.agent"))
                .url(requestURL)
                .build();

        try {
            OkHttpClient okHttpClient = client.newBuilder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

            Response response = okHttpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            responseString = response.body().string();
        } catch (Exception exception) {
            Log.w(TAG, "sendGet: " + exception.getMessage());
        }

        return responseString;
    }

    public static InputStream sendGetStream(String requestURL) {

        InputStream responseStream = null;
        cookieHelper = new OkHttp3CookieHelper();

        Request request = new Request.Builder()
                .header("User-Agent", System.getProperty("http.agent"))
                .url(requestURL)
                .build();

        try {
            OkHttpClient okHttpClient = client.newBuilder()
                    .cookieJar(cookieHelper.cookieJar())
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

            Response response = okHttpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            responseStream = response.body().byteStream();
        } catch (Exception exception) {
            Log.w(TAG, "sendGetStream: " + exception.getMessage());
        }

        return responseStream;
    }

    public static String sendPostWithCookies(String requestURL, RequestBody requestBody) {

        Request request = new Request.Builder()
                .url(requestURL)
                .header("User-Agent", System.getProperty("http.agent"))
                .header("Content-Type", "  application/x-www-form-urlencoded; charset=UTF-8")
                .post(requestBody)
                .build();

        String responseString = "";

        try {
            OkHttpClient okHttpClient = client.newBuilder()
                    .cookieJar(cookieHelper.cookieJar())
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

            Response response = okHttpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            responseString = response.body().string();
        } catch (Exception exception) {
            Log.w(TAG, "sendPost: " + exception.getMessage());
        }

        return responseString;
    }

    private static String updateString(String string) {
        char[] array = string.toCharArray();
        int length = array.length;
        int half = (int) Math.floor(array.length / 2);
        for (int i = 0; i < half; i++) {
            array[i] ^= array[length - i - 1];
            array[length - i - 1] ^= array[i];
            array[i] ^= array[length - i - 1];
        }
        return String.valueOf(array);
    }

    private static String getPart() {
        return "==";
    }

    private static String getHeader1() {
        //AS-UUID
        return updateString("DIUU-SA");
    }

    private static String getHeader2() {
        //AS-SIGN
        return updateString("NGIS-SA");
    }

    private static String getHeader3() {
        //AS-CLIENT
        return updateString("TNEILC-SA");
    }

    private static String getHeader4() {
        //AS-VERSION
        return updateString("NOISREV-SA");
    }

    private static String getHeader5() {
        //X-REQUEST-ID
        return updateString("DI-TSEUQER-X");
    }

    private static String getHeader6() {
        //AS-USER-TYPE
        return updateString("EPYT-RESU-SA");
    }

    private static String getHeader7() {
        //AS-MILEAGE
        return updateString("EGAELIM-SA");
    }

    private static String getHeader8() {
        //AS-APP-NAME
        return updateString("EMAN-PPA-SA");
    }

    private static String getHeader9() {
        //AS-SSAD
        return updateString("DASS-SA");
    }
}
