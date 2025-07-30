package io.sekretess.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import io.sekretess.adapters.BusinessesAdapter;
import io.sekretess.R;
import io.sekretess.dto.BusinessDto;
import io.sekretess.repository.DbHelper;
import io.sekretess.utils.ApiClient;
import io.sekretess.utils.FileTarget;

import java.util.List;
import java.util.stream.Collectors;

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
        toolbar.setTitle("Businesses");

        View view = inflater.inflate(R.layout.businesses_fragment, container, false);
        DbHelper dbHelper = DbHelper.getInstance(getActivity().getApplicationContext());

        subscribedBusinessesRecycler = view.findViewById(R.id.businessesRecycler);
        List<String> subscribedBusinesses = ApiClient
                .getSubscribedBusinesses(getContext(), dbHelper.getAuthState().getIdToken());

        List<BusinessDto> businessList = ApiClient
                .getBusinesses(getContext())
                .stream()
                .peek(businessDto -> businessDto
                        .setSubscribed(isSubscribed(businessDto.getBusinessName(), subscribedBusinesses)))
                .peek(businessDto -> Picasso
                        .get()
                        .load(Uri.parse(businessDto.getIcon()))
                        .into(new FileTarget(getContext(), businessDto.getBusinessName())))
                .collect(Collectors.toList());

        BusinessesAdapter businessesAdapter =
                new BusinessesAdapter(getContext(), businessList, getParentFragmentManager());
        subscribedBusinessesRecycler.setAdapter(businessesAdapter);
        subscribedBusinessesRecycler.setLayoutManager(new GridLayoutManager(this.getActivity(), 4));
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

}