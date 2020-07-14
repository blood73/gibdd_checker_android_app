package ru.bloodsoft.gibddchecker.ui.recycler_views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.models.PhoneItem;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class PhoneItemRecyclerViewAdapter extends RecyclerView.Adapter<PhoneItemRecyclerViewAdapter.CustomViewHolder> {
    private List<PhoneItem> phoneItemList;
    private Context mContext;
    private AdapterView.OnItemClickListener onItemClickListener;
    private static final String TAG = makeLogTag(PhoneItemRecyclerViewAdapter.class);

    public PhoneItemRecyclerViewAdapter(Context context, List<PhoneItem> phoneItemList) {
        this.phoneItemList = phoneItemList;
        this.mContext = context;
    }

    @Override
    public PhoneItemRecyclerViewAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.phone_item_row, null);
        PhoneItemRecyclerViewAdapter.CustomViewHolder viewHolder = new PhoneItemRecyclerViewAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PhoneItemRecyclerViewAdapter.CustomViewHolder customViewHolder, int i) {
        final PhoneItem phoneItem = phoneItemList.get(i);

        //Setting text view title
        String carName = phoneItem.getCarName();
        long rawDate = phoneItem.getDate();

        //convert unix epoch timestamp (seconds) to milliseconds
        //long timestamp = Long.parseLong(rawDate) * 1000L;

        String region = phoneItem.getRegion();
        String price = phoneItem.getPrice();
        String mileage = phoneItem.getMileage();
        String source = phoneItem.getSource();
        if (source.equals("null")) {
            source = "н/д";
        }
        String year = phoneItem.getYear();
        String imageUrl = phoneItem.getImageUrl();

        customViewHolder.carName.setText(carName);
        customViewHolder.date.setText(getDate(rawDate));
        customViewHolder.region.setText(region);
        customViewHolder.price.setText(price + " руб.");
        customViewHolder.mileage.setText(mileage);
        customViewHolder.source.setText(source);
        customViewHolder.year.setText(year);

        if (!imageUrl.isEmpty()) {
            Glide.with(App.getContext())
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.no_image_auto)
                            .centerCrop())
                    .into(customViewHolder.photo);
        }

        customViewHolder.infoButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // button click event
                String detailsInfo = phoneItem.getDescription();

                if (detailsInfo.equals("null")) {
                    detailsInfo = mContext.getResources().getString(R.string.phone_details_no_info);
                }

                AlertDialog.Builder builderInfoDialog = new AlertDialog.Builder(mContext);
                builderInfoDialog.setTitle(mContext.getResources().getString(R.string.phone_details_button_header));
                builderInfoDialog.setMessage(detailsInfo);
                builderInfoDialog.setCancelable(true);
                builderInfoDialog.setNeutralButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alertInfoDialog = builderInfoDialog.create();
                alertInfoDialog.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != phoneItemList ? phoneItemList.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView carName;
        protected TextView date;
        protected TextView region;
        protected TextView price;
        protected TextView mileage;
        protected TextView source;
        protected TextView year;
        protected ImageView photo;
        protected Button infoButton;

        public CustomViewHolder(View view) {
            super(view);
            this.carName = (TextView) view.findViewById(R.id.car_name);
            this.date = (TextView) view.findViewById(R.id.created_time);
            this.region = (TextView) view.findViewById(R.id.city_name);
            this.price = (TextView) view.findViewById(R.id.price);
            this.mileage = (TextView) view.findViewById(R.id.mileage);
            this.source = (TextView) view.findViewById(R.id.source_host);
            this.year = (TextView) view.findViewById(R.id.year);
            this.photo = (ImageView) view.findViewById(R.id.auto);
            this.infoButton = (Button) view.findViewById(R.id.info);
        }
    }


    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
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