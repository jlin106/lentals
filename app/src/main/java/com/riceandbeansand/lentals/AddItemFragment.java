package com.riceandbeansand.lentals;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class AddItemFragment extends Fragment {

    final int GALLERY_REQUEST_CODE = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.additem, container, false);

        final Button chooseImageBtn = view.findViewById(R.id.chooseImageBtn);
        chooseImageBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent pickPhoto =new Intent(Intent.ACTION_PICK);
                pickPhoto.setType("image/*");
                String[] mimeTypes = {"image/jpeg", "image/png"};
                pickPhoto.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                startActivityForResult(pickPhoto, GALLERY_REQUEST_CODE);

            }
        });


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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case GALLERY_REQUEST_CODE:
                    //data.getData returns the content URI for the selected Image
                    Uri selectedImage = data.getData();
                    ((ImageView) getView().findViewById(R.id.imageHolder)).setImageURI(selectedImage);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
