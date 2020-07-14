package ru.bloodsoft.gibddchecker.ui.recycler_views;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.models.AccidentItem;


public class AccidentItemRecyclerViewAdapter extends RecyclerView.Adapter<AccidentItemRecyclerViewAdapter.CustomViewHolder> {
    private List<AccidentItem> accidentItemList;
    private Context mContext;
    private AdapterView.OnItemClickListener onItemClickListener;

    public AccidentItemRecyclerViewAdapter(Context context, List<AccidentItem> accidentItemList) {
        this.accidentItemList = accidentItemList;
        this.mContext = context;
    }

    @Override
    public AccidentItemRecyclerViewAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.accident_item_row, null);
        AccidentItemRecyclerViewAdapter.CustomViewHolder viewHolder = new AccidentItemRecyclerViewAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(AccidentItemRecyclerViewAdapter.CustomViewHolder customViewHolder, int i) {
        final AccidentItem accidentItem = accidentItemList.get(i);

        //Setting text view title
        String vehicleModel = accidentItem.getVehicleModel();
        String vehicleMark = accidentItem.getVehicleMark();
        String vehicleYear = accidentItem.getVehicleYear();
        String vehicleDamageState = accidentItem.getVehicleDamageState();
        String regionName = accidentItem.getRegionName();
        String accidentTime = accidentItem.getAccidentTime();
        String accidentNumber = accidentItem.getAccidentNumber();
        String accidentType = accidentItem.getAccidentType();

        //Damage Points
        List damagePoints = accidentItem.getDamagePoints();

        //https://antiperekup.net/api/v1/get_damage_image/?damages[]=01
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("antiperekup.net")
                .appendPath("api")
                .appendPath("v1")
                .appendPath("get_damage_image");

        for (int j = 0; j < damagePoints.size(); j++) {
            builder.appendQueryParameter("damages[]", damagePoints.get(j).toString());
        }

        String imageUrl = builder.build().toString();

        customViewHolder.vehicleModel.setText(vehicleModel);
        customViewHolder.vehicleMark.setText(vehicleMark);
        customViewHolder.vehicleYear.setText(vehicleYear);
        customViewHolder.vehicleDamageState.setText(vehicleDamageState);
        customViewHolder.regionName.setText(regionName);
        customViewHolder.accidentTime.setText(accidentTime);
        customViewHolder.accidentNumber.setText(accidentNumber);
        customViewHolder.accidentType.setText(accidentType);

        DisplayMetrics displayMetrics = App.getContext().getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density - 40;
        Integer imageWidth = Math.round(dpWidth);

        if (imageWidth == 0) {
            imageWidth = 800;
        }

        if (!imageUrl.isEmpty()) {
            Glide.with(App.getContext())
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.no_image_auto)
                            .override(imageWidth, 400))
                    .into(customViewHolder.photo);
        }

    }

    @Override
    public int getItemCount() {
        return (null != accidentItemList ? accidentItemList.size() : 0);
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView vehicleModel;
        protected TextView vehicleMark;
        protected TextView vehicleYear;
        protected TextView vehicleDamageState;
        protected TextView regionName;
        protected TextView accidentTime;
        protected TextView accidentNumber;
        protected TextView accidentType;
        protected ImageView photo;

        public CustomViewHolder(View view) {
            super(view);
            this.vehicleModel = (TextView) view.findViewById(R.id.vehicleModel);
            this.vehicleMark = (TextView) view.findViewById(R.id.vehicleMark);
            this.vehicleYear = (TextView) view.findViewById(R.id.vehicleYear);
            this.vehicleDamageState = (TextView) view.findViewById(R.id.vehicleDamageState);
            this.regionName = (TextView) view.findViewById(R.id.regionName);
            this.accidentTime = (TextView) view.findViewById(R.id.accidentTime);
            this.accidentNumber = (TextView) view.findViewById(R.id.accidentNumber);
            this.accidentType = (TextView) view.findViewById(R.id.accidentType);
            this.photo = (ImageView) view.findViewById(R.id.image);
        }
    }


    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}