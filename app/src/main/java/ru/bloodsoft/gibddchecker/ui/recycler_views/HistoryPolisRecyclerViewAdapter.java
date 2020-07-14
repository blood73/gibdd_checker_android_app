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
import ru.bloodsoft.gibddchecker.models.Polis;

public class HistoryPolisRecyclerViewAdapter extends RecyclerView.Adapter<HistoryPolisRecyclerViewAdapter.MyViewHolder> {
    private List<Polis> polisItemList;
    private Context mContext;
    private static HistoryPolisRecyclerViewAdapter.ClickListener clickListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CardView mCardView;
        public TextView eaistoVinView;
        public TextView eaistoBodyView;
        public TextView eaistoFrameView;
        public TextView eaistoRegView;
        public ImageButton removeButton;
        public MyViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            mCardView = (CardView) v.findViewById(R.id.card_view);
            eaistoVinView = (TextView) v.findViewById(R.id.polis_vin);
            eaistoBodyView = (TextView) v.findViewById(R.id.polis_body_number);
            eaistoFrameView = (TextView) v.findViewById(R.id.polis_frame_number);
            eaistoRegView = (TextView) v.findViewById(R.id.polis_reg_number);
            removeButton = (ImageButton) v.findViewById(R.id.remove_button);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(HistoryPolisRecyclerViewAdapter.ClickListener clickListener) {
        HistoryPolisRecyclerViewAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryPolisRecyclerViewAdapter(Context context, List<Polis> polisItem) {
        this.polisItemList = polisItem;
        this.mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HistoryPolisRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_polis_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        HistoryPolisRecyclerViewAdapter.MyViewHolder vh = new HistoryPolisRecyclerViewAdapter.MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(HistoryPolisRecyclerViewAdapter.MyViewHolder holder, final int position) {
        final Polis polisItem = polisItemList.get(position);
        holder.eaistoVinView.setText(polisItem.vin);
        holder.eaistoBodyView.setText(polisItem.bodyNumber);
        holder.eaistoFrameView.setText(polisItem.frameNumber);
        holder.eaistoRegView.setText(polisItem.regNumber);

        // Set a click listener for item remove button
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get singleton instance of database
                HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(mContext);
                // delete vin from the database
                databaseHelper.deletePolis(polisItem.vin, polisItem.bodyNumber,
                        polisItem.frameNumber, polisItem.regNumber);

                polisItemList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, polisItemList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != polisItemList ? polisItemList.size() : 0);
    }
}