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
import ru.bloodsoft.gibddchecker.models.Fssp;

public class HistoryFsspRecyclerViewAdapter extends RecyclerView.Adapter<HistoryFsspRecyclerViewAdapter.MyViewHolder> {
    private List<Fssp> fsspItemList;
    private Context mContext;
    private static HistoryFsspRecyclerViewAdapter.ClickListener clickListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CardView mCardView;
        public TextView fsspRegion;
        public TextView fsspFirstname;
        public TextView fsspLastname;
        public TextView fsspPatronymic;
        public TextView fsspDob;
        public ImageButton removeButton;
        public MyViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            mCardView = (CardView) v.findViewById(R.id.card_view);
            fsspRegion = (TextView) v.findViewById(R.id.fssp_region);
            fsspFirstname = (TextView) v.findViewById(R.id.fssp_firstname);
            fsspLastname = (TextView) v.findViewById(R.id.fssp_lastname);
            fsspPatronymic = (TextView) v.findViewById(R.id.fssp_patronymic);
            fsspDob = (TextView) v.findViewById(R.id.fssp_dob);
            removeButton = (ImageButton) v.findViewById(R.id.remove_button);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(HistoryFsspRecyclerViewAdapter.ClickListener clickListener) {
        HistoryFsspRecyclerViewAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryFsspRecyclerViewAdapter(Context context, List<Fssp> fsspItemList) {
        this.fsspItemList = fsspItemList;
        this.mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HistoryFsspRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_fssp_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        HistoryFsspRecyclerViewAdapter.MyViewHolder vh = new HistoryFsspRecyclerViewAdapter.MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(HistoryFsspRecyclerViewAdapter.MyViewHolder holder, final int position) {
        final Fssp fsspItem = fsspItemList.get(position);
        holder.fsspRegion.setText(fsspItem.region);
        holder.fsspFirstname.setText(fsspItem.firstname);
        holder.fsspLastname.setText(fsspItem.lastname);
        holder.fsspPatronymic.setText(fsspItem.patronymic);
        holder.fsspDob.setText(fsspItem.dob);

        // Set a click listener for item remove button
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get singleton instance of database
                HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(mContext);
                // delete vin from the database
                databaseHelper.deleteFssp(fsspItem.region, fsspItem.firstname, fsspItem.lastname,
                        fsspItem.patronymic, fsspItem.dob);

                fsspItemList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, fsspItemList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != fsspItemList ? fsspItemList.size() : 0);
    }
}