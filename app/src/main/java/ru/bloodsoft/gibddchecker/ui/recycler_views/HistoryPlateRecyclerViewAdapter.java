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
import ru.bloodsoft.gibddchecker.models.Plate;

public class HistoryPlateRecyclerViewAdapter  extends RecyclerView.Adapter<HistoryPlateRecyclerViewAdapter.MyViewHolder> {
    private List<Plate> plateItemList;
    private Context mContext;
    private static HistoryPlateRecyclerViewAdapter.ClickListener clickListener;

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
            phoneView = (TextView) v.findViewById(R.id.plate);
            removeButton = (ImageButton) v.findViewById(R.id.remove_button);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(HistoryPlateRecyclerViewAdapter.ClickListener clickListener) {
        HistoryPlateRecyclerViewAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryPlateRecyclerViewAdapter(Context context, List<Plate> plateItem) {
        this.plateItemList = plateItem;
        this.mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HistoryPlateRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_plate_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        HistoryPlateRecyclerViewAdapter.MyViewHolder vh = new HistoryPlateRecyclerViewAdapter.MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(HistoryPlateRecyclerViewAdapter.MyViewHolder holder, final int position) {
        final Plate plateItem = plateItemList.get(position);
        holder.phoneView.setText(plateItem.plateNumber);

        // Set a click listener for item remove button
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get singleton instance of database
                HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(mContext);
                // delete phone from the database
                databaseHelper.deletePlate(plateItem.plateNumber);

                plateItemList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, plateItemList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != plateItemList ? plateItemList.size() : 0);
    }
}