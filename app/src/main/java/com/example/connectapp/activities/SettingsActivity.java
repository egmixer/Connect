package com.example.connectapp.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.connectapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

public class SettingsActivity extends BaseActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    @BindView(R.id.main_toolbar)
    Toolbar toolbar;
    @BindView(R.id.tool_bar_txt)
    TextView toolbarTxt;
    @BindView(R.id.edit_profile_btn)
    Button editProfileBtn;
    @BindView(R.id.delete_account_btn)
    Button deleteAccountBtn;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarTxt.setText(getString(R.string.settings));
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
    }

    @OnClick(R.id.edit_profile_btn)
    void setEditProfileBtn() {
        startActivity(new Intent(SettingsActivity.this, EditProfileActivity.class));
    }

    @OnClick(R.id.delete_account_btn)
    void deleteAccount() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                SettingsActivity.this);
        builder.setMessage(
                getString(R.string.delete_account_msg))
                .setCancelable(false)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                final int id) {
                                showLoadingDialog(getString(R.string.delete_account), getString(R.string.wait));
                                String userRef = "Users/" + mFirebaseUser.getUid();
                                String friendsRef = "Friend/" + mFirebaseUser.getUid();
                                String messagesRef = "messages/" + mFirebaseUser.getUid();
                                final Map deleteMap = new HashMap<>();
                                deleteMap.put(userRef, null);
                                deleteMap.put(friendsRef, null);
                                deleteMap.put(messagesRef, null);
                                mDatabaseRef.child("Friend").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild(mFirebaseUser.getUid()))
                                            mDatabaseRef.child("Friend")
                                                    .child(dataSnapshot.getKey())
                                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mDatabaseRef.child("messages").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.hasChild(mFirebaseUser.getUid()))
                                                                mDatabaseRef
                                                                        .child("messages")
                                                                        .child(dataSnapshot.getKey())
                                                                        .removeValue()
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                mDatabaseRef.child("Chat").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                        if (dataSnapshot.hasChild(mFirebaseUser.getUid()))
                                                                                            mDatabaseRef.child("Chat").child(dataSnapshot.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void aVoid) {
                                                                                                    mDatabaseRef.updateChildren(deleteMap, new DatabaseReference.CompletionListener() {
                                                                                                        @Override
                                                                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                                            mFirebaseUser
                                                                                                                    .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onSuccess(Void aVoid) {
                                                                                                                    dismissDialog();
                                                                                                                    Log.d(TAG, "OK! Works fine!");
                                                                                                                    startActivity(new Intent(SettingsActivity.this, SignInActivity.class));
                                                                                                                }
                                                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                                                @Override
                                                                                                                public void onFailure(@NonNull Exception e) {
                                                                                                                    dismissDialog();
                                                                                                                    Log.e(TAG, "onFailure: " + e.getMessage());
                                                                                                                    Snackbar.make(findViewById(android.R.id.content), e.getMessage().toString(),
                                                                                                                            Snackbar.LENGTH_SHORT).show();
                                                                                                                }
                                                                                                            });
                                                                                                            if (databaseError != null) {
                                                                                                                dismissDialog();
                                                                                                                Log.e(TAG, "onFailure: " + databaseError.getMessage());
                                                                                                                Snackbar.make(findViewById(android.R.id.content), databaseError.getMessage(), Snackbar.LENGTH_SHORT).show();
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                                }
                                                                                            });
                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(DatabaseError databaseError) {
                                                                                        dismissDialog();
                                                                                        Log.e(TAG, "onFailure: " + databaseError.getMessage());
                                                                                        Snackbar.make(findViewById(android.R.id.content), databaseError.getMessage(), Snackbar.LENGTH_SHORT).show();
                                                                                    }
                                                                                });

                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        dismissDialog();
                                                                        Log.e(TAG, "onFailure: " + e.getMessage());
                                                                        Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {
                                                            dismissDialog();
                                                            Log.e(TAG, "onFailure: " + databaseError.getMessage());
                                                            Snackbar.make(findViewById(android.R.id.content), databaseError.getMessage(), Snackbar.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    dismissDialog();
                                                    Log.e(TAG, "onFailure: " + e.getMessage());
                                                    Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                                }
                                            });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        dismissDialog();
                                        Log.e(TAG, "onFailure: " + databaseError.getMessage());
                                        Snackbar.make(findViewById(android.R.id.content), databaseError.getMessage(), Snackbar.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                .setNegativeButton(R.string.later, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,
                                        final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
