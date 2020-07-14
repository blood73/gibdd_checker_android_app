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
import ru.bloodsoft.gibddchecker.models.Plate;
import ru.bloodsoft.gibddchecker.ui.recycler_views.EmptyRecyclerView;
import ru.bloodsoft.gibddchecker.ui.recycler_views.HistoryPlateRecyclerViewAdapter;

public class HistoryPlateFragment extends Fragment {

    GetDataFromDatabase getDataFromDatabase;
    private List<Plate> plateItemList;
    private FirebaseAnalytics mFirebaseAnalytics;

    public HistoryPlateFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_history_plate, container, false);

        // Replaced RecyclerView with EmptyRecyclerView
        EmptyRecyclerView rv =
                (EmptyRecyclerView) rootView.findViewById(R.id.history_plate_recycler_view);

        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        // Fetch the empty view from the layout and set it on
        // the new recycler view
        View emptyView = rootView.findViewById(R.id.plate_empty_view);
        rv.setEmptyView(emptyView);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "History plate");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);

        getDataFromDatabase = new GetDataFromDatabase();

        try {
            plateItemList = getDataFromDatabase.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }

    private class GetDataFromDatabase extends AsyncTask<Void, Void, List<Plate>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Plate> doInBackground(Void... params) {

            HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(getContext());
            List<Plate> plates = databaseHelper.getAllPlates();

            return plates;
        }

        @Override
        protected void onPostExecute(List<Plate> plates) {

            // Replaced RecyclerView with EmptyRecyclerView
            EmptyRecyclerView rv =
                    (EmptyRecyclerView) getView().findViewById(R.id.history_plate_recycler_view);

            HistoryPlateRecyclerViewAdapter adapter = new HistoryPlateRecyclerViewAdapter(getContext(), plates);
            rv.setAdapter(adapter);

            adapter.setOnItemClickListener(new HistoryPlateRecyclerViewAdapter.ClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    Plate plateItem = plateItemList.get(position);
                    Intent detailIntent = new Intent(getContext(), PlateActivity.class);
                    detailIntent.putExtra(PlateActivity.ARG_PLATE, plateItem.plateNumber);
                    startActivity(detailIntent);
                }
            });
        }
    }
}