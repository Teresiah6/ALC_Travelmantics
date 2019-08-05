package com.example.android.travelmantics;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DealsAdapter extends RecyclerView.Adapter<DealsAdapter.DealViewHolder> {

    ArrayList<HolidayDeal> deals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildListener;
    private ImageView imageDeal;

    public DealsAdapter (){
        //Utils.openFirebaseReference("holidayDeals");
        //reference to Util
        mFirebaseDatabase = Utils.mFirebaseDatabase;
        mDatabaseReference = Utils.mDatabaseReference;
        this.deals = Utils.holidayDeals;
        mChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                HolidayDeal holidayDeal = dataSnapshot.getValue(HolidayDeal.class);
                assert holidayDeal != null;
                Log.d("Deal:", holidayDeal.getTitle() );
                holidayDeal.setId(dataSnapshot.getKey());
                deals.add(holidayDeal);
                notifyItemInserted(deals.size()-1);

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
      //  deals = Utils.holidayDeals;
        mDatabaseReference.addChildEventListener(mChildListener);
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context =parent.getContext();
        View itemView = LayoutInflater.from(context). inflate(R.layout.recycler_view_rows, parent,false);
        return  new DealViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        HolidayDeal holidayDeal =deals.get(position);
        holder.bind(holidayDeal);

    }

    @Override
    public int getItemCount() {
        return deals.size();
    }

    public class DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView titleTextView;
        TextView descriptionTextView;
        TextView costTextView;

        public DealViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
            descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
            costTextView = (TextView) itemView.findViewById(R.id.costTextView);
           imageDeal = (ImageView) itemView.findViewById(R.id.dealImage);
            itemView.setOnClickListener(this);
        }

        public void bind (HolidayDeal holidayDeal){
            //takes holidayDeal as parameter and puts into the textview
            titleTextView.setText(holidayDeal.getTitle());
            descriptionTextView.setText(holidayDeal.getDescription());
            costTextView.setText(holidayDeal.getCost());
            showImage(holidayDeal.getImageUrl());
            Log.d("Show Image", "Image successfully displayed");

        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Log.d("Clicked", String.valueOf(position));
            HolidayDeal selectedDeal = deals.get(position);
            // open new activity to view deal
            Intent intent = new Intent (view.getContext(), AdminActivity.class);
            intent.putExtra("Holiday Deal",selectedDeal);
            view.getContext().startActivity(intent);

        }
        private void showImage(String url){
            if (url != null && url.isEmpty()== false){
                Picasso.get()
                        .load(url)
                        .resize(160,160)
                       .centerCrop ()
                        .into(imageDeal);
            }

        }
    }

}
