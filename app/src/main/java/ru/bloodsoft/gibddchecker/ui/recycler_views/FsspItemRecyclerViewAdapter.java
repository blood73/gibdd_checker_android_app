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
import ru.bloodsoft.gibddchecker.models.FsspItem;

public class FsspItemRecyclerViewAdapter extends RecyclerView.Adapter<FsspItemRecyclerViewAdapter.CustomViewHolder> {
    private List<FsspItem> fsspItemList;
    private Context mContext;
    private AdapterView.OnItemClickListener onItemClickListener;

    public FsspItemRecyclerViewAdapter(Context context, List<FsspItem> fsspItemList) {
        this.fsspItemList = fsspItemList;
        this.mContext = context;
    }

    @Override
    public FsspItemRecyclerViewAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fssp_item_row, null);
        FsspItemRecyclerViewAdapter.CustomViewHolder viewHolder = new FsspItemRecyclerViewAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FsspItemRecyclerViewAdapter.CustomViewHolder customViewHolder, int i) {
        final FsspItem fsspItem = fsspItemList.get(i);

        //Setting text view title
        String name = fsspItem.getName();
        String exeProduction = fsspItem.getExeProduction();
        String details = fsspItem.getDetails();
        String subject = fsspItem.getSubject();
        String department = fsspItem.getDepartment();
        String bailiff = fsspItem.getBailiff();

        customViewHolder.name.setText(name);
        customViewHolder.exeProduction.setText(exeProduction);
        customViewHolder.details.setText(details);
        customViewHolder.subject.setText(subject);
        customViewHolder.department.setText(department);
        customViewHolder.bailiff.setText(bailiff);

    }

    @Override
    public int getItemCount() {
        return (null != fsspItemList ? fsspItemList.size() : 0);
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected TextView exeProduction;
        protected TextView details;
        protected TextView subject;
        protected TextView department;
        protected TextView bailiff;

        public CustomViewHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.fssp_name);
            this.exeProduction = (TextView) view.findViewById(R.id.fssp_exe_production);
            this.details = (TextView) view.findViewById(R.id.fssp_details);
            this.subject = (TextView) view.findViewById(R.id.fssp_subject);
            this.department = (TextView) view.findViewById(R.id.fssp_department);
            this.bailiff = (TextView) view.findViewById(R.id.fssp_bailiff);
        }
    }


    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}