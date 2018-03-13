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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.connectapp.R;
import com.example.connectapp.adapters.MessageAdapter;
import com.example.connectapp.models.Message;
import com.example.connectapp.utils.CircularTransform;
import com.example.connectapp.utils.GetTimeAgo;
import com.example.connectapp.utils.PicassoCache;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

public class ChatActivity extends BaseActivity {
    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final int PICK_IMAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 9001;
    @BindView(R.id.chat_toolbar)
    Toolbar toolbar;
    @BindView(R.id.user_name_tv)
    TextView usernameTv;
    @BindView(R.id.last_seen_tv)
    TextView lastSeenTv;
    @BindView(R.id.avatar_img)
    ImageView avatarImg;
    @BindView(R.id.attach_btn)
    ImageButton attachBtn;
    @BindView(R.id.msg_tv)
    EditText msgInput;
    @BindView(R.id.send_btn)
    ImageButton sendBtn;
    @BindView(R.id.chat_list)
    RecyclerView messagesList;
    private DatabaseReference mRootRef;
    private String mCurrentUserId;
    private final ArrayList<Message> mMsgList = new ArrayList<>();
    private MessageAdapter msgAdapter;
    private StorageReference mChatPhotosStorageReference;
    private String chatUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        chatUser = getIntent().getStringExtra("user_id");
        String mChatUserName = getIntent().getStringExtra("user_name");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        sendBtn.setEnabled(false);
        mChatPhotosStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(getString(R.string.firebase_storage_url));
        usernameTv.setText(mChatUserName);
        initMsgsList();
        mRootRef.child("Users").child(chatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                if (image != null && !TextUtils.isEmpty(image)) {
                    PicassoCache.get(ChatActivity.this)
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_place_holder)
                            .resizeDimen(R.dimen.user_avatar_size, R.dimen.user_avatar_size)
                            .centerCrop()
                            .transform(new CircularTransform())
                            .into(avatarImg, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(ChatActivity.this)
                                            .load(image)
                                            .placeholder(R.drawable.ic_place_holder)
                                            .resizeDimen(R.dimen.user_avatar_size, R.dimen.user_avatar_size)
                                            .centerCrop()
                                            .transform(new CircularTransform())
                                            .into(avatarImg);
                                }
                            });
                }
                if (online.equals("true"))
                    lastSeenTv.setText(getString(R.string.online_now));
                else {
                    long lastTime = Long.parseLong(online);
                    String lastSeen = GetTimeAgo.getTimeAgo(lastTime);
                    lastSeenTv.setText(lastSeen);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mRootRef.child("messages").child(mCurrentUserId).child(chatUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message msg = dataSnapshot.getValue(Message.class);
                mMsgList.add(msg);
                msgAdapter.notifyDataSetChanged();
                messagesList.scrollToPosition(mMsgList.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        msgInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0)
                    sendBtn.setEnabled(true);
                else
                    sendBtn.setEnabled(false);
            }
        });
    }

    private void initMsgsList() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        messagesList.setHasFixedSize(true);
        messagesList.setLayoutManager(linearLayoutManager);
        msgAdapter = new MessageAdapter(mMsgList);
        messagesList.setAdapter(msgAdapter);
    }

    @OnClick(R.id.send_btn)
    void setSendBtn() {
        String msg = msgInput.getText().toString();
        msgInput.setText("");
        String currentUserRef = "messages/" + mCurrentUserId + "/" + chatUser;
        String chatUserRef = "messages/" + chatUser + "/" + mCurrentUserId;
        DatabaseReference userMsgPush = mRootRef.child("messages")
                .child(mCurrentUserId).child(chatUserRef).push();
        String pushId = userMsgPush.getKey();
        Map msgMap = new HashMap<>();
        msgMap.put("message", msg);
        msgMap.put("seen", false);
        msgMap.put("type", "text");
        msgMap.put("time_stamp", ServerValue.TIMESTAMP);
        msgMap.put("from", mCurrentUserId);
        Map msgUserMap = new HashMap<>();
        msgUserMap.put(currentUserRef + "/" + pushId, msgMap);
        msgUserMap.put(chatUserRef + "/" + pushId, msgMap);
        mRootRef.updateChildren(msgUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null)
                    Log.d(TAG, "onComplete Error: " + databaseError.getMessage());
            }
        });
        Map chatAddMap = new HashMap<>();
        chatAddMap.put("seen", false);
        chatAddMap.put("time_stamp", ServerValue.TIMESTAMP);
        Map chatUserMap = new HashMap<>();
        chatUserMap.put("Chat/" + mCurrentUserId + "/" + chatUser, chatAddMap);
        chatUserMap.put("Chat/" + chatUser + "/" + mCurrentUserId, chatAddMap);
        mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.d(TAG, "onComplete Error: " + databaseError.getMessage());
                }
            }
        });
        messagesList.scrollToPosition(mMsgList.size() - 1);
    }

    @OnClick(R.id.attach_btn)
    void setAttachBtn() {
        if (ContextCompat.checkSelfPermission(ChatActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ChatActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");
            startActivityForResult(getIntent, PICK_IMAGE);
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE)
            if (resultCode == RESULT_OK) {
                showLoadingDialog(getString(R.string.upload_image), getString(R.string.wait));
                Uri selectedImageUri = data.getData();
                StorageReference photoRef = mChatPhotosStorageReference.child("chat_images").
                        child("chat_" + selectedImageUri.getLastPathSegment() + ".jpg");
                photoRef.putFile(selectedImageUri)
                        .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                                Log.d(TAG, "onSuccess: " + downloadUrl);
                                String currentUserRef = "messages/" + mCurrentUserId + "/" + chatUser;
                                String chatUserRef = "messages/" + chatUser + "/" + mCurrentUserId;
                                DatabaseReference userMsgPush = mRootRef.child("messages")
                                        .child(mCurrentUserId).child(chatUserRef).push();
                                String pushId = userMsgPush.getKey();
                                Map msgMap = new HashMap<>();
                                msgMap.put("message", downloadUrl);
                                msgMap.put("seen", false);
                                msgMap.put("type", "image");
                                msgMap.put("time_stamp", ServerValue.TIMESTAMP);
                                msgMap.put("from", mCurrentUserId);
                                Map msgUserMap = new HashMap<>();
                                msgUserMap.put(currentUserRef + "/" + pushId, msgMap);
                                msgUserMap.put(chatUserRef + "/" + pushId, msgMap);
                                mRootRef.updateChildren(msgUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        dismissDialog();
                                        if (databaseError != null)
                                            Snackbar.make(findViewById(android.R.id.content),
                                                    databaseError.getMessage(), Snackbar.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissDialog();
                        Snackbar.make(findViewById(android.R.id.content),
                                e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
    }

}
