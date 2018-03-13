package com.example.connectapp.fragmnets;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.connectapp.R;
import com.example.connectapp.activities.ChatActivity;
import com.example.connectapp.activities.ProfileActivity;
import com.example.connectapp.models.Friend;
import com.example.connectapp.utils.CircularTransform;
import com.example.connectapp.utils.PicassoCache;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ContactFragment extends Fragment {
    private static final String TAG = ContactFragment.class.getSimpleName();
    @BindView(R.id.content_layout)
    RelativeLayout contentLayout;
    @BindView(R.id.contacts_list)
    RecyclerView contactsList;
    @BindView(R.id.progress)
    ProgressBar progressBar;
    @BindView(R.id.no_contact_txt)
    TextView noContactTxt;
    private ArrayList<String> contacts = new ArrayList<>();
    private DatabaseReference mRootRef;
    private DatabaseReference mFriendsDatabase;
    private FirebaseUser mCurrentUser;
    private String uId = "";

    public ContactFragment() {
    }

    public static ContactFragment newInstance() {
        return new ContactFragment();
    }

    public void getContacts(ArrayList<String> contactList) {
        if (contactList.size() > 0) {
            contacts = contactList;
        } else {
            noContact();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact, container, false);
        ButterKnife.bind(this, rootView);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friend").child(mCurrentUser.getUid());
        mFriendsDatabase.keepSynced(true);
        initContactsList();
        noContact();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);
        showLoading();
        mRootRef.child("Users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                uId = dataSnapshot.getKey();
                final String currentDate = getCurrentDate();
                if (!uId.equals(mCurrentUser.getUid())) {
                    if (dataSnapshot.hasChild("phone")) {
                        String contactPhone = dataSnapshot.child("phone").getValue().toString();
                        for (String phone : contacts) {
                            if (contactPhone.contains(phone)) {
                                Map friendsMap = new HashMap();
                                friendsMap.put("Friend/" + mCurrentUser.getUid() + "/" + uId + "/date", currentDate);
                                friendsMap.put("Friend/" + uId + "/" + mCurrentUser.getUid() + "/date", currentDate);
                                mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError != null) {
                                            Snackbar.make(contentLayout, databaseError.getMessage(),
                                                    Snackbar.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
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
        return rootView;
    }

    private void initContactsList() {
        contactsList.setHasFixedSize(true);
        contactsList.setLayoutManager(new LinearLayoutManager(getContext()));
    }


    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        contactsList.setVisibility(View.INVISIBLE);
        noContactTxt.setVisibility(View.INVISIBLE);
    }

    private void showContacts() {
        progressBar.setVisibility(View.INVISIBLE);
        contactsList.setVisibility(View.VISIBLE);
        noContactTxt.setVisibility(View.INVISIBLE);
    }

    private void noContact() {
        progressBar.setVisibility(View.INVISIBLE);
        contactsList.setVisibility(View.INVISIBLE);
        noContactTxt.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friend, ContactsHolder> contactsAdapter = new FirebaseRecyclerAdapter<Friend, ContactsHolder>(
                Friend.class,
                R.layout.item_contact,
                ContactsHolder.class,
                mFriendsDatabase) {
            @Override
            protected void populateViewHolder(final ContactsHolder viewHolder, Friend model, int position) {
                if (model != null)
                    showContacts();
                else
                    noContact();
                viewHolder.setStatus(model.getDate());
                final String listUserId = getRef(position).getKey();
                mRootRef.child("Users").child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String online = dataSnapshot.child("online").getValue().toString();
                        String thumb = dataSnapshot.child("thumb_image").getValue().toString();
                        viewHolder.setUsername(userName);
                        if (thumb != null && !TextUtils.isEmpty(thumb)) {
                            viewHolder.setUserAvatar(thumb,
                                    getContext());
                        }
                        viewHolder.setOnlineStatus(online);
                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send a message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Option");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                                profileIntent.putExtra("user_id", listUserId);
                                                startActivity(profileIntent);
                                                break;
                                            case 1:
                                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                chatIntent.putExtra("user_id", listUserId);
                                                chatIntent.putExtra("user_name", userName);
                                                startActivity(chatIntent);
                                                break;
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        contactsList.setAdapter(contactsAdapter);
    }

    private String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return df.format(c.getTime());
    }

    public static class ContactsHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_avatar)
        ImageView avatar;
        @BindView(R.id.user_name_tv)
        TextView username;
        @BindView(R.id.user_status_tv)
        TextView mStatus;
        @BindView(R.id.user_status)
        ImageView isOnlineImg;

        public ContactsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void setUsername(String name) {
            username.setText(name);
        }

        void setStatus(String status) {
            mStatus.setText(status);
        }

        void setUserAvatar(final String thumbUrl, final Context context) {
            PicassoCache.get(context)
                    .load(thumbUrl)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.ic_place_holder)
                    .resizeDimen(R.dimen.user_avatar_size, R.dimen.user_avatar_size)
                    .centerCrop()
                    .transform(new CircularTransform())
                    .into(avatar, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(context)
                                    .load(thumbUrl)
                                    .placeholder(R.drawable.ic_place_holder)
                                    .resizeDimen(R.dimen.user_avatar_size, R.dimen.user_avatar_size)
                                    .centerCrop()
                                    .transform(new CircularTransform())
                                    .into(avatar);
                        }
                    });
        }

        void setOnlineStatus(String status) {
            if (status.equals("true"))
                isOnlineImg.setVisibility(View.VISIBLE);
            else
                isOnlineImg.setVisibility(View.INVISIBLE);
        }
    }

}


