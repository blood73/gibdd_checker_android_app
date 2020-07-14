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
import ru.bloodsoft.gibddchecker.models.Fine;
import ru.bloodsoft.gibddchecker.ui.recycler_views.EmptyRecyclerView;
import ru.bloodsoft.gibddchecker.ui.recycler_views.HistoryFinesRecyclerViewAdapter;

public class HistoryFinesFragment extends Fragment {

    GetDataFromDatabase getDataFromDatabase;
    private List<Fine> finesItemList;
    private FirebaseAnalytics mFirebaseAnalytics;

    public HistoryFinesFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_history_fines, container, false);

        // Replaced RecyclerView with EmptyRecyclerView
        EmptyRecyclerView rv =
                (EmptyRecyclerView) rootView.findViewById(R.id.history_fines_recycler_view);

        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        // Fetch the empty view from the layout and set it on
        // the new recycler view
        View emptyView = rootView.findViewById(R.id.fines_history_empty_view);
        rv.setEmptyView(emptyView);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "History fines");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);

        getDataFromDatabase = new GetDataFromDatabase();

        try {
            finesItemList = getDataFromDatabase.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }

    private class GetDataFromDatabase extends AsyncTask<Void, Void, List<Fine>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Fine> doInBackground(Void... params) {

            HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(getContext());
            List<Fine> fines = databaseHelper.getAllFines();

            return fines;
        }

        @Override
        protected void onPostExecute(List<Fine> fines) {

            // Replaced RecyclerView with EmptyRecyclerView
            if (getView() != null) {
                EmptyRecyclerView rv =
                        (EmptyRecyclerView) getView().findViewById(R.id.history_fines_recycler_view);

                HistoryFinesRecyclerViewAdapter adapter = new HistoryFinesRecyclerViewAdapter(getContext(), fines);
                rv.setAdapter(adapter);

                adapter.setOnItemClickListener(new HistoryFinesRecyclerViewAdapter.ClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        Fine fineItem = finesItemList.get(position);
                        Intent detailIntent = new Intent(getContext(), FinesActivity.class);
                        detailIntent.putExtra(FinesActivity.ARG_FINES_REG_NUMBER, fineItem.regNumber);
                        detailIntent.putExtra(FinesActivity.ARG_FINES_STS_NUMBER, fineItem.stsNumber);
                        startActivity(detailIntent);
                    }
                });
            }
        }
    }
}