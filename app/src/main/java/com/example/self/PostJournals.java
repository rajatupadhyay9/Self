package com.example.self;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;

import Util.JournalAPI;
import model.Journal;

public class PostJournals extends AppCompatActivity implements View.OnClickListener {
    private Button saveButton;
    private ProgressBar progressBar;
    private ImageView imageView, addPhotoButton;
    private EditText titleEditText, thoughtEditText;

    private String currentUserEmail, currentUserId;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journals);
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.post_progressBar);
        titleEditText = findViewById(R.id.post_title_et);
        thoughtEditText = findViewById(R.id.post_description_et);
        saveButton = findViewById(R.id.post_button);
        addPhotoButton = findViewById(R.id.post_camera_button);
        imageView = findViewById(R.id.post_imageView);

        saveButton.setOnClickListener(this);
        addPhotoButton.setOnClickListener(this);
        progressBar.setVisibility(View.INVISIBLE);

        currentUserId = JournalAPI.getInstance().getUserID();
        currentUserEmail = JournalAPI.getInstance().getUserEmail();
        collectionReference = db.collection(currentUserId);
        storageReference = FirebaseStorage.getInstance().getReference();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {

                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.post_button:
                progressBar.setVisibility(View.VISIBLE);
                saveJournal();
                break;
            case R.id.post_camera_button:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
                break;
        }
    }

    private void saveJournal() {
        String title = titleEditText.getText().toString().trim(),
                thoughts = thoughtEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thoughts)
                && imageUri != null) {
            StorageReference filepath = storageReference
                    .child("journal_images")
                    .child(currentUserId + Timestamp.now().getSeconds());
            filepath.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        filepath.getDownloadUrl().addOnSuccessListener(uri -> {
                            Journal journal = new Journal();
                            journal.setTitle(title);
                            journal.setThought(thoughts);
                            journal.setImageUrl(uri.toString());
                            journal.setTimeAdded(new Timestamp(new Date()));
                            journal.setEmailId(currentUserEmail);
                            journal.setUserId(currentUserId);
                            collectionReference.add(journal)
                                    .addOnSuccessListener(documentReference -> {
                                        startActivity(new Intent(PostJournals.this,
                                                ShowJournals.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d("TAG", "saveJournal: " + e.toString());
                                    });
                        });
                        progressBar.setVisibility(View.INVISIBLE);
                    }).addOnFailureListener(e -> {
                progressBar.setVisibility(View.INVISIBLE);
            });
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}