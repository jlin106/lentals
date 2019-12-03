package com.riceandbeansand.lentals;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.w3c.dom.Text;

public class AddItemFragment extends Fragment {

    final int GALLERY_REQUEST_CODE = 0;
    private boolean isNew = true;
    private String itemID;
    private String selectedImage;
    private String profPicEncodedString; // can't access local var from inner classes

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.additem, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Add Item");

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final Switch visibleSwitch = (Switch) view.findViewById(R.id.visibilitySwitch);
        final TextView rateView = (TextView) view.findViewById(R.id.rateText);
        final TextView itemNameView = (TextView) view.findViewById(R.id.itemNameText);
        final TextView descripView = (TextView) view.findViewById(R.id.descriptionText);
        final ImageView imageView = (ImageView) view.findViewById(R.id.imageHolder);
        final Button chooseImageBtn = view.findViewById(R.id.chooseImageBtn);
        final Button postItem = view.findViewById(R.id.postBtn);
        final Button cancelItem = view.findViewById(R.id.noPostBtn);
        final Button deleteItem = view.findViewById(R.id.deleteItemBtn);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            isNew = false;
            deleteItem.setVisibility(View.VISIBLE);
            postItem.setText("Update!");
            itemID = bundle.getString("itemID", "");
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Update Item");
            DocumentReference item = db.collection("items").document(itemID);
            item.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            System.out.println("DOCUMENT EXISTS");
                            String selectedName = document.getString("name");
                            Double selectedPrice = document.getDouble("price");
                            selectedImage = document.getString("image");
                            String selectedDescrip = document.getString("descrip");
                            boolean selectedVisible = document.getBoolean("visible");

                            itemNameView.setText(selectedName);
                            rateView.setText(selectedPrice.toString());
                            descripView.setText(selectedDescrip);
                            visibleSwitch.setChecked(selectedVisible);

                            byte[] decodedString = Base64.decode(selectedImage, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            imageView.setImageBitmap(decodedByte);
                        }
                    }
                }
            });
        }

        chooseImageBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent pickPhoto =new Intent(Intent.ACTION_PICK);
                pickPhoto.setType("image/*");
                String[] mimeTypes = {"image/jpeg", "image/png"};
                pickPhoto.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                startActivityForResult(pickPhoto, GALLERY_REQUEST_CODE);
            }
        });

        postItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                String userID = user.getUid();
                String userName = user.getDisplayName();
                boolean visible = visibleSwitch.isChecked();
                Double price = Double.parseDouble(rateView.getText().toString());
                String itemName = (itemNameView.getText().toString());
                String description = (descripView.getText().toString());
                String profileId = "";
                for (UserInfo profile : user.getProviderData()) {
                    profileId = profile.getUid();
                }

                //get image as base64
                String itemImgEncodedString = selectedImage;

                try {
                    Uri imageURI = (Uri) imageView.getTag();
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageURI);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    byte[] byteArray = outputStream.toByteArray();
                    itemImgEncodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
                } catch (Exception e) {
                    Log.d("App", "Failed to encode image " + e);
                }

                db.collection("users").document(userID).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        profPicEncodedString = document.getString("picture");
                                    }
                                }
                            }
                        });

                //Should validate stuff server side
                if (itemName == "" || itemImgEncodedString == "") {
                    return;
                }

                Map<String, Object> docData = new HashMap<>();
                docData.put("name", itemName);
                docData.put("visible", visible);
                docData.put("price", price);
                docData.put("userID", userID);
                docData.put("userName", userName); //should be gotten from userID/uid, but have to create users collection (keyed by uid) manually
                docData.put("image", itemImgEncodedString);
                docData.put("descrip", description);
                docData.put("profileID", profileId);
                docData.put("profilePicture", profPicEncodedString);

                if (isNew) {
                    //not secure -- DB permissions are such that people can post under any userID
                    db.collection("items").document().set(docData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("App", "New DocumentSnapshot successfully written!");
                                    getActivity().getSupportFragmentManager().popBackStack();;
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("App", "Error writing document", e);
                                }
                            });
                } else {
                    db.collection("items").document(itemID).set(docData, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("App", "Updated DocumentSnapshot successfully written!");
                                    getActivity().getSupportFragmentManager().popBackStack();;
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("App", "Error writing document", e);
                                }
                            });
                }
            }
        });

        cancelItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();;
            }
        });

        deleteItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                db.collection("items").document(itemID).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("App", "DocumentSnapshot successfully deleted!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("App", "Error deleting document", e);
                            }
                        });
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
