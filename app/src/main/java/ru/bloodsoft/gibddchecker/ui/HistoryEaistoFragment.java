package ru.bloodsoft.gibddchecker.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.database.HistoryDatabaseHelper;
import ru.bloodsoft.gibddchecker.models.Eaisto;
import ru.bloodsoft.gibddchecker.models.Insurance;
import ru.bloodsoft.gibddchecker.ui.recycler_views.EmptyRecyclerView;
import ru.bloodsoft.gibddchecker.ui.recycler_views.HistoryEaistoRecyclerViewAdapter;

public class HistoryEaistoFragment extends Fragment {

    GetDataFromDatabase getDataFromDatabase;
    private List<Eaisto> eaistoItemList;
    private FirebaseAnalytics mFirebaseAnalytics;

    public HistoryEaistoFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_history_eaisto, container, false);

        // Replaced RecyclerView with EmptyRecyclerView
        EmptyRecyclerView rv =
                (EmptyRecyclerView) rootView.findViewById(R.id.history_eaisto_recycler_view);

        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        // Fetch the empty view from the layout and set it on
        // the new recycler view
        View emptyView = rootView.findViewById(R.id.ensurance_eaisto_empty_view);
        rv.setEmptyView(emptyView);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "History eaisto");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);

        getDataFromDatabase = new GetDataFromDatabase();

        try {
            eaistoItemList = getDataFromDatabase.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }

    private class GetDataFromDatabase extends AsyncTask<Void, Void, List<Eaisto>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Eaisto> doInBackground(Void... params) {

            HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(getContext());
            List<Eaisto> eaisto = databaseHelper.getAllEaisto();

            return eaisto;
        }

        @Override
        protected void onPostExecute(List<Eaisto> eaisto) {

            // Replaced RecyclerView with EmptyRecyclerView
            if (getView() != null) {
                EmptyRecyclerView rv =
                        (EmptyRecyclerView) getView().findViewById(R.id.history_eaisto_recycler_view);

                HistoryEaistoRecyclerViewAdapter adapter = new HistoryEaistoRecyclerViewAdapter(getContext(), eaisto);
                rv.setAdapter(adapter);

                adapter.setOnItemClickListener(new HistoryEaistoRecyclerViewAdapter.ClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        Eaisto eaistoItem = eaistoItemList.get(position);
                        Intent detailIntent = new Intent(getContext(), EaistoActivity.class);
                        detailIntent.putExtra(EaistoActivity.ARG_VIN, eaistoItem.vin);
                        detailIntent.putExtra(EaistoActivity.ARG_REG_NUMBER, eaistoItem.regNumber);
                        startActivity(detailIntent);
                    }
                });
            }
        }
    }
}