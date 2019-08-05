package com.example.android.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import io.grpc.okhttp.internal.Util;

public class AdminActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private static final String TAG = "AdminActivity";
    private static final int PICTURE_RESULT = 42;
    private DatabaseReference mDatabaseReference;
    EditText titletext;
    EditText descriptiontext;
    EditText costtext;

    HolidayDeal deal;
    ImageView imageView;
    private Button uploadImagebtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_);
        // link to util

        //reference to Util
        mFirebaseDatabase = Utils.mFirebaseDatabase;
        mDatabaseReference = Utils.mDatabaseReference;

        titletext = (EditText) findViewById(R.id.deal_title);
        descriptiontext = (EditText) findViewById(R.id.deal_description);
        costtext = (EditText) findViewById(R.id.deal_cost);
        imageView = (ImageView) findViewById(R.id.imageView);


        Intent intent = getIntent();
        HolidayDeal deal = (HolidayDeal) intent.getSerializableExtra("Holiday Deal");
        if (deal == null) {
            // got there by clicking new travel deal and therefore create new holiday deal
            deal = new HolidayDeal();
        }
        this.deal = deal;
        titletext.setText(deal.getTitle());
        descriptiontext.setText(deal.getDescription());
        costtext.setText(deal.getCost());


        showImage(deal.getImageUrl());


        uploadImagebtn = (Button) findViewById(R.id.select_image_button);
        uploadImagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent, "insert picture"), PICTURE_RESULT);
                Log.d("Image upload button", "searching for Image to upload");
            }
        });
        ;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if (Utils.isAdmin) {
            menu.findItem(R.id.menu_delete).setVisible(true);
            menu.findItem(R.id.menu_save).setVisible(true);
            enableEditTexts(true);
            findViewById(R.id.select_image_button).setEnabled(true);

        } else {

            menu.findItem(R.id.menu_delete).setVisible(false);
            menu.findItem(R.id.menu_save).setVisible(false);
            enableEditTexts(false);
            findViewById(R.id.select_image_button).setEnabled(false);
        }

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                saveDeal();
                Toast.makeText(this, "Deal Saved", Toast.LENGTH_LONG).show();
                clear();
                backToList();
                return true;
            case R.id.menu_delete:
                deleteDeal();
                Toast.makeText(this, "Deal has been deleted", Toast.LENGTH_SHORT).show();
                backToList();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void saveDeal() {
        deal.setTitle(titletext.getText().toString());
        deal.setDescription(descriptiontext.getText().toString());
        deal.setCost(costtext.getText().toString());


        if (deal.getId() == null) {
            //create new deal
            mDatabaseReference.push().setValue(deal);
        } else {
            //display the deal with that id
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }
    }

    private void deleteDeal() {
        if (deal == null) {
            return;
        }
        mDatabaseReference.child(deal.getId()).removeValue();
        if (deal.getImageName() != null && deal.getImageName().isEmpty() == false) {
            StorageReference picRef = Utils.mStorage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete Image", "Image has been successfully deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Log.d("Delete Image", e.getMessage());

                }
            });
        }
    }

    //go back to the list
    private void backToList() {
        Intent intent = new Intent(this, UserActivity.class);
        startActivity(intent);
    }

    public void clear() {
        titletext.setText("");
        descriptiontext.setText("");
        costtext.setText("");
        titletext.requestFocus();
    }

    private void enableEditTexts(boolean isEnabled) {
        titletext.setEnabled(isEnabled);
        descriptiontext.setEnabled(isEnabled);
        costtext.setEnabled(isEnabled);
    }

    //uploading image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            assert data != null;
            Uri imageUri = data.getData();
            assert imageUri != null;
            final StorageReference reference = Utils.mStorageReference.
                    child(Objects.requireNonNull(imageUri.getLastPathSegment()));

            final UploadTask uploadTask = reference.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,
                    Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Task continuation to get the download URL
                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        String downloadUri = Objects.requireNonNull(task.getResult()).toString();
                        String fileName = uploadTask.getSnapshot().getStorage().getPath();
                        Log.e(TAG, "onComplete: -----------------" + fileName);
                        deal.setImageUrl(downloadUri);
                        deal.setImageName(fileName);
                        showImage(downloadUri);
                    } else {
                        Toast.makeText(AdminActivity.this, "", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    //displaying image
    public void showImage(String url) {
        if (url != null && !url.isEmpty()) {
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get()
                    .load(url)
                    .resize(width, width * 2 / 3)
                    .centerCrop()
                    .into(imageView);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }
}
