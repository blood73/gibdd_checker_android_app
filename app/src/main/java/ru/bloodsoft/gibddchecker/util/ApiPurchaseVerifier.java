package ru.bloodsoft.gibddchecker.util;

import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;

public class ApiPurchaseVerifier extends AsyncTask<ArrayList<String>, Boolean, Boolean> {

    private static final String TAG = "ApiPurchaseVerifier";

    public static final String API_URL = SanitizeHelper.decryptString("GsM7ShuIHsE0vEeI6xq4RsVEO8x1E8U3idGIkl5oaFJV4aKJIIbCMESY1Naf4mS9");
    private NewWebService newWebService;

    public AsyncResponse delegate = null;

    public static String getMerchantId() {
        return "00396829761213182714";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(ArrayList<String>... passing) {

        ArrayList<String> passed = passing[0]; //get passed arraylist
        Object[] mStringArray = passed.toArray();


        String jsonPayload = (String) mStringArray[0];

        String response = "";

        newWebService = new NewWebService();

        RequestBody formBody = new FormBody.Builder()
                .add("payload", jsonPayload)
                .build();

        String apiResponse = newWebService.sendNewHttpsPost(API_URL, formBody);

        JSONObject responseJsonObject = null;
        JSONObject responseJson = null;
        boolean purchaseResult = false;
        String purchaseText = "";

        try {
            responseJsonObject = new JSONObject(apiResponse);
            responseJson = responseJsonObject.optJSONObject("data");

            purchaseResult = responseJson.optBoolean("purchase_status");
            purchaseText = responseJson.optString("purchase_message");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        logD(TAG, "purchase_status: " + Boolean.toString(purchaseResult));
        logD(TAG, "purchase_message: " + purchaseText);

        return purchaseResult;
    }

    @Override
    protected void onPostExecute(Boolean purchaseResult) {
        delegate.processFinish(purchaseResult);
    }

}