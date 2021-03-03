package com.hasan.firebase_chatapp.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hasan.firebase_chatapp.R;


import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    EditText kod;
    Button btn_login, sendCode;
    ProgressBar bekleme;
    AppCompatImageView onay;

    FirebaseAuth auth;
    DatabaseReference reference;

    PhoneAuthProvider.ForceResendingToken mResendToken;
    String mVerificationId;
    String numara;
    String codesend;
    String username;
    TextView forgot_password;
    int time = 60;
    CountDownTimer Cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("tr");

        bekleme = findViewById(R.id.onay);
        onay = findViewById(R.id.onayc);

        kod = findViewById(R.id.code);


        btn_login = findViewById(R.id.btn_login);

        numara = getIntent().getStringExtra("num");
        username = getIntent().getStringExtra("user");

        kodsend(numara);

        TextView textTimer = (TextView) findViewById(R.id.timer);

        Cd = new CountDownTimer(59000, 1000) {

            public void onTick(long millisUntilFinished) {
                textTimer.setText(checkDigit(time));
                time--;
            }

            public void onFinish() {
                Toast.makeText(getApplicationContext(), "Süreniz doldu tekrar deneyiniz !!!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this, Tel.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            }

        }.start();

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bekleme.setVisibility(View.VISIBLE);
                onay.setVisibility(View.GONE);
                dogrula();
            }
        });


    }

    private void dogrula() {
        String code = kod.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codesend, code);
        giris(credential, username);
    }

    private void kodsend(String n) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(n)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)   // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);


    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {

            codesend = credential.getSmsCode();
            if (codesend != null) {
                giris(credential, username);
            }


        }

        @Override
        public void onVerificationFailed(FirebaseException e) {


            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            kod.setText(e.getMessage());

            if (e instanceof FirebaseAuthInvalidCredentialsException) {

            } else if (e instanceof FirebaseTooManyRequestsException) {

            }


        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {

            codesend = verificationId;


            // ...
        }
    };

    private void giris(PhoneAuthCredential credential, String name) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            assert firebaseUser != null;
                            String userid = firebaseUser.getUid();

                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
                            String userName = name;
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("username", userName);
                            hashMap.put("numara", numara);
                            hashMap.put("imageURL", "default");
                            hashMap.put("status", "offline");
                            hashMap.put("search", userName.toLowerCase());
                            hashMap.put("hesap",true);

                            SharedPreferences ayarlar1 = getSharedPreferences("User", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = ayarlar1.edit();
                            editor.putString("name", userName);
                            editor.putString("num", numara);
                            editor.commit();

                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @SuppressLint("UseCompatLoadingForDrawables")
                                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    bekleme.setVisibility(View.GONE);
                                    onay.setImageDrawable(getDrawable(R.drawable.onay));
                                    onay.setVisibility(View.VISIBLE);
                                    if (task.isSuccessful()) {
                                        Cd.cancel();
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        } else {
                            // Sign in failed, display a message and update the UI

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(), "Kodunuz hatalı!!!", Toast.LENGTH_SHORT).show();
                                bekleme.setVisibility(View.GONE);
                                onay.setVisibility(View.VISIBLE);
                                onay.setImageDrawable(getDrawable(R.drawable.cancel));
                            }
                        }
                    }
                });

    }


    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK ){
            Cd.cancel();
            finish();
        }
        return true;

    }
}
