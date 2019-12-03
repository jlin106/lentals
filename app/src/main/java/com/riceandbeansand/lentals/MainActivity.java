package com.riceandbeansand.lentals;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ListingsFragment.OnDataPass {

    private static final String ACCESS_FINE_LOCATION = "Baltimore" ;
    private static final String TAG = "Baltimore" ;
    private Fragment mainListingsFragment;
    private FirebaseAuth mAuth;
    private boolean loggedIn = false; //this should be set in the Firebase db, here temporarily
    private FragmentTransaction transaction;
    ListingsFragment.OnDataPass dataPasser;
    String name = "JOHN DOE"; //default user name
    String uid = "";
    String pgUserId = null;

    @Override
    public void onDataPass(String pgUserId) {
        this.pgUserId = pgUserId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** get keyhash for fb login.
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.riceandbeansand.lentals",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.d("ERROR:", "NAME NOT FOUND");
        }
        catch (NoSuchAlgorithmException e) {
            Log.d("ERROR:", "NO SUCH ALGORITHMS EXCEPTION");
        }
         */

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);

        //This isn't really a nice way to set the items that the listingsFragment displays. Query is not serializable.
        //Handles case of fragment refreshing. Which calling method on fragment to set item would not do.
        Bundle args = new Bundle();
        args.putString("queryType", "mainItems");
        mainListingsFragment = new ListingsFragment();
        mainListingsFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mainListingsFragment).commit();

        setContentView(R.layout.activity_main);

        //create toolbars
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Main Listings");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.main_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /**
        //get map stuff
        String google_api_key = "AIzaSyBEfXjyqr8kFWjigV43vcCevu6EUQH6";
        Places.initialize(getApplicationContext(), google_api_key);
        PlacesClient placesClient = Places.createClient(this);
        // Use fields to define the data types to return.
        List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);

// Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request =
                FindCurrentPlaceRequest.newInstance(placeFields);

// Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);

            placeResponse.addOnCompleteListener(task --> {
                if (task.isSuccessful()){
                    FindCurrentPlaceResponse response = task.getResult();
                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        Log.i(TAG, String.format("Place '%s' has likelihood: %f",
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getLikelihood()));
                    }
                } else {
                    Exception exception = task.getException();
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                        }
                }
            });
        } else {
            // A local method to request required permissions;
            // See https://developer.android.com/training/permissions/requesting
           // getLocationPermission();
        }
             **/
       // SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
       //         .findFragmentById(R.id.toMaps);
        // mapFragment.getMapAsync((OnMapReadyCallback) this);



    }

    @Override
    protected void onStart() {
        super.onStart();
        View navHeader = ((NavigationView) findViewById(R.id.main_nav_view)).getHeaderView(0);

        navHeader.findViewById(R.id.profilePictureContainer).setClipToOutline(true);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            final TextView nameLabel = (TextView) navHeader.findViewById(R.id.nameLabel);
            final ImageView profilePictureView = (ImageView) navHeader.findViewById(R.id.userProfilePic);

            for (UserInfo profile : user.getProviderData()) {
                // Get UID specific to the provider
                uid = profile.getUid();
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference item = db.collection("users").document(user.getUid());
            item.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            System.out.println("DOCUMENT EXISTS");
                            String name = document.getString("name");
                            String picture = document.getString("picture");

                            nameLabel.setText(name);

                            try {
                                if (picture != null && !picture.isEmpty()) {
                                    byte[] decodedString = Base64.decode(picture, Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    profilePictureView.setImageBitmap(decodedByte);
                                }
                            } catch (Exception e) {
                                Log.d("TAG", "Couldn't set user profile picture");
                            }
                        }
                    }
                }
            });


        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.toMainListings) {
            Bundle args = new Bundle();
            args.putString("queryType", "mainItems");
            mainListingsFragment = new ListingsFragment();
            mainListingsFragment.setArguments(args);
            clearBackstack();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mainListingsFragment).commit();

        } else if (id == R.id.toMyItems) {
            Bundle args = new Bundle();
            args.putString("queryType", "myItems");
            args.putString("userId", mAuth.getCurrentUser().getUid());
            mainListingsFragment = new ListingsFragment();
            mainListingsFragment.setArguments(args);
            clearBackstack();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mainListingsFragment).commit();
        } else if (id == R.id.toMaps) {
            SupportMapFragment mFragment = SupportMapFragment.newInstance();
            getSupportActionBar().setTitle("Map");
            clearBackstack();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mFragment).commit();
            //mFragment.getMapAsync((OnMapReadyCallback) this);
        }
        else if (id == R.id.logOut) {
            clearBackstack();
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            Bundle args = new Bundle();
            args.putString("queryType", "searchItems");
            args.putString("userId", pgUserId);
            args.putString("searchQuery", query);
            Fragment searchListings = new ListingsFragment();
            searchListings.setArguments(args);
            getSupportFragmentManager().beginTransaction().addToBackStack(null)
                    .replace(R.id.fragment_container, searchListings).commit();
        }
    }

    private void logout() {
        mAuth.signOut();
        LoginManager.getInstance().logOut();

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);

        // Add SearchWidget.
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.options_menu_main_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

//    @Override
//    public boolean onQueryTextSubmit(String query) {
//        Intent searchIntent = new Intent(this, SearchableActivity.class);
//        searchIntent.putExtra(SearchManager.QUERY, query);
//
//        Bundle appData = new Bundle();
//        appData.putString(SearchableActivity.JARGON, pgUserId); // put extra data to Bundle
//        searchIntent.putExtra(SearchManager.APP_DATA, appData); // pass the search context data
//        searchIntent.setAction(Intent.ACTION_SEARCH);
//
//        startActivity(searchIntent);
//
//        Bundle args = new Bundle();
//        args.putString("queryType", "searchItems");
//        args.putString("userId", pgUserId);
//        args.putString("searchQuery", query);
//        Fragment searchListings = new ListingsFragment();
//        searchListings.setArguments(args);
//        getSupportFragmentManager().beginTransaction().addToBackStack(null)
//                .replace(R.id.fragment_container, searchListings).commit();
//
//        return true; // we start the search activity manually
//    }
//
//    @Override
//    public boolean onQueryTextChange(String newText) {
//        return false;
//    }

    private void clearBackstack() {
        try {
            FragmentManager.BackStackEntry entry = getSupportFragmentManager().getBackStackEntryAt(
                    0);
            getSupportFragmentManager().popBackStack(entry.getId(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().executePendingTransactions();
        } catch (Exception e) {
            getSupportFragmentManager().popBackStack(null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().executePendingTransactions();
        }

    }

}