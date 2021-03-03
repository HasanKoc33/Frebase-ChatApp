package com.hasan.firebase_chatapp.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hasan.firebase_chatapp.Adapter.UserAdapter;
import com.hasan.firebase_chatapp.Model.User;
import com.hasan.firebase_chatapp.R;
import com.hasan.firebase_chatapp.Util.OnLoadMoreListener;

import java.util.ArrayList;
import java.util.List;


public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> mUsers = new ArrayList<>();
    private List<User> mUsersL = new ArrayList<>();
    EditText search_users;
    private AdView mAdView;
    protected Handler handler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_users, container, false);
        mAdView = view.findViewById(R.id.adView);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        handler = new Handler();
        mUsers = new ArrayList<>();
        reklam();
        // readUsers();

        search_users = view.findViewById(R.id.search_users);

        search_users.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (search_users.getText().toString().charAt(0)=='5'
                            ||search_users.getText().toString().charAt(0)=='+'
                    ||search_users.getText().toString().charAt(0)=='0') {
                        if (search_users.getText().toString().length() > 10){
                            searchUsers(search_users.getText().toString().toLowerCase(), "numara");
                            if (search_users.getText().toString().charAt(0)=='0'){
                                String s0 = search_users.getText().toString().toLowerCase();
                                String s ="";
                                for (int i =1; i<s0.length(); i++) s+=s0.charAt(i);
                                searchUsers(s, "search");
                            }

                        }

                        else if (search_users.getText().toString().length() == 10)
                            searchUsers(search_users.getText().toString().toLowerCase(), "search");

                    }else {
                        searchUsers(search_users.getText().toString().toLowerCase(), "search");
                    }
                    return true;
                }
                return false;
            }
        });
        search_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!search_users.getText().toString().trim().equals("") && charSequence.charAt(0) != '5')
                    searchUsers(charSequence.toString().toLowerCase(),"search");
                else {
                    mUsers.clear();
                    yansit();
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        userAdapter = new UserAdapter(getContext(), mUsersL, false, recyclerView);
        recyclerView.setAdapter(userAdapter);
        userAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                //add null , so the adapter will check view_type and show progress bar at bottom
                mUsersL.add(null);
                userAdapter.notifyItemInserted(mUsersL.size() - 1);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //   remove progress item
                        if (mUsersL.size() != 0) {
                            mUsersL.remove(mUsersL.size() - 1);
                            userAdapter.notifyItemRemoved(mUsersL.size());
                            //add items one by one
                            int start = mUsersL.size();
                            int end = start + 10;

                            for (int i = start; i <= end; i++) {
                                if (mUsers.size() > i) {
                                    mUsersL.add(mUsers.get(i));
                                    userAdapter.notifyItemInserted(mUsersL.size());
                                }
                            }
                            userAdapter.setLoaded();
                        }
                        //or you can add all at once but do not forget to call mAdapter.notifyDataSetChanged();
                    }
                }, 2000);

            }
        });
        return view;
    }

    private void reklam() {
        MobileAds.initialize(getContext(), (OnInitializationCompleteListener) initializationStatus -> {
        });
        AdView adView = new AdView(getContext());

        adView.setAdSize(AdSize.BANNER);

        adView.setAdUnitId(getString(R.string.banner_reklam));

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void searchUsers(String s,String ara) {

        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild(ara)
                .startAt(s)
                .endAt(s + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    assert fuser != null;
                    if (!user.getId().equals(fuser.getUid())) {
                        mUsers.add(user);
                    }
                }
                yansit();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void readUsers() {

        mUsers.clear();
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    if (!user.getId().equals(firebaseUser.getUid())) {
                        mUsers.add(user);
                    }

                }
                Toast.makeText(getContext(), String.valueOf(mUsers.size()), Toast.LENGTH_SHORT).show();
                yansit();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void yansit() {
        if (mUsersL != null) {
            mUsersL.clear();
            userAdapter.notifyDataSetChanged();
        }
        for (int x = 0; x < 10; x++) {
            if (mUsers.size() > x) {
                mUsersL.add(mUsers.get(x));

                userAdapter.notifyItemInserted(mUsersL.size());

                userAdapter.setLoaded();
            }
        }
    }
}
