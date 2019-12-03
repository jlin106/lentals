package com.riceandbeansand.lentals;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import kotlin.Unit;

public class AddItemFragment extends Fragment {

    final int GALLERY_REQUEST_CODE = 0;
    private String selectedImage;
    final long MAX_IMAGE_SIZE = 8*1024*1024;

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
        final boolean isNewItem = bundle == null;
        final String itemID = bundle!=null ? bundle.getString("itemID", "") : "";

        if (!isNewItem) {
            deleteItem.setVisibility(View.VISIBLE);
            postItem.setText("Update!");
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Update Item");
            DocumentReference item = db.collection("items").document(itemID);
            item.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        DocumentSnapshot doc= task.getResult();

                        itemNameView.setText(doc.getString("name"));
                        rateView.setText(doc.getDouble("price").toString());
                        descripView.setText(doc.getString("descrip"));
                        visibleSwitch.setChecked(doc.getBoolean("visible"));

                        UtilityKt.getImageFileFromGSUrlWithCache(doc.getString("imagePath"), getActivity().getCacheDir(), (File file) -> {
                            Bitmap decodedBytes = BitmapFactory.decodeFile(file.getAbsolutePath());
                            imageView.setImageBitmap(decodedBytes);
                            return Unit.INSTANCE; //required by Java for kotlin interop
                        });

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

        postItem.setOnClickListener(v -> {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();

            Map<String, Object> docData = new HashMap<>();
            docData.put("name", itemNameView.getText().toString());
            docData.put("visible", visibleSwitch.isChecked());
            docData.put("price", Double.parseDouble(rateView.getText().toString()));
            docData.put("userID", user.getUid());
            docData.put("userName", user.getDisplayName()); //should be gotten from userID/uid, but have to create users collection (keyed by uid) manually
            docData.put("descrip", descripView.getText().toString());
            docData.put("profileID", "");

            for (UserInfo profile : user.getProviderData()) {
                docData.put("profileID", profile.getUid());
            }
            String postedItemID = itemID.equals("") ? db.collection("items").document().getId() : itemID;

            if (isNewItem) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                String imageKey = "mainImage" + postedItemID;
                StorageReference imageRef = storage.getReference().child("images").child(imageKey);
                Uri imageURI = (Uri) imageView.getTag();
                Cursor returnCursor = getActivity().getContentResolver().query(imageURI, null, null, null, null);
                returnCursor.moveToFirst();
                long size = returnCursor.getLong(returnCursor.getColumnIndex(OpenableColumns.SIZE));
                if (size > MAX_IMAGE_SIZE) {
                    //ideally we should downscale their image for them if it's too large
                    Toast.makeText(getActivity(),
                            "Please make your image < 8 MB",
                            Toast.LENGTH_SHORT).show();
                }
                docData.put("imagePath", imageRef.toString());
                try {
                    UtilityKt.setImageFileToGSUrlWithCache(imageRef.toString(), getActivity().getCacheDir(),
                            getActivity().getContentResolver().openInputStream(imageURI));
                } catch (Exception e) {
                    Log.d("App", "failed to open image file", e);
                }
            }
            //Should validate stuff server side
            if (docData.get("name") == "") {
                return;
            }

            db.collection("items").document(postedItemID).set(docData, SetOptions.merge())
                    .addOnSuccessListener(n -> {
                        Log.d("App", "New DocumentSnapshot successfully written!");
                        getActivity().getSupportFragmentManager().popBackStack();
                    }).addOnFailureListener(e -> {
                Log.w("App", "Error writing document", e);
            });

        });


        cancelItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();;
            }
        });

        deleteItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                db.collection("items").document(itemID).get().addOnSuccessListener(doc -> {
                    storage.getReferenceFromUrl(doc.getString("imagePath")).delete().addOnFailureListener(e -> {
                        Log.d("app", "failed to delete blob image ", e);
                    });
                    db.collection("items").document(itemID).delete()
                            .addOnFailureListener(e -> Log.w("App", "Error deleting document", e));
                });
                getActivity().getSupportFragmentManager().popBackStack();
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
