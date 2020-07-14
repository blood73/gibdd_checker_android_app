package ru.bloodsoft.gibddchecker.ui.recycler_views;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.List;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.database.HistoryDatabaseHelper;
import ru.bloodsoft.gibddchecker.models.Insurance;

public class HistoryInsuranceRecyclerViewAdapter extends RecyclerView.Adapter<HistoryInsuranceRecyclerViewAdapter.MyViewHolder> {
    private List<Insurance> insuranceItemList;
    private Context mContext;
    private static ClickListener clickListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CardView mCardView;
        public TextView insuranceSerialTextView;
        public TextView insuranceNumberView;
        public ImageButton removeButton;
        public MyViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            mCardView = (CardView) v.findViewById(R.id.card_view);
            insuranceSerialTextView = (TextView) v.findViewById(R.id.insurance_serial);
            insuranceNumberView = (TextView) v.findViewById(R.id.insurance_number);
            removeButton = (ImageButton) v.findViewById(R.id.remove_button);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        HistoryInsuranceRecyclerViewAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryInsuranceRecyclerViewAdapter(Context context, List<Insurance> insuranceItemList) {
        this.insuranceItemList = insuranceItemList;
        this.mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HistoryInsuranceRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_insurance_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Insurance insuranceItem = insuranceItemList.get(position);
        holder.insuranceSerialTextView.setText(insuranceItem.insuranceSerial);
        holder.insuranceNumberView.setText(insuranceItem.insuranceText);

        // Set a click listener for item remove button
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get singleton instance of database
                HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(mContext);
                // delete vin from the database
                databaseHelper.deleteInsurance(insuranceItem.insuranceSerial, insuranceItem.insuranceText);

                insuranceItemList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, insuranceItemList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != insuranceItemList ? insuranceItemList.size() : 0);
    }
}