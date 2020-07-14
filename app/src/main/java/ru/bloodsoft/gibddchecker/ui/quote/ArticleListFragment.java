package ru.bloodsoft.gibddchecker.ui.quote;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.models.GibddContent;
import ru.bloodsoft.gibddchecker.ui.AutospotAct;
import ru.bloodsoft.gibddchecker.ui.FullReportActivity;
import ru.bloodsoft.gibddchecker.ui.PerekupAct1;
import ru.bloodsoft.gibddchecker.ui.PerekupAct2;
import ru.bloodsoft.gibddchecker.ui.PerekupAct3;
import ru.bloodsoft.gibddchecker.ui.ShtrafiOnline;
import ru.bloodsoft.gibddchecker.ui.SravniAct;
import ru.bloodsoft.gibddchecker.ui.TOActivity;

/**
 * Shows a list of all available quotes.
 * <p/>
 */
public class ArticleListFragment extends ListFragment {

    private Callback callback = dummyCallback;

    /**
     * A callback interface. Called whenever a item has been selected.
     */
    public interface Callback {
        void onItemSelected(String id);
    }

    /**
     * A dummy no-op implementation of the Callback interface. Only used when no active Activity is present.
     */
    private static final Callback dummyCallback = new Callback() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new MyListAdapter());
        setHasOptionsMenu(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        GibddContent.DummyItem item = GibddContent.ITEMS.get(position);

        if (item.id.equals("5")) {
            Intent toIntent = new Intent(getActivity(), TOActivity.class);
            startActivity(toIntent);
        } else if (item.id.equals("6")) {
            Intent toIntent = new Intent(getActivity(), SravniAct.class);
            startActivity(toIntent);
        } else if (item.id.equals("7")) {
            Intent toIntent = new Intent(getActivity(), PerekupAct1.class);
            startActivity(toIntent);
        } else if (item.id.equals("8")) {
            Intent toIntent = new Intent(getActivity(), PerekupAct2.class);
            startActivity(toIntent);
        } else if (item.id.equals("9")) {
            Intent toIntent = new Intent(getActivity(), PerekupAct3.class);
            startActivity(toIntent);
        } else if (item.id.equals("11")) {
            Intent toIntent = new Intent(getActivity(), ShtrafiOnline.class);
            startActivity(toIntent);
        } else if (item.id.equals("0")) {
            Intent toIntent = new Intent(getActivity(), FullReportActivity.class);
            startActivity(toIntent);
        } else if (item.id.equals("12")) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://autospot.ru/brands/?utm_source=navis_vin&utm_medium=affiliate&utm_campaign=app"));
            startActivity(i);
        } else if (item.id.equals("13")) {
            Intent toIntent = new Intent(getActivity(), AutospotAct.class);
            startActivity(toIntent);
        } else {
            callback.onItemSelected(GibddContent.ITEMS.get(position).id);
        }

    }

    /**
     * onAttach(Context) is not called on pre API 23 versions of Android.
     * onAttach(Activity) is deprecated but still necessary on older devices.
     */
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachToContext(context);
    }

    /**
     * Deprecated on API 23 but still necessary for pre API 23 devices.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    /**
     * Called when the fragment attaches to the context
     */
    protected void onAttachToContext(Context context) {
        if (!(context instanceof Callback)) {
            throw new IllegalStateException("Activity must implement callback interface.");
        }

        callback = (Callback) context;
    }

    private class MyListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return GibddContent.ITEMS.size();
        }

        @Override
        public Object getItem(int position) {
            return GibddContent.ITEMS.get(position);
        }

        @Override
        public long getItemId(int position) {
            return GibddContent.ITEMS.get(position).id.hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            final GibddContent.DummyItem item = (GibddContent.DummyItem) getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_article, container, false);
            }

            if (position == 4) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_report, container, false);
            } else {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_article, container, false);
            }

            ((TextView) convertView.findViewById(R.id.article_title)).setText(item.title);

            final ImageView img = (ImageView) convertView.findViewById(R.id.thumbnail);

            Glide.with(getActivity())
                    .asBitmap()
                    .load(item.iconId)
                    .apply(new RequestOptions()
                            .fitCenter())
                    .into(new BitmapImageViewTarget(img) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            if (resource != null) {
                                RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getActivity().getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                img.setImageDrawable(circularBitmapDrawable);
                            }
                        }
                    });

            return convertView;
        }
    }

    public ArticleListFragment() {
    }
}
