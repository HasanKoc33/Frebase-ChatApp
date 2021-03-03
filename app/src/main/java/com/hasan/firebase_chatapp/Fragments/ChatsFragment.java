package com.hasan.firebase_chatapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hasan.firebase_chatapp.Activity.Rehber;
import com.hasan.firebase_chatapp.Adapter.UserAdapter;
import com.hasan.firebase_chatapp.Model.Chatlist;
import com.hasan.firebase_chatapp.Model.User;
import com.hasan.firebase_chatapp.Notifications.Token;
import com.hasan.firebase_chatapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    ExtendedFloatingActionButton rehber;
    private UserAdapter userAdapter;
    private List<User> mUsers= new ArrayList<>();

    FirebaseUser fuser;
    DatabaseReference reference;
    ValueEventListener chetsListener;


    private List<Chatlist> usersList;
    private List<Chatlist> usersList1;
    private AdView mAdView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        mAdView = view.findViewById(R.id.adView);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reklam();
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        usersList = new ArrayList<>();
        usersList1 = new ArrayList<>();
        rehber = view.findViewById(R.id.rehber);


        rehber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Rehber.class);
                startActivity(intent);
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser.getUid());
        chetsListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                usersList1.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    usersList.add(chatlist);
                }
                ArrayList<Long> sıra = new ArrayList<>();
                for (Chatlist user : usersList) {
                    long s0 = Long.parseLong(user.date);
                    sıra.add(s0);

                }

                Collections.sort(sıra);
                for (int i = sıra.size() - 1; i >= 0; i--) {
                    Long s0 = sıra.get(i);
                    for (Chatlist user : usersList) {
                        long s1 = Long.parseLong(user.date);
                        if (s0 == s1) {
                            usersList1.add(user);
                        }
                    }
                }

                chatList();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        chatList();
        chatList();
        updateToken(FirebaseInstanceId.getInstance().getToken());


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
    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(fuser.getUid()).setValue(token1);
    }

    private void chatList() {
        mUsers.clear();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (Chatlist chatlist : usersList1) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user.getId().equals(chatlist.getId())) {
                            mUsers.add(user);
                        }
                    }
                }
                userAdapter = new UserAdapter(getContext(), mUsers, true,recyclerView);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        reference.removeEventListener(chetsListener);

    }
}
