package ru.bloodsoft.gibddchecker.ui.recycler_views;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.models.EaistoItem;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class EaistoItemRecyclerViewAdapter extends RecyclerView.Adapter<EaistoItemRecyclerViewAdapter.CustomViewHolder> {
    private List<EaistoItem> eaistoItemList;
    private Context mContext;
    private AdapterView.OnItemClickListener onItemClickListener;
    private static final String TAG = makeLogTag(EaistoItemRecyclerViewAdapter.class);

    public EaistoItemRecyclerViewAdapter(Context context, List<EaistoItem> historyItemList) {
        this.eaistoItemList = historyItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.eaisto_item_row, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final EaistoItem eaistoItem = eaistoItemList.get(i);

        //Setting text view title
        String cardNumber = eaistoItem.getCardNumber();
        String model = eaistoItem.getModel();
        final String vin = eaistoItem.getVin();
        final String regNumber = eaistoItem.getRegNumber();
        String dateFrom = eaistoItem.getDateFrom();
        String dateTo = eaistoItem.getDateTo();

        customViewHolder.cardNumber.setText(cardNumber);
        customViewHolder.model.setText(model);
        customViewHolder.vin.setText(vin);
        customViewHolder.regNumber.setText(regNumber);
        String dateFromFormatted = getDate(Long.parseLong(dateFrom));
        String dateToFormatted = getDate(Long.parseLong(dateTo));

        customViewHolder.dateFrom.setText(dateFromFormatted);
        customViewHolder.dateTo.setText(dateToFormatted);

        customViewHolder.copyRegButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // button click event
                ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(regNumber, regNumber);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(App.getContext(), "Скопировано", Toast.LENGTH_LONG).show();
            }
        });

        customViewHolder.copyButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // button click event
                ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(vin, vin);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(App.getContext(), "Скопировано", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != eaistoItemList ? eaistoItemList.size() : 0);
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView cardNumber;
        protected TextView model;
        protected TextView vin;
        protected TextView regNumber;
        protected TextView dateFrom;
        protected TextView dateTo;
        protected TextView operator;
        protected Button copyButton;
        protected Button copyRegButton;

        public CustomViewHolder(View view) {
            super(view);
            this.cardNumber = (TextView) view.findViewById(R.id.cardNumber);
            this.model = (TextView) view.findViewById(R.id.model);
            this.vin = (TextView) view.findViewById(R.id.vin);
            this.regNumber = (TextView) view.findViewById(R.id.regNumber);
            this.dateFrom = (TextView) view.findViewById(R.id.dateFrom);
            this.dateTo = (TextView) view.findViewById(R.id.dateTo);
            this.copyButton = (Button) view.findViewById(R.id.copy_vin);
            this.copyRegButton = (Button) view.findViewById(R.id.copy_regnumber);
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
            TimeZone tz = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp * 1000);
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);
        }catch (Exception e) {
        }
        return "";
    }
}