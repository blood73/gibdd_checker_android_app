package ru.bloodsoft.gibddchecker.util;

import android.os.AsyncTask;
import java.util.ArrayList;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class UpdateVehicleAsyncTask extends AsyncTask<ArrayList<String>, Boolean, Boolean> {

    private static final String TAG = "UpdateVehicleAsyncTask";

    public static final String API_URL = SanitizeHelper.decryptString("GsM7ShuIHsE0vEeI6xq4RjXcLKYGcqZc/Tk3kPCtZ2VmsJyg6MVx7Ha0Lc35azRx");
    private NewWebService newWebService;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(ArrayList<String>... passing) {

        ArrayList<String> passed = passing[0]; //get passed arraylist
        Object[] mStringArray = passed.toArray();


        String vin = (String) mStringArray[0];
        String json = (String) mStringArray[1];

        String response = "";

        newWebService = new NewWebService();

        RunCounts settings = new RunCounts();

        RequestBody formBody = new FormBody.Builder()
                .add("vin", vin)
                .add("json", json)
                .build();

        String apiResponse = newWebService.sendNewHttpsPost(API_URL, formBody);

        return true;
    }

    @Override
    protected void onPostExecute(Boolean purchaseResult) {

    }

}