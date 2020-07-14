package ru.bloodsoft.gibddchecker.ui.recycler_views;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.models.FinesItem;
import ru.bloodsoft.gibddchecker.util.NewWebService;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class FinesItemRecyclerViewAdapter extends RecyclerView.Adapter<FinesItemRecyclerViewAdapter.CustomViewHolder> {
    private List<FinesItem> finesItemList;
    ProgressDialog mProgressDialog;
    GetFinesImage getFinesImage;
    NewWebService getFinesImageRequest;
    private Context mContext;
    private String URL_GET_FINES_IMAGE = "http://check.gibdd.ru/proxy/check/fines/pics";
    private AdapterView.OnItemClickListener onItemClickListener;
    private static final String TAG = makeLogTag(FinesItemRecyclerViewAdapter.class);

    public FinesItemRecyclerViewAdapter(Context context, List<FinesItem> finesItemList) {
        this.finesItemList = finesItemList;
        this.mContext = context;
    }

    @Override
    public FinesItemRecyclerViewAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fines_item_row, null);
        FinesItemRecyclerViewAdapter.CustomViewHolder viewHolder = new FinesItemRecyclerViewAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FinesItemRecyclerViewAdapter.CustomViewHolder customViewHolder, int i) {
        final FinesItem finesItem = finesItemList.get(i);

        //Setting text view title
        String date = finesItem.getDate();
        String koap = finesItem.getKoap();
        String division = finesItem.getDivision();
        String summa = finesItem.getSumma();
        String addr = finesItem.getFullAddr();
        final String coords = finesItem.getCoordinates();

        long timestamp = dateToTimestamp(date);
        String formattedDate = getDate(timestamp);

        customViewHolder.DateDecis.setText(formattedDate);
        customViewHolder.KoAPcode.setText(koap);
        customViewHolder.Addr.setText(addr);
        customViewHolder.Summa.setText(App.getContext().getResources().getString(R.string.fines_rub, summa));

        /*customViewHolder.loadImageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ArrayList<String> passing1 = new ArrayList<String>();
                passing1.add(finesItem.getNumPost());
                passing1.add(finesItem.getRegNumber());
                passing1.add(finesItem.getDivision());
                passing1.add(finesItem.getReqToken());

                getFinesImage = new GetFinesImage();
                try {
                    getFinesImage.execute(passing1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/

        customViewHolder.openMapButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String coordinates = String.format("geo:0,0?q=" + coords);
                try {
                    Intent intentMap = new Intent(Intent.ACTION_VIEW, Uri.parse(coordinates));
                    mContext.startActivity(intentMap);
                } catch (Exception e) {
                    Toast.makeText(App.getContext(), "Не найдено подходящих приложений", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != finesItemList ? finesItemList.size() : 0);
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView DateDecis;
        protected TextView KoAPcode;
        protected TextView Summa;
        protected TextView Addr;
        //protected ImageButton loadImageButton;
        protected Button openMapButton;

        public CustomViewHolder(View view) {
            super(view);
            this.DateDecis = (TextView) view.findViewById(R.id.DateDecis);
            this.KoAPcode = (TextView) view.findViewById(R.id.KoAPcode);
            this.Summa = (TextView) view.findViewById(R.id.Summa);
            this.Addr = (TextView) view.findViewById(R.id.Addr);
            //this.loadImageButton = (ImageButton) view.findViewById(R.id.load_image);
            this.openMapButton = (Button) view.findViewById(R.id.open_map);
        }
    }


    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private String getDate(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch(Exception ex){
            return "";
        }
    }

    private Long dateToTimestamp(String dateRaw) {
        try {
            //2014-08-14 16:17:00
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = (Date)formatter.parse(dateRaw);
            long output=date.getTime() / 1000L;
            String str=Long.toString(output);
            return Long.parseLong(str) * 1000;

        } catch(Exception ex){
            return (long) 0;
        }
    }

    private class GetFinesImage extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected String doInBackground(ArrayList<String>... passing) {

            ArrayList<String> passed = passing[0]; //get passed arraylist
            Object[] mStringArray = passed.toArray();

            /*
                http://check.gibdd.ru/proxy/check/fines/pics
                post:18810164140513019926
                regnum:М820НЕ64
                divid:1163097
                reqToken:d8d3091caf76b510775463e8397d199f
             */

            logD(TAG, "get fines image");

            String post = (String) mStringArray[0];
            String regnum = (String) mStringArray[1];
            String divid = (String) mStringArray[2];
            String reqToken = (String) mStringArray[3];

            logD(TAG, "post: " + post);
            logD(TAG, "regnum: " + regnum);
            logD(TAG, "divid: " + divid);
            logD(TAG, "reqToken: " + reqToken);

            String params = "post=" + post + "&regnum=" + regnum +
                    "&divid=" + divid + "&reqToken=" + reqToken;

            RequestBody formBody = new FormBody.Builder()
                    .add("post", post)
                    .add("regnum", regnum)
                    .add("divid", divid)
                    .add("reqToken", reqToken)
                    .build();

            String response = "";
            try {
                response = getFinesImageRequest.sendPostWithCookies(URL_GET_FINES_IMAGE, formBody);
            } catch (Exception e) {
                e.printStackTrace();
            }

            logD(TAG, "Response: " + response);

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            dismissProgressDialog();
            //{"requestTime":"11.05.2017 15:14","code":"OK","reqToken":"d8d3091caf76b510775463e8397d199f","comment":"","photos":[{"base64Value":"\/9j\/4AAQS/......Q==","type":"-2"}],"version":"1.2"}
            Toast.makeText(App.getContext(), response, Toast.LENGTH_LONG).show();
        }
    }

    private void showProgressDialog() {
        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(mContext.getResources().getString(R.string.loading));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}