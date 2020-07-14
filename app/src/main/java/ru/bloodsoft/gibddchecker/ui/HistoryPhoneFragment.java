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
import ru.bloodsoft.gibddchecker.models.Phone;
import ru.bloodsoft.gibddchecker.ui.recycler_views.EmptyRecyclerView;
import ru.bloodsoft.gibddchecker.ui.recycler_views.HistoryPhoneRecyclerViewAdapter;

public class HistoryPhoneFragment extends Fragment {

    GetDataFromDatabase getDataFromDatabase;
    private List<Phone> phoneItemList;
    private FirebaseAnalytics mFirebaseAnalytics;

    public HistoryPhoneFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_history_phone, container, false);

        // Replaced RecyclerView with EmptyRecyclerView
        EmptyRecyclerView rv =
                (EmptyRecyclerView) rootView.findViewById(R.id.history_phone_recycler_view);

        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        // Fetch the empty view from the layout and set it on
        // the new recycler view
        View emptyView = rootView.findViewById(R.id.phone_empty_view);
        rv.setEmptyView(emptyView);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "History phone");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);

        getDataFromDatabase = new GetDataFromDatabase();

        try {
            phoneItemList = getDataFromDatabase.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }

    private class GetDataFromDatabase extends AsyncTask<Void, Void, List<Phone>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Phone> doInBackground(Void... params) {

            HistoryDatabaseHelper databaseHelper = HistoryDatabaseHelper.getInstance(getContext());
            List<Phone> phones = databaseHelper.getAllPhones();

            return phones;
        }

        @Override
        protected void onPostExecute(List<Phone> phones) {

            // Replaced RecyclerView with EmptyRecyclerView
            if (getView() != null) {
                EmptyRecyclerView rv =
                        (EmptyRecyclerView) getView().findViewById(R.id.history_phone_recycler_view);

                HistoryPhoneRecyclerViewAdapter adapter = new HistoryPhoneRecyclerViewAdapter(getContext(), phones);
                rv.setAdapter(adapter);

                adapter.setOnItemClickListener(new HistoryPhoneRecyclerViewAdapter.ClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        Phone phoneItem = phoneItemList.get(position);
                        Intent detailIntent = new Intent(getContext(), PhoneActivity.class);
                        detailIntent.putExtra(PhoneActivity.ARG_PHONE, phoneItem.phoneNumber);
                        startActivity(detailIntent);
                    }
                });
            }
        }
    }
}