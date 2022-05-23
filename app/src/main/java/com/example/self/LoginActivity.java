package com.example.self;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.internal.TaskUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import Util.JournalAPI;

public class LoginActivity extends AppCompatActivity {
    private Button createAccountButton, loginButton;
    private TextView orTextView;
    private EditText emailEditText, passwordEditText, confirmPasswordText;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        createAccountButton = findViewById(R.id.create_account_button);
        loginButton = findViewById(R.id.email_signin_button);
        orTextView = findViewById(R.id.orTextView);
        confirmPasswordText = findViewById(R.id.confirm_password);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        progressBar = findViewById(R.id.login_progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if(currentUser != null){
                    //already logged in
                } else {
                    //no user yet
                }
            }
        };

        createAccountButton.setOnClickListener(v -> {
            if(confirmPasswordText.getVisibility() == View.GONE){
                orTextView.setVisibility(View.GONE);
                loginButton.setVisibility(View.GONE);
                confirmPasswordText.setVisibility(View.VISIBLE);
            } else {
                String email, password, password2;
                email = emailEditText.getText().toString().trim();
                password = passwordEditText.getText().toString().trim();
                password2 = confirmPasswordText.getText().toString().trim();

                if(password.equals(password2) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                    createUserEmailaccount(email, password);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.create_account_error_text,
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        loginButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.signInWithEmailAndPassword(emailEditText.getText().toString().trim(),
                    passwordEditText.getText().toString().trim())
                    .addOnSuccessListener(authResult -> {
                        currentUser = firebaseAuth.getCurrentUser();
                        String currentUserId = currentUser.getUid();
                        JournalAPI.getInstance().setUserID(currentUserId);
                        JournalAPI.getInstance().setUserEmail(currentUser.getEmail());
                        startActivity(new Intent(LoginActivity.this,
                                ShowJournals.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.INVISIBLE);
                    });
        });
    }

    void createUserEmailaccount(String email, String password){
        findViewById(R.id.login_progressBar).setVisibility(View.VISIBLE);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d("TAG", "createUserEmailaccount: HERE BOYS");
                        currentUser = firebaseAuth.getCurrentUser();
                        Map<String, String> intro = new HashMap<>();
                        intro.put("Email", email);
                        db.collection(currentUser.getUid())
                                .document("INTRO")
                                .set(intro);

                        JournalAPI journalAPI = JournalAPI.getInstance();
                        journalAPI.setUserID(currentUser.getUid());
                        journalAPI.setUserEmail(email);
                        startActivity(new Intent(LoginActivity.this, ShowJournals.class));
                        finish();
                    }})
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "onFailure: " + e.toString());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}