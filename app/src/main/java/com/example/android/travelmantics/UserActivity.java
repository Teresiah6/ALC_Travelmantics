package com.example.android.travelmantics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ChildEventListener;



import java.util.ArrayList;



public class UserActivity extends AppCompatActivity {
   ArrayList<HolidayDeal> deals;
   private FirebaseDatabase mFirebaseDatabase;
   private DatabaseReference mDatabaseReference;
   private ChildEventListener mChildListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_activity_menu, menu);
        MenuItem insertMenu =menu.findItem(R.id.insert_menu);
        if (Utils.isAdmin == true){
            insertMenu.setVisible(true);
        }else{
            insertMenu.setVisible(false);
        }

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.insert_menu:
                Intent intent = new Intent (this, AdminActivity.class);
                startActivity(intent);
                return true;
            case R.id.logout_menu:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                             Log.d("Log out", "user Logged out");
                             Utils.attachAuthListener();
                            }
                        });
                Utils.detachAuthListener();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.detachAuthListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.openFirebaseReference("holidayDeals", this);
        RecyclerView recyclerViewDeals = (RecyclerView) findViewById(R.id.deals_recycler_view);
        final DealsAdapter adapter = new DealsAdapter();
        recyclerViewDeals.setAdapter(adapter);
        LinearLayoutManager dealsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewDeals.setLayoutManager(dealsLayoutManager);
        Utils.attachAuthListener();
    }


    public void displayMenu(){
        invalidateOptionsMenu(); // shows that the contents of the Menu have changed
    }
}
