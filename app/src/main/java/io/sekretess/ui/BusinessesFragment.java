package io.sekretess.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.sekretess.adapters.BusinessesAdapter;
import io.sekretess.R;
import io.sekretess.dto.BusinessDto;
import io.sekretess.repository.DbHelper;
import io.sekretess.utils.ApiClient;

import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class BusinessesFragment extends Fragment {
    private RecyclerView subscribedBusinessesRecycler;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Toolbar toolbar = getActivity().findViewById(R.id.my_toolbar);
        toolbar.setTitle("Sekretess/Businesses");

        View view = inflater.inflate(R.layout.businesses_fragment, container, false);
        DbHelper dbHelper = DbHelper.getInstance(getActivity().getApplicationContext());

        subscribedBusinessesRecycler = view.findViewById(R.id.businessesRecycler);
        List<BusinessDto> businessList = ApiClient.getBusinesses(getContext());
        List<String> subscribedBusinesses = ApiClient
                .getSubscribedBusinesses(getContext(), dbHelper.getAuthState().getIdToken());
        BusinessesAdapter businessesAdapter =
                new BusinessesAdapter(businessList, subscribedBusinesses, getParentFragmentManager());
        subscribedBusinessesRecycler.setAdapter(businessesAdapter);
        subscribedBusinessesRecycler.setLayoutManager(new GridLayoutManager(this.getActivity(), 4));
        return view;
    }

}