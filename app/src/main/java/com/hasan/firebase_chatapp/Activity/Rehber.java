package com.hasan.firebase_chatapp.Activity;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hasan.firebase_chatapp.Adapter.UserAdapter;
import com.hasan.firebase_chatapp.Model.User;
import com.hasan.firebase_chatapp.R;

import java.util.ArrayList;

public class Rehber extends AppCompatActivity {
    public static final int REQUEST_READ_CONTACTS = 79;
    RecyclerView recyclerView;
    ArrayList<String> numberArray;
    private UserAdapter userAdapter;

    Context context = this;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rehber);
        numberArray = new ArrayList();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            getAllContacts();
        } else {
            if (!requestPermission()){
                ayarlar();
            }
        }



    }

    private Boolean requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) {

            return false;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getAllContacts();
                } else {
                    // permission denied,Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void ayarlar(){
        final Dialog builder = new Dialog(context);
        builder.setContentView(R.layout.uyari);
        builder.setTitle(R.string.app_name);

        TextView mesaj = (TextView) builder.findViewById(R.id.mesaj);
        TextView evet = (TextView) builder.findViewById(R.id.evet);
        TextView iptal = (TextView) builder.findViewById(R.id.iptal);
        FrameLayout fm = (FrameLayout) builder.findViewById(R.id.back);
        mesaj.setText(getString(R.string.rizin));
        evet.setText(getString(R.string.ayarlar));
        iptal.setVisibility(View.GONE);
        fm.setBackground(getDrawable(R.color.hint));
        evet.setTextColor(getColor(R.color.dark));
        mesaj.setTextColor(getColor(R.color.dark));

        evet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
                Rehber.this.startActivity(intent);
                finish();
                builder.dismiss();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Intent intent = new Intent(Rehber.this, MainActivity.class);
                startActivity(intent);
                finish();
                builder.dismiss();
            }
        });


        builder.show();
    }

    private void getAllContacts() {
        ArrayList<String> nameList = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));
                nameList.add(name);

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        numberArray.add(phoneNo);

                    }
                    pCur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }
        users(numberArray);
    }

    private void users(ArrayList<String> arrayList){
        ArrayList<User> kayitli = new ArrayList();
        kayitli.clear();



        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();

        for (String s : arrayList) {
           String number=s.replaceAll("\\s+", "");

            Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("numara")
                    .startAt(s)
                    .endAt(s+"\uf8ff");

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);

                        assert user != null;
                        assert fuser != null;
                        if (!user.getId().equals(fuser.getUid())) {
                            kayitli.add(user);

                        }


                    }
                    listele(kayitli);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



        }
    }

    private void listele(ArrayList<User> kayitli){
        userAdapter = new UserAdapter(getApplicationContext(), kayitli, false,recyclerView);
        recyclerView.setAdapter(userAdapter);

    }
}
