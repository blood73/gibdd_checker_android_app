package ru.bloodsoft.gibddchecker.util;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class ApiNotificationTokenAsyncTask extends AsyncTask<ArrayList<String>, Boolean, Boolean> {

    private static final String TAG = "ApiNotificationTokenAsyncTask";
    //https://antiperekup.net/api/v1/update_token/
    public static final String API_URL = SanitizeHelper.decryptString("sdfgdf/Tk3kPCtZ2WrcO/xCNc7BAYTxIk5KRV8");
    private NewWebService newWebService;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(ArrayList<String>... passing) {

        ArrayList<String> passed = passing[0]; //get passed arraylist
        Object[] mStringArray = passed.toArray();


        String token = (String) mStringArray[0];

        String response = "";

        newWebService = new NewWebService();

        RunCounts settings = new RunCounts();

        RequestBody formBody = new FormBody.Builder()
                .add("ssad", settings.getSSAD())
                .add("app_name", NewWebService.APP_NAME)
                .add("token", token)
                .build();

        String apiResponse = newWebService.sendNewHttpsPost(API_URL, formBody);

        JSONObject responseJsonObject = null;
        JSONObject responseJson = null;
        String gibddSetting = "";

        try {
            responseJsonObject = new JSONObject(apiResponse);
            responseJson = responseJsonObject.optJSONObject("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /*try {
            responseJson = responseJson.optJSONObject("settings");
            gibddSetting = responseJson.optString("use_gibdd_without_captcha");

            RunCounts runCounts = new RunCounts();
            runCounts.setUseGibddWithoutCaptcha(gibddSetting);

        } catch (Exception e) {
            e.printStackTrace();
        }*/

        return true;
    }

    @Override
    protected void onPostExecute(Boolean purchaseResult) {

    }

}