package com.riceandbeansand.lentals;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class UserProfileFragment extends Fragment {
    private String name;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.user_profile, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Item Profile");

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            name = bundle.getString("name", "UNKNOWN");
            userId = bundle.getString("userId");
        }

        ((TextView) view.findViewById(R.id.userNameTitle)).setText(name);

        Bundle args = new Bundle();
        args.putString("queryType", "userItems");
        args.putString("userId", userId);
        Fragment userListings = new ListingsFragment();
        userListings.setArguments(args);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.list_container, userListings).commit();

        return view;
    }
}
