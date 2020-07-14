package ru.bloodsoft.gibddchecker.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.database.HistoryDatabaseHelper;
import ru.bloodsoft.gibddchecker.models.Vin;
import ru.bloodsoft.gibddchecker.models.VinSearchType;
import ru.bloodsoft.gibddchecker.ui.quote.ArticleDetailActivity;
import ru.bloodsoft.gibddchecker.ui.quote.ArticleDetailFragment;
import ru.bloodsoft.gibddchecker.ui.recycler_views.EmptyRecyclerView;
import ru.bloodsoft.gibddchecker.ui.recycler_views.HistoryVinRecyclerViewAdapter;
import ru.bloodsoft.gibddchecker.util.SettingsStorage;

public class HistoryVinFragment extends Fragment {

    GetDataFromDatabase getDataFromDatabase;
    private List<Vin> vinItemList;
    private FirebaseAnalytics mFirebaseAnalytics;

    public HistoryVinFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_history_vin, container, false);

        // Replaced RecyclerView with EmptyRecyclerView
        EmptyRecyclerView rv =
                (EmptyRecyclerView) rootView.findViewById(R.id.history_vin_recycler_view);

        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        // Fetch the empty view from the layout and set it on
        // the new recycler view
        View emptyView = rootView.findViewById(R.id.vin_history_empty_view);
        rv.setEmptyView(emptyView);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "History vin");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);

        getDataFromDatabase = new GetDataFromDatabase();

        try {
            vinItemList = getDataFromDatabase.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }

    private class GetDataFromDatabase extends AsyncTask<Void, Void, List<Vin>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Vin> doInBackground(Void... params) {

            HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(getContext());
            List <Vin> vins = databaseHelper.getAllVins();

            return vins;
        }

        @Override
        protected void onPostExecute(List <Vin> vins) {

            // Replaced RecyclerView with EmptyRecyclerView
            EmptyRecyclerView rv = null;
            try {
                rv =
                        (EmptyRecyclerView) getView().findViewById(R.id.history_vin_recycler_view);
            } catch (Exception e) {
                //nothing
            }

            HistoryVinRecyclerViewAdapter adapter = new HistoryVinRecyclerViewAdapter(getContext(), vins);
            if (rv != null) {
                rv.setAdapter(adapter);
            }

            adapter.setOnItemClickListener(new HistoryVinRecyclerViewAdapter.ClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    Vin vinItem = vinItemList.get(position);

                    // Start the detail activity
                    VinSearchType searchType = new VinSearchType();
                    Integer searchVinInt = searchType.getSearchPosition(vinItem.vinType);
                    SettingsStorage settings = new SettingsStorage();

                    if (searchVinInt == null) {
                        searchVinInt = 1;
                    }

                    if (getContext() != null) {
                        if (searchVinInt <= 4) {
                            Intent detailIntent = new Intent(getContext(), ArticleDetailActivity.class);
                            detailIntent.putExtra(ArticleDetailFragment.ARG_ITEM_ID, searchVinInt.toString());
                            detailIntent.putExtra(ArticleDetailFragment.ARG_VIN, vinItem.vinText);
                            startActivity(detailIntent);
                        } else if (searchVinInt == 5) {
                            Intent detailIntent = new Intent(getContext(), ReestrActivity.class);
                            detailIntent.putExtra(ReestrActivity.ARG_VIN, vinItem.vinText);
                            startActivity(detailIntent);
                        /*} else if (searchVinInt == 6) {
                            Intent detailIntent = new Intent(getContext(), VinDecoderActivity.class);
                            detailIntent.putExtra(VinDecoderActivity.ARG_VIN, vinItem.vinText);
                            startActivity(detailIntent);*/
                        } else if (searchVinInt == 7 && settings.showMileageSearch()) {
                            Intent detailIntent = new Intent(getContext(), MileageActivity.class);
                            detailIntent.putExtra(MileageActivity.ARG_VIN, vinItem.vinText);
                            startActivity(detailIntent);
                        }
                    }
                }
            });
        }
    }
}