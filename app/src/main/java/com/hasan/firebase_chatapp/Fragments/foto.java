package com.hasan.firebase_chatapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hasan.firebase_chatapp.Model.User;
import com.hasan.firebase_chatapp.R;

public class foto extends Fragment {

    View view;
    ImageView foto;


    DatabaseReference reference;

    String fuser_id;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_foto, container, false);
        foto = view.findViewById(R.id.foto);


        fuser_id = getUserFromBundle();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser_id);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
               // username.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    foto.setImageResource(R.drawable.user);
                } else {
                    Glide.with(getContext()).load(user.getImageURL()).into(foto);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    private String getUserFromBundle() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getString(getString(R.string.profilfoto));
        } else {
            return null;
        }
    }


}