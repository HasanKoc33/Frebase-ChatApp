package com.hasan.firebase_chatapp.Activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hasan.firebase_chatapp.Fragments.ChatsFragment;
import com.hasan.firebase_chatapp.Fragments.ProfileFragment;
import com.hasan.firebase_chatapp.Fragments.UsersFragment;
import com.hasan.firebase_chatapp.Model.User;
import com.hasan.firebase_chatapp.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

// Reklam

public class MainActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    public Context context = this;
    int unread = 0;


    @SuppressLint("ResourceType")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);

        SharedPreferences ayarlar = getSharedPreferences("karsila", Context.MODE_PRIVATE);
        boolean karsilaa = ayarlar.getBoolean("karsila", true);

        if (karsilaa) {
            final Dialog builder = new Dialog(context);
            builder.setContentView(R.layout.karsila);

            TextView tv = (TextView) findViewById(R.id.mes);

            builder.show();
            SharedPreferences ayarlar1 = getSharedPreferences("karsila", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = ayarlar1.edit();
            editor.putBoolean("karsila", false);
            editor.commit();
        }
        if (isNetworkConnected()) {

            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    username.setText(user.getUsername());
                    if (user.getImageURL().equals("default")) {
                        profile_image.setImageResource(R.drawable.user);
                    } else {

                        Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            final TabLayout tabLayout = findViewById(R.id.tab_layout);
            final ViewPager viewPager = findViewById(R.id.view_pager);
            ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

            viewPagerAdapter.addFragment(new ChatsFragment(), "");

            viewPagerAdapter.addFragment(new UsersFragment(), "");

            viewPagerAdapter.addFragment(new ProfileFragment(), "");

            viewPager.setAdapter(viewPagerAdapter);

            tabLayout.setupWithViewPager(viewPager);
            tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#00ffff"));
            tabLayout.setSelectedTabIndicatorHeight((int) (6 * getResources().getDisplayMetrics().density));
           // tabLayout.setTabTextColors(Color.parseColor("#4a804d"), Color.parseColor("#ffff00"));
            tabLayout.setTabRippleColor(ColorStateList.valueOf(getColor(R.color.biz_bulduk)));
            TabLayout.Tab tab = tabLayout.getTabAt(0);
            tab.setIcon(R.drawable.chat);
            int tabIconColor = ContextCompat.getColor(context, R.color.cyan);
            tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
            TabLayout.Tab tab1 = tabLayout.getTabAt(1);
            tab1.setIcon(R.drawable.ic_baseline_search_24);
            int tabIconColor1 = ContextCompat.getColor(context, R.color.hint);
            tab1.getIcon().setColorFilter(tabIconColor1, PorterDuff.Mode.SRC_IN);
            TabLayout.Tab tab2 = tabLayout.getTabAt(2);
            tab2.setIcon(R.drawable.userp);
            int tabIconColor2 = ContextCompat.getColor(context, R.color.hint);
            tab2.getIcon().setColorFilter(tabIconColor2, PorterDuff.Mode.SRC_IN);
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    int tabIconColor = ContextCompat.getColor(context, R.color.cyan);
                    tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);

                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    int tabIconColor = ContextCompat.getColor(context, R.color.hint);
                    tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });


            profile_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TabLayout.Tab tab = tabLayout.getTabAt(2);
                    tab.select();
                }
            });


        } else {
            final Dialog builder = new Dialog(context);
            builder.setContentView(R.layout.uyari);
            builder.setTitle(R.string.app_name);

            TextView mesaj = (TextView) builder.findViewById(R.id.mesaj);
            TextView evet = (TextView) builder.findViewById(R.id.evet);
            TextView iptal = (TextView) builder.findViewById(R.id.iptal);
            FrameLayout fm = (FrameLayout) builder.findViewById(R.id.back);
            mesaj.setText(getString(R.string.interner));
            evet.setText(getString(R.string.tamam));
            iptal.setVisibility(View.GONE);
            fm.setBackground(getDrawable(R.color.hint));
            evet.setTextColor(getColor(R.color.dark));
            mesaj.setTextColor(getColor(R.color.dark));

            evet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    moveTaskToBack(true);
                    finish();
                    builder.dismiss();
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    moveTaskToBack(true);
                    finish();
                    builder.dismiss();
                }
            });


            builder.show();


        }
        status(getString(R.string.online));
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }


    // menü şimdilik devredışı brakıldı
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.logout:
                final Dialog builder = new Dialog(context);
                builder.setContentView(R.layout.uyari);
                builder.setTitle(R.string.app_name);

                TextView mesaj = (TextView) builder.findViewById(R.id.mesaj);
                TextView evet = (TextView) builder.findViewById(R.id.evet);
                TextView iptal = (TextView) builder.findViewById(R.id.iptal);
                mesaj.setText("Çıkış yapmak istedigine  \nEmin misin?");

                evet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(MainActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        builder.dismiss();
                    }
                });
                iptal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        builder.dismiss();
                    }
                });

                builder.show();
                return true;
        }

        return false;
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        // Ctrl + O

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    private void status(String status) {
        if (isNetworkConnected()) {
            reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", status);
            reference.updateChildren(hashMap);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        status(getString(R.string.online));
    }

    @Override
    protected void onResume() {
        super.onResume();
        status(getString(R.string.online));
    }

    @Override
    protected void onPause() {
        super.onPause();
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        String zaman = df.format("yyyy/MM/dd HH:mm", new Date()).toString();
        status(getString(R.string.sg) + zaman);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        String zaman = df.format("yyyy/MM/dd HH:mm", new Date()).toString();
        status(getString(R.string.sg) + zaman);


    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            finish();
        }
        return false;
    }
}
