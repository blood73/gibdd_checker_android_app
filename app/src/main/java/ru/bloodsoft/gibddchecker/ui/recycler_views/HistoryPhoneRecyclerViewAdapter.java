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
import ru.bloodsoft.gibddchecker.models.Phone;

public class HistoryPhoneRecyclerViewAdapter extends RecyclerView.Adapter<HistoryPhoneRecyclerViewAdapter.MyViewHolder> {
    private List<Phone> phoneItemList;
    private Context mContext;
    private static HistoryPhoneRecyclerViewAdapter.ClickListener clickListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CardView mCardView;
        public TextView phoneView;
        public ImageButton removeButton;
        public MyViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            mCardView = (CardView) v.findViewById(R.id.card_view);
            phoneView = (TextView) v.findViewById(R.id.phone);
            removeButton = (ImageButton) v.findViewById(R.id.remove_button);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(HistoryPhoneRecyclerViewAdapter.ClickListener clickListener) {
        HistoryPhoneRecyclerViewAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryPhoneRecyclerViewAdapter(Context context, List<Phone> phoneItem) {
        this.phoneItemList = phoneItem;
        this.mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HistoryPhoneRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_phone_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        HistoryPhoneRecyclerViewAdapter.MyViewHolder vh = new HistoryPhoneRecyclerViewAdapter.MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(HistoryPhoneRecyclerViewAdapter.MyViewHolder holder, final int position) {
        final Phone phoneItem = phoneItemList.get(position);
        holder.phoneView.setText("7" + phoneItem.phoneNumber);

        // Set a click listener for item remove button
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get singleton instance of database
                HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(mContext);
                // delete phone from the database
                databaseHelper.deletePhone(phoneItem.phoneNumber);

                phoneItemList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, phoneItemList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != phoneItemList ? phoneItemList.size() : 0);
    }
}