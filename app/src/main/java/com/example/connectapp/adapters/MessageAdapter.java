package com.example.connectapp.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.connectapp.R;
import com.example.connectapp.models.Message;
import com.example.connectapp.utils.PicassoCache;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Message> mMessageList = new ArrayList<>();
    private final FirebaseAuth mAuth;

    public MessageAdapter(ArrayList<Message> mMessageList) {
        this.mMessageList = mMessageList;
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(viewType == 0 ? R.layout.item_message : R.layout.item_sender_message, parent, false);
        if (viewType == 0) {
            return new ReceiverHolder(v);
        } else {
            return new SenderHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message msg = mMessageList.get(position);
        if (getItemViewType(position) == 1) {
            if (msg.getType().equals("image"))
                ((SenderHolder) holder).setMyMsgImg(msg.getMessage());
            else
                ((SenderHolder) holder).setMyMsgTv(msg.getMessage());
        } else if (getItemViewType(position) == 0) {
            if (msg.getType().equals("image"))
                ((ReceiverHolder) holder).setUserMsgImg(msg.getMessage());
            else
                ((ReceiverHolder) holder).setUserMsgTv(msg.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        String userId = mAuth.getCurrentUser().getUid();
        Message msg = mMessageList.get(position);
        String fromUser = msg.getFrom();
        if (fromUser.equals(userId)) {
            return 1; // sender
        } else {
            return 0; // receiver
        }
    }

    public class SenderHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.my_image_msg)
        ImageView myMsgImage;
        @BindView(R.id.my_msg_tv)
        TextView myMsgTv;
        @BindView(R.id.progress_my)
        ProgressBar myProgressBar;

        public SenderHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void setMyMsgTv(String msg) {
            myMsgImage.setVisibility(View.GONE);
            myMsgTv.setVisibility(View.VISIBLE);
            myProgressBar.setVisibility(View.INVISIBLE);
            myMsgTv.setText(msg);
        }

        private void setMyMsgImg(final String msg) {
            myMsgTv.setVisibility(View.GONE);
            myMsgImage.setVisibility(View.VISIBLE);
            myProgressBar.setVisibility(View.VISIBLE);
            PicassoCache.get(itemView.getContext())
                    .load(msg)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(myMsgImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            myProgressBar.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onError() {
                            Toast.makeText(itemView.getContext(), R.string.error_load_image,Toast.LENGTH_SHORT).show();
                            myProgressBar.setVisibility(View.INVISIBLE);
                            Picasso.with(itemView.getContext())
                                    .load(msg)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(myMsgImage);
                        }
                    });
        }

    }

    public class ReceiverHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_msg_tv)
        TextView userMsgTv;
        @BindView(R.id.user_image_msg)
        ImageView userMsgImg;
        @BindView(R.id.progress_user)
        ProgressBar userProgressBar;

        public ReceiverHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void setUserMsgTv(String msg) {
            userMsgImg.setVisibility(View.GONE);
            userMsgTv.setVisibility(View.VISIBLE);
            userProgressBar.setVisibility(View.INVISIBLE);
            userMsgTv.setText(msg);
        }

        private void setUserMsgImg(final String msg) {
            userMsgTv.setVisibility(View.GONE);
            userMsgImg.setVisibility(View.VISIBLE);
            userProgressBar.setVisibility(View.VISIBLE);
            PicassoCache.get(itemView.getContext())
                    .load(msg)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(userMsgImg, new Callback() {
                        @Override
                        public void onSuccess() {
                            userProgressBar.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onError() {
                            Toast.makeText(itemView.getContext(), R.string.error_load_image,Toast.LENGTH_SHORT).show();
                            userProgressBar.setVisibility(View.INVISIBLE);
                            Picasso.with(itemView.getContext())
                                    .load(msg)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(userMsgImg);
                        }
                    });
        }
    }
}
