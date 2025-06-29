package com.sekretess.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sekretess.adapters.BusinessesAdapter;
import com.sekretess.R;
import com.sekretess.dto.BusinessDto;
import com.sekretess.repository.DbHelper;
import com.sekretess.utils.ApiClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class BusinessesFragment extends Fragment {
    private RecyclerView recyclerView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.businesses_fragment, container, false);
        DbHelper dbHelper = DbHelper.getInstance(getActivity().getApplicationContext());

        recyclerView = view.findViewById(R.id.businessesRecycler);
        List<BusinessDto> businessList = ApiClient.getBusinesses()
                .stream()
                .map(BusinessDto::new)
                .collect(Collectors.toList());
        List<String> subscribedBusinesses = ApiClient
                .getSubscribedBusinesses(dbHelper.getAuthState().getIdToken());
        BusinessesAdapter businessesAdapter =
                new BusinessesAdapter(businessList, subscribedBusinesses);
        recyclerView.setAdapter(businessesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        return view;
    }

}