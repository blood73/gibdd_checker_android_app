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
import ru.bloodsoft.gibddchecker.models.Mileage;

public class HistoryMileageRecyclerViewAdapter extends RecyclerView.Adapter<HistoryMileageRecyclerViewAdapter.MyViewHolder> {
    private List<Mileage> mileageItemList;
    private Context mContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView vinTextView;
        public TextView mileageTextView;
        public TextView dateTextView;
        public ImageButton removeButton;
        public MyViewHolder(View v) {
            super(v);
            mCardView = (CardView) v.findViewById(R.id.card_view);
            vinTextView = (TextView) v.findViewById(R.id.vin);
            mileageTextView = (TextView) v.findViewById(R.id.mileage);
            dateTextView = (TextView) v.findViewById(R.id.date);
            removeButton = (ImageButton) v.findViewById(R.id.remove_button);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryMileageRecyclerViewAdapter(Context context, List<Mileage> mileageItemList) {
        this.mileageItemList = mileageItemList;
        this.mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HistoryMileageRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_mileage_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Mileage mileageItem = mileageItemList.get(position);
        holder.vinTextView.setText(mileageItem.vin);
        holder.mileageTextView.setText(mileageItem.mileage + " км");
        holder.dateTextView.setText(mileageItem.date);

        // Set a click listener for item remove button
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get singleton instance of database
                HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(mContext);
                // delete vin from the database
                databaseHelper.deleteMileage(mileageItem.vin);

                mileageItemList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mileageItemList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != mileageItemList ? mileageItemList.size() : 0);
    }
}