package ru.bloodsoft.gibddchecker.ui.recycler_views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import java.util.List;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.models.WantedItem;

public class WantedItemRecyclerViewAdapter extends RecyclerView.Adapter<WantedItemRecyclerViewAdapter.CustomViewHolder> {
    private List<WantedItem> wantedItemList;
    private Context mContext;
    private AdapterView.OnItemClickListener onItemClickListener;

    public WantedItemRecyclerViewAdapter(Context context, List<WantedItem> wantedItemList) {
        this.wantedItemList = wantedItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.wanted_item_row, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final WantedItem wantedItem = wantedItemList.get(i);

        //Setting text view title
        customViewHolder.Model.setText(wantedItem.getModel());
        customViewHolder.RegInic.setText(wantedItem.getRegInic());
        customViewHolder.DataPu.setText(wantedItem.getDataPu());
        customViewHolder.GodVyp.setText(wantedItem.getGodVyp());
    }

    @Override
    public int getItemCount() {
        return (null != wantedItemList ? wantedItemList.size() : 0);
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView RegInic;
        protected TextView DataPu;
        protected TextView GodVyp;
        protected TextView Model;

        public CustomViewHolder(View view) {
            super(view);
            this.Model = (TextView) view.findViewById(R.id.Model);
            this.RegInic = (TextView) view.findViewById(R.id.RegInic);
            this.DataPu = (TextView) view.findViewById(R.id.DataPu);
            this.GodVyp = (TextView) view.findViewById(R.id.GodVyp);
        }
    }


    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}