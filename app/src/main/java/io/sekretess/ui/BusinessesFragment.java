package io.sekretess.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import io.sekretess.adapters.BusinessesAdapter;
import io.sekretess.R;
import io.sekretess.dto.BusinessDto;
import io.sekretess.repository.DbHelper;
import io.sekretess.utils.ApiClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class BusinessesFragment extends Fragment {
    private RecyclerView subscribedBusinessesRecycler;
    private BusinessesAdapter businessesAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Toolbar toolbar = getActivity().findViewById(R.id.my_toolbar);
        toolbar.setTitle("Businesses");

        View view = inflater.inflate(R.layout.businesses_fragment, container, false);
        DbHelper dbHelper = new DbHelper(getActivity().getApplicationContext());

        subscribedBusinessesRecycler = view.findViewById(R.id.businessesRecycler);
        List<String> subscribedBusinesses = ApiClient
                .getSubscribedBusinesses(getContext(), dbHelper.getAuthState().getIdToken());

        List<BusinessDto> businessList = ApiClient
                .getBusinesses(getContext())
                .stream()
                .peek(businessDto -> businessDto
                        .setSubscribed(isSubscribed(businessDto.getBusinessName(), subscribedBusinesses)))
                .collect(Collectors.toList());

        businessesAdapter = new BusinessesAdapter(getContext(), businessList, getParentFragmentManager());
        subscribedBusinessesRecycler.setAdapter(businessesAdapter);
        subscribedBusinessesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        SearchView searchView = view.findViewById(R.id.searchView);
        prepareSearchView(searchView);
        return view;
    }

    private boolean isSubscribed(String businessName, List<String> mSubscribedBusinesses) {
        if (mSubscribedBusinesses == null) return false;
        for (String subscribedBusiness : mSubscribedBusinesses) {
            if (subscribedBusiness.equalsIgnoreCase(businessName)) {
                return true;
            }
        }
        return false;
    }

    private void prepareSearchView(SearchView searchView) {
        SearchView.SearchAutoComplete viewById = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        viewById.setTextColor(Color.WHITE);

        // To change magnifier icon color
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        searchIcon.setColorFilter(Color.WHITE);
        // To change close button icon color
        ImageView searchCloseIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        searchCloseIcon.setColorFilter(Color.WHITE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                businessesAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

}