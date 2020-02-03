package com.projet.fitapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.io.IOException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "ERROR";
    private DrawerLayout drawer;
    private GoogleSignInAccount account;
    private GoogleSignInClient mGoogleSignInClient;
    private boolean isSigned = false;
    private Menu menu;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mDatabaseUserReference;
    private DatabaseReference mDatabaseProfileReference;
    private DatabaseReference mDatabaseGoalReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mDatabaseUserReference = mDatabaseReference.child("users");
        mDatabaseProfileReference = mDatabaseReference.child("profiles");
        mDatabaseGoalReference = mDatabaseReference.child("goals");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        menu = navigationView.getMenu();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},1053);
        }

        //handle device rotation
        if (savedInstanceState == null) {
            //open fragment activity
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new WelcomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_github);
            onClickLogInItem();

        }


    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_activity:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ActivityFragment()).commit();
                break;
            case R.id.nav_profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
                break;
            case R.id.nav_stats:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new StatsFragment()).commit();
                break;
            case R.id.nav_github:
                onClickGitItem();
                break;
            case R.id.nav_mail:
                onClickMailItem();
                break;
            case R.id.nav_logout:
                if (isSigned) onClickLogoutItem();
                else onClickLogInItem();
                updateMenuLogState();

                break;

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * Open a link redirecting to my Github page
     */
    private void onClickGitItem() {
        Uri uri = Uri.parse("https://github.com/KellySeng");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    /**
     * Send mail through an application already existing
     */
    private void onClickMailItem() {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        String[] contact = {"kelly.seng@etu.upmc.fr"};
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "FitApp");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, contact);
        emailIntent.setType("message/rfc822");
        startActivity(Intent.createChooser(emailIntent, "Send email"));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case 101:
                    try {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        if (task != null) {
                            account = task.getResult(ApiException.class);
                            final TextView name = findViewById(R.id.nav_name);
                            name.setText(account.getDisplayName());


                            TextView email = findViewById(R.id.nav_email);
                            email.setText(account.getEmail());

                            final ImageView photo = findViewById(R.id.nav_photo);
                            isSigned = true;

                            //save user in firebase
                            final String id = Integer.toString(account.getEmail().hashCode());

                            isSigned = true;
                            updateMenuLogState();
                            mDatabaseProfileReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.hasChild(id)) {
                                        User u = new User(account.getFamilyName(), account.getGivenName(),
                                                account.getEmail());
                                        Profile p = new Profile("", "", "", "",
                                                "", "", "");
                                        Goal g = new Goal("undefined");
                                        mDatabaseUserReference.child(id).setValue(u);
                                        mDatabaseProfileReference.child(id).setValue(p);
                                        mDatabaseGoalReference.child(id).setValue(g);
                                    } else {
                                        String nickname = dataSnapshot.child(id)
                                                .child("nickname").getValue().toString();

                                        name.setText(nickname);

                                        String imageUrl = dataSnapshot.child(id)
                                                .child("imageUrl").getValue().toString();
                                        if (!imageUrl.isEmpty() && !imageUrl.contains("http")) {
                                            try {
                                                Bitmap imageBitmap = decodeFromFirebaseBase64(imageUrl);
                                                photo.setImageBitmap(imageBitmap);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    } catch (ApiException e) {
                        // The ApiException status code indicates the detailed failure reason.
                        Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
                    }
                    break;
            }
    }


    @Override
    public void onStart() {
        super.onStart();
        GoogleSignInAccount alreadyloggedAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (alreadyloggedAccount != null) {
            isSigned = true;
        } else {
            isSigned = false;
        }
        updateMenuLogState();
    }

    /**
     * Disconnect a google account from the application
     */
    private void onClickLogoutItem() {
        mGoogleSignInClient.signOut();
        TextView name = findViewById(R.id.nav_name);
        name.setText("");

        TextView email = findViewById(R.id.nav_email);
        email.setText("");

        ImageView photo = findViewById(R.id.nav_photo);
        photo.setImageResource(R.mipmap.ic_launcher_round);
       // Toast.makeText(this, "You have been disconnected", Toast.LENGTH_SHORT).show();

        finishAndRemoveTask();
    }

    /**
     * Connect a google account from the application
     */
    private void onClickLogInItem() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //start an intent for signing with google mail
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 101);
    }

    /**
     * Enable/Disable login or logout
     */
    private void updateMenuLogState() {
        MenuItem logItem = menu.findItem(R.id.nav_logout);
        if (isSigned) {
            logItem.setTitle("Logout");
            logItem.setIcon(R.drawable.ic_logout);
        } else {
            logItem.setTitle("Login");
            logItem.setIcon(R.drawable.ic_login);
        }
    }

    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

}


