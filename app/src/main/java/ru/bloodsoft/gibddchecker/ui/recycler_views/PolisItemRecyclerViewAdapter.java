package ru.bloodsoft.gibddchecker.ui.recycler_views;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.models.PolisItem;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class PolisItemRecyclerViewAdapter extends RecyclerView.Adapter<PolisItemRecyclerViewAdapter.CustomViewHolder> {
    private List<PolisItem> polisItemList;
    private Context mContext;
    private AdapterView.OnItemClickListener onItemClickListener;
    private static final String TAG = makeLogTag(PolisItemRecyclerViewAdapter.class);

    public PolisItemRecyclerViewAdapter(Context context, List<PolisItem> polisItemList) {
        this.polisItemList = polisItemList;
        this.mContext = context;
    }

    @Override
    public PolisItemRecyclerViewAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.polis_item_row, null);
        PolisItemRecyclerViewAdapter.CustomViewHolder viewHolder = new PolisItemRecyclerViewAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PolisItemRecyclerViewAdapter.CustomViewHolder customViewHolder, int i) {
        final PolisItem polisItem = polisItemList.get(i);

        //Setting text view title
        String policyBsoSerial = polisItem.getPolicyBsoSerial();
        String policyBsoNumber = polisItem.getPolicyBsoNumber();
        String insCompanyName = polisItem.getInsCompanyName();
        String isRestrict = polisItem.getIsRestrict();

        customViewHolder.policyBsoSerial.setText(policyBsoSerial);
        customViewHolder.policyBsoNumber.setText(policyBsoNumber);
        customViewHolder.insCompanyName.setText(insCompanyName);
        customViewHolder.isRestrict.setText(isRestrict);

        customViewHolder.copyButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // button click event
                ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(polisItem.getPolicyBsoNumber(), polisItem.getPolicyBsoNumber());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(App.getContext(), "Скопировано", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != polisItemList ? polisItemList.size() : 0);
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView policyBsoSerial;
        protected TextView policyBsoNumber;
        protected TextView insCompanyName;
        protected TextView isRestrict;
        protected Button copyButton;

        public CustomViewHolder(View view) {
            super(view);
            this.policyBsoSerial = (TextView) view.findViewById(R.id.policyBsoSerial);
            this.policyBsoNumber = (TextView) view.findViewById(R.id.policyBsoNumber);
            this.insCompanyName = (TextView) view.findViewById(R.id.insCompanyName);
            this.isRestrict = (TextView) view.findViewById(R.id.isRestrict);
            this.copyButton = (Button) view.findViewById(R.id.polis_copy);
        }
    }


    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}