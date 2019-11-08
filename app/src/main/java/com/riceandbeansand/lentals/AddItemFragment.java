package com.riceandbeansand.lentals;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class AddItemFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.additem, container, false);

        final Button postItem = view.findViewById(R.id.postBtn);
        postItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TO DO: add item to database, return to where ever intent was sent
                //Intent intent = new Intent(AddItem.this, MainListings.class);
                //startActivity(intent);
            }
        });

        final Button cancelItem = view.findViewById(R.id.noPostBtn);
        cancelItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();;
            }
        });

        return view;
    }
}
