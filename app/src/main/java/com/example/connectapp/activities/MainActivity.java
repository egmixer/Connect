package com.example.connectapp.activities;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.connectapp.R;
import com.example.connectapp.adapters.ViewPagerAdapter;
import com.example.connectapp.fragmnets.ChatsFragment;
import com.example.connectapp.fragmnets.ContactFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;

import butterknife.BindView;

import static android.Manifest.permission.READ_CONTACTS;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_READ_CONTACTS = 0;
    @BindView(R.id.main_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tab_pager)
    ViewPager mViewPager;
    @BindView(R.id.main_tabs)
    TabLayout mTabLayout;
    @BindView(R.id.tool_bar_txt)
    TextView toolbarTxt;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ContactFragment contactFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(mToolbar);
        toolbarTxt.setText(getString(R.string.app_name));
        contactFragment = ContactFragment.newInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        Log.d(TAG, "onCreate: " + mCurrentUser.getPhoneNumber());
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    setupViewPager(mViewPager);
                    mTabLayout.setupWithViewPager(mViewPager);
                    mUserDatabase.child("online").setValue("true");
                    populateContacts();
                } else {
                    mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                }
            }
        };
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(ChatsFragment.newInstance(), "Chat");
        adapter.addFragment(contactFragment, "Contacts");
        viewPager.setAdapter(adapter);
    }

    private void populateContacts() {
        if (!mayRequestContacts()) {
            return;
        }
        ContactTask contactTask = new ContactTask();
        contactTask.execute();
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(findViewById(android.R.id.content), R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateContacts();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_logout_btn) {
            FirebaseAuth.getInstance().signOut();
            if (mAuth.getCurrentUser() == null)
                mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
        }
        if (item.getItemId() == R.id.main_settings_btn)
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        return true;
    }

    private class ContactTask extends AsyncTask<Void, ArrayList<String>, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> phones = new ArrayList<>();
            ContentResolver cr = getContentResolver();
            // Read Contacts
            Cursor c = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.Contacts._ID,
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.RawContacts.ACCOUNT_TYPE},
                    ContactsContract.RawContacts.ACCOUNT_TYPE + " <> 'google' ",
                    null, null);
            if (c.getCount() <= 0) {
                Toast.makeText(MainActivity.this, R.string.no_contacts, Toast.LENGTH_SHORT).show();
            } else {
                while (c.moveToNext()) {
                    phones.add(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                }
            }
            return phones;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            contactFragment.getContacts(strings);
        }
    }

}
