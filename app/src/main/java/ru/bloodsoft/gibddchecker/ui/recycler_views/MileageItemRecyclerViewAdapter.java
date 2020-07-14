package ru.bloodsoft.gibddchecker.ui.recycler_views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.models.MileageItem;

import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class MileageItemRecyclerViewAdapter extends RecyclerView.Adapter<MileageItemRecyclerViewAdapter.CustomViewHolder> {
    private List<MileageItem> mileageItemList;
    private Context mContext;
    private static final String TAG = makeLogTag(MileageItemRecyclerViewAdapter.class);

    public MileageItemRecyclerViewAdapter(Context context, List<MileageItem> mileageItemList) {
        this.mileageItemList = mileageItemList;
        this.mContext = context;
    }

    @Override
    public MileageItemRecyclerViewAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.mileage_item_row, null);
        MileageItemRecyclerViewAdapter.CustomViewHolder viewHolder = new MileageItemRecyclerViewAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MileageItemRecyclerViewAdapter.CustomViewHolder customViewHolder, int i) {
        final MileageItem mileageItem = mileageItemList.get(i);

        long timestamp = mileageItem.getTimestamp();
        String source = mileageItem.getSource();
        String city = mileageItem.getCity();
        int distance = mileageItem.getDistance();
        int price = mileageItem.getPrice();

        customViewHolder.createdDate.setText(getDate(timestamp));
        if (source.equals("eaisto")) {
            customViewHolder.source.setText(mContext.getString(R.string.eaisto));
        } else {
            customViewHolder.source.setText(source);
        }

        if (city.isEmpty()) {
            customViewHolder.city.setVisibility(View.GONE);
            customViewHolder.cityHeader.setVisibility(View.GONE);
        } else {
            customViewHolder.city.setVisibility(View.VISIBLE);
            customViewHolder.cityHeader.setVisibility(View.VISIBLE);
            customViewHolder.city.setText(city);
        }

        customViewHolder.distance.setText(Integer.toString(distance) + " км");


        if (price == 0) {
            customViewHolder.price.setVisibility(View.GONE);
            customViewHolder.priceHeader.setVisibility(View.GONE);
        } else {
            customViewHolder.price.setVisibility(View.VISIBLE);
            customViewHolder.priceHeader.setVisibility(View.VISIBLE);
            customViewHolder.price.setText(Integer.toString(price) + " руб.");
        }

    }

    @Override
    public int getItemCount() {
        return (null != mileageItemList ? mileageItemList.size() : 0);
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView createdDate;
        protected TextView source;
        protected TextView city;
        protected TextView cityHeader;
        protected TextView distance;
        protected TextView price;
        protected TextView priceHeader;

        public CustomViewHolder(View view) {
            super(view);
            this.createdDate = (TextView) view.findViewById(R.id.created_date);
            this.source = (TextView) view.findViewById(R.id.source);
            this.city = (TextView) view.findViewById(R.id.city);
            this.cityHeader = (TextView) view.findViewById(R.id.city_header);
            this.distance = (TextView) view.findViewById(R.id.distance);
            this.price = (TextView) view.findViewById(R.id.price);
            this.priceHeader = (TextView) view.findViewById(R.id.price_header);
        }
    }

    public  String getDate(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp * 1000);
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);
        }catch (Exception e) {
        }
        return "";
    }
}