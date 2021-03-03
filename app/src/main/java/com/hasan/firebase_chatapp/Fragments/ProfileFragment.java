package com.hasan.firebase_chatapp.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hasan.firebase_chatapp.Activity.Foto_Viev;
import com.hasan.firebase_chatapp.Activity.StartActivity;
import com.hasan.firebase_chatapp.Model.User;
import com.hasan.firebase_chatapp.R;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {

    CircleImageView image_profile;
    TextView username, Tel, sil, hesap;

    DatabaseReference reference;
    FirebaseUser fuser;
    StorageReference storageReference;
    private String url;

    ValueEventListener listener;
    ValueEventListener listener0;
    private AdView mAdView;

    String num, name;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAdView = view.findViewById(R.id.adView);
        image_profile = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);
        Tel = view.findViewById(R.id.Tel);
        sil = view.findViewById(R.id.sil);
        hesap = view.findViewById(R.id.erisim);
        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        reklam();
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        SharedPreferences ayarlar = getContext().getSharedPreferences("User", Context.MODE_PRIVATE);
        num = ayarlar.getString("num", null);
        name = ayarlar.getString("name", null);
        if (name!=null|| num!=null) {
            username.setText(name);
            Tel.setText(num);
        }

        yenile();
        listener = reference.child("imageURL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                yenile();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        listener0 = reference.child("search").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String s = dataSnapshot.getValue().toString();
                if (s.charAt(0) == '5' && s.length() == 10) {
                    hesap.setText(getString(R.string.Khesap));
                } else {
                    hesap.setText(getString(R.string.Ahesap));

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


        sil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hSil();
            }
        });

        hesap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                erisim();

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

    private void erisim() {
        final Dialog builder = new Dialog(getContext());
        builder.setContentView(R.layout.ayarlar);
        builder.setTitle(R.string.app_name);

        TextView kapali = (TextView) builder.findViewById(R.id.kapali);
        TextView acik = (TextView) builder.findViewById(R.id.acik);

        kapali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fuser = FirebaseAuth.getInstance().getCurrentUser();
                reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                String tel = Tel.getText().toString();
                String tel0 = "";
                for (int i = 0; i < tel.length(); i++) {
                    if (i > 2) {
                        tel0 += tel.charAt(i);
                    }
                }
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("search", tel0);
                hashMap.put("hesap", false);

                reference.updateChildren(hashMap);
                builder.dismiss();
            }
        });

        acik.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fuser = FirebaseAuth.getInstance().getCurrentUser();
                reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                String usernamee = username.getText().toString().toLowerCase();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("search", usernamee);
                hashMap.put("hesap", true);

                reference.updateChildren(hashMap);
                builder.dismiss();
            }
        });

        builder.show();


    }


    private void yenile() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (name==null|| num==null) {
                    username.setText(user.getUsername());
                    Tel.setText(user.getNumara());
                    SharedPreferences ayarlar1 = getContext().getSharedPreferences("User", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = ayarlar1.edit();
                    editor.putString("name", user.getUsername());
                    editor.putString("num", user.getNumara());
                    editor.commit();
                }
                url = user.getImageURL();

                if (user.getImageURL().equals("default")) {
                    image_profile.setImageResource(R.drawable.user);
                } else {
                    try {
                        Glide.with(getContext()).load(user.getImageURL()).into(image_profile);

                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
      /*  File root = Environment.getExternalStorageDirectory();
        File imgFile = new File(root.getAbsolutePath() + "/Selefkos/pf/" + fuser.getUid() + ".jpg");
        if(imgFile.exists()){

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            image_profile.setImageBitmap(myBitmap);

        }else{
            image_profile.setImageResource(R.drawable.user);
        }*/
    }


    private void openImage() {
        Intent i = new Intent(getActivity(), Foto_Viev.class);
        i.putExtra("8090", url);
        i.putExtra("8091", fuser.getUid());
        getActivity().startActivity(i);
    }


    private void hSil() {
        final Dialog builder = new Dialog(getContext());
        builder.setContentView(R.layout.uyari);
        builder.setTitle(R.string.app_name);


        TextView mesaj = (TextView) builder.findViewById(R.id.mesaj);
        TextView evet = (TextView) builder.findViewById(R.id.evet);
        TextView iptal = (TextView) builder.findViewById(R.id.iptal);
        mesaj.setText("Hesabınız kalıcı olarak tüm verileriniz ile birlikte silinecek  \nEmin misin?");

        evet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    assert fuser != null;
                    fuser.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "hesabınız silindi...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    startActivity(new Intent(getActivity(), StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                } catch (Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                }
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

    }

    @Override
    public void onPause() {
        super.onPause();
        reference.removeEventListener(listener);
        reference.removeEventListener(listener0);

    }
}
