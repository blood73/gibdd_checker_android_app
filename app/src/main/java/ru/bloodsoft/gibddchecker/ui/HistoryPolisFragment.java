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
import ru.bloodsoft.gibddchecker.models.Polis;
import ru.bloodsoft.gibddchecker.ui.recycler_views.EmptyRecyclerView;
import ru.bloodsoft.gibddchecker.ui.recycler_views.HistoryPolisRecyclerViewAdapter;

public class HistoryPolisFragment extends Fragment {

    GetDataFromDatabase getDataFromDatabase;
    private List<Polis> polisItemList;
    private FirebaseAnalytics mFirebaseAnalytics;

    public HistoryPolisFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_history_polis, container, false);

        // Replaced RecyclerView with EmptyRecyclerView
        EmptyRecyclerView rv =
                (EmptyRecyclerView) rootView.findViewById(R.id.history_polis_recycler_view);

        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        // Fetch the empty view from the layout and set it on
        // the new recycler view
        View emptyView = rootView.findViewById(R.id.ensurance_polis_empty_view);
        rv.setEmptyView(emptyView);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "History polis");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);

        getDataFromDatabase = new GetDataFromDatabase();

        try {
            polisItemList = getDataFromDatabase.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }

    private class GetDataFromDatabase extends AsyncTask<Void, Void, List<Polis>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Polis> doInBackground(Void... params) {

            HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(getContext());
            List<Polis> polis = databaseHelper.getAllPolises();

            return polis;
        }

        @Override
        protected void onPostExecute(List<Polis> polis) {

            // Replaced RecyclerView with EmptyRecyclerView
            if (getView() != null) {
                EmptyRecyclerView rv =
                        (EmptyRecyclerView) getView().findViewById(R.id.history_polis_recycler_view);

                HistoryPolisRecyclerViewAdapter adapter = new HistoryPolisRecyclerViewAdapter(getContext(), polis);
                rv.setAdapter(adapter);

                adapter.setOnItemClickListener(new HistoryPolisRecyclerViewAdapter.ClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        Polis polisItem = polisItemList.get(position);
                        Intent detailIntent = new Intent(getContext(), PolisActivity.class);
                        detailIntent.putExtra(EaistoActivity.ARG_VIN, polisItem.vin);
                        detailIntent.putExtra(EaistoActivity.ARG_REG_NUMBER, polisItem.regNumber);
                        startActivity(detailIntent);
                    }
                });
            }
        }
    }
}