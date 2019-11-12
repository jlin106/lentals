package com.riceandbeansand.lentals;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class AddItemFragment extends Fragment {

    final int GALLERY_REQUEST_CODE = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.additem, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Add Item");

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
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                Double price = Double.parseDouble(((TextView) getView().findViewById(R.id.rateText)).getText().toString());
                String itemName = ((TextView) getView().findViewById(R.id.itemNameText)).getText().toString();

                //get image as base64
                String encodedString = "";
                try {
                    Uri imageURI = (Uri) ((ImageView) getView().findViewById(R.id.imageHolder)).getTag();
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageURI);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    byte[] byteArray = outputStream.toByteArray();
                    encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
                } catch (Exception e) {
                    Log.d("App", "Failed to encode image " + e);
                }

                //Should validate stuff server side
                if (itemName == "" || encodedString == "") {
                    return;
                }

                Map<String, Object> docData = new HashMap<>();
                docData.put("name", itemName);
                docData.put("price", price);
                docData.put("userID", userID);
                docData.put("userName", userName); //should be gotten from userID/uid, but have to create users collection (keyed by uid) manually
                docData.put("image", encodedString);

                //not secure -- DB permissions are such that people can post under any userID
                db.collection("items").document().set(docData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("App", "DocumentSnapshot successfully written!");
                                getActivity().getSupportFragmentManager().popBackStack();;
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("App", "Error writing document", e);
                            }
                        });
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
                    ImageView imageHolder =((ImageView) getView().findViewById(R.id.imageHolder));
                    imageHolder.setImageURI(selectedImage);
                    imageHolder.setTag(selectedImage);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
