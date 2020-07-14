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
import ru.bloodsoft.gibddchecker.models.Vin;
import ru.bloodsoft.gibddchecker.models.VinSearchType;

public class HistoryVinRecyclerViewAdapter extends RecyclerView.Adapter<HistoryVinRecyclerViewAdapter.MyViewHolder> {
    private List<Vin> vinItemList;
    private Context mContext;
    private static ClickListener clickListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CardView mCardView;
        public TextView vinTextView;
        public TextView vinTypeView;
        public ImageButton removeButton;
        public MyViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            mCardView = (CardView) v.findViewById(R.id.card_view);
            vinTextView = (TextView) v.findViewById(R.id.vin_number);
            vinTypeView = (TextView) v.findViewById(R.id.vin_type);
            removeButton = (ImageButton) v.findViewById(R.id.remove_button);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        HistoryVinRecyclerViewAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryVinRecyclerViewAdapter(Context context, List<Vin> vinItemList) {
        this.vinItemList = vinItemList;
        this.mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HistoryVinRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_vin_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Vin vinItem = vinItemList.get(position);
        holder.vinTextView.setText(vinItem.vinText);

        VinSearchType searchType = new VinSearchType();
        String searchTypeText = searchType.getSearchType(vinItem.vinType);

        holder.vinTypeView.setText(searchTypeText);

        // Set a click listener for item remove button
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get singleton instance of database
                HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(mContext);
                // delete vin from the database
                databaseHelper.deleteVin(vinItem.vinText, vinItem.vinType);

                vinItemList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, vinItemList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != vinItemList ? vinItemList.size() : 0);
    }
}