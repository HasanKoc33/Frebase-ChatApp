package com.hasan.firebase_chatapp.Fragments.view;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hasan.firebase_chatapp.Fragments.foto;
import com.hasan.firebase_chatapp.Model.User;
import com.hasan.firebase_chatapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileFragment extends Fragment {

    private CircleImageView image_profile;
    private TextView username;

    private DatabaseReference reference;
    private User fuser;
    private String fuser_id;

    private StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;

    public static ViewProfileFragment newInstance() {
        return new ViewProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.view_profile_fragment, container, false);

        image_profile = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        fuser_id = getUserFromBundle();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser_id);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")){
                    image_profile.setImageResource(R.drawable.user);
                } else {
                    Glide.with(getContext()).load(user.getImageURL()).into(image_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
            }
        });

        return view;
    }



    public void openImage(){

        Fragment fragmentk = getFragmentManager().findFragmentByTag(getString(R.string.profilfoto));
        Fragment fragmentk1 = getFragmentManager().findFragmentByTag(getString(R.string.vievProfil));
        if (fragmentk!=null)  {
            getFragmentManager().beginTransaction().remove(fragmentk).commit();
        }
        else {
            getFragmentManager().beginTransaction().remove(fragmentk1).commit();
            foto fragment = new foto();
            Bundle args = new Bundle();
            args.putString(getString(R.string.profilfoto),fuser_id);
            fragment.setArguments(args);
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.Main, fragment, getString(R.string.profilfoto));
            transaction.addToBackStack(null);
            transaction.commit();
        }

    }



    private String getUserFromBundle(){
        Bundle bundle = this.getArguments();
        if(bundle != null){
            return bundle.getString(getString(R.string.intent_user));
        }else{
            return null;
        }
    }
}