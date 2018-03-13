package com.example.connectapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.connectapp.R;
import com.example.connectapp.utils.CircularTransform;
import com.example.connectapp.utils.PicassoCache;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.OnClick;

public class ProfileActivity extends BaseActivity {
    @BindView(R.id.main_toolbar)
    Toolbar toolbar;
    @BindView(R.id.tool_bar_txt)
    TextView toolbarTxt;
    @BindView(R.id.user_avatar_iv)
    ImageView userAvatar;
    @BindView(R.id.status_tv)
    TextView statusTV;
    @BindView(R.id.phone_tv)
    TextView phoneTV;
    @BindView(R.id.send_message_btn)
    ImageButton sendMsgBtn;
    private String uId = "";
    String image = "";
    String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        uId = getIntent().getStringExtra("user_id");
        DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uId);
        showLoadingDialog(getString(R.string.profile_data), getString(R.string.wait));
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dismissDialog();
                if (dataSnapshot.hasChild("phone"))
                    phoneTV.setText(dataSnapshot.child("phone").getValue().toString());
                if (dataSnapshot.hasChild("status"))
                    statusTV.setText(dataSnapshot.child("status").getValue().toString());
                if (dataSnapshot.hasChild("image"))
                    image = dataSnapshot.child("image").getValue().toString();
                if (dataSnapshot.hasChild("name")) {
                    username = dataSnapshot.child("name").getValue().toString();
                    toolbarTxt.setText(username);
                }
                if (!TextUtils.isEmpty(image)) {
                    PicassoCache.get(ProfileActivity.this)
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
                                    Picasso.with(ProfileActivity.this)
                                            .load(image)
                                            .placeholder(R.drawable.ic_place_holder)
                                            .resizeDimen(R.dimen.user_avatar_size, R.dimen.user_avatar_size)
                                            .centerCrop()
                                            .into(userAvatar);
                                }
                            });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @OnClick(R.id.send_message_btn)
    void setSendMsgBtn() {
        Intent i = new Intent(ProfileActivity.this, ChatActivity.class);
        i.putExtra("user_id", uId);
        i.putExtra("user_name", username);
        startActivity(i);
    }
}
