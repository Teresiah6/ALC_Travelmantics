package com.example.android.travelmantics;

import android.app.Activity;
import android.app.ListActivity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {
    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseReference;
    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseAuth.AuthStateListener mAuthListener;
    private static Utils utils;
    public static ArrayList<HolidayDeal> holidayDeals;
    private static UserActivity caller;
    private static final int RC_SIGN_IN = 111;
    public static boolean isAdmin;
    public static FirebaseStorage mStorage;
    public static StorageReference mStorageReference;


    private Utils() {
    }


    // open reference of a child
    public static void openFirebaseReference(String reference, final UserActivity callerActivity) {
        if (utils == null) {
            utils = new Utils();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            caller = callerActivity;
            mFirebaseAuth = FirebaseAuth.getInstance();
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    // first see if user is logged in to avoid being redirected to login screen
                    if (firebaseAuth.getCurrentUser() == null) {
                        Utils.signIn();
                    } else {
                        String userId = firebaseAuth.getUid();
                        checkAdmin(userId);

                    }
                    Toast.makeText(callerActivity.getBaseContext(), "Welcome Back!", Toast.LENGTH_SHORT).show();


                }

                ;


            };


            connectStorage();


        }


        holidayDeals = new ArrayList<HolidayDeal>();

        // open path called as a parameter
        mDatabaseReference = mFirebaseDatabase.getReference().child(reference);
    }

    public static void attachAuthListener() {
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    public static void detachAuthListener() {
        mFirebaseAuth.removeAuthStateListener(mAuthListener);
    }

    private static void signIn() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(), // for email sigin
                new AuthUI.IdpConfig.GoogleBuilder().build()); //for google sigin


// Create and launch sign-in intent
        caller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    private static void checkAdmin(String uid) {
        Utils.isAdmin = false;
        DatabaseReference reference = mFirebaseDatabase.getReference().child("administrators")
                .child(uid);
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Utils.isAdmin = true;
                caller.displayMenu();
                Log.d("Admin", "You are an administrator");

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        reference.addChildEventListener(childEventListener);

    }

    public static void connectStorage() {
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference().child("deals_pics");
    }
}
