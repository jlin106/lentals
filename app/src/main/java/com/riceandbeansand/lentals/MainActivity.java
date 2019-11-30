package com.riceandbeansand.lentals;

import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ListingsFragment.OnDataPass, SearchView.OnQueryTextListener {

    private Fragment mainListingsFragment;
    private FirebaseAuth mAuth;
    private boolean loggedIn = false; //this should be set in the Firebase db, here temporarily
    private FragmentTransaction transaction;
    ListingsFragment.OnDataPass dataPasser;
    String userName = "JOHN DOE"; //default user name
    String uid = "";
    String pgUserId = null;

    @Override
    public void onDataPass(String pgUserId) {
        this.pgUserId = pgUserId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    }

    @Override
    protected void onStart() {
        super.onStart();
        View navHeader = ((NavigationView) findViewById(R.id.main_nav_view)).getHeaderView(0);

        navHeader.findViewById(R.id.profilePictureContainer).setClipToOutline(true);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Get user name
            userName = user.getDisplayName();
            TextView nameLabel = (TextView) navHeader.findViewById(R.id.nameLabel);
             nameLabel.setText(userName);

            for (UserInfo profile : user.getProviderData()) {
                // Get UID specific to the provider
                uid = profile.getUid();
            }

            ProfilePictureView profilePictureView = (ProfilePictureView) navHeader.findViewById(R.id.userProfilePic);
            profilePictureView.setProfileId(uid);
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
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mainListingsFragment).commit();

        } else if (id == R.id.toMyItems) {
            Bundle args = new Bundle();
            args.putString("queryType", "userItems");
            args.putString("userId", mAuth.getCurrentUser().getUid());
            mainListingsFragment = new ListingsFragment();
            mainListingsFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mainListingsFragment).commit();
        } else if (id == R.id.toMaps) {
            SupportMapFragment mFragment = SupportMapFragment.newInstance();
            getSupportActionBar().setTitle("Map");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mFragment).commit();
        }
        else if (id == R.id.logOut) {
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void logout() {
        mAuth.signOut();
        LoginManager.getInstance().logOut();

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
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

        return true; // we start the search activity manually
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

}