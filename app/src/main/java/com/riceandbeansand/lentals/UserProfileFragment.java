package com.riceandbeansand.lentals;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.facebook.login.widget.ProfilePictureView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileFragment extends Fragment {
    private String name;
    private String userId;
    String currentUserID;
    private String profileId;
    private String profilePicture;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.user_profile, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            name = bundle.getString("name", "UNKNOWN");
            userId = bundle.getString("userId");
        }

        view.findViewById(R.id.profilePictureContainer).setClipToOutline(true);
        final TextView userNameView = (TextView) view.findViewById(R.id.userName);
        final Button messageBtn = (Button) view.findViewById(R.id.message_btn);
        final ImageView profilePictureView = (ImageView) view.findViewById(R.id.userProfilePic);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            String profilePicture = doc.getString("picture");
            try {
                if (profilePicture != null && !profilePicture.isEmpty()) {
                    byte[] decodedString = Base64.decode(profilePicture, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    profilePictureView.setImageBitmap(decodedByte);
                }
            } catch (Exception e) {
                Log.d("TAG", "Couldn't set user profile picture");
            }
        });

        userNameView.setText(name);

        messageBtn.setText("Message");
        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                boolean lesser = currentUserID.compareTo(userId) < 0;
                String chatID = lesser ? currentUserID + userId : userId + currentUserID;
                args.putString("chatID", chatID);
                args.putString("name", name);
                Fragment chatFragment = new ChatFragment();
                chatFragment.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null)
                        .replace(R.id.fragment_container, chatFragment).commit();
            }
        });

        if(currentUserID.equals(userId)) {
            messageBtn.setVisibility(View.GONE);
        }

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
