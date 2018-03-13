package com.example.connectapp.fragmnets;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.connectapp.R;
import com.example.connectapp.activities.ChatActivity;
import com.example.connectapp.models.Chat;
import com.example.connectapp.utils.CircularTransform;
import com.example.connectapp.utils.GetTimeAgo;
import com.example.connectapp.utils.PicassoCache;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ChatsFragment extends Fragment {
    private static final String TAG = ChatsFragment.class.getSimpleName();
    @BindView(R.id.content_layout)
    RelativeLayout contentLayout;
    @BindView(R.id.chats_list)
    RecyclerView chatsList;
    @BindView(R.id.no_chats_txt)
    TextView noChatsTv;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;
    private DatabaseReference mChatsDatabase;

    public ChatsFragment() {
        // Required empty public constructor
    }

    public static ChatsFragment newInstance() {
        return new ChatsFragment();
    }

    private void showChats() {
        chatsList.setVisibility(View.VISIBLE);
        noChatsTv.setVisibility(View.INVISIBLE);
    }

    private void showNoChats() {
        chatsList.setVisibility(View.INVISIBLE);
        noChatsTv.setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chats, container, false);
        ButterKnife.bind(this, rootView);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = firebaseAuth.getCurrentUser();
        showNoChats();
        mChatsDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrentUser.getUid());
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mChatsDatabase.keepSynced(true);
        chatsList.setHasFixedSize(true);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Chat, ChatsViewHolder> chatsAdapter = new FirebaseRecyclerAdapter<Chat, ChatsViewHolder>(
                Chat.class,
                R.layout.item_chat,
                ChatsViewHolder.class,
                mChatsDatabase) {
            @Override
            protected void populateViewHolder(final ChatsViewHolder viewHolder, Chat model, int position) {
                if (model != null)
                    showChats();
                long time = 0;
                if (model != null) {
                    time = model.getTime_stamp();
                    viewHolder.setLastTimeStamp(GetTimeAgo.getTimeAgo(time));
                }
                final String userId = getRef(position).getKey();
                mRootRef.child("Users").child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String thumb = dataSnapshot.child("thumb_image").getValue().toString();
                        viewHolder.setUsername(userName);
                        if (thumb != null && !TextUtils.isEmpty(thumb)) {
                            viewHolder.setUserAvatar(thumb,
                                    getContext());
                        }
                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", userId);
                                chatIntent.putExtra("user_name", userName);
                                startActivity(chatIntent);

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        chatsList.setAdapter(chatsAdapter);

    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_name_tv)
        TextView username;
        @BindView(R.id.last_msg_time_tv)
        TextView lastTimeStamp;
        @BindView(R.id.user_avatar)
        ImageView userAvatar;

        public ChatsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setUsername(String name) {
            username.setText(name);
        }

        public void setLastTimeStamp(String time) {
            lastTimeStamp.setText(time);
        }

        public void setUserAvatar(final String thumbUrl, final Context context) {
            PicassoCache.get(context)
                    .load(thumbUrl)
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
                            Picasso.with(context)
                                    .load(thumbUrl)
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
