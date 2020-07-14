package ru.bloodsoft.gibddchecker.ui.recycler_views;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.models.ReportItem;
import ru.bloodsoft.gibddchecker.util.RunCounts;
import ru.bloodsoft.gibddchecker.util.SanitizeHelper;

import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class ReportItemRecyclerViewAdapter extends RecyclerView.Adapter<ReportItemRecyclerViewAdapter.CustomViewHolder> {
    private List<ReportItem> reportItemList;
    private Context mContext;
    private AdapterView.OnItemClickListener onItemClickListener;
    private static final String TAG = makeLogTag(ReportItemRecyclerViewAdapter.class);

    static {
        System.loadLibrary("native-lib");
    }

    public static native String getApiUrl();

    public ReportItemRecyclerViewAdapter(Context context, List<ReportItem> reportItemList) {
        this.reportItemList = reportItemList;
        this.mContext = context;
    }

    @Override
    public ReportItemRecyclerViewAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.report_item_row, null);
        ReportItemRecyclerViewAdapter.CustomViewHolder viewHolder = new ReportItemRecyclerViewAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    // Clean all elements of the recycler
    public void clear() {
        reportItemList.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final ReportItemRecyclerViewAdapter.CustomViewHolder customViewHolder, int i) {
        final ReportItem reportItem = reportItemList.get(i);

        //Setting text view title
        String reportNumber = reportItem.getReportNumber();
        String vin = reportItem.getVin();
        String status = reportItem.getStatus();
        long addedOn = reportItem.getAddedOn();
        long updatedOn = reportItem.getUpdatedOn();

        String statusText = "";

        if (status.equals("N")) {
            statusText = "Отчет добавлен в очередь";
        } else if (status.equals("I")) {
            statusText = "Отчет генерируется";
        } else if (status.equals("P")) {
            statusText = "Отчет сгенерирован";
        } else if (status.equals("F")) {
            statusText = "Произошла ошибка при создании отчета. Мы попробуем сгенерировать отчет чуть позже. Ожидайте. Отчет будет создан в любом случае, как только все источники данных восстановят свою работу";
        }

        customViewHolder.reportNumber.setText(reportNumber);
        customViewHolder.vin.setText(vin);
        customViewHolder.reportStatus.setText(statusText);
        customViewHolder.addedOn.setText(getDate(addedOn));
        customViewHolder.updatedOn.setText(getDate(updatedOn));

        if (status.equals("P")) {
            customViewHolder.downloadButton.setVisibility(View.VISIBLE);

            customViewHolder.downloadButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    // button click event
                    String reportNumber = reportItem.getReportNumber();

                    RunCounts settings = new RunCounts();
                    String ssad = settings.getSSAD();


                    String pdfUrl = SanitizeHelper.decryptString(getApiUrl()) + reportNumber
                            + "&ssad=" + ssad;

                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl));
                        customViewHolder.itemView.getContext().startActivity(browserIntent);
                    } catch (Exception e) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(pdfUrl), "application/pdf");

                            customViewHolder.itemView.getContext().startActivity(intent);
                        } catch (Exception ex) {
                            Toast.makeText(customViewHolder.itemView.getContext(), customViewHolder.itemView.getContext().getResources().getString(R.string.report_cant_open), Toast.LENGTH_SHORT).show();

                            try {
                                ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText(pdfUrl, pdfUrl);
                                clipboard.setPrimaryClip(clip);
                            } catch (Exception exe) {
                                exe.printStackTrace();
                            }
                        }
                    }
                }
            });
        } else {
            customViewHolder.downloadButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return (null != reportItemList ? reportItemList.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView reportNumber;
        protected TextView vin;
        protected TextView reportStatus;
        protected TextView addedOn;
        protected TextView updatedOn;
        protected Button downloadButton;

        public CustomViewHolder(View view) {
            super(view);
            this.reportNumber = (TextView) view.findViewById(R.id.report_number);
            this.vin = (TextView) view.findViewById(R.id.vin);
            this.reportStatus = (TextView) view.findViewById(R.id.status);
            this.addedOn = (TextView) view.findViewById(R.id.added_on);
            this.updatedOn = (TextView) view.findViewById(R.id.updated_on);
            this.downloadButton = (Button) view.findViewById(R.id.download);
        }
    }


    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public  String getDate(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getTimeZone("UTC");
            calendar.setTimeInMillis(timestamp * 1000);
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);
        }catch (Exception e) {
        }
        return "";
    }
}