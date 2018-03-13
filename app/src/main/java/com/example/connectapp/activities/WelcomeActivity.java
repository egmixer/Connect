package com.example.connectapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.connectapp.R;
import com.example.connectapp.utils.CircularTransform;
import com.example.connectapp.utils.DocumentHelper;
import com.example.connectapp.utils.ImageUtils;
import com.example.connectapp.utils.PicassoCache;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

public class WelcomeActivity extends BaseActivity {
    private static final String TAG = WelcomeActivity.class.getSimpleName();
    private static final int PICK_IMAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 9001;
    @BindView(R.id.pick_image_btn)
    ImageButton pickImageBtn;
    @BindView(R.id.user_avatar_iv)
    ImageView userAvatar;
    @BindView(R.id.user_name_input)
    EditText usernameInput;
    @BindView(R.id.next_btn)
    Button nxtBtn;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mUserDatabase;
    private StorageReference mStorageRef;
    private String image = "";
    private String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        String uId = "";
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(getString(R.string.firebase_database_ref));
        if (mCurrentUser != null)
            uId = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(uId);
        mUserDatabase.keepSynced(true);
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    mUserDatabase.child("online").setValue("true");
                    mUserDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("name")) {
                                name = dataSnapshot.child("name").getValue().toString();
                                usernameInput.setText(name);
                            }
                            if (dataSnapshot.hasChild("image")) {
                                image = dataSnapshot.child("image").getValue().toString();
                                if (image != null && !TextUtils.isEmpty(image)) {
                                    PicassoCache.get(WelcomeActivity.this)
                                            .load(image)
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .placeholder(R.drawable.ic_place_holder)
                                            .resizeDimen(R.dimen.user_avatar_size, R.dimen.user_avatar_size)
                                            .centerCrop()
                                            .transform(new CircularTransform())
                                            .into(userAvatar, new Callback() {
                                                @Override
                                                public void onSuccess() {
                                                }

                                                @Override
                                                public void onError() {
                                                    Picasso.with(WelcomeActivity.this)
                                                            .load(image)
                                                            .placeholder(R.drawable.ic_place_holder)
                                                            .resizeDimen(R.dimen.user_avatar_size, R.dimen.user_avatar_size)
                                                            .centerCrop()
                                                            .transform(new CircularTransform())
                                                            .into(userAvatar);
                                                }
                                            });
                                }
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
                    FirebaseAuth.getInstance().signOut();
                }
            }
        };


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @OnClick(R.id.pick_image_btn)
    void setPickImageBtn() {
        if (ContextCompat.checkSelfPermission(WelcomeActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(WelcomeActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");
            startActivityForResult(getIntent, PICK_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE)
            if (resultCode == RESULT_OK) {
                Uri imagePath = data.getData();
                if (data.getData() != null) {
                    showLoadingDialog(getString(R.string.upload_image), getString(R.string.wait));
                    String filePath = DocumentHelper.getPath(this, imagePath);
                    final Uri file = Uri.fromFile(new File(ImageUtils.compressImage(filePath)));
                    StorageReference fileStorage = mStorageRef.child("profile_images").
                            child("profile_" + mCurrentUser.getUid() + ".jpg");
                    final StorageReference thumbStorage = mStorageRef.child("profile_images")
                            .child("thumbs").child("profile_" + mCurrentUser.getUid() + ".jpg");
                    fileStorage.putFile(imagePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            final String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                            UploadTask uploadTask = thumbStorage.putFile(file);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    String thumbUrl = task.getResult().getDownloadUrl().toString();
                                    if (task.isSuccessful()) {
                                        Map updateHash = new HashMap();
                                        updateHash.put("image", downloadUrl);
                                        updateHash.put("thumb_image", thumbUrl);
                                        mUserDatabase.updateChildren(updateHash).
                                                addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        dismissDialog();
                                                        if (!task.isSuccessful()) {
                                                            Log.d(TAG, "onFailure: " + task.getException());
                                                            Snackbar.make(findViewById(android.R.id.content), task.getException().toString(),
                                                                    Snackbar.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        Log.d(TAG, "onFailure: " + task.getException());
                                        dismissDialog();
                                        Snackbar.make(findViewById(android.R.id.content), task.getException().toString(),
                                                Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d(TAG, "onFailure: " + exception.getMessage());
                            dismissDialog();
                            Snackbar.make(findViewById(android.R.id.content), exception.getMessage(),
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.next_btn)
    void setNextBtn() {
        String name = usernameInput.getEditableText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            usernameInput.setError(getString(R.string.user_name_empty));
            return;
        }
        if (TextUtils.isEmpty(image)) {
            Snackbar.make(findViewById(android.R.id.content), R.string.avatar_empty,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }
        showLoadingDialog(getString(R.string.update_name), getString(R.string.wait));
        Map userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("status","Available");
        mUserDatabase.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dismissDialog();
                if (!task.isSuccessful()) {
                    Log.d(TAG, "onFailure: " + task.getException());
                    Snackbar.make(findViewById(android.R.id.content), task.getException().toString(),
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");
                    startActivityForResult(getIntent, PICK_IMAGE);
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.ask_for_permission),
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

}
