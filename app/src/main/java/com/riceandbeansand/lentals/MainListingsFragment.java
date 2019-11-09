package com.riceandbeansand.lentals;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainListingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.mainlistings_content, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Main Listings");

        FloatingActionButton fab = view.findViewById(R.id.main_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(MainListings.this, AddItem.class);
                //startActivity(intent);
            }
        });

        return view;
    }
}
