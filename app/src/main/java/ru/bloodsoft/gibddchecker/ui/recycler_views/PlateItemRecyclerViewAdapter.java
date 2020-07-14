package ru.bloodsoft.gibddchecker.ui.recycler_views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.models.PlateItem;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class PlateItemRecyclerViewAdapter extends RecyclerView.Adapter<PlateItemRecyclerViewAdapter.CustomViewHolder> {
    private List<PlateItem> plateItemList;
    private Context mContext;
    private AdapterView.OnItemClickListener onItemClickListener;
    private static final String TAG = makeLogTag(PlateItemRecyclerViewAdapter.class);

    public PlateItemRecyclerViewAdapter(Context context, List<PlateItem> plateItemList) {
        this.plateItemList = plateItemList;
        this.mContext = context;
    }

    @Override
    public PlateItemRecyclerViewAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plate_item_row, null);
        PlateItemRecyclerViewAdapter.CustomViewHolder viewHolder = new PlateItemRecyclerViewAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PlateItemRecyclerViewAdapter.CustomViewHolder customViewHolder, int i) {
        final PlateItem plateItem = plateItemList.get(i);

        //Setting text view title
        String make = plateItem.getMake();
        String model = plateItem.getModel();
        String date = plateItem.getDate();

        String imageUrl = plateItem.getImage();
        String url = plateItem.getUrl();
        customViewHolder.make.setText(make);
        customViewHolder.model.setText(model);

        long timestamp = dateToTimestamp(date);
        String formattedDate = getDate(timestamp);

        customViewHolder.date.setText(formattedDate);

        if (!imageUrl.isEmpty()) {
            Glide.with(App.getContext())
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.no_image_auto)
                            .centerCrop())
                    .into(customViewHolder.photo);
        }

        if (!url.isEmpty()) {
            customViewHolder.open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // open in browser
                    String url = plateItem.getUrl();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    mContext.startActivity(i);
                }
            });
        } else {
            customViewHolder.open.setVisibility(View.GONE) ;
        }

    }

    @Override
    public int getItemCount() {
        return (null != plateItemList ? plateItemList.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView make;
        protected TextView model;
        protected TextView date;
        protected ImageView photo;
        protected Button open;

        public CustomViewHolder(View view) {
            super(view);
            this.make = (TextView) view.findViewById(R.id.make);
            this.model = (TextView) view.findViewById(R.id.model);
            this.date = (TextView) view.findViewById(R.id.date);
            this.photo = (ImageView) view.findViewById(R.id.auto);
            this.open = (Button) view.findViewById(R.id.open_url);
        }
    }


    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private String getDate(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch(Exception ex){
            return "";
        }
    }

    private Long dateToTimestamp(String dateRaw) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = (Date)formatter.parse(dateRaw);
            long output=date.getTime() / 1000L;
            String str=Long.toString(output);
            return Long.parseLong(str) * 1000;

        } catch(Exception ex){
            return (long) 0;
        }
    }
}