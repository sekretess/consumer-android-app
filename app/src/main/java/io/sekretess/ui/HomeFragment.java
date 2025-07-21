package io.sekretess.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.sekretess.R;
import io.sekretess.adapters.SubscribedBusinessesAdapter;
import io.sekretess.repository.DbHelper;
import io.sekretess.utils.ApiClient;

import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.subscribed_businesses_fragment, container, false);
        DbHelper dbHelper = DbHelper.getInstance(getActivity().getApplicationContext());

        recyclerView = view.findViewById(R.id.businessSubscriptionRecycler);

        List<String> subscribedBusinesses = ApiClient
                .getSubscribedBusinesses(getContext(), dbHelper.getAuthState().getIdToken());
        SubscribedBusinessesAdapter businessesAdapter =
                new SubscribedBusinessesAdapter(subscribedBusinesses);
        recyclerView.setAdapter(businessesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        return view;
    }
}
