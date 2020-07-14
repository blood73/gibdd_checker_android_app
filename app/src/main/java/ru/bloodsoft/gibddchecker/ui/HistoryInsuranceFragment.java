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
import ru.bloodsoft.gibddchecker.models.Insurance;
import ru.bloodsoft.gibddchecker.ui.recycler_views.EmptyRecyclerView;
import ru.bloodsoft.gibddchecker.ui.recycler_views.HistoryInsuranceRecyclerViewAdapter;

public class HistoryInsuranceFragment extends Fragment {

    GetDataFromDatabase getDataFromDatabase;
    private List<Insurance> insuranceItemList;
    private FirebaseAnalytics mFirebaseAnalytics;

    public HistoryInsuranceFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_history_insurance, container, false);

        // Replaced RecyclerView with EmptyRecyclerView
        EmptyRecyclerView rv =
                (EmptyRecyclerView) rootView.findViewById(R.id.history_insurance_recycler_view);

        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        // Fetch the empty view from the layout and set it on
        // the new recycler view
        View emptyView = rootView.findViewById(R.id.insurance_history_empty_view);
        rv.setEmptyView(emptyView);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "History insurance");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);

        getDataFromDatabase = new GetDataFromDatabase();

        try {
            insuranceItemList = getDataFromDatabase.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }

    private class GetDataFromDatabase extends AsyncTask<Void, Void, List<Insurance>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Insurance> doInBackground(Void... params) {

            HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(getContext());
            List<Insurance> insurances = databaseHelper.getAllInsurances();

            return insurances;
        }

        @Override
        protected void onPostExecute(List<Insurance> insurances) {

            // Replaced RecyclerView with EmptyRecyclerView
            if (getView() != null) {
                EmptyRecyclerView rv =
                        (EmptyRecyclerView) getView().findViewById(R.id.history_insurance_recycler_view);

                HistoryInsuranceRecyclerViewAdapter adapter = new HistoryInsuranceRecyclerViewAdapter(getContext(), insurances);
                rv.setAdapter(adapter);

                adapter.setOnItemClickListener(new HistoryInsuranceRecyclerViewAdapter.ClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        Insurance insuranceItem = insuranceItemList.get(position);
                        Intent detailIntent = new Intent(getContext(), InsuranceActivity.class);
                        detailIntent.putExtra(InsuranceActivity.ARG_INSURANCE_NUMBER, insuranceItem.insuranceText);
                        detailIntent.putExtra(InsuranceActivity.ARG_INSURANCE_SERIAL, insuranceItem.insuranceSerial);
                        startActivity(detailIntent);
                    }
                });
            }
        }
    }
}