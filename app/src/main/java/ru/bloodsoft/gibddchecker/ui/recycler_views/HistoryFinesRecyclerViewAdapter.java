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
import ru.bloodsoft.gibddchecker.models.Fine;

public class HistoryFinesRecyclerViewAdapter extends RecyclerView.Adapter<HistoryFinesRecyclerViewAdapter.MyViewHolder> {
    private List<Fine> finesItemList;
    private Context mContext;
    private static ClickListener clickListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CardView mCardView;
        public TextView regNumberView;
        public TextView stsNumberView;
        public ImageButton removeButton;
        public MyViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            mCardView = (CardView) v.findViewById(R.id.card_view);
            regNumberView = (TextView) v.findViewById(R.id.regnumber);
            stsNumberView = (TextView) v.findViewById(R.id.stsnumber);
            removeButton = (ImageButton) v.findViewById(R.id.remove_button);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        HistoryFinesRecyclerViewAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryFinesRecyclerViewAdapter(Context context, List<Fine> finesItemList) {
        this.finesItemList = finesItemList;
        this.mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HistoryFinesRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_fines_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Fine finesItem = finesItemList.get(position);
        holder.regNumberView.setText(finesItem.regNumber);
        holder.stsNumberView.setText(finesItem.stsNumber);

        // Set a click listener for item remove button
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get singleton instance of database
                HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(mContext);
                // delete vin from the database
                databaseHelper.deleteFine(finesItem.regNumber, finesItem.stsNumber);

                finesItemList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, finesItemList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != finesItemList ? finesItemList.size() : 0);
    }
}