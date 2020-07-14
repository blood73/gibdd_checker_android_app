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
import ru.bloodsoft.gibddchecker.models.Fssp;
import ru.bloodsoft.gibddchecker.ui.recycler_views.EmptyRecyclerView;
import ru.bloodsoft.gibddchecker.ui.recycler_views.HistoryFsspRecyclerViewAdapter;

public class HistoryFsspFragment extends Fragment {

    GetDataFromDatabase getDataFromDatabase;
    private List<Fssp> fsspItemList;
    private FirebaseAnalytics mFirebaseAnalytics;

    public HistoryFsspFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_history_fssp, container, false);

        // Replaced RecyclerView with EmptyRecyclerView
        EmptyRecyclerView rv =
                (EmptyRecyclerView) rootView.findViewById(R.id.history_fssp_recycler_view);

        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        // Fetch the empty view from the layout and set it on
        // the new recycler view
        View emptyView = rootView.findViewById(R.id.fssp_history_empty_view);
        rv.setEmptyView(emptyView);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "History fssp");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);

        getDataFromDatabase = new GetDataFromDatabase();

        try {
            fsspItemList = getDataFromDatabase.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }

    private class GetDataFromDatabase extends AsyncTask<Void, Void, List<Fssp>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Fssp> doInBackground(Void... params) {

            HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(getContext());
            List<Fssp> fssp = databaseHelper.getAllFssp();

            return fssp;
        }

        @Override
        protected void onPostExecute(List<Fssp> fssp) {

            // Replaced RecyclerView with EmptyRecyclerView
            if (getView() != null) {
                EmptyRecyclerView rv =
                        (EmptyRecyclerView) getView().findViewById(R.id.history_fssp_recycler_view);

                HistoryFsspRecyclerViewAdapter adapter = new HistoryFsspRecyclerViewAdapter(getContext(), fssp);
                rv.setAdapter(adapter);

                adapter.setOnItemClickListener(new HistoryFsspRecyclerViewAdapter.ClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        Fssp fsspItem = fsspItemList.get(position);
                        Intent detailIntent = new Intent(getContext(), FsspActivity.class);
                        detailIntent.putExtra(FsspActivity.ARG_REGION, fsspItem.region);
                        detailIntent.putExtra(FsspActivity.ARG_FIRSTNAME, fsspItem.firstname);
                        detailIntent.putExtra(FsspActivity.ARG_LASTNAME, fsspItem.lastname);
                        detailIntent.putExtra(FsspActivity.ARG_PATRONYMIC, fsspItem.patronymic);
                        detailIntent.putExtra(FsspActivity.ARG_DOB, fsspItem.dob);
                        startActivity(detailIntent);
                    }
                });
            }
        }
    }
}