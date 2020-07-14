package ru.bloodsoft.gibddchecker.ui.recycler_views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.models.Organs;
import ru.bloodsoft.gibddchecker.models.RestrictedItem;
import ru.bloodsoft.gibddchecker.models.RestrictedTypes;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class RestrictedItemRecyclerViewAdapter extends RecyclerView.Adapter<RestrictedItemRecyclerViewAdapter.CustomViewHolder> {
    private List<RestrictedItem> restrictedItemList;
    private Context mContext;
    private AdapterView.OnItemClickListener onItemClickListener;
    private static final String TAG = makeLogTag(RestrictedItemRecyclerViewAdapter.class);

    public RestrictedItemRecyclerViewAdapter(Context context, List<RestrictedItem> restrictedItemList) {
        this.restrictedItemList = restrictedItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.restricted_item_row, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final RestrictedItem restrictedItem = restrictedItemList.get(i);

        //Setting text view title
        customViewHolder.RegName.setText(restrictedItem.getRegName());
        customViewHolder.DateOgr.setText(formattedDateFromString(restrictedItem.getDateOgr(), restrictedItem.getDateOgr()));

        RestrictedTypes restrictedType = new RestrictedTypes();
        String restrictedTypeDescr = restrictedType.getRestrictedType(restrictedItem.getOgrKod());
        customViewHolder.OgrKod.setText(restrictedTypeDescr);

        customViewHolder.TsModel.setText(restrictedItem.getTsmodel());
        customViewHolder.Phone.setText(restrictedItem.getPhone());

        Organs organ = new Organs();
        String organDescr = organ.getOrganType(restrictedItem.getDivType());
        customViewHolder.DivType.setText(organDescr);

        customViewHolder.OsnOgr.setText(restrictedItem.getOsnOgr());
    }

    @Override
    public int getItemCount() {
        return (null != restrictedItemList ? restrictedItemList.size() : 0);
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView RegName;
        protected TextView DateOgr;
        protected TextView OgrKod;
        protected TextView TsModel;
        protected TextView Phone;
        protected TextView DivType;
        protected TextView OsnOgr;

        public CustomViewHolder(View view) {
            super(view);
            this.RegName = (TextView) view.findViewById(R.id.RegName);
            this.DateOgr = (TextView) view.findViewById(R.id.DateOgr);
            this.OgrKod = (TextView) view.findViewById(R.id.OgrKod);
            this.TsModel = (TextView) view.findViewById(R.id.TsModel);
            this.Phone = (TextView) view.findViewById(R.id.Phone);
            this.DivType = (TextView) view.findViewById(R.id.DivType);
            this.OsnOgr = (TextView) view.findViewById(R.id.osnOgr);
        }
    }

    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     *
     * Format date from 2016-07-25T00:00:00.000+03:00 string to dd.mm.yyyy format
     * @param inputDate
     * @return
     */
    public static String formattedDateFromString(String inputDate, String defaultText) {

        String inputFormat = "dd.MM.yyyy'T'HH:mm:ss";
        String outputFormat = "dd.MM.yyyy";

        Date parsed = null;
        String outputDate = defaultText;

        SimpleDateFormat df_input = new SimpleDateFormat(inputFormat, java.util.Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat(outputFormat, java.util.Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
            outputDate = df_output.format(parsed);
        } catch (Exception e) {
            outputDate = inputDate;
        }

        return outputDate;

    }
}