package com.example.connectapp.widget;


import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.connectapp.R;
import com.example.connectapp.models.User;
import com.example.connectapp.utils.PicassoCache;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConnectRemoteService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return  new ListViewFactory(this.getApplicationContext());
    }

    class ListViewFactory implements RemoteViewsService.RemoteViewsFactory {
        private final String TAG = ConnectRemoteService.class.getSimpleName();
        private final Context context;
        private final ArrayList<User> contactsList = new ArrayList<>();
        private final List<String> idsList = new ArrayList<>();
        private final AppWidgetManager appWidgetManager;
        private final int[] appWidgetId;

        public ListViewFactory(Context context) {
            this.context = context;
            appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName component = new ComponentName(context, ConnectWidget.class);
            appWidgetId = appWidgetManager.getAppWidgetIds(component);
        }

        @Override
        public void onCreate() {
            fetchFriendsData();
        }

        private void fetchFriendsData() {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.connect_widget);
            if(firebaseUser!=null){
                DatabaseReference friendsDatabase = FirebaseDatabase.getInstance().getReference()
                        .child("Friend").child(firebaseUser.getUid());
                final DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference()
                        .child("Users");
                friendsDatabase.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot != null) {
                            String uId = dataSnapshot.getKey();
                            idsList.add(uId);
                            userDatabase.child(uId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User user = new User();
                                    user.setName(dataSnapshot.child("name").getValue().toString());
                                    user.setThumb_image(dataSnapshot.child("thumb_image").getValue().toString());
                                    user.setStatus(dataSnapshot.child("status").getValue().toString());
                                    user.setOnline(dataSnapshot.child("online").getValue().toString());
                                    contactsList.add(user);
                                    Log.d(TAG, "onDataChange: " + contactsList.size());
                                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_contacts_list);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    views.setViewVisibility(R.id.empty_txt, View.VISIBLE);
                                    views.setTextViewText(R.id.empty_txt, databaseError.getMessage());
                                    appWidgetManager.updateAppWidget(appWidgetId, views);
                                }
                            });
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
            }
            else {
                views.setViewVisibility(R.id.empty_txt, View.VISIBLE);
                views.setTextViewText(R.id.empty_txt, getString(R.string.no_contacts));
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }

        }

        @Override
        public void onDataSetChanged() {

        }

        @Override
        public void onDestroy() {
            contactsList.clear();
            idsList.clear();
        }

        @Override
        public int getCount() {
            return contactsList.size();
        }

        @Override
        public RemoteViews getViewAt(int i) {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.item_widget);
            if (contactsList.size() != 0) {
                User friend = contactsList.get(i);
                String id = idsList.get(i);
                rv.setTextViewText(R.id.user_name_txt, friend.getName());
                rv.setTextViewText(R.id.user_status_txt, friend.getStatus());
                if (friend.getThumb_image() != null && !friend.getThumb_image().trim().isEmpty()) {
                    try {
                        Bitmap bitmap = PicassoCache.get(context).load(Uri.parse(friend.getThumb_image())).get();
                        rv.setImageViewBitmap(R.id.user_avatar, bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, "getViewAt: " + e.getMessage());
                    }
                }
                Bundle extras = new Bundle();
                extras.putString("user_id", id);
                extras.putString("user_name", friend.getName());
                Intent fillInIntent = new Intent();
                fillInIntent.putExtras(extras);
                rv.setOnClickFillInIntent(R.id.user_name_txt, fillInIntent);
            }else{
                rv.setViewVisibility(R.id.empty_txt, View.VISIBLE);
                rv.setTextViewText(R.id.empty_txt, getString(R.string.no_contacts));
            }
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
