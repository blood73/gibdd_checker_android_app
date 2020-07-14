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
import ru.bloodsoft.gibddchecker.models.Eaisto;

public class HistoryEaistoRecyclerViewAdapter extends RecyclerView.Adapter<HistoryEaistoRecyclerViewAdapter.MyViewHolder> {
    private List<Eaisto> eaistoItemList;
    private Context mContext;
    private static HistoryEaistoRecyclerViewAdapter.ClickListener clickListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CardView mCardView;
        public TextView eaistoVinView;
        public TextView eaistoRegView;
        public ImageButton removeButton;
        public MyViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            mCardView = (CardView) v.findViewById(R.id.card_view);
            eaistoVinView = (TextView) v.findViewById(R.id.eaisto_vin);
            eaistoRegView = (TextView) v.findViewById(R.id.eaisto_reg_number);
            removeButton = (ImageButton) v.findViewById(R.id.remove_button);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(HistoryEaistoRecyclerViewAdapter.ClickListener clickListener) {
        HistoryEaistoRecyclerViewAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryEaistoRecyclerViewAdapter(Context context, List<Eaisto> eaistoItem) {
        this.eaistoItemList = eaistoItem;
        this.mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HistoryEaistoRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_eaisto_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        HistoryEaistoRecyclerViewAdapter.MyViewHolder vh = new HistoryEaistoRecyclerViewAdapter.MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(HistoryEaistoRecyclerViewAdapter.MyViewHolder holder, final int position) {
        final Eaisto eaistoItem = eaistoItemList.get(position);
        holder.eaistoVinView.setText(eaistoItem.vin);
        holder.eaistoRegView.setText(eaistoItem.regNumber);

        // Set a click listener for item remove button
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get singleton instance of database
                HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(mContext);
                // delete vin from the database
                databaseHelper.deleteEaisto(eaistoItem.vin, "",
                        "", eaistoItem.regNumber);

                eaistoItemList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, eaistoItemList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != eaistoItemList ? eaistoItemList.size() : 0);
    }
}