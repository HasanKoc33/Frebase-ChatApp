package com.hasan.firebase_chatapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hasan.firebase_chatapp.Model.User;
import com.hasan.firebase_chatapp.R;

import java.util.ArrayList;

public class Tel extends AppCompatActivity {

    EditText num , alan, username;
    DatabaseReference reference;
    ArrayList<String> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tel);

        num = findViewById(R.id.numara);
        alan = findViewById(R.id.alan);
        username = findViewById(R.id.username);
        isim();

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (kontrol()){
                    if (!username.getText().toString().isEmpty()) {
                        String numara = "+" + alan.getText().toString() + num.getText().toString();
                        String user = username.getText().toString();
                        if (!users.contains(user)) {
                            Intent i = new Intent(Tel.this, LoginActivity.class);
                            i.putExtra("user",user);
                            i.putExtra("num", numara);
                            startActivity(i);
                        }else {
                            Toast.makeText(getApplicationContext(),
                                    "Kullanıcı adı zaten alınmış",Toast.LENGTH_SHORT).show();
                        }

                    }else
                        Toast.makeText(getApplicationContext(),"isim giriniz efendim !!!",Toast.LENGTH_SHORT).show();

                }else
                    Toast.makeText(getApplicationContext(),"numara hatalı !!!",Toast.LENGTH_SHORT).show();

            }
        });
    }

    private boolean kontrol(){
        if (!num.getText().toString().isEmpty() &&
                num.getText().toString().length()==10 &&
                !alan.getText().toString().isEmpty()
        )
            return true;
        else return false;
    }

    private void isim(){
        users = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    User user = dataSnapshot1.getValue(User.class);
                   users.add(user.getUsername());
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}