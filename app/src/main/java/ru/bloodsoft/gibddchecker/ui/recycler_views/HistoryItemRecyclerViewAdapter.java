package ru.bloodsoft.gibddchecker.ui.recycler_views;

import android.content.Context;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.models.HistoryItem;
import ru.bloodsoft.gibddchecker.models.Tenure;
import ru.bloodsoft.gibddchecker.models.TypeOperation;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class HistoryItemRecyclerViewAdapter extends RecyclerView.Adapter<HistoryItemRecyclerViewAdapter.CustomViewHolder> {
    private List<HistoryItem> historyItemList;
    private Context mContext;
    private AdapterView.OnItemClickListener onItemClickListener;
    private static final String TAG = makeLogTag(HistoryItemRecyclerViewAdapter.class);

    public HistoryItemRecyclerViewAdapter(Context context, List<HistoryItem> historyItemList) {
        this.historyItemList = historyItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.history_item_row, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final HistoryItem historyItem = historyItemList.get(i);

        //Setting text view title
        String personType = historyItem.getPersonType();

        if (personType.equals("Natural")) {
            personType = mContext.getResources().getString(R.string.person_natural);
        } else {
            personType = mContext.getResources().getString(R.string.person_legal);
        }

        customViewHolder.PersonType.setText(personType);
        customViewHolder.From.setText(formattedDateFromString(historyItem.getFrom(), mContext.getResources().getString(R.string.date_now)));
        customViewHolder.To.setText(formattedDateFromString(historyItem.getTo(), mContext.getResources().getString(R.string.date_now)));
        customViewHolder.Tenure.setText(Tenure.getTenureByDates(historyItem.getFrom(), historyItem.getTo()));

        customViewHolder.infoButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // button click event
                String operationType = historyItem.getLastOperation();
                TypeOperation to =  new TypeOperation();
                String operationTypeDescr = to.getDescriptionByTypeOperation(operationType);

                AlertDialog.Builder builderInfoDialog = new AlertDialog.Builder(mContext);
                builderInfoDialog.setTitle(mContext.getResources().getString(R.string.history_details));
                builderInfoDialog.setMessage(operationTypeDescr);
                builderInfoDialog.setCancelable(true);
                builderInfoDialog.setNeutralButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alertInfoDialog = builderInfoDialog.create();
                alertInfoDialog.show();
            }
        });
    }

    /**
     *
     * Format date from 2016-07-25T00:00:00.000+03:00 string to dd.mm.yyyy format
     * @param inputDate
     * @return
     */
    public static String formattedDateFromString(String inputDate, String defaultText) {

        String inputFormat = "yyyy-MM-dd'T'HH:mm:ss";
        String outputFormat = "dd.MM.yyyy";

        Date parsed = null;
        String outputDate = defaultText;

        SimpleDateFormat df_input = new SimpleDateFormat(inputFormat, java.util.Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat(outputFormat, java.util.Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
            outputDate = df_output.format(parsed);
        } catch (Exception e) {
        }

        return outputDate;

    }

    @Override
    public int getItemCount() {
        return (null != historyItemList ? historyItemList.size() : 0);
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView PersonType;
        protected TextView From;
        protected TextView To;
        protected TextView Tenure;
        protected Button infoButton;

        public CustomViewHolder(View view) {
            super(view);
            this.PersonType = (TextView) view.findViewById(R.id.Person);
            this.From = (TextView) view.findViewById(R.id.From);
            this.To = (TextView) view.findViewById(R.id.To);
            this.Tenure = (TextView) view.findViewById(R.id.tenure);
            this.infoButton = (Button) view.findViewById(R.id.info);
        }
    }


    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}