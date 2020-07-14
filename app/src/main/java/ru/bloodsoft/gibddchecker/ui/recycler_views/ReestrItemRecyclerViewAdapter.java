package ru.bloodsoft.gibddchecker.ui.recycler_views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import java.util.List;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.models.ReestrItem;

import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class ReestrItemRecyclerViewAdapter extends RecyclerView.Adapter<ReestrItemRecyclerViewAdapter.CustomViewHolder> {
    private List<ReestrItem> reestrItemList;
    private Context mContext;
    private AdapterView.OnItemClickListener onItemClickListener;
    private static final String TAG = makeLogTag(ReestrItemRecyclerViewAdapter.class);

    public ReestrItemRecyclerViewAdapter(Context context, List<ReestrItem> reestrItemList) {
        this.reestrItemList = reestrItemList;
        this.mContext = context;
    }

    @Override
    public ReestrItemRecyclerViewAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.reestr_item_row, null);
        ReestrItemRecyclerViewAdapter.CustomViewHolder viewHolder = new ReestrItemRecyclerViewAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ReestrItemRecyclerViewAdapter.CustomViewHolder customViewHolder, int i) {
        final ReestrItem reestrItem = reestrItemList.get(i);

        String regDate = reestrItem.getRegDate();
        String regInfoText = reestrItem.getRegInfoText();
        String pledgor = reestrItem.getPledgor();
        String mortgagees = reestrItem.getMortgagees();

        customViewHolder.RegDate.setText(regDate);
        customViewHolder.RegInfoText.setText(regInfoText);
        customViewHolder.Pledgor.setText(pledgor);
        customViewHolder.Mortgagees.setText(mortgagees);
    }

    @Override
    public int getItemCount() {
        return (null != reestrItemList ? reestrItemList.size() : 0);
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView RegDate;
        protected TextView RegInfoText;
        protected TextView Pledgor;
        protected TextView Mortgagees;

        public CustomViewHolder(View view) {
            super(view);
            this.RegDate = (TextView) view.findViewById(R.id.RegDate);
            this.RegInfoText = (TextView) view.findViewById(R.id.RegInfoText);
            this.Pledgor = (TextView) view.findViewById(R.id.Pledgor);
            this.Mortgagees = (TextView) view.findViewById(R.id.Mortgagees);
        }
    }


    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}